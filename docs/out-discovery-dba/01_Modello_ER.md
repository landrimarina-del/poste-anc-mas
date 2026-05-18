# 01 - Modello ER sintetico

> Modello dati derivato dai workflow operativi (cap. 04 Architect), dagli stati pratica (cap. 06 Architect), dalle capability BA, dal mapping architetturale (cap. 09 Architect) e dallo schema funzionale del payload BPM→SD (`docs/requirements/source-of-truth/InterfaceAgreement.md` — sezione "Logica del servizio Interfaccia d'input" / BA `01_Glossario_Assunzioni.md` §7). Baseline funzionale: `Attivazione nuova carta_Discovery.md`. Naming `snake_case` come da convenzione cap. 08 Architect. Tecnologia di riferimento: **MariaDB 10.11+** (decisione Coordinator 2026-05-13 — D1 risolta a favore di MariaDB con schema portabile per il sotto-set SQL D4).

## 1. Entità per bounded context

### BC1 — Practice Management (ownership pratica e dati anagrafici)
- **practice** — entità centrale; rappresenta la pratica ANC. Chiave funzionale `requestId`, idempotenza su `id_work_item`. Contiene anche i campi di testata dell'IA (`canale`, `num_pratica`, `cf_cliente`, `codice_cliente`, `data_inserimento_richiesta`).
- **client_data** — dati cliente associati alla pratica (1:1). Campi allineati alla sezione `CLIENTE` dell'Interface Agreement: nome, cognome, sesso, data/comune/provincia/nazione di nascita, cittadinanza, cellulare, telefono.
- **client_address** — indirizzo di residenza del cliente (1:1 opzionale, sezione `INDIRIZZO_DI_RESIDENZA` dell'IA: luogo, comune, provincia, nazione, cap, civico).
- **card_data** — dati carta bloccata (1:1). Campi allineati alla sezione `DATI_CARTA_BLOCCATA` dell'IA: PAN, tipo carta, intestazione.
- **practice_state_history** — cronologia transizioni di stato (1:N), audit autoritativo del lifecycle (cap. 06 §3 VC-S1).
- **related_action** — azioni correlate visibili nel tab Riepilogo (1:N, opzionale POC).

### BC2 — Workflow / Task Orchestration
- **task** — task ANC generato all'apertura pratica (N:1 con practice). Contiene assegnazione (utente o gruppo), stato (`IN_CODA`/`IN_CARICO`/`COMPLETATO`/`RIASSEGNATO`).
- **task_assignment_history** — storico riassegnazioni (1:N), per audit Supervisore.

> Nota: l'engine Flowable embedded mantiene proprie tabelle (`ACT_*`). La tabella `task` qui modellata è una **proiezione applicativa** sul task Flowable, indicizzata e arricchita per supportare Lista Attività, filtri Supervisione e contatori dashboard senza join verso `ACT_*`.

### BC3 — Document & Checklist
- **attachment** — allegati pratica (N:1 con practice); il binario risiede su MinIO, in DB il riferimento (`storage_uri`) + metadata. Campi allineati alla sezione `DOCUMENTI.CONTENUTI` dell'IA: `codice_doc_id` (1|2|3), `nome_file`, `estensione` (pdf|jpeg|jpg|png), `id_doc` (univoco IA), `link_download` (URL originario).
- **checklist_response** — risposte checklist Verbale/Carta (N:1 con practice). Stato `BOZZA`/`RIAPERTA`/`CONSOLIDATA` (cap. 06 §5).
- **checklist_item_catalog** — catalogo statico item checklist con codice, label, tipo (`VERBALE`/`CARTA`), help text. Seed iniziale.
- **practice_outcome** — snapshot esito calcolato (1:1 con practice; ricreato al `MODIFICA` checklist).

### BC4 — BPM Integration Gateway
- **bpm_inbound_message** — log richieste in ingresso da BPM (per audit, troubleshooting, idempotenza). Unique su `id_work_item` + `received_at`.
- **bpm_outbound_message** — log invii esiti verso BPM (single OK / single KO / KO multipli) con stato consegna e timestamp ack.
- **event_outbox** — outbox pattern: ogni transazione che muta dominio inserisce qui l'evento di dominio (cap. 04 §6); un dispatcher locale lo consuma (POC: in-process).

### BC5 — Supervision & Reporting
> Nessuna entità nuova: usa proiezioni/letture aggregate su `practice`, `task`, `practice_state_history`. Eventuali viste materializzate sono out-of-scope POC.

### BC6 — Signal Management
- **signal** — segnalazione operatore (N:1 con practice). Stato `IN_CODA`/`IN_LAVORAZIONE`/`CHIUSO`.
- **signal_state_history** — storico transizioni (1:N).

### Cross-cutting
- **app_user** — utenti applicativi (login locale POC, SSO target). Username univoco.
- **role** — ruoli applicativi (Operatore ANC, Supervisore ANC, Admin).
- **user_role** — associazione N:N utente↔ruolo.
- **user_group** — gruppi operativi (es. `GRUPPO_OPERATORE_ANC`) per assegnazione task.
- **user_group_member** — associazione N:N utente↔gruppo.
- **audit_event** — audit trail applicativo trasversale (chi/quando/cosa). Riferimento opzionale a `practice_id`.

## 2. Diagramma ER (sintetico)

```
                            ┌────────────────────┐
                            │  bpm_inbound_msg   │
                            └─────────┬──────────┘
                                      │ origina
                                      ▼
   ┌──────────────┐ 1   1  ┌───────────────────┐  1   N  ┌────────────────────────┐
   │ client_data  │◄──────►│      practice     │◄────────│ practice_state_history │
   └──────────────┘        │                   │         └────────────────────────┘
                           │  requestId        │
   ┌──────────────┐ 1   1  │  id_work_item (U) │  1   N  ┌────────────────────────┐
   │  card_data   │◄──────►│  document_type    │────────►│       attachment       │
   └──────────────┘        │  stato            │         └────────────────────────┘
                           │  data_chiusura    │
                           └─┬──────┬──────┬───┘
                             │      │      │
                       1   N │      │ 1  N │ 1   1
                             ▼      ▼      ▼
                       ┌─────────┐ ┌────────────────┐ ┌──────────────────┐
                       │  task   │ │ checklist_resp │ │ practice_outcome │
                       └─┬───────┘ └─────┬──────────┘ └──────────────────┘
                  1   N  │               │ N   1
                         ▼               ▼
              ┌──────────────────┐   ┌───────────────────────┐
              │ task_assign_hist │   │ checklist_item_catalog│
              └──────────────────┘   └───────────────────────┘

   ┌───────────┐ N  N  ┌──────┐         ┌──────────────────┐
   │ app_user  │◄─────►│ role │         │ bpm_outbound_msg │── N:1 ─► practice
   └─────┬─────┘       └──────┘         └──────────────────┘
         │ N
         │ N           ┌────────────┐
         └────────────►│ user_group │── candidate ──► task
                       └────────────┘

   ┌────────┐  N  1  ┌─────────────────────┐
   │ signal │───────►│ signal_state_history│
   └───┬────┘        └─────────────────────┘
       │ N:1
       ▼
    practice

   ┌──────────────┐                     ┌──────────────┐
   │ event_outbox │ (dispatch async)    │ audit_event  │ (trasversale)
   └──────────────┘                     └──────────────┘
```

## 3. Relazioni e cardinalità

| Relazione | Tipo | Vincolo | Note |
|---|---|---|---|
| `practice` 1—1 `client_data` | obbligatoria | FK + unique | snapshot dati cliente da sezione `CLIENTE` IA |
| `client_data` 1—1 `client_address` | opzionale | FK + unique | sezione `INDIRIZZO_DI_RESIDENZA` IA, facoltativa |
| `practice` 1—1 `card_data` | obbligatoria | FK + unique | snapshot dati carta bloccata da sezione `DATI_CARTA_BLOCCATA` IA |
| `practice` 1—N `attachment` | obbligatoria (≥1) | FK | cap. 04: validazione "DOCUMENTI" presente |
| `practice` 1—N `practice_state_history` | obbligatoria (≥1: APERTA) | FK | VC-S1 |
| `practice` 1—N `task` | tipicamente 1, evolutiva ≥1 | FK | task ANC generato da `svc.createTask` |
| `practice` 1—1 `practice_outcome` | opzionale (popolata da `svc.computeOutcome`) | FK + unique | snapshot al CHIUDI PRATICA |
| `practice` 1—N `checklist_response` | opzionale (popolata in intake) | FK | una riga per item compilato |
| `checklist_response` N—1 `checklist_item_catalog` | obbligatoria | FK | item del catalogo |
| `practice` 1—N `bpm_outbound_message` | obbligatoria al `sendOutcomeToBpm` | FK | tracciamento invii |
| `bpm_inbound_message` 1—1 `practice` | opzionale (errore -4/-5: niente practice) | FK nullable | audit input |
| `task` 1—N `task_assignment_history` | obbligatoria (≥1: assegnazione iniziale) | FK | audit riassegnazioni |
| `signal` N—1 `practice` | obbligatoria | FK | segnalazione su pratica |
| `signal` 1—N `signal_state_history` | obbligatoria (≥1) | FK | lifecycle segnalazione |
| `app_user` N—N `role` via `user_role` | — | PK composta | RBAC |
| `app_user` N—N `user_group` via `user_group_member` | — | PK composta | candidate group task |
| `audit_event` N—1 `practice` | opzionale | FK nullable | eventi cross-context |

## 4. Ownership entità ↔ bounded context

| Entità | Owner BC | Scrittori autorizzati |
|---|---|---|
| `practice` | BC1 | BC4 (apertura, finalizzazione), BC2 (transizione IN_LAVORAZIONE → IN_ATTESA), BC3 (set `document_type` immutabile) |
| `client_data`, `client_address`, `card_data` | BC1 | BC4 (apertura) |
| `practice_state_history` | BC1 (M-Audit) | qualunque scrittore di `practice.stato` (atomico, stessa tx) |
| `attachment` | BC3 | BC4 (apertura: registra metadata), BC3 (preview/download read-only) |
| `task`, `task_assignment_history` | BC2 | BC2 esclusivo |
| `checklist_response`, `checklist_item_catalog`, `practice_outcome` | BC3 | BC3 esclusivo |
| `bpm_inbound_message`, `bpm_outbound_message`, `event_outbox` | BC4 | BC4 (inbound/outbound), tutti i BC scrivono `event_outbox` (cross-cutting) |
| `signal`, `signal_state_history` | BC6 | BC6 esclusivo |
| `app_user`, `role`, `user_role`, `user_group`, `user_group_member` | M-IAM | M-IAM (POC: seed); flag soft-delete |
| `audit_event` | M-Audit | scrittura by all, lettura by BC1/BC5 |

## 5. Note di modellazione

- **Snapshot vs. live**: `client_data`/`client_address`/`card_data` sono snapshot al momento dell'apertura (immutabili nel POC). Eventuali aggiornamenti futuri seguiranno pattern history.
- **Document type**: persistito su `practice.document_type` con check applicativo + DB di immutabilità una volta valorizzato (vincolo VC-S4, capability C4.3).
- **Outbox**: `event_outbox` è la sola scrittura "trasversale" abilitata per ogni BC. Il dispatcher è in-process nel POC; nel target migra a broker (DOC2: RabbitMQ/Kafka).
- **Identificatori**: PK tecniche `BIGINT AUTO_INCREMENT`. Identificatori funzionali (es. `requestId`, `id_work_item`) modellati come UNIQUE.
- **Time**: tutti i timestamp in `DATETIME(3)` UTC; il fuso applicativo è gestito a livello presentation.
- **Soft delete**: solo per `app_user` (`active` flag). Le entità di dominio non vengono cancellate (audit-by-default).
