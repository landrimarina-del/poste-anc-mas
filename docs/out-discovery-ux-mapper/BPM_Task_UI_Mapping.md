# BPM Task UI Mapping — ANC

> Mappa fra workflow BPM (vedi `out-discovery-architect/04_Workflow_Architecture.md`, `06_State_Management.md`) e schermate operative della Scrivania Digitale legacy (Discovery: `Attivazione nuova carta_Discovery.md`).
> Obiettivo: per ogni task BPM identificare la schermata corrispondente, le azioni utente, le transizioni di workflow e gli eventi UI da preservare nel porting.
> Fonti autoritative: Product Backlog ANC v1.3, Documento Discovery ANC, Linee guida controlli Backoffice, Interface Agreement BPM↔SD, matrice controlli, automatismi esiti.

## 1. Indice task BPM → schermata

| # | Task BPM | Tipo | Owner | Schermata SD | Sezione UI principale | Ruolo abilitato |
|---|---|---|---|---|---|---|
| T1 | `svc.openPractice` | service | BC4 (sistema) | nessuna UI (callback BPM) | — | system |
| T2 | `svc.createTask` | service | BC2 (sistema) | nessuna UI | — | system |
| T3 | `task.acceptPractice` | human | BC2 | **Schermata Tipizzazione (pre-accept)** | banner di accettazione + viewer preview | OPERATORE |
| T4 | `task.typeAndChecklist` — step typing | human | BC3 | **Schermata Tipizzazione Documento** | selettore Tipo Documento + viewer | OPERATORE |
| T5 | `task.typeAndChecklist` — milestone Dati Pratica | human (read) | BC3 | **Milestone Dati Pratica** | sezioni Dati Pratica / Dati Cliente | OPERATORE |
| T6 | `task.typeAndChecklist` — milestone Verifica Documento | human | BC3 | **Milestone Verifica Documento** | due colonne: checklist + viewer | OPERATORE |
| T7 | `svc.computeOutcome` | service | BC3 (sistema) | nessuna UI (calcolo) | — | system |
| T8 | `task.typeAndChecklist` — milestone Riepilogo | human | BC3 | **Milestone Riepilogo** | card esito + note + CHIUDI | OPERATORE |
| T9 | `svc.sendOutcomeToBpm` | service | BC4 (sistema) | nessuna UI | — | system |
| T10 | `evt.waitOutcomeAck` | message event | BC4 | nessuna UI (stato transitorio visibile in Lista Pratiche) | colonna Stato = `IN_ATTESA_CONFERMA_BPM` | system |
| T11 | `svc.finalizeOnAck` | service | BC4 (sistema) | refresh Dettaglio Pratica | — | system |
| T12 | `signal.create` / `signal.work` / `signal.close` | service + human | BC6 | **Dashboard Segnalazioni** | viste "Segnalazioni Attive" e "Visualizza Segnalazioni" | OPERATORE / SUPERVISORE |
| T13 | riassegnazione task (workflow trasversale) | human | BC2 | **Tab Riassegna Attività** | filtri Owner/Assegnatario + azioni Riassegna a Gruppo/Utente | SUPERVISORE |

---

## 2. Dettaglio task BPM ↔ UI

### T3 — `task.acceptPractice` → Schermata Tipizzazione (pre-accept)

| Aspetto | Dettaglio |
|---|---|
| Entry point UX | Tab **Attività** → click sul link colonna "Attività" della riga del task in stato IN_CODA |
| Pre-condizioni | Pratica `APERTA`, task `IN_CODA` assegnato al gruppo `GRUPPO_OPERATORE_ANC` |
| Azioni utente | `ACCETTA` (CTA primaria), `INDIETRO` (torna alla Lista Attività senza prendere in carico) |
| Eventi UI | click `ACCETTA` → POST `/api/v1/tasks/{id}/accept`; click `INDIETRO` → navigazione client-side, nessuna mutazione |
| Transizione workflow | `task.IN_CODA` → `IN_CARICO`; `practice.APERTA` → `IN_LAVORAZIONE` |
| Validazioni | nessuna; ACCETTA è atomico (V6 Presa in carico obbligatoria) |
| Stato UI dopo azione | redirect alla schermata **Tipizzazione Documento** (T4) |

