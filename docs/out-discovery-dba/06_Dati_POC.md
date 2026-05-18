# 06 - Dati POC

> Dataset minimo necessario a esercitare end-to-end la roadmap (vertical slice). Le pratiche **non** vengono pre-seedate: vengono create dal `bpm-stub` invocando `POST /api/v1/bpm/practices`, così da esercitare il workflow reale (validazione, idempotenza, generazione `requestId`, creazione task).

## 1. Utenti demo

| Username | Ruolo | Gruppi | Scopo |
|---|---|---|---|
| `op.rossi` | OPERATORE_ANC | GRUPPO_OPERATORE_ANC | esegue ACCETTA, tipizzazione, checklist, CHIUDI |
| `op.bianchi` | OPERATORE_ANC | GRUPPO_OPERATORE_ANC | secondo operatore (test riassegnazione) |
| `sup.verdi` | SUPERVISORE_ANC | GRUPPO_OPERATORE_ANC | dashboard, riassegnazioni, contatori |
| `admin` | ADMIN | — | amministrazione utenti, debug |

Password POC: tutte `Demo1234!` (hash BCrypt nel seed). Marcare il seed come **POC-only** (cap. 05 §5).

## 2. Ruoli e gruppi

| Tipo | Codice | Note |
|---|---|---|
| Ruolo | `OPERATORE_ANC` | C8.2 — accesso Lista Attività, intake |
| Ruolo | `SUPERVISORE_ANC` | C6.* — accesso Riassegnazione, dashboard |
| Ruolo | `ADMIN` | gestione tecnica POC |
| Gruppo | `GRUPPO_OPERATORE_ANC` | candidate group `task.acceptPractice` |

## 3. Catalogo checklist (`checklist_item_catalog`)

### 3.1 Checklist Verbale

| code | label | triggers_cascade | help_text |
|---|---|---|---|
| `VERB_DOCUMENTO_PRESENTE` | Documento presente | 1 | Verifica presenza fisica del verbale |
| `VERB_INTESTAZIONE_CORRETTA` | Intestazione corretta | 0 | Confronto con anagrafica cliente |
| `VERB_FIRME_PRESENTI` | Firme presenti | 0 | Tutte le firme richieste apposte |
| `VERB_TIMBRO_PRESENTE` | Timbro presente | 0 | Timbro Ufficio Postale leggibile |
| `VERB_DICHIARAZIONE_COMPLETA` | Dichiarazione completa | 0 | Sezione dichiarazione integralmente compilata |
| `VERB_DATA_VALIDA` | Data verbale valida | 0 | Data coerente con apertura pratica |
| `VERB_LEGGIBILITA` | Documento leggibile | 0 | Scansione di qualità sufficiente |
| `VERB_CORRISPONDENZA_DATI` | Corrispondenza dati cliente | 0 | Dati anagrafici coerenti |

### 3.2 Checklist Carta

| code | label | triggers_cascade | help_text |
|---|---|---|---|
| `CARD_DOCUMENTO_PRESENTE` | Documento Carta presente | 1 | Verifica presenza Carta Poste Italiane |
| `CARD_INTESTAZIONE_CORRETTA` | Intestazione corretta | 0 | Intestazione Carta = cliente |
| `CARD_PAN_LEGGIBILE` | PAN leggibile | 0 | Numero carta visibile |
| `CARD_SCADENZA_VALIDA` | Scadenza valida | 0 | Carta non scaduta |
| `CARD_FIRMA_RETRO` | Firma sul retro | 0 | Firma cliente apposta |
| `CARD_CONFORMITA` | Conformità grafica | 0 | Layout/branding conforme |

> La cascata KO opera così: se `*_DOCUMENTO_PRESENTE` = NO, tutti gli item della checklist sono auto-marcati `NA` (cap. 09 Architect, capability C4.7).

### 3.3 Causali KO baseline (`checklist_response.ko_reason_code`)

Codici letterali, allineati alla baseline funzionale BA:

