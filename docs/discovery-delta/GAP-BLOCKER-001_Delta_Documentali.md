# GAP-BLOCKER-001 — Delta Documentali (consolidato)

NON è una riscrittura. Ogni voce indica documento, sezione/punto, modifica puntuale, motivazione.
Applicazione subordinata all'autorizzazione del Coordinator e alla risoluzione delle OQ residue (vedi Impact Analysis §7).

## A. Documenti Architect

### A1. [docs/out-discovery-architect/05_API_Candidate.md](docs/out-discovery-architect/05_API_Candidate.md)
- **§2.3 "Allegati & Viewer (BC3)"** — sotto la tabella, aggiungere nota: «`preview` e `download` servono binari materializzati su object storage (MinIO), non proxy verso `LINKDOWNLOAD` sorgente».
- **§2.8 "BPM Integration (BC4)"** — sulla riga `POST /bpm/practices` annotare: «il servizio acquisisce sincrono i binari via `LINKDOWNLOAD` (allow-list host, validazione MIME/estensione, size cap); in caso di fallimento pull → `resultCode = -4` con `details.attachmentErrors[]` e nessuna pratica creata».
- **§4 "Convenzioni contrattuali"** — bullet aggiuntivo: «Trasporto allegati inbound: metadata + URL (`LINKDOWNLOAD`), pull-through sincrono lato SD; nessun binario inline / multipart».

### A2. [docs/out-discovery-architect/04_Workflow_Architecture.md](docs/out-discovery-architect/04_Workflow_Architecture.md)
- **§2 `anc.main` → `svc.openPractice`** — aggiungere bullet: «acquisisce i binari degli allegati via `LINKDOWNLOAD` (allow-list, validazione MIME/estensione, size cap) e li persiste su object storage; emette `AttachmentIngested` per ciascun `CONTENUTI[i]`».
- **§6 "Eventi di dominio chiave"** — riga aggiuntiva: `AttachmentIngested` (BC4 → M-Audit, BC3).

### A3. [docs/out-discovery-architect/06_State_Management.md](docs/out-discovery-architect/06_State_Management.md)
- **§9 Mapping workflow → entità**, riga `svc.openPractice` — annotare: `attachment` popolata con metadati IA (`id_doc`, `file_name`, `estensione`, `link_download`) **e riferimento object storage** del binario acquisito; `ingestion_status` rappresenta la transizione PENDING→AVAILABLE (atomica in POC).

### A4. [docs/out-discovery-architect/07_Deployment_Locale.md](docs/out-discovery-architect/07_Deployment_Locale.md)
- **§1 Tabella Servizi** — su `bpm-stub`: «espone anche un file server statico sui path referenziati da `LINKDOWNLOAD` per esercitare il pull-through SD». Nessun nuovo container.
- **§4 Variabili d'ambiente** — aggiungere: `ATTACHMENT_ALLOWLIST_HOSTS=bpm-stub`, `ATTACHMENT_MAX_BYTES=<cap>`, `ATTACHMENT_PULL_TIMEOUT_MS=<timeout>`.

### A5. [docs/out-discovery-architect/10_POC_Runtime_Simplification_Matrix.md](docs/out-discovery-architect/10_POC_Runtime_Simplification_Matrix.md)
- **Nuova riga #21 — Document transport BPM→SD**:
  - Enterprise previsto: ECM/Object storage condiviso con presigned URL.
  - Sostituzione POC: metadata (`LINKDOWNLOAD`) + pull-through sincrono SD→MinIO via `bpm-stub` file server.
  - Motivazione: IA v1.4 non prevede binario inline; nessun ECM condiviso disponibile localmente.
  - Impatto evolutivo: passaggio a presigned URL/ECM modifica solo `AttachmentFetcher`; schema `attachment` ed endpoint viewer/download invariati.

## B. Documenti DBA

