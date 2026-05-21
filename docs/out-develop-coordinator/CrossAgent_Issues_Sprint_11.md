# CrossAgent Issues — Sprint 11

**Sprint:** 11  
**Data rilevazione:** 2026-05-21  
**Owner:** develop-coordinator-gap

---

## ISS-S11-01 — ARCHITETTURA BPM-STUB: servizio non è Node.js/Express

**Severità:** CRITICA  
**Rilevata da:** develop-coordinator-gap (ispezione docker-compose + container inspect)  
**Agente impattato:** develop-backend-gap (Stream 8)

**Problema:**  
Il `Sprint_Execution_Plan_Sprint_11.md §Stream 8` assumeva che `infra/bpm-stub/` fosse un server Node.js/Express da estendere con nuovi endpoint.  
In realtà:
- `anc-bpm-stub` = **NGINX 1.27-alpine** che serve solo file statici da `infra/bpm-stub/files/` (per mock attachment download, GAP-BLOCKER-001)
- `anc-bpm-outbound-fake` = **mendhak/http-https-echo:36** che risponde 200 a qualsiasi richiesta senza logica

Nessuno dei due può implementare:
- `POST /receive-outcome` con modalità OK/KO configurabile
- `POST /ticketing/open-ticket` con generazione UUID
- `GET/PUT /admin/mode` con stato in-memory

**Risoluzione adottata (senza modifica roadmap GAP):**  
Creare un **nuovo servizio** `bpm-outbound-stub` in `infra/bpm-outbound-stub/` (Node.js/Express) che **sostituisce** `bpm-outbound-fake` nel docker-compose.  
La `anc-bpm-stub` (NGINX statico) rimane invariata — scopo diverso.

**Impatto su AC:**  
AC-S11-BPM-OK, AC-S11-BPM-KO, AC-S11-RETRY, AC-S11-ADMIN, AC-S11-ADMIN-2, AC-S11-TICK-1 richiedono il nuovo servizio.

**Riferimento GAP:** `Sprint_Execution_Plan_Sprint_11.md §Stream 8`, `docs/out-discovery-architect/GAP_Architettura.md §GAP-US-07`

---

## ISS-S11-02 — BPMN: nessun `waitOutcomeAck` da rimuovere

**Severità:** INFORMATIVA  
**Rilevata da:** develop-coordinator-gap  
**Agente impattato:** develop-backend-gap (Stream 6)

**Problema:**  
`Sprint_Execution_Plan_Sprint_11.md §Stream 6` richiedeva di rimuovere `evt.waitOutcomeAck` da `anc-main.bpmn20.xml`.  
In realtà esiste solo `sprint0_foundation_placeholder.bpmn20.xml` (processo vuoto start→end).  
Il workflow BPM è implementato direttamente nel codice Java tramite `HttpBpmOutcomeOutboundGateway` → `ANC_BPM_OUTBOUND_URL`.

**Risoluzione adottata:**  
Stream 6 (rimozione nodo BPMN) è **N/A per Sprint 11**.  
Il pattern sincrono retry agisce direttamente su `HttpBpmOutcomeOutboundGateway` / nuovo `BpmOutboundService`.

**Impatto su AC:** Nessuno — AC-S11-* non richiedono modifica BPMN.

---

---

## ISS-S11-03 — FLYWAY: migration V15/V16 out-of-order per V100 preesistente

**Severità:** CRITICA  
**Rilevata da:** develop-coordinator-gap (crash loop backend — `FlywayValidateException`)  
**Agente impattato:** develop-backend-gap (Stream 1-2)

**Problema:**  
Sprint 10 aveva già applicato `V100__favorite_link_crud.sql` al database.  
Le migration Sprint 11 erano denominate `V15__gap_us02_bpm_outbound_retry.sql` e `V16__gap_us01_practice_ticket.sql`.  
Flyway 10.x rifiuta di applicare V15 e V16 perché sono versioni **inferiori a V100** già nella `flyway_schema_history` (`outOfOrder=false` per default).  
Il backend entrava in crash loop con `FlywayValidateException: Detected resolved migration not applied to database: 15/16`.

**Causa radice:**  
Il `Sprint_Execution_Plan_Sprint_11.md` e la `GAP_Roadmap` usavano versioni V15÷V22 per Sprint 11÷15, non considerando che Sprint 10 aveva già usato V100 per il GAP Link Favoriti (UX-GAP-02).

**Risoluzione adottata:**  
Rinominati i file migration:
- `V15__gap_us02_bpm_outbound_retry.sql` → `V101__gap_us02_bpm_outbound_retry.sql`
- `V16__gap_us01_practice_ticket.sql` → `V102__gap_us01_practice_ticket.sql`

**Piano di allineamento versioni migration per Sprint 12÷15:**

| Sprint | Scope | Versioni corrette |
|--------|-------|-------------------|
| 11 | BPM Retry + Ticketing | V101, V102 ← applicato |
| 13 | Checklist Avanzata | V103, V104, V105 |
| 14 | Lista Attività + Filtri | V106 |
| 15 | Tecnici Trasversali | V107, V108 |

**Azioni richieste:**  
- Aggiornare `Sprint_Execution_Plan_Sprint_11.md` con riferimento a V101/V102  
- Il `GAP_Roadmap_Sprint5_Sprint10.md` va annotato (non modificato) con nota di allineamento  
- Aggiornare Sprint_Execution_Plan Sprint 13÷15 con le versioni corrette V103÷V108

**Riferimento GAP:** `GAP-DBA.md §V15 §V16`, `GAP_Roadmap_Sprint5_Sprint10.md §Sprint 11`

---

## Stato issue

| ID | Stato | Risoluzione |
|---|---|---|
| ISS-S11-01 | RISOLTO | Nuovo servizio `bpm-outbound-stub` in `infra/bpm-outbound-stub/` — container healthy |
| ISS-S11-02 | N/A | Stream 6 BPMN non applicabile — workflow implementato in Java |
| ISS-S11-03 | RISOLTO | Migration rinominate V101/V102 — rebuild backend in corso |
| ISS-S11-02 | CHIUSA | Stream 6 N/A — nessuna azione richiesta |
