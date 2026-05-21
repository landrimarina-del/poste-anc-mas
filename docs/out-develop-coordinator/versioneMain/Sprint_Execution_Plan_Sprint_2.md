# Sprint Execution Plan - Sprint 2

## Sprint attivo
- Sprint: 2
- Tema: Repository Pratiche (Lista + Dettaglio read-only)
- Riferimento roadmap: docs/out-discovery-business-analist/03_Roadmap_Porting.md

## Scope Sprint 2 (vincolato)
- Lista pratiche read-only con filtri.
- Ordinamento asc/desc.
- Paginazione.
- Dettaglio pratica read-only.
- Tab Cronologia pratica.
- Tab Stati pratica.

## Input documentali usati
- BA: docs/out-discovery-business-analist/03_Roadmap_Porting.md
- BA: docs/out-discovery-business-analist/04_Epic_UserStories.md
- BA: docs/out-discovery-business-analist/06_Dipendenze_Funzionali.md
- Architect: docs/out-discovery-architect/05_API_Candidate.md
- Architect: docs/out-discovery-architect/06_State_Management.md
- UX: docs/out-discovery-ux-mapper/BPM_Task_UI_Mapping.md

## Orchestrazione agenti eseguita
1. develop-backend
- Implementazione API read-only Sprint 2:
  - GET /api/v1/practices
  - GET /api/v1/practices/{id}
  - GET /api/v1/practices/{id}/history
  - GET /api/v1/practices/{id}/states
- Fallback read-only su colonne opzionali.
- Test backend aggiornati.

2. develop-frontend
- Implementazione tab Pratiche con filtri/ordinamento/paginazione.
- Implementazione dettaglio pratica read-only (Riepilogo/Cronologia/Stati).
- Integrazione endpoint Sprint 2 backend.

3. develop-qa
- Smoke Sprint 2 eseguito.
- Esito complessivo: FAIL (NO-GO).

4. Loop correttivo post-QA (coordinato)
- develop-frontend: rimossa esposizione route/tab `/riassegna-attivita` dal perimetro Sprint 2.
- develop-qa: re-test mirato; defect scope DQ-S2-001 dichiarato CHIUSO.

5. Chiusura finale QA
- develop-qa: recepita evidenza runtime manuale operatore su regressione Sprint1->Sprint2 (lista + dettaglio visibili).
- Stato defect: DQ-S2-001 CHIUSO, DQ-S2-002 CHIUSO, DQ-S2-003 CHIUSO.
- Raccomandazione QA finale Sprint 2: GO.

## Deliverable sprint generati dagli stream
- Backend:
  - docs/out-develop-backend/README_Sprint_2.md
  - docs/out-develop-backend/API_Implementate_Sprint_2.md
  - docs/out-develop-backend/Workflow_Implementati_Sprint_2.md
- Frontend:
  - docs/out-develop-frontend/README_Sprint_2.md
- QA:
  - docs/out-develop-qa/Smoke_Test_Report_Sprint_2.md
  - docs/out-develop-qa/Sprint_Test_Checklist_Sprint_2.md
  - docs/out-develop-qa/Defect_List_Sprint_2.md
  - docs/out-develop-qa/BPM_Workflow_Validation_Sprint_2.md

## Decision gate
- Stato gate Sprint 2: GO (issue bloccanti chiuse).

## Azioni correttive immediate
1. Completato: rimozione/isolation feature fuori scope Sprint 3+ dal perimetro Sprint 2 (DQ-S2-001 chiuso).
2. Completato: evidenza runtime regressione Sprint 1 -> Sprint 2 recepita in QA (DQ-S2-002 chiuso).
3. Completato: riallineamento riferimenti documentali BA (DQ-S2-003 chiuso).
