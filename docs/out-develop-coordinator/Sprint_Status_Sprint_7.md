# Sprint Status - Sprint 7

Data: 2026-05-15  
Sprint: 7  
Gate corrente: GO

## Stato stream
- Backend: completato, build Docker PASS, endpoint supervision operativi.
- Frontend: completato, test Sprint 7 PASS, deploy stack aggiornato.
- QA: smoke Sprint 7 aggiornato a PASS su US-E8.01..E8.05 con evidenze runtime API/DB.

## Evidenze tecniche
Riferimenti principali:
- [infra/db/migrations/V12__sprint7_supervision_reassign.sql](../../infra/db/migrations/V12__sprint7_supervision_reassign.sql)
- [apps/backend/src/main/java/it/poste/anc/supervision/api/SupervisionTaskController.java](../../apps/backend/src/main/java/it/poste/anc/supervision/api/SupervisionTaskController.java)
- [apps/backend/src/main/java/it/poste/anc/supervision/application/SupervisionTaskService.java](../../apps/backend/src/main/java/it/poste/anc/supervision/application/SupervisionTaskService.java)
- [apps/frontend/src/features/supervisor/ReassignActivitiesPage.jsx](../../apps/frontend/src/features/supervisor/ReassignActivitiesPage.jsx)
- [apps/frontend/src/core/api/supervisionTasksApi.js](../../apps/frontend/src/core/api/supervisionTasksApi.js)
- [apps/frontend/src/features/supervisor/ReassignActivitiesPage.test.jsx](../../apps/frontend/src/features/supervisor/ReassignActivitiesPage.test.jsx)

Validazioni coordinator:
1. docker compose build backend: PASS.
2. docker compose build frontend: PASS.
3. npm --prefix apps/frontend run test -- --run ReassignActivitiesPage.test.jsx: PASS (4/4).
4. GET /api/v1/supervision/tasks con sup.verdi: PASS (200, resultCode 0).
5. GET /api/v1/supervision/tasks con op.rossi: PASS (403, resultCode 6001).
6. Reassign-group task 3: PASS (REASSIGN_GROUP, task IN_CODA).
7. Reassign-user task 3 verso op.bianchi: PASS (REASSIGN_USER, task IN_CARICO).
8. VC-1: PASS (practice 6 resta IN_LAVORAZIONE durante riassegnazioni).

Nota ambiente:
- test Maven host non eseguibile per mismatch JDK locale (release 21); validazione backend effettuata via build Docker con JDK 21 containerizzato.

## Copertura Acceptance Sprint 7

| AC | Stato |
|---|---|
| AC-S7-01 accesso tab/endpoint supervisore | PASS |
| AC-S7-02 filtri lista riassegna (pratica, owner, assegnatario) | PASS |
| AC-S7-03 riassegna a gruppo operatore ANC | PASS |
| AC-S7-04 riassegna a utente specifico | PASS |
| AC-S7-05 autorizzazione solo supervisore | PASS |
| AC-S7-06 VC-1 stato pratica immutato | PASS |

Dettaglio report QA:
- [docs/out-develop-qa/Smoke_Test_Report_Sprint_7.md](../out-develop-qa/Smoke_Test_Report_Sprint_7.md)
- [docs/out-develop-qa/Sprint_Test_Checklist_Sprint_7.md](../out-develop-qa/Sprint_Test_Checklist_Sprint_7.md)
- [docs/out-develop-qa/Defect_List_Sprint_7.md](../out-develop-qa/Defect_List_Sprint_7.md)

## Decisione gate
GO.

Condizioni post-go (non bloccanti):
1. completare evidenze UI visuali T13 (screen/video) in ciclo QA successivo.
2. aggiungere prova esplicita filtro Data Assegnazione in runbook QA runtime.
