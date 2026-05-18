# UI Reverse Engineering — Attivazione Nuova Carta (ANC)

Documento di mappatura semantica dell'interfaccia utente della Scrivania Digitale per il processo ANC, ricostruita a partire da:
- Discovery (`docs/requirements/Attivazione nuova carta_Discovery.md`)
- Manuale Operatore (`docs/requirements/UI_Operatore.md`)
- Manuale Supervisore (`docs/requirements/UI_Supervisore.md`)
- Screenshot di riferimento (`docs/requirements/ui-reference/`)

Lingua: italiano. Naming applicativo preservato.

---

## 1. Attori e profili UI

| Profilo | Welcome Page | Tab principali | Azioni dedicate |
|---|---|---|---|
| Specialista ANC (Operatore) | Welcome Page Specialista | Home, Attività, Pratiche | Dashboard Segnalazioni (invio/visualizzazione) |
| Supervisore ANC | Welcome Page Supervisore | Home, Pratiche, Riassegna Attività | Dashboard Segnalazioni (riassegna/visualizzazione) |

Entrambi i profili condividono: header con contatori in alto a destra, accesso al dettaglio pratica, visualizzazione cronologia/stati/azioni correlate.

---

## 2. Screen Inventory

### 2.1 Schermate comuni

| ID | Schermata | Profili | Scopo |
|---|---|---|---|
| SC-00 | Login | Operatore, Supervisore | Autenticazione con credenziali; instradamento alla Welcome Page per profilo |
| SC-01 | Home Page | Operatore, Supervisore | Punto di atterraggio post-login; navigazione tab, contatori real-time, azioni rapide |
| SC-02 | Tab Pratiche (Lista Pratiche) | Operatore, Supervisore | Consultazione storico pratiche con filtri |
| SC-03 | Dettaglio Pratica — Riepilogo | Operatore, Supervisore | Vista d'insieme di una pratica con linea avanzamento e milestone |
| SC-04 | Dettaglio Pratica — Cronologia | Operatore, Supervisore | Log temporale attività e utenti |
| SC-05 | Dettaglio Pratica — Stati | Operatore, Supervisore | Storico sequenziale dei passaggi di stato |
| SC-06 | Dettaglio Pratica — Azioni correlate | Operatore, Supervisore | Azioni sussidiarie legate alla pratica |
| SC-07 | Milestone Dati Pratica (sola lettura) | Operatore, Supervisore | Visualizzazione dati anagrafici/canale della pratica |
| SC-08 | Milestone Dati Lavorazione (sola lettura) | Operatore, Supervisore | Vista checklist compilata + allegati |
| SC-09 | Milestone Esito (sola lettura) | Operatore, Supervisore | Visualizzazione Esito SD, Data Esito, note operatore |

### 2.2 Schermate specifiche Operatore

| ID | Schermata | Scopo |
|---|---|---|
| SC-OP-01 | Tab Attività (Lista Attività) | Coda lavorazioni dell'operatore e del gruppo |
| SC-OP-02 | Presa in Carico (ACCETTA / INDIETRO) | Accettazione esplicita del task prima della lavorazione |
| SC-OP-03 | Tipizzazione Documento | Selezione manuale Verbale di denuncia / Carta + preview allegato |
| SC-OP-04 | Milestone Dati Pratica (Lavorazione) | Dati anagrafici della pratica in lavorazione |
| SC-OP-05 | Milestone Verifica Documento — Verbale di Denuncia | Compilazione checklist conformità verbale + preview |
| SC-OP-06 | Milestone Verifica Documento — Carta | Compilazione checklist conformità carta tagliata + preview |
| SC-OP-07 | Milestone Riepilogo | Esito calcolato (card APPROVATA/RESPINTA), note interne, chiusura pratica |
| SC-OP-08 | Dashboard Segnalazioni — Visualizza Le Segnalazioni Attive | Lista segnalazioni attive verso Sinergia con filtri |
| SC-OP-09 | Dashboard Segnalazioni — Visualizza Segnalazioni | Storico completo segnalazioni |

