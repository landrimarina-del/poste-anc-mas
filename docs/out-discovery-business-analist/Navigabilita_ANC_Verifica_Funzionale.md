# Guida alla Navigazione Applicazione ANC — Verifica Conformità Funzionale

**Versione**: 1.0  
**Data**: 2026-05-27  
**Autore**: Discovery Business Analyst  
**Obiettivo**: Descrivere la navigabilità dell'applicazione `apps/kogito` per ruolo **Operatore** e **Supervisore**, a supporto della verifica che il porting tecnico abbia mantenuto invariati i requisiti funzionali originari.  
**Fonte di verità**: Attivazione nuova carta_Discovery.md, Product Backlog v1.3  
**URL applicazione (locale)**: http://localhost:81

---

## Prerequisiti

| Credenziale | Ruolo | Username di esempio |
|-------------|-------|---------------------|
| Operatore ANC | `OPERATORE` / `OPERATORE_ANC` | operatore1 |
| Supervisore ANC | `SUPERVISORE` / `SUPERVISORE_ANC` | supervisore1 |

---

## 1. Accesso al Sistema (Login)

**Percorso**: `http://localhost:81/login`  
**Requisito originario**: L'operatore accede alla Scrivania Digitale inserendo le proprie credenziali nella pagina di login.

### Navigazione

1. Aprire il browser su `http://localhost:81`  
2. Il sistema reindirizza automaticamente a `/login`  
3. Inserire username e password  
4. Cliccare **ACCEDI**  
5. Il sistema reindirizza a `/home` in base al ruolo autenticato

### Cosa verificare

- [ ] Il login con credenziali valide reindirizza alla Home Page
- [ ] Il login con credenziali non valide mostra un messaggio di errore
- [ ] Accedendo a una rotta protetta senza login, il sistema reindirizza a `/login`

---

## 2. Home Page

**Percorso**: `/home`  
**Accesso**: tutti i ruoli autenticati  
**Requisito originario**: Pagina di atterraggio dopo il login con panoramica operativa, contatori in tempo reale, tab di navigazione principale.

### Navigazione

1. Dopo il login, la Home Page è la schermata di atterraggio
2. In alto sono presenti i **contatori dinamici** (Attività in coda, Pratiche attive, Pratiche chiuse)
3. La Home Page presenta i **grafici statistici** (Pratiche Giornaliere, Pratiche Giornaliere Lavorate, Pratiche per Stato)
4. È presente la sezione **Link Favoriti** con possibilità di aggiungere, modificare ed eliminare link

### Cosa verificare

- [ ] I contatori mostrano valori in tempo reale
- [ ] I grafici istogrammi sono visualizzati (Pratiche Giornaliere, Lavorate, per Stato)
- [ ] La sezione Link Favoriti consente di aggiungere un link (Titolo, URL, Tipo: Interno/Esterno/Legacy)
- [ ] I tab di navigazione **Home**, **Attività**, **Pratiche** sono accessibili dalla barra superiore

---

## 3. Percorso Operatore

### 3.1 Lista Attività (Coda Lavorazioni)

**Percorso**: `/attivita`  
**Ruoli**: `OPERATORE`, `OPERATORE_ANC`  
**Requisito originario**: Schermata dedicata alla gestione operativa quotidiana. Mostra i task in coda o in lavorazione associati allo specialista o al suo gruppo.

#### Navigazione

1. Cliccare sul tab **"Attività"** nella barra di navigazione
2. La lista mostra i task disponibili con colonne: Numero Pratica, Stato, Data Apertura, Assegnatari, Attività
3. È possibile **filtrare** la lista (es. per Numero Pratica o Stato); il campo **Tipo Pratica** è valorizzato di default con "Attivazione Nuova Carta" e non è editabile
4. Le colonne sono **ordinabili** (eccetto la colonna Assegnatari)
5. Cliccando sul link nella colonna **"Attività"** si accede al task specifico

#### Cosa verificare

- [ ] La lista attività è visibile solo per il ruolo Operatore (accedendo come Supervisore il tab Attività non deve essere presente o deve essere limitato)
- [ ] Il filtro per Tipo Pratica mostra "Attivazione Nuova Carta" non modificabile
- [ ] Le colonne sono ordinabili con icone freccia (eccetto Assegnatari)
- [ ] Cliccando sul link attività si naviga al task

---

### 3.2 Presa in Carico — Tipizzazione Documento

**Percorso**: `/attivita/:taskId/tipizzazione`  
**Ruoli**: `OPERATORE`, `OPERATORE_ANC`  
**Requisito originario**: L'operatore deve accettare il task e selezionare manualmente la tipologia di documento (Verbale di Denuncia o Carta) prima di poter procedere con l'istruttoria.

