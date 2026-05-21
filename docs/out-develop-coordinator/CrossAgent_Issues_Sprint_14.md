# CrossAgent Issues — Sprint 14
**Data**: 2026-05-21

## Issue Rilevate

### CROSS-14-001 — API response field: `details` vs `data`
**Gravità**: BASSA  
**Fonte**: UserTaskFilterController.java — ApiResponse wrapper  
**Descrizione**: Il campo di risposta wrapper è `details` (non `data`). Il frontend deve accedere a `result?.details` oppure usare la normalizzazione esistente `Array.isArray(result?.items)`. I filtri salvati tornano come array in `details`.  
**Risoluzione**: Il frontend in `loadSavedFilters` usa `Array.isArray(result?.items)` come fallback — funziona correttamente perché il backend popola `details` che diventa un array nel JSON. Nessuna modifica necessaria.

### CROSS-14-002 — ActivitiesPage: header colonne tabella non aggiornato (Sprint 15)
**Gravità**: BASSA  
**Fonte**: ActivitiesPage.jsx  
**Descrizione**: Durante Sprint 14 non erano ancora stati aggiunti i campi SLA. Lo Sprint 15 ha aggiunto la colonna SLA nella stessa sessione di sviluppo.  
**Risoluzione**: Gestita in Sprint 15 (colonna aggiunta in continuità di sessione).