### T4 — `task.typeAndChecklist` step typing → Schermata Tipizzazione Documento

| Aspetto | Dettaglio |
|---|---|
| Entry point UX | redirect post-ACCETTA |
| Layout | due colonne: **sinistra** dati base pratica + selettore Tipo Documento + box informativo (icona "i") per errore tecnico; **destra** preview allegato (Piccolo / Medio / Grande), pulsante Download |
| Componenti chiave | dropdown `Tipo Documento` (valori: "Verbale di denuncia" \| "Carta"), CTA `CONFERMA`, controllo dimensione preview, pulsante `Download` |
| Azioni utente | selezione Tipo Documento, CONFERMA, Download allegato, regolazione dimensione preview |
| Eventi UI | click `CONFERMA` → POST `/api/v1/practices/{id}/intake/typing` con payload `{ documentType }` |
| Transizione workflow | nessun cambio stato pratica; persiste `practice.document_type` (irreversibile, V5) |
| Validazioni | Tipo Documento obbligatorio; CONFERMA disabilitata finché non selezionato |
| Comportamento irreversibile | dopo CONFERMA il selettore diventa read-only per tutta la vita pratica |
| Errore tecnico viewer | box info "i" istruisce: tipizzare comunque e chiudere KO se download fallisce (criticità nota viewer) |
| Stato UI dopo azione | abilitazione milestone successive (Dati Pratica, Verifica Documento, Riepilogo) |

### T5 — milestone Dati Pratica (read-only)

| Aspetto | Dettaglio |
|---|---|
| Scopo | consultazione informazioni di testata: data apertura, canale, NUM_PRATICA, dati cliente di anagrafica |
| Azioni utente | navigazione fra milestone (linea avanzamento), espandi/comprimi sezione `Indirizzo di Residenza` |
| Eventi UI | nessuna mutazione |
| Transizione workflow | nessuna |

### T6 — milestone Verifica Documento → cuore istruttoria

| Aspetto | Dettaglio |
|---|---|
| Layout | **2 colonne**: sinistra Dati Cliente + Dati Carta Bloccata + Checklist; destra anteprima allegato (`Piccolo/Medio/Grande`) + Download |
| Checklist abilitata | derivata da `practice.document_type` (Verbale OPPURE Carta) — V4 singola tipologia |
| Controllo primario | radio `Documento presente? Si/No` |
| Logica condizionale (V8 cascata KO) | se `Si` → colonna Conformità abilitata; se `No` → colonna Conformità disabilitata, tutti i controlli auto-KO |
| Causali KO (V9) | obbligatorie se "idoneità formale" = `No`: Intestazione, Firme, Timbro, Dichiarazione, Carta Poste Italiane (selezione multipla → invio KO multipli a BPM, V10) |
| Controllo facoltativo | corrispondenza numero carta (solo se PAN presente nel verbale) |
| Azioni utente | compilazione checklist Si/No, selezione causali KO, `SALVA E PROSEGUI`, `MODIFICA` (su bozza salvata), `Mostra Descrizione` (help in linea per ogni item), Download allegato |
| Eventi UI | `SALVA E PROSEGUI` → PUT `/api/v1/practices/{id}/intake/checklist`; `MODIFICA` → POST `/api/v1/practices/{id}/intake/checklist/edit`; `Mostra Descrizione` → GET `/api/v1/practices/{id}/intake/checklist/help/{itemId}` |
| Transizione workflow | nessun cambio stato pratica; cambia stato checklist `NON_INIZIATA` → `BOZZA` (o `RIAPERTA` se MODIFICA) |
| Validazioni | salvataggio bloccato se "idoneità formale" = No senza causale (V9); item obbligatori non compilati → form inline error |
| Side effect | abilitazione milestone **Riepilogo** dopo primo `SALVA E PROSEGUI` |

### T7 — `svc.computeOutcome` (sistema)

| Aspetto | Dettaglio |
|---|---|
| Innesco | post `SALVA E PROSEGUI` e ricalcolato post-MODIFICA |
| Algoritmo (V7 esito automatico, non forzabile) | tutti SI → `APPROVATA`; almeno un NO → `RESPINTA`; controlli facoltativi vuoti = neutri (non concorrono) |
| Output | snapshot `practice_outcome` con `outcome`, `ko_codes[]`, `notes` |
| Impatto UI | card Esito in **Milestone Riepilogo** (verde APPROVATA / rossa RESPINTA) — sola lettura |

