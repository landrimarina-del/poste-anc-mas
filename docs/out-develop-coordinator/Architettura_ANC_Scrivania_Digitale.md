# Architettura ANC Scrivania Digitale — Vista Business

## Lo stack completo

```
┌─────────────────────────────────────────────────────────────────────┐
│  UTENTE (Browser)                                                   │
│                                                                     │
│  http://localhost:81      → Scrivania Digitale (React)              │
│  http://localhost:8083    → KIE Management Console (admin)          │
│  http://localhost:8084    → KIE Task Console (operatori)            │
└───────────┬─────────────────────┬───────────────────────────────────┘
            │ REST                │ GraphQL
            ▼                     ▼
┌───────────────────┐   ┌──────────────────────────────────────────┐
│  BACKEND ANC      │   │  DATA INDEX  (porta 8082)                │
│  Spring Boot      │   │  Quarkus — legge eventi HTTP             │
│  porta 8081       │──▶│  PostgreSQL — read-model                 │
│                   │   │  espone GraphQL per le console           │
│  ┌─────────────┐  │   └──────────────────────────────────────────┘
│  │ Kogito 10   │  │
│  │ EMBEDDED    │  │   ┌──────────────────────────────────────────┐
│  │ (stesso JVM)│  │──▶│  MARIADB  (porta 3308)                   │
│  └─────────────┘  │   │  schema "anc"    → dati applicativi      │
│                   │   │  schema "kogito" → stato processo BPMN   │
└───────────────────┘   └──────────────────────────────────────────┘
```

---

## Cosa succede quando un BPM apre una pratica

```
BPM ESTERNO                  BACKEND ANC                    KOGITO ENGINE
(es. sistema Poste)          (Spring Boot :8081)            (embedded)
     │                              │                            │
     │── POST /api/v1/tasks ───────▶│                            │
     │   (payload pratica)          │                            │
     │                              │ 1. Salva pratica in DB ANC │
     │                              │    stato = APERTA          │
     │                              │                            │
     │                              │ 2. startProcess() ────────▶│
     │                              │                            │ Crea istanza
     │                              │                            │ processo BPMN
     │                              │                            │ "anc_pratica"
     │                              │                            │
     │                              │ 3. Salva task in DB ANC    │
     │                              │    stato = IN_CODA         │◀─ UserTask creata
     │                              │    (UserTask = Ready)      │   in RAM
     │                              │                            │
     │                              │ 4. Pubblica evento ───────────────────────▶
     │                              │    ProcessInstanceStateDataEvent          │
     │                              │    UserTaskInstanceStateDataEvent         │
     │◀─ HTTP 200 ─────────────────│                            │   DATA INDEX  │
                                                                    aggiorna PG  │
                                                                    ─────────────┘
```

---

## Cosa succede quando l'operatore accetta la pratica

```
OPERATORE                    BACKEND ANC                    KOGITO ENGINE
(Scrivania :81)              (:8081)                        (embedded)
     │                              │                            │
     │── POST /api/v1/tasks/        │                            │
     │   {id}/accept ──────────────▶│                            │
     │                              │ 1. UPDATE task in DB ANC   │
     │                              │    stato = IN_CARICO       │
     │                              │    owner = op.rossi        │
     │                              │                            │
     │                              │ 2. claimTask() ───────────▶│
     │                              │                            │ UserTask
     │                              │                            │ Ready → Reserved
     │                              │                            │ actualOwner=op.rossi
     │                              │                            │
     │                              │ 3. Pubblica evento ───────────────────────▶
     │◀─ HTTP 200 ─────────────────│    UserTaskInstanceStateDataEvent         │
                                                                    DATA INDEX  │
                                                                    status=Reserved
```

---

## Cosa succede quando l'operatore chiude la pratica

