# 02 - Capability Map

Classificazione:
- **CORE_POC** → indispensabile alla baseline end-to-end della POC.
- **OPTIONAL_POC** → utile, alleggerisce il valore demo ma non blocca il flusso.
- **FUTURE_ENTERPRISE** → previsto a regime, fuori dalla POC.
- **OUT_OF_SCOPE** → non oggetto di porting.

## C1. Integrazione e Acquisizione Dati

| ID | Capability | Descrizione | Classificazione |
|---|---|---|---|
| C1.1 | Servizio apertura pratica (BPM→SD) | Servizio per ricezione richiesta + allegati (contratto Interface Agreement BPM↔SD, `InterfaceAgreement.md`) | CORE_POC |
| C1.2 | Validazione messaggio in ingresso / campi obbligatori | Verifica struttura, dominio CODICE_DOC_ID (1,2,3), oggetto DOCUMENTI | CORE_POC |
| C1.3 | Idempotenza ID_WORKITEM | Blocco duplicati, resultCode -5 | CORE_POC |
| C1.4 | Error handling con resultCode tipizzati | -4 (messaggio in ingresso non valido), -5 (duplicato), messaggi descrittivi | CORE_POC |
| C1.5 | Stub BPM (mock chiamante) | Simulazione richieste per sviluppo/demo | CORE_POC |

## C2. Gestione e Monitoraggio Pratiche

| ID | Capability | Descrizione | Classificazione |
|---|---|---|---|
| C2.1 | Creazione pratica stato "Aperta" | Persistenza pratica + dati cliente + dati carta bloccata. Generazione interna dell'identificativo pratica (`requestId`, chiave di riconciliazione cross-sistema, internalizzata nel porting open source). | CORE_POC |
| C2.2 | Lista Pratiche con filtri | Filtri: Pratica N°, Stato, Data Apertura/Chiusura/UltimaModifica, Esito SD | CORE_POC |
| C2.3 | Ordinamento colonne | Asc/Desc su tutte le colonne (eccetto Assegnatari) | CORE_POC |
| C2.4 | Paginazione liste | Comandi `<<`, `<`, `>`, `>>` | CORE_POC |
| C2.5 | Export Excel lista pratiche | Esportazione lista pratiche in formato foglio di calcolo | OPTIONAL_POC |
| C2.6 | Dettaglio Pratica – Riepilogo | Linea avanzamento + dati testata, cliente, carta | CORE_POC |
| C2.7 | Dettaglio Pratica – Cronologia | Log attività + utente + descrizione evento | CORE_POC |
| C2.8 | Dettaglio Pratica – Stati | Storico transizioni di stato | CORE_POC |
| C2.9 | Dettaglio Pratica – Azioni Correlate | Vista azioni sussidiarie | OPTIONAL_POC |

## C3. Workflow Operativo (Task Management)

| ID | Capability | Descrizione | Classificazione |
|---|---|---|---|
| C3.1 | Generazione automatica task | Creazione task in Lista Attività alla creazione pratica | CORE_POC |
| C3.2 | Lista Attività (coda) | Vista task con filtri (Pratica N°, Stato), ordinamento | CORE_POC |
| C3.3 | Presa in carico ("ACCETTA") | Cambia owner del task, transizione pratica → In Lavorazione | CORE_POC |
| C3.4 | Tasto "Indietro" su accettazione | Annulla la presa in carico prima del commit | OPTIONAL_POC |
| C3.5 | Filtro tipo pratica fisso ANC | Default "Attivazione Nuova Carta", non editabile | CORE_POC |

## C4. Esecuzione Verifiche di Conformità

| ID | Capability | Descrizione | Classificazione |
|---|---|---|---|
| C4.1 | Visualizzatore documenti integrato | Anteprima allegati (zoom, dimensioni piccolo/medio/grande) | CORE_POC |
| C4.2 | Download manuale fallback | Download file in caso di fallimento viewer | CORE_POC |
| C4.3 | Tipizzazione manuale documento | Selezione Verbale/Carta + CONFERMA (irreversibile) | CORE_POC |
| C4.4 | Checklist Verbale di Denuncia | Controlli leggibilità, idoneità formale, coerenza dati cliente, numero carta | CORE_POC |
| C4.5 | Checklist Carta tagliata | Controlli conformità immagine carta | CORE_POC |
| C4.6 | Causali KO formali | Intestazione, Firme, Timbro, Dichiarazione, Carta Poste Italiane | CORE_POC |
| C4.7 | Logica condizionale checklist | "No" su presenza documento → cascata KO automatica | CORE_POC |
| C4.8 | Salva e Prosegui (bozza) | Salvataggio intermedio + abilitazione Riepilogo | CORE_POC |
| C4.9 | Modifica checklist salvata | Riapertura editing prima della chiusura | CORE_POC |
| C4.10 | Help in linea ("Mostra Descrizione") | Istruzioni per ogni controllo | OPTIONAL_POC |
| C4.11 | Espandi/Comprimi sezioni UI | Indirizzo residenza, preview allegato | OPTIONAL_POC |
| C4.12 | Box informativo errore tecnico | Istruzione su tipizzare e chiudere KO se download fallisce | OPTIONAL_POC |