#### Navigazione

1. Dalla Lista Attività, cliccare sul link del task desiderato
2. Viene presentata la schermata di **Tipizzazione Documento**
3. Sul lato destro è visibile il **visualizzatore allegato** del documento caricato dal cliente
4. Cliccare **"ACCETTA"** per prendere formalmente in carico il task
   - Senza premere ACCETTA non è possibile procedere
   - È presente il tasto **"Indietro"** per tornare alla lista senza prendere in carico
5. Dopo ACCETTA, selezionare dal menu a tendina il tipo documento: **"Verbale di denuncia"** o **"Carta"**
6. Cliccare **"CONFERMA"** per rendere definitiva la scelta e abilitare i controlli

> ⚠️ **Vincolo irreversibile**: dopo aver cliccato CONFERMA la tipizzazione non è più modificabile.

#### Cosa verificare

- [ ] Lo stato della pratica passa da "Aperta" a "In Lavorazione" dopo ACCETTA + CONFERMA
- [ ] Il menu a tendina propone solo le opzioni "Verbale di denuncia" e "Carta"
- [ ] Dopo CONFERMA il tipo documento non è più modificabile
- [ ] Il visualizzatore allegato mostra l'anteprima del documento
- [ ] È presente il tasto "Indietro" (senza prendere in carico)
- [ ] In caso di problemi al visualizzatore è presente un'istruzione (icona "i") per procedere manualmente

---

### 3.3 Istruttoria Documentale — Milestone Lavorazione

**Percorso**: `/attivita/:taskId/tipizzazione` (step successivi alla tipizzazione)  
**Ruoli**: `OPERATORE`, `OPERATORE_ANC`  
**Requisito originario**: Compilazione della checklist di conformità specifica per il tipo di documento selezionato, con visualizzatore integrato, dati cliente e dati carta bloccata in confronto.

#### Navigazione — Milestone Verifica Documento

1. Dopo la tipizzazione, il sistema naviga alla milestone **"Verifica Documento"**
2. L'interfaccia è organizzata in **due colonne**:
   - Sinistra: dati anagrafici cliente, dati carta bloccata, checklist di conformità
   - Destra: visualizzatore allegato (zoom, download manuale)
3. Compilare la checklist in base alla tipologia selezionata:

**Se Verbale di Denuncia:**
- Controllo presenza verbale (Radio: **Si / No**)
  - Se "No": tutti i controlli di conformità vanno automaticamente in KO
  - Se "Si": la colonna Conformità si attiva
- Controllo leggibilità (obbligatorio)
- Controllo idoneità formale (obbligatorio)
  - Se negativo: selezionare **causale obbligatoria** (Intestazione / Firme / Timbro / Carta Poste Italiane)
- Controllo coerenza dati cliente (obbligatorio)
- Corrispondenza numero carta sul verbale (**facoltativo** — compilare solo se il numero è presente nel testo)
  - Rendere obbligatorio il controllo tramite apposita icona, se applicabile

**Se Carta:**
- Controllo presenza carta (Radio: **Si / No**)
- Verifica conformità immagine carta tagliata

4. Per ogni controllo della checklist, cliccando **"Mostra Descrizione"** è possibile visualizzare le istruzioni operative
5. Cliccare **"SALVA E PROSEGUI"** per salvare la checklist e procedere al Riepilogo
6. Dopo il salvataggio, cliccare **"MODIFICA"** per riaprire e correggere la checklist se necessario

#### Cosa verificare

- [ ] La checklist visualizzata corrisponde alla tipologia di documento scelta (Verbale vs Carta)
- [ ] Selezionando "No" alla presenza del documento, tutti i controlli vanno in KO automaticamente
- [ ] Se il controllo formale fallisce, il campo causale diventa obbligatorio (Intestazione / Firme / Timbro)
- [ ] Il controllo numero carta è facoltativo (non obbligatorio di default)
- [ ] Il tasto "Mostra Descrizione" mostra le istruzioni per il singolo controllo
- [ ] "SALVA E PROSEGUI" salva e abilita la milestone Riepilogo
- [ ] "MODIFICA" rende editabile la checklist già salvata
- [ ] È possibile effettuare il download manuale del documento allegato
- [ ] Il visualizzatore consente di regolare la dimensione dell'anteprima (Piccolo / Medio / Grande)

---

### 3.4 Riepilogo e Chiusura Pratica