```
OPERATORE                    BACKEND ANC                    KOGITO ENGINE
     │                              │                            │
     │── POST /api/v1/practices/    │                            │
     │   {id}/intake/close ────────▶│                            │
     │                              │ 1. validateChecklist()     │
     │                              │ 2. UPDATE practice         │
     │                              │    stato = IN_ATTESA_ACK   │
     │                              │ 3. DELETE task da DB       │
     │                              │                            │
     │                              │ 4. completeTask() ────────▶│
     │                              │                            │ UserTask
     │                              │                            │ Reserved → Completed
     │                              │                            │ Processo avanza
     │                              │                            │ (o termina)
     │                              │ 5. Invia esito a BPM       │
     │                              │    (outbound stub)         │
     │◀─ HTTP 200 ─────────────────│                            │
```

---

## Le tre persistenze — chi ricorda cosa

```
┌──────────────────────────────────────────────────────────────────┐
│  MariaDB — schema "anc"          DATI APPLICATIVI                │
│  ✅ sopravvive al restart                                        │
│                                                                  │
│  practice    → numero pratica, stato ANC, chi l'ha aperta       │
│  task        → assegnazione operatore, chi ha fatto claim       │
│  checklist   → documenti verificati                             │
│  attachment  → metadati allegati (file su MinIO)                │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│  MariaDB — schema "kogito"       STATO PROCESSO BPMN             │
│  ✅ sopravvive al restart                                        │
│                                                                  │
│  process_instances → blob con lo stato interno del BPMN         │
│                      (in quale nodo si trova il processo)        │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│  RAM JVM                         USERTASK CORRENTI               │
│  ❌ si azzera al restart                                         │
│                                                                  │
│  UserTask → chi ha il claim, stato Ready/Reserved/Completed     │
│  (in produzione si usa il JDBC addon per persistere anche qui)  │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│  PostgreSQL — Data Index         STORICO PER MONITORAGGIO        │
│  ✅ sopravvive al restart                                        │
│                                                                  │
│  processes       → lista processi, stati, date, businessKey     │
│  user_task_inst  → chi ha fatto claim, quando, su quale pratica │
│  READ-ONLY: aggiornato via eventi HTTP, NON è la fonte di verità│
└──────────────────────────────────────────────────────────────────┘
```

---

## Limiti POC vs Produzione

| Aspetto | POC attuale | Produzione |
|---|---|---|
| **UserTask persistence** | RAM (perse al restart) | JDBC addon → DB |
| **Kogito runtime** | Embedded in Spring Boot | Quarkus standalone |
| **Autenticazione console** | Nessuna | Keycloak / OIDC |
| **Deploy** | 1 container backend | Runtime separato |
| **Scalabilità** | Single node | Cluster + Infinispan |

---

## In una frase per il business

> Il sistema BPM esterno (Poste) **apre** la pratica → Kogito **orchestra** il ciclo di vita tramite un workflow BPMN → l'operatore **lavora** sulla Scrivania Digitale → a chiusura il sistema **notifica** il BPM con l'esito. Tutto il processo è **monitorabile in tempo reale** dalla KIE Management Console.

---

## Il ciclo di vita completo del Task

```
EVENTO                    TASK (DB anc)              PRATICA (DB anc)        KOGITO (RAM)
─────────────────────────────────────────────────────────────────────────────────────────
BPM apre pratica
  └─ POST /tasks          creato                     APERTA                  UserTask
                          stato = IN_CODA                                     Ready

Operatore accetta
  └─ POST /tasks/{id}/    aggiornato                 IN_LAVORAZIONE          UserTask
     accept               stato = IN_CARICO                                   Reserved
                          owner = op.rossi                                    actualOwner
                                                                              = op.rossi

Operatore chiude
  └─ POST /practices/     ❌ CANCELLATO              IN_ATTESA_ACK           UserTask
     {id}/intake/close    DELETE dal DB                                       Completed
                          (non esiste più)

Sistema esterno risponde
  (sincrono nel close)                               CHIUSA_OK               Processo
                                                     oppure                  COMPLETED
                                                     CHIUSA_KO               (termina)
```

### Il punto chiave sul Task

Il task viene **cancellato fisicamente** (`DELETE FROM task`) nel momento stesso in cui l'operatore chiude la pratica — prima ancora di aspettare la risposta dal sistema esterno.

