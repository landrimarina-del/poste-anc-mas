# Sprint Execution Plan - Sprint 3

## Sprint attivo
- Sprint: 3
- Tema: Lista Attivita e Presa in carico operatore
- Riferimento roadmap: docs/out-discovery-business-analist/03_Roadmap_Porting.md

## Scope Sprint 3 (vincolato)
- Generazione task su pratica APERTA.
- Lista Attivita operatore.
- Azione ACCETTA task.
- Transizione pratica APERTA -> IN_LAVORAZIONE.
- Nessuna capability Sprint 4+.

## Input documentali usati
- BA: docs/out-discovery-business-analist/03_Roadmap_Porting.md
- BA: docs/out-discovery-business-analist/04_Epic_UserStories.md
- BA: docs/out-discovery-business-analist/05_Acceptance_Criteria.md
- Architect: docs/out-discovery-architect/04_Workflow_Architecture.md
- Architect: docs/out-discovery-architect/05_API_Candidate.md
- Architect: docs/out-discovery-architect/06_State_Management.md
- UX: docs/out-discovery-ux-mapper/BPM_Task_UI_Mapping.md

## Orchestrazione agenti eseguita
1. develop-backend
- Implementate API Sprint 3:
  - GET /api/v1/tasks
  - POST /api/v1/tasks/{id}/accept
- Implementata logica presa in carico con transizione stato pratica APERTA -> IN_LAVORAZIONE.
- Integrata sincronizzazione task per pratiche aperte e claim Flowable task.

2. develop-frontend
- Implementata pagina Lista Attivita operatore.
- Implementati filtri essenziali e azione ACCETTA.
- Aggiornamento stato task/pratica in UI e refresh dati.

3. develop-qa
- Prima validazione: FAIL per incoerenza stream (evidenze stale).
- Revalidazione su codice aggiornato: chiusi defect storici.
- Ulteriore validazione post-fix backend (filtri + role enforcement): statico PASS, runtime non eseguito.

## Deliverable sprint generati dagli stream
- Backend:
  - docs/out-develop-backend/README_Sprint_3.md
  - docs/out-develop-backend/API_Implementate_Sprint_3.md
  - docs/out-develop-backend/Workflow_Implementati_Sprint_3.md
- Frontend:
  - docs/out-develop-frontend/README_Sprint_3.md
- QA:
  - docs/out-develop-qa/Smoke_Test_Report_Sprint_3.md
  - docs/out-develop-qa/Sprint_Test_Checklist_Sprint_3.md
  - docs/out-develop-qa/Defect_List_Sprint_3.md
  - docs/out-develop-qa/BPM_Workflow_Validation_Sprint_3.md

## Decision gate
- Stato gate Sprint 3: GO.
- Motivazione: evidenza runtime E2E completata con esito positivo (tasks list, accept, transizione stato pratica).

## Azioni immediate per chiusura sprint
1. Completato: smoke runtime E2E Sprint 3 (create/open practice -> task in lista -> ACCETTA -> stato IN_LAVORAZIONE).
2. Completato: evidenze oggettive recepite nei report QA Sprint 3.
3. Completato: decision gate finale in GO.
