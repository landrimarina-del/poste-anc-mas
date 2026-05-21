# Sprint Execution Plan — Sprint 11

**Sprint:** 11  
**Titolo:** BPM Retry Sincrono + Ticketing Mock  
**Data:** 2026-05-21  
**Gate precedente:** Sprint 10 — GO (V1÷V14 applicati, 2026-05-16)  
**Owner orchestrazione:** develop-coordinator

---

## 1. Scope

Implementare la chiusura dei GAP critici sul workflow BPM:

- **GAP-US-02**: sostituire il pattern callback inbound (`evt.waitOutcomeAck`) con chiamata **sincrona a retry** (`BpmOutboundService` con `RetryTemplate`);
- **GAP-US-07**: esporre sul `bpm-stub` l'endpoint `POST /receive-outcome` con modalità OK/KO configurabile a runtime via `PUT /admin/mode`;
- **GAP-US-01**: aggiungere il mock ticketing (`POST /ticketing/open-ticket` su bpm-stub) e persistere il `ticket_id` sul record pratica.

Lo scope **non** include modifiche a UI, checklist, filtri o altri GAP (Sprint 12+).  
Nessuna modifica agli endpoint già funzionanti da Sprint 1÷10.

---

## 2. Stream

| # | Stream | Agente | Output atteso |
|---|---|---|---|
| 1 | DB: V101 bpm_outbound retry columns | develop-dba | `infra/db/migrations/V101__gap_us02_bpm_outbound_retry.sql` |
| 2 | DB: V102 practice.ticket_id | develop-dba | `infra/db/migrations/V102__gap_us01_practice_ticket.sql` |
| 3 | Backend: BpmOutboundService | develop-backend | `apps/backend/src/main/java/it/poste/anc/bpmgw/outbound/BpmOutboundService.java` |
| 4 | Backend: TicketingClient | develop-backend | `apps/backend/src/main/java/it/poste/anc/ticketing/TicketingClient.java` |
| 5 | Backend: modifica openPractice | develop-backend | `BpmPracticeInboundService.java` (aggiunta chiamata TicketingClient) |
| 6 | Backend: BPMN anc.main rimozione waitOutcomeAck | develop-backend | `apps/backend/src/main/resources/processes/anc-main.bpmn20.xml` |
| 7 | Backend: application-poc.yml | develop-backend | `apps/backend/src/main/resources/application-poc.yml` |
| 8 | bpm-stub: nuovi endpoint | develop-backend | `infra/bpm-stub/` |
| 9 | QA: script AC + smoke test | develop-qa | `tools/qa/sprint11/` |

---

## 3. Decisioni non rinegoziabili

| Tema | Decisione |
|---|---|
| Pattern BPM | Chiamata sincrona SD→BPM. Nessun callback inbound. `evt.waitOutcomeAck` rimosso. |
| Retry | Spring `RetryTemplate`: `maxAttempts=${bpm.max-retry}`, `backoff=${bpm.retry-interval-ms}` |
| stato_invio | TINYINT 0/1/2/3 — coesiste con colonne `send_status`/`ack_status` preesistenti (non rimuovere) |
| Ticketing failure | Failure ticketing → NON blocca creazione pratica (best-effort, log WARN, ticket_id=null) |
| bpm-stub modalità | `BPM_STUB_ESITO_MODE=OK` default. Cambio via `PUT /admin/mode` senza restart container |

---

## 4. Specifiche per stream

### Stream 1 — V101

**File**: `infra/db/migrations/V101__gap_us02_bpm_outbound_retry.sql`  
**Spec completa**: `docs/out-discovery-dba/GAP-DBA.md §V101`

```sql
-- V101 — GAP-US-02 — pattern idempotente (IF NOT EXISTS)
ALTER TABLE bpm_outbound_message
    ADD COLUMN IF NOT EXISTS retry_count      INT           NOT NULL DEFAULT 0
        COMMENT '0=mai tentato; N=numero tentativi effettuati',
    ADD COLUMN IF NOT EXISTS max_retry        INT           NOT NULL DEFAULT 3,
    ADD COLUMN IF NOT EXISTS stato_invio      TINYINT       NOT NULL DEFAULT 0
        COMMENT '0=attesa 1=inviato_ok 2=errore_transiente 3=scartato',
    ADD COLUMN IF NOT EXISTS response_json    TEXT          NULL,
    ADD COLUMN IF NOT EXISTS error_message    VARCHAR(1000) NULL,
    ADD COLUMN IF NOT EXISTS last_attempt_at  DATETIME(3)   NULL;

SELECT COUNT(*) INTO @cnt FROM information_schema.statistics
WHERE table_schema=DATABASE() AND table_name='bpm_outbound_message'
  AND index_name='idx_bom_stato_invio';
SET @s=IF(@cnt=0,'CREATE INDEX idx_bom_stato_invio ON bpm_outbound_message(stato_invio)','DO 0');
PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
```

