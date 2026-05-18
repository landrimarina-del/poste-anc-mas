# Sprint Execution Plan — Sprint 4 PATCH GAP-BLOCKER-001

**Sprint:** 4 (PATCH)
**Gap chiuso:** GAP-BLOCKER-001 — Document Transport Mode
**Data:** 2026-05-15
**Owner orchestrazione:** develop-coordinator

---

## 1. Scope

Implementare la chiusura del GAP-BLOCKER-001 introducendo:

- trasporto allegati BPM → ANC tramite **metadata + pull-through sincrono** (no base64, no multipart);
- storage oggetto via **MinIO** (bucket `anc-attachments`, chiave `<practiceId>/<idDoc>.<estensione>`);
- lifecycle ingestione su `attachment` (`ingestion_status` ∈ {PENDING, AVAILABLE, FAILED});
- failure pull ⇒ **rollback transazionale** della pratica, `resultCode=-4`, `attachmentErrors[]` nel messaggio;
- viewer attachment legge da MinIO (no più redirect 302);
- regressione frontend: stato `attachmentError` con probe pre-preview.

Lo scope **non** include modifiche a roadmap, architettura, ddl drift CODICE_DOC_ID/ESTENSIONE (OQ-G5).

## 2. Stream eseguiti

| # | Stream | Agente | Output |
|---|---|---|---|
| 1 | DDL lifecycle attachment | DBA (Discovery delta) → develop-backend | [infra/db/migrations/V9__sprint4_attachment_ingestion_lifecycle.sql](../../infra/db/migrations/V9__sprint4_attachment_ingestion_lifecycle.sql) |
| 2 | Ingestion package (fetcher + storage) | develop-backend | [apps/backend/src/main/java/it/poste/anc/document/ingestion](../../apps/backend/src/main/java/it/poste/anc/document/ingestion) |
| 3 | Refactor `openPractice` + writer REQUIRES_NEW | develop-backend | [BpmPracticeInboundService.java](../../apps/backend/src/main/java/it/poste/anc/bpmgw/inbound/BpmPracticeInboundService.java), [BpmInboundMessageWriter.java](../../apps/backend/src/main/java/it/poste/anc/bpmgw/inbound/BpmInboundMessageWriter.java) |
| 4 | Viewer da MinIO + rimozione redirect | develop-backend | [AttachmentQueryService.java](../../apps/backend/src/main/java/it/poste/anc/document/application/AttachmentQueryService.java), [AttachmentController.java](../../apps/backend/src/main/java/it/poste/anc/document/api/AttachmentController.java) |
| 5 | Config + dipendenze | develop-backend | [application-poc.yml](../../apps/backend/src/main/resources/application-poc.yml), [pom.xml](../../apps/backend/pom.xml) |
| 6 | Infra: MinIO + bpm-stub | develop-backend | [docker-compose.yml](../../docker-compose.yml), [infra/bpm-stub](../../infra/bpm-stub) |
| 7 | Regressione frontend (probe preview) | develop-frontend | [TypingPage.jsx](../../apps/frontend/src/features/intake/TypingPage.jsx) |
| 8 | Smoke AC suite | develop-qa (BLOCKED-EXEC) + Coordinator | [tools/qa/sprint4](../../tools/qa/sprint4), AC eseguiti dal Coordinator (vedi Sprint_Status) |
| 9 | Fix compile test costruttore | develop-backend | [BpmPracticeInboundServiceIdempotencyTest.java](../../apps/backend/src/test/java/it/poste/anc/bpmgw/inbound/BpmPracticeInboundServiceIdempotencyTest.java) |

## 3. Decisioni blocked (non rinegoziabili)

| Tema | Decisione |
|---|---|
| Trasporto | metadata + pull-through **sincrono** in `openPractice` |
| Storage | MinIO, bucket `anc-attachments`, chiave `<practiceId>/<idDoc>.<estensione>` |
| Colonna DB | `storage_uri` (NON `storage_key`) |
| Failure pull | rollback transazione + `resultCode=-4` + `attachmentErrors[]` |
| Lifecycle | `ingestion_status` ∈ {PENDING, AVAILABLE, FAILED}, transito atomico in POC |
| Sicurezza | allow-list host (hostname match `bpm-stub`), schemi http/https, no redirect cross-host, MIME vs ESTENSIONE, cap size streaming |

## 4. Sequenza esecuzione

```
DBA(V9) ─▶ Backend(ingestion + refactor + viewer + config) ─▶ Infra(compose+stub+minio)
                                          │
                                          ├─▶ Frontend(regressione probe)
                                          │
                                          └─▶ QA(script AC) ─▶ Coordinator(exec AC)
```

## 5. Dipendenze

- V9 dipende solo da V8 (idempotente, ALTER ADD COLUMN + CHECK pattern PREPARE/EXECUTE).
- Backend `AttachmentStorage` richiede MinIO healthy prima di startup (depends_on healthcheck).
- Backend `AttachmentFetcher` richiede `bpm-stub` started + allow-list configurata via env.
- Frontend probe è additivo, nessuna dipendenza API nuova.

## 6. Deliverable runtime verificati

- 6 container healthy: `anc-db`, `anc-minio`, `anc-bpm-stub`, `anc-backend`, `anc-frontend`, `anc-reverse-proxy`.
- Flyway V1…V9 success=1.
- Bucket `anc-attachments/1/S4P-DOC-1.pdf` presente in MinIO.
- API `/api/v1/bpm/practices` happy + KO host + idempotenza verificati end-to-end.
