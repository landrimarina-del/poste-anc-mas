# Sprint Status — Sprint 4 PATCH GAP-BLOCKER-001

**Data:** 2026-05-15
**Sprint:** 4 (PATCH)
**Gate finale:** 🟡 **CONDITIONAL-GO**

---

## 1. Esito Acceptance Criteria

| ID | AC | Esito | Evidenza |
|---|---|---|---|
| AC-DB-1 | V9 applicata e idempotente | ✅ PASS | `flyway_schema_history.version='9' success=1`; ricreazione stack `down -v && up -d` senza errori |
| AC-DB-2 | CHECK `ingestion_status ∈ {PENDING,AVAILABLE,FAILED}` | ✅ PASS | `SHOW CREATE TABLE attachment` mostra `chk_att_ingestion_status_s4` |
| AC-DB-3 | UNIQUE `(practice_id, id_doc)` | ✅ PASS | `SHOW INDEXES` mostra `uk_att_practice_id_doc` (Non_unique=0) |
| AC-INGEST-4 | Happy path: pratica APERTA + attachment AVAILABLE + oggetto MinIO | ✅ PASS | HTTP 200 `resultCode=0 practiceId=1`; row `id_doc=S4P-DOC-1 ingestion_status=AVAILABLE storage_uri=s3://anc-attachments/1/S4P-DOC-1.pdf mime_type=application/pdf size_bytes=327`; oggetto MinIO `/data/anc-attachments/1/S4P-DOC-1.pdf` presente |
| AC-INGEST-1 | Host non in allow-list → rollback + resultCode=-4 + attachmentErrors[] | ✅ PASS | HTTP 200 `resultCode=-4` con `{"attachmentErrors":[{"idDoc":"S4P-DOC-HOST","fileName":"verbale.pdf","reason":"Host non in allow-list: example.com"}]}`; query `practice WHERE num_pratica='APP-S4P-002'` ⇒ 0 righe (rollback OK) |
| AC-IDEM-1 | Re-invio stesso ID_WORKITEM → resultCode=-5 | ✅ PASS | HTTP 200 `resultCode=-5 "Idempotenza violata: ID_WORKITEM gia' presente"` |
| AC-VIEWER-1 | GET `/api/v1/attachments/{id}/preview` ⇒ 200 application/pdf | 🟡 BLOCKED-AUTH | endpoint risponde `401 {"resultCode":1002,"resultMessage":"Autenticazione richiesta"}` per chiamata diretta senza sessione operatore; comportamento atteso da SecurityConfig pre-esistente, non regressione introdotta dalla patch. Verifica funzionale viewer **da completare con login operatore** (vedi defect aperti). |

Audit `bpm_inbound_message` mostra correttamente i tracciamenti dei tre risultati (`0`, `-4`, `-5`) con `result_code` e `result_message` attesi.

## 2. Defect

### Chiusi in patch
- **CR-S4-02** Test idempotency con vecchio costruttore 2-args ⇒ corretto a 6-args con Mockito + anonymous PTM; build `--no-cache` **OK**.

### Aperti
- **CR-S4-01** SEV-3 — `infra/bpm-stub/files/sample.png` e `sample.jpg` sono placeholder testuali, non immagini valide. Non blocca gli AC eseguiti (happy path su `sample.pdf`).
- **ISS-S4P-01** SEV-2 — Agente `develop-qa` privo di accesso terminale: AC eseguiti dal Coordinator in suo nome; deliverable QA presenti in [tools/qa/sprint4](../../tools/qa/sprint4) ma marcati BLOCKED-EXEC.
- **ISS-S4P-02** SEV-3 — copertura PNG/JPG felicemente eseguita non possibile finché CR-S4-01 aperto.
- **ISS-S4P-03** SEV-3 — fixture > 25 MB (cap streaming `ANC_ATTACHMENT_MAX_BYTES=26214400`) non disponibile, AC size-cap non verificato.
- **ISS-S4P-04** SEV-3 — AC-VIEWER-1 BLOCKED-AUTH: viewer non riesoglibile senza sessione operatore. Verifica end-to-end via UI (login operatore + apertura pratica) non eseguita in questo round.
- **CR-S4-03 / CR-S4-04** invariati (decisioni di scope già prese).

## 3. Deliverable

### Backend
- [V9__sprint4_attachment_ingestion_lifecycle.sql](../../infra/db/migrations/V9__sprint4_attachment_ingestion_lifecycle.sql)
- [document/ingestion/](../../apps/backend/src/main/java/it/poste/anc/document/ingestion) (`AttachmentFetcher`, `AttachmentStorage`, `FetchedAttachment`, `AttachmentIngestionError`, `AttachmentIngestionException`)
- [BpmPracticeInboundService.java](../../apps/backend/src/main/java/it/poste/anc/bpmgw/inbound/BpmPracticeInboundService.java)
- [BpmInboundMessageWriter.java](../../apps/backend/src/main/java/it/poste/anc/bpmgw/inbound/BpmInboundMessageWriter.java)
- [AttachmentQueryService.java](../../apps/backend/src/main/java/it/poste/anc/document/application/AttachmentQueryService.java)
- [AttachmentController.java](../../apps/backend/src/main/java/it/poste/anc/document/api/AttachmentController.java)
- [application-poc.yml](../../apps/backend/src/main/resources/application-poc.yml)
- [pom.xml](../../apps/backend/pom.xml) (AWS SDK v2 S3 2.25.50)
- Test fix: [BpmPracticeInboundServiceIdempotencyTest.java](../../apps/backend/src/test/java/it/poste/anc/bpmgw/inbound/BpmPracticeInboundServiceIdempotencyTest.java)

### Frontend
- [TypingPage.jsx](../../apps/frontend/src/features/intake/TypingPage.jsx)

### Infra
- [docker-compose.yml](../../docker-compose.yml) (services `minio`, `bpm-stub`; env `ANC_ATTACHMENT_*`)
- [infra/bpm-stub/](../../infra/bpm-stub) (`sample.pdf` reale; `sample.png`/`sample.jpg` placeholder — CR-S4-01)

### QA
- [tools/qa/sprint4](../../tools/qa/sprint4) — `run-ac.ps1`, `verify-v9-schema.sh`
- [docs/out-develop-qa](../../docs/out-develop-qa) — Smoke_Test_Report, Sprint_Test_Checklist, Defect_List, BPM_Workflow_Validation (Sprint_4_Patch_GAP_BLOCKER_001)

### Discovery delta (riferimento)
- [docs/discovery-delta/GAP-BLOCKER-001_*](../discovery-delta) (5 file)

## 4. Vertical slice BPM eseguibile localmente

- Stack: `docker compose down -v && docker compose up -d --build` ⇒ 6 container healthy.
- Inbound BPM: `POST http://localhost/api/v1/bpm/practices` con header `X-SD-API-Key: anc-poc-bpm-inbound-key` e LINKDOWNLOAD `http://bpm-stub/files/sample.pdf` ⇒ pratica aperta, attachment AVAILABLE, oggetto su MinIO.
- Console MinIO disponibile su `http://localhost:9001` (creds: `anc` / `anc-poc-minio-secret`).

## 5. Decisione gate

**🟡 CONDITIONAL-GO** — la patch chiude il GAP-BLOCKER-001 sul percorso happy path e su tutte le condizioni di failure transazionali verificate via API; resta da completare la verifica end-to-end del viewer via UI (ISS-S4P-04) e sostituire i sample PNG/JPG placeholder (CR-S4-01). Entrambi gli aperti sono non-blocker per la decisione di scope del PATCH (trasporto allegati + storage + lifecycle + rollback).
