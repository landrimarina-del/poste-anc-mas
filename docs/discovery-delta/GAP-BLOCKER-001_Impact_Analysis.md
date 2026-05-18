# GAP-BLOCKER-001 — Impact Analysis (consolidato)

Fonti consolidate: report Discovery-Architect + Discovery-DBA del 2026-05-15.
Scope: SOLO trasporto documento BPM→SD. Nessun impatto su altri GAP.

## 1. Sintesi del gap

Il GAP rileva che lo Sprint 4 assume — come vincolo certo — il trasporto documento via **metadata + URL** senza che la modalità sia esplicitamente confermata dai documenti funzionali. L'analisi della fonte di verità ([InterfaceAgreement.md](docs/requirements/source-of-truth/InterfaceAgreement.md) v1.4 + Addendum POC) conferma che `DOCUMENTI[].CONTENUTI[]` espone esclusivamente `NOME_FILE`, `ESTENSIONE`, `ID_DOC`, `LINKDOWNLOAD`: **nessun campo binario inline** (no base64, no multipart). L'ambiguità non è sul payload inbound (compatibile solo con metadata+link), ma sulla **strategia di retrieval** lato SD: persistere solo il link (viewer dipende dalla sorgente) o effettuare pull del binario all'apertura pratica (viewer autonomo). Lo schema `attachment` reale (V3 + V6) modella già entrambi i mondi (`link_download` + `storage_uri/mime_type/size_bytes`), ma con i campi binario `NOT NULL`: il modello non supporta uno stato "metadata-only / binario non ancora scaricato", rendendo ambigua la semantica della INSERT al momento dell'apertura.

## 2. Aree e componenti impattati

### 2.1 Architettura (Discovery-Architect)
- **BC4 — BPM Integration (inbound)**: `svc.openPractice` deve definire formalmente la strategia di retrieval allegato.
- **BC3 — Allegati & Viewer**: endpoint `/attachments/{id}/preview` e `/attachments/{id}/download` ([05_API_Candidate.md](docs/out-discovery-architect/05_API_Candidate.md)) devono funzionare anche se la sorgente `LINKDOWNLOAD` diventa indisponibile.
- **Storage (MinIO)**: bucket `anc-attachments` previsto in [07_Deployment_Locale.md](docs/out-discovery-architect/07_Deployment_Locale.md) ma il quando popolarlo non è definito.
- **M-Audit**: deve tracciare l'evento di acquisizione binario.
- **bpm-stub**: in POC deve esporre anche le URL di `LINKDOWNLOAD` come file server statico.
- **Reverse proxy (NGINX)**: invariato.

### 2.2 Dati (Discovery-DBA)
- `attachment` (BC3): manca un esplicito **lifecycle di ingestione**; campi `storage_uri/mime_type/size_bytes` oggi NOT NULL → bloccano stato "metadata-only".
- `practice` (BC1): nessun impatto strutturale.
- `audit_event` / `practice_state_history`: nessun nuovo stato pratica; eventi `ATTACHMENT_INGESTED` / `ATTACHMENT_INGEST_FAILED` esprimibili senza DDL.
- `bpm_inbound_message`: payload archiviato in `payload_json` → invariato.
- Vincolo `UNIQUE (practice_id, id_doc)` già presente (V6) → univocità ID_DOC per pratica garantita.

### 2.3 Flussi impattati
1. **Apertura pratica** (`anc.main → svc.openPractice`): oggi descritto solo come "persiste Practice"; manca il passo allegati.
2. **Persistenza allegato**: ambiguità su `storage_uri` al momento della INSERT.
3. **Viewer / download**: comportamento non definito se l'allegato è solo "link".

## 3. Rischi tecnici e di sicurezza

| Rischio | Severità | Mitigazione proposta |
|---|---|---|
| SSRF su `LINKDOWNLOAD` | Alta | Allow-list host (POC: `bpm-stub`); veto su schemi non-http(s), redirect cross-host, IP privati |
| Ingestione binari non controllata | Media | Validazione `Content-Type` vs `ESTENSIONE` dichiarata; size cap; timeout pull |
| Indisponibilità sorgente in fase viewer | Alta | Materializzare il binario in MinIO al momento dell'apertura |
| Antivirus scan | Media | OUT_OF_SCOPE POC (debito tecnico registrato) |
| Payload size | Bassa | Cap configurabile (`ATTACHMENT_MAX_BYTES`) |
| Audit trail incompleto | Media | Evento `AttachmentIngested(success\|failure, reason)` |

