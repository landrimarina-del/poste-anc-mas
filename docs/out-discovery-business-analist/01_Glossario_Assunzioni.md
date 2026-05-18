# 01 - Glossario e Assunzioni

> Fonti autoritative (`docs/requirements/source-of-truth/`):> - `Attivazione nuova carta_Discovery.md` (Documento Discovery ANC \u2014 baseline funzionale)> - `126440 - Poste_BOA - Product Backlog - Attivazione Nuova Carta_v1.3.md` (Product Backlog ANC v1.3)
> - `InterfaceAgreement.md` (Interface Agreement Servizi BPM ↔ SD)
> - `BPM_SubmitWorkItem_v1.1.yaml` (contratto SubmitWorkItem v1.1)
> - `Linee guida controlli Backoffice.md` (checklist controlli BO)
> - `automatismiEsiti.xlsx` (automatismi calcolo esito)
> - `checklistSD.xlsx` (checklist SD)
> - `matriceControlli.xlsx` (matrice controlli/esiti)
>
> Baseline: comportamento esistente della soluzione "Attivazione Nuova Carta" (ANC) sulla Scrivania Digitale (SD) di Poste Italiane.
> Obiettivo del porting: **ricostruzione open source** della Scrivania Digitale preservando il comportamento funzionale esistente.

## 1. Termini Business (dominio operativo)

