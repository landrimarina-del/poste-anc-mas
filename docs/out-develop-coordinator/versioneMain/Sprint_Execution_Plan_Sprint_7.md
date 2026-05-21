# Sprint Execution Plan - Sprint 7

Data: 2026-05-15  
Owner orchestrazione: develop-coordinator

## Obiettivo Sprint
Implementare la vertical slice Supervisore Riassegna Attivita:
- C6.1
- C6.2
- C6.3
- C6.4
- US-E8.01..US-E8.05

Riferimenti Discovery:
- [docs/out-discovery-business-analist/03_Roadmap_Porting.md](../out-discovery-business-analist/03_Roadmap_Porting.md)
- [docs/out-discovery-business-analist/04_Epic_UserStories.md](../out-discovery-business-analist/04_Epic_UserStories.md)
- [docs/out-discovery-business-analist/06_Dipendenze_Funzionali.md](../out-discovery-business-analist/06_Dipendenze_Funzionali.md)
- [docs/out-discovery-architect/04_Workflow_Architecture.md](../out-discovery-architect/04_Workflow_Architecture.md)
- [docs/out-discovery-architect/05_API_Candidate.md](../out-discovery-architect/05_API_Candidate.md)
- [docs/out-discovery-architect/06_State_Management.md](../out-discovery-architect/06_State_Management.md)
- [docs/out-discovery-ux-mapper/BPM_Task_UI_Mapping.md](../out-discovery-ux-mapper/BPM_Task_UI_Mapping.md)

## Scope Sprint 7
1. Tab Riassegna Attivita visibile al solo ruolo SUPERVISORE.
2. Lista task supervisione con filtri: Pratica N, Data Assegnazione, Owner, Assegnatario.
3. Riassegnazione task a Gruppo Operatore ANC.
4. Riassegnazione task a Utente specifico operatore.
5. Enforcement autorizzativo backend solo SUPERVISORE_ANC.
6. Audit riassegnazione e storico assegnazioni.
7. Vincolo VC-1 preservato: nessuna modifica stato pratica durante riassegnazione.

Out of scope:
- dashboard supervisore con istogrammi (Sprint 8).
- segnalazioni Sinergia (Sprint 9).
- hardening/polish (Sprint 10).

## Orchestrazione agenti

### Stream backend (develop-backend)
Consegne principali:
- API supervision Sprint 7:
  - [apps/backend/src/main/java/it/poste/anc/supervision/api/SupervisionTaskController.java](../../apps/backend/src/main/java/it/poste/anc/supervision/api/SupervisionTaskController.java)
  - [apps/backend/src/main/java/it/poste/anc/supervision/application/SupervisionTaskService.java](../../apps/backend/src/main/java/it/poste/anc/supervision/application/SupervisionTaskService.java)
  - [apps/backend/src/main/java/it/poste/anc/supervision/api/SupervisionTaskListItem.java](../../apps/backend/src/main/java/it/poste/anc/supervision/api/SupervisionTaskListItem.java)
  - [apps/backend/src/main/java/it/poste/anc/supervision/api/SupervisionTaskReassignGroupRequest.java](../../apps/backend/src/main/java/it/poste/anc/supervision/api/SupervisionTaskReassignGroupRequest.java)
  - [apps/backend/src/main/java/it/poste/anc/supervision/api/SupervisionTaskReassignUserRequest.java](../../apps/backend/src/main/java/it/poste/anc/supervision/api/SupervisionTaskReassignUserRequest.java)
  - [apps/backend/src/main/java/it/poste/anc/supervision/api/SupervisionTaskReassignResponse.java](../../apps/backend/src/main/java/it/poste/anc/supervision/api/SupervisionTaskReassignResponse.java)
- Migrazione Sprint 7:
  - [infra/db/migrations/V12__sprint7_supervision_reassign.sql](../../infra/db/migrations/V12__sprint7_supervision_reassign.sql)
- Test backend:
  - [apps/backend/src/test/java/it/poste/anc/supervision/api/SupervisionTaskControllerTest.java](../../apps/backend/src/test/java/it/poste/anc/supervision/api/SupervisionTaskControllerTest.java)
  - [apps/backend/src/test/java/it/poste/anc/supervision/application/SupervisionTaskServiceTest.java](../../apps/backend/src/test/java/it/poste/anc/supervision/application/SupervisionTaskServiceTest.java)

### Stream frontend (develop-frontend)
Consegne principali:
- Routing/tab supervisore:
  - [apps/frontend/src/app/routes.jsx](../../apps/frontend/src/app/routes.jsx)
  - [apps/frontend/src/app/shell/AppShell.jsx](../../apps/frontend/src/app/shell/AppShell.jsx)
- Feature Riassegna Attivita:
  - [apps/frontend/src/features/supervisor/ReassignActivitiesPage.jsx](../../apps/frontend/src/features/supervisor/ReassignActivitiesPage.jsx)
  - [apps/frontend/src/core/api/supervisionTasksApi.js](../../apps/frontend/src/core/api/supervisionTasksApi.js)
  - [apps/frontend/src/styles.css](../../apps/frontend/src/styles.css)
- Test frontend:
  - [apps/frontend/src/features/supervisor/ReassignActivitiesPage.test.jsx](../../apps/frontend/src/features/supervisor/ReassignActivitiesPage.test.jsx)

### Stream QA (develop-qa)
Consegne principali:
- Report Sprint 7 aggiornati con evidenze runtime:
  - [docs/out-develop-qa/Smoke_Test_Report_Sprint_7.md](../out-develop-qa/Smoke_Test_Report_Sprint_7.md)
  - [docs/out-develop-qa/Sprint_Test_Checklist_Sprint_7.md](../out-develop-qa/Sprint_Test_Checklist_Sprint_7.md)
  - [docs/out-develop-qa/Defect_List_Sprint_7.md](../out-develop-qa/Defect_List_Sprint_7.md)
  - [docs/out-develop-qa/BPM_Workflow_Validation_Sprint_7.md](../out-develop-qa/BPM_Workflow_Validation_Sprint_7.md)

## Validazione tecnica coordinator
1. Lint/diagnostica file modificati: nessun errore IDE rilevato.
2. Build backend locale Maven su host: fallita per toolchain JDK locale non compatibile (release 21 non supportato).
3. Build backend Docker: PASS.
4. Build frontend Docker: PASS.
5. Test frontend Sprint 7:
   - comando: npm --prefix apps/frontend run test -- --run ReassignActivitiesPage.test.jsx
   - esito: PASS (4/4).
6. Deploy stack aggiornato:
   - comando: docker compose up -d backend frontend reverse-proxy
   - esito: PASS (servizi healthy).
7. Smoke runtime Sprint 7 API/DB:
   - supervisore GET /api/v1/supervision/tasks: PASS (200, resultCode 0)
   - operatore GET /api/v1/supervision/tasks: PASS (403, resultCode 6001)
   - filtri supervision tasks: PASS
   - reassign-group: PASS
   - reassign-user: PASS
   - VC-1 stato pratica immutato: PASS (practice 6 resta IN_LAVORAZIONE)
