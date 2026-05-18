# Sprint Execution Plan - Sprint 4

## Sprint attivo
- Sprint: 4
- Tema: Tipizzazione Documento e Viewer
- Riferimento roadmap: docs/out-discovery-business-analist/03_Roadmap_Porting.md

## Scope Sprint 4 (vincolato)
- Viewer integrato allegati con controllo dimensione preview (piccolo/medio/grande).
- Download manuale fallback allegato.
- Box informativo errore tecnico viewer.
- Tipizzazione documento (Verbale/Carta) con conferma.
- Irreversibilita tipizzazione (backend + UI), con audit.
- Nessuna capability Sprint 5+.

## Input documentali usati
- BA: docs/out-discovery-business-analist/03_Roadmap_Porting.md
- BA: docs/out-discovery-business-analist/04_Epic_UserStories.md
- BA: docs/out-discovery-business-analist/05_Acceptance_Criteria.md
- BA: docs/out-discovery-business-analist/06_Dipendenze_Funzionali.md
- Architect: docs/out-discovery-architect/04_Workflow_Architecture.md
- Architect: docs/out-discovery-architect/05_API_Candidate.md
- Architect: docs/out-discovery-architect/06_State_Management.md
- Architect: docs/out-discovery-architect/10_POC_Runtime_Simplification_Matrix.md
- UX: docs/out-discovery-ux-mapper/BPM_Task_UI_Mapping.md

## Orchestrazione agenti eseguita
1. develop-backend
- Implementati endpoint Sprint 4:
  - GET /api/v1/practices/{id}/attachments
  - GET /api/v1/attachments/{id}/preview
  - GET /api/v1/attachments/{id}/download
  - POST /api/v1/practices/{id}/intake/typing
- Implementata irreversibilita tipizzazione con idempotenza su reinvio stesso tipo.
- Introdotta gestione errore tecnico allegato per box informativo UI.
- Introdotto enforcement autorizzativo su tipizzazione (ruolo OPERATORE + ownership task in carico).

2. develop-frontend
- Implementata schermata Tipizzazione Documento con layout a 2 colonne.
- Integrati viewer, resize preview (piccolo/medio/grande) e download fallback.
- Implementata conferma tipizzazione con lock UI post-conferma.
- Allineato filtro stato task ai soli valori backend supportati (IN_CODA, IN_CARICO).

3. develop-qa
- Validazione iniziale: FAIL (difetti QA-S4-001 e QA-S4-002).
- Revalidazione post-fix stream owner: PASS.
- Gate finale Sprint 4: GO.

## Deliverable sprint generati dagli stream
- Backend:
  - docs/out-develop-backend/README_Sprint_4.md
  - docs/out-develop-backend/API_Implementate_Sprint_4.md
  - docs/out-develop-backend/Workflow_Implementati_Sprint_4.md
- Frontend:
  - docs/out-develop-frontend/README_Sprint_4.md
- QA:
  - docs/out-develop-qa/Smoke_Test_Report_Sprint_4.md
  - docs/out-develop-qa/Sprint_Test_Checklist_Sprint_4.md
  - docs/out-develop-qa/Defect_List_Sprint_4.md
  - docs/out-develop-qa/BPM_Workflow_Validation_Sprint_4.md

## Consistenza BPM/UI/API verificata
- BPMN: nessuna alterazione del lifecycle oltre Sprint 4.
- UI task: entry da Lista Attivita con navigazione coerente verso Tipizzazione.
- API: allineamento endpoint viewer/download/typing tra backend e frontend.
- Stato pratica: resta IN_LAVORAZIONE durante tipizzazione (nessuna chiusura Sprint 5/6).

## Decision gate
- Stato gate Sprint 4: GO.
- Motivazione: issue bloccanti chiuse e rivalidazione QA PASS sul perimetro Sprint 4.

## Azioni immediate post-avvio
1. Completato: sviluppo backend/frontend Sprint 4 in scope.
2. Completato: gestione conflitti cross-agent e chiusura difetti bloccanti.
3. Da completare (non bloccante gate): evidenza runtime E2E tracciata in ambiente di integrazione.