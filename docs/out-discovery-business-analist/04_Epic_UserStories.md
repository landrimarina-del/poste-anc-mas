# 04 - Epic e User Stories

Formato: **As a … I want … so that …**
Le US sono raggruppate per **Epic**, allineate alle Capability (cap. 02) e mappate agli Sprint (cap. 03).

---

## EPIC E1 – Foundation & Autenticazione (Sprint 0)

- **US-E1.01** — As an Operatore, I want to fare login con username e password, so that posso accedere alla mia coda di lavoro.
- **US-E1.02** — As a Supervisore, I want to fare login con un profilo dedicato, so that posso accedere agli strumenti di supervisione.
- **US-E1.03** — As a utente autenticato, I want to navigare tra i tab Home / Attività / Pratiche, so that posso muovermi nelle aree principali.
- **US-E1.04** — As a sistema, I want to bloccare le rotte protette agli utenti non autenticati, so that proteggo i dati operativi.

## EPIC E2 – Integrazione BPM e Apertura Pratica (Sprint 1)

- **US-E2.01** — As a BPM, I want to inviare a SD una richiesta di apertura pratica con dati cliente, dati carta e allegati, so that la pratica viene creata in stato "Aperta".
- **US-E2.02** — As a sistema SD, I want to validare che la richiesta contenga l'oggetto `DOCUMENTI`, so that rifiuto richieste incomplete.
- **US-E2.03** — As a sistema SD, I want to validare `CODICE_DOC_ID ∈ {1,2,3}`, so that rifiuto messaggi non conformi con `resultCode: -4`.
- **US-E2.04** — As a sistema SD, I want to verificare l'idempotenza su `ID_WORKITEM`, so that non creo pratiche duplicate (`resultCode: -5`).
- **US-E2.05** — As a sistema SD, I want to persistere l'allegato ricevuto, so that l'operatore potrà visualizzarlo successivamente.
- **US-E2.06** — As a sistema SD, I want to registrare l'evento di apertura nell'audit trail, so that la cronologia è ricostruibile.
- **US-E2.07** — As a sistema SD, I want to validare che la testata contenga tutti i campi obbligatori (CANALE, ID_WORKITEM, NUM_PRATICA, CF_CLIENTE, DATA_INSERIMENTO_RICHIESTA, CLIENTE, DATI_CARTA_BLOCCATA, DOCUMENTI), so that rifiuto messaggi incompleti con `resultCode: -4`.
- **US-E2.08** — As a sistema SD, I want to validare che l'oggetto CLIENTE contenga i campi obbligatori (NOME, COGNOME, DATANASCITA, COMUNENASCITA, PROVINCIANASCITA, NAZIONENASCITA, CITTADINANZA), so that rifiuto messaggi non conformi con `resultCode: -4`.
- **US-E2.09** — As a sistema SD, I want to validare che DATI_CARTA_BLOCCATA contenga I_NUMERO_CARTA e I_TIPO_CARTA, so that rifiuto messaggi privi di dati carta minimi con `resultCode: -4`.
- **US-E2.10** — As a sistema SD, I want to validare che ESTENSIONE di ogni allegato in DOCUMENTI.CONTENUTI appartenga al dominio {pdf, jpeg, jpg, png}, so that rifiuto allegati non ammessi con `resultCode: -4`.
- **US-E2.11** — As a sistema SD, I want to persistere tutti i campi della richiesta valida (testata, CLIENTE incluso INDIRIZZO_DI_RESIDENZA, DATI_CARTA_BLOCCATA, metadati DOCUMENTI/CONTENUTI), so that il dettaglio pratica è completo come in baseline.

## EPIC E3 – Repository Pratiche (Sprint 2)

