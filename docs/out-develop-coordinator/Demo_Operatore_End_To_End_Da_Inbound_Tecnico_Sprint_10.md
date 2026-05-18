# Demo Operatore End-to-End da Inbound Tecnico (Sprint 10)

Data: 2026-05-16  
Target: demo funzionale lato utente OPERATORE ANC  
Obiettivo: mostrare l'intero giro dalla creazione pratica via inbound tecnico fino alle interazioni operative complete in app.

## 1. Perimetro demo

Questo script copre:
1. Ingresso tecnico pratica (simulazione BPM inbound).
2. Login operatore e presa in carico task.
3. Tipizzazione documento e checklist.
4. Chiusura pratica e verifica avanzamento stato.
5. Consultazione dettaglio pratica (Azioni Correlate, Storico).
6. Gestione Segnalazioni lato operatore.
7. Funzioni Sprint 10 lato operatore (Export, Link Favoriti).

## 2. Prerequisiti

1. Stack locale avviato (`docker compose up -d --build`).
2. Reverse proxy disponibile su `http://localhost`.
3. Credenziali demo operatore:
- username: `op.rossi`
- password: `Demo1234!`
4. DB pulito consigliato per demo narrativa completa (opzionale ma raccomandato).

## 3. Step 0 - Inbound tecnico pratica (regia tecnica, pre-demo operatore)

Nota: questo step non e una azione UI operatore; serve a popolare la coda lavori che l'operatore vedra in app.

Eseguire da terminale:

```powershell
$base='http://localhost'
$payload = @'
{
  "CANALE": "APP_POSTEPAY",
  "ID_WORKITEM": "POC-WI-0001",
  "NUM_PRATICA": "POC-PRAT-0001",
  "CF_CLIENTE": "RSSMRA80A01H501U",
  "CODICE_CLIENTE": "CLT001",
  "DATA_INSERIMENTO_RICHIESTA": "16/05/2026 10:20:00",
  "CLIENTE": {
    "COGNOME": "ROSSI",
    "NOME": "MARIO",
    "CODICE_FISCALE": "RSSMRA80A01H501U",
    "DATA_NASCITA": "01/01/1980",
    "SESSO": "M",
    "COMUNENASCITA": "ROMA",
    "PROVINCIANASCITA": "RM",
    "NAZIONENASCITA": "ITALIA",
    "CITTADINANZA": "ITALIANA"
  },
  "DATI_CARTA_BLOCCATA": {
    "I_NUMERO_CARTA": "1234567890123456",
    "I_TIPO_CARTA": "POSTEPAY"
  },
  "DOCUMENTI": [
    {
      "CODICE_DOC_ID": 1,
      "CONTENUTI": [
        {
          "NOME_FILE": "sample",
          "ESTENSIONE": "pdf",
          "ID_DOC": "DOC-POC-001",
          "LINKDOWNLOAD": "http://bpm-stub/files/sample.pdf"
        }
      ]
    }
  ]
}
'@

curl.exe -s -H "Content-Type: application/json" -H "X-SD-API-Key: anc-poc-bpm-inbound-key" -d $payload "$base/api/v1/bpm/practices"
```

Esito atteso:
- `resultCode = 0`
- `details.state = APERTA`

## 4. Step 1 - Login operatore in applicazione

1. Aprire `http://localhost`.
2. Eseguire login con `op.rossi / Demo1234!`.
3. Verificare atterraggio su Home operatore.

Messaggio demo suggerito:
- "Accedo come operatore ANC; ora vedo il mio perimetro operativo e posso lavorare la coda task."

## 5. Step 2 - Apertura coda attivita e presa in carico

1. Entrare nella sezione Attivita.
2. Verificare presenza task in stato `In coda` (`IN_CODA`) relativo alla pratica inbound.
3. Cliccare `ACCETTA`.
4. Verificare passaggio a `IN_CARICO` e disponibilita del link tipizzazione.

Messaggio demo suggerito:
- "La pratica arriva da inbound tecnico, ma la lavorazione parte solo quando l'operatore la prende in carico."

## 6. Step 3 - Tipizzazione documento

1. Aprire pagina tipizzazione della pratica (da Attivita).
2. Selezionare tipo documento `VERBALE` (in alternativa `CARTA`).
3. Premere conferma tipizzazione.
4. Verificare messaggio informativo di tipizzazione confermata.

Messaggio demo suggerito:
- "La tipizzazione e irreversibile per garantire coerenza del ramo checklist."

## 7. Step 4 - Checklist operatore e salvataggio

Nella `Checklist Verbale`:
1. Compilare i controlli obbligatori (esempio positivo):
- presenza documento = SI
- leggibilita = SI
- conformita formale = SI
- coerenza dati cliente = SI
- controllo numero carta richiesto = NO
2. Inserire eventuali note interne.
3. Premere `SALVA E PROSEGUI`.
4. Verificare:
- stato checklist aggiornato
- eventuale badge bozza
- esito riepilogo valorizzato

