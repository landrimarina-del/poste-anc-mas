---
app: "Attivazione Nuova Carta"
prefix: "BOA_ANC"
layer: "functional"
export_version: "20260506"
analyzed: "2026-05-20"
output_dir: "docs/reverse/attivazione-nuova-carta/v20260506/"
---

# Attivazione Nuova Carta — Documento Funzionale

> Reverse engineering funzionale dell'applicazione Appian **Attivazione Nuova Carta** (`BOA_ANC`).  
> Export version: `20260506` · Appian: `25.2.1285.0` · Data analisi: `2026-05-20`

---

## STEP 0 — Ricognizione struttura export

### Identificazione applicazione

| Attributo | Valore |
|---|---|
| Nome applicazione | Attivazione Nuova Carta |
| Prefisso | BOA_ANC |
| UUID applicazione | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9070895` |
| URL Identifier | `gGsQzg` |
| Versione Appian | 25.2.1285.0 |
| Data export | 2026-05-06 T15:19:00 UTC |
| Pubblicata | No (draft) |
| Pubblica | No |

### Struttura directory export

| Directory | Contenuto |
|---|---|
| `application/` | Manifest applicazione, mappa oggetti associati |
| `connectedSystem/` | Sistema connesso BPM esterno (1 elemento) |
| `content/` | Rules, costanti, interfacce, integrazioni (~130 oggetti) |
| `dataStore/` | Configurazione datastore JPA (2 datastore) |
| `datatype/` | Datatype business (18 tipi) |
| `group/` | Gruppi Appian (5 gruppi) |
| `META-INF/` | MANIFEST.MF, export.log, plugins.txt |
| `processModel/` | Process model BPM (20 modelli) |
| `processModelFolder/` | Cartelle organizzative |
| `recordType/` | Record Type Pratica_ANC (1 elemento) |
| `site/` | Siti applicativi (2 siti) |
| `webApi/` | API REST esposte (1 WebAPI) |

### Datatype business principali

`BOA_ANC_Pratica` · `BOA_ANC_Cliente` · `BOA_ANC_DatiCarta` · `BOA_ANC_ContenutiDenuncia` · `BOA_ANC_Documento` · `BOA_ANC_TipiDocumento` · `BOA_ANC_CaseChecklist` · `BOA_ANC_RefChecklist` · `BOA_ANC_CaseNote` · `BOA_ANC_Audit` · `BOA_ANC_StatiPratica` · `BOA_ANC_FiltriUtente` · `BOA_ANC_GestioneScodamento` · `BOA_ANC_ScodamentoStati` · `BOA_ANC_Response_BPM_InvioCallback` · `BOA_ANC_Debug_WebServices` · `BOA_ANC_Debug_WsInput` · `BOA_ANC_V_Pratica`

---

## STEP 1 — Ruoli e Gruppi Business

### Gerarchia gruppi

```
BOA ANC All Users  (_e-..._2234)
├── Operatore ANC            (_e-..._2238)
├── Operatore WebApi ANC     (_e-..._2240)
└── Supervisore ANC          (_e-..._2242)