### T8 — milestone Riepilogo → chiusura task

| Aspetto | Dettaglio |
|---|---|
| Componenti | linea avanzamento (Raccolta input / Lavorazione / Chiusura), **card Esito** (verde/rossa, read-only), campo `Note interne` (visibile solo se RESPINTA, facoltativo V12), CTA `CHIUDI PRATICA` |
| Azioni utente | inserimento note (opzionale), `CHIUDI PRATICA`; eventuale `MODIFICA` per tornare alla checklist |
| Eventi UI | `CHIUDI PRATICA` → POST `/api/v1/practices/{id}/intake/close` |
| Transizione workflow | `task.IN_CARICO` → `COMPLETATO`; `practice.IN_LAVORAZIONE` → `IN_ATTESA_CONFERMA_BPM`; consolida `checklist_response.stato=CONSOLIDATA`; emette messaggio outbound BPM (T9) |
| Validazioni | bottone CHIUDI PRATICA abilitato solo se esito calcolato e checklist consolidabile |
| Stato UI dopo azione | task rimosso dalla Lista Attività dell'operatore; redirect possibile a Lista Attività |

### T10 — `evt.waitOutcomeAck` (transitorio)

| Aspetto | Dettaglio |
|---|---|
| Visibilità UI | la pratica appare in Lista Pratiche con `Stato = IN_ATTESA_CONFERMA_BPM`; nessuna azione utente disponibile |
| Timer di sicurezza | gestito a livello BPMN (boundary timer); UI non espone retry manuale nella POC |

### T11 — `svc.finalizeOnAck` (sistema)

| Aspetto | Dettaglio |
|---|---|
| Innesco | POST `/api/v1/bpm/outcome-ack` da BPM |
| Transizione workflow | `IN_ATTESA_CONFERMA_BPM` → `CHIUSA_OK` \| `CHIUSA_KO`; valorizza `data_chiusura = sysdate` (V11) |
| Visibilità UI | refresh delle viste Lista Pratiche, Dettaglio Pratica → Riepilogo (linea avanzamento "Chiusura" completata), Cronologia + Stati aggiornati |

### T12 — `anc.signal` (segnalazioni Sinergia)

| Step | Schermata | Azioni utente | Eventi UI | Transizione |
|---|---|---|---|---|
| start | **Dashboard Segnalazioni → Invio** (da contesto pratica) | compila form, `Invia` | POST `/api/v1/signals` | `(none)` → `IN_CODA` |
| work | **Le Mie Segnalazioni** | presa in carico | POST `/api/v1/signals/{id}/take` | `IN_CODA` → `IN_LAVORAZIONE` |
| reassign | **Visualizza Segnalazioni** (vista globale) | `Riassegna` a operatore/gruppo/sé | POST `/api/v1/signals/{id}/reassign` | nessun cambio stato pratica |
| close | **Visualizza Segnalazioni** | post-ack stub Sinergia | (sistema) | `IN_LAVORAZIONE` → `CHIUSO` |

### T13 — Riassegnazione task (Supervisore)

| Aspetto | Dettaglio |
|---|---|
| Schermata | **Tab Riassegna Attività** |
| Filtri | Pratica N°, Data Assegnazione, Owner, Assegnatario |
| Azioni utente | `Riassegna a Gruppo` (→ `GRUPPO_OPERATORE_ANC`), `Riassegna a Utente` (selezione utente) |
| Eventi UI | POST `/api/v1/supervision/tasks/{id}/reassign-group` \| `.../reassign-user` |
| Transizione workflow | `task.owner_user_id` / `candidate_group_id` aggiornato; **nessun cambio** stato pratica (VC-1) |
| Ruolo abilitato | solo `SUPERVISORE` (V13) |

---

## 3. Eventi UI ↔ eventi di dominio BPM

