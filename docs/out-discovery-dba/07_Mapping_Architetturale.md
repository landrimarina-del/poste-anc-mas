# 07 - Mapping Architetturale (DBA)

> Vista trasversale che lega: **capability BA → entità persistite** e **workflow → persistenza**. Allineata al [09_Mapping_Architetturale](../out-discovery-architect/09_Mapping_Architetturale.md) dell'Architect. Nessuna nuova capability introdotta (consistency rule).

## 1. Capability → Entità

| Capability BA | Modulo (BC) | Entità primarie | Entità collaterali (audit/outbox) |
|---|---|---|---|
| C1.1 Apertura pratica | BC4 | `practice`, `client_data`, `card_data`, `attachment`, `bpm_inbound_message` | `practice_state_history`, `event_outbox`, `audit_event` |
| C1.2 Validazione payload | BC4 | `bpm_inbound_message` (riga di errore con `result_code=-4`) | — |
| C1.3 Idempotenza ID_WORKITEM | BC4 | UNIQUE `practice.id_work_item` | `bpm_inbound_message` (replay con `result_code=-5`) |
| C1.4 ResultCode tipizzati | BC4 | nessuna persistenza extra | response handler |
| C1.5 Stub BPM | esterno | — | — |
| C2.1 Creazione APERTA | BC1 | `practice`, `practice_state_history` | `event_outbox` |
| C2.2 Lista pratiche con filtri | BC1 | `practice` (read) | indice `idx_practice_stato_data` |
| C2.3 Ordinamento | BC1 | `practice` (read) | — |
| C2.4 Paginazione | BC1 | `practice` (read) | — |
| C2.5 Export Excel | BC1 | `practice` (read) | — |
| C2.6 Dettaglio Riepilogo | BC1 | `practice`, `client_data`, `card_data` | — |
| C2.7 Cronologia | BC1 / M-Audit | `audit_event` | — |
| C2.8 Stati | BC1 / M-Audit | `practice_state_history` | — |
| C2.9 Azioni Correlate | BC1 | `related_action` | — |
| C3.1 Generazione task | BC2 | `task`, `task_assignment_history` (INITIAL) | `event_outbox` |
| C3.2 Lista Attività | BC2 | `task` (read) | indici owner/group |
| C3.3 Presa in carico | BC2 | `task`, `practice` (stato), `practice_state_history`, `task_assignment_history` | `event_outbox` |
| C3.4 Tasto Indietro | FE | nessuna mutazione | — |
| C3.5 Tipo pratica fisso | BC2 | filtro in query (`tipo_pratica='ANC'`) | — |
| C4.1 Viewer documenti | BC3 | `attachment` (read) | binario su MinIO |
| C4.2 Download fallback | BC3 | `attachment` (read) | binario su MinIO |
| C4.3 Tipizzazione irreversibile | BC3 | `practice.document_type` (set unique) | `event_outbox` (`DocumentTyped`) |
| C4.4 Checklist Verbale | BC3 | `checklist_response` (item Verbale), `checklist_item_catalog` | — |
| C4.5 Checklist Carta | BC3 | `checklist_response` (item Carta), `checklist_item_catalog` | — |
| C4.6 Causali KO | BC3 | `checklist_response.ko_reason_code` | seed catalogo |
| C4.7 Cascata KO | BC3 | `checklist_response` (auto-NA via flag `triggers_cascade`) | — |
| C4.8 Salva e Prosegui | BC3 | `checklist_response.stato=BOZZA` | `event_outbox` (`ChecklistSaved`) |
| C4.9 Modifica | BC3 | `checklist_response.stato=RIAPERTA`, `revision++` | `practice_outcome` ricalcolato |
| C4.10 Help in linea | BC3 | `checklist_item_catalog.help_text` | — |
| C4.11/C4.12 UI helpers | FE | — | — |
| C5.1 Calcolo esito | BC3 | `practice_outcome` | `event_outbox` (`OutcomeComputed`) |
| C5.2 Note interne | BC3 | `practice_outcome.notes` | — |
| C5.3 Chiusura task | BC2 | `task.stato=COMPLETATO`, `practice.stato=IN_ATTESA_CONFERMA_BPM`, `practice_state_history` | `event_outbox` |
| C5.4 Invio esito (single) | BC4 | `bpm_outbound_message` (PENDING→SENT) | — |
| C5.5 Invio esito (KO multipli) | BC4 | `bpm_outbound_message` (payload con `ko_codes_csv`) | — |
| C5.6 Sincronizzazione finale | BC4 | `practice` (stato finale + `data_chiusura`), `bpm_outbound_message.ack_status`, `practice_state_history` | `event_outbox` (`OutcomeAckReceived`) |
| C5.7 Stub BPM | esterno | — | — |
| C6.1 Tab Riassegna | BC2/BC5 | `task` (read) | — |
| C6.2 Riassegna a Gruppo | BC2 | `task.candidate_group_id`, `task_assignment_history` (REASSIGN_GROUP) | `event_outbox` (`TaskReassigned`) |
| C6.3 Riassegna a Utente | BC2 | `task.owner_user_id`, `task_assignment_history` (REASSIGN_USER) | `event_outbox` (`TaskReassigned`) |
| C6.4 Filtri Riassegna | BC2 | `task` (read) | indici owner/group |
| C6.5 Contatori real-time | BC5 | aggregati su `practice`, `task` | — |
| C6.6 Istogramma giornaliere | BC5 | aggregato `practice(data_apertura)` | — |
| C6.7 Istogramma lavorate OK/KO | BC5 | aggregato `practice(stato, data_chiusura)` | — |
| C6.8 Istogramma per stato | BC5 | aggregato `practice(stato)` | — |
| C7.1 Invio segnalazione | BC6 | `signal`, `signal_state_history` | `event_outbox` (`SignalCreated`) |
| C7.2 Le Mie Segnalazioni | BC6 | `signal` (read, owner) | — |
| C7.3 Vista globale | BC6 | `signal` (read) | — |
| C7.4 Riassegna segnalazione | BC6 | `signal.owner_user_id`, `signal_state_history` | `event_outbox` (`SignalReassigned`) |
| C7.5 Stub Sinergia | esterno | `signal.sinergia_ack_at` | — |
| C8.1 Tab navigazione | FE | — | — |
| C8.2 Login | M-IAM | `app_user`, `user_role` | — |
| C8.3 Link Favoriti | M-IAM (opt) | `user_favorite_link` (out-of-scope POC) | — |
| C9.1 Audit trail | M-Audit | `audit_event` | — |
| C9.2 Storico transizioni | M-Audit | `practice_state_history` | — |
| C9.3 Autorizzazione per ruolo | M-IAM | `user_role`, `role` | filtri service layer |