5. Aprire `Mostra Descrizione` su un item checklist (es. `DOCUMENTPRESENT`) e verificare contenuto help.

Messaggio demo suggerito:
- "L'operatore riceve guida contestuale, riduce errori e standardizza la valutazione documentale."

## 8. Step 5 - Chiusura lavorazione operatore

1. Premere `CHIUDI PRATICA`.
2. Verificare messaggio: stato pratica aggiornato a `IN_ATTESA_CONFERMA_BPM`.

Messaggio demo suggerito:
- "La parte operatore termina qui: la pratica passa in attesa della conferma di esito dal BPM."

## 9. Step 6 - Consultazione dettaglio pratica in app (lato operatore)

1. Aprire `Pratiche`.
2. Cercare la pratica (`POC-PRAT-0001`).
3. Aprire dettaglio pratica.
4. Navigare tab/aree:
- `Azioni Correlate`
- storico/lifecycle (history/states)
5. Verificare coerenza del percorso eseguito (apertura, presa in carico, tipizzazione, checklist, close).

Messaggio demo suggerito:
- "L'audit trail e consultabile e ricostruisce l'intera catena operativa."

## 10. Step 7 - Gestione Segnalazioni lato operatore

1. Dal dettaglio pratica, usare il pulsante/azione che apre `Segnalazioni` con contesto pratica precompilato.
2. In `Dashboard Segnalazioni`, compilare il form di nuova segnalazione e inviare.
3. Verificare che la segnalazione compaia in `Le Mie Segnalazioni`.
4. Verificare stato iniziale segnalazione (`IN_CODA` o `IN_LAVORAZIONE` in base al flusso).
5. Se disponibile sulla riga, usare `FORWARD SINERGIA` e verificare messaggio di successo con eventuale ticket.

Messaggio demo suggerito:
- "L'operatore puo aprire e seguire segnalazioni direttamente dal contesto pratica, mantenendo tracciabilita operativa end-to-end."

## 11. Step 8 - Funzioni Sprint 10 lato operatore

## 10.1 Export lista pratiche
1. Da lista pratiche eseguire export.
2. Verificare download file Excel.

## 10.2 Link Favoriti in Home
1. Tornare su Home.
2. Sezione `Link Favoriti`.
3. Creare un link con:
- Titolo: `Lista pratiche`
- URL: `/pratiche` oppure dominio esterno
- Tipo: `ESTERNO`
4. Verificare apertura link e operazioni CRUD (modifica/elimina).

Messaggio demo suggerito:
- "Le capability Sprint 10 migliorano usabilita e produttivita operatore senza alterare il workflow core."

## 12. Step 9 - (Opzionale di regia) ACK BPM e chiusura finale

Nota: non e un'azione operatore UI, ma completa la narrativa E2E.

Da terminale:

```powershell
$base='http://localhost'
$ack='{"correlationId":"POC-ACK-0001","practiceId":1,"requestId":"POC-PRAT-0001","outcome":"OK","koCodes":[]}'
curl.exe -s -u 'op.rossi:Demo1234!' -H "Content-Type: application/json" -d $ack "$base/api/v1/bpm/outcome-ack"
```

Esito atteso:
- `resultCode = 0`
- `finalState = CHIUSA_OK`

Poi in app:
1. riaprire dettaglio pratica;
2. verificare stato finale aggiornato e storico arricchito.

## 13. Checklist rapida di successo demo

La demo e riuscita se sono veri tutti i punti:
1. la pratica e creata da inbound tecnico e compare in coda operatore.
2. l'operatore esegue ACCETTA -> tipizzazione -> checklist -> chiusura.
3. help checklist e azioni correlate sono consultabili.
4. gestione segnalazioni operatore (creazione + lista personale + forward se disponibile) funzionante.
5. export Excel e link favoriti funzionano.
6. storico pratica mostra traccia completa delle azioni.

## 14. Incidenti comuni e recupero rapido

1. Nessun task visibile in Attivita:
- verificare di aver eseguito lo Step 0 inbound.
- verificare login come `op.rossi`.

2. Errori 404 su help/related/history:
- su DB pulito avvengono se la pratica non e stata ancora creata.

3. Impossibile tipizzare:
- il task deve essere prima accettato (`IN_CARICO`).

4. Impossibile chiudere pratica:
- compilare i campi checklist obbligatori e salvare prima la bozza.

5. Segnalazione non visibile in `Le Mie Segnalazioni`:
- aggiornare la dashboard segnalazioni;
- verificare che l'invio sia andato a buon fine (messaggio di successo);
- controllare eventuali filtri stato applicati.