BOA ANC Administrators  (_e-..._2236)   ← ruolo tecnico / admin
```

- **BOA ANC All Users**: gruppo ombrello che aggrega tutti gli utenti business dell'applicazione. Membership policy: chiusa.
- **Operatore ANC**: operatore di back-office che lavora le pratiche dalla Scrivania Digitale.
- **Operatore WebApi ANC**: ruolo tecnico/sistemistico che chiama la WebAPI di creazione pratica da sistemi esterni (es. BPM mittente).
- **Supervisore ANC**: supervisore con visibilità completa sulle pratiche e capacità di riassegnare attività agli operatori.
- **BOA ANC Administrators**: gruppo amministrativo Appian, non è un ruolo business operativo.

### Tabella ruoli business

| Ruolo Business | Responsabilità Operativa | Aree Accessibili | Capability |
|---|---|---|---|
| Operatore ANC | Lavorazione pratica: verifica documenti, tipizzazione, checklist, chiusura | Scrivania Digitale Operatore ANC | Home dashboard, Lista attività, Pratiche, Lavorazione pratica |
| Supervisore ANC | Monitoraggio pratiche, riassegnazione attività, vista globale | Scrivania Digitale Supervisore ANC | Home dashboard supervisore, Pratiche, Riassegna Attività |
| Operatore WebApi ANC | Invio nuove pratiche dall'esterno tramite API | WebAPI REST `BOA_ANC_WebApi_CreaPratica` | Creazione pratica via POST |
| BOA ANC Administrators | Amministrazione applicazione, configurazione | Tutti i siti (administrator) | Admin owner di tutti i processModel e siti |

### Tabella siti e pagine

| Sito | Pagina | Attore abilitato | Funzione operativa |
|---|---|---|---|
| Scrivania Digitale Operatore ANC (`scrivania-digitale-operatore-anc`) | Home | Operatore ANC | Dashboard operativa con KPI e stato pratiche (`BOA_ANC_Interfaccia_DashBoard_Operatore`) |
| Scrivania Digitale Operatore ANC | Attività | Operatore ANC | Lista delle attività/task assegnati all'operatore (`BOA_ANC_ListaAttivita`) |
| Scrivania Digitale Operatore ANC | Pratiche | Operatore ANC | Vista record Pratiche_ANC con filtri e drill-down pratica |
| Scrivania Digitale Supervisore ANC (`scrivania-digitale-supervisore-anc`) | Home | Supervisore ANC | Dashboard supervisore con grafici produttività e KPI (`BOA_ANC_Interfaccia_DashBoard_Supervisore`) |
| Scrivania Digitale Supervisore ANC | Pratiche | Supervisore ANC | Vista record Pratiche_ANC (visibilità completa) |
| Scrivania Digitale Supervisore ANC | Riassegna Attività | Supervisore ANC | Interfaccia di riassegnazione task (`BOA_ANC_ProcessoSupervisore_RiassegnazioneTask`) |

---

## STEP 2 — Capability Applicative

### Tabella capability

| Capability | Attore | Trigger | Processo coinvolto | Risultato operativo |
|---|---|---|---|---|
| **Creazione Pratica** | Sistema esterno (BPM mittente) tramite `Operatore WebApi ANC` | Chiamata POST `/boaanccreapratica` | `BOA_ANC_ProcessoWebApi_CreaPratica` → `BOA_ANC_ElaborazioneJsonInput_CreazionePratica` | Pratica creata in stato **Aperta**, dati cliente/carta/denuncia memorizzati, documenti scaricati |
| **Download documenti** | Sistema (automatico post-creazione) | Avvio processo downstream da creazione | `BOA_ANC_DownloadContenuti_CreazionePratica` → `BOA_ANC_ScaricaSingoloDoc` | Documenti allegati scaricati da link remoti e archiviati nel document store Appian |
| **Visualizzazione lista attività** | Operatore ANC | Accesso alla pagina "Attività" | `BOA_ANC_ListaAttivita` + `BOA_ANC_FiltriUtente_ListaAttivita` | Lista task assegnati all'operatore con filtri personalizzabili salvati per sessione |
| **Avvio lavorazione pratica** | Operatore ANC | Presa in carico task | `BOA_ANC_AvvioAttivita` | Pratica passa in stato **In Lavorazione**, attività assegnata all'operatore |
| **Verifica documenti** | Operatore ANC | Apertura task di verifica | `BOA_ANC_Task_VerificaDocumenti` | Operatore verifica e visualizza dati pratica, cliente, carta, documenti allegati |
| **Tipizzazione documento** | Operatore ANC | Azione di tipizzazione su documento | `BOA_ANC_Processo_TipizzaDoc` | Documento tipizzato per codice, stato pratica aggiornato, checklist generata |
| **Lavorazione pratica** | Operatore ANC | Navigazione nel task di lavorazione | `BOA_ANC_Task_Lavorazione` | Operatore compila checklist, aggiunge note (CaseNote), completa la verifica |
| **Gestione checklist** | Operatore ANC | Completamento sezione checklist | `BOA_ANC_CheckList_Section` + `BOA_ANC_DefinisciEsitoPraticaByCheckList` | Ogni item checklist valorizzato con esito; l'esito complessivo della pratica viene calcolato automaticamente |
| **Aggiunta note operative** | Operatore ANC | Compilazione campo note nel task | `BOA_ANC_Task_MenuLaterale` (note laterali) | Nota `BOA_ANC_CaseNote` associata alla pratica; visibile in cronologia |
| **Riepilogo e chiusura task** | Operatore ANC | Completamento task | `BOA_ANC_Task_Riepilogo` + `BOA_ANC_SalvataggioDati` | Dati consolidati salvati, pratica pronta per chiusura |
| **Cambio stato pratica** | Sistema (automatico) | Completamento di ogni fase | `BOA_ANC_Processo_CambioStato` + `BOA_ANC_ScriviAudit` | Stato pratica aggiornato, traccia audit scritta per ogni transizione |
| **Consultazione cronologia / audit** | Operatore ANC, Supervisore ANC | Accesso alla tab "Cronologia" del record | `BOA_ANC_Audit` (detail view "Cronologia") | Visione completa di tutte le azioni eseguite sulla pratica con timestamp e operatore |
| **Invio esito a BPM (callback)** | Sistema (batch automatico) | Batch schedulato ogni 10 minuti | `BOA_ANC_Batch_ScodamentoCallback` → `BOA_ANC_ScodamentoSingoloEsito` → `BOA_ANC_ChiamaWS_BPM_InvioCallback` | Esito pratica (OK/KO) inviato al sistema BPM esterno; pratica passa in stato **Chiusa OK** o **Chiusa KO** |
| **Riassegnazione attività** | Supervisore ANC | Accesso alla pagina "Riassegna Attività" | `BOA_ANC_ProcessoSupervisore_RiassegnazioneTask` → `BOA_ANC_Single_Riassegnazione` | Task riassegnato a nuovo operatore |
| **Monitoraggio dashboard operatore** | Operatore ANC | Accesso alla Home | `BOA_ANC_Interfaccia_DashBoard_Operatore` + `BOA_ANC_GetNumeroPraticheDashBoard` | KPI su pratiche assegnate, in lavorazione, chiuse |
| **Monitoraggio dashboard supervisore** | Supervisore ANC | Accesso alla Home supervisore | `BOA_ANC_Interfaccia_DashBoard_Supervisore` + grafici pratiche per stato/giornaliere | Grafici produttività giornaliera, pratiche lavorate, distribuzione per stato |
| **Salvataggio filtri utente** | Operatore ANC | Applicazione filtri sulla lista attività | `BOA_ANC_SalvaFiltriUtente` | Filtri personali dell'operatore persistiti per la sessione successiva |

---

## STEP 3 — Lifecycle Funzionale Pratica

### Stati operativi pratica

| Stato | Descrizione operativa |
|---|---|
| **Aperta** | Pratica ricevuta e registrata dal sistema BPM. Dati e documenti importati. In attesa di presa in carico. |
| **In Lavorazione** | Pratica presa in carico da un Operatore ANC. Attività di verifica e lavorazione in corso. |
| **In Attesa Conferma BPM** | Operatore ha completato la lavorazione. Esito generato da SD in attesa di essere inviato e confermato dal sistema BPM. |
| **Chiusa OK** | Esito inviato a BPM e pratica conclusa con esito positivo. |
| **Chiusa KO** | Esito inviato a BPM e pratica conclusa con esito negativo. |

### Fasi del case (BOA_ANC_STATI_CASE)

| Fase Case | Fase operativa |
|---|---|
| Raccolta Input | Ricezione e parsing della pratica dalla WebAPI; download documenti |
| Lavorazione | Verifica documenti, tipizzazione, checklist, note |
| Chiusura Pratica | Definizione esito, invio callback a BPM |

### Esiti attività operatore

| Esito | Significato |
|---|---|
| **APPROVATA** | L'operatore ha valutato positivamente la pratica (tutte le verifiche OK) |
| **RESPINTA** | L'operatore ha rifiutato la pratica (verifica fallita o documentazione insufficiente) |

### Esiti SD (inviati a BPM)

| Esito SD | Significato |
|---|---|
| **OK** | Pratica approvata, attivazione carta autorizzata |
| **KO** | Pratica respinta, attivazione carta negata |

### Tabella lifecycle funzionale

| Stato Funzionale | Evento | Attore | Effetto operativo |
|---|---|---|---|
| — (inizio) | Chiamata POST `/boaanccreapratica` dal sistema BPM | Sistema esterno (BPM) | Creazione pratica, download documenti, audit registrata |
| Aperta | Avvio processo di ricezione completato | Sistema | Pratica visibile in lista attività degli Operatori ANC |
| Aperta → In Lavorazione | Presa in carico del task | Operatore ANC | Task assegnato all'operatore; `BOA_ANC_AvvioAttivita` eseguito |
| In Lavorazione | Verifica documenti (`BOA_ANC_Task_VerificaDocumenti`) | Operatore ANC | Visualizzazione dati cliente, carta, allegati; accesso al document viewer |
| In Lavorazione | Tipizzazione documento (`BOA_ANC_Processo_TipizzaDoc`) | Operatore ANC | Documento classificato per tipo; checklist generata automaticamente |
| In Lavorazione | Compilazione checklist (`BOA_ANC_CheckList_Section`) | Operatore ANC | Item checklist valorizzati; esito pratica calcolato da `BOA_ANC_DefinisciEsitoPraticaByCheckList` |
| In Lavorazione | Aggiunta note (CaseNote) | Operatore ANC | Nota operativa registrata e visibile in cronologia |
| In Lavorazione | Completamento task (`BOA_ANC_Task_Riepilogo`) | Operatore ANC | Dati salvati; esito SD (OK/KO) definito |
| In Lavorazione → In Attesa Conferma BPM | Salvataggio dati e chiusura task (`BOA_ANC_SalvataggioDati` + `BOA_ANC_Processo_CambioStato`) | Sistema | Esito accodato per invio callback; audit aggiornata |
| In Attesa Conferma BPM | Batch callback ogni 10 minuti (`BOA_ANC_Batch_ScodamentoCallback`) | Sistema (schedulato) | Esiti da inviare recuperati; chiamata WS al sistema BPM |
| In Attesa Conferma BPM → Chiusa OK | Callback BPM confermato con esito OK | Sistema BPM | Pratica chiusa con esito positivo |
| In Attesa Conferma BPM → Chiusa KO | Callback BPM confermato con esito KO | Sistema BPM | Pratica chiusa con esito negativo |
| Qualsiasi stato | Riassegnazione task dal Supervisore | Supervisore ANC | Task riassegnato a nuovo Operatore ANC |

### Diagramma lifecycle funzionale sintetico

```text
[Ricezione WebAPI]
        |
        v
   [APERTA]
   Dati pratica + documenti importati
        |
        v (Operatore ANC prende in carico)
   [IN LAVORAZIONE]
   ├── Verifica documenti
   ├── Tipizzazione documento → genera checklist
   ├── Compilazione checklist → calcola esito
   ├── Aggiunta note operative
   └── Riepilogo → definisce Esito SD (OK / KO)
        |
        v (Salvataggio e chiusura task)
   [IN ATTESA CONFERMA BPM]
   Esito accodato per invio
        |
        v (Batch ogni 10 min → chiamata WS BPM)
       / \
      /   \