| Codice | Descrizione |
|---|---|
| `KO_INTESTAZIONE_ERRATA` | Intestazione non conforme |
| `KO_FIRME_MANCANTI` | Firme mancanti o incomplete |
| `KO_TIMBRO_ASSENTE` | Timbro assente o illeggibile |
| `KO_DICHIARAZIONE_INCOMPLETA` | Sezione dichiarazione incompleta |
| `KO_CARTA_NON_CONFORME` | Carta Poste Italiane non conforme |
| `KO_DOCUMENTO_ASSENTE` | Documento non presente |
| `KO_LEGGIBILITA_INSUFFICIENTE` | Scansione non leggibile |

> Mapping `KO_*` → codice numerico per BPM è una **open question** (R3 BA `07_Risk_Open_Questions`). Per il POC, `bpm_outbound_message.payload_json` trasporta i codici letterali; la tabella di trasformazione potrà essere aggiunta come `R__seed_ko_mapping.sql` quando confermata.

## 4. Stati demo / scenari di test

| Scenario | Come riprodurre | Stato finale atteso |
|---|---|---|
| Apertura OK | `bpm-stub` invia `POST /bpm/practices` con `id_work_item` nuovo, `CODICE_DOC_ID ∈ {1,2,3}` | `practice.stato=APERTA`, task `IN_CODA` |
| Idempotenza | secondo invio con stesso `id_work_item` | response `resultCode=-5`, nessun nuovo record |
| Apertura KO validazione | `CODICE_DOC_ID=99` | response `resultCode=-4`, riga in `bpm_inbound_message`, nessuna `practice` |
| Accept task | `op.rossi` → `POST /tasks/{id}/accept` | `task.stato=IN_CARICO`, `practice.stato=IN_LAVORAZIONE` |
| Tipizzazione + checklist OK | `op.rossi` completa flusso, tutti SI | `practice_outcome.outcome=APPROVATA`, `practice.stato=IN_ATTESA_CONFERMA_BPM` |
| Checklist KO multipli | risposte NO su Intestazione + Firme + Timbro | `practice_outcome.outcome=RESPINTA`, `ko_codes_csv` con 3 codici |
| Cascata KO | `VERB_DOCUMENTO_PRESENTE=NO` | tutti gli altri `NA`, `outcome=RESPINTA`, codice `KO_DOCUMENTO_ASSENTE` |
| Modifica checklist | post SALVA → MODIFICA → cambia risposte → CHIUDI | `checklist_response.revision++`, `practice_outcome` ricalcolato |
| Sync finale OK | `bpm-stub` invia `POST /bpm/outcome-ack` con `ack_status=ACK_OK` | `practice.stato=CHIUSA_OK`, `data_chiusura` valorizzata |
| Sync finale KO | `ack_status=ACK_KO` | `practice.stato=CHIUSA_KO` |
| Riassegnazione gruppo | `sup.verdi` → `POST /supervision/tasks/{id}/reassign-group` | `task` torna `IN_CODA`, riga in `task_assignment_history` |
| Riassegnazione utente | `sup.verdi` → `POST /supervision/tasks/{id}/reassign-user` body=`op.bianchi` | `task.owner_user_id` aggiornato |
| Segnalazione | `op.rossi` → `POST /signals` | `signal.stato=IN_CODA` |
| Esportazione Excel | `sup.verdi` → `GET /practices/export?stato=CHIUSA_OK` | file XLSX scaricabile |

## 5. Volumi POC indicativi

| Tabella | Righe attese end-of-POC | Note |
|---|---|---|
| `practice` | 50–200 | generate via stub durante test |
| `task` | ~1× practice | un task ANC per pratica |
| `checklist_response` | ~7× practice (media item compilati) | volume gestibile senza partitioning |
| `attachment` | ~1–3× practice | binari su MinIO, metadata in DB |
| `event_outbox` | ~10× practice | ripulibile dopo dispatch |
| `audit_event` | ~15× practice | nessuna policy di retention nel POC |

## 6. Pulizia / reset POC

Comandi previsti per reset rapido in dev:

```bash
docker compose down -v        # azzera volumi DB + MinIO
docker compose up -d          # ricrea con migrate + seed
```

Nessuna routine di anonymization: i dati POC sono fittizi by design.
