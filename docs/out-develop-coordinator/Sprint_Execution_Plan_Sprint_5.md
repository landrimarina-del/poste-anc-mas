# Sprint Execution Plan — Sprint 5

Data: 2026-05-15  
Owner orchestrazione: develop-coordinator

## Obiettivo Sprint
Implementare la vertical slice **Checklist Verbale + Calcolo Esito** nel rispetto di Discovery MAS:
- C4.4, C4.6, C4.7, C4.8, C4.9, C5.1, C5.2
- US-E6.01..US-E6.09

Riferimenti:
- [docs/out-discovery-business-analist/03_Roadmap_Porting.md](../out-discovery-business-analist/03_Roadmap_Porting.md)
- [docs/out-discovery-business-analist/04_Epic_UserStories.md](../out-discovery-business-analist/04_Epic_UserStories.md)
- [docs/out-discovery-business-analist/06_Dipendenze_Funzionali.md](../out-discovery-business-analist/06_Dipendenze_Funzionali.md)
- [docs/out-discovery-architect/05_API_Candidate.md](../out-discovery-architect/05_API_Candidate.md)
- [docs/out-discovery-ux-mapper/BPM_Task_UI_Mapping.md](../out-discovery-ux-mapper/BPM_Task_UI_Mapping.md)

## Scope implementativo (solo Sprint 5)
- Checklist Verbale condizionale a tipizzazione = VERBALE.
- Regole KO cascata da Documento Presente = NO.
- Causali KO formali obbligatorie se idoneità formale = NO.
- Salvataggio bozza + riapertura modifica.
- Calcolo automatico esito (APPROVATA/RESPINTA) read-only.
- Note interne solo in caso di RESPINTA.

Out of scope:
- CHIUDI PRATICA.
- Invio esito BPM e callback ack BPM (Sprint 6).

## Orchestrazione eseguita

### Stream 1 — Backend (develop-backend)
Consegne:
- endpoint BC3 checklist:
  - GET /api/v1/practices/{id}/intake/checklist
  - PUT /api/v1/practices/{id}/intake/checklist
  - POST /api/v1/practices/{id}/intake/checklist/edit
- servizio checklist + regole outcome:
  - [apps/backend/src/main/java/it/poste/anc/document/application/IntakeChecklistService.java](../../apps/backend/src/main/java/it/poste/anc/document/application/IntakeChecklistService.java)
- controller/DTO:
  - [apps/backend/src/main/java/it/poste/anc/document/api/IntakeChecklistController.java](../../apps/backend/src/main/java/it/poste/anc/document/api/IntakeChecklistController.java)
  - [apps/backend/src/main/java/it/poste/anc/document/api/IntakeChecklistRequest.java](../../apps/backend/src/main/java/it/poste/anc/document/api/IntakeChecklistRequest.java)
  - [apps/backend/src/main/java/it/poste/anc/document/api/IntakeChecklistResponse.java](../../apps/backend/src/main/java/it/poste/anc/document/api/IntakeChecklistResponse.java)
  - [apps/backend/src/main/java/it/poste/anc/document/api/IntakeChecklistEditResponse.java](../../apps/backend/src/main/java/it/poste/anc/document/api/IntakeChecklistEditResponse.java)
- test regole chiave:
  - [apps/backend/src/test/java/it/poste/anc/document/application/IntakeChecklistServiceTest.java](../../apps/backend/src/test/java/it/poste/anc/document/application/IntakeChecklistServiceTest.java)

Migrazione:
- V10 checklist/outcome:
  - [infra/db/migrations/V10__intake_checklist_verbale.sql](../../infra/db/migrations/V10__intake_checklist_verbale.sql)

### Stream 2 — Frontend (develop-frontend)
Consegne:
- integrazione checklist verbale + salvataggio/modifica + card esito:
  - [apps/frontend/src/features/intake/TypingPage.jsx](../../apps/frontend/src/features/intake/TypingPage.jsx)
- client API intake:
  - [apps/frontend/src/core/api/intakeApi.js](../../apps/frontend/src/core/api/intakeApi.js)
- styling support:
  - [apps/frontend/src/styles.css](../../apps/frontend/src/styles.css)

### Stream 3 — QA (develop-qa)
Consegne:
- suite AC Sprint5 + runbook:
  - [tools/qa/sprint5/ac-min-cases-sprint5.md](../../tools/qa/sprint5/ac-min-cases-sprint5.md)
  - [tools/qa/sprint5/run-sprint5-qa.ps1](../../tools/qa/sprint5/run-sprint5-qa.ps1)
  - [tools/qa/sprint5/run-sprint5-qa.sh](../../tools/qa/sprint5/run-sprint5-qa.sh)
- report QA:
  - [docs/out-develop-qa/Smoke_Test_Report_Sprint_5.md](../out-develop-qa/Smoke_Test_Report_Sprint_5.md)
  - [docs/out-develop-qa/Sprint_Test_Checklist_Sprint_5.md](../out-develop-qa/Sprint_Test_Checklist_Sprint_5.md)
  - [docs/out-develop-qa/Defect_List_Sprint_5.md](../out-develop-qa/Defect_List_Sprint_5.md)
  - [docs/out-develop-qa/BPM_Workflow_Validation_Sprint_5.md](../out-develop-qa/BPM_Workflow_Validation_Sprint_5.md)

## Validazione tecnica coordinatore
- Build backend: PASS.
- Backend redeploy: PASS.
- Flyway V10 applicata: PASS.
- Tabelle create: checklist_verbale, practice_outcome.

Evidenze schema:
- [infra/db/migrations/V10__intake_checklist_verbale.sql](../../infra/db/migrations/V10__intake_checklist_verbale.sql)

## Dipendenze e blocchi
- Dipendenza funzionale rispettata: Sprint5 dopo tipizzazione Sprint4.
- Copertura QA runtime completa ancora dipendente da esecuzione manuale runbook (BLOCKED-EXEC da agente QA).
- Verifica puntuale conformità completa al file [docs/requirements/source-of-truth/checklistSD.xlsx](../requirements/source-of-truth/checklistSD.xlsx) pianificata in refinement Sprint5.