### Stream 2 — V102

**File**: `infra/db/migrations/V102__gap_us01_practice_ticket.sql`  
**Spec completa**: `docs/out-discovery-dba/GAP-DBA.md §V102`

```sql
-- V102 — GAP-US-01 — pattern idempotente
ALTER TABLE practice
    ADD COLUMN IF NOT EXISTS ticket_id VARCHAR(100) NULL
        COMMENT 'ID ticket sistema di ticketing esterno (mock in POC)';

SELECT COUNT(*) INTO @cnt FROM information_schema.statistics
WHERE table_schema=DATABASE() AND table_name='practice' AND index_name='idx_practice_ticket';
SET @s=IF(@cnt=0,'CREATE INDEX idx_practice_ticket ON practice(ticket_id)','DO 0');
PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
```

### Stream 3 — BpmOutboundService

**Package**: `it.poste.anc.bpmgw.outbound`  
**Spec completa**: `docs/out-discovery-architect/GAP_Architettura.md §GAP-US-02`

Comportamento richiesto:
1. Prima di chiamare BPM → persiste riga `bpm_outbound_message` con `stato_invio=0`, `retry_count=0`.
2. `RetryTemplate` max `${bpm.max-retry}` (default 3), backoff `${bpm.retry-interval-ms}` (default 2000ms).
3. Ogni tentativo: `retry_count++`, `last_attempt_at = now`.
4. Risposta valida: `stato_invio=1`, `response_json=<raw>` → chiama `svc.finalizeOnAck(esito)`.
5. Tentativo fallito retryable: `stato_invio=2`.
6. Retry esauriti: `stato_invio=3` → emette `BpmSendFailed` event → pratica rimane `IN_ATTESA_CONFERMA_BPM`.

### Stream 4÷5 — TicketingClient + openPractice

**Package**: `it.poste.anc.ticketing`  
**Spec completa**: `docs/out-discovery-architect/GAP_Architettura.md §GAP-US-01`

Modifica in `openPractice` (dopo `practice.persist()`, prima di `PracticeOpened` event):
```
if (ticketing.enabled):
  try:
    resp = POST ${ticketing.base-url}${ticketing.open-ticket-path}
           body: { idWorkItem, canale }
    practice.ticket_id = resp.ticketId
  catch Exception:
    log.warn("Ticketing mock non disponibile — ticket_id null per pratica {}", practice.id)
    // NON rilanciare — pratica creata comunque
```

### Stream 6 — BPMN anc.main

**File**: `apps/backend/src/main/resources/processes/anc-main.bpmn20.xml`

Rimuovere il nodo:
```xml
<intermediateCatchEvent id="evt.waitOutcomeAck">
  <messageEventDefinition messageRef="msg.outcomeAck"/>
</intermediateCatchEvent>
```
e il relativo sequence flow in ingresso/uscita.  
Collegare `svc.sendOutcomeToBpm` direttamente a `svc.finalizeOnAck` tramite gateway esclusivo su `esito=true/false`.

### Stream 7 — application-poc.yml

Aggiungere (o aggiornare se già parzialmente presente):

```yaml
bpm:
  base-url: ${BPM_STUB_BASE_URL:http://bpm-stub:8090}
  receive-outcome-path: /receive-outcome
  timeout-ms: ${BPM_TIMEOUT_MS:50000}
  max-retry: ${BPM_MAX_RETRY:3}
  retry-interval-ms: ${BPM_RETRY_INTERVAL_MS:2000}

ticketing:
  base-url: ${TICKETING_BASE_URL:http://bpm-stub:8090}
  open-ticket-path: /ticketing/open-ticket
  enabled: ${TICKETING_ENABLED:true}
```

### Stream 8 — bpm-stub

**Directory**: `infra/bpm-stub/`

Nuovi endpoint da aggiungere al server Express/Node.js (o framework usato):

