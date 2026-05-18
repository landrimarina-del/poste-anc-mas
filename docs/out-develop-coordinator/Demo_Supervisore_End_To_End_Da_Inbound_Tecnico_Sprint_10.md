# Demo Supervisore End-to-End da Inbound Tecnico (Sprint 10)

Data: 2026-05-16  
Target: demo funzionale lato utente SUPERVISORE ANC  
Obiettivo: mostrare il ciclo completo di supervisione dopo apertura tecnica pratica e includere governance task/segnalazioni.

## 1. Perimetro demo supervisore

Questo script copre:
1. Inbound tecnico pratica (pre-condizione operativa).
2. Dashboard supervisore (contatori + grafici).
3. Supervisione task e riassegnazione (`Riassegna Attivita`).
4. Gestione segnalazioni (`Vista globale supervisore`, forward Sinergia, riassegnazione).
5. Consultazione pratica e audit trail da area `Pratiche`.
6. Chiusura narrativa end-to-end con ACK BPM (step tecnico opzionale di regia).

## 2. Prerequisiti

1. Stack locale attivo su `http://localhost`.
2. Utente supervisore disponibile:
- username: `sup.verdi`
- password: `Demo1234!`
3. DB pulito consigliato per demo lineare.

## 3. Step 0 - Inbound tecnico pratica (regia)

Nota: come per demo operatore, questo step inizializza la pratica e la coda task.

```powershell
$base='http://localhost'
$payload = @'
{
  "CANALE": "APP_POSTEPAY",
  "ID_WORKITEM": "POC-WI-SUP-0001",
  "NUM_PRATICA": "POC-PRAT-SUP-0001",
  "CF_CLIENTE": "VRDGPP80A01H501X",
  "CODICE_CLIENTE": "CLT-SUP-001",
  "DATA_INSERIMENTO_RICHIESTA": "16/05/2026 11:00:00",
  "CLIENTE": {
    "COGNOME": "VERDI",
    "NOME": "GIUSEPPE",
    "CODICE_FISCALE": "VRDGPP80A01H501X",
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
          "ID_DOC": "DOC-SUP-001",
          "LINKDOWNLOAD": "http://bpm-stub/files/sample.pdf"
        }
      ]
    }
  ]
}
'@

curl.exe -s -H "Content-Type: application/json" -H "X-SD-API-Key: anc-poc-bpm-inbound-key" -d $payload "$base/api/v1/bpm/practices"
```

Esito atteso: `resultCode = 0`, pratica in stato `APERTA`.

## 4. Step 1 - Login supervisore

1. Aprire `http://localhost`.
2. Login con `sup.verdi / Demo1234!`.
3. Verificare tab disponibili per supervisore:
- `Home`
- `Riassegna Attivita`
- `Pratiche`
- `Segnalazioni`

Messaggio demo:
- "Il supervisore ha un cockpit differente dall'operatore, orientato a monitoraggio e riallocazione del lavoro."

## 5. Step 2 - Home supervisore (monitoraggio)

1. Restare su `Home`.
2. Verificare sezione `Welcome Page Supervisore`.
3. Verificare contatori:
- Attivita
- Pratiche Attive
- Pratiche Chiuse
4. Verificare grafici:
- `Pratiche Giornaliere`
- `Pratiche Giornaliere Lavorate (OK/KO)`
- `Pratiche per Stato`
5. Usare filtro mese `Mese (YYYY-MM)` e verificare refresh dati.

Messaggio demo:
- "Il supervisore controlla volumi e trend prima di intervenire sulle code."

## 6. Step 3 - Riassegna Attivita (task supervision)

1. Aprire tab `Riassegna Attivita`.
2. Verificare elenco task e filtri.
3. Selezionare un task e provare:
- `RIASSEGNA A GRUPPO`
- `RIASSEGNA A UTENTE` (inserendo username destinatario)
4. Verificare messaggi di successo:
- `Riassegnazione a Gruppo Operatore ANC completata.`
- `Riassegnazione completata verso utente ...`

Messaggio demo:
- "Il supervisore bilancia carico verso gruppo o singolo operatore senza uscire dal flusso applicativo."

## 7. Step 4 - Segnalazioni (governance supervisore)

1. Aprire tab `Segnalazioni`.
2. Verificare `Dashboard Segnalazioni`.
3. Nella `Vista globale supervisore`:
- applicare filtri
- premere `Aggiorna vista globale`
4. Su una riga segnalazione testare:
- `FORWARD SINERGIA`
- `RIASSEGNA` (target `USER`, `GROUP` o `ME`)
5. Verificare aggiornamento stato/ticket Sinergia.

Messaggio demo:
- "La supervisione non e solo consultiva: include azioni operative su segnalazioni e orchestrazione inter-canale."

## 8. Step 5 - Pratiche (consultazione e audit)

1. Aprire tab `Pratiche`.
2. Cercare la pratica inbound (`POC-PRAT-SUP-0001`).
3. Aprire dettaglio pratica.
4. Verificare:
- sezione/tab `Azioni Correlate`
- cronologia/stati della pratica

Messaggio demo:
- "Il supervisore puo ricostruire la storia completa e verificare coerenza tra workflow e operativita utente."

## 9. Step 6 - Chiusura narrativa E2E (opzionale tecnico)

Se si vuole mostrare anche stato finale pratica (`CHIUSA_OK`), eseguire ACK BPM:

```powershell
$base='http://localhost'
$ack='{"correlationId":"POC-ACK-SUP-0001","requestId":"POC-PRAT-SUP-0001","outcome":"OK","koCodes":[]}'
curl.exe -s -u 'sup.verdi:Demo1234!' -H "Content-Type: application/json" -d $ack "$base/api/v1/bpm/outcome-ack"
```

Poi ricaricare dettaglio pratica e verificare stato finale.

## 10. Checklist di successo demo supervisore

Demo riuscita se:
1. login supervisore mostra il menu dedicato.
2. dashboard home espone contatori/grafici coerenti.
3. riassegnazione task a gruppo/utente funziona.
4. vista globale segnalazioni e azioni `FORWARD SINERGIA`/`RIASSEGNA` funzionano.
5. dettaglio pratica mostra azioni correlate e audit trail consultabile.

## 11. Troubleshooting rapido

1. Nessun dato in dashboard supervisore:
- verificare apertura pratica inbound (Step 0).

2. Errori su riassegnazione utente:
- inserire username valido nel campo utente destinatario.

3. Segnalazioni non visibili in globale:
- usare `Aggiorna vista globale` e controllare filtri stato.

4. Audit pratica non aggiornato:
- ricaricare dettaglio pratica dopo azioni/ack.
