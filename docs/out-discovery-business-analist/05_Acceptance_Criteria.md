# 05 - Acceptance Criteria (Gherkin)

Riferimenti: `04_Epic_UserStories.md`. Ogni AC è espressa in Given/When/Then.

---

## E2 – Integrazione BPM / Apertura Pratica

### AC-E2.01 – Apertura nominale
```gherkin
Given il sistema BPM invia una richiesta di apertura pratica
And la richiesta contiene dati cliente, dati carta bloccata e oggetto DOCUMENTI valido
And ID_WORKITEM è nuovo
When SD elabora la richiesta
Then la pratica viene creata in stato "Aperta"
And viene generato un task corrispondente in Lista Attività
And la response contiene resultCode "0" e l'identificativo Pratica N°
```

### AC-E2.02 – Documenti mancanti
```gherkin
Given una richiesta BPM senza oggetto DOCUMENTI
When SD valida la richiesta
Then la pratica NON viene creata
And il sistema restituisce un errore con messaggio descrittivo
```

### AC-E2.03 – CODICE_DOC_ID non valido
```gherkin
Given una richiesta BPM con DOCUMENTI.CODICE_DOC_ID = 4
When SD valida la richiesta
Then il sistema restituisce resultCode "-4"
And resultMessage indica i valori ammessi (1, 2, 3)
And la pratica NON viene creata
```

### AC-E2.04 – Idempotenza
```gherkin
Given una pratica esistente con ID_WORKITEM = "X"
When BPM invia una nuova richiesta con lo stesso ID_WORKITEM "X"
Then SD restituisce resultCode "-5"
And nessuna pratica duplicata viene creata
```

### AC-E2.05 – Campi obbligatori di testata mancanti
```gherkin
Given una richiesta BPM priva di almeno uno dei campi obbligatori di testata
  (CANALE, ID_WORKITEM, NUM_PRATICA, CF_CLIENTE, DATA_INSERIMENTO_RICHIESTA,
   CLIENTE, DATI_CARTA_BLOCCATA, DOCUMENTI)
When SD valida la richiesta
Then la pratica NON viene creata
And il sistema restituisce resultCode "-4"
And resultMessage indica il campo mancante
```

### AC-E2.06 – Dati CLIENTE obbligatori mancanti
```gherkin
Given una richiesta BPM in cui CLIENTE non contiene almeno uno tra
  NOME, COGNOME, DATANASCITA, COMUNENASCITA, PROVINCIANASCITA,
  NAZIONENASCITA, CITTADINANZA
When SD valida la richiesta
Then la pratica NON viene creata
And il sistema restituisce resultCode "-4"
And resultMessage indica il campo CLIENTE mancante
```

### AC-E2.07 – Dati Carta bloccata obbligatori mancanti
```gherkin
Given una richiesta BPM in cui DATI_CARTA_BLOCCATA non contiene
  I_NUMERO_CARTA oppure I_TIPO_CARTA
When SD valida la richiesta
Then la pratica NON viene creata
And il sistema restituisce resultCode "-4"
And resultMessage indica il campo carta mancante
```

### AC-E2.08 – Estensione allegato non ammessa
```gherkin
Given una richiesta BPM con un elemento DOCUMENTI.CONTENUTI
  con ESTENSIONE diversa da pdf, jpeg, jpg, png
When SD valida la richiesta
Then la pratica NON viene creata
And il sistema restituisce resultCode "-4"
And resultMessage indica l'estensione non ammessa
```

### AC-E2.09 – Persistenza dati pratica
```gherkin
Given una richiesta BPM valida è stata accettata
When SD crea la pratica
Then sono persistiti i dati di testata (CANALE, ID_WORKITEM, NUM_PRATICA,
  CF_CLIENTE, CODICE_CLIENTE, DATA_INSERIMENTO_RICHIESTA)
And sono persistiti i dati CLIENTE inclusi nel payload (NOME, COGNOME, SESSO,
  DATANASCITA, COMUNENASCITA, PROVINCIANASCITA, NAZIONENASCITA, CITTADINANZA,
  CELLULARE, TELEFONO, INDIRIZZO_DI_RESIDENZA)
And sono persistiti i dati DATI_CARTA_BLOCCATA (I_NUMERO_CARTA, I_TIPO_CARTA,
  I_INTEST_CARTA)
And sono persistiti i metadati DOCUMENTI / CONTENUTI (NOME_FILE, ESTENSIONE,
  ID_DOC, LINKDOWNLOAD) di ciascun allegato
And l'evento di apertura è tracciato in Cronologia
```