### 2.3 Schermate specifiche Supervisore

| ID | Schermata | Scopo |
|---|---|---|
| SC-SUP-01 | Home Page Supervisore (con Grafici) | Contatori + 3 istogrammi (Pratiche Giornaliere, Pratiche Giornaliere Lavorate, Pratiche per Stato) |
| SC-SUP-02 | Tab Riassegna Attività | Lista task riassegnabili + sezione Dettagli riassegnazione |
| SC-SUP-03 | Dashboard Segnalazioni — Riassegna Segnalazioni | Riassegna segnalazioni a gruppo / utente / se stesso |
| SC-SUP-04 | Dashboard Segnalazioni — Visualizza Le Mie Segnalazioni | Segnalazioni in carico al supervisore |
| SC-SUP-05 | Dashboard Segnalazioni — Visualizza Segnalazioni | Storico completo segnalazioni |

---

## 3. Workflow UI

### 3.1 Workflow Operatore (end-to-end lavorazione pratica)

```
Login → Home → Tab Attività → click "Attività" su riga task
      → Schermata Presa in Carico (ACCETTA / INDIETRO)
      → Tipizzazione Documento (Verbale | Carta) → CONFERMA  [IRREVERSIBILE]
      → Milestone "Dati Pratica" (consultazione)
      → Milestone "Verifica Documento" (compilazione checklist)
         ├─ Presenza documento? Si/No
         ├─ Compilazione colonna Conformità (se "Si")
         ├─ Causali KO obbligatorie se controllo formale = NO
         └─ SALVA E PROSEGUI  (oppure MODIFICA per riaprire)
      → Milestone "Riepilogo"
         ├─ Card APPROVATA (verde) | RESPINTA (rossa)  [sola lettura]
         ├─ Note interne (facoltative, solo se RESPINTA)
         └─ CHIUDI PRATICA → task rimosso dalla Lista Attività
      → Pratica in stato "In Attesa Conferma BPM"
      → (Sincronizzazione automatica BPM) → "Chiusa OK" | "Chiusa KO"
```

### 3.2 Workflow Supervisore (monitoraggio + riassegnazione)

```
Login → Home Supervisore
      ├─ Monitor contatori real-time
      ├─ Analisi grafici (Pratiche Giornaliere / Lavorate / per Stato)
      ├─ Tab Pratiche → Dettaglio Pratica (sola lettura: Dati, Cronologia, Stati)
      └─ Tab Riassegna Attività
            ├─ Filtra "Le attività dei miei processi"
            ├─ Sezione "Dettagli riassegnazione" → scegli Gruppo Operatore ANC | Utente
            ├─ Flag selezione task in tabella
            └─ CONFERMA → riassegnazione effettiva

Azioni → Dashboard Segnalazioni
      ├─ Riassegna Segnalazioni → Gruppo | Utente | Se stesso → CONFERMA
      ├─ Visualizza Le Mie Segnalazioni
      └─ Visualizza Segnalazioni (storico)
```

### 3.3 Stati pratica e transizioni operative (innesco da UI)

| Da | A | Innesco UI |
|---|---|---|
| (inesistente) | Aperta | Ricezione richiesta BPM (automatico, nessuna UI) |
| Aperta | In Lavorazione | Tasto ACCETTA + CONFERMA tipizzazione documento |
| In Lavorazione | In Attesa Conferma BPM | Tasto CHIUDI PRATICA in Milestone Riepilogo |
| In Attesa Conferma BPM | Chiusa OK / Chiusa KO | Conferma ricezione BPM (automatico) |

---

## 4. Navigation Map

### 4.1 Operatore

