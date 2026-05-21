# Sprint Status — Sprint 15
**Data**: 2026-05-21  
**Stato**: ✅ GO

## Scope
Tecnici Trasversali: SLA + link_download + badge frontend

## Migration Flyway
| Script | Descrizione | Stato |
|--------|-------------|-------|
| V106 | ALTER TABLE attachment MODIFY link_download VARCHAR(2500) | ✅ Applicata |
| V107 | ALTER TABLE task ADD COLUMN sla_due_date DATETIME(3) NULL (idempotente) | ✅ Applicata |

## Deliverable Backend
| ID | Stato |
|----|-------|
| TaskDetailResponse.java: slaDueDate + slaStatus | ✅ |
| TaskManagementService.java: SELECT sla_due_date, computeSlaStatus() | ✅ |

## Deliverable Frontend
| ID | Stato |
|----|-------|
| ActivitiesPage.jsx: colonna SLA con badge | ✅ |
| TaskLavorazionePage.jsx: badge SLA nell'header | ✅ |
| ClassificazioneStep.jsx: placeholder documento FAILED | ✅ |
| styles.css: .badge-sla*, .attachment-unavailable | ✅ |

## Acceptance Criteria
| AC | Condizione | Esito |
|----|------------|-------|
| AC-S15-01 | GET /tasks/{id} include slaDueDate e slaStatus | ✅ PASS (campi nel record) |
| AC-S15-02 | computeSlaStatus: SCADUTO se now>sla_due_date, IN_TEMPO altrimenti, null se null | ✅ PASS |
| AC-S15-03 | Colonna SLA nella griglia attività | ✅ PASS |
| AC-S15-04 | Badge SLA nell'header lavorazione | ✅ PASS |
| AC-S15-05 | Placeholder attachment FAILED | ✅ PASS |
| AC-S15-06 | link_download VARCHAR(2500) | ✅ PASS |

## Note
- sla_due_date valorizzato da null su task esistenti (non retroattivo — nessun task in DB nel POC)
- La logica di setting sla_due_date (es. +5 giorni lavorativi da created_at) è rinviata a implementazione Flowable listener fuori scope POC
