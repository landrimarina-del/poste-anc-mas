# 04 - Workflow Architecture

## 1. Processi BPMN candidati

| ID | Nome | Scopo | Trigger |
|---|---|---|---|
| `anc.main` | Ciclo di vita pratica ANC | Orchestrazione end-to-end pratica: apertura → istruttoria → invio esito → chiusura su ack BPM. | Evento `OpenPracticeRequested` da BC4 (BPM gateway). |
| `anc.intake` | Istruttoria pratica (sotto-processo) | Presa in carico, tipizzazione, checklist, calcolo esito. | Call activity da `anc.main`. |
| `anc.signal` | Gestione segnalazione Sinergia | Lifecycle segnalazione (In Coda → In Lavorazione → Chiuso). | Azione utente "Invia segnalazione". |

## 2. `anc.main` — flusso principale

```
[Start]
   │
   ▼
(SVC) svc.openPractice
   │   - validazione DOCUMENTI, CODICE_DOC_ID ∈ {1,2,3}
   │   - idempotenza ID_WORKITEM (errore -5)
   │   - persiste Practice (stato = APERTA), genera requestId
   ▼
(SVC) svc.createTask
   │   - genera task ANC nella Lista Attività
   ▼
[CallActivity] anc.intake          → produce: outcome, koCodes[], notes
   │
   ▼
(SVC) svc.sendOutcomeToBpm
   │   - costruisce payload conforme Interface Agreement BPM↔SD (single OK / single KO / KO multipli)
   │   - transizione: APERTA/IN_LAVORAZIONE → IN_ATTESA_CONFERMA_BPM
   ▼
(EVT) evt.waitOutcomeAck            ← message event da BC4 (ack BPM)
   │   timer di sicurezza (configurabile, vedi R-OPS-1)
   ▼
(SVC) svc.finalizeOnAck
   │   - transizione: IN_ATTESA_CONFERMA_BPM → CHIUSA_OK | CHIUSA_KO
   │   - valorizza dataChiusura = sysdate
   ▼
[End]
```

## 3. `anc.intake` — sotto-processo istruttoria

```
[Start]
   │
   ▼
(HUMAN) task.acceptPractice
   │   azione: ACCETTA | INDIETRO
   │   - su ACCETTA: stato pratica → IN_LAVORAZIONE; assegnazione owner = utente corrente
   ▼
(HUMAN) task.typeAndChecklist
   │   form steps:
   │     1. Tipizzazione (Verbale | Carta) + CONFERMA  [irreversibile]
   │     2. Checklist Verbale OPPURE Checklist Carta
   │     3. SALVA E PROSEGUI → riepilogo
   │     4. (opzionale) MODIFICA checklist
   │     5. CHIUDI PRATICA
   ▼
(SVC) svc.computeOutcome
   │   - regola: tutti SI → APPROVATA; almeno un NO → RESPINTA
   │   - cascata KO se "documento presente" = NO
   │   - mapping causali → koCodes[]
   ▼
[End]   → outcome, koCodes[], notes
```

### Form engine
- Form dinamici basati sulle definizioni di task: campi della checklist mappati 1:1 sulla baseline (BA US-E6.x / US-E7.x).
- Help in linea per controllo (`Mostra Descrizione`) → testo statico associato a campo.

## 4. Human Task

| Task ID | Form | Owner / Candidate | Vincoli |
|---|---|---|---|
| `task.acceptPractice` | accettazione + viewer preview | candidate group: `GRUPPO_OPERATORE_ANC` | nessun owner pre-assegnato; ACCETTA assegna l'owner |
| `task.typeAndChecklist` | tipizzazione + checklist + chiusura | owner = utente che ha accettato | tipizzazione irreversibile; CHIUDI PRATICA chiude il task |

## 5. Stati pratica e ownership delle transizioni

| Stato | Owner della transizione in ingresso | Innesco |
|---|---|---|
| (inesistente) → **APERTA** | BC4 (BPM gateway) | richiesta apertura validata |
| APERTA → **IN_LAVORAZIONE** | BC2 (workflow) | azione ACCETTA su `task.acceptPractice` |
| IN_LAVORAZIONE → **IN_ATTESA_CONFERMA_BPM** | BC2 → BC4 | CHIUDI PRATICA → invio esito |
| IN_ATTESA_CONFERMA_BPM → **CHIUSA_OK** | BC4 | ack BPM con esito positivo |
| IN_ATTESA_CONFERMA_BPM → **CHIUSA_KO** | BC4 | ack BPM con esito negativo |

Ogni transizione produce: (a) record in `practice_state_history` (M-Audit), (b) evento di dominio sul `event_outbox`.

## 6. Eventi di dominio chiave

| Evento | Emesso da | Consumatori |
|---|---|---|
| `PracticeOpened` | BC4 | BC2 (genera task), M-Audit, M-Supervision |
| `TaskAccepted` | BC2 | BC1 (transizione stato), M-Audit |
| `DocumentTyped` | BC3 | M-Audit |
| `ChecklistSaved` | BC3 | M-Audit |
| `OutcomeComputed` | BC3 | BC2 (continua processo) |
| `OutcomeSentToBpm` | BC4 | M-Audit |
| `OutcomeAckReceived` | BC4 | BC1 (transizione finale), M-Audit |
| `TaskReassigned` | BC2 | M-Audit, M-Supervision |
| `SignalCreated` / `SignalReassigned` | BC6 | M-Audit |

## 7. Riassegnazione (workflow trasversale)

Operazione del Supervisore non inclusa nella definizione BPMN principale:
- Esposta come API di BC2 (`POST /tasks/{id}/reassign`).
- Aggiorna assegnazione del task Flowable; emette `TaskReassigned`; **non altera** lo stato pratica.

## 8. Workflow segnalazioni `anc.signal`

```
[Start] → (SVC) creaSegnalazione (stato = IN_CODA, link a practiceId)
       → (HUMAN) lavorazioneSegnalazione (presa in carico → IN_LAVORAZIONE)
       → (SVC) inoltroSinergia (stub) → (EVT) ackSinergia
       → (SVC) chiudiSegnalazione (stato = CHIUSO)
[End]
```