```
[Header]
  ├─ Tab Home
  ├─ Tab Attività
  ├─ Tab Pratiche
  └─ Contatori (Attività | Pratiche Attive | Pratiche Chiuse)

[Home]
  ├─ Azione: Dashboard Segnalazioni
  │     ├─ Visualizza Le Segnalazioni Attive
  │     └─ Visualizza Segnalazioni
  ├─ Sezione: Link Favoriti  (+ Aggiungi nuovo link)
  └─ Sezione: I Miei Widget  (link Web Mail)

[Tab Attività]
  └─ Riga task → link colonna "Attività"
        └─ Schermata Presa in Carico
              └─ ACCETTA
                    └─ Tipizzazione Documento
                          └─ CONFERMA
                                ├─ Milestone Dati Pratica
                                ├─ Milestone Verifica Documento
                                └─ Milestone Riepilogo → CHIUDI PRATICA

[Tab Pratiche]
  └─ Riga pratica → link colonna "Pratica N."
        └─ Dettaglio Pratica
              ├─ Riepilogo (Dati Pratica | Dati Lavorazione | Esito)
              ├─ Cronologia
              ├─ Stati
              └─ Azioni correlate
```

### 4.2 Supervisore

```
[Header]
  ├─ Tab Home
  ├─ Tab Pratiche
  ├─ Tab Riassegna Attività
  └─ Contatori (Attività | Pratiche Attive | Pratiche Chiuse)

[Home]
  ├─ Azione: Dashboard Segnalazioni
  │     ├─ Riassegna Segnalazioni
  │     ├─ Visualizza Le Mie Segnalazioni
  │     └─ Visualizza Segnalazioni
  └─ Grafici (Pratiche Giornaliere | Lavorate | per Stato)

[Tab Pratiche] → Dettaglio Pratica (sola lettura)
[Tab Riassegna Attività] → selezione + CONFERMA
```

---

## 5. Component Mapping

### 5.1 Componenti trasversali

| Componente | Dove appare | Note comportamentali |
|---|---|---|
| Header con tab | Tutte le schermate | Tab differenti per profilo |
| Contatori real-time | Header in alto a destra | Attività / Pratiche Attive / Pratiche Chiuse |
| Linea di avanzamento (griglia gialla con barra blu) | Tipizzazione, Milestone Lavorazione, Dettaglio Riepilogo | Step: "Raccolta input" → "Lavorazione" → "Chiusura Pratica" |
| Card informazioni generiche pratica | Sotto la linea di avanzamento | Nome processo "Attivazione Nuova Carta" + Codice Fiscale Cliente + Pratica N. |
| Sezione milestone verticale | Schermate di lavorazione e dettaglio | Pulsanti milestone: Dati Pratica, Verifica Documento / Dati Lavorazione, Riepilogo / Esito |
| Barra blu espandibile | Sezione "Indirizzo di Residenza" dentro Dati Cliente | Espandi/comprimi sottosezione |
| Tasto NASCONDI SEZIONE | Milestone Verifica Documento / Dati Lavorazione | Nasconde sezioni informative |
| Tasto NASCONDI ALLEGATI | Milestone Verifica Documento / Dati Lavorazione | Nasconde preview allegati |
| Visualizzatore documento (preview) | Colonna destra in Tipizzazione e Verifica Documento | Icona occhio per anteprima; download manuale se KO rendering; livelli dimensione (Piccolo/Medio/Grande) |
| Box informativo (icona "i") | Tipizzazione Documento | Istruzione: in caso di mancato download, selezionare comunque tipo e chiudere con esito negativo |
| Tasto "Mostra Descrizione" | Ogni riga checklist | Espone descrizione operativa del controllo |
| Paginazione | Tutte le liste | `<<`, `<`, `>`, `>>` con numero pagina corrente / totale |
| Indicatori ordinamento colonna | Tutte le tabelle | Frecce blu su/giù sulla testata colonna |
| Esporta Excel | Tab Pratiche, liste segnalazioni | Icona dedicata |
| Tasto Aggiorna | Liste con filtri | Refresh dati |
| Tasto Cancella Filtri / Salva Filtri | Tab Pratiche, Tab Attività, Dashboard Segnalazioni | Pulisci selezione filtri o salvale come preset |

### 5.2 Componenti specifici di interazione