La logica è:
> "L'operatore ha fatto il suo lavoro → il task è finito → non serve più tracciarlo"

La **pratica** invece rimane e traccia lo stato di attesa:

```
IN_LAVORAZIONE
    │
    │ operatore chiude
    ▼
IN_ATTESA_ACK          ← pratica esiste, task no
    │
    │ sistema esterno risponde (sincrono, stesso HTTP call)
    ▼
CHIUSA_OK  o  CHIUSA_KO   ← processo Kogito = COMPLETED
```

### Riepilogo semantico delle entità

| Entità | Rappresenta | Quando nasce | Quando muore |
|---|---|---|---|
| **Processo Kogito** | Il workflow BPM della pratica | apertura | chiusura confermata |
| **UserTask Kogito** | Il "token" di lavoro nel BPMN | apertura | chiusura (completeTask) |
| **Task DB ANC** | L'assegnazione operatore | apertura | chiusura (DELETE) |
| **Pratica DB ANC** | Il dato business | apertura | mai (storico permanente) |

Il **task ANC** e la **UserTask Kogito** sono due rappresentazioni dello stesso concetto — uno nel DB applicativo, l'altro nel motore BPM — e muoiono entrambi alla chiusura.

---

## Struttura del BPMN — 1 processo, 1 task

In questa POC il BPMN ha **un solo UserTask**:

```
PROCESSO anc_pratica
│
├── START
│
├── [UserTask] "Lavorazione Pratica"   ← l'unico task
│        │
│        │  Ready → Reserved → Completed
│        │  (aperta) (accettata) (chiusa)
│
└── END
```

**1 processo = 1 pratica = 1 task** che cambia stato, poi viene cancellato quando il processo termina.

In un BPMN più complesso ci potrebbero essere più task sequenziali o paralleli
(es. "Verifica Documentale" → "Approvazione Supervisore" → "Notifica Cliente") —
ogni nodo sarebbe un task separato. Nella POC il flusso è volutamente semplice:
un solo step di lavorazione.

---

## Cosa vedi nelle console KIE

### Management Console (`:8083`) — Processi

| Momento | Processo visibile? | Stato |
|---|---|---|
| Pratica creata | ✅ | ACTIVE |
| Operatore accetta | ✅ | ACTIVE |
| Pratica chiusa | ✅ | COMPLETED |

Il processo **rimane sempre visibile** — anche dopo la chiusura è nello storico con stato COMPLETED.

---

### Task Console (`:8084`) — UserTask

| Momento | Task visibile? | Stato |
|---|---|---|
| Pratica creata | ✅ | Ready |
| Operatore accetta | ✅ | Reserved |
| Pratica chiusa | ⚠️ | Completed — dipende dal filtro |

Il task **non scompare** dal Data Index — l'evento `Completed` viene salvato.
La Task Console di default mostra solo task **attivi** (Ready/Reserved).
Per vedere i task completati occorre usare il filtro "Show completed" o "All".

```
Task Console (filtro default)     Task Console (filtro ALL)
┌──────────────────────┐          ┌──────────────────────┐
│  Ready    ✅ visibile│          │  Ready      ✅        │
│  Reserved ✅ visibile│          │  Reserved   ✅        │
│  Completed ❌ nascosto│         │  Completed  ✅        │
└──────────────────────┘          └──────────────────────┘
```

> **Nota:** dopo la chiusura il task è ancora nel Data Index (storico), ma la console
> lo nasconde nel filtro di default perché non è più azionabile dall'operatore.

---

## Cos'è il "Claim"

**Claim** = "prendo in carico questo task".

È il gesto con cui un operatore dice al sistema:
> "Questo task è mio, lo sto lavorando io"

### Prima del claim
```
Task: Lavorazione Pratica
Stato: Ready
Owner: nessuno
Visibile a: tutti gli operatori del gruppo GRUPPO_OPERATORE_ANC
```

### Dopo il claim
```
Task: Lavorazione Pratica
Stato: Reserved
Owner: op.rossi
Visibile a: solo op.rossi (è suo)
```

