# GAP-Architettura — Patch Tecnica per l'Implementazione

> **Scopo**: Documento di patch architetturale in due parti:
> - **Parte A** (§§ GAP-US-01 → GAP-US-11): copertura delle 11 User Story validate dalla sessione GAP Review (`docs/out-GAP-Analysis/03_GAP_Coverage_Review.md § Testo completo US validate`).
> - **Parte B** (§§ TECNICO-GAP-A → TECNICO-GAP-F): requisiti tecnici rilevati nel reverse engineering (`02_bpm.md`, `03_technical.md`) non ancora coperti dai documenti architetturali esistenti né dalla Parte A. Aggiunti a seguito della verifica di copertura del 2026-05-21.
>
> **Data**: 2026-05-21  
> **Riferimenti**:  
> - US validate: `docs/out-GAP-Analysis/03_GAP_Coverage_Review.md`  
> - Architettura applicativa: `docs/out-discovery-architect/01_Architettura_Applicativa.md`  
> - Workflow: `docs/out-discovery-architect/04_Workflow_Architecture.md`  
> - API Candidate: `docs/out-discovery-architect/05_API_Candidate.md`  
> - State Management: `docs/out-discovery-architect/06_State_Management.md`  
> - Deployment: `docs/out-discovery-architect/07_Deployment_Locale.md`  
> - Mapping: `docs/out-discovery-architect/09_Mapping_Architetturale.md`

---

## Come leggere questo documento

Ogni sezione tratta una GAP-US e contiene:

| Blocco | Destinatario |
|---|---|
| **Impatto DB** | Discovery-DBA: tabelle/colonne da creare o modificare, vincoli, FK |
| **Impatto Backend** | Sviluppo BE: endpoint da aggiungere/modificare, logiche di servizio |
| **Impatto BPMN** | Sviluppo BE/BPM: modifiche ai processi Flowable |
| **Impatto Frontend** | Sviluppo FE: componenti React da creare, campi da esporre |
| **Configurazione** | Tutti: variabili d'ambiente, `application.yml` |

---

## GAP-US-02 — Chiamata BPM sincrona con retry configurabile

> **Priorità**: CRITICA — impatta il workflow `anc.main` e tutti i test di chiusura pratica.

### Synopsis

Il pattern originale nei documenti architetturali introduce un `evt.waitOutcomeAck` (BPMN message event) che attende che BPM chiami in ingresso `POST /bpm/outcome-ack`. La GAP-US-02 stabilisce invece una **chiamata sincrona con retry**: SD invia a BPM e riceve la risposta nella stessa HTTP response, poi aggiorna immediatamente lo stato della pratica. Il callback inbound da BPM non esiste.

### Impatto BPMN — `anc.main`

**Rimuovere** il nodo `evt.waitOutcomeAck` e l'endpoint `POST /api/v1/bpm/outcome-ack` come percorso primario.

**Sostituire** con il seguente flusso sincrono:

```
(SVC) svc.sendOutcomeToBpm
   │   - chiama bpm-stub POST /receive-outcome (con RetryTemplate)
   │   - timeout per tentativo: ${bpm.timeout-ms} (default 50000)
   │   - max retry: ${bpm.max-retry} (default 3)
   │   - intervallo retry: ${bpm.retry-interval-ms} (default 2000)
   │   - risposta attesa: { esito: boolean, descrizioneEsito: string }
   │
   ├─── esito = true  ──► svc.finalizeOnAck(CHIUSA_OK)
   │
   ├─── esito = false ──► svc.finalizeOnAck(CHIUSA_KO)
   │
   └─── retry esauriti (exception) ──► pratica resta IN_ATTESA_CONFERMA_BPM
                                        emette evento BpmSendFailed (audit)
                                        log ERROR con correlation_id
```

> Il timer di sicurezza (VC-S2 in `06_State_Management.md`) rimane valido come boundary event di fallback per le pratiche bloccate in `IN_ATTESA_CONFERMA_BPM`.

### Impatto DB

```sql
-- Tabella outbox BPM (persistenza tentativi)
CREATE TABLE bpm_outbound_message (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    practice_id        BIGINT       NOT NULL,
    stato_invio        TINYINT      NOT NULL DEFAULT 0,
    -- 0=attesa, 1=inviato_ok, 2=errore_transiente, 3=scartato (retry esauriti)
    retry_count        INT          NOT NULL DEFAULT 0,
    max_retry          INT          NOT NULL DEFAULT 3,
    payload_json       TEXT         NOT NULL,     -- payload inviato a BPM
    response_json      TEXT         NULL,         -- ultima risposta BPM
    error_message      VARCHAR(1000) NULL,
    created_at         DATETIME(3)  NOT NULL,
    last_attempt_at    DATETIME(3)  NULL,
    correlation_id     VARCHAR(36)  NOT NULL,
    CONSTRAINT fk_bpm_out_practice FOREIGN KEY (practice_id) REFERENCES practice(id),
    INDEX idx_bom_stato (stato_invio),
    INDEX idx_bom_practice (practice_id)
);
```

**Transizioni stato_invio**:
- `0 → 1`: risposta BPM con `esito=true/false` ricevuta
- `0 → 2`: errore o timeout nel tentativo (ancora retryable)
- `2 → 1`: retry riuscito
- `2 → 3`: retry_count ≥ max_retry

### Impatto Backend

**Nuovo servizio**: `BpmOutboundService`

```java
// application.yml — profilo poc
bpm:
  base-url: ${BPM_STUB_BASE_URL:http://bpm-stub:8090}
  receive-outcome-path: /receive-outcome
  timeout-ms: 50000
  max-retry: 3
  retry-interval-ms: 2000
```

**Comportamento**:
1. Prima di chiamare BPM, persiste `bpm_outbound_message` con `stato_invio=0`.
2. Usa Spring `RetryTemplate` (o `@Retryable`) con `maxAttempts=max-retry`, `backoff=retry-interval-ms`.
3. Aggiorna `retry_count` e `last_attempt_at` a ogni tentativo.
4. Su successo: `stato_invio=1`, chiama `svc.finalizeOnAck(esito)`.
5. Su retry esauriti: `stato_invio=3`, emette evento `BpmSendFailed`, **non** modifica lo stato pratica (rimane `IN_ATTESA_CONFERMA_BPM`).

**API da RIMUOVERE** (non più pathway primario):

```
POST /api/v1/bpm/outcome-ack   ← rimuovere dalla tabella API Candidate §2.8
                                  e dal mapping C5.6
```

> Nota per DBA: aggiornare il commento sulla tabella `bpm_outbound_message` nell'`09_Mapping_Architetturale.md` alla capability C5.4/C5.5.

---

## GAP-US-07 — Mock BPM callback URL configurabile

> **Dipende da**: GAP-US-02 (stesso flusso sincrono)

### Synopsis

Il `bpm-stub` deve esporre un endpoint che simula la ricezione dell'esito e restituisce una risposta `{esito, descrizioneEsito}`, configurabile per esercitare sia il percorso OK che il percorso KO.

### Impatto DB

