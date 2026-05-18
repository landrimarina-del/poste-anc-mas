# GAP-BLOCKER-001 — Remediation Sprint 4

Obiettivo: chiudere il blocker **senza modificare la baseline roadmap**, restando nello scope Sprint 4 (tipizzazione + viewer + download). Nessuna anticipazione di capability Sprint 5+.

## 1. Azioni Architettura / Backend

| # | Azione | Owner | Note |
|---|---|---|---|
| R-A1 | Formalizzare il pattern «metadata + pull-through sincrono» in `svc.openPractice` come decisione architetturale | Architect | Applicare delta documentali A1–A5 |
| R-A2 | Estendere `bpm-stub` con file server statico `GET /files/{ID_DOC}` (asset di test: `sample.pdf`, `sample.png`, `sample.jpg`) referenziato da `LINKDOWNLOAD` nei payload trigger | Backend | Nessun nuovo container; coerente con [10_POC_Runtime_Simplification_Matrix.md](docs/out-discovery-architect/10_POC_Runtime_Simplification_Matrix.md) riga #4 |
| R-A3 | Implementare componente `AttachmentFetcher` in BC4 con: allow-list host (`ATTACHMENT_ALLOWLIST_HOSTS`), size cap (`ATTACHMENT_MAX_BYTES`), timeout (`ATTACHMENT_PULL_TIMEOUT_MS`), validazione `Content-Type` ↔ `ESTENSIONE` | Backend | Mitigazione SSRF |
| R-A4 | Persistere binario su bucket MinIO `anc-attachments`; valorizzare `attachment.storage_uri` (chiave canonica suggerita: `<practiceId>/<ID_DOC>.<estensione>`) + `mime_type`, `size_bytes`, `checksum_sha256` | Backend | Naming `storage_uri` come da schema reale (NON `storage_key`) |
| R-A5 | Emettere evento `AttachmentIngested(success\|failure, reason)` su `event_outbox` per ogni `CONTENUTI[i]` | Backend | M-Audit consumer |
| R-A6 | Failure pull anche su un solo allegato → rollback transazione apertura pratica, `resultCode = -4`, `details.attachmentErrors[]` | Backend | Idempotenza su `ID_WORKITEM` invariata |
| R-A7 | Endpoint `/attachments/{id}/preview` e `/attachments/{id}/download`: lettura **esclusivamente** da MinIO (no proxy verso `LINKDOWNLOAD`) | Backend | Resilienza al downtime sorgente |

## 2. Azioni Dati

| # | Azione | Owner | Note |
|---|---|---|---|
| R-D1 | Creare migrazione `V9__sprint4_attachment_ingestion_lifecycle.sql` (additiva, idempotente) | DBA + Backend | NON modificare V1–V8 |
| R-D2 | DDL V9: `ALTER TABLE attachment MODIFY storage_uri VARCHAR(512) NULL; MODIFY mime_type VARCHAR(64) NULL; MODIFY size_bytes BIGINT NULL;` | DBA | Allinea al lifecycle |
| R-D3 | DDL V9: `ADD COLUMN IF NOT EXISTS ingestion_status VARCHAR(16) NOT NULL DEFAULT 'PENDING' AFTER checksum_sha256; ADD COLUMN IF NOT EXISTS ingested_at DATETIME(3) NULL AFTER ingestion_status; ADD COLUMN IF NOT EXISTS ingestion_error VARCHAR(500) NULL AFTER ingested_at;` | DBA | Idempotente |
| R-D4 | CHECK constraint condizionale `chk_att_ingestion_status_s4` (pattern V8 con prepared statement) `IN ('PENDING','AVAILABLE','FAILED')` | DBA | Idempotente |
| R-D5 | `KEY idx_att_ingest_status (ingestion_status)` con guard su `information_schema` | DBA | Idempotente |
| R-D6 | Backfill in coda a V9: `UPDATE attachment SET ingestion_status='AVAILABLE', ingested_at=COALESCE(ingested_at, created_at) WHERE ingestion_status='PENDING' AND storage_uri IS NOT NULL AND mime_type IS NOT NULL AND size_bytes IS NOT NULL;` | DBA | Retro-compat con dati Sprint 1–3 |
| R-D7 | Eventuale inclusione in V9 dei CHECK `chk_att_codice_doc_s4` / `chk_att_estensione_s4` (DDL drift OQ-G5) — solo se Coordinator autorizza | DBA | Fuori scope GAP se gestito separatamente |

## 3. Criteri di accettazione tecnici (Sprint 4 chiusura GAP)

| ID | Criterio |
|---|---|
| AC-INGEST-1 | Pull di host non in allow-list → rifiuto, `resultCode=-4`, nessuna scrittura su MinIO/DB |
| AC-INGEST-2 | Mismatch `Content-Type` ↔ `ESTENSIONE` dichiarata → rifiuto, `resultCode=-4` |
| AC-INGEST-3 | Pull oltre size cap o timeout → rifiuto, `resultCode=-4` |
| AC-INGEST-4 | Apertura pratica con N allegati validi: N righe `attachment` con `ingestion_status='AVAILABLE'`, `storage_uri/mime_type/size_bytes/checksum_sha256` valorizzati, N oggetti su MinIO |
| AC-VIEWER-1 | `GET /attachments/{id}/preview` serve il binario letto da MinIO con `Content-Type` corretto e supporto Range request |
| AC-VIEWER-2 | Il viewer funziona anche con `bpm-stub` stoppato dopo l'apertura pratica |
| AC-DOWNLOAD-1 | `GET /attachments/{id}/download` ritorna `Content-Disposition: attachment; filename="<NOME_FILE>.<ESTENSIONE>"` |
| AC-AUDIT-1 | Per ogni `CONTENUTI[i]` esiste un evento `AttachmentIngested` (success o failure) |
| AC-IDEM-1 | Re-invio stesso `ID_WORKITEM` → `resultCode=-5`, nessun ri-ingest |
| AC-DB-1 | Re-run V9 (Flyway repair) → no-op |
| AC-DB-2 | INSERT `attachment` con `ingestion_status='BOGUS'` → fallisce CHECK |
| AC-DB-3 | Doppia INSERT stesso `(practice_id, id_doc)` → fallisce UNIQUE `uk_att_practice_id_doc` |

## 4. NON in scope (registrato come debito)

- Antivirus scan su binari acquisiti (FUTURE_ENTERPRISE).
- Retention/purge automatica binari post chiusura pratica (FUTURE_ENTERPRISE).
- Migrazione a presigned URL / ECM condiviso (FUTURE_ENTERPRISE — interessa solo `AttachmentFetcher`).
- Modifiche a user story / acceptance criteria funzionali Sprint 4 — NESSUNA.
- Modifiche alla baseline roadmap — NESSUNA.

## 5. Impatto su artefatti operativi correnti

- Codice Sprint 4 (`IntakeTypingService`, `TypingPage.jsx`, `ActivitiesPage.jsx`, V8): **nessuna regressione richiesta**.
- Endpoint viewer/download esistenti: comportamento allineato al nuovo modello senza cambi di firma.
- `docker-compose.yml`: aggiungere variabili d'ambiente `ATTACHMENT_*` su `anc-backend` e mount asset statici su `bpm-stub`.