- **US-E3.01** — As a utente, I want to visualizzare la lista completa delle pratiche, so that posso consultare lo storico.
- **US-E3.02** — As a utente, I want to filtrare le pratiche per N°, Stato, Data Apertura/Chiusura/Ultima Modifica, Esito SD, so that trovo rapidamente l'istanza di interesse.
- **US-E3.03** — As a utente, I want to ordinare le colonne in asc/desc, so that organizzo la vista.
- **US-E3.04** — As a utente, I want to navigare le pagine dei risultati, so that gestisco liste lunghe.
- **US-E3.05** — As a utente, I want to aprire il dettaglio di una pratica, so that vedo dati di testata, cliente, carta bloccata.
- **US-E3.06** — As a utente, I want to visualizzare il tab Cronologia di una pratica, so that vedo lo storico delle attività e degli utenti.
- **US-E3.07** — As a utente, I want to visualizzare il tab Stati, so that vedo la sequenza dei cambiamenti di stato.

## EPIC E4 – Lista Attività e Presa in Carico (Sprint 3)

- **US-E4.01** — As a sistema, I want to generare automaticamente un task alla creazione della pratica, so that compare nella Lista Attività.
- **US-E4.02** — As an Operatore, I want to vedere la mia coda nel tab Attività con tipo pratica = ANC, so that vedo i task da lavorare.
- **US-E4.03** — As an Operatore, I want to filtrare la coda per Pratica N° e Stato, so that trovo rapidamente un task.
- **US-E4.04** — As an Operatore, I want to cliccare ACCETTA su un task, so that lo prendo in carico e la pratica passa a "In Lavorazione".
- **US-E4.05** — As an Operatore, I want to tornare indietro dalla schermata di accettazione senza prendere in carico, so that posso annullare la scelta.

## EPIC E5 – Tipizzazione e Visualizzazione Documenti (Sprint 4)

- **US-E5.01** — As an Operatore, I want to visualizzare l'allegato della pratica in un viewer integrato, so that posso analizzarlo senza scaricarlo.
- **US-E5.02** — As an Operatore, I want to regolare la dimensione dell'anteprima (piccolo/medio/grande), so that leggo agevolmente il documento.
- **US-E5.03** — As an Operatore, I want to scaricare manualmente il documento se il viewer fallisce, so that posso comunque procedere.
- **US-E5.04** — As an Operatore, I want to selezionare manualmente il tipo documento (Verbale / Carta) e CONFERMARE, so that il sistema attiva la checklist corretta.
- **US-E5.05** — As a sistema, I want to rendere irreversibile la scelta di tipizzazione, so that è preservata la coerenza della pratica.
- **US-E5.06** — As an Operatore, I want to vedere un box informativo se non riesco a scaricare l'allegato, so that so come procedere (tipizzare e chiudere KO).

## EPIC E6 – Istruttoria e Checklist Verbale (Sprint 5)

- **US-E6.01** — As an Operatore, I want to dichiarare se il Verbale è presente (Si/No), so that il sistema sa se proseguire i controlli.
- **US-E6.02** — As a sistema, I want to forzare KO automatico su tutti i controlli se la presenza è "No", so that la pratica va in Respinta coerentemente.
- **US-E6.03** — As an Operatore, I want to compilare i controlli di leggibilità, idoneità formale, coerenza dati cliente, so that valido la conformità del verbale.
- **US-E6.04** — As an Operatore, I want to indicare la causale di KO (Intestazione/Firme/Timbro/Dichiarazione/Carta PI) se l'idoneità formale è "No", so that il motivo è tracciato e inviato a BPM.
- **US-E6.05** — As an Operatore, I want to compilare la corrispondenza numero carta solo se presente nel verbale, so that gestisco il controllo facoltativo.
- **US-E6.06** — As an Operatore, I want to salvare la checklist in bozza con "SALVA E PROSEGUI", so that posso passare al Riepilogo.
- **US-E6.07** — As an Operatore, I want to modificare una checklist salvata, so that correggo eventuali errori prima della chiusura.
- **US-E6.08** — As a sistema, I want to calcolare automaticamente l'esito (Approvata se tutti SI, Respinta se almeno un NO), so that l'operatore non può forzare l'esito.
- **US-E6.09** — As an Operatore, I want to inserire note interne facoltative se la pratica è Respinta, so that dettaglio le motivazioni.

## EPIC E7 – Checklist Carta e Chiusura E2E con BPM (Sprint 6)

