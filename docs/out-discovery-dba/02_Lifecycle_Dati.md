# 02 - Lifecycle Dati

> Mapping fra il lifecycle pratica autoritativo (cap. 06 Architect) e le entità persistite. Definisce gli stati, le transizioni, gli effetti collaterali sul DB e l'idempotenza.

## 1. Stati pratica e codifica

| Stato (codice DB) | Descrizione | Terminale | Owner transizione in ingresso |
|---|---|---|---|
| `APERTA` | Pratica creata da BPM, task `IN_CODA` | no | BC4 |
| `IN_LAVORAZIONE` | Operatore ha eseguito ACCETTA | no | BC2 |
| `IN_ATTESA_CONFERMA_BPM` | Esito calcolato e inviato a BPM, in attesa ack | no | BC2 → BC4 |
| `CHIUSA_OK` | Ack BPM positivo | sì | BC4 |
| `CHIUSA_KO` | Ack BPM negativo | sì | BC4 |

Codifica DB: ENUM (MariaDB) o `VARCHAR(32)` con CHECK constraint applicativo. Adottiamo **`VARCHAR(32) + CHECK`** per portabilità (vincolo D4 segnalato dall'Architect — sotto-set SQL).

## 2. Transizioni principali e side-effect

| Transizione | Innesco | Side-effect persistenti (atomici, stessa tx) |
|---|---|---|
| `(none) → APERTA` | `POST /api/v1/bpm/practices` valido | INSERT `practice`, `client_data`, `card_data`, `attachment[*]`, `bpm_inbound_message`, `practice_state_history`(APERTA), `event_outbox`(`PracticeOpened`), INSERT `task`(IN_CODA) |
| `APERTA → IN_LAVORAZIONE` | `POST /api/v1/tasks/{id}/accept` | UPDATE `task` (owner, IN_CARICO), UPDATE `practice.stato`, INSERT `practice_state_history`, INSERT `task_assignment_history`, INSERT `event_outbox`(`TaskAccepted`) |
| `IN_LAVORAZIONE → IN_ATTESA_CONFERMA_BPM` | `POST /api/v1/practices/{id}/intake/close` | UPSERT `practice_outcome`, UPDATE `checklist_response.stato=CONSOLIDATA`, UPDATE `task.stato=COMPLETATO`, UPDATE `practice.stato`, INSERT `practice_state_history`, INSERT `bpm_outbound_message`(PENDING), INSERT `event_outbox`(`OutcomeSentToBpm`) |
| `IN_ATTESA_CONFERMA_BPM → CHIUSA_OK\|CHIUSA_KO` | `POST /api/v1/bpm/outcome-ack` | UPDATE `practice.stato`, UPDATE `practice.data_chiusura=NOW()`, UPDATE `bpm_outbound_message.ack_status`, INSERT `practice_state_history`, INSERT `event_outbox`(`OutcomeAckReceived`) |

### Idempotenza

| Operazione | Chiave idempotenza | Azione su replay |
|---|---|---|
| Apertura pratica | `practice.id_work_item` (UNIQUE) | INSERT respinto → response `resultCode=-5` |
| Accept task | `task.version` (`If-Match`) | conflict 409 se già accettato |
| Close intake | stato pratica = `IN_ATTESA_CONFERMA_BPM` | no-op + 200 |
| Outcome ack | (`practice_id`, `outbound_message_id`) UNIQUE su `bpm_outbound_message` ack | no-op se già finalizzata |

## 3. Lifecycle task (BC2)

| Stato | Descrizione | Transizione |
|---|---|---|
| `IN_CODA` | task creato, nessun owner | → `IN_CARICO` (ACCETTA), → `RIASSEGNATO` (Supervisore) |
| `IN_CARICO` | owner assegnato | → `COMPLETATO` (CHIUDI PRATICA), → `RIASSEGNATO` |
| `COMPLETATO` | task chiuso, esito inviato | terminale |
| `RIASSEGNATO` | stato logico transitorio | torna a `IN_CODA` (se gruppo) o `IN_CARICO` (se utente) |

> Nota: `RIASSEGNATO` viene tracciato in `task_assignment_history` ma non è uno stato "stabile"; lo stato corrente del task resta uno fra `IN_CODA`/`IN_CARICO`/`COMPLETATO`. Modellato così per evitare doppia gestione.

## 4. Lifecycle checklist (BC3)

| Stato `checklist_response.stato` | Innesco |
|---|---|
| `NON_INIZIATA` | nessun record (default implicito) |
| `BOZZA` | "SALVA E PROSEGUI" |
| `RIAPERTA` | "MODIFICA" — riusa stesso record, incrementa `revision` |
| `CONSOLIDATA` | "CHIUDI PRATICA" — snapshot finale, immutabile |

Vincolo: una pratica ha `checklist_response` per ogni item del catalogo applicabile alla sua tipizzazione. La tipizzazione (`practice.document_type`) determina quale subset del catalogo è valido.

## 5. Lifecycle segnalazione (BC6)

| Stato | Innesco |
|---|---|
| `IN_CODA` | creazione (`POST /api/v1/signals`) |
| `IN_LAVORAZIONE` | presa in carico operatore |
| `CHIUSO` | chiusura post-ack stub Sinergia |

## 6. Mapping workflow → entità (vista compatta)

| Step BPMN / azione | Entità scritte | Entità lette |
|---|---|---|
| `svc.openPractice` | practice, client_data, card_data, attachment, bpm_inbound_message, practice_state_history, event_outbox, task | — |
| `svc.createTask` | task, task_assignment_history | practice |
| `task.acceptPractice` (ACCETTA) | task, practice, practice_state_history, task_assignment_history, event_outbox | task, app_user, user_group |
| `task.typeAndChecklist` (typing) | practice (`document_type`), event_outbox | practice |
| `task.typeAndChecklist` (checklist save) | checklist_response, event_outbox | checklist_item_catalog |
| `svc.computeOutcome` | practice_outcome, event_outbox | checklist_response |
| `task.typeAndChecklist` (CHIUDI) | task, practice, practice_state_history, bpm_outbound_message, event_outbox | practice_outcome |
| `svc.sendOutcomeToBpm` | bpm_outbound_message (PENDING→SENT) | bpm_outbound_message |
| `svc.finalizeOnAck` | practice, practice_state_history, bpm_outbound_message, event_outbox | bpm_outbound_message |
| `signal.create` | signal, signal_state_history, event_outbox | practice |
| `signal.reassign` | signal, event_outbox | signal |

## 7. Vincoli di lifecycle a livello DB

- **L-1**: `practice.stato` può cambiare solo via stored procedure applicativa (POC: service layer) che inserisce sempre il record di history nella stessa transazione.
- **L-2**: stati terminali (`CHIUSA_OK`, `CHIUSA_KO`) → trigger applicativo respinge UPDATE successivi.
- **L-3**: `practice.document_type` non è modificabile una volta non-NULL (check trigger applicativo).
- **L-4**: `requestId` è generato all'INSERT iniziale e immutabile (NOT NULL + UNIQUE + nessun UPDATE consentito).
- **L-5**: ogni `event_outbox.status` segue `PENDING → DISPATCHED → ACK` (FAILED come stato di errore retry-able).