[CHIUSA OK] [CHIUSA KO]
```

---

## STEP 4 — Interfacce Operative

### Scrivania Digitale Operatore ANC

#### Home — `BOA_ANC_Interfaccia_DashBoard_Operatore`

Dashboard personale dell'operatore con:
- Contatori pratiche per stato (`BOA_ANC_GetNumeroPraticheDashBoard`)
- Grafici pratiche giornaliere ricevute (`BOA_ANC_GraficiPraticheGiornaliere_Section`)
- Grafici pratiche giornaliere lavorate (`BOA_ANC_GraficiPraticheGiornaliereLavorate_Section`)
- Distribuzione pratiche per stato (`BOA_ANC_GraficiPraticheByStato_Section`)

#### Pagina Attività — `BOA_ANC_ListaAttivita`

Lista task assegnati all'operatore con:
- Filtri personalizzabili persistiti (`BOA_ANC_FiltriUtente_ListaAttivita`, salvati via `BOA_ANC_SalvaFiltriUtente`)
- Accesso diretto al task di lavorazione pratica

#### Pagina Pratiche — RecordType `Pratica_ANC`

Griglia pratiche con colonne: **Pratica N.** · **Codice Fiscale** · **Codice Cliente** · **Data Apertura** · **Data Ultima Modifica** · **Data Chiusura** · **Data Inserimento Richiesta** · **Esito SD** · **Operatore** · **Stato** · **Segnalazioni** · **Ticket SN**

Filtri rapidi (facet): Stato · Data Chiusura · Data Apertura · Data Ultima Modifica · Esito SD

Detail view pratica:
- Tab **Summary** (`BOA_ANC_Summary`): riepilogo completo dei dati pratica
- Tab **Cronologia** (`BOA_ANC_Audit`): storico di tutte le azioni eseguite

### Task di lavorazione pratica

I task operativi si compongono delle seguenti interfacce:

| Interfaccia task | Scopo operativo | Dati mostrati |
|---|---|---|
| `BOA_ANC_Task_VerificaDocumenti` | Prima verifica documentale | Pratica, Cliente, DatiCarta, ContenutiDenuncia, Checklist iniziale |
| `BOA_ANC_Task_Lavorazione` | Lavorazione completa pratica | Pratica, Cliente, DatiCarta, ContenutiDenuncia, Checklist, Documento, Note |
| `BOA_ANC_Task_TipizzazioneDocumenti` | Tipizzazione documenti allegati | Documento da tipizzare, tipo documento da selezionare |
| `BOA_ANC_Task_CheckList` | Compilazione checklist | Items checklist con esiti e motivazioni |
| `BOA_ANC_Task_Riepilogo` | Riepilogo finale e chiusura | Pratica, Documento, Note, Checklist; selezione esito APPROVATA/RESPINTA |
| `BOA_ANC_Task_MenuLaterale` | Menu laterale contestuale nel task | Pratica, Note laterali |

### Sezioni di dettaglio pratica

| Sezione | Dati visualizzati |
|---|---|
| `BOA_ANC_Sezione_DatiPratica` | Numero pratica, stato, canale, date, operatore, esito SD |
| `BOA_ANC_Sezione_DatiCliente` | Anagrafica cliente: nome, cognome, CF, sesso, data nascita, comune/provincia nascita, cittadinanza, recapiti |
| `BOA_ANC_Sezione_IndirizzoResidenza_DatiCliente` | Indirizzo residenza: via, comune, provincia, nazione, CAP, civico |
| `BOA_ANC_Sezione_DatiCarta` | Numero carta bloccata, tipo carta, intestatario |
| `BOA_ANC_Sezione_Documenti` | Lista documenti allegati con viewer integrato (`BOA_ANC_DocumentViewer_Section`) |
| `BOA_ANC_CheckList_Section` | Checklist generata per la pratica con stati item, motivazioni, esito complessivo |
| `BOA_ANC_Summary_Esito` | Esito SD calcolato con evidenza visiva (APPROVATA/RESPINTA) |

### Scrivania Digitale Supervisore ANC

#### Home — `BOA_ANC_Interfaccia_DashBoard_Supervisore`
Stessa struttura grafici della dashboard operatore ma con visibilità su tutto il team.

#### Riassegna Attività — `BOA_ANC_Intertfaccia_RiassegnazioneTask`
Interfaccia per:
- Selezionare il task da riassegnare (`BOA_ANC_GetAttivita`, `BOA_ANC_Filtri_TaskReportSupervisoreGruppi`)
- Scegliere il nuovo operatore assegnatario
- Avviare il processo di riassegnazione (`BOA_ANC_ProcessoSupervisore_RiassegnazioneTask`)

---

## STEP 5 — Integrazioni Esterne

### Sistema connesso BPM — `BOA_ANC_SistemaConnesso_BPM`

| Attributo | Valore |
|---|---|
| Nome | BOA_ANC_SistemaConnesso_BPM |
| URL base | `https://bpm-attivazione-carte-eap-bpm-attivazione-carte.apps.gen3gppito.cloudcoll.poste.it` |
| Autenticazione | OAuth 2.0 Client Credentials (Azure AD) |
| Tenant Azure AD | `08b638e8-0676-45cc-b677-5c038b17d28a` |
| Scope | `24de986e-2c18-4207-8e3f-72f288595a7e/.default` |