| Termine | Definizione |
|---|---|
| ANC (Attivazione Nuova Carta) | Processo di Back Office per attivare una carta sostitutiva a seguito di blocco per furto/smarrimento. |
| Pratica | Istanza di lavorazione creata in SD a fronte di una richiesta BPM. Identificata da Pratica N° (oggi emessa dalla runtime Appian; nel porting generata internamente dall'applicazione open source). |
| Task / Attività | Singola unità di lavoro generata automaticamente per ogni nuova pratica, presente in "Lista Attività". |
| Verbale di Denuncia | Documento ufficiale di furto/smarrimento da verificare. |
| Carta (tagliata) | Immagine della carta bloccata, oggetto di verifica conformità. |
| Tipizzazione Documento | Azione manuale dell'operatore: dichiara se l'allegato è Verbale o Carta. Irreversibile. |
| Checklist di Conformità | Insieme di controlli standardizzati (leggibilità, firme, timbri, ecc.) eseguiti dall'operatore. |
| Esito SD | Risultato finale calcolato automaticamente da SD (OK / KO) sulla base della checklist. |
| KO Multipli | Pratica respinta per più motivi: tutti i codici di KO vengono inviati a BPM. |
| Presa in Carico | Azione di "ACCETTA" che assegna il task all'operatore e cambia stato pratica in "In Lavorazione". |
| Lista Attività (Coda Lavorazioni) | Vista dei task assegnati o disponibili per l'operatore. |
| Dashboard Segnalazioni | Modulo per inoltrare segnalazioni verso il sistema esterno Sinergia (PIX). |
| Riassegnazione | Azione del Supervisore: spostare un task tra gruppo/utente. |

## 2. Termini Tecnici e Integrazioni

| Termine | Definizione |
|---|---|
| BPM | Business Process Management: sistema esterno orchestratore. Invia richieste e riceve esiti. |
| SD (Scrivania Digitale) | Applicazione di Back Office, oggetto del porting. |
| BOA | Back Office Automation: ambito tecnologico Poste. |
| Appian | **Piattaforma low-code legacy** su cui è attualmente implementata la Scrivania Digitale ANC. È la sorgente del porting, NON un sistema esterno con cui integrarsi nel target. |
| requestId | Identificativo univoco della richiesta/pratica generato al recepimento della richiesta sulla nuova piattaforma open source. **Chiave primaria di riconciliazione** tra BPM, DataLake e Sinergia (PIX). |
| Porting open source | Ricostruzione della Scrivania Digitale su tecnologie open source preservando il comportamento funzionale esistente e i contratti d'interfaccia (incluso `requestId`) verso BPM, DataLake, Sinergia. La definizione dello stack è demandata all'Architect. |
| Front End (FE) | Canale cliente che carica i documenti (verbale o carta). Non fornisce tipizzazione automatica. |
| ID_WORKITEM | Identificativo univoco inviato da BPM, base per controllo idempotenza. |
| CODICE_DOC_ID | Codice del documento allegato (valori ammessi: 1, 2, 3). |
| Sinergia / PIX (SAC-SBO) | Sistema esterno per gestione segnalazioni e ticket. |
| Data Lake | Sistema di archiviazione/analytics impattato. |
| Interface Agreement (IA) | Documento contrattuale BPM ↔ SD (`InterfaceAgreement.md`) che definisce il payload di apertura pratica e la risposta. |
| SubmitWorkItem 1.1 | Servizio (`BPM_SubmitWorkItem_v1.1.yaml`) per invio esito SD verso BPM. |
| Idempotenza | Logica che impedisce creazione di pratiche duplicate per ID_WORKITEM esistente. |

## 3. Attori

### Attori Umani
- **Operatore BO / Specialista ANC**: presa in carico, tipizzazione, checklist, chiusura pratica.
- **Supervisore ANC**: monitoraggio, riassegnazione task, governance segnalazioni Sinergia.
- **Cliente**: indirettamente, carica i documenti tramite Front End (non interagisce con SD).

### Attori di Sistema
- **Scrivania Digitale (SD)** *(nel porting: applicazione open source; oggi: implementata su Appian)*: crea pratica/task, calcola esito, invia esito a BPM. Nel porting genera internamente l'identificativo Pratica N° (oggi prodotto dalla runtime Appian).
- **BPM**: invia richieste apertura, riceve esiti, conferma chiusura.
- **Sinergia (PIX)**: ricezione segnalazioni e ticket.
- **Data Lake**: ricezione dati per archiviazione/analisi.

## 4. Scope

### IN SCOPE (Baseline esistente da preservare)
- Porting della Scrivania Digitale ANC dalla piattaforma Appian a tecnologie open source, **preservando il comportamento funzionale** descritto nel Discovery.
- Integrazione BPM ↔ SD: apertura pratica e invio esito.
- Ciclo di vita pratica: Aperta → In Lavorazione → In Attesa Conferma BPM → Chiusa OK/KO.
- Lista Attività con presa in carico esplicita ("ACCETTA").
- Tipizzazione manuale documento (Verbale / Carta) — irreversibile.
- Checklist conformità (Verbale e Carta), salvataggio bozza, modifica, calcolo esito automatico.
- Causali KO formali (Intestazione, Firme, Timbro, Dichiarazione, Carta Poste Italiane).
- Visualizzatore allegati integrato con fallback download manuale.
- Lista Pratiche con filtri (stato, date, esito), ordinamento, export Excel, paginazione.
- Dettaglio Pratica: Riepilogo, Cronologia, Stati, Azioni Correlate.
- Profilo Supervisore: Riassegna Attività (a gruppo o utente), monitoraggio.
- Dashboard Segnalazioni verso Sinergia (PIX).
- Reporting Home: contatori real-time, istogrammi (Pratiche Giornaliere, Lavorate, per Stato).
- Home Page: tab Home/Attività/Pratiche, link favoriti, widget posta elettronica.
- Help in linea checklist ("Mostra Descrizione").

### OUT OF SCOPE (POC)
- Ridisegno funzionale o nuove capability non presenti.
- Tipizzazione automatica lato Front End.
- Migrazione dati storici dalla piattaforma Appian legacy.
- Coesistenza/strangler con l'istanza Appian in produzione.
- Logica di business interna a BPM, Sinergia, Data Lake.
- Autenticazione SSO enterprise (in POC: login locale minimale).
- Reportistica avanzata oltre i 3 istogrammi mensili.
- Mobile / responsive enterprise.

## 5. Vincoli funzionali (baseline)

- **V1 – Idempotenza**: ID_WORKITEM duplicato → errore (`resultCode: -5`), nessuna creazione.
- **V2 – Documenti obbligatori**: richiesta BPM senza oggetto `DOCUMENTI` → rifiutata.
- **V3 – Validazione CODICE_DOC_ID**: valori ammessi 1,2,3; altrimenti `resultCode: -4`.
- **V4 – Singola tipologia**: ogni pratica gestisce un solo tipo (Verbale OPPURE Carta).
- **V5 – Tipizzazione irreversibile**: una volta confermata, non modificabile.
- **V6 – Presa in carico obbligatoria**: senza "ACCETTA" non si lavora la pratica.
- **V7 – Esito automatico**: calcolato dal sistema, non forzabile manualmente.
- **V8 – Cascata KO**: documento "non presente" → tutti controlli automaticamente KO.
- **V9 – Causale obbligatoria**: se "idoneità formale" = NO → causale obbligatoria (Intestazione/Firme/Timbro/Dichiarazione/Carta Poste Italiane).
- **V10 – KO Multipli**: tutti i motivi devono essere comunicati a BPM.
- **V11 – Stato finale condizionato**: chiusura definitiva (Chiusa OK/KO) solo dopo conferma BPM.
- **V12 – Note solo in caso di Respinta**: facoltative.
- **V13 – Riassegnazione**: solo profilo Supervisore.

## 6. Assunzioni

- **A0**: Il porting è **rebuild open source** della Scrivania Digitale ANC oggi su Appian. La piattaforma Appian è sorgente di analisi, **non un sistema esterno** con cui integrarsi nel target. L'identificativo "Pratica N°" — oggi emesso dalla runtime Appian — sarà generato dall'applicazione target.
- **A1**: La POC ricostruisce funzionalmente la baseline; non sono richieste integrazioni reali con BPM/Sinergia/DataLake → si useranno **stub/mock** lato porting.
- **A2**: Autenticazione: due ruoli funzionali (`OPERATORE`, `SUPERVISORE`). Le scelte tecnologiche (provider IAM, fallback locale) sono demandate all'Architect.
- **A3**: Le scelte tecnologiche su persistenza, storage allegati, framework FE/BE, runtime e integrazione sono **demandate all'Architect**. Il BA fissa esclusivamente requisiti funzionali e contratti d'interfaccia esistenti.
- **A4**: Le API verso BPM verranno simulate con stub che rispettano il contratto funzionale (campi e payload) dell'Interface Agreement esistente (`InterfaceAgreement.md`).
- **A5**: I codici causale KO e i resultCode (-4, -5) sono trattati come baseline immutabile.
- **A6**: La UX a due colonne (dati+checklist a sinistra, viewer a destra) è vincolante.
- **A7**: I 3 istogrammi mensili e i contatori real-time sono parte CORE della Home Supervisore.
- **A8**: La Dashboard Segnalazioni Sinergia in POC è un modulo "skeleton" (CRUD locale + simulazione invio).
- **A9**: Il widget posta elettronica e i link favoriti sono OPTIONAL_POC.
- **A10**: La conservazione su Data Lake è OUT_OF_SCOPE per la POC (placeholder).

## 7. Schema funzionale payload apertura pratica (BPM → SD)

Estratto dall'Interface Agreement (`InterfaceAgreement.md`, sezione "Logica del servizio Interfaccia d'input"). Il BA riporta i campi senza introdurne di nuovi; il mapping tecnico verso lo storage e i nomi delle proprietà in formato camelCase è a cura dell'Architect.

### 7.1 Testata

| Campo (IA) | Tipo | Obbl. | Descrizione |
|---|---|---|---|
| CANALE | String | O | Canale da cui è stata richiesta la verifica |
| ID_WORKITEM | String | O | Id processo BPM, base dell'idempotenza (V1) e usato per la callback esito |
| NUM_PRATICA | String | O | Numero Pratica lato BPM |
| CF_CLIENTE | String | O | Codice Fiscale del Cliente |
| CODICE_CLIENTE | String | F | Identificativo cliente AUC |
| DATA_INSERIMENTO_RICHIESTA | String | O | Data/ora richiesta cliente, formato `dd/mm/yyyy hh:mm:ss` fuso italiano |
| CLIENTE | Object | O | Vedi 7.2 |
| DATI_CARTA_BLOCCATA | Object | O | Vedi 7.3 |
| DOCUMENTI | Object | O | Vedi 7.4 (V2) |

### 7.2 CLIENTE

| Campo (IA) | Tipo | Obbl. | Descrizione |
|---|---|---|---|
| NOME | String | O | Nome del cliente |
| COGNOME | String | O | Cognome del cliente |
| SESSO | String | F | Sesso |
| DATANASCITA | String | O | Data di nascita |
| COMUNENASCITA | String | O | Comune di nascita |
| PROVINCIANASCITA | String | O | Provincia di nascita |
| NAZIONENASCITA | String | O | Nazione di nascita |
| CITTADINANZA | String | O | Cittadinanza |
| CELLULARE | String | F | Numero cellulare |
| TELEFONO | String | F | Numero telefono |
| INDIRIZZO_DI_RESIDENZA | Object | F | Vedi 7.2.1 |

#### 7.2.1 INDIRIZZO_DI_RESIDENZA

| Campo (IA) | Tipo | Obbl. | Descrizione |
|---|---|---|---|
| LUOGO | String | F | DUG, toponimo e civico |
| COMUNE | String | F | Comune di residenza |
| PROVINCIA | String | F | Provincia di residenza |
| NAZIONE | String | F | Nazione di residenza |
| CAP | String | F | CAP |
| CIVICO | String | F | Numero civico |

### 7.3 DATI_CARTA_BLOCCATA

| Campo (IA) | Tipo | Obbl. | Descrizione |
|---|---|---|---|
| I_NUMERO_CARTA | String | O | PAN della carta bloccata, in chiaro |
| I_TIPO_CARTA | String | O | Tipologia carta |
| I_INTEST_CARTA | String | F | Intestazione carta |

### 7.4 DOCUMENTI

| Campo (IA) | Tipo | Obbl. | Descrizione / Dominio |
|---|---|---|---|
| CODICE_DOC_ID | Int | O | Valori ammessi {1, 2, 3} (V3). Mapping descrittivo a Verbale/Carta/combinazione: vedi `07_Risk_Open_Questions.md` R2 |
| CONTENUTI | Lista&lt;Object&gt; | O | Elenco allegati; vedi 7.4.1 |

#### 7.4.1 CONTENUTI (per ogni allegato)

| Campo (IA) | Tipo | Obbl. | Descrizione / Dominio |
|---|---|---|---|
| NOME_FILE | String | O | Nome del file allegato |
| ESTENSIONE | String | O | Valori ammessi: `pdf`, `jpeg`, `png`, `jpg` |
| ID_DOC | String | O | Identificativo univoco del documento da scaricare |
| LINKDOWNLOAD | String | O | Link per il download del file binario |

### 7.5 Risposta funzionale

| Esito | resultCode | Significato funzionale |
|---|---|---|
| OK | `0` | Pratica creata; nel corpo è restituito il numero Pratica generato dalla SD |
| KO | `-4` | Messaggio in ingresso non valido (es. `DOCUMENTI` mancante, `CODICE_DOC_ID` non in {1,2,3}, campi obbligatori assenti) |
| KO | `-5` | `ID_WORKITEM` già noto: pratica già aperta per quel work-item (idempotenza, V1) |