| Componente | Schermata | Comportamento |
|---|---|---|
| Pulsante ACCETTA | Presa in Carico | Prende formalmente in carico la pratica; stato → "In Lavorazione" |
| Pulsante INDIETRO | Presa in Carico | Torna alla Lista Attività senza accettare |
| Dropdown "Tipo Documento" | Tipizzazione | Valori: "Verbale di denuncia", "Carta" |
| Pulsante CONFERMA (tipizzazione) | Tipizzazione | Abilita la checklist specifica; **azione irreversibile** |
| Radio button "Presenza documento" Si/No | Verifica Documento | Se "No" → colonna Conformità disabilitata e tutti i controlli forzati a KO |
| Icona "rendi obbligatorio" controllo numero carta | Checklist Verbale | Trasforma il controllo facoltativo in obbligatorio (uso quando il numero è presente sul verbale) |
| Pulsante SALVA E PROSEGUI | Verifica Documento | Salva la checklist e abilita la milestone Riepilogo |
| Pulsante MODIFICA | Verifica Documento (post-salvataggio) | Riapre la checklist in modalità editabile |
| Card APPROVATA (verde) | Riepilogo | Sola lettura; illuminata se esito = OK |
| Card RESPINTA (rossa) | Riepilogo | Sola lettura; illuminata se esito = KO |
| Box "Note interne" | Riepilogo | Visibile solo se RESPINTA; inserimento facoltativo |
| Pulsante CHIUDI PRATICA | Riepilogo | Chiude il task, invia esito al BPM, rimuove dalla Lista Attività |
| Form "+ Aggiungi nuovo link" | Home — Link Favoriti | Campi: Titolo Link, Link, Tipo Link (Interno/Esterno/Legacy); pulsante SALVA |
| Icone edit/delete link | Home — Link Favoriti | Modifica / Elimina link salvato |
| Widget Posta Elettronica | Home — I Miei Widget | Link diretto alla web mail |
| Checkbox di selezione riga (Riassegna Attività) | Supervisore — Riassegna Attività | Abilita il pulsante CONFERMA |
| Radio "Riassegna a": Gruppo Operatore ANC / Utente | Supervisore — Dettagli riassegnazione | Determina il target; per "Utente" appare campo di input utente |
| Pulsante CONFERMA (riassegna) | Supervisore — Riassegna Attività/Segnalazioni | Esegue la riassegnazione |
| Pulsante ESCI | Dashboard Segnalazioni | Torna alla Home Page |

---

## 6. Form Behavior

### 6.1 Form Tipizzazione Documento

- Campo obbligatorio: **Tipo Documento** (dropdown).
- Valori ammessi: `Verbale di denuncia`, `Carta`.
- Pulsante CONFERMA disabilitato finché non viene selezionato un valore.
- Dopo CONFERMA la scelta non è più modificabile per tutta la durata della pratica.
- Vincolo operativo: ogni pratica gestisce **un solo tipo di documento**, anche se il cliente ha caricato entrambi.

### 6.2 Form Checklist Verbale di Denuncia

Struttura tabellare (colonne: Richiesto, Descrizione, Conforme, Note, Esito Controllo, Causale).

- Controllo primario "Presenza documento" (Si/No) — gate per la colonna Conformità.
- Controllo "Idoneità formale del verbale" → se Conforme = NO, **causale obbligatoria** tra:
  - Intestazione
  - Firme
  - Intestazione Conforme al Timbro
  - Dichiarazione Conforme alle Firme
  - Carta Poste Italiane
- Controllo "Corrispondenza numero carta su verbale": facoltativo di default; reso obbligatorio se l'operatore clicca l'icona apposita (perché il numero è presente sul verbale).
- Tutti gli esiti dei singoli controlli sono valorizzati come OK / KO; la Causale è popolata coerentemente con l'esito.
- Salvataggio: pulsante SALVA E PROSEGUI. Modifica post-salvataggio: pulsante MODIFICA.

### 6.3 Form Checklist Carta