- **US-E7.01** — As an Operatore, I want to compilare la checklist Carta tagliata (presenza e conformità), so that valuto la conformità della carta.
- **US-E7.02** — As an Operatore, I want to cliccare "CHIUDI PRATICA", so that il task viene rimosso dalla mia lista e la pratica passa a "In Attesa Conferma BPM".
- **US-E7.03** — As a sistema, I want to inviare a BPM l'esito (OK / single KO / KO multipli) con i codici causale, so that il flusso a valle può proseguire.
- **US-E7.04** — As a sistema, I want to ricevere la conferma da BPM e portare la pratica a "Chiusa OK" o "Chiusa KO", valorizzando la data chiusura, so that il ciclo si chiude formalmente.
- **US-E7.05** — As a sistema, I want to registrare ogni transizione nello storico Stati e nella Cronologia, so that l'audit è completo.

## EPIC E8 – Riassegna Attività (Sprint 7)

- **US-E8.01** — As a Supervisore, I want to accedere al tab "Riassegna Attività", so that vedo tutti i task dei processi di mia competenza.
- **US-E8.02** — As a Supervisore, I want to filtrare per Pratica N°, Data Assegnazione, Owner, Assegnatario, so that individuo task da spostare.
- **US-E8.03** — As a Supervisore, I want to riassegnare un task al "Gruppo Operatore ANC", so that lo rimetto nella coda comune.
- **US-E8.04** — As a Supervisore, I want to riassegnare un task a un Utente specifico, so that bilancio i carichi.
- **US-E8.05** — As a sistema, I want to consentire la riassegnazione solo al ruolo SUPERVISORE, so that è rispettata la governance.

## EPIC E9 – Home Supervisore (Sprint 8)

- **US-E9.01** — As a Supervisore, I want to vedere in alto a destra i contatori Attività / Pratiche Attive / Pratiche Chiuse, so that monitoro i volumi in tempo reale.
- **US-E9.02** — As a Supervisore, I want to consultare l'istogramma "Pratiche Giornaliere" per il mese selezionato, so that valuto il volume in ingresso.
- **US-E9.03** — As a Supervisore, I want to consultare l'istogramma "Pratiche Giornaliere Lavorate" suddiviso OK/KO, so that misuro la produttività.
- **US-E9.04** — As a Supervisore, I want to consultare l'istogramma "Pratiche per Stato", so that identifico colli di bottiglia.
- **US-E9.05** — As a Supervisore, I want to selezionare il mese da calendario, so that filtro le statistiche.

## EPIC E10 – Dashboard Segnalazioni Sinergia (Sprint 9)

- **US-E10.01** — As an Operatore/Supervisore, I want to inviare una segnalazione legata a una pratica ANC, so that la inoltro al sistema esterno Sinergia.
- **US-E10.02** — As a utente, I want to vedere "Le Mie Segnalazioni", so that monitoro quelle in mio carico.
- **US-E10.03** — As a Supervisore, I want to vedere la vista globale Segnalazioni con filtri (ID, stato, operatore, range temporali), so that controllo lo stato del modulo.
- **US-E10.04** — As a Supervisore, I want to riassegnare una segnalazione attiva a un operatore/gruppo/me, so that ne garantisco la presa in carico.
- **US-E10.05** — As a sistema, I want to simulare l'apertura ticket verso stub Sinergia, so that la POC dimostra l'integrazione.

## EPIC E11 – Hardening, UX accessoria, Audit (Sprint 10)

- **US-E11.01** — As an Operatore, I want to cliccare "Mostra Descrizione" su ogni controllo della checklist, so that leggo le istruzioni operative.
- **US-E11.02** — As a utente, I want to espandere/comprimere sezioni (indirizzo residenza, preview), so that ottimizzo lo spazio.
- **US-E11.03** — As a utente, I want to gestire i Link Favoriti (Titolo, URL, Tipo Interno/Esterno/Legacy), so that velocizzo le attività accessorie.
- **US-E11.04** — As a utente, I want to esportare la lista pratiche in Excel, so that posso analizzarla offline.
- **US-E11.05** — As a utente, I want to consultare il tab "Azioni Correlate" sul dettaglio pratica, so that vedo eventuali attività sussidiarie.
- **US-E11.06** — As a sistema, I want to esporre un audit trail completo per ogni pratica, so that la tracciabilità è totale.