---

## E3 – Repository Pratiche

### AC-E3.01 – Filtro per stato
```gherkin
Given la lista pratiche contiene istanze in vari stati
When l'utente filtra per Stato = "In Lavorazione"
Then sono mostrate solo le pratiche in tale stato
```

### AC-E3.02 – Ordinamento colonna
```gherkin
Given la lista pratiche è caricata
When l'utente clicca sull'header colonna "Data Apertura"
Then l'elenco viene ordinato in modo ascendente
And un nuovo clic ordina in modo discendente
```

### AC-E3.03 – Dettaglio Cronologia
```gherkin
Given una pratica con almeno 3 azioni registrate
When l'utente apre la pratica e seleziona il tab Cronologia
Then vede l'elenco delle attività con utente, timestamp e descrizione
```

---

## E4 – Lista Attività e Presa in Carico

### AC-E4.01 – Generazione task
```gherkin
Given una nuova pratica viene creata in stato "Aperta"
When il sistema processa la creazione
Then un task corrispondente compare nella Lista Attività con tipo pratica = "Attivazione Nuova Carta"
```

### AC-E4.02 – Accettazione task
```gherkin
Given un task disponibile in Lista Attività
And l'utente loggato ha ruolo OPERATORE
When l'operatore clicca "ACCETTA" sul task
Then il task risulta assegnato all'operatore
And la pratica passa allo stato "In Lavorazione"
And l'evento è registrato in Cronologia
```

### AC-E4.03 – Tasto Indietro
```gherkin
Given l'operatore è nella schermata di accettazione di un task
When clicca "Indietro"
Then ritorna alla Lista Attività senza modificare lo stato del task
```

---

## E5 – Tipizzazione e Viewer

### AC-E5.01 – Visualizzazione integrata
```gherkin
Given una pratica in lavorazione con allegato disponibile
When l'operatore apre la schermata di tipizzazione
Then vede l'anteprima del documento nella colonna destra
And può regolare la dimensione (piccolo, medio, grande)
```

### AC-E5.02 – Download fallback
```gherkin
Given il viewer integrato non riesce a caricare l'allegato
When l'operatore clicca su "Download"
Then il file viene scaricato in locale
And l'operatore può comunque proseguire la tipizzazione
```

### AC-E5.03 – Tipizzazione irreversibile
```gherkin
Given un task accettato senza tipizzazione confermata
When l'operatore seleziona "Verbale di denuncia" e clicca CONFERMA
Then la checklist Verbale viene abilitata
And il campo Tipo Documento NON è più modificabile per la pratica
And la pratica resta in "In Lavorazione"
```

---

## E6 – Checklist Verbale e Calcolo Esito

### AC-E6.01 – Cascata KO su documento assente
```gherkin
Given la checklist Verbale è abilitata
When l'operatore seleziona "No" su "Verbale presente?"
Then la colonna Conformità viene disabilitata
And tutti i controlli successivi sono valorizzati automaticamente in KO
And l'esito calcolato è "Respinta"
```

### AC-E6.02 – Causale KO obbligatoria
```gherkin
Given l'operatore ha indicato Verbale presente = "Si"
And risponde "No" a "Il Verbale di Denuncia è idoneo al controllo formale?"
When tenta di salvare la checklist senza causale
Then il sistema impedisce il salvataggio
And richiede di selezionare almeno una causale tra Intestazione, Firme, Timbro, Dichiarazione, Carta Poste Italiane
```

### AC-E6.03 – Numero carta facoltativo
```gherkin
Given il verbale non contiene il numero della carta
When l'operatore lascia vuoto il controllo "corrispondenza numero carta"
Then la checklist può essere comunque salvata
And il controllo non concorre al calcolo esito
```

### AC-E6.04 – Calcolo esito Approvata
```gherkin
Given tutti i controlli obbligatori della checklist sono valorizzati con "Si"
When l'operatore preme "SALVA E PROSEGUI"
Then la card di Riepilogo mostra esito "Approvata" in verde
```

### AC-E6.05 – Calcolo esito Respinta
```gherkin
Given almeno un controllo obbligatorio è valorizzato con "No"
When l'operatore preme "SALVA E PROSEGUI"
Then la card di Riepilogo mostra esito "Respinta" in rosso
And il campo Note interne diventa visibile e facoltativo
```