Nessuno.

### Impatto Backend

**`bpm-stub`** — nuovi endpoint stub:

```
POST /receive-outcome
    Body in: { resultCode, resultMessage, idWorkItem, esito, koCodes[] }
    Body out: { esito: boolean, descrizioneEsito: string }
    Comportamento: restituisce esito in base a variabile d'ambiente BPM_STUB_ESITO_MODE
```

```
GET /admin/mode
    Risposta: { mode: "OK" | "KO" }
PUT /admin/mode
    Body: { mode: "OK" | "KO" }
    Scopo: cambiare modalità di risposta a runtime per i test
```

### Configurazione

```yaml
# docker-compose.yml — servizio bpm-stub
bpm-stub:
  environment:
    BPM_STUB_ESITO_MODE: OK           # OK | KO
    BPM_STUB_KO_DESCRIZIONE: "Esito rifiutato da BPM (mock)"
```

```yaml
# application.yml backend — profilo poc
bpm:
  base-url: ${BPM_STUB_BASE_URL:http://bpm-stub:8090}
  receive-outcome-path: /receive-outcome
```

> Il valore `BPM_STUB_BASE_URL` è già documentato in `07_Deployment_Locale.md §4` come variabile d'ambiente principale. Nessuna modifica al deployment richiesta.

---

## GAP-US-01 — Mock integrazione sistema di ticketing

### Synopsis

All'apertura di una pratica (`svc.openPractice`) il sistema deve chiamare un mock ticketing, ricevere un `ticketId` fittizio e persistirlo sul record pratica.

### Impatto DB

```sql
-- Aggiungere colonna alla tabella practice
ALTER TABLE practice
    ADD COLUMN ticket_id VARCHAR(100) NULL
        COMMENT 'ID ticket sistema esterno (mock in POC)';

-- Aggiungere indice opzionale per ricerca
CREATE INDEX idx_practice_ticket ON practice(ticket_id);
```

### Impatto Backend

**Nuovo endpoint sul `bpm-stub`** (riutilizzo container esistente):

```
POST /ticketing/open-ticket
    Body in: { idWorkItem: string, canale: string }
    Body out: { ticketId: string }
    Comportamento: genera MOCK-TICKET-{uuid} deterministico
```

**Configurazione** (aggiungere a `application.yml`):

```yaml
ticketing:
  base-url: ${TICKETING_BASE_URL:http://bpm-stub:8090}
  open-ticket-path: /ticketing/open-ticket
  enabled: true      # false = skip chiamata (POC senza ticketing)
```

**Modifica `svc.openPractice`** (in `anc.main`):

```
svc.openPractice:
  1. Valida payload
  2. Verifica idempotenza id_workitem
  3. Persiste practice (stato=APERTA)   ← invariato
  4. [NUOVO] Chiama TicketingClient.openTicket(idWorkItem, canale)
             → on success: practice.ticket_id = response.ticketId
             → on failure: log WARN, ticket_id = null (pratica creata comunque)
  5. Emette evento PracticeOpened
```

> La chiamata al ticketing NON è transazionale con la creazione pratica: un fallimento del mock non blocca l'apertura.

### Impatto Frontend

La colonna `ticket_id` **non** compare nella griglia lista pratiche (confermato da GAP-US-09).  
Verificare con BA se esporre nel tab Dettaglio pratica (sezione dati testata).

---

## GAP-US-03 — Verifica documenti + classificazione irreversibile sequenziale

### Synopsis

Dopo ACCETTA, l'operatore percorre due schermate distinte prima della checklist:  
1. **Verifica Documenti** (read-only): dati cliente, carta, allegati con viewer.  
2. **Classificazione Documento** (schermata separata): selezione tipo + CONFERMA irreversibile.  
Solo dopo la CONFERMA viene generata la checklist.

### Impatto DB

Nessun cambiamento strutturale. La colonna `document_type` su `practice` già modellata con vincolo di immutabilità post-set (VC-S4 in `06_State_Management.md`).

### Impatto BPMN — `anc.intake`

Il task `task.typeAndChecklist` è un form multi-step. **Aggiungere** uno step iniziale read-only prima della tipizzazione:

```
[HUMAN] task.typeAndChecklist — AGGIORNAMENTO FORM STEPS:

  Step 0 — [NUOVO] Verifica Documenti (read-only)
     Mostra: dati cliente, dati carta, elenco allegati con viewer integrato
     Azione: "Procedi a Classificazione" → step 1
             "Indietro" → task.acceptPractice (solo se pratica IN_LAVORAZIONE)

  Step 1 — Classificazione Documento (ex "Tipizzazione")
     Form: select tipo documento (Verbale | Carta) + checkbox conferma
     Azione: "CONFERMA" [irreversibile] → chiama POST /intake/typing → step 2
     Nota: il pulsante CONFERMA è abilitato solo dopo selezione tipo

  Step 2 — Checklist (Verbale OPPURE Carta)     ← invariato
  Step 3 — Salva e Prosegui → Riepilogo          ← invariato
  Step 4 — (opzionale) MODIFICA                  ← invariato
  Step 5 — CHIUDI PRATICA                        ← invariato
```

### Impatto Backend

Nessun nuovo endpoint. Il `GET /practices/{id}` e `GET /practices/{id}/attachments` già coprono i dati necessari allo Step 0.

Il `POST /intake/typing` (già esistente) rimane invariato: eseguito alla CONFERMA dello Step 1.

**Aggiungere** al response di `GET /tasks/{id}` il campo `intakeStep` (enum: `VERIFICA | CLASSIFICAZIONE | CHECKLIST | RIEPILOGO`) derivato dallo stato corrente della pratica e dalla presenza di `document_type`:

```json
"intakeStep": "VERIFICA"   // se document_type IS NULL
"intakeStep": "CHECKLIST"  // se document_type IS NOT NULL e checklist non consolidata
"intakeStep": "RIEPILOGO"  // se checklist stato BOZZA
```

### Impatto Frontend

- Aggiungere il componente `VerificaDocumentiStep` (step 0): riusa `PracticeDetailViewer` e `AttachmentViewer` già esistenti (read-only).
- Il componente di tipizzazione diventa `ClassificazioneStep` (step 1): confirm dialog prima dell'invio.

---

## GAP-US-04 — Sidebar navigazione lavorazione (3 step collassabile)

### Synopsis

Durante la lavorazione (`task.typeAndChecklist`) è visibile una sidebar sinistra con 3 step fissi: **Dati Pratica → Verifica Documento → Riepilogo**. Collassabile a icone. Step 3 (Riepilogo) disabilitato fino a "Salva e Prosegui".

### Impatto DB

Nessuno. Le fasi non sono persistite come campo separato (confermato esplicitamente da GAP-US-06).

### Impatto Backend

**Aggiungere** al response di `GET /tasks/{id}` (o `GET /practices/{id}` nel contesto lavorazione) il campo `sidebarState`:

