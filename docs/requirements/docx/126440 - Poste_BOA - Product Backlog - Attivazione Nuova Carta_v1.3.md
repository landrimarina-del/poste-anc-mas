DTO Unit

Attivazione Nuova Carta

Back Office Automation

> Product Backlog
>
> Codice GoTo: 126440

  --------------- --------------------- ---------------
                   **Nome e Cognome**      **Data**

  Written by:       Amalia Cacciatore    *28/04/2023*

  Verified by         RT&TL Lutech       *GG/MM/AAAA*

  Approved by:     DTO Poste Italiane    *GG/MM/AAAA*
  --------------- --------------------- ---------------

Distribution List

  ---------------- ------------------------------------------------------------------------ ---------------
  **Name**         **Reference**                                                            **Role**

  Antonio Forino   <antonio.forino@posteitaliane.it>                                        DTO -- Poste

  Vito Lecci       <vito.lecci@posteitaliane.it>                                            DTO -- Poste

  Alessandro Gatta <alessandro.gatta@posteitaliane.it>                                      DTO -- Poste

  Vincenzo Genova  <vincenzo.genova@posteitaliane.it>                                       DTO -- Poste

  Marichita        <marichita.petrone@posteitaliane.it>                                     DTO -- Poste
  Petrone                                                                                   

  Francesco        <francesco.maragno@posteitaliane.it>                                     DTO -- Poste
  Maragno                                                                                   

  Maria Cristina   <mariac.fior@posteitaliane.it>                                           DTO -- Poste
  Fiori                                                                                     

  Team Collaudo    <teamBOA@exprivia.com>                                                   Team Collaudo

  Marcello Maio    [marcello.maio@lutech.it](mailto:marcello.maio@)                         RT Lutech

  Stefano Pintore  [stefano.pintore@lutech.it](mailto:stefano.pintore@atos.net)             TL Lutech

  Botta Gennaro    [gennaro.botta@lutech.it](mailto:gennaro.botta@atos.net)                 TM Lutech

  Simona Zarro     simona.zarro@lutech.it                                                   CM lutech

  Seif Benjouira   [seifeddine.benjouira@lutech.it](mailto:seifeddine.benjouira@atos.net)   AD Lutech

  Luana Mirabella  [luana.mirabella@lutech.it](mailto:luana.mirabella@atos.net)             BA Lutech

  Amalia           [amalia.cacciatore@lutech.it](mailto:amalia.cacciatore@atos.net)         BA Lutech
  Cacciatore                                                                                

  Scrum Team       <Poste-BOA@atos365.onmicrosoft.com>                                      Scrum Team
  ---------------- ------------------------------------------------------------------------ ---------------

Changes to the document

