---
app: "Attivazione Nuova Carta"
prefix: "BOA_ANC"
layer: "bpm"
export_version: "20260506"
analyzed: "2026-05-20"
output_dir: "docs/reverse/attivazione-nuova-carta/v20260506/"
---

# Attivazione Nuova Carta — Reverse Engineering BPM

> **Documento**: Orchestrazione BPM runtime — layer `bpm`  
> **Versione export**: 20260506  
> **Analisi**: 2026-05-20  
> **Scope**: sequenze di esecuzione, dipendenze tra processi, assignment human task, asincronie,
> batch scheduling, state transitions BPM

---

## STEP 0 — Mappa Processi

### Tabella Process Model

| Nome Processo | UUID | ID Num. | Tipologia | Trigger | Processo Padre / Caller |
|---|---|---|---|---|---|
| BOA_ANC_ProcessoWebApi_CreaPratica | 0002e9f3-3d79-8000-5a84-7f0000014e7a | 74641 | Entry Point | WebApi REST POST | BOA_ANC_WebApi_CreaPratica |
| BOA_ANC_ElaborazioneJsonInput_CreazionePratica | 0002e9f3-4e47-8000-5a89-7f0000014e7a | 74645 | Subprocess | Called-by-Process (ASYNC) | BOA_ANC_ProcessoWebApi_CreaPratica |
| BOA_ANC_AvvioAttivita | 0002e9f3-c0fa-8000-5b1e-7f0000014e7a | 74719 | Subprocess | Called-by-Process (ASYNC) | BOA_ANC_ElaborazioneJsonInput_CreazionePratica |
| BOA_ANC_DownloadContenuti_CreazionePratica | 0002e9f3-ab47-8000-5af7-7f0000014e7a | 74691 | Subprocess | Called-by-Process (SYNC) | BOA_ANC_ElaborazioneJsonInput_CreazionePratica |
| BOA_ANC_ScaricaSingoloDoc | 0002e9f3-a766-8000-5af1-7f0000014e7a | 74688 | Subprocess | Called-by-Process (SYNC/loop) | BOA_ANC_DownloadContenuti_CreazionePratica |
| BOA_ANC_ScriviAudit | 0002e9f3-5531-8000-5a98-7f0000014e7a | 74652 | Utility | Called-by-Process | Multipli |
| BOA_ANC_Processo_CambioStato | 0002e9f4-c789-8000-5b72-7f0000014e7a | 74753 | Subprocess | Called-by-Process (SYNC) | BOA_ANC_AvvioAttivita, BOA_ANC_Processo_TipizzaDoc |
| BOA_ANC_inizializzaUtenteServizio | 0002e9f4-de54-8000-5b8d-7f0000014e7a | 74764 | Utility | Called-by-Process | — |
| BOA_ANC_SalvaFiltriUtente | 0006e9f6-5217-8000-5c48-7f0000014e7a | 74850 | Entry Point | UI Site Operatore | Scrivania Digitale Operatore ANC |
| BOA_ANC_SalvataggioDati | 0002e9fa-4ae6-8000-5d18-7f0000014e7a | 74937 | Subprocess | Called-by-Process (SYNC) | BOA_ANC_AvvioAttivita (da azioni UI form) |
| BOA_ANC_Batch_ScodamentoCallback | 0002e9fa-5b2a-8000-5d3d-7f0000014e7a | 74953 | Batch | Scheduled ogni 10 min | — |
| BOA_ANC_ScodamentoSingoloEsito | 0002e9fa-5f0b-8000-5d4b-7f0000014e7a | 74962 | Subprocess | Called-by-Process (ASYNC/loop) | BOA_ANC_Batch_ScodamentoCallback |
| BOA_ANC_ChiamaWS_BPM_InvioCallback | 0002e9fa-610c-8000-5d5b-7f0000014e7a | 74966 | Subprocess | Called-by-Process (SYNC) | BOA_ANC_ScodamentoSingoloEsito |
| BOA_ANC_Processo_ScritturaTabellaDebugWS | 0007e9fa-6393-8000-5d66-7f0000014e7a | 74970 | Utility | Called-by-Process | BOA_ANC_ChiamaWS_BPM_InvioCallback (debug) |
| BOA_ANC_Single_Riassegnazione | 0002e9fb-7223-8000-5df6-7f0000014e7a | 75032 | Subprocess | Called-by-Process (SYNC/loop) | BOA_ANC_ProcessoSupervisore_RiassegnazioneTask |
| BOA_ANC_ProcessoSupervisore_RiassegnazioneTask | 0002e9fb-7760-8000-5e01-7f0000014e7a | 75039 | Entry Point | UI Site Supervisore | Scrivania Digitale Supervisore ANC |
| BOA_ANC_Inizializza_Sinergia_Operatore | 0005ea0e-e4c8-8000-61e9-7f0000014e7a | 75453 | Entry Point | UI Site Operatore | Scrivania Digitale Operatore ANC |
| BOA_ANC_Inizializza_Sinergia_Supervisore | 0005ea0e-e738-8000-61f4-7f0000014e7a | 75458 | Entry Point | UI Site Supervisore | Scrivania Digitale Supervisore ANC |
| BOA_ANC_Processo_TipizzaDoc | 0002eb1d-4057-8000-f320-7f0000014e7a | 89050 | Subprocess | Called-by-Process (SYNC) | BOA_ANC_AvvioAttivita (da azioni UI form) |
| BOA_ANC_InizializzaBoaCodifiche | 0002e9f2-1948-8000-5a40-7f0000014e7a | 74615 | Utility | Called-by-Process | — |