```json
{
  "sidebarState": {
    "currentStep": "VERIFICA_DOCUMENTO",
    "steps": [
      { "id": "DATI_PRATICA",       "label": "Dati Pratica",       "enabled": true,  "completed": true  },
      { "id": "VERIFICA_DOCUMENTO", "label": "Verifica Documento",  "enabled": true,  "completed": false },
      { "id": "RIEPILOGO",          "label": "Riepilogo",           "enabled": false, "completed": false }
    ]
  }
}
```

Regola di derivazione `RIEPILOGO.enabled`:  
→ `true` se `checklist_response.stato IN (BOZZA, RIAPERTA, CONSOLIDATA)`  
→ `false` altrimenti

### Impatto Frontend

- Nuovo componente `WorkflowSidebar` (colonna sinistra, larghezza collassabile).
- Stato `collapsed: boolean` in `localStorage` (non su DB) per persistenza sessione utente.
- Note interne (campo testo, solo se esito RESPINTA) sono nel tab Riepilogo, **non** in un pannello separato (confermato da GAP-US-04).

---

## GAP-US-05 — Filtri Lista Attività salvati su DB per utente

### Synopsis

L'operatore può salvare e riutilizzare set di filtri nella Lista Attività. I filtri sono persistiti su DB per utente.

### Impatto DB

```sql
CREATE TABLE user_task_filter (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    filter_name     VARCHAR(100) NULL,              -- opzionale, etichetta libera
    stato           VARCHAR(50)  NULL,
    tipo_pratica    VARCHAR(50)  NULL,
    pratica_numero  VARCHAR(50)  NULL,
    nome_attivita   VARCHAR(100) NULL,
    data_scadenza_da DATE        NULL,
    data_scadenza_a  DATE        NULL,
    assegnatario    VARCHAR(100) NULL,
    utente_in_carico VARCHAR(100) NULL,
    created_at      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_utf_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_utf_user_created (user_id, created_at DESC)
);
```

> Limite ultimi N set per utente: gestito a livello applicativo (default N=5, configurabile `tasks.saved-filters.max-per-user=5`).

### Impatto Backend

**Nuovi endpoint** (aggiungere a `05_API_Candidate.md §2.5 Task`):

```
GET  /api/v1/tasks/filters/saved
     Risposta: ultimi N set salvati per l'utente corrente
     Ordine: created_at DESC

POST /api/v1/tasks/filters/saved
     Body: { stato, tipoPratica, praticaNumero, nomeAttivita,
             dataScadenzaDa, dataScadenzaA, assegnatario, utenteInCarico }
     Risposta: 201 Created con l'ID del set salvato
     Side effect: se user ha già N set, elimina il più vecchio (FIFO)

DELETE /api/v1/tasks/filters/saved/{id}
     Risposta: 204 No Content
     Autorizzazione: solo il proprietario del set
```

**Aggiornare `09_Mapping_Architetturale.md`**:

| Capability BA | Modulo | API | Entità |
|---|---|---|---|
| C3.6 Filtri salvati Lista Attività | M-Workflow Engine (BC2) | `GET/POST/DELETE /tasks/filters/saved` | `user_task_filter` |

### Impatto Frontend

- Sezione "Ultimi N filtri salvati" sopra la Lista Attività (griglia con 8 colonne, click riga popola i filtri).
- Pulsante "Applica Filtri" (no save) e "Applica e Salva Filtri" (save + apply).
- Pulsante "Azzera Filtri" (clear form, no save).

---

## GAP-US-06 — Linea di avanzamento fasi operative

### Synopsis

Nel dettaglio pratica e durante la lavorazione è visibile una `PhaseProgressBar` a 3 fasi fisse: **Raccolta Input → Lavorazione → Chiusura Pratica**. Stato derivato dallo stato pratica, non persistito.

### Impatto DB

Nessuno (confermato da GAP-US-06: "Le fasi operative non sono persistite come campo separato su database").

### Impatto Backend

**Aggiungere** il campo derivato `fase` al response di `GET /practices/{id}`:

```json
"fase": "LAVORAZIONE"
```

**Regola di derivazione** (lato servizio, BC1):

```
practice.stato == APERTA                       → fase = "RACCOLTA_INPUT"
practice.stato == IN_LAVORAZIONE               → fase = "LAVORAZIONE"
practice.stato IN (IN_ATTESA_CONFERMA_BPM,
                   CHIUSA_OK, CHIUSA_KO)       → fase = "CHIUSURA_PRATICA"
```

### Impatto Frontend

- Nuovo componente `PhaseProgressBar` con 3 nodi e connettori.
- Fase corrente evidenziata (colore/bold). Non cliccabile.
- Riutilizzato sia nel dettaglio pratica (read) sia nella vista lavorazione.

---

## GAP-US-08 — Visibilità condizionale item checklist per `idDipendenza`

### Synopsis

Ogni item checklist può avere un'item "padre" (`idDipendenza`): se la risposta del padre non soddisfa la condizione, l'item figlio non è visibile. Le regole sono statiche (da configurazione), non variano durante la lavorazione.

### Impatto DB

```sql
-- Aggiungere colonne alla tabella del catalogo checklist
-- (nome fisico da confermare con DBA: ref_checklist_item o analogo)
ALTER TABLE ref_checklist_item
    ADD COLUMN id_dipendenza        BIGINT  NULL
        COMMENT 'FK a ref_checklist_item.id: item padre da cui dipende la visibilità',
    ADD COLUMN valore_attivo_dipendenza VARCHAR(10) NULL
        COMMENT 'Valore risposta padre che ABILITA la visibilità di questo item (es. SI, NO)',
    ADD CONSTRAINT fk_rci_dipendenza
        FOREIGN KEY (id_dipendenza) REFERENCES ref_checklist_item(id);
```

> Se la tabella DBA ha nome diverso da `ref_checklist_item`, applicare le stesse colonne alla tabella che gestisce il catalogo checklist differenziato per `categoria`.

### Impatto Backend

**Modifica `GET /practices/{id}/intake/checklist`**: aggiungere campo `visible` calcolato per ogni item:

```json
{
  "items": [
    {
      "id": 5,
      "label": "Firma del dichiarante presente",
      "idDipendenza": 3,
      "valoreDipendenza": "SI",
      "visible": true,         // calcolato: item 3 ha risposta SI nel contesto corrente
      "risposta": null,
      "nota": null,
      "codiceCausale": null
    }
  ]
}
```

**Algoritmo visibilità** (eseguito lato servizio):
1. Carica tutte le risposte correnti della pratica.
2. Per ogni item con `idDipendenza != null`: `visible = (risposta_padre == valore_attivo_dipendenza)`.
3. Per ogni item con `idDipendenza == null`: `visible = true`.
4. La cascata KO globale (flagPresenza = No) è indipendente dalla visibilità condizionale.

### Impatto Frontend

Il componente checklist filtra le righe per `item.visible == true` prima del rendering. Nessun item nascosto invia risposta nel `PUT /intake/checklist`.

---

## GAP-US-09 — Griglia pratiche — 11 colonne fisse

### Synopsis

