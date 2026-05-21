# CrossAgent Issues — Sprint 15

## ISSUE-S15-001 — CRITICA: Stack mancante Flowable reale

**Rilevata da**: Coordinator (test manuali Sprint 15)  
**Data**: 21/05/2026  
**Severity**: CRITICA — blocca completamento AC TECNICO-GAP-B  
**Stato**: APERTA — escalation a Discovery Architect

### Descrizione

Il GAP_Architettura.md (TECNICO-GAP-B, §SlaTaskListener) prevede che `sla_due_date` 
venga popolata da un `SlaTaskListener` Flowable al momento della creazione del task.

**Problema**: il container `anc-bpm-stub` è **nginx:1.27-alpine** — un mock che 
restituisce risposte statiche. Non è presente alcun motore Flowable reale nello stack.

```
docker ps output:
anc-bpm-stub   nginx:1.27-alpine   Up 6 hours (healthy)   80/tcp
```

Non esiste `anc-flowable` o equivalente. Il `SlaTaskListener` non può essere 
implementato senza un motore Flowable reale.

### Impatto

- `task.sla_due_date` rimane sempre NULL per tutti i nuovi task
- I badge SLA (SCADUTO/IN_TEMPO) non vengono mai mostrati in produzione
- TECNICO-GAP-B è parzialmente implementato (colonna DB + badge FE + logica BE 
  presente, ma nessun trigger di popolamento automatico)

### Decisione richiesta a Discovery Architect

Scegliere tra:

**Opzione A** — Introduce Flowable reale nello stack:
- Aggiunge container Flowable (es. `flowable/flowable-rest:7.x`)
- Deploy `ANC-Process.bpmn20.xml`
- Implementa `SlaTaskListener` come `JavaDelegate`
- Backend integra Flowable REST API invece di `anc-bpm-stub`
- **Impatto**: riscrittura infrastruttura + backend significativa

**Opzione B** — Calcolo SLA in backend senza Flowable:
- Al momento di `acceptTask`, il backend calcola `accepted_at + 5 giorni lavorativi`
  e salva direttamente su `task.sla_due_date`
- Nessun listener Flowable necessario
- **Impatto**: minimo, solo `TaskManagementService.acceptTask()`
- **Nota**: allineato con la realtà della POC (no Flowable reale)

### Azione temporanea

Per validazione visiva badge SLA durante i test:
```sql
UPDATE task SET sla_due_date = '2026-05-15 12:00:00' WHERE id = 14; -- SCADUTO
UPDATE task SET sla_due_date = '2026-06-30 12:00:00' WHERE id = 14; -- IN_TEMPO
```

### Prossimo passo

**BLOCCO** su Sprint 15 AC-TECNICO-GAP-B fino a decisione architetto.