### Analogia business

È come una **coda di sportello**:
- la pratica arriva in coda (**Ready** = "disponibile")
- l'operatore la prende dallo sportello (**Claim** = "la prendo io")
- ora è bloccata per quell'operatore (**Reserved** = "in lavorazione")
- nessun altro operatore può prenderla mentre è riservata

### Nella POC

| Azione UI Scrivania | Equivale a | Stato Kogito |
|---|---|---|
| Pratica arriva | — | Ready |
| Operatore clicca **"Accetta"** | Claim | Reserved |
| Operatore clicca **"Chiudi pratica"** | Complete | Completed |

Il **claim** nella Scrivania ANC corrisponde al pulsante **"Accetta"** (`POST /api/v1/tasks/{id}/accept`).

---

## Note sulla KIE Task Console

La Task Console (`:8084`) mostra i task Kogito ma ha alcune limitazioni nella POC:

- **"Cannot show task form"** — normale: il BPMN non ha un form HTML associato al task.
  La UI operativa è la Scrivania ANC (porta 81), non la Task Console.
- **"Impersonate"** — senza Keycloak la console non sa chi sei. Per vedere i task
  di un operatore inserire: `User = op.rossi` / `Groups = GRUPPO_OPERATORE_ANC` → Apply.
- Il pulsante **Claim** appare dopo aver impostato l'impersonazione corretta.

---

## Task Console (`:8084`) — Problema di versione

### Versioni disponibili

| Console | Versione | Funziona senza Keycloak |
|---|---|---|
| Management Console | `10.2.0` (Apache KIE) | ✅ Impersonate disponibile |
| Task Console | `10.0.0` (Apache KIE) | ❌ Solo "About" nel menu utente |

La Task Console `10.0.0` è l'**unica versione disponibile** su Docker Hub per Apache KIE.
Non esiste una versione `10.2.0` della Task Console. Senza Keycloak non riesce a
identificare l'utente e mostra sempre "Anonymous" → **No results found** nella inbox.

### Soluzione per la POC

Usare la **Management Console** (`http://localhost:8083`) al posto della Task Console:

1. Aprire `http://localhost:8083`
2. Menu sinistro → **Tasks**
3. Espandere **Impersonate** in cima alla pagina
4. Inserire `User = op.rossi` / `Groups = GRUPPO_OPERATORE_ANC`
5. Cliccare **Apply**
6. I task del gruppo diventano visibili con pulsante **Claim**

### Per la produzione

In produzione con **Keycloak** configurato, la Task Console funziona nativamente:
l'operatore si autentica e vede solo i propri task senza impersonazione manuale.

---

## Mappa degli stati: Pratica, Task e Kogito Data Index

| Fase | `practice.stato` | `task.stato` | Kogito Data Index (`tasks.state`) |
|------|-----------------|--------------|-----------------------------------|
| Pratica ricevuta da BPM | `APERTA` | — | `Ready` |
| Operatore accetta il task | `IN_LAVORAZIONE` | `IN_CARICO` | `Reserved` |
| SD chiude (esito APPROVATA) | `CHIUSA_SD_OK` | `CHIUSA_SD_OK` | `Completed` |
| SD chiude (esito RESPINTA) | `CHIUSA_SD_KO` | `CHIUSA_SD_KO` | `Completed` |
| ACK BPM ricevuto — esito OK | `CHIUSA_EXT_OK` | `CHIUSA_EXT_OK` | `Completed` |
| ACK BPM ricevuto — esito KO | `CHIUSA_EXT_KO` | `CHIUSA_EXT_KO` | `Completed` |

**Legenda:**

- **`CHIUSA_SD_*`** — chiusura confermata lato Scrivania Digitale; il task Kogito viene
  completato (`complete`) in questo momento. Il processo BPM esterno riceverà l'esito via outbound.
- **`CHIUSA_EXT_*`** — aggiornamento del DB ANC dopo ricezione dell'ACK dal sistema BPM esterno.
  Il task Kogito è già `Completed` dal passo precedente.
