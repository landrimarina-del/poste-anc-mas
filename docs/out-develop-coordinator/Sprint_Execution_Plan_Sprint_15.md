# Sprint Execution Plan — Sprint 15
**Data produzione**: 2026-05-21  
**Stato**: ✅ COMPLETATO

## Scope
Tecnici Trasversali: SLA + link_download espansione + SLA badge frontend

## Riferimenti GAP
- TECNICO-GAP-A: `attachment.link_download` VARCHAR 1024 → 2500
- TECNICO-GAP-B: `task.sla_due_date` DATETIME(3) NULL
- GAP-UI §1.5: SLA badge IN_TEMPO / SCADUTO

## Migration Flyway
| Script | Descrizione |
|--------|-------------|
| V106 | ALTER TABLE attachment MODIFY COLUMN link_download VARCHAR(2500) |
| V107 | ALTER TABLE task ADD COLUMN sla_due_date DATETIME(3) NULL (idempotente) |

## Deliverable Backend
| ID | Classe | Descrizione |
|----|--------|-------------|
| D9-BE-1 | TaskDetailResponse.java | Aggiunto slaDueDate (Instant) e slaStatus (String) |
| D9-BE-2 | TaskManagementService.java | SELECT t.sla_due_date; computeSlaStatus() helper |

## Deliverable Frontend
| ID | File | Descrizione |
|----|------|-------------|
| D9-FE-1 | ActivitiesPage.jsx | Colonna SLA con badge IN TEMPO / SCADUTO / — |
| D9-FE-2 | TaskLavorazionePage.jsx | Badge SLA sotto titolo header (solo se slaStatus presente) |
| D9-FE-3 | ClassificazioneStep.jsx | Placeholder "Documento non disponibile" se ingestionStatus=FAILED |
| D9-FE-4 | styles.css | Classi .badge-sla, .badge-sla-in-tempo, .badge-sla-scaduto, .attachment-unavailable |

## Acceptance Criteria
| AC | Condizione |
|----|------------|
| AC-S15-01 | GET /tasks/{id} include campi slaDueDate e slaStatus |
| AC-S15-02 | slaStatus=SCADUTO se now > sla_due_date; IN_TEMPO se now ≤ sla_due_date; null se sla_due_date null |
| AC-S15-03 | Colonna SLA visibile nella griglia lista attività |
| AC-S15-04 | Badge SLA visibile nell'header Task Lavorazione |
| AC-S15-05 | Attachment con ingestionStatus=FAILED mostra placeholder invece di iframe |
| AC-S15-06 | attachment.link_download accetta URL fino a 2500 chars |