+-------------------+-------------------+--------------------------------------------------------------+----------------------------+
| **Version**       | **Date**          | **Description of Changes**                                   | **Written by**             |
+:=================:+==================:+==============================================================+===========================:+
| 1.0               | 28/04/2023        | Versione di prima emissione                                  | Lutech                     |
+-------------------+-------------------+--------------------------------------------------------------+----------------------------+
| 1.1               | 02/05/2023        | Aggiornamento References \[3\], References \[4\], References | Lutech                     |
|                   |                   | \[5\] References \[6\] -- Aggiornamento Scenario             |                            |
|                   |                   | [4.3.1.2](#scenario-filtro-lista-pratiche), US               |                            |
|                   |                   | [4.8](#user-story-chiusura-di-un-task-nella-lista-attività), |                            |
|                   |                   | Scenario [4.3.1.1](#scenario-visualizzazione-lista-pratiche) |                            |
+-------------------+-------------------+--------------------------------------------------------------+----------------------------+
| 1.2               | 02/05/2023        | Aggiornamento References \[3\]                               | Lutech                     |
+-------------------+-------------------+--------------------------------------------------------------+----------------------------+
| 1.3               | 27/11/2023        | Aggiornamento References \[3\] -- Aggiornamento versione 1.2 | Lutech                     |
|                   |                   | come da revisione per introduzione CR.                       |                            |
+-------------------+-------------------+--------------------------------------------------------------+----------------------------+
|                   |                   |                                                              |                            |
+-------------------+-------------------+--------------------------------------------------------------+----------------------------+
|                                                                                                                                   |
+---------------------------------------+-------------------------------------------------------------------------------------------+
| **Link Documento**                    | Link                                                                                      |
+---------------------------------------+-------------------------------------------------------------------------------------------+

> []{#_Toc40969612 .anchor}Table 1 -- Version Control

References

1.  PI-DTO, 20230202_Linee guida controlli Backoffice_FG2
    ![](media/image1.emf)

2.  LUTECH, Codici Motivazioni BPM ![](media/image2.emf)

3.  LUTECH, Interface Agreement Servizi SD per BPM_ANC_ver1.4
    ![](media/image3.emf)

4.  LUTECH, Checklist SD_126440\_ V0.5 ![](media/image4.emf)

5.  []{#_Ref93566583 .anchor}BPM, Swagger Servizio SubmitWorkItem 1.1
    ![](media/image5.emf)

6.  LUTECH, Automatismi Esiti SD_ANC v. 0.2 ![](media/image6.emf)

Index

[1. Acronyms, glossary [5](#acronyms-glossary)](#acronyms-glossary)

[2. Overview [6](#overview)](#overview)

[3. Backlog and Iteration Functionalities
[7](#backlog-and-iteration-functionalities)](#backlog-and-iteration-functionalities)

[4. Functional Definition
[8](#functional-definition)](#functional-definition)

[4.1. Integration Story "Integrazione BPM: Richiesta di Apertura
Pratica"
[8](#integration-story-integrazione-bpm-richiesta-di-apertura-pratica)](#integration-story-integrazione-bpm-richiesta-di-apertura-pratica)

[4.1.1. Acceptance Criteria
[8](#acceptance-criteria)](#acceptance-criteria)

[4.1.1.1. Scenario: Ricezione richiesta Creazione Pratica
[8](#scenario-ricezione-richiesta-creazione-pratica)](#scenario-ricezione-richiesta-creazione-pratica)

[4.1.1.2. Scenario: Response Servizio Apertura Pratica
[8](#scenario-response-servizio-apertura-pratica)](#scenario-response-servizio-apertura-pratica)

[4.1.1.3. Scenario: Mancato invio oggetto "Documenti"
[8](#scenario-mancato-invio-oggetto-documenti)](#scenario-mancato-invio-oggetto-documenti)

[4.1.1.4. Scenario: Gestione dell'Idempotenza
[8](#scenario-gestione-dellidempotenza)](#scenario-gestione-dellidempotenza)

[4.1.1.5. Scenario: Interrogazione fallita per indisponibilità del
servizio
[8](#scenario-interrogazione-fallita-per-indisponibilità-del-servizio)](#scenario-interrogazione-fallita-per-indisponibilità-del-servizio)

[4.2. User Story "Creazione Pratica"
[9](#user-story-creazione-pratica)](#user-story-creazione-pratica)

[4.2.1. Acceptance Criteria
[9](#acceptance-criteria-1)](#acceptance-criteria-1)

[4.2.1.1. Scenario: Creazione Pratica
[9](#scenario-creazione-pratica)](#scenario-creazione-pratica)

[4.2.1.2. Scenario: Riepilogo Pratica
[9](#scenario-riepilogo-pratica)](#scenario-riepilogo-pratica)

[4.2.1.3. Scenario: Funzionalità di Visualizzazione per la Navigazione
[10](#scenario-funzionalità-di-visualizzazione-per-la-navigazione)](#scenario-funzionalità-di-visualizzazione-per-la-navigazione)

[4.3. User Story "Visualizzazione Lista Pratiche"
[11](#user-story-visualizzazione-lista-pratiche)](#user-story-visualizzazione-lista-pratiche)

[4.3.1. Acceptance criteria
[11](#acceptance-criteria-2)](#acceptance-criteria-2)

[4.3.1.1. Scenario: Visualizzazione Lista Pratiche
[11](#scenario-visualizzazione-lista-pratiche)](#scenario-visualizzazione-lista-pratiche)

[4.3.1.2. Scenario: Filtro Lista Pratiche
[11](#scenario-filtro-lista-pratiche)](#scenario-filtro-lista-pratiche)

[4.4. User Story "Creazione di un Task nella Lista Attività"
[11](#user-story-creazione-di-un-task-nella-lista-attività)](#user-story-creazione-di-un-task-nella-lista-attività)

[4.4.1. Acceptance Criteria
[12](#acceptance-criteria-3)](#acceptance-criteria-3)

[4.4.1.1. Scenario: Generazione del Task
[12](#scenario-generazione-del-task)](#scenario-generazione-del-task)

[4.5. User Story "Visualizzazione Lista Attività"
[12](#user-story-visualizzazione-lista-attività)](#user-story-visualizzazione-lista-attività)

[4.5.1. Acceptance criteria
[12](#acceptance-criteria-4)](#acceptance-criteria-4)

[4.5.1.1. Scenario: Visualizzazione Lista Attività
[12](#scenario-visualizzazione-lista-attività)](#scenario-visualizzazione-lista-attività)

[4.5.1.2. Scenario: Sorting Colonne
[12](#scenario-sorting-colonne)](#scenario-sorting-colonne)

[4.5.1.3. Scenario: Filtro Attività
[12](#scenario-filtro-attività)](#scenario-filtro-attività)

[4.6. User Story "Presa in carico di un Task nella Lista Attività"
[12](#user-story-presa-in-carico-di-un-task-nella-lista-attività)](#user-story-presa-in-carico-di-un-task-nella-lista-attività)

[4.6.1. Acceptance Criteria
[13](#acceptance-criteria-5)](#acceptance-criteria-5)

[4.6.1.1. Scenario: Presa in Carico del Task Attività
[13](#scenario-presa-in-carico-del-task-attività)](#scenario-presa-in-carico-del-task-attività)

[4.6.1.2. Scenario: Selezione Tipologia Documento da verificare
[13](#scenario-selezione-tipologia-documento-da-verificare)](#scenario-selezione-tipologia-documento-da-verificare)

[4.6.1.3. Scenario: Visualizzazione Dettaglio Task Attività
[13](#scenario-visualizzazione-dettaglio-task-attività)](#scenario-visualizzazione-dettaglio-task-attività)

[4.6.1.4. Scenario: Visualizzazione dei documenti allegati
[14](#scenario-visualizzazione-dei-documenti-allegati)](#scenario-visualizzazione-dei-documenti-allegati)

[4.6.1.5. Scenario: Download manuale dei documenti allegati
[14](#scenario-download-manuale-dei-documenti-allegati)](#scenario-download-manuale-dei-documenti-allegati)

[4.7. User Story "Verifiche del Back-Office"
[15](#user-story-verifiche-del-back-office)](#user-story-verifiche-del-back-office)

[4.7.1. Acceptance Criteria
[15](#acceptance-criteria-6)](#acceptance-criteria-6)

[4.7.1.1. Scenario: Controllo Presenza Verbale di Denuncia
[15](#scenario-controllo-presenza-verbale-di-denuncia)](#scenario-controllo-presenza-verbale-di-denuncia)

[4.7.1.2. Scenario: Controlli Conformità Verbale di Denuncia
[15](#scenario-controlli-conformità-verbale-di-denuncia)](#scenario-controlli-conformità-verbale-di-denuncia)

[4.7.1.3. Scenario: Specifiche Controllo di Conformità N. 2
[15](#scenario-specifiche-controllo-di-conformità-n.-2)](#scenario-specifiche-controllo-di-conformità-n.-2)

[4.7.1.4. Scenario: Specifiche Controllo di Conformità N. 4
[15](#scenario-specifiche-controllo-di-conformità-n.-4)](#scenario-specifiche-controllo-di-conformità-n.-4)

[4.7.1.5. Scenario: Salva e Modifica Checklist
[16](#scenario-salva-e-modifica-checklist)](#scenario-salva-e-modifica-checklist)

[4.7.1.6. Scenario: Selezione Automatica dell'Esito sul controllo del
Verbale di Denuncia
[16](#scenario-selezione-automatica-dellesito-sul-controllo-del-verbale-di-denuncia)](#scenario-selezione-automatica-dellesito-sul-controllo-del-verbale-di-denuncia)

[4.8. User Story "Chiusura di un Task nella Lista Attività"
[16](#user-story-chiusura-di-un-task-nella-lista-attività)](#user-story-chiusura-di-un-task-nella-lista-attività)

[4.8.1. Acceptance Criteria
[16](#acceptance-criteria-7)](#acceptance-criteria-7)

[4.8.1.1. Scenario: Selezione Automatica dell'Esito Generale della
Pratica
[16](#scenario-selezione-automatica-dellesito-generale-della-pratica)](#scenario-selezione-automatica-dellesito-generale-della-pratica)

[4.8.1.2. Scenario: Chiusura Task con Pratica Approvata
[16](#scenario-chiusura-task-con-pratica-approvata)](#scenario-chiusura-task-con-pratica-approvata)

[4.8.1.3. Scenario: Chiusura Task con Pratica Respinta
[16](#scenario-chiusura-task-con-pratica-respinta)](#scenario-chiusura-task-con-pratica-respinta)

[4.8.1.4. Scenario: Verifica Chiusura Task
[16](#scenario-verifica-chiusura-task)](#scenario-verifica-chiusura-task)

[4.8.1.5. Scenario: Cambio Stato della Pratica
[16](#scenario-cambio-stato-della-pratica)](#scenario-cambio-stato-della-pratica)

[4.9. Integration Story "Integrazione BPM: Invio Esito Verifiche BO"
[17](#integration-story-integrazione-bpm-invio-esito-verifiche-bo)](#integration-story-integrazione-bpm-invio-esito-verifiche-bo)

[4.9.1. Acceptance Criteria
[17](#acceptance-criteria-8)](#acceptance-criteria-8)

[4.9.1.1. Scenario: Invio Esiti Verifiche BO
[17](#scenario-invio-esiti-verifiche-bo)](#scenario-invio-esiti-verifiche-bo)

[4.9.1.2. Scenario: Esito con KO multipli
[17](#scenario-esito-con-ko-multipli)](#scenario-esito-con-ko-multipli)

[4.9.1.3. Scenario: Interrogazione fallita per indisponibilità del
servizio
[17](#scenario-interrogazione-fallita-per-indisponibilità-del-servizio-1)](#scenario-interrogazione-fallita-per-indisponibilità-del-servizio-1)

[4.10. User Story "Chiusura Pratica"
[17](#user-story-chiusura-pratica)](#user-story-chiusura-pratica)

[4.10.1. Acceptance Criteria
[17](#acceptance-criteria-9)](#acceptance-criteria-9)

[4.10.1.1. Scenario: Gestione Chiusura Pratica in OK
[17](#scenario-gestione-chiusura-pratica-in-ok)](#scenario-gestione-chiusura-pratica-in-ok)

[4.10.1.2. Scenario: Gestione Chiusura Pratica in KO
[17](#scenario-gestione-chiusura-pratica-in-ko)](#scenario-gestione-chiusura-pratica-in-ko)

[4.11. User Story "Funzionalità del Supervisore: Riassegna Attività"
[17](#user-story-funzionalità-del-supervisore-riassegna-attività)](#user-story-funzionalità-del-supervisore-riassegna-attività)

[4.11.1. Acceptance Criteria
[18](#acceptance-criteria-10)](#acceptance-criteria-10)

[4.11.1.1. Scenario: Tipologia di Riassegnazione
[18](#scenario-tipologia-di-riassegnazione)](#scenario-tipologia-di-riassegnazione)

[4.11.1.2. Scenario: Visualizzazione Task da riassegnare
[18](#scenario-visualizzazione-task-da-riassegnare)](#scenario-visualizzazione-task-da-riassegnare)

[4.11.1.3. Scenario: Filtri Task da riassegnare
[18](#scenario-filtri-task-da-riassegnare)](#scenario-filtri-task-da-riassegnare)

[4.11.1.4. Scenario: Sorting Colonne
[18](#scenario-sorting-colonne-1)](#scenario-sorting-colonne-1)

[5. Functional Definition Impacts
[19](#functional-definition-impacts)](#functional-definition-impacts)

[6. Assunzioni [20](#assunzioni)](#assunzioni)

[7. Open Point [21](#open-point)](#open-point)

[8. Ruoli [22](#ruoli)](#ruoli)

[9. Sistemi da integrare
[23](#sistemi-da-integrare)](#sistemi-da-integrare)

[10. Modello di processo
[24](#modello-di-processo)](#modello-di-processo)

[11. State Model [25](#state-model)](#state-model)

[12. Index of figures and tables
[26](#index-of-figures-and-tables)](#index-of-figures-and-tables)

# Acronyms, glossary

  -----------------------------------------------------------------------
  **Acronimo**     **Descrizione**
  ---------------- ------------------------------------------------------
  SD               Scrivania Digitale

  BO               Back Office

  BPM              Sistemi di Business Process Management
  -----------------------------------------------------------------------

  : []{#_Toc360181424 .anchor}Table 2 -- Acronyms, abbreviations

  -----------------------------------------------------------------------
  **Term**         **Meaning**
  ---------------- ------------------------------------------------------
                   

  -----------------------------------------------------------------------

  : []{#_Toc40969614 .anchor}Table 3 -- Glossary

# Overview

Scopo del progetto è lo sviluppo dell'integrazione tra BPM e Scrivania
Digitale per effettuare i controlli sulla veridicità del Verbale di
Denuncia di smarrimento/furto e sulla carta bloccata lato Back-Office,
al fine permettere al Cliente di procedere all'attivazione della nuova
carta in sostituzione.

In fase di collaudo dell'iniziativa è emerso che dal Front End non
arriva la tipizzazione del documento. Poiché su SD tale tipizzazione è
definita dal campo CODICE_DOC_ID, il valore acquisito sarà rielaborato
dal Sistema in modo tale che da interfaccia l'Operatore, a valle della
presa in carico del task, dovrà selezionare il tipo di documento
(Verbale di Denuncia o Carta) dopo averlo visualizzato tra gli allegati.

.

# Backlog and Iteration Functionalities

La realizzazione della presente iniziativa prevede lo sviluppo delle
seguenti funzionalità:

- *Integration Story "Integrazione BPM: Richiesta di Apertura Pratica"*

- *User Story "Creazione Pratica"*

- *User Story "Visualizzazione Lista Pratiche"*

- *User Story "Creazione di un Task nella Lista Attività"*

- *User Story "Visualizzazione Lista Attività"*

- *User Story "Presa in carico di un Task nella Lista Attività"*

- *User Story "Verifiche del Back-Office"*

- *User Story "Chiusura di un Task nella Lista Attività"*

- *Integration Story "Integrazione BPM: Invio Esito Verifiche BO"*

- *User Story "Chiusura Pratica"*

- *User Story "Funzionalità del Supervisore: Riassegna Attività"*

# Functional Definition

## Integration Story "Integrazione BPM: Richiesta di Apertura Pratica"

Come Sistema devo esporre un servizio per consentire al sistema esterno
chiamante BPM di inviare i dati necessari per la creazione di una
pratica. La pratica da creare dovrà essere corredata di un solo
documento da controllare (contenuto nell'oggetto "Documenti" del json) e
potrà essere di due tipologie

- Immagine del Verbale di Denuncia

> oppure

- Immagine della Carta tagliata da un lato

L'oggetto "Documenti" potrà contenere all'interno più "Contenuti" (più
file). Il BPM non effettua alcun controllo sui contenuti caricati dal
Cliente.

Per i dettagli dell'IA riferirsi al References \[3\].

### Acceptance Criteria 

#### Scenario: Ricezione richiesta Creazione Pratica 

Verificare che il Sistema riceva una richiesta di creazione Pratica che
contenga obbligatoriamente l'oggetto "DOCUMENTI" e risponda
correttamente al servizio chiamante.

#### Scenario: Response Servizio Apertura Pratica

Verificare che il Sistema restituisca nella response i seguenti valori:

- Info:

  - Result Code (obbligatorio)

  - Result Message (obbligatorio)

  - Details (facoltativo)

- appianTicketID (ovvero l'ID Pratica, obbligatorio in caso di Result
  Code "OK")

#### Scenario: Mancato invio oggetto "Documenti" 

Verificare che il Sistema restituisca errore in caso il json inviato dal
BPM per la creazione della pratica non contenga l'oggetto "Documenti" al
suo interno.

#### Scenario: Gestione dell'Idempotenza 

Verificare che il Sistema restituisca il seguente messaggio in caso
vengano inviati da BPM dati per la creazione di una pratica, relativi a
un ID_WORKITEM già presente su SD:

{ \"info\" : { \"resultCode\" : \"-5\", \"resultMessage\" :
\"L\'ID_WORKITEM indicato corrisponde a una pratica esistente con
idCase: XXXXX (*IDCASE esistente)*, \"details\" : null } }

#### Scenario: Interrogazione fallita per indisponibilità del servizio

Verificare che il Sistema restituisca un messaggio di indisponibilità
del servizio quando questo non è raggiungibile.

## User Story "Creazione Pratica"

A valle della corretta ricezione delle informazioni inviate dal sistema
chiamante (BPM), come Sistema devo creare una pratica, al fine di
renderla visibile e lavorabile dall'Operatore. La pratica creata potrà
contenere al suo interno un solo documento da controllare che potrà
essere:

- Immagine del Verbale di Denuncia

> oppure

- Immagine della Carta tagliata da un lato.

L'oggetto "Documenti" potrà contenere all'interno più "Contenuti" (più
file). SD non effettua alcun controllo sui contenuti caricati dal
Cliente.

### Acceptance Criteria

#### Scenario: Creazione Pratica 

Verificare che il Sistema abbia creato la pratica dal tab Pratiche, che
lo stato della pratica sia "Aperta" e che sia stato generato il task per
permettere all'Operatore di procedere con le verifiche lato BO.

#### Scenario: Riepilogo Pratica 

Verificare che il Sistema mostri la pratica suddivisa nelle seguenti
sezioni e con i seguenti dati:

- **DATI TESTATA**

<!-- -->

- Macrostep di lavorazione (Raccolta input, Lavorazione, Chiusura
  Pratica)

<!-- -->

- Processo (Attivazione Nuova Carta)

- Codice Fiscale

- Pratica N.

<!-- -->

- **DATI PRATICA**

<!-- -->

- Data Apertura

- Data Ultima modifica

- Stato

- Codice Cliente

- Codice Fiscale

- Canale

- Esito SD (campo mostrato e valorizzato a valle delle Verifiche BO,
  valori consentiti. OK, KO)

- Data Chiusura Pratica (campo mostrato e valorizzato a valle della
  chiusura pratica)

<!-- -->

- **DATI LAVORAZIONE**

  - **Colonna Sinistra**

  <!-- -->

  - **Dati Cliente**

  <!-- -->

  - Nome

  - Cognome

  - Sesso

  - Codice Fiscale

  - Data di Nascita

  - Comune di Nascita

  - Provincia di Nascita

  - Nazione di Nascita

  - Cittadinanza

  - Cellulare

  - Telefono

  - Indirizzo di Residenza\*

\*La sezione "Indirizzo di Residenza" in fase di apertura del tab DATI
LAVORAZIONE non sarà esplosa.

- **Dati Carta Bloccata**

  - Numero Carta (PAN)

  - Tipo di Carta

  - Intestazione Carta

<!-- -->

- **Contenuti Verbale di Denuncia** (Presente a valle della selezione
  dell'Operatore nel task del tipo documento "Verbale dei Denuncia")

  - Nome File

  - Estensione

<!-- -->

- **Controlli Verbale di Denuncia** (Presente a valle della selezione
  dell'Operatore nel task del tipo documento "Verbale dei Denuncia")

> La sezione presenta una checklist per le verifiche, effettuate o da
> effettuare, del Back-Office sul Verbale di Denuncia.
>
> Per la checklist delle verifiche del BO fare riferimento al References
> \[4\].

- **Contenuti Carta** (Presente a valle della selezione dell'Operatore
  nel task del tipo documento "Carta")

  - Nome File

  - Estensione

<!-- -->

- **Controlli Carta** (Presente a valle della selezione dell'Operatore
  nel task del tipo documento "Carta")

> La sezione presenta una checklist per le verifiche, effettuate o da
> effettuare, del Back-Office sulla Carta.
>
> Per la checklist delle verifiche del BO fare riferimento al References
> \[4\].

- **Colonna Destra:**

<!-- -->

- Sezione di Visualizzazione Allegato Verbale di Denuncia o Allegato
  Carta.

<!-- -->

- **ESITO**

> La sezione contiene l'esito positivo o negativo dei controlli
> effettuati dal BO, la data di esitazione e la sezione "Note"
> (valorizzata o meno poiché non obbligatoria) visualizzata solo in caso
> di respingimento pratica.

#### Scenario: Funzionalità di Visualizzazione per la Navigazione

Verificare che lo Specialista, nel tab "Dati Lavorazione", possa
nascondere o visualizzare le diverse sezioni attraverso le seguenti
funzionalità:

- Nascondi Sezione (Dati)

- Visualizza Sezione (Dati)

- Nascondi Allegati

- Visualizza Allegati

## User Story "Visualizzazione Lista Pratiche" 

Come Operatore BO devo poter visualizzare tutte le pratiche censite a
sistema.

### Acceptance criteria

#### Scenario: Visualizzazione Lista Pratiche

Verificare che l'utente visualizzi i seguenti campi in colonna:

- Pratica N°: identificativo pratica Appian

- Codice Fiscale

- Codice Cliente

- Data Apertura: data in cui la pratica è stata aperta su Scrivania

- Data Ultima Modifica

- Data Chiusura

- 

- Data Inserimento Richiesta

- Esito SD

- Operatore

- Stato pratica come da state model

- Segnalazioni

#### Scenario: Filtro Lista Pratiche 

Verificare che le pratiche possano essere filtrate per:

- Campo per ricerca Pratiche di *Attivazione Nuova Carta*

- Filtro per Stato Pratica, saranno presenti nel menu a tendina i
  seguenti stati:

  - Aperta

  - In Lavorazione

  - In Attesa Conferma BPM

  - Chiusa OK

  - Chiusa KO

- Filtro per Data Chiusura, consente all'utente di filtrare per "Data
  Chiusura", selezionando (tramite calendario) una data di inizio e una
  data di fine per impostare il range di tempo di interesse.

- Filtro per Data Apertura

- Filtro per Data ultima Modifica

- Filtro per Esito SD

## User Story "Creazione di un Task nella Lista Attività"

Come Sistema devo generare un task, nella Lista Attività, a valle
dell'apertura pratica, per permettere all'Operatore di procedere con la
lavorazione.

### Acceptance Criteria

#### Scenario: Generazione del Task 

Verificare che, a valle della creazione di una pratica con stato
"Aperta", il Sistema generi il task denominato:

*Attivazione Nuova Carta - 'Nome Cliente' 'Cognome Cliente'*

nella lista Attività, per permettere all'operatore di procedere con le
verifiche del Back- Office.

## User Story "Visualizzazione Lista Attività"

Come Operatore BO devo poter visualizzare la lista delle attività
presenti nella mia coda lavorazioni per poter procedere con presa in
carico del task.

### Acceptance criteria

#### Scenario: Visualizzazione Lista Attività 

Verificare che l'utente visualizzi i seguenti campi in colonna:

- Pratica N.

- Attività

- Assegnatari

- Utente In Carico

- Data Creazione

- Data Presa in carico

- Stato

#### Scenario: Sorting Colonne

Verificare che il Sistema permetta di ordinare le colonne disponibili,
ad esclusione della colonna "Assegnatari".

#### Scenario: Filtro Attività

Verificare che l'utente possa filtrare la lista attività inserendo i
seguenti filtri:

- Stato

- Tipo Pratica (popolato con "Attivazione Nuova Carta", non editabile)

- Pratica N.

- Nome Attività

- Assegnatari

- Utente in carico

## User Story "Presa in carico di un Task nella Lista Attività"

Come Operatore BO devo poter prendere in carico l'Attività per poter
procedere alla lavorazione della pratica. Per ogni Task devo poterne
visualizzare il dettaglio e l'allegato.

### Acceptance Criteria

#### Scenario: Presa in Carico del Task Attività

Verificare che lo Specialista possa prendere in carico l'Attività per
poterla lavorare, tramite la funzionalità apposita.

#### Scenario: Selezione Tipologia Documento da verificare

Verificare che l'Operatore, a valle della presa in carico del task,
debba obbligatoriamente selezionare il Tipo di Documento da sottoporre a
controlli (Verbale di Denuncia o Carta) per procedere nella lavorazione.

Poiché l'oggetto "Documenti" potrà contenere all'interno più "Contenuti"
(più file) ed in caso il Cliente abbia caricato sia il file del Verbale
di Denuncia che il file della Carta, sarà l'Operatore a decidere su
quale documento dovrà essere effettuato il controllo del Back Office.
Dopo aver confermato la selezione del "Tipo Documento", non sarà più
possibile modificarlo e la pratica acquisirà lo stato "In Lavorazione".

#### Scenario: Visualizzazione Dettaglio Task Attività

Verificare che lo Specialista possa visualizzare le seguenti sezioni con
il seguente dettaglio:

- **DATI TESTATA**

<!-- -->

- Macrostep di lavorazione (Raccolta input, Lavorazione, Chiusura
  Pratica)

<!-- -->

- Processo (Attivazione Nuova Carta)

- Codice Fiscale

- Pratica N.

<!-- -->

- **DATI PRATICA**

  - **Dati Pratica**

  <!-- -->

  - Data Apertura

  - Data Ultima modifica

  - Stato

  - Codice Cliente

  - Codice Fiscale

  - Canale

- **VERIFICA DOCUMENTI**

  - **Dati Cliente**

<!-- -->

- Nome

- Cognome

- Sesso

- Codice Fiscale

- Data di Nascita

- Comune di Nascita

- Provincia di Nascita

- Nazione di Nascita

- Cittadinanza

- Cellulare

- Telefono

<!-- -->

- Indirizzo di Residenza\*

\* La sezione "Indirizzo di Residenza" in fase di apertura task non sarà
esplosa.

- **Dati Carta**

<!-- -->

- Numero Carta (PAN)

- Tipo di Carta

- Intestazione Carta

<!-- -->

- **Contenuti Verbale di Denuncia** (Presente a valle della selezione
  del tipo documento "Verbale dei Denuncia")

<!-- -->

- Nome File

- Estensione

<!-- -->

- **Controlli Verbale di Denuncia** (Presente a valle della selezione
  del tipo documento "Verbale dei Denuncia")

> La sezione presenta una checklist per le verifiche del Back-Office sul
> Verbale di Denuncia.
>
> Per la checklist delle verifiche del BO fare riferimento al References
> \[4\].

- **Contenuti Carta** (Presente a valle della selezione del tipo
  documento "Carta")

<!-- -->

- Nome File

- Estensione

<!-- -->

- **Controlli Carta** (Presente a valle della selezione del tipo
  documento "Carta")

> La sezione presenta una checklist per le verifiche del Back-Office
> sulla Carta.
>
> Per la checklist delle verifiche del BO fare riferimento al References
> \[4\].

- **Sezione di Visualizzazione Allegati** (colonna a destra)

> La sezione presenta l'allegato del Verbale di Denuncia o della Carta
> per la consultazione visiva da parte del Back-Office e la verifica
> rispetto ai dati nella colonna a sinistra.

- **RIEPILOGO**

> La sezione, abilitata solo a completamento dei controlli lato BO,
> contiene il repilogo delle Verifiche del BO e le seguenti card in sola
> lettura evidenziate in base alle logiche di automatismo del Sistema
> sui controlli effettuati:

- Approvata

- Respinta

> Sarà inoltre visibile la sezione "Note", non obbligatoria, solo in
> caso di respingimento pratica ed il tasto "Chiudi Pratica" per
> procedere con la comunicazione dell'esito Verifiche BO a BPM e la
> chiusura del task e della lavorazione lato Operatore BO.

#### Scenario: Visualizzazione dei documenti allegati

Verificare che lo Specialista possa visualizzare correttamente
l'allegato alla pratica.

#### Scenario: Download manuale dei documenti allegati

Verificare che lo Specialista, in caso di mancata visualizzazione
dell'allegato, possa effettuare il download manuale del documento
tramite apposita funzionalità.

## User Story "Verifiche del Back-Office"

Come Specialista devo poter effettuare delle verifiche sul Verbale di
Denuncia o sulla Carta, ed indicare se il documento è conforme per poter
procedere nella lavorazione. In caso di non conformità devo poter
inserire facoltativamente una nota.

Per le causali di KO da inviare al BPM fare riferimento al References
\[2\].

Per la checklist delle verifiche del BO fare riferimento al References
\[4\]

Per gli Automatismi di SD fare riferimento al References \[6\].

### Acceptance Criteria

#### Scenario: Controllo Presenza Verbale di Denuncia

Verificare che il Sistema permetta allo Specialista di indicare la
presenza del Verbale di Denuncia. In caso positivo si abiliterà la
colonna "Conformità" per poter esprimere l'esito sui controlli. In caso
negativo la colonna verrà disabilitata e tutti i Controlli di Conformità
Documento andranno in KO. Di default il radio button non è valorizzato e
la colonna Conformità è disabilitata.

#### Scenario: Controlli Conformità Verbale di Denuncia

Verificare che il Sistema permetta allo Specialista di indicare per il
Verbale di Denuncia la conformità o meno in merito tramite i seguenti
controlli (fare riferimento al References \[4\]):

1.  "Il Verbale di Denuncia risulta leggibile?"

2.  "Il Verbale di Denuncia è idoneo al controllo formale?"

3.  "I dati del Verbale di Denuncia sono coerenti con i dati del
    Cliente?"

4.  "Il numero della carta nel Verbale di Denuncia corrisponde al numero
    di carta presente tra i dati della pratica?"

Di default i radio button della checklist non sono valorizzati.

#### Scenario: Specifiche Controllo di Conformità N. 2

Verificare che, in caso di non Conformità del controllo

"Il Verbale di Denuncia è idoneo al controllo formale?"

il Sistema permetta allo Specialista di selezionare una delle seguenti
voci per cui il controllo risultato in KO:

- Intestazione

- Firme

- Intestazione Conforme al Timbro

- Dichiarazione Conforme alle Firme

- Carta Poste Italiane

Ad ognuna delle voci indicate corrisponde un codice motivazione
specifico da inviare al BPM per informare il Cliente del motivo di
respingimento pratica.

#### Scenario: Specifiche Controllo di Conformità N. 4

Verificare che il controllo

"Il numero della carta nel Verbale di Denuncia corrisponde al numero di
carta presente tra i dati della pratica?"

sia facoltativo per l'Operatore, in quanto dovrà essere compilato solo
in caso di presenza numero di Carta sul Verbale di Denuncia.

#### Scenario: Salva e Modifica Checklist

Verificare che il Sistema permetta all'Operatore BO di salvare (tasto
SALVA E PROSEGUI) e modificare (tasto MODIFICA) la checklist compilata.
Solo alla pressione del tasto SALVA E PROSEGUI si abiliterà la sezione
RIEPILOGO.

#### Scenario: Selezione Automatica dell'Esito sul controllo del Verbale di Denuncia

Verificare che il Sistema, a valle dei controlli effettuati
dall'Operatore BO, selezioni in automatico l'esito sul controllo del
Verbale di Denuncia o sulla Carta.

## User Story "Chiusura di un Task nella Lista Attività"

Come Specialista devo poter chiudere un Task, ovvero respingere la
pratica se i controlli non hanno dato esito positivo o approvarla in
caso contrario.

### Acceptance Criteria

#### Scenario: Selezione Automatica dell'Esito Generale della Pratica

Verificare che il sistema, a valle dei controlli effettuati
dall'Operatore BO, selezioni in automatico l'esito generale della
pratica (OK, KO), in base agli automatismi riportati nel References
\[6\].

#### Scenario: Chiusura Task con Pratica Approvata

Verificare che il sistema, a valle delle Verifiche BO, evidenzi la card
in sola lettura "Approvata" e mostri:

- Esito Controllo sul Verbale di Denuncia o sulla Carta;

- Il tasto "Chiudi Pratica" per chiudere il task.

#### Scenario: Chiusura Task con Pratica Respinta

Verificare che il sistema, a valle delle Verifiche BO, evidenzi la card
in sola lettura "Respinta" (KO) e mostri:

- Esito Controllo sul Verbale di Denuncia o sulla Carta e la/le
  Motivazione/i per i controlli andati in KO;

- Una nota da valorizzare in maniera facoltativa da parte dell'Operatore
  BO

- Il tasto "Chiudi Pratica" per chiudere il task.

#### Scenario: Verifica Chiusura Task

Verificare che, a valle della chiusura del Task, questo non sia più
presente nella Lista delle Attività.

#### Scenario: Cambio Stato della Pratica 

Verificare che, a valle della chiusura del Task, lo stato della pratica
cambi da "In Lavorazione" a "In Attesa Conferma BPM".

## Integration Story "Integrazione BPM: Invio Esito Verifiche BO"

Come Sistema devo poter invocare il servizio esposto da BPM per poter
inviare gli esiti delle verifiche da parte del Back-Office al BPM.

Per i dettagli tecnici della IA riferirsi al References
[\[5\]](#_Ref93566583)

### Acceptance Criteria

#### Scenario: Invio Esiti Verifiche BO

Verificare che il Sistema comunichi correttamente gli esiti di verifiche
BO al BPM.

#### Scenario: Esito con KO multipli

Verificare che il Sistema, in caso di più KO riscontrati dall'Operatore,
invii correttamente tutte le motivazioni di KO riscontrate al BPM.

#### Scenario: Interrogazione fallita per indisponibilità del servizio

Verificare che il Sistema riceva un messaggio di indisponibilità del
servizio quando questo non è raggiungibile.

## User Story "Chiusura Pratica"

Affinché il sistema possa procedere con la chiusura della pratica,
l'Operatore dovrà cliccare sul tasto "Chiudi Pratica" del task a fine
lavorazione.

Come Sistema, a valle della conferma ricezione esiti da parte di BPM,
devo poter gestire la chiusura della Pratica che potrà essere delle
seguenti tipologie:

- Chiusa OK

- Chiusa KO

### Acceptance Criteria

#### Scenario: Gestione Chiusura Pratica in OK

Verificare che il Sistema, a valle della conferma ricezione esiti da
parte di BPM, proceda con la chiusura della Pratica, modifichi lo stato
della pratica in "Chiusa OK" e valorizzi la data chiusura con la
sysdate.

#### Scenario: Gestione Chiusura Pratica in KO 

Verificare che il Sistema, a valle della conferma ricezione esiti da
parte di BPM, proceda con la chiusura della Pratica, modifichi lo stato
della pratica in "Chiusa KO" e valorizzi la data chiusura con la
sysdate.

## User Story "Funzionalità del Supervisore: Riassegna Attività"

Come Supervisore devo poter devo poter visualizzare tutte le Attività e
devo poterle riassegnare alla coda del gruppo o ad un utente del gruppo.

### Acceptance Criteria

#### Scenario: Tipologia di Riassegnazione 

Verificare che il Supervisore possa scegliere di:

- Riassegnare Attività ad un gruppo

- Riassegnare Attività ad Utenti

#### Scenario: Visualizzazione Task da riassegnare

Verificare che l'utente visualizzi per ogni attività i seguenti campi in
colonna:

- Processo

- Pratica N.

- Nome Attività

- Assegnatario

- Owner

- Data Assegnazione

- Data Presa in carico

- Data Scadenza

- Stato

#### Scenario: Filtri Task da riassegnare

Verificare che i task da riassegnare possano essere filtrati per:

- Pratica N.

- Data Assegnazione

- Data Scadenza

- Owner

- Assegnatario

#### Scenario: Sorting Colonne

Verificare che il Sistema permetta di ordinare le colonne disponibili,
ad esclusione della colonna "Assegnatario".

# Functional Definition Impacts

L'iniziativa descritta nel presente Product Backlog ha impatti anche su
Sinergia SAC-SBO (PIX) e Data Lake.

# Assunzioni

  -------------------------------------------------------------------------
  **ID**   **Topic**       **Descrizione**
  -------- --------------- ------------------------------------------------
  **1**                    

  **2**                    

  **3**                    

  **4**                    
  -------------------------------------------------------------------------

# Open Point

  --------------------------------------------------------------------------------
  **ID**   **Topic**   **Descrizione**                         **Aggiornamento**
  -------- ----------- --------------------------------------- -------------------
  **1**                                                        

  **2**                                                        

  **3**                                                        

  **4**                                                        

  **5**                                                        

  **6**                                                        

  **7**                                                        

  **8**                                                        
  --------------------------------------------------------------------------------

# Ruoli 

![](media/image7.png){width="6.492361111111111in"
height="3.671527777777778in"}

# Sistemi da integrare

![](media/image8.png){width="6.492361111111111in"
height="3.6694444444444443in"}

# Modello di processo 

Di seguito viene mostrato il workflow del processo.

![](media/image9.png){width="6.492361111111111in"
height="3.6729166666666666in"}

# State Model

Lo state model del flusso presenta i seguenti Stati.

![](media/image10.png){width="6.492361111111111in"
height="3.6631944444444446in"}

# Index of figures and tables

Figures

No table of figures entries found.

Tables

[Table 1 -- Version Control [2](#_Toc40969612)](#_Toc40969612)

[Table 2 -- Acronyms, abbreviations [4](#_Toc360181424)](#_Toc360181424)

[Table 3 -- Glossary [4](#_Toc40969614)](#_Toc40969614)