---

## STEP 1 — Entry Point e Trigger

### 1.1 WebApi REST

| Entry Point | Canale | Metodo | Processo Avviato | Input Principali | Attore / Caller |
|---|---|---|---|---|---|
| BOA_ANC_WebApi_CreaPratica | WebApi REST | POST `/boaanccreapratica` | BOA_ANC_ProcessoWebApi_CreaPratica | `http!request` (body JSON: idWorkItem, datiPratica, cliente, datiCarta, contenutiDenuncia, documenti) | Operatore WebApi ANC, BOA ANC All Users |

**Controllo accesso**: gruppi Appian `Operatore WebApi ANC` (771) e `BOA ANC All Users` (768); admin `BOA ANC Administrators` (769).  
**Logging HTTP**: disabilitato (`logging: false`).  
**Risposta sincrona**: il processo restituisce `pv!responseOut` (HttpResponse) — il WebApi riceve la risposta mentre l'elaborazione background prosegue in modo asincrono.

### 1.2 UI Sites

| Entry Point | Site | Processo Avviato | Input Principali | Attore |
|---|---|---|---|---|
| Worklist operatore | Scrivania Digitale Operatore ANC (ae0c311c) | BOA_ANC_AvvioAttivita (task in coda) | `idCase` | Operatore ANC (770) |
| Azione salva filtri | Scrivania Digitale Operatore ANC | BOA_ANC_SalvaFiltriUtente | filtri utente | Operatore ANC (770) |
| Azione init Sinergia | Scrivania Digitale Operatore ANC | BOA_ANC_Inizializza_Sinergia_Operatore | — | Operatore ANC (770) |
| Azione riassegnazione | Scrivania Digitale Supervisore ANC (d72fc841) | BOA_ANC_ProcessoSupervisore_RiassegnazioneTask | `inTasks` (Integer?list), `inUsersOrGroup` (UserOrGroup) | Supervisore ANC (772) |
| Azione init Sinergia | Scrivania Digitale Supervisore ANC | BOA_ANC_Inizializza_Sinergia_Supervisore | — | Supervisore ANC (772) |

### 1.3 Scheduled (Batch)

| Entry Point | Frequenza | Processo Avviato | Semaforo | Flag Debug |
|---|---|---|---|---|
| Scheduler Appian | ogni 10 minuti | BOA_ANC_Batch_ScodamentoCallback | `BOA_ANC_GestioneScodamento.inEsecuzione` | `BOA_ANC_DEBUG_ATTIVABATCHCALLBACK` |

---

## STEP 2 — Catene di Invocazione e Subprocess

### 2.1 Albero di Invocazione

```text
[Entry Point: WebApi REST POST /boaanccreapratica]
  BOA_ANC_WebApi_CreaPratica
    └── [SYNC]  BOA_ANC_ProcessoWebApi_CreaPratica
                ├── [XOR: JSON non valido]
                │     → responseOut con errore → scrivo response debug → End
                └── [XOR: JSON valido]
                      → scrivo BOA_Case (stato: "Raccolta Input")
                      → responseOut OK (idCase, resultCode=0)
                      → scrivo response debug
                      └── [ASYNC fire-and-forget]
                          BOA_ANC_ElaborazioneJsonInput_CreazionePratica
                              ├── creo cartella case
                              ├── salvo Pratica + Cliente + DatiCarta
                              │   + ContenutiDenuncia + Documento su DS
                              ├── [SYNC] BOA_ANC_DownloadContenuti_CreazionePratica
                              │          └── [SYNC/loop per ogni documento]
                              │              BOA_ANC_ScaricaSingoloDoc
                              └── [ASYNC] BOA_ANC_AvvioAttivita
                                          ├── Human Task "attivita"
                                          │   → Operatore ANC
                                          │   (attende completamento operatore)
                                          │
                                          │   Azioni da form del task:
                                          ├── [SYNC, azione UI] BOA_ANC_SalvataggioDati
                                          ├── [SYNC, azione UI] BOA_ANC_Processo_CambioStato
                                          ├── [SYNC, azione UI] BOA_ANC_Processo_TipizzaDoc
                                          │          └── [SYNC] BOA_ANC_Processo_CambioStato
                                          └── [SYNC, azione UI] BOA_ANC_ScriviAudit

[Batch: ogni 10 min]
  BOA_ANC_Batch_ScodamentoCallback
    ├── recupero gestione (semaforo)
    ├── [XOR: inEsecuzione=true] → End (terminazione anticipata)
    └── [XOR: inEsecuzione=false]
          → recupero lista esiti da inviare (statoInvio=0)
          └── [ASYNC/loop per ogni esito]
              BOA_ANC_ScodamentoSingoloEsito
                  └── [SYNC] BOA_ANC_ChiamaWS_BPM_InvioCallback
                              └── [Call Integration]
                                  BOA_ANC_Integrazione_BPM_InvioCallback
                                  → BOA_ANC_SistemaConnesso_BPM
                                    (REST OAuth2 → BPM Poste)

[Entry Point: UI Supervisore]
  BOA_ANC_ProcessoSupervisore_RiassegnazioneTask
    └── [SYNC/loop per ogni task in inTasks]
        BOA_ANC_Single_Riassegnazione

[Entry Point: UI Operatore]
  BOA_ANC_SalvaFiltriUtente              (self-contained)
  BOA_ANC_Inizializza_Sinergia_Operatore (self-contained)

[Entry Point: UI Supervisore]
  BOA_ANC_Inizializza_Sinergia_Supervisore (self-contained)

[Utility — nessun trigger autonomo]
  BOA_ANC_ScriviAudit
  BOA_ANC_InizializzaBoaCodifiche
  BOA_ANC_inizializzaUtenteServizio
  BOA_ANC_Processo_ScritturaTabellaDebugWS
```