**Direzione**: Appian → BPM (callback esito) tramite `BOA_ANC_Integrazione_BPM_InvioCallback`.

### WebAPI in ingresso — `BOA_ANC_WebApi_CreaPratica`

| Attributo | Valore |
|---|---|
| Nome | BOA_ANC_WebApi_CreaPratica |
| Metodo HTTP | POST |
| URL Alias | `/boaanccreapratica` |
| Descrizione | Permette la creazione di una pratica di Attivazione Nuova Carta |
| Autorizzati | Operatore WebApi ANC, BOA ANC All Users |

**Payload JSON ricevuto** (struttura input):
```json
{
  "CANALE": "APP",
  "ID_WORKITEM": "<id>",
  "NUM_PRATICA": "<num>",
  "CF_CLIENTE": "<codice-fiscale>",
  "DATA_INSERIMENTO_RICHIESTA": "gg/mm/aaaa HH:MM:SS",
  "CLIENTE": {
    "NOME", "COGNOME", "SESSO", "DATANASCITA",
    "COMUNENASCITA", "PROVINCIANASCITA", "NAZIONENASCITA",
    "CITTADINANZA", "CELLULARE", "TELEFONO",
    "INDIRIZZO_DI_RESIDENZA": { "LUOGO", "COMUNE", "PROVINCIA", "NAZIONE", "CAP", "CIVICO" }
  },
  "DATI_CARTA_BLOCCATA": { "I_NUMERO_CARTA", "I_TIPO_CARTA", "I_INTEST_CARTA" },
  "DOCUMENTI": {
    "CODICE_DOC_ID": "<codice>",
    "CONTENUTI": [{ "NOME_FILE", "ESTENSIONE", "ID_DOC", "LINKDOWNLOAD" }]
  }
}
```

