# Sprint Execution Plan — Sprint 14
**Data produzione**: 2026-05-21  
**Stato**: ✅ COMPLETATO

## Scope
Lista Attività + Pratiche + Filtri Salvati

## Riferimenti GAP
- GAP-US-05: Filtri salvati lista attività
- GAP-UI §2.3: ListaAttività filtri
- GAP-UI §2.14: ListaPratiche colonne
- GAP-UI §2.12: DettaglioPratica tab Stati

## Migration Flyway
| Script | Descrizione |
|--------|-------------|
| V105 | user_task_filter (BIGINT id, username, filter_name, filter_json, created_at) |

## Deliverable Backend
| ID | Classe | Endpoint |
|----|--------|---------|
| D8-BE-1 | UserTaskFilterDto.java | record DTO |
| D8-BE-2 | UserTaskFilterRequest.java | record Request |
| D8-BE-3 | UserTaskFilterService.java | listFilters, saveFilter (FIFO max 5), deleteFilter |
| D8-BE-4 | UserTaskFilterController.java | GET/POST/DELETE /api/v1/tasks/filters/saved |

## Deliverable Frontend
| ID | File | Descrizione |
|----|------|-------------|
| D8-FE-1 | ActivitiesPage.jsx | Sezione filtri salvati (GET/DELETE), ROW_HIGHLIGHT su selezione |
| D8-FE-2 | ActivitiesPage.jsx | Bottoni "APPLICA E SALVA FILTRI" + "AZZERA FILTRI" |
| D8-FE-3 | PracticesPage.jsx | Griglia pratiche: no colonne Id/Canale/Scadenza, badge ⚠ segnalazioni |
| D8-FE-4 | PracticeDetailPage.jsx | Tab "Stati" con GET /practices/{id}/states |
| D8-FE-5 | tasksApi.js | getSavedFilters, saveFilter, deleteFilter |

## Acceptance Criteria
| AC | Condizione |
|----|------------|
| AC-S14-01 | GET /tasks/filters/saved restituisce lista (max 5) per l'utente autenticato |
| AC-S14-02 | POST /tasks/filters/saved persiste filtro con FIFO se >5 |
| AC-S14-03 | DELETE /tasks/filters/saved/{id} elimina solo il filtro dell'utente |
| AC-S14-04 | ActivitiesPage mostra sezione filtri salvati |
| AC-S14-05 | Click "Applica" su filtro salvato popola i campi filtro |
| AC-S14-06 | Tab "Stati" nel dettaglio pratica mostra cronologia stati |