**Percorso**: `/attivita/:taskId/tipizzazione` (ultima milestone)  
**Ruoli**: `OPERATORE`, `OPERATORE_ANC`  
**Requisito originario**: L'operatore visualizza l'esito calcolato automaticamente dal sistema e chiude la pratica inviando l'esito al BPM.

#### Navigazione

1. Dopo "SALVA E PROSEGUI", il sistema abilita la milestone **"Riepilogo"**
2. L'esito finale è presentato in card di **sola lettura**:
   - Card **verde**: pratica **Approvata**
   - Card **rossa**: pratica **Respinta**
3. L'operatore **non può forzare manualmente** l'esito: deve modificare la checklist
4. In caso di pratica **Respinta**, è visibile il campo **"Note"** (facoltativo) per dettagliare i motivi del KO
5. Cliccare **"CHIUDI PRATICA"** per:
   - Rimuovere il task dalla Lista Attività
   - Inviare l'esito al sistema BPM
   - Cambiare lo stato della pratica in "In Attesa Conferma BPM"

#### Cosa verificare

- [ ] L'esito (Approvata/Respinta) è calcolato automaticamente dal sistema in base alla checklist
- [ ] L'esito è in sola lettura (non modificabile direttamente)
- [ ] Il campo Note è visibile solo per pratiche Respinte
- [ ] Il campo Note è facoltativo
- [ ] Dopo "CHIUDI PRATICA" il task non è più nella Lista Attività
- [ ] Lo stato della pratica diventa "In Attesa Conferma BPM"
- [ ] Quando il BPM conferma la ricezione, lo stato diventa "Chiusa OK" o "Chiusa KO"

---

### 3.5 Lista Pratiche (Storico)

**Percorso**: `/pratiche`  
**Accesso**: tutti i ruoli  
**Requisito originario**: Consultazione dell'intero storico delle pratiche, con filtri per stato, data e esito.

#### Navigazione

1. Cliccare sul tab **"Pratiche"** nella barra di navigazione
2. La lista mostra tutte le pratiche con: ID Pratica, Stato, Data Apertura, Data Chiusura, Esito SD, Assegnatario
3. Applicare filtri: **ID Pratica**, **Stato** (Aperta / In Lavorazione / In Attesa Conferma BPM / Chiusa OK / Chiusa KO), range temporali (Apertura, Chiusura, Ultima Modifica)
4. Cliccare su una pratica per aprire il **Dettaglio Pratica**
5. Esportare l'elenco in **Excel** tramite apposito tasto
6. Navigare tra le pagine dell'elenco (`>`, `<`, `>>`, `<<`)

#### Cosa verificare

- [ ] I filtri per Stato propongono tutti e 5 gli stati previsti
- [ ] I filtri per range temporale (Apertura, Chiusura, Ultima Modifica) funzionano
- [ ] L'esportazione Excel è disponibile
- [ ] La paginazione funziona

---

### 3.6 Dettaglio Pratica

**Percorso**: `/pratiche/:practiceId`  
**Accesso**: tutti i ruoli  
**Requisito originario**: Vista d'insieme di una pratica con avanzamento nel workflow, dati, cronologia e stati.

#### Navigazione

1. Dalla Lista Pratiche, cliccare sull'ID di una pratica
2. Il Dettaglio Pratica mostra:
   - **Riepilogo**: linea di avanzamento (Raccolta Input → Lavorazione → Chiusura), dati generici
   - **Dati Pratica**: data apertura, canale provenienza
   - **Dati Lavorazione**: checklist compilata, allegati (sola lettura)
   - **Esito**: risultato finale della lavorazione
3. Tab di tracciabilità:
   - **Cronologia**: log di tutte le attività condotte con utente e descrizione evento
   - **Stati**: sequenza temporale dei cambiamenti di stato con data e operatore
   - **Azioni Correlate**: eventuali azioni sussidiarie legate alla pratica

#### Cosa verificare

- [ ] La linea di avanzamento mostra correttamente la fase attuale della pratica
- [ ] I dati cliente e dati carta bloccata sono visibili
- [ ] Il tab Cronologia mostra le attività in ordine cronologico con utente associato
- [ ] Il tab Stati mostra la sequenza completa di tutti i passaggi di stato
- [ ] La checklist e gli allegati in Dati Lavorazione sono in sola lettura

---

## 4. Percorso Supervisore

> Il Supervisore ha accesso a tutte le funzionalità dell'Operatore in modalità consultazione, più le funzionalità esclusive di orchestrazione descritte di seguito.

### 4.1 Home Page Supervisore

**Percorso**: `/home`  
**Ruoli**: `SUPERVISORE`, `SUPERVISORE_ANC`  
**Requisito originario**: Visione d'insieme con grafici statistici per monitoraggio delle performance del team.