La lista pratiche (`GET /practices`) espone esattamente 11 colonne fisse nell'ordine definito. La colonna `Segnalazioni` è calcolata. `data_scadenza`, `id`, `canale` non compaiono nella griglia.

### Impatto DB

```sql
-- Verifica che tutte le colonne necessarie esistano su practice
-- (lista da confermare con DBA nell'implementazione schema)
-- Colonne richieste in GET /practices:
--   numero_pratica, codice_fiscale, codice_cliente,
--   data_apertura, data_ultima_modifica, data_chiusura,
--   data_inserimento_richiesta, esito_sd, operatore (FK a user),
--   stato, segnalazioni (COUNT da signal JOIN)

-- data_scadenza: presente su DB, non esposta in griglia
-- ticket_id: presente su DB (da GAP-US-01), non esposto in griglia
```

> DBA: assicurarsi che `practice` abbia `data_inserimento_richiesta` (timestamp della richiesta in ingresso da BPM, distinto da `data_apertura`). Se non presente, aggiungere:
>
> ```sql
> ALTER TABLE practice ADD COLUMN data_inserimento_richiesta DATETIME(3) NULL;
> ```

### Impatto Backend

**Aggiornare il DTO** `PracticeListItemDto` con i campi nell'ordine della US:

```java
record PracticeListItemDto(
    String  numeroPratica,          // link al dettaglio (opaco per il BE)
    String  codiceFiscale,
    String  codiceCliente,
    Instant dataApertura,
    Instant dataUltimaModifica,
    Instant dataChiusura,           // null se non ancora chiusa
    Instant dataInserimentoRichiesta,
    String  esitoSd,                // APPROVATA | RESPINTA | null
    String  operatore,              // username/nome operatore in carico
    String  stato,
    int     segnalazioni            // COUNT(signal WHERE practice_id=?)
) {}
```

**`segnalazioni`**: calcolato con una JOIN o subquery su `signal` (count segnalazioni attive per pratica). Non persistito.

### Impatto Frontend

- Tabella con 11 colonne fisse, ordine non modificabile.
- `Pratica N.` = link al dettaglio (react-router).
- `Segnalazioni` = icona visiva se > 0.
- Nessun column picker/customization.

---

## GAP-US-10 — Tab "Stati" separato nel dettaglio pratica

### Synopsis

Nel dettaglio pratica esiste il tab "**Stati**" (distinto dal tab "Cronologia"): mostra la sequenza delle transizioni di stato in ordine cronologico.

### Impatto DB

La tabella `practice_state_history` è già modellata in `06_State_Management.md §8`. Verificare la presenza di tutte le colonne necessarie:

```sql
-- Struttura attesa (DBA verifica/crea)
CREATE TABLE practice_state_history (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    practice_id    BIGINT       NOT NULL,
    from_state     VARCHAR(50)  NULL,       -- NULL per la prima transizione (APERTA)
    to_state       VARCHAR(50)  NOT NULL,
    actor          VARCHAR(100) NOT NULL,   -- username o 'SYSTEM'
    occurred_at    DATETIME(3)  NOT NULL,
    correlation_id VARCHAR(36)  NULL,
    CONSTRAINT fk_psh_practice FOREIGN KEY (practice_id) REFERENCES practice(id),
    INDEX idx_psh_practice_time (practice_id, occurred_at)
);
```

### Impatto Backend

**`GET /api/v1/practices/{id}/states`** (già presente in `05_API_Candidate.md §2.2`):

Response contract da documentare esplicitamente:

```json
{
  "states": [
    {
      "stato":      "APERTA",
      "dataOraInizio": "2026-05-21T09:00:00.000Z",
      "attore":     "SYSTEM"
    },
    {
      "stato":      "IN_LAVORAZIONE",
      "dataOraInizio": "2026-05-21T09:15:00.000Z",
      "attore":     "mario.rossi"
    }
  ]
}
```

> Nessun cambio al path o al metodo. Solo documentare il contratto nel `05_API_Candidate.md`.

### Impatto Frontend

- Aggiungere tab "**Stati**" nella navigazione del dettaglio pratica (distinto da "**Cronologia**").
- Tab Stati: griglia sola lettura con colonne `Stato | Data/Ora | Operatore`.
- Tab Cronologia: rimane invariato (log azioni operative da `audit_event`).

---

## GAP-US-11 — Doppio meccanismo motivazioni item checklist

### Synopsis

Per ogni item checklist marcato come non conforme, l'operatore può inserire (entrambi opzionali):  
1. **nota libera** (VARCHAR 255)  
2. **codice causale formale** dal catalogo, filtrato per categoria dell'item.

### Impatto DB

```sql
-- Nuova tabella catalogo causali
CREATE TABLE ref_causali_checklist (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    codice      VARCHAR(20)  NOT NULL,
    descrizione VARCHAR(500) NOT NULL,
    categoria   VARCHAR(50)  NOT NULL,    -- 'Verbale' | 'Carta' (da BOA_ANC_RefChecklist)
    attivo      BOOLEAN      NOT NULL DEFAULT TRUE,
    UNIQUE INDEX uq_causali_codice_cat (codice, categoria)
);

-- Aggiungere colonne a checklist_response (risposta per singolo item)
ALTER TABLE checklist_response
    ADD COLUMN nota            VARCHAR(255) NULL
        COMMENT 'Nota libera operatore (opzionale, solo se item KO)',
    ADD COLUMN codice_causale_id BIGINT     NULL
        COMMENT 'FK a ref_causali_checklist.id (opzionale)',
    ADD CONSTRAINT fk_cr_causale
        FOREIGN KEY (codice_causale_id) REFERENCES ref_causali_checklist(id);
```

> Seed dati: popolare `ref_causali_checklist` con i codici causali esistenti nel sistema Appian (da `MatriciControlli.xlsx`). Responsabilità DBA per il dataset di seed.

### Impatto Backend

**Nuovo endpoint** (aggiungere a `05_API_Candidate.md §2.4 Tipizzazione & Checklist`):

```
GET /api/v1/practices/{id}/intake/checklist/causali?categoria={Verbale|Carta}
    Risposta: lista { id, codice, descrizione } filtrata per categoria
    Scopo: popola il dropdown causali nell'UI
```

**Modifica `PUT /api/v1/practices/{id}/intake/checklist`**: il body deve accettare per ogni item:

```json
{
  "items": [
    {
      "id": 5,
      "risposta": "NO",
      "nota": "Il documento risulta illeggibile nella parte inferiore",
      "codiceCausaleId": 12
    }
  ]
}
```

**Aggiornare `09_Mapping_Architetturale.md`**:

| Capability BA | API | Entità |
|---|---|---|
| C4.6 Causali KO | `GET /intake/checklist/causali?categoria=` | `ref_causali_checklist` |
| C4.6 Causali KO | `PUT /intake/checklist` (nota + codiceCausaleId per item) | `checklist_response.nota`, `checklist_response.codice_causale_id` |

### Impatto Frontend

- Per ogni item checklist con risposta "NO": espandere la riga con due campi opzionali:
  - `textarea` nota (max 255 chars, counter visivo).
  - `select/dropdown` causale (opzioni da `GET /intake/checklist/causali?categoria=`).
