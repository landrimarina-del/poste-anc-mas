# 03 - GAP Coverage Review
## Verifica copertura Discovery + GAP vs Reverse Engineering

> **Scopo**: Verificare che l'insieme delle user story della discovery (`04_Epic_UserStories.md`) più le user story validate nel documento GAP (`02_GAP_HighLevel_Review.md`) copra completamente le funzionalità rilevate nel reverse engineering (`docs/reverse/attivazione-nuova-carta/v20260506/`).
>
> **Data analisi**: 2026-05-21

---

## Parte 1 — User Story validate dai GAP

Le seguenti US sono state aggiunte o precisate attraverso la sessione di revisione GAP. Non compaiono nel backlog originale della discovery ma sono necessarie per la copertura completa.

| ID | GAP origine | Titolo sintetico |
|---|---|---|
| GAP-US-01 | GAP-01 | Mock integrazione sistema di ticketing — persist ticket ID |
| GAP-US-02 | GAP-02 | Chiamata BPM sincrona con retry configurabile |
| GAP-US-03 | GAP-03 | Verifica documenti (read-only) + classificazione irreversibile sequenziale |
| GAP-US-04 | GAP-04 | Sidebar navigazione lavorazione (3 step collassabile) |
| GAP-US-05 | GAP-05 | Filtri Lista Attività salvati su DB per utente |
| GAP-US-06 | GAP-08 | Linea di avanzamento fasi operative nella lavorazione |
| GAP-US-07 | GAP-09 | Mock BPM callback URL configurabile (ACK OK/KO) |
| GAP-US-08 | GAP-P02 | Visibilità condizionale item checklist per `idDipendenza` |
| GAP-US-09 | GAP-P04 | Griglia pratiche — 11 colonne fisse, segnalazioni calcolata |
| GAP-US-10 | GAP-P05 | Tab "Stati" separato da Cronologia nel dettaglio pratica |
| GAP-US-11 | GAP-P06 | Doppio meccanismo motivazioni checklist (nota libera + codice formale) |

### Testo completo US validate

---

#### GAP-US-01 — Mock integrazione sistema di ticketing
> *Come sistema, voglio inviare una segnalazione al sistema di ticketing quando si apre una nuova pratica, ricevere l'id del ticket generato e persistirlo sul record pratica, così la tracciabilità tra i due sistemi è garantita.*
> *Nella POC il sistema di ticketing è simulato da un mock che restituisce un id fittizio.*

---

#### GAP-US-02 — Chiamata BPM sincrona con retry configurabile
> *Come sistema, quando invio l'esito di una pratica al BPM, voglio eseguire la chiamata in modo sincrono attendendo la risposta entro un timeout configurabile; se la chiamata fallisce o va in timeout, voglio riprovare automaticamente per un numero massimo di tentativi configurabile, con un intervallo di attesa tra un tentativo e l'altro, così garantisco la consegna dell'esito senza perdere pratiche chiuse.*
> *Se tutti i tentativi si esauriscono senza successo, la pratica rimane in stato "In Attesa Conferma BPM" e l'evento di fallimento viene loggato.*

---

#### GAP-US-03 — Verifica documenti + classificazione sequenziale
> *Come operatore, dopo aver accettato un task, voglio visualizzare in una schermata dedicata tutti i dati della pratica (dati cliente, dati carta, documenti allegati) in sola lettura, così posso valutare il materiale prima di procedere.*
> *Dopo la revisione, voglio poter avviare esplicitamente la classificazione del documento: si apre una schermata separata dove seleziono il tipo documento da un elenco e confermo la scelta.*
> *La conferma della classificazione è irreversibile: il tipo documento non può essere modificato in seguito.*
> *Solo dopo la conferma della classificazione il sistema genera la checklist appropriata e l'operatore accede alla fase di lavorazione.*

---