#### Navigazione

1. Dopo il login come Supervisore, la Home Page mostra:
   - **Contatori real-time**: numero di attività in coda, pratiche attive, pratiche chiuse
   - **Grafico "Pratiche Giornaliere"**: richieste di lavorazione aperte ogni giorno
   - **Grafico "Pratiche Giornaliere Lavorate"**: pratiche concluse suddivise per esito (Chiusa OK / Chiusa KO)
   - **Grafico "Pratiche per Stato"**: distribuzione totale nelle fasi di lavorazione
2. I grafici coprono un **periodo mensile** selezionabile tramite calendario

#### Cosa verificare

- [ ] I 3 grafici istogrammi sono visualizzati sulla Home Page
- [ ] Il filtro calendario per periodo mensile funziona
- [ ] I contatori in alto a destra mostrano valori aggiornati

---

### 4.2 Lista Pratiche — Ricerca Avanzata

**Percorso**: `/pratiche`  
**Ruoli**: `SUPERVISORE`, `SUPERVISORE_ANC`  
**Requisito originario**: Il Supervisore può filtrare e consultare l'intero repository per identificare pratiche bloccate o anomale.

#### Navigazione

1. Navigare al tab **"Pratiche"**
2. Applicare filtri avanzati:
   - Filtrare per **"In Attesa Conferma BPM"** per identificare potenziali blocchi tecnici di comunicazione BPM
   - Filtrare per **"In Lavorazione"** per identificare possibili ritardi operativi
   - Usare **"Data Ultima Modifica"** per isolare pratiche che non avanzano da troppo tempo
3. Selezionare una pratica per accedere al dettaglio in sola lettura

#### Cosa verificare

- [ ] Il filtro per stato "In Attesa Conferma BPM" restituisce le pratiche in quello stato
- [ ] Il filtro per "Data Ultima Modifica" è disponibile e funzionante
- [ ] Il Supervisore può accedere al dettaglio pratica in sola lettura (inclusa checklist compilata e allegati)

---

### 4.3 Riassegna Attività

**Percorso**: `/riassegna-attivita`  
**Ruoli**: `SUPERVISORE`, `SUPERVISORE_ANC`  
**Requisito originario**: Il Supervisore può spostare le attività dalla coda di gruppo a un utente specifico, o tra operatori, per bilanciare il carico di lavoro.

#### Navigazione

1. Dalla barra di navigazione, accedere al tab **"Riassegna Attività"** (visibile solo per il ruolo Supervisore)
2. La pagina mostra l'elenco di **tutte le attività dei processi di competenza** ("Le attività dei miei processi")
3. Selezionare uno o più task da riassegnare
4. Scegliere la tipologia di riassegnazione:
   - **Gruppo Operatore ANC**: restituisce il task alla coda comune
   - **Utente**: assegna il task a uno specifico operatore
5. Confermare la riassegnazione

#### Cosa verificare