- I due campi sono **opzionali e indipendenti**: l'operatore può inserirne uno, entrambi, o nessuno.

---

## Tabella riepilogativa — Modifiche schema DB (input per Discovery-DBA)

| Tabella | Tipo | Descrizione | GAP-US |
|---|---|---|---|
| `practice` | ALTER ADD COLUMN | `ticket_id VARCHAR(100) NULL` | GAP-US-01 |
| `practice` | ALTER ADD COLUMN | `data_inserimento_richiesta DATETIME(3) NULL` | GAP-US-09 |
| `bpm_outbound_message` | CREATE | Outbox BPM con stati invio 0/1/2/3 + retry_count | GAP-US-02 |
| `user_task_filter` | CREATE | Set filtri Lista Attività salvati per utente | GAP-US-05 |
| `ref_causali_checklist` | CREATE | Catalogo causali KO con categoria | GAP-US-11 |
| `checklist_response` | ALTER ADD COLUMN | `nota VARCHAR(255) NULL` | GAP-US-11 |
| `checklist_response` | ALTER ADD COLUMN | `codice_causale_id BIGINT NULL FK` | GAP-US-11 |
| `ref_checklist_item` | ALTER ADD COLUMN | `id_dipendenza BIGINT NULL FK` | GAP-US-08 |
| `ref_checklist_item` | ALTER ADD COLUMN | `valore_attivo_dipendenza VARCHAR(10) NULL` | GAP-US-08 |
| `practice_state_history` | CREATE/VERIFY | Storico transizioni stati pratica | GAP-US-10 |

> Tutte le CREATE/ALTER sono da tradurre in migration script Flyway (path: `infra/db/migrations/`) con versione progressiva: `V2__gap_us_02_bpm_outbound.sql`, `V3__gap_us_01_ticketing.sql`, ecc.

---

## Tabella riepilogativa — Nuovi/modificati endpoint API

| Metodo | Path | Tipo | GAP-US |
|---|---|---|---|
| ~~POST~~ | ~~`/bpm/outcome-ack`~~ | RIMOSSO (pathway primario) | GAP-US-02 |
| GET | `/tasks/filters/saved` | NUOVO | GAP-US-05 |
| POST | `/tasks/filters/saved` | NUOVO | GAP-US-05 |
| DELETE | `/tasks/filters/saved/{id}` | NUOVO | GAP-US-05 |
| GET | `/practices/{id}/intake/checklist/causali?categoria=` | NUOVO | GAP-US-11 |
| GET | `/tasks/{id}` | MODIFICA — aggiunge `intakeStep`, `sidebarState` | GAP-US-03, GAP-US-04 |
| GET | `/practices/{id}` | MODIFICA — aggiunge `fase` derivato | GAP-US-06 |
| GET | `/practices/{id}/states` | MODIFICA — documenta contratto response | GAP-US-10 |
| GET | `/practices/{id}/intake/checklist` | MODIFICA — aggiunge campo `visible` per item | GAP-US-08 |
| PUT | `/practices/{id}/intake/checklist` | MODIFICA — accetta `nota` + `codiceCausaleId` per item | GAP-US-11 |
| GET | `/practices` | MODIFICA — DTO con 11 campi fissi + `segnalazioni` | GAP-US-09 |
| GET | `bpm-stub /receive-outcome` (POST) | NUOVO — stub sincrono | GAP-US-07 |
| GET | `bpm-stub /ticketing/open-ticket` (POST) | NUOVO — stub ticketing | GAP-US-01 |
| PUT | `bpm-stub /admin/mode` | NUOVO — cambio modalità OK/KO a runtime | GAP-US-07 |

---

## Tabella riepilogativa — Modifiche BPMN

| Processo | Modifica | Tipo | GAP-US |
|---|---|---|---|
| `anc.main` | Rimuovere `evt.waitOutcomeAck` (message event) | RIMOZIONE | GAP-US-02 |
| `anc.main` | `svc.sendOutcomeToBpm` → sincrono con retry → `svc.finalizeOnAck` diretta | MODIFICA | GAP-US-02 |
| `anc.main` | `svc.openPractice` → aggiunge chiamata `TicketingClient` post-persist | MODIFICA | GAP-US-01 |
| `anc.intake` | `task.typeAndChecklist` → aggiunge Step 0 "Verifica Documenti" (read-only) | MODIFICA | GAP-US-03 |
| `anc.intake` | Step 1 rinominato "Classificazione" con confirm dialog | MODIFICA | GAP-US-03 |

---

## Vincoli implementativi trasversali

1. **Nessuna autenticazione**: la POC non richiede autenticazione (Simplification Matrix riga 3). I nuovi endpoint non aggiungono security filter. Il campo `user_id` nei filtri salvati si basa sull'utente di sessione (Basic Auth mock).

2. **Tutte le integrazioni esterne sono mock**: `bpm-stub` (già presente) copre sia GAP-US-02/07 sia GAP-US-01 (ticketing). Nessun nuovo container docker richiesto.

3. **Flyway**: ogni ALTER/CREATE è uno script di migrazione separato, versionato sequenzialmente da `V2` in poi.

4. **Backward compatibility**: le modifiche a `GET /practices/{id}` (aggiunta `fase`) e `GET /tasks/{id}` (aggiunta `intakeStep`, `sidebarState`) sono additive: nessun campo esistente viene rimosso o rinominato.

5. **GAP-US-06 (fasi)**: il campo `fase` nel response non è in DB. È un campo calcolato nel DTO di presentazione. Non impatta il modello dati del DBA.

---

---

# PARTE B — Verifica Tecnica vs Reverse Engineering

> **Fonte**: verifica completa di `docs/reverse/attivazione-nuova-carta/v20260506/02_bpm.md`  
> e `docs/reverse/attivazione-nuova-carta/v20260506/03_technical.md`  
> eseguita il 2026-05-21 dopo la produzione della Parte A.  
>
> Questa sezione documenta i requisiti tecnici del reverse che **non** sono coperti né dai  
> documenti architetturali esistenti (`01`→`10`) né dalla Parte A (GAP-US-01→GAP-US-11).

---

## Matrice di Copertura Tecnica

