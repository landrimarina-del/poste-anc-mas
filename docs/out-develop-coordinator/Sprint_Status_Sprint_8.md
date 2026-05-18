# Sprint Status - Sprint 8

Data: 2026-05-15  
Sprint: 8  
Gate corrente: CONDITIONAL-GO

## Stato stream
- Backend: API dashboard operative a runtime; build Docker PASS; test backend dashboard containerizzati FAIL per setup test DB (H2/Flyway).
- Frontend: Home Supervisore completata; test Sprint 8 PASS (2/2).
- QA: smoke Sprint 8 aggiornati con evidenze runtime (PASS 5 / FAIL 1 / BLOCKED 0).

## Evidenze tecniche
Riferimenti principali:
- [apps/backend/src/main/java/it/poste/anc/supervision/api/SupervisionDashboardController.java](../../apps/backend/src/main/java/it/poste/anc/supervision/api/SupervisionDashboardController.java)
- [apps/backend/src/main/java/it/poste/anc/supervision/application/SupervisionDashboardService.java](../../apps/backend/src/main/java/it/poste/anc/supervision/application/SupervisionDashboardService.java)
- [apps/frontend/src/features/home/HomePage.jsx](../../apps/frontend/src/features/home/HomePage.jsx)
- [apps/frontend/src/core/api/supervisionDashboardApi.js](../../apps/frontend/src/core/api/supervisionDashboardApi.js)
- [apps/frontend/src/features/home/HomePage.test.jsx](../../apps/frontend/src/features/home/HomePage.test.jsx)
- [docs/out-develop-qa/Smoke_Test_Report_Sprint_8.md](../out-develop-qa/Smoke_Test_Report_Sprint_8.md)
- [docs/out-develop-qa/Defect_List_Sprint_8.md](../out-develop-qa/Defect_List_Sprint_8.md)

Validazioni coordinator:
1. `docker compose build backend frontend`: PASS.
2. `npm --prefix apps/frontend run test -- --run HomePage.test.jsx`: PASS (2/2).
3. `docker compose up -d`: PASS (stack healthy/running).
4. GET `/api/v1/supervision/dashboard/counters` con `sup.verdi`: PASS (200, resultCode 0).
5. GET `/api/v1/supervision/dashboard/daily-opened?month=2026-05` con `sup.verdi`: PASS (200, resultCode 0).
6. GET `/api/v1/supervision/dashboard/daily-worked?month=2026-05` con `sup.verdi`: PASS (200, resultCode 0).
7. GET `/api/v1/supervision/dashboard/by-state` con `sup.verdi`: PASS (200, resultCode 0).
8. GET `/api/v1/supervision/dashboard/counters` con `op.rossi`: PASS (403, resultCode 6101).
9. GET `/api/v1/supervision/dashboard/daily-opened?month=2026-13` con `sup.verdi`: PASS (400, resultCode 6102).
10. Test backend dashboard containerizzati: FAIL (11 run, 5 errori in `SupervisionDashboardServiceTest`, root cause bootstrap H2/Flyway su `V2__seed_demo_users`).

## Copertura Acceptance Sprint 8

| AC | Stato |
|---|---|
| AC-S8-01 contatori Home Supervisore | PASS |
| AC-S8-02 istogramma Pratiche Giornaliere | PASS |
| AC-S8-03 istogramma Pratiche Lavorate OK/KO | PASS |
| AC-S8-04 istogramma Pratiche per Stato | PASS |
| AC-S8-05 enforcement ruolo supervisore | PASS |
| AC-S8-06 filtro mese statistiche dashboard | FAIL* |

\* Fail dichiarato QA per rischio/coerenza incompleta su filtro mese del grafico by-state (DEF-S8-001), in attesa chiarimento formale BA/Architect.

Dettaglio report QA:
- [docs/out-develop-qa/Smoke_Test_Report_Sprint_8.md](../out-develop-qa/Smoke_Test_Report_Sprint_8.md)
- [docs/out-develop-qa/Sprint_Test_Checklist_Sprint_8.md](../out-develop-qa/Sprint_Test_Checklist_Sprint_8.md)
- [docs/out-develop-qa/Defect_List_Sprint_8.md](../out-develop-qa/Defect_List_Sprint_8.md)
- [docs/out-develop-qa/BPM_Workflow_Validation_Sprint_8.md](../out-develop-qa/BPM_Workflow_Validation_Sprint_8.md)

## Decisione gate
CONDITIONAL-GO.

Condizioni di chiusura post-go:
1. chiarire formalmente BA/Architect il comportamento atteso di `/supervision/dashboard/by-state` rispetto al mese selezionato (issue DEF-S8-001).
2. riallineare test backend `SupervisionDashboardServiceTest` al contesto Flyway/H2 o migrare a strategia test DB coerente con schema MariaDB.
