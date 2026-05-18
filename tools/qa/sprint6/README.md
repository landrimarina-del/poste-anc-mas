# QA Sprint 6 - US-E7.01..US-E7.05

Questa cartella contiene runbook e script ripetibili per validare la vertical slice Sprint 6:
- checklist Carta
- CHIUDI PRATICA e transizione IN_ATTESA_CONFERMA_BPM
- outbound esito verso bpm-stub
- ACK bpm e finalizzazione CHIUSA_OK/CHIUSA_KO
- idempotenza ACK
- storico stati e audit

## Prerequisiti

- Stack ANC locale avviato (backend + frontend + bpm-stub)
- Backend raggiungibile (default: http://localhost:8080)
- Utente OPERATORE valido
- Pratiche test in stato IN_LAVORAZIONE con tipizzazione Carta
- PowerShell 7+ oppure Bash 4+

## Variabili ambiente supportate

Generali:
- `ANC_BASE_URL` (default `http://localhost:8080`)
- `ANC_USERNAME` (default `operatore`)
- `ANC_PASSWORD` (default `operatore`)
- `ANC_TOKEN` (opzionale)
- `ANC_BPM_STUB_BASE_URL` (es. `http://localhost:8090`)
- `ANC_BPM_STUB_OUTCOME_LOG_PATH` (default `/receive-outcome/logs`)

Per AC Sprint 6:
- `ANC_PRACTICE_ID_S6_01`
- `ANC_PRACTICE_ID_S6_02`
- `ANC_PRACTICE_ID_S6_03`
- `ANC_PRACTICE_ID_S6_04`
- `ANC_PRACTICE_ID_S6_06`
- `ANC_PRACTICE_ID_S6_07`
- `ANC_PRACTICE_ID_S6_08`
- `ANC_PRACTICE_ID_S6_09`
- `ANC_WORKITEM_ID_S6_06` (opzionale)
- `ANC_WORKITEM_ID_S6_07` (opzionale)
- `ANC_WORKITEM_ID_S6_08` (opzionale)

Nota: usare practice id distinti evita side effect tra casi.

## Esecuzione

PowerShell:

```powershell
pwsh ./tools/qa/sprint6/run-sprint6-qa.ps1
```

Bash:

```bash
bash ./tools/qa/sprint6/run-sprint6-qa.sh
```

Output generato:
- `tools/qa/sprint6/out/QA_Result_Sprint_6.md`

## Runbook manuale UI (obbligatorio)

1. Aprire una pratica con tipizzazione Carta confermata.
2. Verificare checklist Carta: presenza e conformita.
3. Eseguire combinazioni minime:
   - card_present=NO => outcome RESPINTA
   - card_present=SI + conformita=SI => APPROVATA
4. Eseguire CHIUDI PRATICA e confermare:
   - task rimosso da Lista Attivita
   - stato pratica IN_ATTESA_CONFERMA_BPM
5. Simulare ACK BPM OK e KO tramite endpoint `POST /api/v1/bpm/outcome-ack`.
6. Verificare stato finale CHIUSA_OK / CHIUSA_KO e data chiusura valorizzata.
7. Ripetere ACK su stessa pratica (replay) e verificare idempotenza.
8. Verificare tab Stati e Cronologia/audit per eventi close/finalize.

## BLOCKED-EXEC

Se la sessione non consente runtime locale, marcare gli AC come `BLOCKED-EXEC` nei report QA e usare questo runbook per esecuzione differita.