#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${ANC_BASE_URL:-http://localhost:8080}"
USERNAME="${ANC_USERNAME:-operatore}"
PASSWORD="${ANC_PASSWORD:-operatore}"
TOKEN="${ANC_TOKEN:-}"
BPM_STUB_BASE_URL="${ANC_BPM_STUB_BASE_URL:-}"
BPM_STUB_OUTCOME_LOG_PATH="${ANC_BPM_STUB_OUTCOME_LOG_PATH:-/receive-outcome/logs}"
OUT_DIR="tools/qa/sprint6/out"
OUT_FILE="${OUT_DIR}/QA_Result_Sprint_6.md"

mkdir -p "${OUT_DIR}"

log_blocked() {
  local id="$1"
  local msg="$2"
  RESULTS+="| ${id} | BLOCKED-EXEC | ${msg} |\n"
}

request() {
  local method="$1"
  local path="$2"
  local body="${3:-}"
  local url="${BASE_URL}${path}"

  if [[ -n "${body}" ]]; then
    curl -sS -X "${method}" "${url}" \
      -H "Authorization: Bearer ${TOKEN}" \
      -H "Content-Type: application/json" \
      --data "${body}" \
      -w "\n%{http_code}"
  else
    curl -sS -X "${method}" "${url}" \
      -H "Authorization: Bearer ${TOKEN}" \
      -w "\n%{http_code}"
  fi
}

request_anon_get() {
  local absolute_url="$1"
  curl -sS -X GET "${absolute_url}" -w "\n%{http_code}" || true
}

extract_status() {
  echo "$1" | tail -n1
}

extract_body() {
  echo "$1" | sed '$d'
}

contains() {
  local haystack="$1"
  local needle="$2"
  [[ "${haystack}" == *"${needle}"* ]]
}

RESULTS=""