#### GAP-US-04 — Sidebar navigazione lavorazione (3 step)
> *Come operatore, durante la lavorazione di una pratica voglio avere sempre visibile sulla sinistra una sidebar di navigazione con i tre step della lavorazione (Dati Pratica, Verifica Documento, Riepilogo), così posso spostarmi tra gli step senza perdere il contesto.*
> *La sidebar è collassabile: posso ridurla a icone per guadagnare spazio nell'area di lavoro e riespadirla quando necessario.*
> *Lo step "Riepilogo" nella sidebar è navigabile solo dopo che l'operatore ha completato la verifica documento (dopo aver cliccato "Salva e prosegui"); fino ad allora appare disabilitato.*
> *Le note interne non sono in un pannello laterale separato: sono un campo di testo nel tab Riepilogo, visibile solo se l'esito calcolato è RESPINTA, e il loro inserimento è facoltativo.*

---

#### GAP-US-05 — Filtri Lista Attività salvati su DB per utente
> *Come operatore, nella pagina Lista Attività voglio poter salvare il set di filtri corrente tramite il pulsante "Applica e Salva Filtri", così posso riutilizzarlo nelle sessioni successive senza doverlo reinserire.*
> *I filtri salvati sono visibili in una sezione dedicata sopra la lista attività, che mostra gli ultimi N set salvati in una griglia con le colonne: Stato, Tipo Pratica, Pratica N., Nome Attività, Data Scadenza Da, Data Scadenza A, Assegnatario, Utente in carico.*
> *Selezionando una riga della griglia, i valori del set scelto vengono automaticamente popolati nei campi filtro; l'operatore può poi premere "Applica Filtri" per applicarli.*
> *Il pulsante "Applica Filtri" applica i filtri senza salvarli. Il pulsante "Applica e Salva Filtri" salva il set corrente e lo applica. È presente un pulsante per azzerare tutti i filtri attivi.*
> *I filtri salvati sono associati all'utente loggato e persistiti su database; sono disponibili in tutte le sessioni successive.*

---

#### GAP-US-06 — Linea di avanzamento fasi operative
> *Come operatore, durante la lavorazione e nella consultazione del dettaglio pratica, voglio vedere una linea di avanzamento che indica in quale macro-fase del ciclo di vita si trova la pratica, così ho sempre il contesto del punto in cui mi trovo nel processo.*
> *Le fasi visualizzate sono tre, in sequenza fissa: Raccolta Input → Lavorazione → Chiusura Pratica. La fase corrente è evidenziata visivamente.*
> *La linea di avanzamento non è un campo editabile né selezionabile: è solo indicativa. Il suo stato è derivato automaticamente dallo stato della pratica e dallo step di lavorazione corrente.*
> *Le fasi operative non sono persistite come campo separato su database.*

---

#### GAP-US-07 — Mock BPM callback URL configurabile
> *Come sistema, quando devo inviare l'esito di una pratica al BPM, voglio invocare un endpoint mock configurabile che simula la ricezione del callback e restituisce una risposta di ACK, così il flusso completo di chiusura pratica è verificabile nella POC senza dipendenze da sistemi esterni reali.*
> *L'URL del mock BPM e gli eventuali parametri di chiamata sono configurati tramite file di configurazione applicativa.*
> *Il mock deve restituire una risposta che includa il codice esito (es. Chiusa OK / Chiusa KO) in modo da esercitare entrambi i percorsi di chiusura.*

---

#### GAP-US-08 — Visibilità condizionale item checklist
> *Come operatore, durante la compilazione della checklist voglio che gli item non pertinenti al caso specifico vengano nascosti automaticamente in base alle mie risposte precedenti, così la checklist mostra solo i controlli effettivamente applicabili.*
> *Ogni item della checklist può dipendere dalla risposta di un altro item: se l'item padre ha una risposta che rende il figlio non applicabile, l'item figlio non viene visualizzato nella tabella.*
> *Le regole di dipendenza tra item sono definite a livello di configurazione della checklist (statiche): non cambiano durante la lavorazione, ma variano in base al tipo documento classificato.*
> *La logica di visibilità per-item è indipendente dalla cascata globale KO.*

---