- Il suffisso **`OK`** / **`KO`** riflette l'esito della checklist:
  outcome `APPROVATA` → `OK`; qualsiasi esito di rifiuto → `KO`.
- **Nota Kogito 10.2:** il lifecycle non prevede lo stato `InProgress`. Gli stati disponibili
  sono solo `Ready → Reserved → Completed`. La transizione `complete` è l'unica valida da `Reserved`.

---

## Cronologia pratica — Mappa degli eventi (audit_event)

Il tab **Cronologia** della pratica mostra una timeline unificata composta da due fonti:

- **`audit_event`** — eventi operativi generati automaticamente dai service Java ad ogni operazione di business
- **`practice_state_history`** — transizioni di stato, mostrate con `event_type = STATE_CHANGED`

Gli eventi sono **hardcoded** nei service: non sono configurabili senza modificare il codice.

### Tabella degli event_type

| Azione business | `event_type` | Service responsabile | Actor tipico |
|----------------|-------------|---------------------|-------------|
| Pratica ricevuta dal BPM | `PRACTICE_OPENED` | `BpmPracticeInboundService` | `BPM_SYSTEM` |
| Allegato ricevuto dal BPM | `ATTACHMENT_INGESTED` | `BpmPracticeInboundService` | `BPM_SYSTEM` |
| Operatore prende in carico il task | `TASK_ACCEPTED` | `TaskManagementService` | operatore |
| Supervisore riassegna il task | `TASK_REASSIGNED` | `SupervisionTaskService` | supervisore |
| Tipizzazione del documento | `DOCUMENT_TYPED` | `IntakeTypingService` | operatore |
| Salvataggio checklist durante la lavorazione | `CHECKLIST_SAVED` | `IntakeChecklistService` | operatore |
| Riapertura checklist dopo salvataggio | `CHECKLIST_REOPENED` | `IntakeChecklistService` | operatore |
| Visualizzazione aiuto checklist | `CHECKLIST_HELP_VIEWED` | `IntakeChecklistHelpService` | operatore |
| Chiusura pratica (esito inviato a BPM e confermato) | `PRACTICE_FINALIZED` | `IntakePracticeCloseService` | operatore |
| Chiusura pratica (BPM non disponibile — attesa ACK) | `PRACTICE_CLOSE_REQUESTED` | `IntakePracticeCloseService` | operatore |
| Ricezione ACK dal sistema BPM esterno | `BPM_OUTCOME_ACK_RECEIVED` | `BpmOutcomeAckService` | `bpm-stub` |
| Creazione segnalazione | `SIGNAL_CREATED` | `SignalService` | operatore/supervisore |
| Riassegnazione segnalazione | `SIGNAL_REASSIGNED` | `SignalService` | supervisore |
| Presa in carico segnalazione | `SIGNAL_TAKEN` | `SignalService` | operatore |
| Inoltro segnalazione a Sinergia | `SIGNAL_FORWARDED_SINERGIA` | `SignalService` | operatore/supervisore |
| Aggiunta pratica ai preferiti | `FAVORITE_CREATED` | `FavoriteService` | operatore |
| Modifica preferito | `FAVORITE_UPDATED` | `FavoriteService` | operatore |
| Rimozione pratica dai preferiti | `FAVORITE_DELETED` | `FavoriteService` | operatore |
| Transizione di stato (generata da `practice_state_history`) | `STATE_CHANGED` | — (query unificata) | vario |

### Note

- Gli eventi `SIGNAL_*`, `FAVORITE_*` e `STATE_CHANGED` appaiono nella cronologia della **pratica associata**, non in una cronologia separata per segnalazioni o preferiti.
- Il `correlationId` permette di collegare un evento al contesto che lo ha generato (es. `CLOSE_76_1748720658977`, `SIGNAL_CREATE_3`).
- `STATE_CHANGED` non corrisponde a una riga in `audit_event` ma viene iniettato nella timeline dalla query di `getPracticeHistory()` leggendo `practice_state_history`. La nota mostra la transizione nel formato `FROM_STATE -> TO_STATE | descrizione`.