### AC-E6.06 – Modifica checklist salvata
```gherkin
Given una checklist è stata salvata in bozza
And la pratica non è ancora chiusa
When l'operatore clicca "MODIFICA"
Then la checklist torna editabile
And il salvataggio successivo aggiorna l'esito calcolato
```

---

## E7 – Checklist Carta e Chiusura E2E

### AC-E7.01 – Checklist Carta
```gherkin
Given la tipizzazione è "Carta"
When l'operatore apre la milestone Verifica Documento
Then vede la checklist specifica per la Carta tagliata (presenza e conformità)
```

### AC-E7.02 – Chiusura task
```gherkin
Given la checklist è salvata e l'esito è calcolato
When l'operatore clicca "CHIUDI PRATICA"
Then il task viene rimosso dalla Lista Attività dell'operatore
And la pratica passa allo stato "In Attesa Conferma BPM"
```

### AC-E7.03 – Invio esito a BPM con KO multipli
```gherkin
Given una pratica con più controlli falliti (es. Firme + Timbro)
When SD invia l'esito al sistema BPM
Then il payload contiene tutti i codici causale KO riscontrati
```

### AC-E7.04 – Sincronizzazione finale
```gherkin
Given una pratica in "In Attesa Conferma BPM"
When SD riceve la conferma di ricezione esito da BPM (esito positivo)
Then la pratica passa a "Chiusa OK"
And la Data Chiusura viene valorizzata con sysdate
And l'evento è tracciato in tab Stati
```

### AC-E7.05 – Chiusura KO
```gherkin
Given una pratica respinta in "In Attesa Conferma BPM"
When SD riceve la conferma da BPM
Then la pratica passa a "Chiusa KO"
And la Data Chiusura viene valorizzata
```

---

## E8 – Riassegna Attività (Supervisore)

### AC-E8.01 – Accesso esclusivo
```gherkin
Given l'utente loggato ha ruolo OPERATORE
When tenta di accedere al tab "Riassegna Attività"
Then l'accesso è negato
```

### AC-E8.02 – Riassegnazione a gruppo
```gherkin
Given un task assegnato all'utente "U1"
And il Supervisore è loggato
When seleziona il task e sceglie "Riassegna a Gruppo Operatore ANC"
Then il task non è più assegnato a "U1"
And torna nella coda comune del gruppo
And l'azione è registrata in Cronologia
```

### AC-E8.03 – Riassegnazione a utente
```gherkin
Given un task in coda al Gruppo Operatore ANC
When il Supervisore lo riassegna all'utente "U2"
Then il task compare nella Lista Attività di "U2"
```

---

## E9 – Home Supervisore

### AC-E9.01 – Contatori real-time
```gherkin
Given il Supervisore è sulla Home Page
When il numero di pratiche attive cambia
Then il contatore "Pratiche Attive" si aggiorna senza ricaricare la pagina
```

### AC-E9.02 – Istogramma per stato
```gherkin
Given esistono pratiche in stati Aperta, In Lavorazione, In Attesa Conferma BPM, Chiusa OK, Chiusa KO
When il Supervisore consulta l'istogramma "Pratiche per Stato"
Then ogni stato è rappresentato con la quantità corretta
```

### AC-E9.03 – Selezione mese
```gherkin
Given il Supervisore consulta gli istogrammi
When seleziona un mese diverso dal calendario
Then i grafici vengono ricalcolati per il mese selezionato
```

---

## E10 – Dashboard Segnalazioni

### AC-E10.01 – Invio segnalazione
```gherkin
Given una pratica ANC in stato qualsiasi
When l'utente compila e invia una nuova segnalazione
Then la segnalazione compare in stato "In Coda"
And risulta visibile in vista globale e (per il creatore) in "Le Mie Segnalazioni"
```

### AC-E10.02 – Riassegnazione segnalazione
```gherkin
Given una segnalazione attiva
And l'utente loggato è Supervisore
When la riassegna a un operatore specifico
Then la segnalazione risulta assegnata a quell'operatore
And l'evento è tracciato
```

---

## E11 – UX accessoria

### AC-E11.01 – Help in linea
```gherkin
Given un controllo della checklist
When l'operatore clicca "Mostra Descrizione"
Then viene mostrata la descrizione operativa di quel controllo
```

### AC-E11.02 – Export Excel
```gherkin
Given la lista pratiche è filtrata
When l'utente clicca "Esporta Excel"
Then viene scaricato un file in formato foglio di calcolo (Excel) con i record correntemente filtrati
```
