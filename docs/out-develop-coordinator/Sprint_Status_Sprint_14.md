# Sprint Status — Sprint 14
**Data**: 2026-05-21  
**Stato**: ✅ GO

## Migration Flyway
| Script | Descrizione | Stato |
|--------|-------------|-------|
| V105 | user_task_filter | ✅ Applicata (rank 20) |

## Verifica API
| Endpoint | Metodo | Esito |
|----------|--------|-------|
| /api/v1/tasks/filters/saved | GET | rc=0, details=[] (lista vuota per nuovo utente) |
| /api/v1/tasks/filters/saved | POST | rc=0, record inserito in user_task_filter |
| /api/v1/tasks/filters/saved | GET (dopo POST) | rc=0, details=[{id,filterName,...}] ✅ |

**Nota**: `ApiResponse` usa campo `details` (non `data`) — confermato dalla risposta JSON raw.

## Deliverable
| ID | Tipo | Stato |
|----|------|-------|
| D8-BE-1÷4 | Backend V105 + UserTaskFilter* | ✅ Build OK, V105 applicata |
| D8-FE-1÷5 | Frontend ActivitiesPage filtri salvati | ✅ Già presenti pre-build |
| D8-FE-3 | PracticesPage colonne conformi | ✅ Già conforme |
| D8-FE-4 | PracticeDetailPage tab Stati | ✅ Già implementato |

## Acceptance Criteria
| AC | Esito |
|----|-------|
| AC-S14-01 | ✅ PASS |
| AC-S14-02 | ✅ PASS (record verificato in DB) |
| AC-S14-03 | ✅ PASS (DELETE con ownership check) |
| AC-S14-04 | ✅ PASS (UI già presente) |
| AC-S14-05 | ✅ PASS |
| AC-S14-06 | ✅ PASS |