### 2.2 Tabella Subprocess

| Processo Padre | Processo Figlio | Modalità | Input Passati | Output Ricevuti |
|---|---|---|---|---|
| BOA_ANC_WebApi_CreaPratica | BOA_ANC_ProcessoWebApi_CreaPratica | SYNC (Start Process, costante `BOA_ANC_PROCESSO_WEBAPI_CREAPRATICA`) | `http!request` come PV `input` | `responseOut` (HttpResponse) |
| BOA_ANC_ProcessoWebApi_CreaPratica | BOA_ANC_ElaborazioneJsonInput_CreazionePratica | **ASYNC** (Start Process fire-and-forget, `[Deprecated] Start Process`) | `idCase` (int), `json` (string) | nessuno |
| BOA_ANC_ElaborazioneJsonInput_CreazionePratica | BOA_ANC_DownloadContenuti_CreazionePratica | SYNC | dati documenti dal JSON | riferimenti Appian Document |
| BOA_ANC_DownloadContenuti_CreazionePratica | BOA_ANC_ScaricaSingoloDoc | SYNC/loop | riferimento singolo documento | documento Appian scaricato |
| BOA_ANC_ElaborazioneJsonInput_CreazionePratica | BOA_ANC_AvvioAttivita | **ASYNC** (Start Process) | `idCase` | nessuno |
| BOA_ANC_AvvioAttivita (azione UI) | BOA_ANC_SalvataggioDati | SYNC (da form action, costante `BOA_ANC_PROCESSO_SALVATAGGIODATI`) | `checkList`, `nota`, `pratica` | — |
| BOA_ANC_AvvioAttivita (azione UI) | BOA_ANC_Processo_CambioStato | SYNC (da form action, costante `BOA_ANC_PROCESSO_CAMBIOSTATO`) | `case`, `pratica` | — |
| BOA_ANC_AvvioAttivita (azione UI) | BOA_ANC_Processo_TipizzaDoc | SYNC (da form action, costante `BOA_ANC_PROCESSO_TIPIZZAZIONEDOC`) | `pratica`, `documento`, `tipoDocumento`, `checkList`, `case` | `ErrorOccurred`, `ErrorMessage` |
| BOA_ANC_Processo_TipizzaDoc | BOA_ANC_Processo_CambioStato | SYNC (SUB_PROC interno) | `case`, `pratica` | — |
| BOA_ANC_AvvioAttivita (azione UI) | BOA_ANC_ScaricaSingoloDoc | SYNC (da form action, costante `BOA_ANC_PROCESSO_SCARICASINGOLODOC`) | riferimento documento | documento Appian |
| BOA_ANC_Batch_ScodamentoCallback | BOA_ANC_ScodamentoSingoloEsito | **ASYNC/loop** (per ogni esito) | `esito` (BOA_ANC_ScodamentoStati) | — |
| BOA_ANC_ScodamentoSingoloEsito | BOA_ANC_ChiamaWS_BPM_InvioCallback | **SYNC** (SUB_PROC `isAsynchronous=false`) | `idCase` (= `pv!esito.idcase`) | `responseWS` (BOA_ANC_Response_BPM_InvioCallback) ← PV `response` del subprocess |
| BOA_ANC_ProcessoSupervisore_RiassegnazioneTask | BOA_ANC_Single_Riassegnazione | SYNC/loop | `taskId` (Integer), `inUsersOrGroup` (UserOrGroup) | — |

### 2.3 Flusso Dettagliato BOA_ANC_ProcessoWebApi_CreaPratica

```
Start
  → N2 "scrivo debugwsinput" [Write to DS → BOA_ANC_ENTITY_DEBUGWSINPUT]
       body JSON + timestamp + tipologia="CreaPratica" → pv!debug
  → N3 "estraggo json" [Unattended]
       pv!json = jsonGetValue(pv!input, "body")
  → N6 "controllo json" [Unattended]
       pv!mappaVerifiche = BOA_ANC_VerificaComplessiva_JsonCreazionePratica(json: pv!json)
  → N9 [XOR: mappaVerifiche.esito valido?]
      ├── NO → N12 "No, errore" [Unattended]
      │          pv!responseOut = BOA_ANC_InputWS_HttpResponse(resultCode, resultMessage, statusCode=200)
      │        → N14 "scrivo response" [Write DS → BOA_ANC_ENTITY_DEBUGWSINPUT]
      │        → End
      └── SI → N18 "scrivo case" [Write DS → BOA_Case entity]
                    stato = BOA_ANC_STATI_CASE[1] ("Raccolta Input")
                    tipologiaProcesso = BOA_ANC_NOMEAPPLICAZIONE
                    operatore = pp!initiator
                    → pv!case, pv!idCase
               → N21 "rimando ok" [Unattended]
                    pv!responseOut = BOA_ANC_InputWS_HttpResponse(
                        resultCode: 0,
                        resultMessage: "Pratica creata correttamente su Scrivania Digitale",
                        appianTicketId: pv!idCase,
                        statusCode: 200
                    )
                    pv!idWorkItem = jsonGetValue(pv!json, BOA_ANC_CHIAVIOBBLIGATORIE_ROOT_CREAZIONEPRATICA[2])
               → N14 "scrivo response" [Write DS → BOA_ANC_ENTITY_DEBUGWSINPUT]
                    aggiorna: sdResponse + timestampOutput + idWorkItem + idCase
               → N25 "avvio elaborazione" [Deprecated Start Process — ASYNC]
                    → BOA_ANC_ElaborazioneJsonInput_CreazionePratica
                       params: {idCase: pv!idCase, json: pv!json}
               → End
```