- [ ] La pagina "Riassegna Attività" è accessibile solo per il ruolo Supervisore (per l'Operatore deve restituire 403/Forbidden o non essere visibile)
- [ ] La lista mostra tutte le attività dei processi di competenza del Supervisore
- [ ] È possibile riassegnare un task al "Gruppo Operatore ANC" (coda comune)
- [ ] È possibile riassegnare un task a uno specifico utente
- [ ] Dopo la riassegnazione, il task appare nella coda del destinatario

---

### 4.4 Dashboard Segnalazioni

**Percorso**: `/segnalazioni`  
**Accesso**: `OPERATORE`, `OPERATORE_ANC`, `SUPERVISORE`, `SUPERVISORE_ANC`  
**Requisito originario**: Gestione e monitoraggio delle segnalazioni verso il sistema Sinergia.

#### Navigazione

1. Dalla barra di navigazione, accedere al tab **"Segnalazioni"**
2. La dashboard è divisa in due viste:
   - **"Le Mie Segnalazioni"** / **"Segnalazioni Attive"**: segnalazioni in carico all'utente corrente
   - **"Visualizza Segnalazioni"**: storico completo delle segnalazioni
3. Filtri disponibili: ID Segnalazione, Stato (In Coda / In Lavorazione / Chiuso), Operatore Ultima Modifica, range temporali
4. Il Supervisore può:
   - Inviare e inoltrare segnalazioni verso Sinergia
   - **Riassegnare segnalazioni** a un operatore specifico, a un gruppo o a se stesso

#### Cosa verificare

- [ ] La Dashboard Segnalazioni è accessibile per entrambi i ruoli (Operatore e Supervisore)
- [ ] I filtri per Stato (In Coda / In Lavorazione / Chiuso) funzionano
- [ ] Il filtro "Operatore Ultima Modifica" è disponibile
- [ ] Il Supervisore può riassegnare una segnalazione attiva

---

## 5. Matrice Riepilogativa — Funzionalità per Ruolo

| Funzionalità | Percorso | Operatore | Supervisore |
|-------------|----------|-----------|-------------|
| Login | `/login` | ✅ | ✅ |
| Home Page con contatori | `/home` | ✅ | ✅ |
| Grafici statistici mensili | `/home` | ✅ (visione) | ✅ (visione + analisi) |
| Link Favoriti | `/home` | ✅ | ✅ |
| Lista Attività (Coda) | `/attivita` | ✅ | ❌ (solo Supervisore non lavora task) |
| Presa in Carico (ACCETTA) | `/attivita/:id/tipizzazione` | ✅ | ❌ |
| Tipizzazione Documento | `/attivita/:id/tipizzazione` | ✅ | ❌ |
| Checklist Istruttoria | `/attivita/:id/tipizzazione` | ✅ | ❌ |
| Chiusura Pratica | `/attivita/:id/tipizzazione` | ✅ | ❌ |
| Lista Pratiche (storico) | `/pratiche` | ✅ | ✅ |
| Dettaglio Pratica | `/pratiche/:id` | ✅ | ✅ (sola lettura) |
| Tab Cronologia | `/pratiche/:id` | ✅ | ✅ |
| Tab Stati | `/pratiche/:id` | ✅ | ✅ |
| Esportazione Excel | `/pratiche` | ✅ | ✅ |
| Riassegna Attività | `/riassegna-attivita` | ❌ | ✅ |
| Dashboard Segnalazioni | `/segnalazioni` | ✅ (invio) | ✅ (invio + riassegna) |

---

## 6. Ciclo di Vita della Pratica — Transizioni Attese

```
[BPM] → Apertura Pratica
              │
              ▼
         [Aperta]
              │  Operatore: ACCETTA + CONFERMA tipo documento
              ▼
       [In Lavorazione]
              │  Operatore: completa checklist + CHIUDI PRATICA
              ▼
  [In Attesa Conferma BPM]
              │  BPM: conferma ricezione esito
              ▼
       [Chiusa OK] o [Chiusa KO]
```

### Cosa verificare (stati)

- [ ] Una nuova pratica ricevuta da BPM appare in stato "Aperta" nella Lista Attività
- [ ] Dopo ACCETTA + CONFERMA tipo documento, lo stato è "In Lavorazione"
- [ ] Dopo "CHIUDI PRATICA", lo stato è "In Attesa Conferma BPM" e il task scompare dalla Lista Attività
- [ ] Dopo la conferma BPM, lo stato è "Chiusa OK" (esito positivo) o "Chiusa KO" (esito negativo)
- [ ] La Data Chiusura viene valorizzata con la data corrente (sysdate) al momento della chiusura definitiva

---

## 7. Controlli di Accesso per Ruolo

### Cosa verificare — Sicurezza e Routing

- [ ] Accedere a `/attivita` come Supervisore → reindirizzamento a `/forbidden` o pagina di errore 403
- [ ] Accedere a `/riassegna-attivita` come Operatore → reindirizzamento a `/forbidden` o pagina di errore 403
- [ ] Accedere a qualsiasi rotta protetta senza autenticazione → reindirizzamento a `/login`
- [ ] Dopo logout, accedere alla rotta precedente → reindirizzamento a `/login`

---

## 8. Note per il Verificatore

1. **Idempotenza**: inviare due volte la stessa richiesta BPM con lo stesso `ID_WORKITEM` deve produrre una sola pratica (errore codice `-5` alla seconda richiesta)
2. **Download allegati**: se il visualizzatore integrato non carica il documento, il tasto download manuale deve essere disponibile
3. **Gestione KO Multipli**: se la pratica è Respinta per più motivi, tutti i codici KO devono essere inviati al BPM (non solo il primo)
4. **Tipizzazione irreversibile**: non deve essere possibile cambiare il tipo documento dopo CONFERMA, neanche navigando direttamente all'URL
5. **Campo Note**: deve apparire SOLO per pratiche Respinte (non per le Approvate)
6. **Colonna Assegnatari**: nella Lista Attività non deve essere ordinabile
7. **Tipo Pratica**: il filtro "Tipo Pratica" nella Lista Attività deve essere fisso a "Attivazione Nuova Carta" e non modificabile