```
POST /receive-outcome
  Input:  { resultCode, resultMessage, idWorkItem, esito, koCodes[] }
  Output: { esito: boolean, descrizioneEsito: string }
  Logic:  esito = (BPM_STUB_ESITO_MODE === 'OK')
          descrizioneEsito = esito ? "ACK OK" : process.env.BPM_STUB_KO_DESCRIZIONE

POST /ticketing/open-ticket
  Input:  { idWorkItem, canale }
  Output: { ticketId: "MOCK-TICKET-" + uuid() }

GET  /admin/mode
  Output: { mode: currentMode }

PUT  /admin/mode
  Input:  { mode: "OK" | "KO" }
  Effect: currentMode = body.mode   // in-memory, no restart
  Output: { mode: body.mode }
```

**docker-compose.yml** — aggiungere variabili al servizio `bpm-stub`:
```yaml
environment:
  BPM_STUB_ESITO_MODE: OK
  BPM_STUB_KO_DESCRIZIONE: "Esito rifiutato da BPM (mock)"
```

---

## 5. Sequenza esecuzione

```
DBA (V101 → V102)
        │
        ▼
Backend (BpmOutboundService + TicketingClient + openPractice + BPMN + yml)
        │
        ├──▶ bpm-stub (nuovi endpoint + docker-compose env)
        │
        └──▶ QA (script AC Sprint 11)
```

---

## 6. Acceptance Criteria

| ID | AC | Come verificare |
|---|---|---|
| AC-S11-DB-1 | V101 applicata: `SHOW COLUMNS FROM bpm_outbound_message` mostra `stato_invio`, `retry_count`, `max_retry`, `response_json`, `error_message`, `last_attempt_at` | `SHOW COLUMNS` |
| AC-S11-DB-2 | V102 applicata: `SHOW COLUMNS FROM practice` mostra `ticket_id VARCHAR(100) NULL` | `SHOW COLUMNS` |
| AC-S11-TICK-1 | POST `/api/v1/bpm/practices` happy path → `SELECT ticket_id FROM practice WHERE id=1` → valore non NULL con prefisso `MOCK-TICKET-` | query DB |
| AC-S11-TICK-2 | bpm-stub `/ticketing/open-ticket` unavailable → pratica creata con `ticket_id=NULL`, nessuna eccezione, log WARN | log applicativo |
| AC-S11-BPM-OK | `BPM_STUB_ESITO_MODE=OK` + POST pratica happy path → `practice.stato=CHIUSA_OK`, `bpm_outbound_message.stato_invio=1` | query DB |
| AC-S11-BPM-KO | `BPM_STUB_ESITO_MODE=KO` + POST pratica happy path → `practice.stato=CHIUSA_KO`, `stato_invio=1` | query DB |
| AC-S11-RETRY | bpm-stub `/receive-outcome` down → dopo 3 tentativi → `stato_invio=3`, pratica `IN_ATTESA_CONFERMA_BPM`, log ERROR | query DB + log |
| AC-S11-ADMIN | `PUT /admin/mode {"mode":"KO"}` → `GET /admin/mode` risponde `{"mode":"KO"}` | curl bpm-stub |
| AC-S11-ADMIN-2 | `PUT /admin/mode {"mode":"KO"}` + successivo POST pratica → `CHIUSA_KO` | query DB |
| AC-S11-IDEM | Re-invio stesso ID_WORKITEM → `resultCode=-5` (invariato da Sprint 4) | curl API |
| AC-S11-REG | Flyway `SELECT * FROM flyway_schema_history WHERE version IN ('15','16')` → `success=1` per entrambe | query DB |
| AC-S11-STACK | Stack 6 container healthy: `docker compose ps` → tutti Up | docker |

---

## 7. Dipendenze

- **V14 già applicata** (Sprint 10, 2026-05-16)
- **bpm-stub già presente** e funzionante in Docker Compose (da Sprint 4)
- **Issue ISS-S4P-04** (viewer BLOCKED-AUTH): non impatta questo sprint
- **Nessuna dipendenza** da Sprint 12÷16

---

## 8. Riferimenti spec

| Documento | Sezioni |
|---|---|
| `docs/out-discovery-architect/GAP_Architettura.md` | §GAP-US-02, §GAP-US-07, §GAP-US-01 |
| `docs/out-discovery-dba/GAP-DBA.md` | §V101, §V102 |
| `docs/out-GAP-Analysis/03_GAP_Coverage_Review.md` | §GAP-US-01, §GAP-US-02, §GAP-US-07 |
| `docs/out-develop-coordinator/GAP_Roadmap_Sprint11_Sprint16.md` | §Sprint 11 |