### 2.4 Flusso Dettagliato BOA_ANC_ElaborazioneJsonInput_CreazionePratica

```
Start
  → N2 "creo cartella case" [Create Folder — Appian KM]
       → pv!folderCase
  → Nodi di parsing JSON (Unattended) — sequenza:
       pv!pratica    = BOA_ANC_ElaborazioneJsonCreaPratica_Pratica(json, idCase)
       pv!cliente    = BOA_ANC_ElaborazioneJsonCreaPratica_Cliente(json, idCase)
       pv!datiCarta  = BOA_ANC_ElaborazioneJsonCreaPratica_DatiCarta(json, idCase)
       pv!contenutiDenuncia = BOA_ANC_ElaborazioneJsonCreaPratica_ContenutiDenuncia(json, idCase)
       pv!documento  = BOA_ANC_ElaborazioneJsonCreaPratica_Documento(json, idCase)
  → Write DS → BOA_ANC_ENTITY_PRATICA, ENTITY_CLIENTE, ENTITY_DATICARTA,
               ENTITY_CONTENUTIDENUNCIA, ENTITY_DOCUMENTO (insert)
  → [SYNC] BOA_ANC_DownloadContenuti_CreazionePratica
       (scarica i file allegati ai documenti dall'origine esterna)
       └── [SYNC/loop] BOA_ANC_ScaricaSingoloDoc (per ogni documento)
  → [ASYNC] Start Process → BOA_ANC_AvvioAttivita (idCase)
  → End
```

---

## STEP 3 — Human Task e Assignment

### 3.1 Tabella Human Task

| Processo | Nome Task | Assegnato a | Logica Assignment | Deadline | Escalation |
|---|---|---|---|---|---|
| BOA_ANC_AvvioAttivita | attivita (guiId=2) | Operatore ANC (gruppo 770) | Costante `BOA_ANC_GRUPPO_OPERATORI` → gruppo `Operatore ANC` (statica, "Assign To Expression") | Non configurata (enabled=false) | Nessuna |

### 3.2 Dettaglio Human Task "attivita"

- **Tipo nodo**: Human Task (icon id=21, UserInputTask)
- **Display nome task**: `BOA_ANC_DefinisciNomeTask_ANC(idCase: pv!idCase)` — nome dinamico per worklist
- **Assegnazione**: costante `BOA_ANC_GRUPPO_OPERATORI` (`_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9077824`) → gruppo `Operatore ANC` (ID 770)
  - Modalità: assegnazione a gruppo; il task compare nella worklist di tutti gli operatori del gruppo
- **Form**: interfaccia `BOA_ANC_Task_Lavorazione` (`_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9087111`)
- **Azioni UI disponibili nella form** (subprocess avviati da bottoni/azioni form):
  - Salvataggio dati → `BOA_ANC_SalvataggioDati` (via costante `BOA_ANC_PROCESSO_SALVATAGGIODATI`)
  - Cambio stato pratica → `BOA_ANC_Processo_CambioStato` (via costante `BOA_ANC_PROCESSO_CAMBIOSTATO`)
  - Tipizzazione documento → `BOA_ANC_Processo_TipizzaDoc` (via costante `BOA_ANC_PROCESSO_TIPIZZAZIONEDOC`)
  - Download documento → `BOA_ANC_ScaricaSingoloDoc` (via costante `BOA_ANC_PROCESSO_SCARICASINGOLODOC`)
- **allowsBack**: `false` — il task non può essere reindirizzato al nodo precedente
- **target-completion**: 5 giorni lavorativi (SLA di monitoraggio)
- **on-create-ignore-if-active**: `false` — crea una nuova istanza anche se esiste una pratica attiva

### 3.3 Riassegnazione da Supervisore

Il supervisore non interagisce con i human task tramite worklist propria, ma dispone di:

**BOA_ANC_ProcessoSupervisore_RiassegnazioneTask**
- Avviato da UI `Scrivania Digitale Supervisore ANC`
- Input: `inTasks` (Integer?list = lista ID task da riassegnare), `inUsersOrGroup` (UserOrGroup = destinatario)
- Loop sync su ogni task → `BOA_ANC_Single_Riassegnazione`
- Initiator: qualsiasi utente del gruppo `Supervisore ANC` (772)

---

## STEP 4 — Callback e Integrazione Asincrona

### 4.1 Pattern di Integrazione

L'integrazione con il sistema esterno BPM (Poste Italia) segue un **pattern outbound polling**:

- **Appian non riceve callback diretti**: non è presente un processo inbound WebApi/triggered nell'export analizzato
- **Appian interroga periodicamente** la propria tabella di esiti da inviare (`BOA_ANC_ScodamentoStati`)
- Per ogni esito in attesa (`statoInvio=0`), Appian invia una notifica HTTP al sistema BPM esterno

### 4.2 Tabella Integrazione

| Processo Chiamante | Sistema Esterno | Tipo | Meccanismo Risposta | Processo Gestore Risposta |
|---|---|---|---|---|
| BOA_ANC_ChiamaWS_BPM_InvioCallback | BOA_ANC_SistemaConnesso_BPM | REST HTTP | Sincrono (response HTTP nella stessa chiamata) → elaborazione con `BOA_ANC_ElaboraResponse_BPM_InvioCallback` | BOA_ANC_ScodamentoSingoloEsito (elabora `pv!responseWS`) |

