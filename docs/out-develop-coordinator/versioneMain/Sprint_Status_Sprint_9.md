# Sprint Status - Sprint 9

Data: 2026-05-16  
Sprint: 9  
Gate corrente: GO

## Stato stream
- Backend: modulo Signals operativo a runtime, migrazione V13 applicata in container, build Docker PASS.
- Frontend: dashboard segnalazioni integrata e test Sprint 9 PASS.
- QA: report Sprint 9 riallineati con evidenze runtime (smoke PASS 9/9, checklist PASS 10/10, rilievo BPM `take` chiuso per de-scope formale approvato).

## Evidenze tecniche
Riferimenti principali:
- [infra/db/migrations/V13__sprint9_signals_dashboard.sql](../../infra/db/migrations/V13__sprint9_signals_dashboard.sql)
- [apps/backend/src/main/java/it/poste/anc/signals/api/SignalController.java](../../apps/backend/src/main/java/it/poste/anc/signals/api/SignalController.java)
- [apps/backend/src/main/java/it/poste/anc/signals/application/SignalService.java](../../apps/backend/src/main/java/it/poste/anc/signals/application/SignalService.java)
- [apps/frontend/src/features/signals/SignalsDashboardPage.jsx](../../apps/frontend/src/features/signals/SignalsDashboardPage.jsx)
- [apps/frontend/src/core/api/signalsApi.js](../../apps/frontend/src/core/api/signalsApi.js)
- [docs/out-develop-qa/Smoke_Test_Report_Sprint_9.md](../out-develop-qa/Smoke_Test_Report_Sprint_9.md)
- [docs/out-develop-qa/Defect_List_Sprint_9.md](../out-develop-qa/Defect_List_Sprint_9.md)

Validazioni coordinator:
1. `docker compose build backend frontend`: PASS.
2. `docker compose up -d`: PASS (servizi healthy/running).
3. `npm --prefix apps/frontend run test -- --run src/features/signals/SignalsDashboardPage.test.jsx src/core/api/signalsApi.test.js`: PASS (2 file, 7 test).
4. POST `/api/v1/signals` (op.rossi): PASS (200, resultCode 0, IN_CODA).
5. GET `/api/v1/signals/me` (op.rossi): PASS (200, resultCode 0).
6. GET `/api/v1/signals` (sup.verdi): PASS (200, resultCode 0).
7. GET `/api/v1/signals` (op.rossi): PASS (403, resultCode 7013).
8. POST `/api/v1/signals/{id}/reassign` (sup.verdi): PASS (200, IN_LAVORAZIONE).
9. POST `/api/v1/signals/{id}/forward-sinergia` (sup.verdi): PASS (200, CHIUSO, ticket valorizzato).
10. VC-1 pratica invariata durante ciclo segnalazione: PASS (practice id=1 `IN_LAVORAZIONE` before/after).

## Copertura Acceptance Sprint 9

| AC | Stato |
|---|---|
| AC-S9-01 invio segnalazione da contesto pratica | PASS |
| AC-S9-02 vista Le Mie Segnalazioni | PASS |
| AC-S9-03 vista globale supervisore + filtri | PASS |
| AC-S9-04 riassegnazione segnalazione | PASS |
| AC-S9-05 forward stub Sinergia con ticket | PASS |
| AC-S9-06 enforcement ruoli operatore/supervisore | PASS |
| AC-S9-07 VC-1 stato pratica invariato | PASS |

Dettaglio report QA:
- [docs/out-develop-qa/Smoke_Test_Report_Sprint_9.md](../out-develop-qa/Smoke_Test_Report_Sprint_9.md)
- [docs/out-develop-qa/Sprint_Test_Checklist_Sprint_9.md](../out-develop-qa/Sprint_Test_Checklist_Sprint_9.md)
- [docs/out-develop-qa/Defect_List_Sprint_9.md](../out-develop-qa/Defect_List_Sprint_9.md)
- [docs/out-develop-qa/BPM_Workflow_Validation_Sprint_9.md](../out-develop-qa/BPM_Workflow_Validation_Sprint_9.md)

## Decisione gate
GO.

Condizione residua: nessuna.

Stato azione coordinator:
1. richiesta formale BA/Architect chiusa con decisione in [docs/out-develop-coordinator/Decision_Log_Sprint_9.md](Decision_Log_Sprint_9.md).
2. issue ISS-S9-01 chiusa in [docs/out-develop-coordinator/CrossAgent_Issues_Sprint_9.md](CrossAgent_Issues_Sprint_9.md).