| # | Requisito tecnico (reverse) | Documento arch copertura | Stato |
|---|---|---|---|
| T-01 | WebAPI `POST /boaanccreapratica` — contratto request/response JSON completo | `05_API_Candidate.md §2.8`, `04_Workflow_Architecture.md §2` | ✅ COPERTO |
| T-02 | Catena invocazione ASYNC: WebApi → ElaborazioneJson → AvvioAttivita | `04_Workflow_Architecture.md §2` | ✅ COPERTO |
| T-03 | Download automatico allegati SYNC (DownloadContenuti → ScaricaSingoloDoc) | `03_GAP_Coverage_Review.md FC-02 (PARZIALE)` | ⚠️ PARZIALE → vedi TECNICO-GAP-A |
| T-04 | Human task a gruppo `GRUPPO_OPERATORE_ANC` (singolo task, no pre-assegnazione) | `04_Workflow_Architecture.md §4` | ✅ COPERTO |
| T-05 | SLA 5 giorni lavorativi task (solo monitoraggio, no escalation) | — | ❌ MANCANTE → vedi TECNICO-GAP-B |
| T-06 | Stati pratica STATI_PRATICA [Aperta→Chiusa KO] — 5 valori | `06_State_Management.md §1` | ✅ COPERTO |
| T-07 | Fasi case STATI_CASE [Raccolta Input / Lavorazione / Chiusura Pratica] | GAP_Architettura.md GAP-US-06 | ✅ COPERTO |
| T-08 | STATIINVIO outbox BPM [0=attesa, 1=inviato, 2=errore, 3=scartato] | GAP_Architettura.md GAP-US-02 | ✅ COPERTO |
| T-09 | Storico stati pratica (`ENTITY_STATIPRATICA`) | `06_State_Management.md §8`, GAP_Architettura.md GAP-US-10 | ✅ COPERTO |
| T-10 | Note intermediate per case (`BOA_ANC_ENTITY_CASENOTE`) — durante lavorazione | — | ❌ MANCANTE → vedi TECNICO-GAP-C |
| T-11 | Audit log generale (`ENTITY_AUDIT` / `BOA_ANC_ScriviAudit`) | `09_Mapping_Architetturale.md C9.1`, `audit_event` | ✅ COPERTO |
| T-12 | Log debug WebApi input/output (`ENTITY_DEBUGWSINPUT`) | `03_GAP_Coverage_Review.md FC-03 (OUT_OF_SCOPE)` | ✅ OUT_OF_SCOPE |
| T-13 | Integrazione BPM: outbound sync con retry, timeout 50s | GAP_Architettura.md GAP-US-02 | ✅ COPERTO |
| T-14 | OAuth2 BPM → sostituito da bpm-stub | `10_POC_Runtime_Simplification_Matrix.md riga 4` | ✅ COPERTO |
| T-15 | Response BPM `{esito: boolean, descrizioneEsito: string(4000)}` | GAP_Architettura.md GAP-US-07 | ✅ COPERTO |
| T-16 | Endpoint BPM env-specific (`BOA_ANC_RELATIVEPATH_SCODAMENTOSTATI`) | GAP_Architettura.md GAP-US-02 `bpm.receive-outcome-path` | ✅ COPERTO |
| T-17 | Max retry configurable (`BOA_ANC_NUMBER_MAXRETRY`) | GAP_Architettura.md GAP-US-02 `bpm.max-retry` | ✅ COPERTO |
| T-18 | Max esiti per run (`BOA_ANC_NUMEROESITITOSEND`) | Non applicabile: sostituito da retry sincrono per-request | ✅ N/A (pattern cambiato) |
| T-19 | Semaforo anti-concorrenza batch (`GestioneScodamento.inEsecuzione`) | Non applicabile: nessun batch nel nuovo pattern | ✅ N/A (pattern cambiato) |
| T-20 | Dominio validazione `BOA_ANC_CANALE` = `["APP", "WEB"]` | — | ❌ MANCANTE → vedi TECNICO-GAP-D |
| T-21 | Flag test `BOA_ANC_DEBUG_DEFAULTCODICEDOCID` (forza CODICE_DOC_ID=3) | — | ❌ MANCANTE → vedi TECNICO-GAP-E |
| T-22 | Cleanup process instances (archive 7gg, delete 1gg) | — | ❌ MANCANTE → vedi TECNICO-GAP-F |
| T-23 | Bootstrap codifiche (`BOA_ANC_InizializzaBoaCodifiche`) | `07_Deployment_Locale.md §5` (Flyway seed) | ✅ COPERTO |
| T-24 | Riassegnazione supervisore (loop su lista task) | `05_API_Candidate.md §2.6`, `09_Mapping_Architetturale.md C6.2/C6.3` | ✅ COPERTO |
| T-25 | Sinergia init Operatore + Supervisore | `10_POC_Runtime_Simplification_Matrix.md riga 5`, sinergia-stub | ✅ COPERTO |
| T-26 | Tipizzazione documento → processo separato (BOA_ANC_Processo_TipizzaDoc) | `04_Workflow_Architecture.md §3`, GAP_Architettura.md GAP-US-03 | ✅ COPERTO |
| T-27 | Salvataggio dati checklist durante lavorazione (BOA_ANC_SalvataggioDati) | `PUT /intake/checklist` (BOZZA) | ✅ COPERTO |
| T-28 | CDT `BOA_ANC_Pratica` — campo `canale VARCHAR(255)` validato su dominio | — (campo presente ma dominio non documentato) | ⚠️ PARZIALE → vedi TECNICO-GAP-D |
| T-29 | CDT `BOA_ANC_ContenutiDenuncia` — campo `linkDownload VARCHAR(2500)` | — (campo non citato nei vincoli DB) | ⚠️ PARZIALE → vedi TECNICO-GAP-A |

**Riepilogo**: 21 COPERTI, 4 MANCANTI, 2 PARZIALI (→ TECNICO-GAP-A÷F).

---

## TECNICO-GAP-A — Download documenti da URL remoto (FC-02 PARZIALE)

> **Fonte reverse**: `BOA_ANC_DownloadContenuti_CreazionePratica` → `BOA_ANC_ScaricaSingoloDoc` → `BOA_ANC_Integrazione_DownloadDocument` (`03_technical.md §3`)

### Synopsis

All'apertura pratica, per ogni elemento nell'array `CONTENUTI[]` del payload JSON, il sistema deve scaricare il documento dall'URL remoto (`LINKDOWNLOAD`) e salvarlo nello storage. Questo avviene in modo sincrono all'interno del flusso asincrono `svc.elaborateInput` (equivalente di `BOA_ANC_ElaborazioneJsonInput_CreazionePratica`). I dettagli tecnici (timeout, fallback, comportamento su errore) non sono documentati in nessun documento architetturale.

### Impatto DB

```sql
-- Il campo linkDownload è lungo fino a 2500 caratteri (da CDT BOA_ANC_ContenutiDenuncia)
-- Assicurarsi che la tabella attachment (o contenuto_denuncia) abbia:
ALTER TABLE attachment
    ADD COLUMN link_download VARCHAR(2500) NULL
        COMMENT 'URL sorgente originale del documento (da LINKDOWNLOAD nel payload JSON)',
    ADD COLUMN download_status VARCHAR(20) NULL DEFAULT 'PENDING'
        COMMENT 'PENDING | OK | FAILED — esito del download automatico';
-- Valori download_status: PENDING=non ancora scaricato, OK=scaricato in storage, FAILED=download fallito
```

### Impatto Backend

**Nuovo servizio**: `DocumentDownloadService` (in BC3 — M-Document Service)

```java
// application.yml — profilo poc
document-download:
  timeout-ms: 10000           # 10 secondi (da BOA_ANC_Integrazione_DownloadDocument)
  fallback-bucket: anc-temp   # MinIO bucket temporaneo (equivalente BOA_ANC_FOLDER_TEMP)
  enabled: true               # false = skip download (test/sviluppo)
```