if [[ -z "${TOKEN}" ]]; then
  LOGIN_RESPONSE=$(curl -sS -X POST "${BASE_URL}/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    --data "{\"username\":\"${USERNAME}\",\"password\":\"${PASSWORD}\"}" \
    -w "\n%{http_code}" || true)

  LOGIN_STATUS=$(extract_status "${LOGIN_RESPONSE}")
  LOGIN_BODY=$(extract_body "${LOGIN_RESPONSE}")

  if [[ "${LOGIN_STATUS}" =~ ^2 ]]; then
    TOKEN=$(echo "${LOGIN_BODY}" | sed -n 's/.*"accessToken"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
    if [[ -z "${TOKEN}" ]]; then
      TOKEN=$(echo "${LOGIN_BODY}" | sed -n 's/.*"token"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
    fi
  fi
fi

if [[ -z "${TOKEN}" ]]; then
  printf "# QA Result Sprint 6\n\n| AC | Esito | Note |\n|---|---|---|\n| AC-S6-01..AC-S6-09 | BLOCKED-EXEC | Login/token non disponibile. Eseguire con ANC_TOKEN oppure credenziali valide. |\n" > "${OUT_FILE}"
  echo "Generato ${OUT_FILE}"
  exit 0
fi

run_checklist_case() {
  local case_id="$1"
  local practice_id="$2"
  local payload="$3"
  local expected_http_prefix="$4"
  local expected_body_token="$5"

  if [[ -z "${practice_id}" ]]; then
    log_blocked "${case_id}" "Practice ID non impostato (variabile ANC_PRACTICE_ID_...)."
    return
  fi

  RESPONSE=$(request "PUT" "/api/v1/practices/${practice_id}/intake/checklist" "${payload}" || true)
  STATUS=$(extract_status "${RESPONSE}")
  BODY=$(extract_body "${RESPONSE}")

  if [[ "${STATUS}" == ${expected_http_prefix}* ]] && contains "${BODY}" "${expected_body_token}"; then
    RESULTS+="| ${case_id} | PASS | HTTP ${STATUS}, token atteso trovato: ${expected_body_token} |\n"
  else
    RESULTS+="| ${case_id} | FAIL | HTTP ${STATUS}, atteso ${expected_http_prefix}xx + token ${expected_body_token} |\n"
  fi
}

run_close_case() {
  local case_id="$1"
  local practice_id="$2"

  if [[ -z "${practice_id}" ]]; then
    log_blocked "${case_id}" "Practice ID non impostato (ANC_PRACTICE_ID...)."
    return
  fi

  CLOSE_RESPONSE=$(request "POST" "/api/v1/practices/${practice_id}/intake/close" "{}" || true)
  CLOSE_STATUS=$(extract_status "${CLOSE_RESPONSE}")

  if [[ "${CLOSE_STATUS}" != 2* ]]; then
    RESULTS+="| ${case_id} | FAIL | CHIUDI PRATICA non riuscita (HTTP ${CLOSE_STATUS}) |\n"
    return
  fi

  DETAIL_RESPONSE=$(request "GET" "/api/v1/practices/${practice_id}" "" || true)
  DETAIL_STATUS=$(extract_status "${DETAIL_RESPONSE}")
  DETAIL_BODY=$(extract_body "${DETAIL_RESPONSE}")

  if [[ "${DETAIL_STATUS}" == 2* ]] && contains "${DETAIL_BODY}" "IN_ATTESA_CONFERMA_BPM"; then
    RESULTS+="| ${case_id} | PASS | Stato pratica in IN_ATTESA_CONFERMA_BPM dopo close |\n"
  else
    RESULTS+="| ${case_id} | FAIL | Stato post close non coerente (HTTP ${DETAIL_STATUS}) |\n"
  fi
}

run_ack_finalize_case() {
  local case_id="$1"
  local practice_id="$2"
  local ack_outcome="$3"
  local expected_final_state="$4"
  local workitem_id="$5"

  if [[ -z "${practice_id}" ]]; then
    log_blocked "${case_id}" "Practice ID non impostato (ANC_PRACTICE_ID...)."
    return
  fi

  if [[ -n "${workitem_id}" ]]; then
    ACK_PAYLOAD="{\"practiceId\":\"${practice_id}\",\"outcome\":\"${ack_outcome}\",\"idWorkitem\":\"${workitem_id}\"}"
  else
    ACK_PAYLOAD="{\"practiceId\":\"${practice_id}\",\"outcome\":\"${ack_outcome}\"}"
  fi

  ACK_RESPONSE=$(request "POST" "/api/v1/bpm/outcome-ack" "${ACK_PAYLOAD}" || true)
  ACK_STATUS=$(extract_status "${ACK_RESPONSE}")
  if [[ "${ACK_STATUS}" != 2* ]]; then
    RESULTS+="| ${case_id} | FAIL | ACK BPM non accettata (HTTP ${ACK_STATUS}) |\n"
    return
  fi

  DETAIL_RESPONSE=$(request "GET" "/api/v1/practices/${practice_id}" "" || true)
  DETAIL_STATUS=$(extract_status "${DETAIL_RESPONSE}")
  DETAIL_BODY=$(extract_body "${DETAIL_RESPONSE}")

  if [[ "${DETAIL_STATUS}" == 2* ]] && contains "${DETAIL_BODY}" "${expected_final_state}" && [[ "${DETAIL_BODY}" =~ dataChiusura|closureDate|closedAt ]]; then
    RESULTS+="| ${case_id} | PASS | ${expected_final_state} con data chiusura valorizzata |\n"
  else
    RESULTS+="| ${case_id} | FAIL | Finalizzazione non coerente: atteso ${expected_final_state} con data chiusura |\n"
  fi
}

run_ack_idempotence_case() {
  local case_id="$1"
  local practice_id="$2"
  local ack_outcome="$3"
  local expected_final_state="$4"
  local workitem_id="$5"

  if [[ -z "${practice_id}" ]]; then
    log_blocked "${case_id}" "Practice ID non impostato (ANC_PRACTICE_ID...)."
    return
  fi

  if [[ -n "${workitem_id}" ]]; then
    ACK_PAYLOAD="{\"practiceId\":\"${practice_id}\",\"outcome\":\"${ack_outcome}\",\"idWorkitem\":\"${workitem_id}\"}"
  else
    ACK_PAYLOAD="{\"practiceId\":\"${practice_id}\",\"outcome\":\"${ack_outcome}\"}"
  fi

  BEFORE_RESPONSE=$(request "GET" "/api/v1/practices/${practice_id}" "" || true)
  BEFORE_BODY=$(extract_body "${BEFORE_RESPONSE}")

  FIRST_ACK=$(request "POST" "/api/v1/bpm/outcome-ack" "${ACK_PAYLOAD}" || true)
  FIRST_ACK_STATUS=$(extract_status "${FIRST_ACK}")

  SECOND_ACK=$(request "POST" "/api/v1/bpm/outcome-ack" "${ACK_PAYLOAD}" || true)
  SECOND_ACK_STATUS=$(extract_status "${SECOND_ACK}")

  AFTER_RESPONSE=$(request "GET" "/api/v1/practices/${practice_id}" "" || true)
  AFTER_BODY=$(extract_body "${AFTER_RESPONSE}")

  if [[ "${FIRST_ACK_STATUS}" == 2* ]] && [[ "${SECOND_ACK_STATUS}" == 2* ]] && contains "${AFTER_BODY}" "${expected_final_state}" && [[ "${BEFORE_BODY}" == "${AFTER_BODY}" ]]; then
    RESULTS+="| ${case_id} | PASS | Replay ACK idempotente: stato finale invariato |\n"
  else
    RESULTS+="| ${case_id} | FAIL | Replay ACK ha alterato stato/risposta o HTTP non 2xx |\n"
  fi
}

run_audit_history_case() {
  local case_id="$1"
  local practice_id="$2"

  if [[ -z "${practice_id}" ]]; then
    log_blocked "${case_id}" "Practice ID non impostato (ANC_PRACTICE_ID...)."
    return
  fi

  STATES_RESPONSE=$(request "GET" "/api/v1/practices/${practice_id}/states" "" || true)
  STATES_STATUS=$(extract_status "${STATES_RESPONSE}")
  STATES_BODY=$(extract_body "${STATES_RESPONSE}")

  HISTORY_RESPONSE=$(request "GET" "/api/v1/practices/${practice_id}/history" "" || true)
  HISTORY_STATUS=$(extract_status "${HISTORY_RESPONSE}")
  HISTORY_BODY=$(extract_body "${HISTORY_RESPONSE}")

  if [[ "${STATES_STATUS}" == 2* ]] && [[ "${HISTORY_STATUS}" == 2* ]] && contains "${STATES_BODY}" "IN_ATTESA_CONFERMA_BPM" && ([[ "${STATES_BODY}" == *"CHIUSA_OK"* ]] || [[ "${STATES_BODY}" == *"CHIUSA_KO"* ]]) && [[ "${HISTORY_BODY}" =~ close|chiudi|finalize|ack ]]; then
    RESULTS+="| ${case_id} | PASS | Storico stati e audit coerenti su close/finalize |\n"
  else
    RESULTS+="| ${case_id} | FAIL | Storico stati/audit non coerenti o non reperibili |\n"
  fi
}

P01="${ANC_PRACTICE_ID_S6_01:-}"
P02="${ANC_PRACTICE_ID_S6_02:-}"
P03="${ANC_PRACTICE_ID_S6_03:-}"
P04="${ANC_PRACTICE_ID_S6_04:-}"
P06="${ANC_PRACTICE_ID_S6_06:-}"
P07="${ANC_PRACTICE_ID_S6_07:-}"
P08="${ANC_PRACTICE_ID_S6_08:-}"
P09="${ANC_PRACTICE_ID_S6_09:-}"

W06="${ANC_WORKITEM_ID_S6_06:-}"
W07="${ANC_WORKITEM_ID_S6_07:-}"
W08="${ANC_WORKITEM_ID_S6_08:-}"

run_checklist_case "AC-S6-01" "${P01}" '{"cardPresent":true,"cardConformita":true}' "2" 'card'
run_checklist_case "AC-S6-02" "${P02}" '{"cardPresent":false,"cardConformita":false}' "2" 'RESPINTA'
run_checklist_case "AC-S6-03" "${P03}" '{"cardPresent":true,"cardConformita":true}' "2" 'APPROVATA'
run_close_case "AC-S6-04" "${P04}"

if [[ -z "${BPM_STUB_BASE_URL}" ]]; then
  log_blocked "AC-S6-05" "ANC_BPM_STUB_BASE_URL non impostato. Impossibile verificare outbound verso bpm-stub."
else
  STUB_URL="${BPM_STUB_BASE_URL}${BPM_STUB_OUTCOME_LOG_PATH}"
  STUB_RESPONSE=$(request_anon_get "${STUB_URL}")
  STUB_STATUS=$(extract_status "${STUB_RESPONSE}")
  STUB_BODY=$(extract_body "${STUB_RESPONSE}")
  if [[ "${STUB_STATUS}" == 2* ]] && ([[ "${STUB_BODY}" == *"OK"* ]] || [[ "${STUB_BODY}" == *"KO"* ]]); then
    RESULTS+="| AC-S6-05 | PASS | Outbound verso bpm-stub rilevato con payload esito coerente |\n"
  else
    RESULTS+="| AC-S6-05 | FAIL | Outbound non verificato su ${STUB_URL} (HTTP ${STUB_STATUS}) |\n"
  fi
fi

run_ack_finalize_case "AC-S6-06" "${P06}" "OK" "CHIUSA_OK" "${W06}"
run_ack_finalize_case "AC-S6-07" "${P07}" "KO" "CHIUSA_KO" "${W07}"
run_ack_idempotence_case "AC-S6-08" "${P08}" "OK" "CHIUSA_OK" "${W08}"
run_audit_history_case "AC-S6-09" "${P09}"

{
  echo "# QA Result Sprint 6"
  echo
  echo "| AC | Esito | Note |"
  echo "|---|---|---|"
  printf "%b" "${RESULTS}"
} > "${OUT_FILE}"

echo "Generato ${OUT_FILE}"