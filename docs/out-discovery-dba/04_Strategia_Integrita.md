# 04 - Strategie di Integrità

> Vincoli applicati al modello dati per garantire consistenza fra workflow, stati pratica e capability BA. Si privilegiano vincoli **dichiarativi DB** (PK/FK/UNIQUE/CHECK) ove portabili nel sotto-set SQL concordato (D4 risolta dal Coordinator: schema portabile no `ENUM`/no SEQUENCE/no trigger); i vincoli comportamentali (transizioni atomiche) sono enforced a livello service.

## 1. Chiavi primarie

| Tabella | PK | Strategia |
|---|---|---|
| Tutte le tabelle di dominio | `id BIGINT AUTO_INCREMENT` | identificatori tecnici interni |
| `client_data`, `card_data`, `practice_outcome` | `practice_id` (PK = FK) | relazione 1:1 con `practice` |
| `user_role`, `user_group_member` | PK composta | tabelle di associazione N:N |

## 2. Unique constraint funzionali

| Vincolo | Tabella | Scopo |
|---|---|---|
| UNIQUE `request_id` | `practice` | identificatore funzionale cross-system, immutabile (VC-S5 cap. 06 Architect) |
| UNIQUE `id_work_item` | `practice` | **idempotenza apertura BPM** (capability C1.3, resultCode `-5`) |
| UNIQUE `username` | `app_user` | login |
| UNIQUE `code` | `role`, `user_group`, `checklist_item_catalog` | identificatori semantici |
| UNIQUE `correlation_id` | `bpm_outbound_message` | idempotenza ack BPM (C5.6) |
| UNIQUE (`practice_id`, `item_id`) | `checklist_response` | una risposta per item per pratica |

## 3. Foreign key e politiche referenziali

Politica generale: **`ON DELETE RESTRICT` / `ON UPDATE RESTRICT`**. Audit-by-default: nessuna cancellazione fisica per le entità di dominio. Eccezione: tabelle di associazione N:N (`user_role`, `user_group_member`) → `ON DELETE CASCADE` consentito quando si disattiva un utente.

| FK | Tabella | Riferimento | Policy |
|---|---|---|---|
| `client_data.practice_id` | client_data | practice(id) | RESTRICT |
| `card_data.practice_id` | card_data | practice(id) | RESTRICT |
| `attachment.practice_id` | attachment | practice(id) | RESTRICT |
| `practice_state_history.practice_id` | practice_state_history | practice(id) | RESTRICT |
| `task.practice_id` | task | practice(id) | RESTRICT |
| `task.owner_user_id` | task | app_user(id) | RESTRICT |
| `task.candidate_group_id` | task | user_group(id) | RESTRICT |
| `task_assignment_history.task_id` | task_assignment_history | task(id) | RESTRICT |
| `checklist_response.practice_id` | checklist_response | practice(id) | RESTRICT |
| `checklist_response.item_id` | checklist_response | checklist_item_catalog(id) | RESTRICT |
| `practice_outcome.practice_id` | practice_outcome | practice(id) | RESTRICT |
| `bpm_inbound_message.practice_id` | bpm_inbound_message | practice(id) | RESTRICT (nullable) |
| `bpm_outbound_message.practice_id` | bpm_outbound_message | practice(id) | RESTRICT |
| `signal.practice_id` | signal | practice(id) | RESTRICT |
| `signal_state_history.signal_id` | signal_state_history | signal(id) | RESTRICT |
| `user_role.*`, `user_group_member.*` | associazioni | app_user/role/user_group | CASCADE |

## 4. CHECK constraint (enumerazioni dominio)

