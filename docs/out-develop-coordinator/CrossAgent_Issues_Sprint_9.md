# CrossAgent Issues - Sprint 9

Data: 2026-05-16

## Issue aperte

| ID | Severita | Tema | Owner | Stato | Note |
|---|---|---|---|---|---|
| - | - | Nessuna issue aperta | - | - | Tutte le issue Sprint 9 risultano chiuse. |

## Issue chiuse

| ID | Severita | Tema | Owner | Stato | Evidenza |
|---|---|---|---|---|---|
| ISS-S9-01 | SEV-3 | Incoerenza Discovery su step `signal.work`: presente in UX mapping T12, non presente in API Candidate C7.x | develop-backend / develop-qa / architect-discovery | CLOSED | Decisione Opzione B (de-scope formale endpoint `take`) in [docs/out-develop-coordinator/Decision_Log_Sprint_9.md](Decision_Log_Sprint_9.md). |
| ISS-S9-02 | SEV-2 | Conflitto iniziale output agenti (backend/frontend implementato vs QA FAIL totale) | develop-qa | CLOSED | QA Sprint 9 aggiornato con evidenze runtime coordinator in [docs/out-develop-qa/Smoke_Test_Report_Sprint_9.md](../out-develop-qa/Smoke_Test_Report_Sprint_9.md). |
| ISS-S9-03 | SEV-2 | Mismatch contratto FE/BE su create/reassign signals (`title`/`targetValue`) | develop-frontend | CLOSED | Allineamento payload in [apps/frontend/src/features/signals/SignalsDashboardPage.jsx](../../apps/frontend/src/features/signals/SignalsDashboardPage.jsx) e [apps/frontend/src/core/api/signalsApi.js](../../apps/frontend/src/core/api/signalsApi.js), smoke runtime PASS. |
| ISS-S9-04 | SEV-3 | Test frontend Sprint 9 instabile per isolamento DOM | develop-frontend | CLOSED | Fix cleanup/mocks in [apps/frontend/src/features/signals/SignalsDashboardPage.test.jsx](../../apps/frontend/src/features/signals/SignalsDashboardPage.test.jsx), esito test 7/7 PASS. |
| ISS-S9-05 | SEV-3 | Evidenza VC-1 mancante su segnalazioni | develop-qa / develop-coordinator | CLOSED | Verifica DB before/after su practice id=1: stato invariato `IN_LAVORAZIONE` durante create/reassign/forward segnalazione. |

## Coerenza cross-agent
- Backend e frontend allineati sugli endpoint Sprint 9: `/api/v1/signals`, `/me`, `/{id}/reassign`, `/{id}/forward-sinergia`.
- Enforcement ruoli validato a runtime: supervisore 200 su globale, operatore 403.
- Lifecycle segnalazione validato runtime: `IN_CODA -> IN_LAVORAZIONE -> CHIUSO`.
- VC-1 validato runtime: stato pratica invariato durante azioni segnalazione.

## Azioni immediate
1. Nessuna azione bloccante residua lato coordinator per Sprint 9.
2. Eventuali evolutive su endpoint `take` da pianificare come enhancement di sprint successivo.