### Integrazione download documenti — `BOA_ANC_Integrazione_DownloadDocument`

Chiamata HTTP verso gli URL dei documenti allegati alla pratica (campo `LINKDOWNLOAD`). Scarica ogni file e lo archivia nel document store Appian della pratica.

### Integrazione Sinergia

Il sistema integra con un sistema denominato **Sinergia** (probabilmente sistema di ticketing/segnalazioni di Poste Italiane):
- `BOA_ANC_Inizializza_Sinergia_Operatore` — inizializzazione sessione Sinergia per Operatore
- `BOA_ANC_Inizializza_Sinergia_Supervisore` — inizializzazione sessione Sinergia per Supervisore
- Campo `segnalazioni` sulla pratica: indicatore di segnalazioni Sinergia presenti (icona campanella nella griglia pratiche), controllato da flag debug `BOA_ANC_DEBUG_VISUALIZZASEGNALAZIONISINERGIA`

### Integrazione ServiceNow

Il sistema integra con **ServiceNow** per la gestione ticket:
- Campo `ticketSN` sulla pratica: numero ticket ServiceNow associato alla pratica
- Visibilità colonna condizionale tramite flag `BOA_ANC_DEBUG_ABILITASERVICENOW`

---

## STEP 6 — Processi Principali

### Processo 1: Creazione Pratica (da WebAPI)

