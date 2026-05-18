# Sprint Execution Plan - Sprint 9

Data: 2026-05-15  
Owner orchestrazione: develop-coordinator

## Obiettivo Sprint
Implementare la vertical slice Dashboard Segnalazioni con Sinergia stub:
- C7.1
- C7.2
- C7.3
- C7.4
- C7.5
- US-E10.01..US-E10.05

Riferimenti Discovery:
- [docs/out-discovery-business-analist/03_Roadmap_Porting.md](../out-discovery-business-analist/03_Roadmap_Porting.md)
- [docs/out-discovery-business-analist/04_Epic_UserStories.md](../out-discovery-business-analist/04_Epic_UserStories.md)
- [docs/out-discovery-business-analist/06_Dipendenze_Funzionali.md](../out-discovery-business-analist/06_Dipendenze_Funzionali.md)
- [docs/out-discovery-architect/05_API_Candidate.md](../out-discovery-architect/05_API_Candidate.md)
- [docs/out-discovery-ux-mapper/BPM_Task_UI_Mapping.md](../out-discovery-ux-mapper/BPM_Task_UI_Mapping.md)

## Scope Sprint 9
1. Creazione segnalazione da contesto pratica ANC.
2. Vista Le Mie Segnalazioni.
3. Vista globale supervisore con filtri.
4. Riassegnazione segnalazione (USER/GROUP/ME).
5. Forward verso stub Sinergia con ticket simulato.
6. Enforcement ruoli operatore/supervisore.
7. Verifica VC-1: azioni segnalazione non alterano stato pratica.

Out of scope:
- hardening UX accessoria e polish Sprint 10.
- nuove capability non previste da EPIC E10.

## Orchestrazione agenti

### Stream backend (develop-backend)
Consegne principali:
- Modulo signals backend:
  - [apps/backend/src/main/java/it/poste/anc/signals/api/SignalController.java](../../apps/backend/src/main/java/it/poste/anc/signals/api/SignalController.java)
  - [apps/backend/src/main/java/it/poste/anc/signals/application/SignalService.java](../../apps/backend/src/main/java/it/poste/anc/signals/application/SignalService.java)
  - [apps/backend/src/main/java/it/poste/anc/signals/application/SinergiaStubGateway.java](../../apps/backend/src/main/java/it/poste/anc/signals/application/SinergiaStubGateway.java)
  - [apps/backend/src/main/java/it/poste/anc/signals/api/SignalCreateRequest.java](../../apps/backend/src/main/java/it/poste/anc/signals/api/SignalCreateRequest.java)
  - [apps/backend/src/main/java/it/poste/anc/signals/api/SignalReassignRequest.java](../../apps/backend/src/main/java/it/poste/anc/signals/api/SignalReassignRequest.java)
- Migrazione DB Sprint 9:
  - [infra/db/migrations/V13__sprint9_signals_dashboard.sql](../../infra/db/migrations/V13__sprint9_signals_dashboard.sql)

### Stream frontend (develop-frontend)
Consegne principali:
- Dashboard Segnalazioni e integrazione API:
  - [apps/frontend/src/features/signals/SignalsDashboardPage.jsx](../../apps/frontend/src/features/signals/SignalsDashboardPage.jsx)
  - [apps/frontend/src/core/api/signalsApi.js](../../apps/frontend/src/core/api/signalsApi.js)
  - [apps/frontend/src/app/routes.jsx](../../apps/frontend/src/app/routes.jsx)
  - [apps/frontend/src/app/shell/AppShell.jsx](../../apps/frontend/src/app/shell/AppShell.jsx)
  - [apps/frontend/src/features/practices/PracticeDetailPage.jsx](../../apps/frontend/src/features/practices/PracticeDetailPage.jsx)
- Test frontend Sprint 9:
  - [apps/frontend/src/features/signals/SignalsDashboardPage.test.jsx](../../apps/frontend/src/features/signals/SignalsDashboardPage.test.jsx)
  - [apps/frontend/src/core/api/signalsApi.test.js](../../apps/frontend/src/core/api/signalsApi.test.js)

### Stream QA (develop-qa)
Consegne principali:
- Report Sprint 9 con evidenze runtime coordinator:
  - [docs/out-develop-qa/Smoke_Test_Report_Sprint_9.md](../out-develop-qa/Smoke_Test_Report_Sprint_9.md)
  - [docs/out-develop-qa/Sprint_Test_Checklist_Sprint_9.md](../out-develop-qa/Sprint_Test_Checklist_Sprint_9.md)
  - [docs/out-develop-qa/Defect_List_Sprint_9.md](../out-develop-qa/Defect_List_Sprint_9.md)
  - [docs/out-develop-qa/BPM_Workflow_Validation_Sprint_9.md](../out-develop-qa/BPM_Workflow_Validation_Sprint_9.md)

## Validazione tecnica coordinator
1. Build immagini:
   - `docker compose build backend frontend`
   - esito: PASS (backend/frontend).
2. Deploy stack:
   - `docker compose up -d`
   - esito: PASS (backend/frontend/db/reverse-proxy healthy/running).
3. Test frontend Sprint 9:
   - `npm --prefix apps/frontend run test -- --run src/features/signals/SignalsDashboardPage.test.jsx src/core/api/signalsApi.test.js`
   - esito: PASS (2 file, 7 test).
4. Smoke runtime API Signals:
   - POST `/api/v1/signals` con op.rossi: PASS (200, resultCode 0, state IN_CODA).
   - GET `/api/v1/signals/me` con op.rossi: PASS (200, resultCode 0).
   - GET `/api/v1/signals` con sup.verdi: PASS (200, resultCode 0).
   - GET `/api/v1/signals` con op.rossi: PASS (403, resultCode 7013).
   - POST `/api/v1/signals/{id}/reassign` con sup.verdi: PASS (200, state IN_LAVORAZIONE).
   - POST `/api/v1/signals/{id}/forward-sinergia` con sup.verdi: PASS (200, state CHIUSO, sinergiaTicketId valorizzato).
5. Verifica VC-1 su DB:
   - Practice id=1 stato BEFORE `IN_LAVORAZIONE`.
   - ciclo create/reassign/forward segnalazione: HTTP 200 su tutte le chiamate.
   - Practice id=1 stato AFTER `IN_LAVORAZIONE`.
   - esito: PASS (stato pratica invariato).

## Esito orchestrazione Sprint 9
- Vertical slice segnalazioni eseguibile localmente: SI.
- Copertura smoke QA runtime: PASS 9 / FAIL 0 / BLOCKED 0.
- Rilievi residui: 1 (copertura endpoint `POST /api/v1/signals/{id}/take` non inclusa nel pacchetto smoke).