Stessa struttura tabellare. Controllo primario di presenza identico. Non sono previste causali enumerate equivalenti al verbale (la checklist è specifica per la conformità dell'immagine della carta tagliata).

### 6.4 Form Riepilogo / Chiusura

- Esito generale: **calcolato dal sistema**, non editabile manualmente.
- Per modificare l'esito occorre tornare alla Verifica Documento (tasto MODIFICA).
- Campo "Note interne": testo libero, **visibile solo se RESPINTA**, facoltativo.
- Azione finale: CHIUDI PRATICA.

### 6.5 Form Link Favoriti (Home Operatore)

Campi obbligatori: Titolo Link, Link (URL), Tipo Link (Interno | Esterno | Legacy). Azioni: SALVA, modifica, elimina.

### 6.6 Form Dettagli Riassegnazione (Supervisore)

- Radio: "Gruppo Operatore ANC" / "Utente" (per Riassegna Segnalazioni anche "Se stesso").
- Se "Utente" selezionato: appare riga di input utente.
- Selezione di almeno una riga in tabella (checkbox) abilita CONFERMA.

---

## 7. Table Behavior

### 7.1 Lista Pratiche (Tab Pratiche — Operatore e Supervisore)

Filtri: Cerca Pratiche, Stato (Aperta, In Lavorazione, In Attesa Conferma BPM, Chiusa Ok, Chiusa KO), Data Apertura, Data Chiusura, Data Ultima Modifica, Esito SD (OK/KO).

Colonne: Pratica N. (link al Dettaglio), Codice Fiscale, Codice Cliente, Data Apertura, Data Ultima Modifica, Data Chiusura, Data Inserimento Richiesta, Esito SD, Operatore, Stato, Segnalazioni.

Caratteristiche:
- Ordinamento ascendente/discendente su tutte le colonne (frecce blu sulla testata).
- Esportazione Excel.
- Cancella/Salva filtri, Aggiorna.
- Paginazione `<<`, `<`, `>`, `>>`.

### 7.2 Lista Attività (Tab Attività — Operatore)

Filtri: Stato (In Coda, In Lavorazione), Tipo Pratica (default "Attivazione Nuova Carta", **non editabile**), Pratica N., Nome Attività, Assegnatario, Utente in carico.

Azioni filtri: APPLICA FILTRI, APPLICA FILTRI E SALVA, CANCELLA FILTRI.

Colonne: Pratica N., Attività (link al task), Assegnatari (non ordinabile), Utente in carico, Data Creazione, Data Presa in Carico, Stato.

Caratteristiche: ordinamento su tutte le colonne tranne "Assegnatari"; paginazione standard.

### 7.3 Lista Riassegna Attività (Supervisore)

Sezione "Le attività dei miei processi".

Filtri: Pratica N., Data Assegnazione, Owner, Assegnatario.

Colonne: Processo, Pratica, Nome Attività, Assegnatario, Owner, Data Assegnazione, Data Presa in carico, Stato. Checkbox di selezione per riga.

Azione "Pulisci Filtri". Selezione → CONFERMA (con sezione Dettagli riassegnazione).

### 7.4 Liste Segnalazioni (Dashboard Segnalazioni)

#### 7.4.1 Visualizza Le Segnalazioni Attive (Operatore) / Riassegna Segnalazioni (Supervisore)

Filtri: Pratica N., Id Segnalazione, Attività Segnalazione, Stato (Tutti, In Coda, In Lavorazione), Data Creazione Da/A, Utente in Carico (+ Assegnatario, Data Presa in carico nel profilo Supervisore).

Azioni: AGGIORNA, CANCELLA FILTRI, ESCI.

Colonne: ID Pratica, ID Segnalazione, Attività Segnalazione (link), Assegnatario (non ordinabile in profilo Operatore), Utente in Carico, Data Creazione, Data Presa in carico, Stato.

#### 7.4.2 Visualizza Segnalazioni (storico)

Filtri: Pratica N., Id Segnalazione, Data Creazione Da/A, Data Chiusura Da/A, Operatore Ultima Modifica, Stato (In Coda, In Lavorazione, Chiuso).

Colonne: Pratica N., Id Segnalazione, Attività Segnalazione, Assegnatario, Operatore Ultima Modifica, Data Creazione, Data Chiusura, Stato.

Tutte le colonne ordinabili.

#### 7.4.3 Visualizza Le Mie Segnalazioni (Supervisore)

Stessi filtri di "Riassegna Segnalazioni" applicati al subset di segnalazioni in carico al supervisore.

---

## 8. Dashboard Structure (Supervisore)

### 8.1 Layout Home Supervisore

- **Header alto-destra**: contatori real-time (Attività, Pratiche Attive, Pratiche Chiuse).
- **Azione centrale**: Dashboard Segnalazioni (con i tre tasti: Riassegna Segnalazioni, Visualizza Le Mie Segnalazioni, Visualizza Segnalazioni).
- **Area grafici**: tre istogrammi affiancati o impilati con selettore periodo (calendario + frecce `←` / `→` per mese precedente/successivo). Tooltip su hover.

### 8.2 Istogramma "Pratiche Giornaliere"

Asse X: giorni del mese selezionato. Asse Y: numero di richieste di lavorazione aperte ogni giorno su SD. Tooltip con dettaglio puntuale.

### 8.3 Istogramma "Pratiche Giornaliere Lavorate"

Asse X: giorni del mese. Asse Y: pratiche concluse, **segmentate per esito**: "Chiusa OK" e "Chiusa KO".

### 8.4 Istogramma "Pratiche per Stato"

Distribuzione totale delle pratiche per stato di lavorazione: Aperta, In Lavorazione, In Attesa Conferma BPM, Chiusa OK, Chiusa KO.

---

## 9. Milestone Workflow

### 9.1 Milestone in fase di Lavorazione (Operatore)

| Ordine | Milestone | Stato attivazione | Componenti chiave |
|---|---|---|---|
| 1 | Dati Pratica | Sempre visibile in sola lettura | Data Apertura, Data Ultima Modifica, Stato, Codice Cliente, Codice Fiscale, Canale |
| 2 | Verifica Documento | Editabile dopo CONFERMA tipizzazione | Dati Cliente, Dati Carta Bloccata, Contenuti documento (preview), Checklist controlli |
| 3 | Riepilogo | Abilitata solo dopo SALVA E PROSEGUI | Card APPROVATA/RESPINTA, Note interne (solo se RESPINTA), CHIUDI PRATICA |

### 9.2 Milestone in fase di Dettaglio Pratica (consultazione)

| Ordine | Milestone | Contenuto |
|---|---|---|
| 1 | Dati Pratica | Stessi campi della lavorazione + Esito SD e Data Chiusura Pratica se completata |
| 2 | Dati Lavorazione | Vista in sola lettura della checklist compilata (colonne: Richiesto, Descrizione, Conforme, Note, Esito Controllo, Causale) + preview allegati |
| 3 | Esito | Esito Scrivania Digitale, Data Esito Scrivania Digitale, Note operatore (se inserite) |

### 9.3 Linea di avanzamento processo

Visibile in tutte le schermate di lavorazione e dettaglio, sempre nella stessa sequenza:

```
Raccolta input  →  Lavorazione  →  Chiusura Pratica
```

Rappresentata come griglia gialla con barra orizzontale blu che evidenzia lo step corrente.

---

## 10. Validazioni e vincoli operativi (UI-level)

### 10.1 Validazioni di processo

| Schermata | Vincolo | Comportamento UI |
|---|---|---|
| Lista Attività | Lavorazione consentita solo dopo ACCETTA | Il task non è apribile in modalità editabile finché non accettato |
| Tipizzazione | Tipo Documento obbligatorio | CONFERMA disabilitato fino a selezione |
| Tipizzazione | Irreversibilità | Dopo CONFERMA non è possibile cambiare tipo documento |
| Verifica Documento | Presenza documento gating | Colonna Conformità disabilitata se "No"; tutti i controlli forzati a KO |
| Verifica Documento | Causale KO controllo formale verbale | Obbligatorio selezionare almeno una delle 5 causali |
| Verifica Documento | Numero carta su verbale | Facoltativo di default; obbligatorio se attivato tramite apposita icona |
| Verifica Documento | Abilitazione Riepilogo | Riepilogo accessibile solo dopo SALVA E PROSEGUI |
| Riepilogo | Esito immutabile manualmente | Card in sola lettura; per cambiarlo serve MODIFICA in Verifica Documento |
| Riepilogo | Note interne | Campo visibile solo se RESPINTA, inserimento facoltativo |
| Lista Attività | Filtro Tipo Pratica | Default "Attivazione Nuova Carta", disabilitato |
| Liste in generale | Colonna Assegnatari | Non ordinabile |
| Riassegna Attività | Selezione obbligatoria | Pulsante CONFERMA abilitato solo dopo selezione di almeno una riga e scelta del target |

### 10.2 Stati di errore/eccezione gestiti in UI

- Indisponibilità visualizzatore documento → tasto/azione di **download manuale** dell'allegato.
- Box informativo (icona "i") in Tipizzazione con istruzioni alternative se download impossibile (selezionare tipo documento e chiudere con esito negativo).
- Errori di integrazione (idempotenza, ID_WORKITEM duplicato, oggetto DOCUMENTI mancante): gestiti lato servizio, non producono schermate UI per l'operatore (eventuali codici errore restituiti al BPM, es. `-5`, `-4`).

---

## 11. Azioni utente — Matrice riassuntiva

### 11.1 Operatore

| Area | Azione | Trigger UI |
|---|---|---|
| Home | Aggiungere/Modificare/Eliminare link favoriti | + Aggiungi nuovo link / icone edit/delete |
| Home | Accedere alla web mail | Widget "I Miei Widget" |
| Tab Pratiche | Filtrare, ordinare, esportare, paginare | Filtri, frecce testata, icona Excel, paginazione |
| Tab Pratiche | Aprire Dettaglio Pratica | Link colonna "Pratica N." |
| Tab Attività | Filtrare, salvare filtri, ordinare | Pulsanti APPLICA FILTRI / APPLICA FILTRI E SALVA / CANCELLA FILTRI |
| Tab Attività | Aprire un task | Link colonna "Attività" |
| Presa in Carico | Accettare il task | Pulsante ACCETTA |
| Presa in Carico | Tornare indietro | Pulsante INDIETRO |
| Tipizzazione | Selezionare tipo documento e confermare | Dropdown + CONFERMA |
| Tipizzazione | Visualizzare anteprima documento | Icona occhio / controlli dimensione (Piccolo/Medio/Grande) |
| Verifica Documento | Espandere/comprimere sezioni e allegati | NASCONDI SEZIONE / NASCONDI ALLEGATI / barra blu Indirizzo Residenza |
| Verifica Documento | Compilare checklist | Radio button, dropdown causale, Mostra Descrizione |
| Verifica Documento | Rendere obbligatorio controllo numero carta | Icona dedicata |
| Verifica Documento | Salvare e proseguire / modificare | SALVA E PROSEGUI / MODIFICA |
| Riepilogo | Inserire note interne (se RESPINTA) | Box Note interne |
| Riepilogo | Chiudere la pratica | CHIUDI PRATICA |
| Dashboard Segnalazioni | Visualizzare segnalazioni attive / storico | Tasti Visualizza Le Segnalazioni Attive / Visualizza Segnalazioni |
| Dashboard Segnalazioni | Aprire processo Sinergia | Click su una segnalazione |

### 11.2 Supervisore

| Area | Azione | Trigger UI |
|---|---|---|
| Home | Monitorare KPI | Contatori, istogrammi, selettore periodo |
| Tab Pratiche | Consultare storico + audit (Cronologia, Stati, Azioni correlate) | Link "Pratica N." + tab milestone in Dettaglio |
| Tab Riassegna Attività | Selezionare target di riassegnazione | Radio: Gruppo Operatore ANC / Utente |
| Tab Riassegna Attività | Selezionare task da riassegnare | Checkbox sulla riga |
| Tab Riassegna Attività | Confermare riassegnazione | Pulsante CONFERMA |
| Dashboard Segnalazioni | Riassegnare segnalazione | Radio (Gruppo / Utente / Se stesso) + CONFERMA |
| Dashboard Segnalazioni | Consultare segnalazioni proprie / globali | Tasti Visualizza Le Mie Segnalazioni / Visualizza Segnalazioni |
| Dashboard Segnalazioni | Tornare alla Home | Pulsante ESCI |

---

## 12. Annotazioni dagli screenshot

Le seguenti caratteristiche visuali sono confermate dagli screenshot di riferimento (`docs/requirements/ui-reference/`) e devono essere preservate:

- Header con logo/brand a sinistra e tab di navigazione orizzontale; contatori posizionati in alto a destra.
- Card del processo riportante "Attivazione Nuova Carta" + dati identificativi pratica subito sotto la linea di avanzamento.
- Schermata Dettaglio Pratiche presenta i tab di milestone in barra verticale o orizzontale con etichette: Dati Pratica, Dati Lavorazione, Esito (consultazione) / Verifica Documento (lavorazione), Riepilogo.
- Checklist organizzata in tabella con colonne ben distinte (Richiesto, Descrizione, Conforme, Note, Esito Controllo, Causale).
- Card APPROVATA / RESPINTA centrali, di grandi dimensioni, mutualmente esclusive.
- Pulsanti azione primari (ACCETTA, CONFERMA, SALVA E PROSEGUI, CHIUDI PRATICA) in posizione consistente in fondo o ai lati della schermata corrente.
- Dashboard Segnalazioni con tasti di sezione in alto e tabella sottostante con filtri sopra.

---

## 13. Conflitti e ambiguità rilevate

Per trasparenza verso il MAS, si segnalano i seguenti punti incoerenti o non risolti tra le fonti:

1. **Naming milestone in Lavorazione**: il manuale Operatore cita testualmente *"3 milenstone contenente cinque tasti: Dati Pratica, Verifica Documento e Riepilogo"*. Il numero di tasti (cinque) non è coerente con i tre nomi elencati. **Si preserva la triade documentata: Dati Pratica → Verifica Documento → Riepilogo**, ipotizzando refuso sul conteggio.

2. **Milestone in Dettaglio Pratica vs Lavorazione**: in Dettaglio (consultazione) i tasti sono "Dati Pratica, Dati Lavorazione, Esito"; in Lavorazione sono "Dati Pratica, Verifica Documento, Riepilogo". Mantenuti distinti perché coerenti con i due contesti (sola lettura vs editabile).

3. **Sigla "PPE"**: appare nel manuale Supervisore in alcune frasi ("Specialista PPE", "Gruppo Operatore PPE") ma il dominio è ANC. Si interpreta come refuso e si normalizza a "Specialista ANC" / "Gruppo Operatore ANC". Da confermare.

4. **"Energy" in Visualizza Le Mie Segnalazioni**: il manuale Supervisore cita "elenco delle pratiche Energy inviate per la segnalazione". Refuso evidente: si normalizza a "pratiche ANC". Da confermare.

5. **Causali KO controllo formale verbale**: il Discovery elenca {Intestazione, Firme, Timbro, Carta Poste Italiane}. Il manuale Operatore elenca {Intestazione, Firme, Intestazione Conforme al Timbro, Dichiarazione Conforme alle Firme, Carta Poste Italiane}. **Si preserva il set più dettagliato del manuale (5 voci)** come riferimento UI; il MAS dovrà allineare con il backend per il mapping codici KO.

6. **Stato "Chiuso" nelle segnalazioni**: nelle viste "Visualizza Segnalazioni" lo stato include "Chiuso" (al maschile, riferito alla segnalazione), mentre per le pratiche è "Chiusa OK / Chiusa KO". Preservare la differenza di naming tra dominio segnalazioni e dominio pratiche.

7. **Indicatori "Richiesto" della checklist**: il manuale parla di icone per "obbligatorio / non obbligatorio" senza nominarle. Non si inventa nomenclatura; la UI deve replicare un indicatore visivo a due stati nella colonna Richiesto.

---

Fine documento.