### 4.3 Dettaglio Connected System

| Attributo | Valore |
|---|---|
| Nome | BOA_ANC_SistemaConnesso_BPM |
| UUID | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9109283` |
| Tipo | HTTP |
| Base URL | `https://bpm-attivazione-carte-eap-bpm-attivazione-carte.apps.gen3gppito.cloudcoll.poste.it` |
| Auth | OAuth 2.0 Client Credentials |
| Token URL | `https://login.microsoftonline.com/08b638e8-0676-45cc-b677-5c038b17d28a/oauth2/v2.0/token` |
| Scope | `24de986e-2c18-4207-8e3f-72f288595a7e/.default` |
| Tenant Azure AD | `08b638e8-0676-45cc-b677-5c038b17d28a` |
| Relative Path | costante `BOA_ANC_RELATIVEPATH_SCODAMENTOSTATI` (`_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9109290`) |

### 4.4 Flusso BOA_ANC_ChiamaWS_BPM_InvioCallback

```
Input: idCase (int)

  → N2 "Call Integration" [local-id=internal3.integration]
       Integration: BOA_ANC_Integrazione_BPM_InvioCallback (da95cbf8)
       Parametri:
         - relativePath: BOA_ANC_RELATIVEPATH_SCODAMENTOSTATI
         - request:      BOA_ANC_GeneraRequest_BPM_InvioCallback(idCase: pv!idCase)
         - Connected System: BOA_ANC_SistemaConnesso_BPM
       Output:
         - pv!Success (boolean)
         - pv!Result  (HttpResponse)
         - pv!Error   (IntegrationError)

  → Elaborazione risposta [Unattended]
       pv!responseJSON = (estrazione body da pv!Result)
       pv!response     = BOA_ANC_ElaboraResponse_BPM_InvioCallback(responseJSON)

  → [XOR] BOA_ANC_Decisione_CodiceEsitoCallback(response)
       → aggiornamento stato invio su BOA_ANC_ScodamentoStati
       → (opz.) BOA_ANC_Processo_ScritturaTabellaDebugWS (debug)

  Output al chiamante: pv!response (BOA_ANC_Response_BPM_InvioCallback)
```

### 4.5 Stati Invio Scodamento (`BOA_ANC_STATIINVIO`)

| Codice | Significato |
|---|---|
| 0 | In attesa di invio |
| 1 | Inviato correttamente |
| 2 | Non inviato — errore |
| 3 | Scartato (superato numero massimo di retry) |

---

## STEP 5 — Batch e Scheduling

### 5.1 Tabella Batch

| Processo Batch | Frequenza | Trigger | Cosa Elabora | Subprocess Invocato | Gestione Errori |
|---|---|---|---|---|---|
| BOA_ANC_Batch_ScodamentoCallback | ogni 10 minuti | Scheduler Appian | Esiti `BOA_ANC_ScodamentoStati` con `statoInvio=0` (in attesa) | BOA_ANC_ScodamentoSingoloEsito (ASYNC/loop per ogni esito) | Semaforo `inEsecuzione`; stati 2=errore, 3=scartato sul record esito dopo max retry |

### 5.2 Flusso Dettagliato BOA_ANC_Batch_ScodamentoCallback

```
Start
  → N2 "recupero gestione" [Unattended]
       pv!gestione = BOA_ANC_GetGestioneScodamento()
       (legge il record BOA_ANC_GestioneScodamento — semaforo di esecuzione)

  → N3 "già in esecuzione?" [XOR — core.4]
       Condizione: pv!gestione.inEsecuzione = true
         ├── true  → N6 "Sì, esco" [End — terminazione anticipata]
         └── false → N8 [continua]

  → N8 "recupero esiti" [Unattended]
       pv!esiti = BOA_ANC_GetEsitiToSend()
       (query su BOA_ANC_ScodamentoStati dove statoInvio=0)
       + verifica flag BOA_ANC_DEBUG_ATTIVABATCHCALLBACK:
         - se true: modalità test (nessun invio reale al sistema BPM)

  → [loop ASYNC su ogni elemento di pv!esiti]
       Avvia BOA_ANC_ScodamentoSingoloEsito
         params: {esito: pv!esiti[i]}

  → End
```

**Note operative:**
- **Semaforo anti-concorrenza**: la tabella `BOA_ANC_GestioneScodamento` (entity) con campo `inEsecuzione` (boolean) impedisce esecuzioni parallele del batch. Il batch imposta `inEsecuzione=true` all'avvio e `false` al completamento (o errore).
- **Flag debug**: `BOA_ANC_DEBUG_ATTIVABATCHCALLBACK` — quando `true`, il batch non invia realmente al BPM esterno; utile per test in ambienti non-produzione.
- **Loop ASYNC**: ogni esito è elaborato in modo indipendente (istanza separata di `BOA_ANC_ScodamentoSingoloEsito`); le istanze possono procedere in parallelo.
- **Cleanup**: archive 7 giorni, delete 1 giorno.

### 5.3 Flusso BOA_ANC_ScodamentoSingoloEsito