**Trigger**: POST `/boaanccreapratica` dal sistema BPM esterno.

| Fase | Processo | Azione operativa |
|---|---|---|
| 1 | `BOA_ANC_ProcessoWebApi_CreaPratica` | Ricezione payload HTTP; validazione struttura JSON (`BOA_ANC_VerificaChiavi_JsonCreazionePratica`, `BOA_ANC_VerificaValori_JsonCreazionePratica`, `BOA_ANC_VerificaComplessiva_JsonCreazionePratica`) |
| 2 | `BOA_ANC_ProcessoWebApi_CreaPratica` | Verifica duplicati per `ID_WORKITEM` (`BOA_ANC_VerificaPraticaDuplicataByIdWorkitem`) |
| 3 | `BOA_ANC_ElaborazioneJsonInput_CreazionePratica` | Parsing JSON: estrazione Pratica, Cliente, DatiCarta, ContenutiDenuncia, Documento; persistenza |
| 4 | `BOA_ANC_DownloadContenuti_CreazionePratica` | Iterazione sui contenuti documenti; avvio download per ciascuno |
| 5 | `BOA_ANC_ScaricaSingoloDoc` | Download singolo documento dal link remoto e archiviazione nel folder `BOA_ANC_FOLDER_DOCUMENTICASE` |
| 6 | `BOA_ANC_AvvioAttivita` | Creazione task assegnato al gruppo `Operatore ANC` |

### Processo 2: Lavorazione Pratica (task operatore)

**Trigger**: Operatore ANC prende in carico il task dalla lista attività.

| Fase | Interfaccia / Processo | Azione operativa |
|---|---|---|
| 1 | `BOA_ANC_Task_VerificaDocumenti` | Revisione dati pratica, cliente, carta, documenti allegati |
| 2 | `BOA_ANC_Task_TipizzazioneDocumenti` | Assegnazione tipo documento (`BOA_ANC_Processo_TipizzaDoc`): classificazione + generazione checklist |
| 3 | `BOA_ANC_CheckList_Section` | Compilazione ogni item checklist con esito (OK/KO) e motivazione (`BOA_ANC_DefinisciMotivazioniCheckList`) |
| 4 | `BOA_ANC_Task_Lavorazione` | Lavorazione completa: review dati, modifica se necessario, aggiunta note CaseNote |
| 5 | `BOA_ANC_Task_Riepilogo` | Riepilogo finale; calcolo esito automatico da checklist (`BOA_ANC_DefinisciEsitoPraticaByCheckList`); selezione APPROVATA/RESPINTA |
| 6 | `BOA_ANC_SalvataggioDati` | Persistenza definitiva dati lavorati |
| 7 | `BOA_ANC_Processo_CambioStato` | Transizione stato pratica → **In Attesa Conferma BPM**; `BOA_ANC_ScriviAudit` |

