# CrossAgent Issues — Sprint 4 PATCH GAP-BLOCKER-001

**Data:** 2026-05-15
**Sprint:** 4 (PATCH)

---

## 1. Tabella issue

| ID | Severità | Titolo | Owner | Stato | Note |
|---|---|---|---|---|---|
| CR-S4-01 | SEV-3 | Sample `sample.png` e `sample.jpg` in `infra/bpm-stub/files/` sono placeholder testuali, non immagini valide | develop-backend (infra) | OPEN | Non blocca AC eseguiti su `sample.pdf`. Da sostituire con asset binari validi PNG/JPEG di piccola taglia. |
| CR-S4-02 | SEV-1 | Build Maven KO su `BpmPracticeInboundServiceIdempotencyTest` (vecchio costruttore 2-args vs nuovo 6-args) | develop-backend | CLOSED | Risolto con Mockito + anonymous `PlatformTransactionManager` no-op; `docker compose build --no-cache backend` PASS. |
| CR-S4-03 | SEV-3 | DDL drift CHECK `codice_doc_id ∈ {1,2,3}` ed `estensione ∈ {pdf,jpeg,jpg,png}` non emesso in V9 (OQ-G5) | discovery (DBA/Architect) | OPEN (out-of-scope) | Decisione di scope già presa: enforcement applicativo in `hasSupportedDocumentCodes`. Non blocca la patch. |
| CR-S4-04 | SEV-3 | Righe `attachment` Sprint 1-3 con `size_bytes=0` non sono backfillate ad `AVAILABLE`: restano in `PENDING` | discovery → develop-backend | OPEN (by design) | Backfill V9 valorizza `AVAILABLE` solo se `storage_uri`, `mime_type`, `size_bytes` non nulli e size>0. Coerente con il contratto post-patch. |
| ISS-S4P-01 | SEV-2 | Agente `develop-qa` privo di accesso terminale: AC eseguiti dal Coordinator in suo nome | develop-qa / tooling agenti | OPEN | Script e checklist consegnati in `tools/qa/sprint4` e `docs/out-develop-qa`. Esecuzione coperta dal Coordinator. |
| ISS-S4P-02 | SEV-3 | Test happy path PNG/JPG non eseguito | develop-qa / Coordinator | OPEN | Dipende da chiusura CR-S4-01. |
| ISS-S4P-03 | SEV-3 | AC size-cap (> 25 MB) non verificato | develop-qa / Coordinator | OPEN | Fixture > 25 MB mancante; cap configurato via `ANC_ATTACHMENT_MAX_BYTES=26214400`. |
| ISS-S4P-04 | SEV-2 | AC-VIEWER-1 BLOCKED-AUTH: GET `/api/v1/attachments/{id}/preview` richiede sessione operatore (`resultCode=1002`) | develop-qa / Coordinator | OPEN | Non è una regressione: `SecurityConfig` pre-patch già protegge `/api/v1/attachments/**`. Verifica end-to-end da effettuare via UI con login operatore o tramite client autenticato. |

## 2. Note di consistenza cross-agent

- **Architect ↔ DBA:** schema V9 emesso conforme a `_Remediation_Sprint4.md` (colonne `ingestion_status`, `ingested_at`, `ingestion_error`; idempotenza via pattern PREPARE/EXECUTE allineata a V8).
- **DBA ↔ Backend:** convenzione nomi colonne `storage_uri`/`ingestion_status` rispettata; backend usa `JdbcTemplate` con SQL diretti, no JPA drift.
- **Backend ↔ Infra:** env `ANC_ATTACHMENT_*` allineati tra `application-poc.yml` defaults e `docker-compose.yml` override; allow-list host = `bpm-stub`.
- **Backend ↔ Frontend:** contratto preview invariato (`GET /api/v1/attachments/{id}/preview` ⇒ 200 stream | 404 not_available | 5xx technical); regressione frontend usa probe HEAD-equivalente in `useEffect`.
- **Workflow Flowable:** non toccato dalla patch (la decisione blocked di trasporto sincrono nell'inbound preserva il workflow definito dal Discovery MAS); nessun BPMN modificato.

## 3. Azioni di follow-up consigliate

1. Sostituire i placeholder PNG/JPG in `infra/bpm-stub/files/` con asset binari validi (chiude CR-S4-01, ISS-S4P-02).
2. Completare AC-VIEWER-1 end-to-end via UI (login operatore → apertura pratica → click preview) o introdurre uno step di login programmatico nel runner QA (chiude ISS-S4P-04).
3. Generare una fixture > 25 MB per chiudere ISS-S4P-03.
4. Promuovere `develop-qa` ad accesso terminale o spostare l'esecuzione AC su un agente dedicato (chiude ISS-S4P-01).
