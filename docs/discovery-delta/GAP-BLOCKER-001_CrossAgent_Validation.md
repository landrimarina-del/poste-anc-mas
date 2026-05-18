# GAP-BLOCKER-001 — Cross-Agent Validation Report

Verifica consistenza fra output Discovery-Architect e Discovery-DBA sul perimetro GAP.

## 1. Naming consistency

| Concetto | Architect | DBA | Decisione Coordinator |
|---|---|---|---|
| Path binario in object storage | `storage_key` | `storage_uri` (presente in DDL V3/V6) | **`storage_uri`** (allineamento allo schema reale) |
| Bucket MinIO | `anc-attachments` | `anc-attachments` | OK |
| URL sorgente IA | `LINKDOWNLOAD` / `source_url` | `link_download` (colonna V6) | **`link_download`** lato DB; `LINKDOWNLOAD` resta termine contrattuale IA |
| Evento applicativo | `AttachmentIngested` | `ATTACHMENT_INGESTED` / `ATTACHMENT_INGEST_FAILED` | OK (Architect = nome dominio; DBA = `event_type` su `audit_event`) |
| Lifecycle stati | PENDING/AVAILABLE/FAILED (implicito) | PENDING/AVAILABLE/FAILED | OK |

**Conflitto non bloccante risolto**: usare `storage_uri` nel codice e nella documentazione architetturale per allineamento con lo schema reale.

## 2. Workflow consistency

- Architect: pull-through **sincrono** in `svc.openPractice`, failure ⇒ rollback + `resultCode=-4`, **nessuna pratica creata**.
- DBA: lifecycle dato include stato `FAILED` come riga `attachment` persistita.

**Conflitto rilevato (non bloccante)**: se la POC adotta rollback totale al primo errore di pull, lo stato `FAILED` non viene mai persistito (la transazione viene annullata). 
**Decisione Coordinator**: modellare `ingestion_status` con dominio `{PENDING, AVAILABLE, FAILED}` per coerenza semantica ed evolutività (async/parziale futuro), ma in POC la riga `FAILED` **non viene persistita** in scenario di rifiuto totale (default OQ-G1). Se in futuro OQ-G1 evolve in «ingest parziale + pratica INCOMPLETA», la riga `FAILED` diventa persistibile senza ulteriori modifiche di schema.

## 3. State consistency

- Stati pratica (`practice.stato`): NESSUN nuovo stato richiesto → invariati.
- Stati allegato (`attachment.ingestion_status`): nuovo dominio enumerato, isolato in `attachment`, nessun impatto su `practice_state_history`.
- Idempotenza inbound (`bpm_inbound_message`): invariata, governata da `ID_WORKITEM`.

OK — nessun conflitto.

## 4. Capability consistency vs roadmap

- Scope GAP rientra nelle capability Sprint 4 già pianificate (C4.1, C4.2, C4.3, C4.12) — viewer, download, gestione errore tecnico.
- Nessuna nuova capability introdotta.
- Nessuna capability Sprint 5+ anticipata (checklist, chiusura).
- Nessuna modifica a [docs/out-discovery-business-analist/03_Roadmap_Porting.md](docs/out-discovery-business-analist/03_Roadmap_Porting.md).

OK.

## 5. DDL drift (segnalazione laterale al GAP)

- `chk_att_codice_doc` / `chk_att_estensione` descritti in [docs/out-discovery-dba/03_Schema_DDL.md](docs/out-discovery-dba/03_Schema_DDL.md) ma **assenti** in migrazioni V3/V6.
- **Non causato dal GAP**, ma adiacente.
- **Decisione Coordinator richiesta** (OQ-G5): includere fix in V9 o aprire gap separato. Default proposto: gap separato, V9 resta focalizzata sul GAP-BLOCKER-001.

## 6. Sintesi conflitti

| ID | Tipo | Stato | Risoluzione |
|---|---|---|---|
| CF-1 | Naming (`storage_key` vs `storage_uri`) | Risolto | Usare `storage_uri` |
| CF-2 | Semantica `FAILED` in modalità rollback | Risolto | Stato modellato in dominio, riga non persistita in POC con OQ-G1=default |
| CF-3 | Naming (`LINKDOWNLOAD` IA vs `link_download` DB) | Falso conflitto | Domini distinti (contrattuale vs DB) |

Nessun conflitto bloccante. Nessuna richiesta di rigenerazione deliverable agli agenti.

## 7. Stato finale validazione

**GO** sulla remediation Sprint 4 proposta, soggetto a:
- Conferma OQ-G1 (rifiuto totale al fallimento pull): default proposto compatibile con baseline.
- Conferma OQ-G5 (DDL drift CHECK): default = gap separato.

Le altre OQ (G2, G3, G4) non sono blocker per l'esecuzione della remediation.