### B1. [docs/out-discovery-dba/01_Modello_ER.md](docs/out-discovery-dba/01_Modello_ER.md)
- **§1 BC3, bullet `attachment`** — aggiungere menzione di `ingestion_status` (PENDING/AVAILABLE/FAILED) + `ingested_at`.
- **§5 Note di modellazione** — nota: «Il binario può essere materializzato in modo non istantaneo rispetto alla INSERT del metadato: `storage_uri/mime_type/size_bytes` ammessi NULL finché `ingestion_status != AVAILABLE`. In POC la transizione PENDING→AVAILABLE è atomica nella stessa transazione di `svc.openPractice`».

### B2. [docs/out-discovery-dba/02_Lifecycle_Dati.md](docs/out-discovery-dba/02_Lifecycle_Dati.md)
- **§2 Transizione `(none) → APERTA`** — precisare: «INSERT `attachment[*]` con `ingestion_status=PENDING`; transizione a `AVAILABLE` post pull-through (POC: stessa transazione); `FAILED` causa rollback dell'apertura pratica».
- **Nuova §4-bis "Lifecycle ingestione allegato"** — tabella stati `PENDING → AVAILABLE` / `PENDING → FAILED` con side-effect `audit_event ATTACHMENT_INGESTED` / `ATTACHMENT_INGEST_FAILED`.

### B3. [docs/out-discovery-dba/03_Schema_DDL.md](docs/out-discovery-dba/03_Schema_DDL.md)
- **§4 DDL `attachment`** — aggiungere colonne:
  - `ingestion_status VARCHAR(16) NOT NULL DEFAULT 'PENDING'`
  - `ingested_at DATETIME(3) NULL`
  - `ingestion_error VARCHAR(500) NULL`
- Rendere NULLABLE: `storage_uri`, `mime_type`, `size_bytes`.
- Aggiungere `CONSTRAINT chk_att_ingestion_status CHECK (ingestion_status IN ('PENDING','AVAILABLE','FAILED'))`.
- Aggiungere `KEY idx_att_ingest_status (ingestion_status)`.

### B4. [docs/out-discovery-dba/04_Strategia_Integrita.md](docs/out-discovery-dba/04_Strategia_Integrita.md)
- **§2 Tabella UNIQUE** — citare esplicitamente `UNIQUE (practice_id, id_doc)` su `attachment` (oggi solo in V6).
- **§4 CHECK** — aggiungere: `attachment.ingestion_status ∈ {PENDING, AVAILABLE, FAILED}`.
- **§5 Vincoli service-enforced** — nuova riga **C-11**: «Transizione `PENDING→AVAILABLE` richiede `storage_uri`, `mime_type`, `size_bytes`, `checksum_sha256` non NULL».

### B5. [docs/out-discovery-dba/06_Dati_POC.md](docs/out-discovery-dba/06_Dati_POC.md)
- **§4 Scenari** — aggiungere scenario «Apertura con allegato ingestito» (PENDING→AVAILABLE atomico) e scenario «URL non raggiungibile» (`FAILED` → rollback pratica).
- **§5 Volumi** — invariato (≈1–3 attachment/pratica).

### B6. [docs/out-discovery-dba/05_Strategia_Migrazioni.md](docs/out-discovery-dba/05_Strategia_Migrazioni.md)
- **§2 Tabella migrazioni** — aggiungere riga `V9 — attachment ingestion lifecycle (GAP-BLOCKER-001)`.
- **NON modificare** le voci V1–V8 (storiche).

## C. Cosa NON modificare

- [docs/out-discovery-business-analist/03_Roadmap_Porting.md](docs/out-discovery-business-analist/03_Roadmap_Porting.md) — baseline roadmap immutata.
- User story e acceptance criteria consolidati Sprint 1–4 — invariati.
- [docs/requirements/source-of-truth/InterfaceAgreement.md](docs/requirements/source-of-truth/InterfaceAgreement.md) — fonte di verità, immutata; la modalità è solo formalizzata lato SD, il contratto BPM resta `metadata + LINKDOWNLOAD`.
- Migrazioni Flyway V1–V8 — immutate.
