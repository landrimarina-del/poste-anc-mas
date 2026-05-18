# Sprint Execution Plan - Sprint 8

Data: 2026-05-15  
Owner orchestrazione: develop-coordinator

## Obiettivo Sprint
Implementare la vertical slice Home Supervisore:
- C6.5
- C6.6
- C6.7
- C6.8
- US-E9.01..US-E9.05

Riferimenti Discovery:
- [docs/out-discovery-business-analist/03_Roadmap_Porting.md](../out-discovery-business-analist/03_Roadmap_Porting.md)
- [docs/out-discovery-business-analist/04_Epic_UserStories.md](../out-discovery-business-analist/04_Epic_UserStories.md)
- [docs/out-discovery-business-analist/06_Dipendenze_Funzionali.md](../out-discovery-business-analist/06_Dipendenze_Funzionali.md)
- [docs/out-discovery-architect/05_API_Candidate.md](../out-discovery-architect/05_API_Candidate.md)
- [docs/out-discovery-architect/06_State_Management.md](../out-discovery-architect/06_State_Management.md)
- [docs/out-discovery-ux-mapper/BPM_Task_UI_Mapping.md](../out-discovery-ux-mapper/BPM_Task_UI_Mapping.md)

## Scope Sprint 8
1. Dashboard Home Supervisore con 3 contatori (Attivita, Pratiche Attive, Pratiche Chiuse).
2. Istogramma Pratiche Giornaliere per mese selezionato.
3. Istogramma Pratiche Giornaliere Lavorate (OK/KO) per mese selezionato.
4. Istogramma Pratiche per Stato.
5. Selettore mese in Home Supervisore.
6. Enforcement ruolo SUPERVISORE lato UI e lato API.

Out of scope:
- dashboard segnalazioni Sinergia (Sprint 9).
- hardening UX/accessori (Sprint 10).

## Orchestrazione agenti

### Stream backend (develop-backend)
Consegne principali:
- API dashboard supervisore:
  - [apps/backend/src/main/java/it/poste/anc/supervision/api/SupervisionDashboardController.java](../../apps/backend/src/main/java/it/poste/anc/supervision/api/SupervisionDashboardController.java)
  - [apps/backend/src/main/java/it/poste/anc/supervision/application/SupervisionDashboardService.java](../../apps/backend/src/main/java/it/poste/anc/supervision/application/SupervisionDashboardService.java)
  - [apps/backend/src/main/java/it/poste/anc/supervision/api/SupervisionDashboardCountersResponse.java](../../apps/backend/src/main/java/it/poste/anc/supervision/api/SupervisionDashboardCountersResponse.java)
  - [apps/backend/src/main/java/it/poste/anc/supervision/api/SupervisionDailyOpenedPoint.java](../../apps/backend/src/main/java/it/poste/anc/supervision/api/SupervisionDailyOpenedPoint.java)
  - [apps/backend/src/main/java/it/poste/anc/supervision/api/SupervisionDailyWorkedPoint.java](../../apps/backend/src/main/java/it/poste/anc/supervision/api/SupervisionDailyWorkedPoint.java)
  - [apps/backend/src/main/java/it/poste/anc/supervision/api/SupervisionPracticeByStatePoint.java](../../apps/backend/src/main/java/it/poste/anc/supervision/api/SupervisionPracticeByStatePoint.java)
- Test backend Sprint 8:
  - [apps/backend/src/test/java/it/poste/anc/supervision/api/SupervisionDashboardControllerTest.java](../../apps/backend/src/test/java/it/poste/anc/supervision/api/SupervisionDashboardControllerTest.java)
  - [apps/backend/src/test/java/it/poste/anc/supervision/application/SupervisionDashboardServiceTest.java](../../apps/backend/src/test/java/it/poste/anc/supervision/application/SupervisionDashboardServiceTest.java)

### Stream frontend (develop-frontend)
Consegne principali:
- Home Supervisore e dashboard:
  - [apps/frontend/src/features/home/HomePage.jsx](../../apps/frontend/src/features/home/HomePage.jsx)
  - [apps/frontend/src/core/api/supervisionDashboardApi.js](../../apps/frontend/src/core/api/supervisionDashboardApi.js)
  - [apps/frontend/src/app/shell/AppShell.jsx](../../apps/frontend/src/app/shell/AppShell.jsx)
  - [apps/frontend/src/styles.css](../../apps/frontend/src/styles.css)
- Test frontend Sprint 8:
  - [apps/frontend/src/features/home/HomePage.test.jsx](../../apps/frontend/src/features/home/HomePage.test.jsx)

### Stream QA (develop-qa)
Consegne principali:
- Report Sprint 8 aggiornati con evidenze runtime:
  - [docs/out-develop-qa/Smoke_Test_Report_Sprint_8.md](../out-develop-qa/Smoke_Test_Report_Sprint_8.md)
  - [docs/out-develop-qa/Sprint_Test_Checklist_Sprint_8.md](../out-develop-qa/Sprint_Test_Checklist_Sprint_8.md)
  - [docs/out-develop-qa/Defect_List_Sprint_8.md](../out-develop-qa/Defect_List_Sprint_8.md)
  - [docs/out-develop-qa/BPM_Workflow_Validation_Sprint_8.md](../out-develop-qa/BPM_Workflow_Validation_Sprint_8.md)

## Validazione tecnica coordinator
1. Diagnostica IDE file Sprint 8: nessun errore rilevato.
2. Build Docker backend+frontend:
   - comando: `docker compose build backend frontend`
   - esito: PASS.
3. Test frontend Sprint 8:
   - comando: `npm --prefix apps/frontend run test -- --run HomePage.test.jsx`
   - esito: PASS (2/2).
4. Deploy stack aggiornato:
   - comando: `docker compose up -d`
   - esito: PASS (backend/frontend/db/reverse-proxy healthy/running).
5. Smoke runtime API dashboard:
   - GET `/api/v1/supervision/dashboard/counters` con `sup.verdi`: PASS (200, resultCode 0)
   - GET `/api/v1/supervision/dashboard/daily-opened?month=2026-05` con `sup.verdi`: PASS (200, resultCode 0)
   - GET `/api/v1/supervision/dashboard/daily-worked?month=2026-05` con `sup.verdi`: PASS (200, resultCode 0)
   - GET `/api/v1/supervision/dashboard/by-state` con `sup.verdi`: PASS (200, resultCode 0)
   - GET `/api/v1/supervision/dashboard/counters` con `op.rossi`: PASS (403, resultCode 6101)
   - GET `/api/v1/supervision/dashboard/daily-opened?month=2026-13` con `sup.verdi`: PASS (400, resultCode 6102)
6. Test backend dashboard containerizzati:
   - comando: `docker run --rm -v "${PWD}/apps/backend:/workspace" -w /workspace maven:3.9-eclipse-temurin-21 mvn -B "-Dtest=SupervisionDashboardControllerTest,SupervisionDashboardServiceTest" test`
   - esito: FAIL (5 errori su `SupervisionDashboardServiceTest` per bootstrap H2/Flyway, tabella `role` non trovata in contesto test).

## Esito orchestrazione Sprint 8
- Vertical slice runtime FE+BE eseguibile localmente: SI.
- Smoke QA runtime: completato con bilancio PASS 5 / FAIL 1 / BLOCKED 0.
- Conflitti cross-agent aperti: SI (filtro mese su by-state, coerenza BA vs API Candidate).