| Evento UI | API invocata | Evento dominio emesso (outbox) | Consumatori |
|---|---|---|---|
| `ACCETTA` | POST `/tasks/{id}/accept` | `TaskAccepted` | BC1 (transizione stato), M-Audit |
| `CONFERMA tipizzazione` | POST `/practices/{id}/intake/typing` | `DocumentTyped` | M-Audit |
| `SALVA E PROSEGUI` | PUT `/practices/{id}/intake/checklist` | `ChecklistSaved` + `OutcomeComputed` | M-Audit, BC2 |
| `MODIFICA` | POST `/practices/{id}/intake/checklist/edit` | `ChecklistReopened` | M-Audit |
| `CHIUDI PRATICA` | POST `/practices/{id}/intake/close` | `OutcomeSentToBpm` | BC4, M-Audit |
| `Riassegna a Gruppo/Utente` | POST `/supervision/tasks/{id}/reassign-*` | `TaskReassigned` | M-Audit, M-Supervision |
| `Invia segnalazione` | POST `/signals` | `SignalCreated` | M-Audit |
| `Riassegna segnalazione` | POST `/signals/{id}/reassign` | `SignalReassigned` | M-Audit |
| (callback BPM) | POST `/bpm/outcome-ack` | `OutcomeAckReceived` | BC1 (transizione finale), M-Audit |

---

## 4. Vincoli operativi preservati dalla UI

| Codice | Vincolo (BA) | Enforcement UI |
|---|---|---|
| V1 | Idempotenza ID_WORKITEM | nessuna UI (server side resultCode -5) |
| V4 | Singola tipologia documento per pratica | checklist abilitata in base a `document_type`; non si possono compilare entrambe |
| V5 | Tipizzazione irreversibile | dropdown Tipo Documento read-only dopo CONFERMA |
| V6 | Presa in carico obbligatoria | nessuna azione di intake disponibile finché stato `APERTA` |
| V7 | Esito automatico non forzabile | card Esito read-only nella milestone Riepilogo |
| V8 | Cascata KO | radio "Documento presente = No" disabilita colonna Conformità e forza KO sugli item |
| V9 | Causale KO obbligatoria | salvataggio bloccato senza causale quando idoneità formale = No |
| V10 | KO multipli a BPM | selezione multipla causali |
| V11 | Chiusura definitiva solo via ack BPM | stato `IN_ATTESA_CONFERMA_BPM` visibile, nessuna CTA "Forza chiusura" |
| V12 | Note solo se Respinta | campo Note visibile solo con esito RESPINTA |
| V13 | Riassegnazione solo Supervisore | Tab Riassegna nascosto al ruolo OPERATORE |

---

## 5. Schermate non-task (di supporto al workflow BPM)

| Schermata | Scopo | Eventi UI rilevanti per BPM |
|---|---|---|
| **Login** | autenticazione | nessuno |
| **Home** | landing + contatori real-time + 3 istogrammi (Supervisore) | refresh contatori su evento dominio |
| **Tab Attività (Lista Attività)** | coda task operatore (tipo pratica = ANC fisso) | filtri Pratica N°/Stato → query `/tasks` |
| **Tab Pratiche (Lista Pratiche)** | repository pratiche | filtri stato/date/esito → query `/practices` |
| **Dettaglio Pratica – Riepilogo** | avanzamento + dati testata | navigazione a milestone |
| **Dettaglio Pratica – Cronologia** | log attività utente | read-only |
| **Dettaglio Pratica – Stati** | storico transizioni | read-only |
| **Dettaglio Pratica – Azioni Correlate** | azioni sussidiarie (opzionale POC) | read-only |

---

## 6. Conflitti/Open question segnalati al Coordinator

- **CODICE_DOC_ID = 3** (Verbale+Carta?): l'IA elenca il valore in dominio ma non descrive il caso UI. La schermata di Tipizzazione legacy mostra **un solo selettore** "Verbale di denuncia"|"Carta": comportamento atteso quando l'allegato include entrambi non descritto. Coerente con R2/R4 BA.
- **Comportamento "Passed with defect" sui test ST46/ST47** (chiusura pratica): la UX della milestone Riepilogo è descritta come funzionante; eventuali difetti latenti rilevati nei Test Book vanno chiariti prima dello sviluppo.
- **Tasto "Indietro" sulla schermata di accettazione**: indicato come opzionale POC dal BA; in `Discovery` è citato esplicitamente. UX-Mapper lo conserva come azione UI nominata in T3.
