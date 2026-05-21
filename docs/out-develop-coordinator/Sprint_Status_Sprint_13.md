# Sprint Status — Sprint 13
**Data**: 2026-05-21  
**Stato**: ✅ GO

## Scope
Checklist Avanzata: Causali KO + Note di Lavorazione

## Migration Flyway
| Script | Descrizione | Stato |
|--------|-------------|-------|
| V103 | ref_causali_checklist + ALTER checklist_verbale + checklist_carta | ✅ Applicata |
| V104 | case_note (tipo IN('LAVORAZIONE','CAMBIO_STATO','CHIUSURA')) | ✅ Applicata |

## Deliverable
| ID | Tipo | Descrizione | Stato |
|----|------|-------------|-------|
| D6-BE-1 | Backend | IntakeChecklistRequest: codiceCausaleId (11° campo) | ✅ |
| D6-BE-2 | Backend | IntakeChecklistService: causali in checklist_verbale + checklist_carta | ✅ |
| D6-BE-3 | Backend | GET /causali?categoria= endpoint | ✅ |
| D6-BE-4 | Backend | CaseNoteController: GET/POST /practices/{id}/notes | ✅ |
| D6-BE-5 | Backend | CaseNoteService: listByPractice + createNote | ✅ |
| D6-FE-1 | Frontend | VerificaDocumentiStep: dropdown causali CARTA (KO area) | ✅ |
| D6-FE-2 | Frontend | VerificaDocumentiStep: dropdown causali VERBALE (per-item KO) | ✅ |
| D6-FE-3 | Frontend | TaskLavorazionePage RIEPILOGO: sezione Note di Lavorazione | ✅ |
| D6-FE-4 | Frontend | intakeApi.js: getCausali(practiceId, categoria) | ✅ |

## Acceptance Criteria
| AC | Condizione | Esito |
|----|------------|-------|
| AC-S13-01 | GET /causali?categoria=CARTA ritorna lista causali | ✅ PASS |
| AC-S13-02 | Dropdown causali visibile in VerificaDocumentiStep quando esito KO | ✅ PASS |
| AC-S13-03 | POST /practices/{id}/notes persiste nota con tipo LAVORAZIONE | ✅ PASS |
| AC-S13-04 | GET /practices/{id}/notes ritorna lista note ordinate per data DESC | ✅ PASS |
| AC-S13-05 | Sezione Note di Lavorazione visibile nello step RIEPILOGO | ✅ PASS |

## Issue Cross-Agent
Vedi CrossAgent_Issues_Sprint_13.md

## Note
- GAP-DBA V17 skippa: riferiva `checklist_item_catalog` non esistente nel POC (schema flat)
- V103 sostituisce V17+V18+V19 con approccio adattato allo schema POC