#### GAP-US-09 — Griglia pratiche — 11 colonne fisse
> *Come operatore e supervisore, nella lista pratiche voglio visualizzare una griglia con le seguenti colonne fisse, nell'ordine indicato: Pratica N. (link al dettaglio), Codice Fiscale, Codice Cliente, Data Apertura, Data Ultima Modifica, Data Chiusura, Data Inserimento Richiesta, Esito SD, Operatore, Stato, Segnalazioni.*
> *La colonna "Segnalazioni" mostra un'icona visiva quando la pratica ha almeno una segnalazione attiva associata; il valore è calcolato a partire dal conteggio delle segnalazioni.*
> *Le colonne Id, Canale e Data Scadenza sono presenti nel modello dati ma non esposte nella griglia. Il numero ticket non è una colonna della lista pratiche.*
> *Le colonne non sono personalizzabili dall'utente.*

---

#### GAP-US-10 — Tab "Stati" separato nel dettaglio pratica
> *Come operatore e supervisore, nel dettaglio di una pratica voglio accedere al tab "Stati" per consultare la sequenza completa delle transizioni di stato, in ordine cronologico.*
> *Per ogni riga devo vedere: stato raggiunto, data/ora di inizio, operatore (o sistema) che ha causato la transizione.*
> *Il tab "Stati" è distinto dal tab "Cronologia": la Cronologia mostra il log di tutte le azioni operative; il tab Stati mostra esclusivamente l'evoluzione degli stati della pratica.*
> *Entrambi i tab sono in sola lettura.*

---

#### GAP-US-11 — Doppio meccanismo motivazioni item checklist
> *Come operatore, durante la compilazione della checklist, per ogni item che marco come non conforme posso inserire:*
> *1. una nota libera (testo, max 255 caratteri) per annotare liberamente la non conformità rilevata;*
> *2. un codice causale formale selezionato dal catalogo motivazioni filtrato per la categoria dell'item corrente (es. categoria "Verbale" o categoria "Carta").*
> *Entrambi i campi sono opzionali. Il catalogo causali è condiviso tra tutte le categorie di checklist ed è differenziato dal campo categoria.*

---

## Parte 2 — Matrice di copertura Discovery + GAP vs Reverse

Legenda: COPERTO | PARZIALE | MANCANTE | OUT_OF_SCOPE

