# CrossAgent Issues — Sprint 13
**Data**: 2026-05-21

## Issue Rilevate

### CROSS-13-001 — Schema DBA divergente da GAP-DBA V17÷V19
**Gravità**: MEDIA  
**Fonte**: GAP-DBA.md §V17, §V18, §V19  
**Descrizione**: Il GAP-DBA V17÷V19 fa riferimento alla tabella `checklist_item_catalog` con struttura normalizzata per item. Il POC usa uno schema flat: `checklist_verbale` + `checklist_carta` con campi booleani fissi.  
**Risoluzione adottata**: Migration V103 adattata per aggiungere `ref_causali_checklist` (tabella lookup causali) + colonna FK `codice_causale_id` su entrambe le tabelle flat esistenti. V17÷V19 skippate.  
**Impatto**: Nessun impatto funzionale sugli AC Sprint 13. Schema è coerente con V1÷V14.  
**Azione suggerita al Discovery Coordinator**: Allineare GAP-DBA per future sprint se necessario.

### CROSS-13-002 — IntakeChecklistServiceTest: costruttori aggiornati manualmente
**Gravità**: BASSA  
**Fonte**: IntakeChecklistServiceTest.java  
**Descrizione**: Il record `IntakeChecklistRequest` è passato da 10 a 11 argomenti (aggiunto `codiceCausaleId`). I test esistenti usavano costruttori a 10 argomenti che causavano errori di compilazione.  
**Risoluzione adottata**: Tutti i costruttori nei test aggiornati con `null` come 11° argomento (posizione finale).  
**Impatto**: Nessuno. Test compilano e passano correttamente.