**Comportamento** (chiamato da `svc.elaborateInput` per ogni elemento di `CONTENUTI[]`):

```
per ogni documento in CONTENUTI[]:
  1. HTTP GET {linkDownload}
     - timeout: ${document-download.timeout-ms}
     - no auth
     - Content-Type risposta: application/octet-stream (binario)

  2. onSuccess:
     - upload su MinIO bucket anc-attachments
       key: practices/{practiceId}/{nomeFile}.{estensione}
     - aggiorna attachment.download_status = 'OK'
     - aggiorna attachment.storage_key = <chiave MinIO>

  3. onError (timeout o HTTP error):
     - upload su MinIO bucket ${fallback-bucket} (anc-temp)
       key: temp/{practiceId}/{nomeFile}.{estensione}
     - aggiorna attachment.download_status = 'FAILED'
     - log WARN: practice {id}, documento {nomeFile}, url {linkDownload}
     - la pratica è comunque creata e il task è comunque avviato
     - l'operatore vedrà il documento come "non disponibile" nel viewer
```

### Impatto Frontend

Il viewer documenti deve gestire il caso `download_status = 'FAILED'`:
- Mostrare un placeholder "Documento non disponibile — download fallito" al posto del viewer.
- Mostrare comunque il pulsante "Download" che punta al fallback-bucket (per eventuale recupero manuale).

### Aggiornamento Mapping Architetturale

| Capability BA | Modulo | Workflow / Step | API | Entità |
|---|---|---|---|---|
| C1.6 Download allegati automatico | M-Document Service (BC3) | `svc.elaborateInput` → `DocumentDownloadService` | (interno, no endpoint esposto) | `attachment.link_download`, `attachment.download_status` |

---

## TECNICO-GAP-B — SLA task 5 giorni lavorativi

> **Fonte reverse**: `02_bpm.md §3.2 / §7` — `target-completion=5.0 giorni, target-lag=1.0` su task "attivita"

### Synopsis

Il human task `task.acceptPractice` / `task.typeAndChecklist` (unico task operativo `BOA_ANC_AvvioAttivita`) ha un SLA di monitoraggio di **5 giorni lavorativi** dalla creazione. Non è una deadline con escalation automatica: è solo un target di monitoring. Nessun documento architetturale menziona questo vincolo SLA.

### Impatto DB

```sql
-- Aggiungere campo sla_due_date alla tabella task (se gestita dal modello applicativo)
-- Se il task è gestito interamente da Flowable, configurare dueDate nel BPMN.
ALTER TABLE task
    ADD COLUMN sla_due_date DATETIME(3) NULL
        COMMENT 'Data/ora target completamento SLA (5 giorni lavorativi dalla creazione)';
```

> **Nota**: Flowable supporta `dueDate` nativo sul UserTask. In alternativa, è sufficiente impostare `dueDate` nel BPMN XML del processo `anc.intake` sull'elemento `<userTask>`. La colonna DB è necessaria solo se il backend espone l'informazione SLA nel proprio modello.

### Impatto BPMN — `anc.intake`

Aggiungere `dueDate` al nodo `task.acceptPractice` (o al task aggregato):

```xml
<userTask id="task.typeAndChecklist" name="Lavorazione Pratica">
  <extensionElements>
    <flowable:taskListener event="create"
      class="it.poste.anc.workflow.SlaTaskListener"/>
  </extensionElements>
</userTask>
```

Il `SlaTaskListener` calcola `dueDate = createdAt + 5 giorni lavorativi` e imposta `task.setDueDate(dueDate)` sulla task Flowable.

### Impatto Backend

**Nessun nuovo endpoint**. Il campo SLA è visibile opzionalmente nel `GET /tasks/{id}` come campo informativo aggiuntivo:

```json
{
  "slaDueDate": "2026-05-28T23:59:59.000Z",
  "slaStatus": "IN_TEMPO"    // IN_TEMPO | SCADUTO (derivato da now > slaDueDate)
}
```

> `slaStatus` è un campo calcolato (no persistenza separata). Non scatena azioni automatiche (confermato dal reverse: `enabled=false` su tutte le deadline Appian).

### Configurazione

```yaml
# application.yml
task:
  sla-working-days: 5    # giorni lavorativi target (da BOA_ANC_AvvioAttivita target-completion)
```

---

## TECNICO-GAP-C — Note intermediate per case (BOA_ANC_ENTITY_CASENOTE)

> **Fonte reverse**: `02_bpm.md §6.4` — `BOA_ANC_ENTITY_CASENOTE` scritto da `BOA_ANC_SalvataggioDati` e `BOA_ANC_Processo_CambioStato`

### Synopsis

Nel reverse, `BOA_ANC_SalvataggioDati` salva i dati della checklist e scrive anche una nota associata al case durante la lavorazione (non solo alla chiusura). Analogamente, `BOA_ANC_Processo_CambioStato` può associare note ai cambi di stato. L'architettura attuale ha `practice_outcome.notes` solo per la nota finale (al momento di CHIUDI PRATICA, visibile solo se RESPINTA). La tabella `BOA_ANC_ENTITY_CASENOTE` del reverse è più generale: note a qualsiasi punto del workflow.

### Analisi del gap

- `practice_outcome.notes` = nota finale alla chiusura → ✅ coperto da GAP-US-04
- Note intermediate scritte da `BOA_ANC_SalvataggioDati` durante lavorazione → ❌ non coperte
- Note associate ai cambi di stato da `BOA_ANC_Processo_CambioStato` → ❌ non coperte

### Impatto DB

```sql
CREATE TABLE case_note (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    practice_id  BIGINT       NOT NULL,
    autore       VARCHAR(100) NOT NULL,
    testo        TEXT         NOT NULL,
    tipo         VARCHAR(50)  NOT NULL,  -- 'LAVORAZIONE' | 'CAMBIO_STATO' | 'CHIUSURA'
    created_at   DATETIME(3)  NOT NULL,
    CONSTRAINT fk_cn_practice FOREIGN KEY (practice_id) REFERENCES practice(id),
    INDEX idx_cn_practice_time (practice_id, created_at)
);
```

### Impatto Backend

**Nuovo endpoint** (aggiungere a `05_API_Candidate.md §2.2 Pratiche`):

```
GET  /api/v1/practices/{id}/notes
     Risposta: lista note in ordine cronologico
     Filtro opzionale: ?tipo=LAVORAZIONE|CAMBIO_STATO|CHIUSURA

POST /api/v1/practices/{id}/notes
     Body: { testo: string, tipo: string }
     Scopo: salvataggio nota intermedia durante lavorazione
     Chiamato da: PUT /intake/checklist (nota di lavorazione)
```

> Le note di tipo `CAMBIO_STATO` sono scritte automaticamente dal backend al cambio stato pratica (no input manuale). Le note di tipo `CHIUSURA` corrispondono all'attuale `practice_outcome.notes`.

### Impatto Frontend