## C5. Automazione Esiti e Chiusura

| ID | Capability | Descrizione | Classificazione |
|---|---|---|---|
| C5.1 | Calcolo esito automatico | Approvata se tutti SI, Respinta se almeno un NO | CORE_POC |
| C5.2 | Note interne su Respinta | Campo note facoltativo, visibile solo se KO | CORE_POC |
| C5.3 | Chiusura task ("CHIUDI PRATICA") | Rimozione task da lista, transizione → In Attesa Conferma BPM | CORE_POC |
| C5.4 | Invio esito a BPM (single KO/OK) | Comunicazione esito + codici motivazione | CORE_POC |
| C5.5 | Invio esito a BPM con KO Multipli | Lista completa motivazioni KO | CORE_POC |
| C5.6 | Sincronizzazione finale stato | Conferma BPM → Chiusa OK / Chiusa KO + sysdate | CORE_POC |
| C5.7 | Stub BPM ricezione esiti | Simulazione conferma per chiusura definitiva | CORE_POC |

## C6. Supervisione e Governance

| ID | Capability | Descrizione | Classificazione |
|---|---|---|---|
| C6.1 | Tab Riassegna Attività | Vista task processi di competenza | CORE_POC |
| C6.2 | Riassegnazione a Gruppo | Spostamento task → Gruppo Operatore ANC | CORE_POC |
| C6.3 | Riassegnazione a Utente | Spostamento task → utente specifico | CORE_POC |
| C6.4 | Filtri Riassegna (Owner, Assegnatario, Data) | Filtri operativi sulla coda | CORE_POC |
| C6.5 | Contatori real-time Home | Attività, Pratiche Attive, Pratiche Chiuse | CORE_POC |
| C6.6 | Istogramma Pratiche Giornaliere | Aperte per giorno (mese selezionabile) | OPTIONAL_POC |
| C6.7 | Istogramma Pratiche Lavorate | Chiuse OK vs KO per giorno | OPTIONAL_POC |
| C6.8 | Istogramma Pratiche per Stato | Distribuzione corrente per stato | OPTIONAL_POC |

## C7. Dashboard Segnalazioni (Sinergia / PIX)

| ID | Capability | Descrizione | Classificazione |
|---|---|---|---|
| C7.1 | Invio segnalazione | Creazione segnalazione legata a pratica ANC | OPTIONAL_POC |
| C7.2 | Vista "Le Mie Segnalazioni" | Filtro per utente in carico | OPTIONAL_POC |
| C7.3 | Vista globale Segnalazioni | Filtri ID, stato, operatore, range temporale | OPTIONAL_POC |
| C7.4 | Riassegna segnalazioni | Assegnazione a operatore/gruppo/sé | OPTIONAL_POC |
| C7.5 | Stub Sinergia (apertura ticket, recupero pratiche/dettaglio) | Simulazione integrazione PIX | OPTIONAL_POC |

## C8. Home Page e Produttività

| ID | Capability | Descrizione | Classificazione |
|---|---|---|---|
| C8.1 | Tab navigazione (Home/Attività/Pratiche) | Layout principale | CORE_POC |
| C8.2 | Login con credenziali | Autenticazione locale (OPERATORE/SUPERVISORE) | CORE_POC |
| C8.3 | Link Favoriti (CRUD) | Aggiunta/modifica/eliminazione link (Interno/Esterno/Legacy) | OPTIONAL_POC |
| C8.4 | Widget posta elettronica | Accesso rapido webmail | FUTURE_ENTERPRISE |

## C9. Audit, Sicurezza, Integrazioni Sistemiche

| ID | Capability | Descrizione | Classificazione |
|---|---|---|---|
| C9.1 | Audit trail granulare | Log azioni utente per pratica | CORE_POC |
| C9.2 | Storico transizioni di stato | Persistenza state log | CORE_POC |
| C9.3 | Autorizzazione per ruolo | Operatore vs Supervisore (riassegnazione) | CORE_POC |
| C9.4 | Integrazione Data Lake | Invio dati per analytics | FUTURE_ENTERPRISE |
| C9.5 | SSO enterprise / IAM | Federazione identità Poste | FUTURE_ENTERPRISE |
| C9.6 | Conservazione documentale a norma | Archiviazione long-term | OUT_OF_SCOPE |