| Tabella | Colonna | Valori ammessi |
|---|---|---|
| `practice` | `stato` | APERTA, IN_LAVORAZIONE, IN_ATTESA_CONFERMA_BPM, CHIUSA_OK, CHIUSA_KO |
| `practice` | `document_type` | NULL, VERBALE, CARTA |
| `attachment` | `codice_doc_id` | '1', '2', '3' (dominio IA `DOCUMENTI.CODICE_DOC_ID`) |
| `attachment` | `estensione` | 'pdf', 'jpeg', 'jpg', 'png' (dominio IA `CONTENUTI.ESTENSIONE`) |
| `task` | `stato` | IN_CODA, IN_CARICO, COMPLETATO |
| `task_assignment_history` | `assignment_type` | INITIAL, REASSIGN_USER, REASSIGN_GROUP |
| `checklist_response` | `answer` | NULL, SI, NO, NA |
| `checklist_response` | `stato` | BOZZA, RIAPERTA, CONSOLIDATA |
| `checklist_item_catalog` | `document_type` | VERBALE, CARTA |
| `practice_outcome` | `outcome` | APPROVATA, RESPINTA |
| `bpm_outbound_message` | `send_status` | PENDING, SENT, FAILED |
| `bpm_outbound_message` | `ack_status` | NULL, ACK_OK, ACK_KO |
| `signal` | `stato` | IN_CODA, IN_LAVORAZIONE, CHIUSO |
| `event_outbox` | `status` | PENDING, DISPATCHED, FAILED |

## 5. Vincoli di consistenza non dichiarativi (service-enforced)

> Vincoli che non sono esprimibili come CHECK semplici e che vengono validati nel service layer dentro la stessa transazione.

| ID | Vincolo | Enforcement |
|---|---|---|
| C-1 | Ogni cambio di `practice.stato` produce un record in `practice_state_history` (VC-S1) | service `PracticeStateService.transition(...)` atomico |
| C-2 | `practice.stato` terminale (CHIUSA_*) non può essere riaperta (VC-S3) | guard nel service + check trigger applicativo |
| C-3 | `practice.document_type` immutabile una volta valorizzato (VC-S4, C4.3) | guard nel service |
| C-4 | `request_id` immutabile (VC-S5) | nessun setter pubblico; UPDATE respinto |
| C-5 | Solo BC4 transita verso APERTA / CHIUSA_*; solo BC2 verso IN_LAVORAZIONE / IN_ATTESA | service di stato esposto solo ai package owner |
| C-6 | `checklist_response.stato=CONSOLIDATA` è immutabile (snapshot finale) | guard service |
| C-7 | Ad ogni transizione di stato pratica corrisponde una riga in `event_outbox` | service di stato `addOutboxEvent(...)` |
| C-8 | `practice_outcome` è ricalcolato (DELETE+INSERT o UPSERT) ad ogni "MODIFICA" checklist | service `OutcomeService.recompute(...)` |
| C-9 | Tipizzazione (`document_type`) deve essere coerente con gli `item_id` delle `checklist_response` (subset del catalogo per `document_type`) | validazione service alla `PUT /intake/checklist` |
| C-10 | Una pratica chiusa non accetta nuove `signal` con stato attivo | guard service `SignalService.create(...)` |

## 6. Concorrenza e locking

- **Optimistic locking**: colonna `version` su `practice` e `task` (Hibernate `@Version`). Conflitti restituiscono HTTP 409.
- **Idempotency tokens**: `bpm_outbound_message.correlation_id` UNIQUE consente retry sicuri di `svc.sendOutcomeToBpm`.
- **Outbox**: dispatcher single-writer in POC (no concorrenza); SELECT + UPDATE con `WHERE status='PENDING'` e advisory ordering by `id`.

## 7. Riservatezza dati

- `app_user.password_hash`: BCrypt cost ≥10 (POC); nessuna password in chiaro.
- `client_data.codice_fiscale`, `card_data.iban`/`pan_masked`: classificati PII; **mascheratura** in tutti i log applicativi (no full PAN, no full IBAN nei log).
- Nessuna criptazione at-rest applicata nel POC (responsabilità infrastruttura target — fuori scope).
- `audit_event.payload_json`: convenzione di non includere PII completo; usare riferimento per ID.

## 8. Vincoli portabilità SQL

> Allineamento a D4 (risolta dal Coordinator: schema portabile). Non si usano:
- tipo `ENUM` (sostituito con `VARCHAR + CHECK`);
- `SEQUENCE` (sostituite con `AUTO_INCREMENT`);
- procedure stored o trigger DB (logica in service layer);
- specifici tipi MariaDB-only se non strettamente necessari (eccetto `JSON` — supportato anche in PostgreSQL come `JSONB`, mappato via JPA).