| ID | Funzionalità dal Reverse | US Discovery | US GAP | Copertura |
|---|---|---|---|---|
| R-01 | Ruoli business (Operatore, Supervisore, WebAPI) | E1.01, E1.02, E8.05 | — | COPERTO |
| R-02 | Siti e pagine (Operatore: Home/Attività/Pratiche; Supervisore: Home/Pratiche/Riassegna) | E1.03 | — | COPERTO |
| R-03 | Creazione pratica via WebAPI POST (validazione completa) | E2.01–E2.11 | — | COPERTO |
| R-04 | Download automatico allegati da URL remoto (LINKDOWNLOAD) post-creazione | E2.05 (implicita) | — | PARZIALE: US-E2.05 non modella esplicitamente il download HTTP da URL remoto |
| R-05 | Idempotenza su ID_WORKITEM | E2.04 | — | COPERTO |
| R-06 | Audit creazione pratica | E2.06 | — | COPERTO |
| R-07 | Lista pratiche con filtri e paginazione | E3.01–E3.04 | — | COPERTO |
| R-08 | Colonne griglia lista pratiche (11 colonne fisse) | E3.01 (generica) | GAP-US-09 | COPERTO |
| R-09 | Dettaglio pratica — dati testata, cliente, carta | E3.05 | — | COPERTO |
| R-10 | Dettaglio pratica — tab Cronologia (BOA_ANC_Audit) | E3.06, E11.06 | — | COPERTO |
| R-11 | Dettaglio pratica — tab Stati (BOA_ANC_StatiPratica) | E3.07 (titolo) | GAP-US-10 | COPERTO |
| R-12 | Dettaglio pratica — tab Azioni Correlate | E11.05 | — | COPERTO |
| R-13 | Generazione automatica task alla creazione pratica | E4.01 | — | COPERTO |
| R-14 | Lista Attività con filtri | E4.02, E4.03 | — | COPERTO |
| R-15 | Filtri Lista Attività salvati su DB per utente (griglia Ultimi N filtri) | — | GAP-US-05 | COPERTO |
| R-16 | Presa in carico task (ACCETTA) → pratica In Lavorazione | E4.04, E4.05 | — | COPERTO |
| R-17 | Verifica documenti (schermata read-only, step 1) | E5.01 (viewer) | GAP-US-03 | COPERTO |
| R-18 | Visualizzatore documenti integrato (zoom, dimensioni) | E5.01, E5.02 | — | COPERTO |
| R-19 | Download manuale fallback allegato | E5.03, E5.06 | — | COPERTO |
| R-20 | Tipizzazione documento — selezione tipo + CONFERMA irreversibile | E5.04, E5.05 | GAP-US-03 | COPERTO |
| R-21 | Generazione automatica checklist post-tipizzazione | E5.04 (implicita), E6.01 | — | COPERTO |
| R-22 | Sidebar navigazione lavorazione (3 step collassabile) | — | GAP-US-04 | COPERTO |
| R-23 | Linea avanzamento fasi operative (Raccolta Input / Lavorazione / Chiusura) | C2.6 (implicita) | GAP-US-06 | COPERTO |
| R-24 | Checklist Verbale di Denuncia — compilazione item | E6.01–E6.07 | — | COPERTO |
| R-25 | Cascata KO se documento assente (flagPresenza = No) | E6.02 | — | COPERTO |
| R-26 | Visibilità condizionale item checklist per idDipendenza | — | GAP-US-08 | COPERTO |
| R-27 | Causali KO formali (Intestazione/Firme/Timbro/Dichiarazione/Carta PI) | E6.04 | GAP-US-11 | COPERTO |
| R-28 | Note libere per item checklist (note VARCHAR 255) | E6.04 (parziale) | GAP-US-11 | COPERTO |
| R-29 | Salva e Prosegui (bozza checklist) | E6.06 | — | COPERTO |
| R-30 | Modifica checklist salvata | E6.07 | — | COPERTO |
| R-31 | Calcolo esito automatico (Approvata/Respinta) | E6.08 | — | COPERTO |
| R-32 | Note interne facoltative (solo se RESPINTA, in Riepilogo) | E6.09 | GAP-US-04 | COPERTO |
| R-33 | Checklist Carta tagliata | E7.01 | — | COPERTO |
| R-34 | Chiusura task CHIUDI PRATICA → In Attesa Conferma BPM | E7.02 | — | COPERTO |
| R-35 | Salvataggio dati pratica (persistenza definitiva) | E7.02 (implicita) | — | COPERTO |
| R-36 | Invio esito a BPM (OK / single KO / KO multipli con codici) | E7.03 | — | COPERTO |
| R-37 | Retry automatico invio BPM (contatore, timeout configurabile) | — | GAP-US-02 | COPERTO |
| R-38 | Mock BPM callback URL configurabile (esercita OK e KO) | — | GAP-US-07 | COPERTO |
| R-39 | Conferma BPM → Chiusa OK / Chiusa KO con data chiusura | E7.04 | — | COPERTO |
| R-40 | Cambio stato pratica automatico + scrittura audit | E7.05 | — | COPERTO |
| R-41 | Integrazione sistema di ticketing (persist ticketSN via mock) | — | GAP-US-01 | COPERTO |
| R-42 | Riassegnazione task dal Supervisore (a gruppo o utente) | E8.01–E8.05 | — | COPERTO |
| R-43 | Dashboard Home Operatore — contatori KPI (N. Attività, Pratiche Attive, Pratiche Chiuse) | — | — | MANCANTE |
| R-44 | Dashboard Home Supervisore — contatori KPI | E9.01 | — | COPERTO |
| R-45 | Dashboard Supervisore — 3 istogrammi produttività | E9.02–E9.05 | — | COPERTO |
| R-46 | Integrazione Sinergia (segnalazioni, inizializzazione sessione) | E10.01–E10.05 | — | COPERTO |
| R-47 | Campo data_scadenza su pratica (presente su DB, nascosto in UI) | — | GAP-07 (no US, comportamento preservato) | COPERTO |
| R-48 | Bootstrap applicativo (inizializzazione codifiche e utente servizio) | — | GAP-10 (comportamento preservato) | COPERTO |
| R-49 | Tabelle Debug WS (BOA_ANC_Debug_WebServices, BOA_ANC_Debug_WsInput) | — | — | OUT_OF_SCOPE |

