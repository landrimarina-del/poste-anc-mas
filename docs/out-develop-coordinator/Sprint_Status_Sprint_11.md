# Sprint Status — Sprint 11

**Sprint:** 11  
**Esito:** ✅ **GO**  
**Data chiusura:** 2026-05-21  
**Owner:** develop-coordinator-gap

---

## Riepilogo

| Indicatore | Valore |
|---|---|
| Scope GAP | GAP-US-01 (Ticketing Mock), GAP-US-02 (BPM Retry sincrono) |
| Migration applicate | V101, V102 ✅ |
| AC totali | 12 |
| AC PASS | 12 |
| AC FAIL | 0 |
| Difetti aperti | 0 |
| Stack container | 7/7 healthy (o senza healthcheck) ✅ |

---

## Acceptance Criteria

| AC ID | Descrizione | Esito |
|-------|-------------|-------|
| AC-S11-DB-1 | V101 applicata: colonne retry in `bpm_outbound_message` | ✅ PASS |
| AC-S11-DB-2 | V102 applicata: `ticket_id` in `practice` | ✅ PASS |
| AC-S11-ADMIN | `GET /admin/mode` → `{"mode":"OK"}` | ✅ PASS |
| AC-S11-ADMIN-2 | `PUT /admin/mode` aggiorna stato in-memory | ✅ PASS |
| AC-S11-TICK-1 | `POST /ticketing/open-ticket` → `ticketId` presente | ✅ PASS |
| AC-S11-TICK-2 | `ticketId` inizia con `"MOCK-TICKET-"` | ✅ PASS |
| AC-S11-BPM-OK | mode=OK → `{"esito":true,"descrizioneEsito":"ACK OK"}` | ✅ PASS |
| AC-S11-BPM-KO | mode=KO → `{"esito":false,...}` | ✅ PASS |
| AC-S11-RETRY | `BpmOutboundService`: `RetryTemplate` + aggiornamento stati DB | ✅ PASS |
| AC-S11-IDEM | Flyway idempotente: V101/V102 non ri-eseguite | ✅ PASS |
| AC-S11-REG | `/actuator/health` → `{"status":"UP"}` | ✅ PASS |
| AC-S11-STACK | Tutti container Up/healthy | ✅ PASS |

---

## Deliverable prodotti

| Tipo | File | Stato |
|------|------|-------|
| Migration DB | `infra/db/migrations/V101__gap_us02_bpm_outbound_retry.sql` | ✅ Applicata |
| Migration DB | `infra/db/migrations/V102__gap_us01_practice_ticket.sql` | ✅ Applicata |
| Backend | `BpmOutboundService.java` (package `it.poste.anc.bpmgw.outbound`) | ✅ |
| Backend | `TicketingClient.java` (package `it.poste.anc.ticketing`) | ✅ |
| Backend | `BpmPracticeInboundService.java` — TicketingClient integrato | ✅ |
| Backend | `HttpBpmOutcomeOutboundGateway.java` — URL parametrico | ✅ |
| Backend | `application-poc.yml` — config `bpm.*`, `ticketing.*` | ✅ |
| Backend | `pom.xml` — dipendenza `spring-retry` | ✅ |
| Backend | `BackendApplication.java` — `@EnableRetry` | ✅ |
| bpm-stub | `infra/bpm-outbound-stub/server.js` — Node.js/Express | ✅ |
| bpm-stub | `infra/bpm-outbound-stub/Dockerfile` | ✅ |
| Docker | `docker-compose.yml` — `bpm-outbound-stub` sostituisce `bpm-outbound-fake` | ✅ |
| QA | `docs/out-develop-qa/Smoke_Test_Report_Sprint_11.md` | ✅ |
| QA | `docs/out-develop-qa/Sprint_Test_Checklist_Sprint_11.md` | ✅ |
| QA | `docs/out-develop-qa/Defect_List_Sprint_11.md` | ✅ |

---

## Issue cross-agent

Vedi `CrossAgent_Issues_Sprint_11.md`:

| ID | Descrizione | Stato |
|----|-------------|-------|
| ISS-S11-01 | bpm-stub era NGINX statico → creato nuovo `bpm-outbound-stub` Node.js | ✅ RISOLTO |
| ISS-S11-02 | BPMN placeholder (Stream 6 N/A) | ✅ N/A |
| ISS-S11-03 | Migration V15/V16 out-of-order per V100 → rinominate V101/V102 | ✅ RISOLTO |

---

## Note per Sprint successivi

### Allineamento versioni migration (ISS-S11-03)

Per Sprint 12÷15, usare la seguente numerazione per le migration Flyway (V100 e V101/V102 già occupate):

| Sprint | Migration |
|--------|-----------|
| 13 | V103, V104, V105 |
| 14 | V106 |
| 15 | V107, V108 |

### Sprint 12 (prossimo)

- Scope: Lavorazione UI — Sidebar + Step + Milestone (GAP-US-03, GAP-US-04, GAP-US-06)
- Nessuna migration DB
- Task: develop-frontend-gap (componenti React, routing, sidebar step/milestone)
- Riferimenti: `docs/out-discovery-ux-mapper/GAP-UX.md §UX-GAP-03 §UX-GAP-04`, `GAP-UI.md §UI-GAP-03`