### Processo 3: Invio Callback a BPM

**Trigger**: Batch schedulato ogni 10 minuti.

| Fase | Processo | Azione operativa |
|---|---|---|
| 1 | `BOA_ANC_Batch_ScodamentoCallback` | Recupero esiti da inviare (`BOA_ANC_GetEsitiToSend`); iterazione per ciascuno |
| 2 | `BOA_ANC_ScodamentoSingoloEsito` | Elaborazione singolo esito: generazione payload (`BOA_ANC_GeneraRequest_BPM_InvioCallback`) |
| 3 | `BOA_ANC_ChiamaWS_BPM_InvioCallback` | Chiamata HTTP al sistema BPM (`BOA_ANC_Integrazione_BPM_InvioCallback`) con OAuth |
| 4 | `BOA_ANC_ElaboraResponse_BPM_InvioCallback` | Elaborazione risposta BPM; determinazione codice esito (`BOA_ANC_Decisione_CodiceEsitoCallback`) |
| 5 | `BOA_ANC_Processo_CambioStato` | Aggiornamento stato pratica → **Chiusa OK** o **Chiusa KO** |

### Processo 4: Riassegnazione Task (supervisore)

**Trigger**: Supervisore ANC accede alla pagina "Riassegna Attività".

| Fase | Processo | Azione operativa |
|---|---|---|
| 1 | `BOA_ANC_ProcessoSupervisore_RiassegnazioneTask` | Supervisore seleziona task da riassegnare e nuovo operatore destinatario |
| 2 | `BOA_ANC_Single_Riassegnazione` | Modifica dell'assegnatario del task; audit della riassegnazione |

---

## STEP 7 — Struttura Informativa della Pratica

### Dato operativo della pratica (BOA_ANC_Pratica)

| Campo | Significato operativo |
|---|---|
| `idCase` | Identificativo univoco interno Appian della pratica |
| `idWorkItem` | ID del work item del sistema BPM mittente (chiave di correlazione) |
| `numPratica` | Numero pratica visibile all'operatore |
| `canale` | Canale di provenienza (es. APP) |
| `cfCliente` | Codice fiscale del cliente |
| `codiceCliente` | Codice cliente Poste |
| `stato` | Stato operativo pratica (Aperta / In Lavorazione / In Attesa Conferma BPM / Chiusa OK / Chiusa KO) |
| `esitoSD` | Esito definito dalla Scrivania Digitale (OK / KO) |
| `operatoreUltimaModifica` | Username operatore che ha effettuato l'ultima modifica |
| `dataApertura` | Data creazione pratica |
| `dataUltimaModifica` | Data ultima modifica |
| `dataChiusura` | Data chiusura pratica |
| `dataInserimentoRichiesta` | Data di inserimento richiesta nel sistema di origine |
| `dataScadenza` | Data di scadenza pratica |
| `segnalazioni` | Contatore segnalazioni Sinergia |
| `ticketSN` | Numero ticket ServiceNow correlato |

### Dato cliente (BOA_ANC_Cliente)

Nome · Cognome · Sesso · Data di nascita · Comune/Provincia/Nazione di nascita · Cittadinanza · Cellulare · Telefono · Indirizzo di residenza (luogo, comune, provincia, nazione, CAP, civico)

### Dato carta bloccata (BOA_ANC_DatiCarta)

Numero carta · Tipo carta (es. Postepay) · Intestatario carta

### Documenti allegati

Ogni pratica ha una lista di **contenuti denuncia** (`BOA_ANC_ContenutiDenuncia`): nome file, estensione, ID documento sorgente, link di download.  
Dopo tipizzazione, ogni contenuto diventa un **documento Appian** (`BOA_ANC_Documento`) con tipo assegnato (`BOA_ANC_TipiDocumento`).