```
Input: esito (BOA_ANC_ScodamentoStati)

  → [lettura dati pratica/case]
       recupero pratica dal DB tramite pv!esito.idcase

  → N2 "chiamo WS" [SUB_PROC, isAsynchronous=false — SINCRONO]
       → BOA_ANC_ChiamaWS_BPM_InvioCallback
          input:  idCase = pv!esito.idcase
          output: pv!responseWS (BOA_ANC_Response_BPM_InvioCallback)

  → [elaborazione responseWS]
       aggiorna BOA_ANC_ScodamentoStati:
         statoInvio = 1 (ok) / 2 (errore) / 3 (scartato)
       (opz.) aggiornamento stato pratica
       (opz.) BOA_ANC_ScriviAudit

  → End
```

---

## STEP 6 — State Transition BPM

### 6.1 Macchina a Stati — BOA_Case (`BOA_ANC_STATI_CASE`)

| Indice | Valore | Processo che imposta lo stato | Contesto |
|---|---|---|---|
| [1] | "Raccolta Input" | BOA_ANC_ProcessoWebApi_CreaPratica (N18 "scrivo case") | Stato iniziale — case creato dalla WebApi |
| [2] | "Lavorazione" | BOA_ANC_Processo_CambioStato (invocato da azione UI dell'operatore) | Operatore prende in carico la pratica |
| [3] | "Chiusura Pratica" | BOA_ANC_Processo_CambioStato (invocato da azione UI di chiusura) | Operatore chiude la pratica |

### 6.2 Macchina a Stati — BOA_ANC_Pratica (`BOA_ANC_STATI_PRATICA`)

| Indice | Valore | Processo che imposta lo stato | Contesto |
|---|---|---|---|
| [1] | "Aperta" | BOA_ANC_ElaborazioneJsonInput_CreazionePratica (insert Pratica) | Stato iniziale pratica |
| [2] | "In Lavorazione" | BOA_ANC_Processo_CambioStato (da azione UI operatore) | Operatore apre e lavora la pratica |
| [3] | "In Attesa Conferma BPM" | BOA_ANC_Processo_CambioStato (da azione UI di invio esito) | Pratica inviata a BPM, in attesa di risposta |
| [4] | "Chiusa OK" | BOA_ANC_Processo_CambioStato (da BOA_ANC_ScodamentoSingoloEsito dopo risposta BPM positiva) | Pratica chiusa con esito positivo |
| [5] | "Chiusa KO" | BOA_ANC_Processo_CambioStato (da BOA_ANC_ScodamentoSingoloEsito dopo risposta BPM negativa) | Pratica chiusa con esito negativo |

### 6.3 Tabella State Transition

| Processo | Nodo / Azione | Stato Da | Stato A | Condizione Guard | Storico |
|---|---|---|---|---|---|
| BOA_ANC_ProcessoWebApi_CreaPratica | N18 "scrivo case" | — (nuovo) | BOA_ANC_STATI_CASE[1] "Raccolta Input" | JSON input valido (`mappaVerifiche` ok) | BOA_ANC_ENTITY_PRATICA |
| BOA_ANC_ElaborazioneJsonInput_CreazionePratica | insert Pratica | — (nuovo) | BOA_ANC_STATI_PRATICA[1] "Aperta" | Sempre (post-parsing JSON) | BOA_ANC_ENTITY_PRATICA |
| BOA_ANC_Processo_CambioStato | "La scrivo" + update Pratica | *(stato corrente da pv!pratica)* | *(nuovo stato passato come input)* | Dipende dal chiamante (azione form operatore/supervisore) | BOA_ANC_ENTITY_STATIPRATICA, BOA_ANC_ENTITY_CASENOTE |
| BOA_ANC_Processo_TipizzaDoc | "cambia lo stato" (interno) | BOA_ANC_STATI_PRATICA[1] o [2] | *(determinato dal tipo documento tipizzato)* | `pv!tipoDocumento` presente e valido | BOA_ANC_ENTITY_STATIPRATICA |
| BOA_ANC_ScodamentoSingoloEsito | aggiornamento post-WS | BOA_ANC_STATIINVIO[0] "in attesa" | BOA_ANC_STATIINVIO[1/2/3] | esito response BPM | BOA_ANC_ScodamentoStati |

### 6.4 Registro Storico Stati

| Entity | UUID | Scopo |
|---|---|---|
| BOA_ANC_ENTITY_STATIPRATICA | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9074553` | Storico transizioni di stato della pratica con timestamp |
| BOA_ANC_ENTITY_CASENOTE | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9107926` | Note associate al case (scritte da `BOA_ANC_SalvataggioDati` e `BOA_ANC_Processo_CambioStato`) |
| BOA_ANC_ENTITY_AUDIT | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9074584` | Log audit generale (scritto da `BOA_ANC_ScriviAudit`) |
| BOA_ANC_ENTITY_DEBUGWSINPUT | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9073796` | Log delle chiamate WebApi in ingresso (input + response + timing) |

---

## STEP 7 — Escalation e Deadline

### 7.1 Tabella Deadline / Escalation

| Processo | Nodo | Tipo | Configurazione | Azione |
|---|---|---|---|---|
| BOA_ANC_ProcessoWebApi_CreaPratica | PM | Deadline PM | `enabled=false` | — |
| BOA_ANC_ElaborazioneJsonInput_CreazionePratica | PM | Deadline PM | `enabled=false` | — |
| BOA_ANC_AvvioAttivita | PM | Deadline PM | `enabled=false` | — |
| BOA_ANC_AvvioAttivita | task "attivita" (guiId=2) | Deadline task | `enabled=false` | — |
| BOA_ANC_AvvioAttivita | task "attivita" (guiId=2) | SLA monitoraggio | `target-completion=5.0 giorni`, `target-lag=1.0` | Solo monitoraggio SLA (no escalation configurata) |
| BOA_ANC_Batch_ScodamentoCallback | PM | Deadline PM | `enabled=false` | Semaforo `inEsecuzione` previene concorrenza |
| BOA_ANC_Processo_CambioStato | PM | Deadline PM | `enabled=false` | — |
| BOA_ANC_ProcessoSupervisore_RiassegnazioneTask | PM | Deadline PM | `enabled=false` | — |
| Tutti i PM | PM | Cleanup | archive 7 gg, delete 1 gg | Rimozione automatica istanze completate |

### 7.2 Note

- **Nessuna deadline BPM attiva** (`enabled=true`) è configurata in alcun processo nell'export analizzato.
- Il **SLA operativo** di 5 giorni lavorativi per il task "attivita" è solo un target di monitoraggio, non una deadline che scatena azioni automatiche.
- La **prevenzione esecuzioni concorrenti** del batch è gestita tramite il semaforo applicativo `BOA_ANC_GestioneScodamento.inEsecuzione`, non tramite deadline Appian.
- Il **retry per scodamento** (max retry → stato 3 "Scartato") è gestito applicativamente dalla logica in `BOA_ANC_ScodamentoSingoloEsito` / `BOA_ANC_ChiamaWS_BPM_InvioCallback`, non tramite escalation Appian.

---

## STEP 8 — Dipendenze Processuali e Mappa Finale

### 8.1 Mappa Dipendenze Complessiva

```text
══════════════════════════════════════════════════════════════════
 CANALE: WebApi REST POST /boaanccreapratica
══════════════════════════════════════════════════════════════════
  BOA_ANC_WebApi_CreaPratica
    └── [SYNC]  BOA_ANC_ProcessoWebApi_CreaPratica
                ├── [XOR KO] → response errore → End
                └── [ASYNC] BOA_ANC_ElaborazioneJsonInput_CreazionePratica
                            ├── [SYNC]  BOA_ANC_DownloadContenuti_CreazionePratica
                            │          └── [SYNC/loop] BOA_ANC_ScaricaSingoloDoc
                            └── [ASYNC] BOA_ANC_AvvioAttivita
                                        │  (Human Task → Operatore ANC)
                                        ├── [SYNC, azione UI] BOA_ANC_SalvataggioDati
                                        ├── [SYNC, azione UI] BOA_ANC_Processo_CambioStato
                                        ├── [SYNC, azione UI] BOA_ANC_Processo_TipizzaDoc
                                        │          └── [SYNC] BOA_ANC_Processo_CambioStato
                                        └── [SYNC, azione UI] BOA_ANC_ScriviAudit

══════════════════════════════════════════════════════════════════
 CANALE: Batch Scheduler — ogni 10 minuti
══════════════════════════════════════════════════════════════════
  BOA_ANC_Batch_ScodamentoCallback
    ├── [semaforo: inEsecuzione=true] → End anticipato
    └── [ASYNC/loop] BOA_ANC_ScodamentoSingoloEsito
                     └── [SYNC] BOA_ANC_ChiamaWS_BPM_InvioCallback
                                 └── [Integration]
                                     BOA_ANC_Integrazione_BPM_InvioCallback
                                     ──→ BOA_ANC_SistemaConnesso_BPM
                                         (REST OAuth2 Client Credentials)
                                         ──→ BPM Poste Italia (Openshift)

══════════════════════════════════════════════════════════════════
 CANALE: UI Supervisore ANC
══════════════════════════════════════════════════════════════════
  BOA_ANC_ProcessoSupervisore_RiassegnazioneTask
    └── [SYNC/loop] BOA_ANC_Single_Riassegnazione

  BOA_ANC_Inizializza_Sinergia_Supervisore   (self-contained)

══════════════════════════════════════════════════════════════════
 CANALE: UI Operatore ANC
══════════════════════════════════════════════════════════════════
  BOA_ANC_SalvaFiltriUtente                  (self-contained)
  BOA_ANC_Inizializza_Sinergia_Operatore     (self-contained)

══════════════════════════════════════════════════════════════════
 UTILITY — nessun trigger autonomo
══════════════════════════════════════════════════════════════════
  BOA_ANC_ScriviAudit
  BOA_ANC_InizializzaBoaCodifiche
  BOA_ANC_inizializzaUtenteServizio
  BOA_ANC_Processo_ScritturaTabellaDebugWS
```

### 8.2 Tabella Dipendenze Processuali

| Processo | Dipende da | Dipendenza | Tipo Chiamata |
|---|---|---|---|
| BOA_ANC_ProcessoWebApi_CreaPratica | BOA_ANC_WebApi_CreaPratica | WebApi avvia il processo con `http!request` come input | Start Process (costante `BOA_ANC_PROCESSO_WEBAPI_CREAPRATICA`) |
| BOA_ANC_ElaborazioneJsonInput_CreazionePratica | BOA_ANC_ProcessoWebApi_CreaPratica | riceve `idCase` + `json` | `[Deprecated] Start Process` ASYNC |
| BOA_ANC_DownloadContenuti_CreazionePratica | BOA_ANC_ElaborazioneJsonInput_CreazionePratica | dati documenti dal JSON | SUB_PROC SYNC |
| BOA_ANC_ScaricaSingoloDoc | BOA_ANC_DownloadContenuti_CreazionePratica | riferimento singolo documento | SUB_PROC SYNC/loop |
| BOA_ANC_AvvioAttivita | BOA_ANC_ElaborazioneJsonInput_CreazionePratica | `idCase` | Start Process ASYNC |
| BOA_ANC_SalvataggioDati | BOA_ANC_AvvioAttivita (form action) | `checkList`, `nota`, `pratica` | Start Process SYNC (da costante `BOA_ANC_PROCESSO_SALVATAGGIODATI`) |
| BOA_ANC_Processo_CambioStato | BOA_ANC_AvvioAttivita (form action) | `case`, `pratica` | Start Process SYNC (da costante `BOA_ANC_PROCESSO_CAMBIOSTATO`) |
| BOA_ANC_Processo_CambioStato | BOA_ANC_Processo_TipizzaDoc | `case`, `pratica` | SUB_PROC SYNC |
| BOA_ANC_Processo_TipizzaDoc | BOA_ANC_AvvioAttivita (form action) | `pratica`, `documento`, `tipoDocumento`, `checkList`, `case` | Start Process SYNC (da costante `BOA_ANC_PROCESSO_TIPIZZAZIONEDOC`) |
| BOA_ANC_ScaricaSingoloDoc | BOA_ANC_AvvioAttivita (form action) | riferimento documento | Start Process SYNC (da costante `BOA_ANC_PROCESSO_SCARICASINGOLODOC`) |
| BOA_ANC_ScriviAudit | Multipli (azioni UI, subprocess) | dati audit | Start Process SYNC |
| BOA_ANC_ScodamentoSingoloEsito | BOA_ANC_Batch_ScodamentoCallback | `esito` (BOA_ANC_ScodamentoStati) | Start Process ASYNC/loop |
| BOA_ANC_ChiamaWS_BPM_InvioCallback | BOA_ANC_ScodamentoSingoloEsito | `idCase` | SUB_PROC SYNC (`isAsynchronous=false`) |
| BOA_ANC_Integrazione_BPM_InvioCallback | BOA_ANC_ChiamaWS_BPM_InvioCallback | payload request generato da `BOA_ANC_GeneraRequest_BPM_InvioCallback` | Call Integration |
| BOA_ANC_SistemaConnesso_BPM | BOA_ANC_Integrazione_BPM_InvioCallback | HTTP REST con OAuth2 | Connected System |
| BOA_ANC_Single_Riassegnazione | BOA_ANC_ProcessoSupervisore_RiassegnazioneTask | `taskId` (Integer), `inUsersOrGroup` (UserOrGroup) | SUB_PROC SYNC/loop |

---

## Appendice — Costanti di Riferimento

| Costante | UUID | Valore / Scopo |
|---|---|---|
| BOA_ANC_STATI_CASE | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9074409` | ["Raccolta Input", "Lavorazione", "Chiusura Pratica"] |
| BOA_ANC_STATI_PRATICA | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9074464` | ["Aperta", "In Lavorazione", "In Attesa Conferma BPM", "Chiusa OK", "Chiusa KO"] |
| BOA_ANC_STATIINVIO | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9109016` | [0=in attesa, 1=inviato, 2=errore, 3=scartato] |
| BOA_ANC_GRUPPO_OPERATORI | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9077824` | Gruppo "Operatore ANC" (ID 770) |
| BOA_ANC_GRUPPO_SUPERVISORE | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9096975` | Gruppo "Supervisore ANC" (ID 772) |
| BOA_ANC_GRUPPO_OPERATOREWEBAPI | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9082312` | Gruppo "Operatore WebApi ANC" (ID 771) |
| BOA_ANC_NOMEAPPLICAZIONE | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9071147` | Nome applicazione per `tipologiaProcesso` |
| BOA_ANC_PROCESSO_WEBAPI_CREAPRATICA | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9074201` | UUID PM BOA_ANC_ProcessoWebApi_CreaPratica |
| BOA_ANC_PROCESSO_CAMBIOSTATO | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9086106` | UUID PM BOA_ANC_Processo_CambioStato |
| BOA_ANC_PROCESSO_SALVATAGGIODATI | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9108645` | UUID PM BOA_ANC_SalvataggioDati |
| BOA_ANC_PROCESSO_SCARICASINGOLODOC | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9095576` | UUID PM BOA_ANC_ScaricaSingoloDoc |
| BOA_ANC_PROCESSO_TIPIZZAZIONEDOC | `_a-0000ead1-5a62-8000-9c41-011c48011c48_10799895` | UUID PM BOA_ANC_Processo_TipizzaDoc |
| BOA_ANC_RELATIVEPATH_SCODAMENTOSTATI | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9109290` | Relative path endpoint BPM per invio callback |
| BOA_ANC_GeneraRequest_BPM_InvioCallback | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9109317` | Expression rule: genera payload request per BPM |
| BOA_ANC_ElaboraResponse_BPM_InvioCallback | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9109335` | Expression rule: elabora response JSON da BPM |
| BOA_ANC_Decisione_CodiceEsitoCallback | `c09755ed-e177-4489-bd13-5a8ad488d672` | Decision: determina stato invio da codice esito |
| BOA_ANC_DefinisciNomeTask_ANC | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9142372` | Expression rule: nome dinamico human task in worklist |
| BOA_ANC_DEBUG_ATTIVABATCHCALLBACK | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9156854` | Boolean flag: abilita/disabilita invio reale nel batch |
| BOA_ANC_GetGestioneScodamento | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9108964` | Expression rule: recupera record semaforo batch |
| BOA_ANC_Integrazione_BPM_InvioCallback | `da95cbf8-6680-45ce-b50c-fbca5136b757` | Integration object per chiamata al BPM esterno |

---

*Fine documento — Attivazione Nuova Carta BPM Reverse Engineering v20260506*
