# 09 - Mapping Architetturale

> Capability → Modulo → Workflow → API → Persistenza. Vista trasversale che lega gli output BA, l'architettura applicativa, il workflow, le API e il modello dati (di competenza DBA).

> Legenda persistenza: l'identificativo entità è indicativo; il modello fisico (PK/FK, indici, vincoli) è responsabilità del Discovery-DBA. Nomi delle tabelle in `snake_case` come da convenzione (cap. 08).

## Tabella di mapping

| Capability BA | Modulo applicativo | Workflow / Step | API esposta | Entità persistite |
|---|---|---|---|---|
| C1.1 Servizio apertura pratica | M-BPM Integration (BC4) | `anc.main` → `svc.openPractice` | `POST /api/v1/bpm/practices` | `practice`, `client_data`, `card_data`, `attachment`, `bpm_inbound_message` |
| C1.2 Validazione messaggio in ingresso | M-BPM Integration | `svc.openPractice` (validator) | `POST /api/v1/bpm/practices` (response) | `bpm_inbound_message` (errore) |
| C1.3 Idempotenza ID_WORKITEM | M-BPM Integration | `svc.openPractice` (pre-check) | `POST /api/v1/bpm/practices` (`-5`) | unique su `practice.id_work_item` |
| C1.4 ResultCode tipizzati | M-BPM Integration | response | `POST /api/v1/bpm/practices` | nessuna persistenza extra |
| C1.5 Stub BPM | bpm-stub (esterno) | trigger esterno | `bpm-stub` UI/CLI | — |
| C2.1 Creazione pratica APERTA | M-Practice Service (BC1) | `svc.openPractice` | inbound BC4 | `practice`, `practice_state_history` |
| C2.2 Lista pratiche con filtri | M-Practice Service | — | `GET /api/v1/practices` | `practice`, viste filtrate |
| C2.3 Ordinamento colonne | M-Practice Service | — | `GET /api/v1/practices?sort=` | — |
| C2.4 Paginazione | M-Practice Service | — | `GET /api/v1/practices?page=&size=` | — |
| C2.5 Export Excel | M-Practice Service | — | `GET /api/v1/practices/export` | — |
| C2.6 Dettaglio Riepilogo | M-Practice Service | — | `GET /api/v1/practices/{id}` | `practice`, `client_data`, `card_data` |
| C2.7 Cronologia | M-Audit (esposto da BC1) | consumer eventi | `GET /api/v1/practices/{id}/history` | `audit_event` |
| C2.8 Stati | M-Audit (esposto da BC1) | consumer transizioni | `GET /api/v1/practices/{id}/states` | `practice_state_history` |
| C2.9 Azioni Correlate | M-Practice Service | — | `GET /api/v1/practices/{id}/related-actions` | `related_action` |
| C3.1 Generazione task | M-Workflow Engine (BC2) | `svc.createTask` | (interno) | `task` (Flowable) |
| C3.2 Lista Attività | M-Workflow Engine | — | `GET /api/v1/tasks` | `task` |
| C3.3 Presa in carico (ACCETTA) | M-Workflow Engine | listener `task.acceptPractice` | `POST /api/v1/tasks/{id}/accept` | `task`, `practice` (stato), `practice_state_history` |
| C3.4 Tasto Indietro | Frontend (UX) | — | nessuna mutazione | — |
| C3.5 Tipo pratica fisso | M-Workflow Engine | filtro task | `GET /api/v1/tasks` | — |
| C4.1 Viewer documenti | M-Document Service (BC3) | — | `GET /api/v1/attachments/{id}/preview` | `attachment` |
| C4.2 Download fallback | M-Document Service | — | `GET /api/v1/attachments/{id}/download` | `attachment` |
| C4.3 Tipizzazione irreversibile | M-Document Service | step `task.typeAndChecklist` | `POST /api/v1/practices/{id}/intake/typing` | `practice.document_type` (immutabile dopo set) |
| C4.4 Checklist Verbale | M-Document Service | step checklist | `GET/PUT /api/v1/practices/{id}/intake/checklist` | `checklist_response` (verbale items) |
| C4.5 Checklist Carta | M-Document Service | step checklist | `GET/PUT /api/v1/practices/{id}/intake/checklist` | `checklist_response` (carta items) |
| C4.6 Causali KO | M-Document Service | validatore checklist | `PUT /intake/checklist` | `checklist_response.ko_reason` |
| C4.7 Logica condizionale (cascata KO) | M-Document Service | regola applicativa | `PUT /intake/checklist` | — |
| C4.8 Salva e Prosegui | M-Document Service | step | `PUT /intake/checklist` | `checklist_response` (stato BOZZA) |
| C4.9 Modifica checklist | M-Document Service | step | `POST /intake/checklist/edit` | `checklist_response` (stato RIAPERTA) |
| C4.10 Help in linea | M-Document Service | — | `GET /intake/checklist/help/{itemId}` | tabella `checklist_help_text` |
| C4.11 Espandi/Comprimi UI | Frontend | — | — | — |
| C4.12 Box informativo errore | Frontend | — | — | — |
| C5.1 Calcolo esito automatico | M-Document Service | `svc.computeOutcome` | (interno) | `practice_outcome` (snapshot) |
| C5.2 Note interne | M-Document Service | step | `PUT /intake/checklist` | `practice_outcome.notes` |
| C5.3 Chiusura task | M-Workflow Engine | `task.typeAndChecklist` close | `POST /api/v1/practices/{id}/intake/close` | `task` (COMPLETATO), `practice` → IN_ATTESA_CONFERMA_BPM |
| C5.4 Invio esito BPM (single) | M-BPM Integration | `svc.sendOutcomeToBpm` | outbound → `bpm-stub` | `bpm_outbound_message` |
| C5.5 Invio esito BPM (KO multipli) | M-BPM Integration | `svc.sendOutcomeToBpm` | outbound → `bpm-stub` | `bpm_outbound_message` |
| C5.6 Sincronizzazione finale | M-BPM Integration | `svc.finalizeOnAck` | `POST /api/v1/bpm/outcome-ack` | `practice` (stato finale, dataChiusura), `practice_state_history` |
| C5.7 Stub BPM ricezione esiti | bpm-stub | — | endpoint stub | — |
| C6.1 Tab Riassegna | M-Workflow Engine | — | `GET /api/v1/supervision/tasks` | `task` |
| C6.2 Riassegna a Gruppo | M-Workflow Engine | — | `POST /api/v1/supervision/tasks/{id}/reassign-group` | `task.assignment` |
| C6.3 Riassegna a Utente | M-Workflow Engine | — | `POST /api/v1/supervision/tasks/{id}/reassign-user` | `task.assignment` |
| C6.4 Filtri Riassegna | M-Workflow Engine | — | `GET /api/v1/supervision/tasks?...` | — |
| C6.5 Contatori real-time | M-Supervision (BC5) | — | `GET /api/v1/supervision/dashboard/counters` | viste aggregate su `practice`, `task` |
| C6.6 Istogramma Pratiche Giornaliere | M-Supervision | — | `.../daily-opened` | aggregati su `practice` |
| C6.7 Istogramma Lavorate (OK/KO) | M-Supervision | — | `.../daily-worked` | aggregati su `practice` |
| C6.8 Istogramma Pratiche per Stato | M-Supervision | — | `.../by-state` | aggregati su `practice` |
| C7.1 Invio segnalazione | M-Signal Service (BC6) | `anc.signal` start | `POST /api/v1/signals` | `signal`, `signal_state_history` |
| C7.2 Le Mie Segnalazioni | M-Signal Service | — | `GET /api/v1/signals/me` | `signal` |
| C7.3 Vista globale | M-Signal Service | — | `GET /api/v1/signals` | `signal` |
| C7.4 Riassegna segnalazione | M-Signal Service | — | `POST /api/v1/signals/{id}/reassign` | `signal.assignment` |
| C7.5 Stub Sinergia | sinergia-stub | — | endpoint stub | — |
| C8.1 Tab navigazione | Frontend Shell | — | — | — |
| C8.2 Login | M-IAM | — | `POST /auth/login` | `user`, `user_role` |
| C8.3 Link Favoriti | Frontend + M-IAM (preferenze) | — | `/api/v1/users/me/favorites` (opt) | `user_favorite_link` |
| C9.1 Audit trail | M-Audit | consumer eventi | (esposto da BC1 history) | `audit_event` |
| C9.2 Storico transizioni | M-Audit | consumer transizioni | (esposto da BC1 states) | `practice_state_history` |
| C9.3 Autorizzazione per ruolo | M-IAM | filtro accessi | tutte le API protette | `user`, `user_role`, `role` |