## 4. Opzioni di trasporto valutate

| Opzione | Pro | Contro | Esito |
|---|---|---|---|
| **A. Link + pull-through SD→MinIO @ `svc.openPractice`** | Compatibile IA v1.4 (no breaking); viewer stabile; coerente con MinIO già previsto; SSRF mitigabile | Richiede stub HTTP file server; pull sincrono | **RACCOMANDATA** |
| B. Binario inline base64 | Single round-trip | Breaking change IA; payload pesanti | **SCARTATA** |
| C. Multipart/form-data | Streaming efficiente | Breaking change IA | **SCARTATA** |
| D. Presigned URL su object storage condiviso | Pattern enterprise pulito | Nessun object storage condiviso BPM↔SD in POC | **SCARTATA POC** (target evolutivo) |

## 5. Raccomandazione consolidata

Adottare **Opzione A — metadata + pull-through sincrono SD→MinIO** dentro `svc.openPractice`, persistendo nella tabella `attachment`:
- metadati IA (`id_doc`, `file_name`, `estensione`, `link_download`, `codice_doc_id`) — già presenti
- riferimento storage locale (`storage_uri`, `mime_type`, `size_bytes`, `checksum_sha256`) — già presenti ma da rendere NULLABLE per coerenza con il lifecycle
- nuovo lifecycle `ingestion_status` (`PENDING` / `AVAILABLE` / `FAILED`) per esplicitare semantica e abilitare evoluzione enterprise senza re-design

Coerenza con baseline:
- Interface Agreement v1.4 + Addendum POC: **nessun breaking change**
- [10_POC_Runtime_Simplification_Matrix.md](docs/out-discovery-architect/10_POC_Runtime_Simplification_Matrix.md) riga #7 (MinIO già presente): invariata
- Roadmap baseline: NON modificata; rimane Sprint 4 = tipizzazione + viewer + download

## 6. Decisione di trasporto per la POC

| Punto | Decisione coordinator (proposta) |
|---|---|
| Modalità inbound | metadata + `LINKDOWNLOAD` (URL) — confermata da IA v1.4 |
| Retrieval lato SD | pull-through **sincrono** in `svc.openPractice` |
| Materializzazione | MinIO bucket `anc-attachments` |
| Failure pull (POC) | rollback dell'apertura pratica + `resultCode = -4` con `details.attachmentErrors[]` |
| Lifecycle dato | `ingestion_status` modellato (PENDING/AVAILABLE/FAILED) ma in POC il transito è atomico nella stessa transazione di `svc.openPractice` |
| Evoluzione enterprise | passaggio a presigned URL / ECM modifica solo `AttachmentFetcher`; schema e API invariati |

## 7. Open question (escalation richiesta)

| ID | Tema | Owner suggerito |
|---|---|---|
| OQ-G1 | Politica fallimento parziale: rifiuto totale (proposta) vs pratica `INCOMPLETA` | BA + Stakeholder funzionali |
| OQ-G2 | Retention/purge binari ingestiti post chiusura pratica | BA + DBA (post-POC) |
| OQ-G3 | Stato `FAILED` blocca lavorazione pratica o è solo informativo? (Rilevante solo se OQ-G1 = ingest parziale) | BA + Architect |
| OQ-G4 | Soglie pragmatiche POC (es. 25 MB/file, 10 file/pratica) | BA |
| OQ-G5 | DDL drift: `chk_att_codice_doc` / `chk_att_estensione` descritti in [03_Schema_DDL.md](docs/out-discovery-dba/03_Schema_DDL.md) ma assenti in V3/V6 — gestire nel medesimo `V9` o gap separato? | DBA + Coordinator |

Le OQ NON sono blocker per la remediation Sprint 4 proposta: le scelte di default (rifiuto totale, no retention, FAILED informativo) sono compatibili con la baseline.