- Nel tab Riepilogo della lavorazione: campo note (già previsto da GAP-US-04) → mappato su `case_note.tipo=CHIUSURA`.
- Il tab Cronologia (`GET /practices/{id}/history`) può includere anche le note di tipo `CAMBIO_STATO` se pertinente.

---

## TECNICO-GAP-D — Dominio validazione campo CANALE

> **Fonte reverse**: `03_technical.md §5 Costanti` — `BOA_ANC_CANALE = ["APP", "WEB"]`

### Synopsis

Il campo `CANALE` nel payload JSON della WebAPI viene validato rispetto alla lista `["APP", "WEB"]`. Questo dominio non è documentato in nessun documento architetturale (né in `08_Vincoli_Tecnici.md` né nella specifica della WebAPI in `05_API_Candidate.md`).

### Impatto DB

Nessuno. Il dominio è una validazione applicativa, non un FK.

### Impatto Backend

**Aggiungere** al validatore di `POST /api/v1/bpm/practices`:

```java
private static final Set<String> CANALI_AMMESSI = Set.of("APP", "WEB");

// In PracticeCreationValidator.validate():
if (!CANALI_AMMESSI.contains(input.getCanale())) {
    throw new ValidationException(resultCode = "-4",
        "CANALE non valido. Valori ammessi: " + CANALI_AMMESSI);
}
```

### Aggiornamento documenti

**`08_Vincoli_Tecnici.md`**: aggiungere vincolo:

```
VT-CANALE: il campo CANALE nel payload di apertura pratica ammette esclusivamente i valori
           {"APP", "WEB"} (lista statica, da BOA_ANC_CANALE). Qualsiasi altro valore
           produce resultCode = -4 (validazione fallita).
```

---

## TECNICO-GAP-E — Flag test CODICE_DOC_ID forzato (POC utility)

> **Fonte reverse**: `03_technical.md §5 Costanti` — `BOA_ANC_DEBUG_DEFAULTCODICEDOCID`  
> Quando `true`, forza `CODICE_DOC_ID = 3` ignorando il valore nel payload JSON.

### Synopsis

Nell'applicazione Appian originale, questo flag consente di testare senza inviare un payload JSON reale contenente `CODICE_DOC_ID`. Nella POC, un meccanismo equivalente è utile per semplificare l'esercizio del flusso completo da `bpm-stub` senza costruire payload JSON complessi.

### Impatto DB

Nessuno.

### Impatto Backend / Configurazione

```yaml
# application.yml — profilo poc
debug:
  default-codice-doc-id: 3      # se != null, sovrascrive il valore ricevuto nel payload
                                 # null = usa il valore dal payload (default produzione)
```

Il `svc.openPractice` legge `${debug.default-codice-doc-id}`: se non nullo, usa il valore di configurazione invece di quello nel JSON. Attivo solo nel profilo `poc` o `local`.

> Questo flag NON va mai attivato nel profilo `target`. Documentare in `08_Vincoli_Tecnici.md` come "solo per ambienti non-produzione".

---

## TECNICO-GAP-F — Politica di cleanup istanze di processo

> **Fonte reverse**: `02_bpm.md §7` — "Cleanup: archive 7 giorni, delete 1 giorno"

### Synopsis

In Appian, le istanze di processo completate vengono archiviate dopo 7 giorni e cancellate dopo 1 giorno (dall'archiviazione). In Flowable 7 embedded, le definizioni di processo e le istanze completate (`ACT_HI_*`) restano nel database a tempo indeterminato. Per non accumulare dati storici indefinitamente nella POC, è necessaria una politica di cleanup.

### Impatto DB

Nessuna nuova tabella. Configurazione del job Flowable di pulizia storia:

### Impatto Backend / Configurazione

```yaml
# application.yml — profilo poc
flowable:
  history-level: audit            # conserva solo dati di audit (no dettaglio variabili)
  process-definition-cache-limit: 10
  async-history:
    enable: false                  # sincrono per POC

# Configurazione job di cleanup storia Flowable
spring:
  batch:
    job:
      enabled: false               # disabilita Spring Batch se non usato
```

**Script di manutenzione** (da aggiungere in `scripts/cleanup-flowable-history.sql`):

```sql
-- Eseguire periodicamente (es. weekly in POC)
-- Cancella istanze di processo completate da più di 30 giorni
DELETE FROM ACT_HI_PROCINST  WHERE END_TIME_ < DATE_SUB(NOW(), INTERVAL 30 DAY);
DELETE FROM ACT_HI_TASKINST  WHERE END_TIME_ < DATE_SUB(NOW(), INTERVAL 30 DAY);
DELETE FROM ACT_HI_ACTINST   WHERE END_TIME_ < DATE_SUB(NOW(), INTERVAL 30 DAY);
DELETE FROM ACT_HI_VARINST   WHERE LAST_UPDATED_TIME_ < DATE_SUB(NOW(), INTERVAL 30 DAY);
```

> Per la POC si usa 30 giorni (più permissivo dei 7+1 giorni Appian). Per il target enterprise, configurare secondo policy di data retention aziendale.

**Aggiornare `07_Deployment_Locale.md §7 Note operative`**:

```
- Cleanup storia Flowable: script `scripts/cleanup-flowable-history.sql` da eseguire
  manualmente o schedulato. Per reset completo della POC usare `docker compose down -v`.
```

---

## Tabella riepilogativa Parte B — Nuove modifiche schema DB

| Tabella | Tipo | Descrizione | TECNICO-GAP |
|---|---|---|---|
| `attachment` | ALTER ADD COLUMN | `link_download VARCHAR(2500) NULL` | TECNICO-GAP-A |
| `attachment` | ALTER ADD COLUMN | `download_status VARCHAR(20) DEFAULT 'PENDING'` | TECNICO-GAP-A |
| `task` | ALTER ADD COLUMN | `sla_due_date DATETIME(3) NULL` | TECNICO-GAP-B |
| `case_note` | CREATE | Note intermediate per pratica (lavorazione + cambio stato + chiusura) | TECNICO-GAP-C |

## Tabella riepilogativa Parte B — Nuovi endpoint API

| Metodo | Path | Tipo | TECNICO-GAP |
|---|---|---|---|
| GET | `/practices/{id}/notes` | NUOVO | TECNICO-GAP-C |
| POST | `/practices/{id}/notes` | NUOVO | TECNICO-GAP-C |
| GET | `/tasks/{id}` | MODIFICA — aggiunge `slaDueDate`, `slaStatus` | TECNICO-GAP-B |

## Tabella riepilogativa Parte B — Nuove configurazioni

| Chiave `application.yml` | Valore default | TECNICO-GAP |
|---|---|---|
| `document-download.timeout-ms` | `10000` | TECNICO-GAP-A |
| `document-download.fallback-bucket` | `anc-temp` | TECNICO-GAP-A |
| `document-download.enabled` | `true` | TECNICO-GAP-A |
| `task.sla-working-days` | `5` | TECNICO-GAP-B |
| `debug.default-codice-doc-id` | `null` | TECNICO-GAP-E |