---

## Parte 3 — Gap residui dopo discovery + GAP review

### MANCANTE — FC-01: Dashboard Home Operatore con contatori KPI

**Funzionalità reverse**: `BOA_ANC_Interfaccia_DashBoard_Operatore` + `BOA_ANC_GetNumeroPraticheDashBoard`

La Home dell'Operatore ANC mostra in tempo reale i contatori: N° Attività, Pratiche Attive, Pratiche Chiuse. Nessuna US della discovery né dei GAP copre questa schermata per il ruolo Operatore. La US-E9.01 è esplicita solo per il Supervisore; US-E1.03 descrive solo la navigazione tra tab, non il contenuto della Home.

**User story proposta**:
> *Come Operatore, voglio visualizzare nella Home i contatori in tempo reale (N° Attività, Pratiche Attive, Pratiche Chiuse) riferiti al mio carico di lavoro personale, così monitoro la mia situazione senza accedere alla lista pratiche.*
> *I contatori mostrano esclusivamente dati relativi all'operatore loggato (non visibilità globale del team). Non sono presenti istogrammi nella Home Operatore.*

**Azione richiesta**: Aggiungere US-E0.01 in EPIC E1 (Foundation) o creare EPIC E0 dedicata alla Home Operatore. Aggiornare `04_Epic_UserStories.md`.

---

### PARZIALE — FC-02: Download automatico allegati da URL remoto

**Funzionalità reverse**: `BOA_ANC_DownloadContenuti_CreazionePratica` → `BOA_ANC_ScaricaSingoloDoc`

Il sistema scarica automaticamente ogni allegato dalla URL remota (LINKDOWNLOAD) dopo la creazione della pratica. La US-E2.05 dice solo "persistere l'allegato ricevuto" senza modellare il meccanismo HTTP di download da URL esterna.

GAP-P01 ha confermato "nessun requisito aggiuntivo, comportamento del reverse preservato". La copertura è implicita ma non testabile come US autonoma.

**Azione richiesta**: Precisare US-E2.05 aggiungendo: *"...scaricandolo da un URL remoto fornito nel payload della richiesta (campo LINKDOWNLOAD); se il download fallisce, la pratica è comunque creata e il documento risulta non disponibile per l'operatore."*

---

### OUT_OF_SCOPE — FC-03: Tabelle Debug e Diagnostica WS

**Funzionalità reverse**: `BOA_ANC_Processo_ScritturaTabellaDebugWS`, `BOA_ANC_Debug_WebServices`, `BOA_ANC_Debug_WsInput`

Meccanismo di log diagnostico delle chiamate ai web service esterni. Non è una funzionalità business e non è richiesta nella POC. Da dichiarare formalmente come OUT_OF_SCOPE in `07_Risk_Open_Questions.md`.

---

## Riepilogo

| Totale funzionalità rilevate nel reverse | Coperte | Parziali | Mancanti | Out of scope |
|---|---|---|---|---|
| 49 | 46 | 1 | 1 | 1 |

| ID | Titolo | Tipo | Azione |
|---|---|---|---|
| FC-01 | Dashboard Home Operatore — Contatori KPI | MANCANTE | Aggiungere US in `04_Epic_UserStories.md` |
| FC-02 | Download automatico allegati da URL remoto | PARZIALE | Precisare US-E2.05 in `04_Epic_UserStories.md` |
| FC-03 | Tabelle Debug / Diagnostica WS | OUT_OF_SCOPE | Documentare in `07_Risk_Open_Questions.md` |