### Checklist (`BOA_ANC_CaseChecklist`)

Generata automaticamente al momento della tipizzazione del documento. Ogni item ha:
- Riferimento checklist template (`BOA_ANC_RefChecklist`)
- Esito item (OK/KO)
- Motivazione
- Visibilità (abilitata/disabilitata da `BOA_ANC_AbilitaVisibilitaCheckList`)

L'esito complessivo della pratica viene calcolato automaticamente dalla checklist tramite `BOA_ANC_DefinisciEsitoPraticaByCheckList`.

### Note operative (`BOA_ANC_CaseNote`)

Note testuali inserite dall'operatore durante la lavorazione. Associate alla pratica e visibili nella cronologia. Recuperabili tramite `BOA_ANC_GetNotaByIdCase`.

### Audit / Cronologia (`BOA_ANC_Audit`)

Traccia di ogni azione operativa sulla pratica: cambio stato, tipizzazione, completamento task, riassegnazione, invio callback. Scritta da `BOA_ANC_ScriviAudit`. Visibile come tab "Cronologia" nel dettaglio record pratica.

### Gestione scodamento callback (`BOA_ANC_GestioneScodamento`, `BOA_ANC_ScodamentoStati`)

Coda di esiti in attesa di essere inviati al BPM. Ogni record di scodamento ha:
- ID pratica
- Esito da inviare (OK/KO)
- Stato invio (`BOA_ANC_STATIINVIO`)
- Contatore retry (max: `BOA_ANC_NUMBER_MAXRETRY`)

---

## Riepilogo oggetti per categoria

### Costanti operative rilevanti

| Costante | Valore / Scopo |
|---|---|
| `BOA_ANC_STATI_PRATICA` | Aperta · In Lavorazione · In Attesa Conferma BPM · Chiusa OK · Chiusa KO |
| `BOA_ANC_STATI_CASE` | Raccolta Input · Lavorazione · Chiusura Pratica |
| `BOA_ANC_ESITI_SD` | OK · KO |
| `BOA_ANC_CANALE` | Codifica canale (es. APP) |
| `BOA_ANC_TIPOLOGIE_CODICE_DOC_ID` | Codici tipo documento |
| `BOA_ANC_TIPOCASE` | Tipo case applicativo |
| `BOA_ANC_NOMEAPPLICAZIONE` | Nome applicazione visualizzato |
| `BOA_ANC_STATIINVIO` | Stati del meccanismo di scodamento callback |
| `BOA_ANC_NUMEROESITITOSEND` | Numero massimo esiti inviati per ciclo batch |
| `BOA_ANC_NUMBER_MAXRETRY` | Numero massimo tentativi callback BPM |
| `BOA_ANC_NUMBER_MAXFILTRITOSHOW` | Numero massimo filtri mostrati nella lista attività |

### Process Model per categoria funzionale

| Categoria | Process Model |
|---|---|
| Creazione pratica | `BOA_ANC_ProcessoWebApi_CreaPratica` · `BOA_ANC_ElaborazioneJsonInput_CreazionePratica` · `BOA_ANC_DownloadContenuti_CreazionePratica` · `BOA_ANC_ScaricaSingoloDoc` · `BOA_ANC_AvvioAttivita` |
| Lavorazione | `BOA_ANC_Processo_TipizzaDoc` · `BOA_ANC_SalvataggioDati` · `BOA_ANC_Processo_CambioStato` |
| Audit | `BOA_ANC_ScriviAudit` |
| Callback BPM | `BOA_ANC_Batch_ScodamentoCallback` · `BOA_ANC_ScodamentoSingoloEsito` · `BOA_ANC_ChiamaWS_BPM_InvioCallback` |
| Riassegnazione | `BOA_ANC_ProcessoSupervisore_RiassegnazioneTask` · `BOA_ANC_Single_Riassegnazione` |
| Filtri utente | `BOA_ANC_SalvaFiltriUtente` |
| Setup/Inizializzazione | `BOA_ANC_InizializzaBoaCodifiche` · `BOA_ANC_inizializzaUtenteServizio` · `BOA_ANC_Inizializza_Sinergia_Operatore` · `BOA_ANC_Inizializza_Sinergia_Supervisore` |
| Debug/Diagnostica | `BOA_ANC_Processo_ScritturaTabellaDebugWS` |

---

*Fine documento — generato da reverse engineering funzionale dell'export Appian versione 20260506.*
