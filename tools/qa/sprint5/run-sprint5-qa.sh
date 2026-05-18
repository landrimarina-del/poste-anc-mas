#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${ANC_BASE_URL:-http://localhost:8080}"
USERNAME="${ANC_USERNAME:-operatore}"
PASSWORD="${ANC_PASSWORD:-operatore}"
TOKEN="${ANC_TOKEN:-}"
OUT_DIR="tools/qa/sprint5/out"
OUT_FILE="${OUT_DIR}/QA_Result_Sprint_5.md"

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
  printf "# QA Result Sprint 5\n\n| AC | Esito | Note |\n|---|---|---|\n| AC-S5-01..07 | BLOCKED-EXEC | Login/token non disponibile. Eseguire con ANC_TOKEN oppure credenziali valide. |\n" > "${OUT_FILE}"
  echo "Generato ${OUT_FILE}"
  exit 0
fi

run_case() {
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

P01="${ANC_PRACTICE_ID_S5_01:-}"
P02="${ANC_PRACTICE_ID_S5_02:-}"
P03="${ANC_PRACTICE_ID_S5_03:-}"
P04="${ANC_PRACTICE_ID_S5_04:-}"
P05="${ANC_PRACTICE_ID_S5_05:-}"
P06="${ANC_PRACTICE_ID_S5_06:-}"
P07="${ANC_PRACTICE_ID_S5_07:-}"

run_case "AC-S5-01" "${P01}" '{"documentoPresente":false,"formalOk":false,"leggibile":false,"datiClienteOk":false}' "2" '"outcome":"RESPINTA"'
run_case "AC-S5-02" "${P02}" '{"documentoPresente":true,"formalOk":true,"leggibile":true,"datiClienteOk":true}' "2" '"outcome":"APPROVATA"'
run_case "AC-S5-03" "${P03}" '{"documentoPresente":true,"formalOk":false,"causaliKo":[]}' "4" 'causale'
run_case "AC-S5-04" "${P04}" '{"documentoPresente":true,"formalOk":false,"causaliKo":["FIRME"]}' "2" '"outcome":"RESPINTA"'
run_case "AC-S5-05" "${P05}" '{"documentoPresente":true,"formalOk":true,"leggibile":true,"datiClienteOk":true}' "2" '"statoChecklist":"BOZZA"'

if [[ -z "${P06}" ]]; then
  log_blocked "AC-S5-06" "Practice ID non impostato (ANC_PRACTICE_ID_S5_06)."
else
  REOPEN_RESPONSE=$(request "POST" "/api/v1/practices/${P06}/intake/checklist/edit" "{}" || true)
  REOPEN_STATUS=$(extract_status "${REOPEN_RESPONSE}")
  REOPEN_BODY=$(extract_body "${REOPEN_RESPONSE}")
  if [[ "${REOPEN_STATUS}" == 2* ]] && contains "${REOPEN_BODY}" '"statoChecklist":"RIAPERTA"'; then
    RESULTS+="| AC-S5-06 | PASS | MODIFICA ha riaperto la checklist in RIAPERTA |\n"
  else
    RESULTS+="| AC-S5-06 | FAIL | MODIFICA non ha restituito stato RIAPERTA (HTTP ${REOPEN_STATUS}) |\n"
  fi
fi

if [[ -n "${P07}" ]]; then
  RESULTS+="| AC-S5-07 | BLOCKED-EXEC | Verifica UI manuale: campo note visibile solo se outcome RESPINTA. |\n"
else
  log_blocked "AC-S5-07" "Verifica UI manuale richiesta e Practice ID non impostato."
fi

{
  echo "# QA Result Sprint 5"
  echo
  echo "| AC | Esito | Note |"
  echo "|---|---|---|"
  printf "%b" "${RESULTS}"
} > "${OUT_FILE}"

echo "Generato ${OUT_FILE}"
