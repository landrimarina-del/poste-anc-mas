# Inbound InterfaceAgreement Alignment Report (2026-05-14)

## Scope richiesto

Allineamento tra sorgente di verita' (`docs/requirements/source-of-truth/InterfaceAgreement.md`) e sviluppo per i punti:

1. Protezione endpoint inbound con API key tecnica
2. Allineamento campi payload obbligatori
3. Allineamento struttura `DOCUMENTI` / `CONTENUTI`
4. Validazione dominio `CODICE_DOC_ID`
5. Allineamento comportamento/contratto documentato tra fonte e implementazione

## Agenti consultati

Sono stati consultati due agenti di supporto per il design della patch e la strategia di allineamento documentale:

- `Backend patch design`: proposta di modifica codice su security + inbound DTO/service
- `Contract-doc alignment`: proposta di aggiornamento minimale del documento fonte di verita'

Entrambe le proposte sono state consolidate e applicate manualmente nel codice.

## Evidenze pre-patch (sintesi)

- Endpoint inbound pubblico (`permitAll`) senza API key.
- DTO inbound non coerente con campi obbligatori della fonte.
- Struttura documenti non coerente (`FILE_NAME` flat vs `CONTENUTI` annidato).
- Nessuna validazione dominio `CODICE_DOC_ID` = {1,2,3}.
- JSON malformato gestito in errore generico (500), non con codice di dominio.

## Patch applicate

### 1) Sicurezza API key tecnica inbound

- Aggiunto filtro: `apps/backend/src/main/java/it/poste/anc/shared/security/BpmInboundApiKeyFilter.java`
- Wiring filtro nella chain security: `apps/backend/src/main/java/it/poste/anc/shared/security/SecurityConfig.java`
- Configurazione env-based:
  - `apps/backend/src/main/resources/application.yml`
  - proprietà: `anc.security.bpm.inbound.api-key`, `anc.security.bpm.inbound.header-name`
- Default POC in compose:
  - `docker-compose.yml`
  - `ANC_BPM_INBOUND_API_KEY=anc-poc-bpm-inbound-key`
  - `ANC_BPM_INBOUND_HEADER_NAME=X-SD-API-Key`

### 2) Allineamento payload inbound

- Refactor DTO inbound:
  - `apps/backend/src/main/java/it/poste/anc/bpmgw/inbound/BpmPracticeOpenRequest.java`
- Campi gestiti:
  - `CANALE`, `ID_WORKITEM`, `NUM_PRATICA`, `CF_CLIENTE`, `DATA_INSERIMENTO_RICHIESTA`, `DOCUMENTI`
- Compatibilita' legacy:
  - alias `REQUEST_ID` / `APPIAN_TICKET_ID` su `NUM_PRATICA`

### 3) Struttura DOCUMENTI/CONTENUTI

- DTO aggiornato a struttura annidata:
  - `DOCUMENTI[].CODICE_DOC_ID`
  - `DOCUMENTI[].CONTENUTI[].NOME_FILE`
  - `DOCUMENTI[].CONTENUTI[].ESTENSIONE`
  - `DOCUMENTI[].CONTENUTI[].ID_DOC`
  - `DOCUMENTI[].CONTENUTI[].LINKDOWNLOAD`

### 4) Validazioni dominio e obbligatorieta'

- Aggiornato service inbound:
  - `apps/backend/src/main/java/it/poste/anc/bpmgw/inbound/BpmPracticeInboundService.java`
- Validazioni applicate:
  - campi obbligatori presenti
  - `DOCUMENTI` non vuoto e con `CONTENUTI`
  - `CODICE_DOC_ID` in `{1,2,3}`
  - timestamp `DATA_INSERIMENTO_RICHIESTA` nel formato `dd/MM/yyyy HH:mm:ss`
- Idempotenza `ID_WORKITEM` preservata
- Persistenza su `practice` preservata

### 5) Gestione errore JSON malformato

- Handler dedicato aggiunto:
  - `apps/backend/src/main/java/it/poste/anc/shared/common/GlobalExceptionHandler.java`
- Ora restituisce `resultCode=-4` per JSON malformato

### 6) Allineamento script di test Sprint 1

- `scripts/sprint1/bpm-open-happy.json`
- `scripts/sprint1/bpm-open-ko4-invalid-doc-code.json`
- `scripts/sprint1/test_open_practice.ps1`

Aggiornati al nuovo contratto payload.

### 7) Allineamento fonte di verita'

- `docs/requirements/source-of-truth/InterfaceAgreement.md`
- Aggiunto addendum POC con:
  - autenticazione tecnica API key
  - input/output POC correnti
  - codici esito POC
  - nota su alias legacy

## Verifica post-patch

Controllo errori statici su file modificati: nessun errore rilevato tramite tooling IDE (`get_errors`) su Java, script e documento.

## Impatto operativo

Per chiamare l'inbound dopo patch e' ora necessario header API key:

- Header di default: `X-SD-API-Key`
- Valore POC compose: `anc-poc-bpm-inbound-key`

Esempio:

```powershell
curl.exe -i -s -X POST http://localhost/api/v1/bpm/practices `
-H "Content-Type: application/json" `
-H "X-SD-API-Key: anc-poc-bpm-inbound-key" `
-d '{"CANALE":"BPM","ID_WORKITEM":"WI-9001","NUM_PRATICA":"REQ-9001","CF_CLIENTE":"RSSMRA80A01H501U","DATA_INSERIMENTO_RICHIESTA":"14/05/2026 12:10:00","DOCUMENTI":[{"CODICE_DOC_ID":1,"CONTENUTI":[{"NOME_FILE":"verbale.pdf","ESTENSIONE":"pdf","ID_DOC":"DOC-9001","LINKDOWNLOAD":"https://example.local/docs/DOC-9001"}]}]}'
```

## Note

- L'adozione di naming neutro e API key tecnica rende il protocollo coerente con i principi architetturali correnti (indipendenza da sistemi legacy).
- L'endpoint mantiene comportamento sincrono e idempotenza su `ID_WORKITEM`.