## 2. Workflow → Persistenza

> Vista compatta dei BPMN candidati (cap. 04 Architect) e degli effetti persistenti.

### 2.1 `anc.main`

| Step | Tipo | Tabelle scritte | Tabelle lette |
|---|---|---|---|
| `svc.openPractice` | service | `practice`, `client_data`, `card_data`, `attachment`, `bpm_inbound_message`, `practice_state_history`, `event_outbox` | UNIQUE check su `practice.id_work_item` |
| `svc.createTask` | service | `task`, `task_assignment_history` (INITIAL) | `practice` |
| `anc.intake` | callActivity | (vedi §2.2) | — |
| `svc.sendOutcomeToBpm` | service | `bpm_outbound_message` (PENDING→SENT), `practice` (→IN_ATTESA), `practice_state_history`, `event_outbox` | `practice_outcome` |
| `evt.waitOutcomeAck` | message event | nessuna (attesa) | `bpm_outbound_message` |
| `svc.finalizeOnAck` | service | `practice` (stato finale, `data_chiusura`), `bpm_outbound_message.ack_*`, `practice_state_history`, `event_outbox` | `bpm_outbound_message` |

### 2.2 `anc.intake`

| Step | Tipo | Tabelle scritte | Tabelle lette |
|---|---|---|---|
| `task.acceptPractice` (ACCETTA) | human | `task`, `practice` (→IN_LAVORAZIONE), `practice_state_history`, `task_assignment_history`, `event_outbox` | `task` |
| `task.typeAndChecklist` (typing) | human | `practice.document_type` (set unico), `event_outbox` | `practice` |
| `task.typeAndChecklist` (checklist save) | human | `checklist_response` (BOZZA / RIAPERTA), `event_outbox` | `checklist_item_catalog` |
| `svc.computeOutcome` | service | `practice_outcome` (UPSERT) | `checklist_response` |
| `task.typeAndChecklist` (CHIUDI) | human → svc | `checklist_response.stato=CONSOLIDATA`, `task.stato=COMPLETATO`, `practice` (→IN_ATTESA), `practice_state_history`, `bpm_outbound_message` (PENDING) | `practice_outcome` |

### 2.3 `anc.signal`

| Step | Tipo | Tabelle scritte | Tabelle lette |
|---|---|---|---|
| start (creazione) | service | `signal` (IN_CODA), `signal_state_history`, `event_outbox` | `practice` |
| presa in carico | human | `signal` (→IN_LAVORAZIONE), `signal_state_history` | `signal` |
| inoltro Sinergia | service | `signal.sinergia_ack_at` (se ack stub) | — |
| chiusura | service | `signal` (→CHIUSO), `signal_state_history`, `event_outbox` | — |

## 3. Coerenza con principi DOC1

| Principio DOC1 | Come è rispettato dal modello dati |
|---|---|
| P1 Modularità per BC | tabelle raggruppate per ownership BC; nessun cross-write se non via outbox |
| P2 API-first | il modello dati supporta i contratti `/api/v1` definiti dall'Architect senza forzature |
| P4 Event-driven | `event_outbox` come asse trasversale di propagazione |
| P5 Audit-by-default | `practice_state_history`, `task_assignment_history`, `signal_state_history`, `audit_event` |
| P8 Idempotenza | UNIQUE su `id_work_item`, `correlation_id`, `(practice_id, item_id)` |
| P9 Schema evolutivo | Flyway versionato, vincoli portabili, no trigger DB, JSON per payload eterogenei |

## 4. Divergenze segnalate (consistency rule — non risolte autonomamente)

| ID | Origine | Impatto sul modello dati | Stato |
|---|---|---|---|
| D1 | Coordinator 2026-05-13 | DBMS confermato: **MariaDB** (Vincoli). Schema mantenuto portabile (no `ENUM`, no SEQUENCE, JSON come tipo standard) | **✅ Risolta** |
| D4 | Coordinator 2026-05-13 | Sotto-set SQL portabile: confermato no `ENUM`/SEQUENCE/trigger; uso di `VARCHAR + CHECK` e `AUTO_INCREMENT` | **✅ Risolta** |
| R2 | BA 07 risk | Significato funzionale di `CODICE_DOC_ID=3` (Verbale+Carta vs altro) non esplicito nell'IA. Schema modella il valore come `VARCHAR(2) IN ('1','2','3')` senza assumere il mapping descrittivo | **Aperta — BA/Stakeholder** |
| R3 | BA 07 risk | Mapping `KO_*` letterale → codici numerici BPM non confermato. Modellato letterale, mapping rinviato a `R__seed_ko_mapping.sql` | **Aperta — BA/BPM owner** |
