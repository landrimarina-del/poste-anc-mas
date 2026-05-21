---
app: "Attivazione Nuova Carta"
prefix: "BOA_ANC"
layer: "technical"
export_version: "20260506"
analyzed: "2026-05-20"
output_dir: "docs/reverse/attivazione-nuova-carta/v20260506/"
---

# Attivazione Nuova Carta — Architettura Tecnica Runtime

> **Scope**: Mappa delle integrazioni, contratti API, ConnectedSystem, CDT usati come contratti,  
> costanti tecniche e dipendenze runtime end-to-end.  
> Non include: logica BPM interna, layout form, mapping entity/datastore, orchestrazione human task.

---

## STEP 0 — Inventario Risorse Tecniche

### Tabella Inventario

| Tipo Artefatto | Nome | UUID / Namespace | File |
|---|---|---|---|
| WebApi | BOA_ANC_WebApi_CreaPratica | `6c1f35fd-c264-42e5-899e-51696acf7b9e` | `webApi/6c1f35fd-c264-42e5-899e-51696acf7b9e.xml` |
| ConnectedSystem | BOA_ANC_SistemaConnesso_BPM | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9109283` | `connectedSystem/_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9109283.xml` |
| Integration Item | BOA_ANC_Integrazione_BPM_InvioCallback | `da95cbf8-6680-45ce-b50c-fbca5136b757` | `content/da95cbf8-6680-45ce-b50c-fbca5136b757.xml` |
| Integration Item | BOA_ANC_Integrazione_DownloadDocument | `a047c306-f234-4b5f-a6b9-d0b8e3ccb8c9` | `content/a047c306-f234-4b5f-a6b9-d0b8e3ccb8c9.xml` |
| CDT (Datatype) | BOA_ANC_Pratica | `{urn:com:appian:types:anc}BOA_ANC_Pratica` | `datatype/%7B...%7DBOA_ANC_Pratica.xsd` |
| CDT (Datatype) | BOA_ANC_Cliente | `{urn:com:appian:types:anc}BOA_ANC_Cliente` | `datatype/%7B...%7DBOA_ANC_Cliente.xsd` |
| CDT (Datatype) | BOA_ANC_DatiCarta | `{urn:com:appian:types:anc}BOA_ANC_DatiCarta` | `datatype/%7B...%7DBOA_ANC_DatiCarta.xsd` |
| CDT (Datatype) | BOA_ANC_ContenutiDenuncia | `{urn:com:appian:types:anc}BOA_ANC_ContenutiDenuncia` | `datatype/%7B...%7DBOA_ANC_ContenutiDenuncia.xsd` |
| CDT (Datatype) | BOA_ANC_Documento | `{urn:com:appian:types:anc}BOA_ANC_Documento` | `datatype/%7B...%7DBOA_ANC_Documento.xsd` |
| CDT (Datatype) | BOA_ANC_Response_BPM_InvioCallback | `{urn:com:appian:types:anc}BOA_ANC_Response_BPM_InvioCallback` | `datatype/%7B...%7DBOA_ANC_Response_BPM_InvioCallback.xsd` |
| CDT (Datatype) | BOA_ANC_ScodamentoStati | `{urn:com:appian:types:anc}BOA_ANC_ScodamentoStati` | `datatype/%7B...%7DBOA_ANC_ScodamentoStati.xsd` |
| CDT (Datatype) | BOA_ANC_GestioneScodamento | `{urn:com:appian:types:anc}BOA_ANC_GestioneScodamento` | `datatype/%7B...%7DBOA_ANC_GestioneScodamento.xsd` |
| CDT (Datatype) | BOA_ANC_Debug_WsInput | `{urn:com:appian:types:anc}BOA_ANC_Debug_WsInput` | `datatype/%7B...%7DBOA_ANC_Debug_WsInput.xsd` |
| CDT (Datatype) | BOA_ANC_StatiPratica | `{urn:com:appian:types:anc}BOA_ANC_StatiPratica` | `datatype/%7B...%7DBOA_ANC_StatiPratica.xsd` |
| CDT (Datatype) | BOA_ANC_Audit | `{urn:com:appian:types:anc}BOA_ANC_Audit` | `datatype/%7B...%7DBOA_ANC_Audit.xsd` |
| CDT (Datatype) | BOA_ANC_FiltriUtente | `{urn:com:appian:types:anc}BOA_ANC_FiltriUtente` | `datatype/%7B...%7DBOA_ANC_FiltriUtente.xsd` |
| CDT (Datatype) | BOA_ANC_CaseChecklist | `{urn:com:appian:types:anc}BOA_ANC_CaseChecklist` | `datatype/%7B...%7DBOA_ANC_CaseChecklist.xsd` |
| CDT (Datatype) | BOA_ANC_TipiDocumento | `{urn:com:appian:types:anc}BOA_ANC_TipiDocumento` | `datatype/%7B...%7DBOA_ANC_TipiDocumento.xsd` |
| Costante PM | BOA_ANC_PROCESSO_WEBAPI_CREAPRATICA | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9074201` | `content/_a-...9074201.xml` |
| Costante PM | BOA_ANC_PROCESSO_CAMBIOSTATO | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9086106` | `content/_a-...9086106.xml` |
| Costante PM | BOA_ANC_PROCESSO_SALVATAGGIODATI | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9108645` | `content/_a-...9108645.xml` |
| Costante PM | BOA_ANC_PROCESSO_SCARICASINGOLODOC | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9095576` | `content/_a-...9095576.xml` |
| Costante PM | BOA_ANC_PROCESSO_TIPIZZAZIONEDOC | `_a-0000ead1-5a62-8000-9c41-011c48011c48_10799895` | `content/_a-...10799895.xml` |
| Costante Tecnica | BOA_ANC_RELATIVEPATH_SCODAMENTOSTATI | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9109290` | `content/_a-...9109290.xml` |
| Costante Tecnica | BOA_ANC_NUMBER_MAXRETRY | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9113710` | `content/_a-...9113710.xml` |
| Costante Tecnica | BOA_ANC_NUMEROESITITOSEND | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9109114` | `content/_a-...9109114.xml` |
| Costante Tecnica | BOA_ANC_STATIINVIO | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9109016` | `content/_a-...9109016.xml` |
| Costante Tecnica | BOA_ANC_CANALE | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9072365` | `content/_a-...9072365.xml` |
| Costante Debug | BOA_ANC_DEBUG_ATTIVABATCHCALLBACK | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9156854` | `content/_a-...9156854.xml` |
| Costante Debug | BOA_ANC_DEBUG_MOCK_CALLBACK | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9171971` | `content/_a-...9171971.xml` |
| Costante Debug | BOA_ANC_DEBUG_ABILITASERVICENOW | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9079307` | `content/_a-...9079307.xml` |
| Costante Debug | BOA_ANC_DEBUG_VISUALIZZASEGNALAZIONISINERGIA | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9079278` | `content/_a-...9079278.xml` |
| Costante Debug | BOA_ANC_DEBUG_DEFAULTCODICEDOCID | `_a-0000ead1-5a62-8000-9c41-011c48011c48_10808176` | `content/_a-...10808176.xml` |
| Expression Rule | BOA_ANC_GeneraRequest_BPM_InvioCallback | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9109317` | `content/_a-...9109317.xml` |
| Expression Rule | BOA_ANC_ElaboraResponse_BPM_InvioCallback | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9109335` | `content/_a-...9109335.xml` |
| Expression Rule | BOA_ANC_InputWS_HttpResponse | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9074231` | `content/_a-...9074231.xml` |

---

## STEP 1 — WebApi Contracts

### Tabella WebApi

| Nome WebApi | Endpoint | Metodo | Auth | Processo Avviato | Modalità | Output Success | Output Error |
|---|---|---|---|---|---|---|---|
| BOA_ANC_WebApi_CreaPratica | `/suite/webapi/boaanccreapratica` | POST | Nessun guest; gruppi: Operatore WebApi ANC, BOA ANC All Users | BOA_ANC_ProcessoWebApi_CreaPratica | **Sincrona** (startProcess → pv.responseOut) | `cast(2802, fv!processInfo.pv.responseOut)` — httpResponse nativa | HTTP 500 · body JSON `{info:{resultCode:"-999", resultMessage:"Errore durante l'esecuzione del processo di creazione pratica"}}` |

### Dettaglio BOA_ANC_WebApi_CreaPratica

**UUID**: `6c1f35fd-c264-42e5-899e-51696acf7b9e`  
**URL Alias**: `boaanccreapratica`  
**Metodo HTTP**: POST  
**Logging**: disabilitato  
**Request Body Type**: NONE (body libero, letto tramite `http!request`)

**Gruppi autorizzati**:
- `web_api_administrator`: BOA ANC Administrators (`_e-..._2236`)
- `web_api_viewer`: Operatore WebApi ANC (`_e-..._2240`), BOA ANC All Users (`_e-..._2234`)

**Espressione WebApi (verbatim)**:
```appian
#"SYSTEM_SYSRULES_startProcess"(
  processModel: #"_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9074201",
  processParameters: { input: http!request },
  onSuccess: cast(2802, fv!processInfo.pv.responseOut),
  onError: #"_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9074231"(
    resultCode: "-999",
    resultMessage: "Errore durante l'esecuzione del processo di creazione pratica",
    statusCode: 500
  )
)
```

**Meccanismo**:  
La WebApi avvia **sincrona** il processo `BOA_ANC_ProcessoWebApi_CreaPratica` tramite la costante `BOA_ANC_PROCESSO_WEBAPI_CREAPRATICA`. Attende il completamento e restituisce la process variable `responseOut` castata a tipo `2802` (HttpResponse Appian). In caso di errore di avvio processo, invoca la regola `BOA_ANC_InputWS_HttpResponse` con HTTP 500.

**Request Body — Struttura JSON attesa** (desunta dal sample presente nel file WebApi):

```json
{
  "CANALE": "APP",
  "ID_WORKITEM": "<string>",
  "NUM_PRATICA": "<string>",
  "CF_CLIENTE": "<string>",
  "DATA_INSERIMENTO_RICHIESTA": "<dd/MM/yyyy HH:mm:ss>",
  "CLIENTE": {
    "NOME": "<string>",
    "COGNOME": "<string>",
    "SESSO": "<string>",
    "DATANASCITA": "<dd/MM/yyyy>",
    "COMUNENASCITA": "<string>",
    "PROVINCIANASCITA": "<string>",
    "NAZIONENASCITA": "<string>",
    "CITTADINANZA": "<string>",
    "CELLULARE": "<string>",
    "TELEFONO": "<string>",
    "INDIRIZZO_DI_RESIDENZA": {
      "LUOGO": "<string>",
      "COMUNE": "<string>",
      "PROVINCIA": "<string>",
      "NAZIONE": "<string>",
      "CAP": "<string>",
      "CIVICO": "<string>"
    }
  },
  "DATI_CARTA_BLOCCATA": {
    "I_NUMERO_CARTA": "<string>",
    "I_TIPO_CARTA": "<string>",
    "I_INTEST_CARTA": "<string>"
  },
  "DOCUMENTI": {
    "CODICE_DOC_ID": "<string>",
    "CONTENUTI": [
      {
        "NOME_FILE": "<string>",
        "ESTENSIONE": "<string>",
        "ID_DOC": "<string>",
        "LINKDOWNLOAD": "<url>"
      }
    ]
  }
}
```

**Response Success** (HTTP 200):  
Struttura determinata dal processo — `pv.responseOut` è di tipo `HttpResponse` (Appian type 2802).  
Il corpo tipico (da `BOA_ANC_InputWS_HttpResponse`) include:
```json
{
  "info": {
    "resultCode": "<string>",
    "resultMessage": "<string>",
    "details": "<string>"
  },
  "appianTicketId": "<int>"
}
```

**Response Error** (HTTP 500 — errore avvio processo):
```json
{
  "info": {
    "resultCode": "-999",
    "resultMessage": "Errore durante l'esecuzione del processo di creazione pratica",
    "details": null
  }
}
```

**Validazione input** (eseguita dal PM `BOA_ANC_ProcessoWebApi_CreaPratica` via regole expression):  
Le chiavi obbligatorie sono censite nelle costanti:
- `BOA_ANC_CHIAVIOBBLIGATORIE_ROOT_CREAZIONEPRATICA` (content `_a-...9071252`)
- `BOA_ANC_CHIAVIOBBLIGATORIE_CLIENTE_CREAZIONEPRATICA` (content `_a-...9071258`)
- `BOA_ANC_CHIAVIOBBLIGATORIE_DATICARTA_CREAZIONEPRATICA` (content `_a-...9071271`)
- `BOA_ANC_CHIAVIOBBLIGATORIE_DENUNCIA_CREAZIONEPRATICA` (content `_a-...9071283`)
- `BOA_ANC_CHIAVIOBBLIGATORIE_CONTENUTI_CREAZIONEPRATICA` (content `_a-...9071290`)

---

## STEP 2 — Connected Systems

### Tabella Connected Systems

| Nome | UUID | Tipo | Base URL | Tipo Auth | Token URL | Scope | Usato da |
|---|---|---|---|---|---|---|---|
| BOA_ANC_SistemaConnesso_BPM | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9109283` | HTTP REST (`system.http`) | `https://bpm-attivazione-carte-eap-bpm-attivazione-carte.apps.gen3gppito.cloudcoll.poste.it` | OAuth Client Credentials Grant | `https://login.microsoftonline.com/08b638e8-0676-45cc-b677-5c038b17d28a/oauth2/v2.0/token` | `24de986e-2c18-4207-8e3f-72f288595a7e/.default` | BOA_ANC_Integrazione_BPM_InvioCallback |

### Dettaglio BOA_ANC_SistemaConnesso_BPM

| Campo | Valore |
|---|---|
| **Nome** | BOA_ANC_SistemaConnesso_BPM |
| **UUID** | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9109283` |
| **Protocollo** | HTTP REST (`system.http`) |
| **Base URL** | `https://bpm-attivazione-carte-eap-bpm-attivazione-carte.apps.gen3gppito.cloudcoll.poste.it` |
| **Auth Type** | OAuth Client Credentials Grant |
| **Token Endpoint** | `https://login.microsoftonline.com/08b638e8-0676-45cc-b677-5c038b17d28a/oauth2/v2.0/token` |
| **Tenant ID** | `08b638e8-0676-45cc-b677-5c038b17d28a` (Microsoft Entra ID) |
| **Scope** | `24de986e-2c18-4207-8e3f-72f288595a7e/.default` |
| **Client ID** | non esportato (campo vuoto nell'export) |
| **Client Secret** | non esportato (campo `EncryptedText`) |
| **Include Scope** | true |
| **Timeout** | non configurato a livello ConnectedSystem (configurato per integration item) |
| **Readers (group)** | BOA ANC All Users (`_e-..._2234`) |
| **Administrators (group)** | BOA ANC Administrators (`_e-..._2236`) |

> **SECURITY NOTE**: client_secret non presente nell'export (campo `EncryptedText` — valore cifrato non leggibile).

---

## STEP 3 — Integration Items

### Tabella Integration Items

| Nome | UUID | ConnectedSystem | Endpoint Completo | Metodo | Body | Response Mapping | Error Handling |
|---|---|---|---|---|---|---|---|
| BOA_ANC_Integrazione_BPM_InvioCallback | `da95cbf8-6680-45ce-b50c-fbca5136b757` | BOA_ANC_SistemaConnesso_BPM | `{BaseURL}` + `ri!relativePath` (valore da `BOA_ANC_RELATIVEPATH_SCODAMENTOSTATI`) | POST | `ri!request` — JSON string generato da `BOA_ANC_GeneraRequest_BPM_InvioCallback` | `BOA_ANC_ElaboraResponse_BPM_InvioCallback` → `BOA_ANC_Response_BPM_InvioCallback` | Timeout 50s; errore → `esito: false`, `descrizioneEsito: <messaggio>` |
| BOA_ANC_Integrazione_DownloadDocument | `a047c306-f234-4b5f-a6b9-d0b8e3ccb8c9` | Nessun ConnectedSystem — URL diretto da `ri!url` | `ri!url` (URL da campo `LINKDOWNLOAD` del JSON di creazione pratica) | GET | Nessun body | Binario (`application/octet-stream`) — documento salvato in Appian come file `{nomeFile}.{estensione}` | Timeout 10s; fallback cartella: `BOA_ANC_FOLDER_TEMP` |

### Dettaglio BOA_ANC_Integrazione_BPM_InvioCallback

**UUID**: `da95cbf8-6680-45ce-b50c-fbca5136b757`  
**Folder**: BOA ANC Integrations (`_a-...9071033`)  
**ConnectedSystem**: BOA_ANC_SistemaConnesso_BPM  
**Auth**: ereditata dal ConnectedSystem (OAuth Client Credentials — token bearer automatico)  
**Metodo**: POST  
**Content-Type**: `application/json`  
**Timeout**: 50 secondi  
**URL**: `isInheritedUrlOptionSelected = true` → usa BaseURL del ConnectedSystem  
**Relative Path**: `ri!relativePath` — valore runtime proveniente dalla costante `BOA_ANC_RELATIVEPATH_SCODAMENTOSTATI` (environment-specific)  
**Body**: `ri!request` — stringa JSON generata dall'expression rule `BOA_ANC_GeneraRequest_BPM_InvioCallback`

**Input parameters**:

| Parametro | Tipo | Descrizione |
|---|---|---|
| `relativePath` | string | Path relativo dell'endpoint BPM (da costante `BOA_ANC_RELATIVEPATH_SCODAMENTOSTATI`) |
| `request` | string | Body JSON serializzato (generato da `BOA_ANC_GeneraRequest_BPM_InvioCallback`) |

**Struttura Body generata da `BOA_ANC_GeneraRequest_BPM_InvioCallback`** (desunta dalla regola):  
La regola legge `pratica` (via `BOA_ANC_GetPraticaByIdCase`) e `checkList` (via `BOA_ANC_GetCaseCheckListByIdCase`), poi calcola il `codiceEsito` tramite `BOA_ANC_Decisione_CodiceEsitoCallback` e produce un JSON che include:
- `esito` (stringa — es. "OK" o codice KO)
- `motivazioni` (lista codici checklist non conformi, se esito ≠ OK)

**Response mapping — `BOA_ANC_ElaboraResponse_BPM_InvioCallback`**:  
Valida il JSON di risposta con `jsonvalidator_isvalid()`, poi mappa nel CDT `BOA_ANC_Response_BPM_InvioCallback`:
```
esito:           (response["esito"].trim().upper() == "OK") → boolean
descrizioneEsito: se esito=OK → null; altrimenti → response["descrizioneEsito"]
```

**Invocata da**: `BOA_ANC_ChiamaWS_BPM_InvioCallback` → invocato da `BOA_ANC_ScodamentoSingoloEsito` → invocato da `BOA_ANC_Batch_ScodamentoCallback`

---

### Dettaglio BOA_ANC_Integrazione_DownloadDocument

**UUID**: `a047c306-f234-4b5f-a6b9-d0b8e3ccb8c9`  
**Folder**: BOA ANC Integrations (`_a-...9071033`)  
**ConnectedSystem**: Nessuno — URL completo passato come `ri!url`  
**Auth**: None  
**Metodo**: GET  
**Body**: nessuno  
**Content-Type response**: `application/octet-stream` (binario)  
**Timeout**: 10 secondi  
**Salvataggio documento**: binario → file Appian con nome `{ri!nomeFile}.{ri!estensione}`  
**Cartella destinazione**: `ri!cartellaDestinazione` oppure (se null) `BOA_ANC_FOLDER_TEMP` (`_a-...9077265`)

**Input parameters**:

| Parametro | Tipo | Descrizione |
|---|---|---|
| `url` | string | URL di download del documento (valore di `LINKDOWNLOAD` dal JSON di creazione pratica) |
| `nomeFile` | string | Nome file (da `NOME_FILE` nel JSON) |
| `estensione` | string | Estensione file (da `ESTENSIONE` nel JSON) |
| `cartellaDestinazione` | CollaborationFolder | Folder Appian di destinazione; se null → `BOA_ANC_FOLDER_TEMP` |
| `onSuccess` | Variant | Callback invocata in caso di successo |
| `onError` | Variant | Callback invocata in caso di errore |

**Invocata da**: `BOA_ANC_ScaricaSingoloDoc` (PM `0002e9f3-a766-8000-5af1-7f0000014e7a`)  
**Chiamata da**: `BOA_ANC_DownloadContenuti_CreazionePratica` (PM `0002e9f3-ab47-8000-5af7-7f0000014e7a`) — per ogni item in `CONTENUTI` del JSON di input

---

## STEP 4 — Datatype Runtime (Contratti CDT)

> Sono documentati i CDT effettivamente usati come contratti di integrazione  
> (input WebApi → JSON parsing, payload integration, response BPM, tracking scodamento).

### Tabella CDT usati in Integrazione

| Nome CDT | Namespace | Usato in | Contesto |
|---|---|---|---|
| BOA_ANC_Pratica | `urn:com:appian:types:anc` | WebApi → PM creazione pratica, `BOA_ANC_GeneraRequest_BPM_InvioCallback` | Persistenza dati pratica; sorgente per body callback |
| BOA_ANC_Cliente | `urn:com:appian:types:anc` | JSON parsing WebApi → `BOA_ANC_ElaborazioneJsonCreaPratica_Cliente` | Input mapping da CLIENTE nel request JSON |
| BOA_ANC_DatiCarta | `urn:com:appian:types:anc` | JSON parsing WebApi → `BOA_ANC_ElaborazioneJsonCreaPratica_DatiCarta` | Input mapping da DATI_CARTA_BLOCCATA nel request JSON |
| BOA_ANC_ContenutiDenuncia | `urn:com:appian:types:anc` | JSON parsing WebApi → `BOA_ANC_ElaborazioneJsonCreaPratica_ContenutiDenuncia`; `BOA_ANC_Integrazione_DownloadDocument` | Array CONTENUTI del request JSON; usata per feed `LINKDOWNLOAD` |
| BOA_ANC_Documento | `urn:com:appian:types:anc` | JSON parsing WebApi → `BOA_ANC_ElaborazioneJsonCreaPratica_Documento` | Mapping DOCUMENTI/CODICE_DOC_ID dal request JSON |
| BOA_ANC_Response_BPM_InvioCallback | `urn:com:appian:types:anc` | Output di `BOA_ANC_Integrazione_BPM_InvioCallback` | Response del sistema BPM esterno dopo callback |
| BOA_ANC_ScodamentoStati | `urn:com:appian:types:anc` | `BOA_ANC_Batch_ScodamentoCallback`, `BOA_ANC_ScodamentoSingoloEsito` | Tracking stato invio callback verso BPM |
| BOA_ANC_GestioneScodamento | `urn:com:appian:types:anc` | `BOA_ANC_Batch_ScodamentoCallback` | Semaforo/controllo batch callback (inEsecuzione flag) |
| BOA_ANC_Debug_WsInput | `urn:com:appian:types:anc` | `BOA_ANC_Processo_ScritturaTabellaDebugWS` | Log debug input/output WebApi (tabella di audit interno) |

### Struttura CDT rilevanti per integrazione

```
BOA_ANC_Pratica {
  id:                      int         [optional — @Id @GeneratedValue]
  idCase:                  int         [optional]
  dataApertura:            dateTime    [optional]
  dataUltimaModifica:      dateTime    [optional]
  operatoreUltimaModifica: string(255) [optional]
  stato:                   string(255) [optional]
  dataChiusura:            dateTime    [optional]
  canale:                  string(255) [optional]   ← valorizzato da BOA_ANC_CANALE ["APP","WEB"]
  idWorkitem:              string(255) [optional]   ← da ID_WORKITEM nel request JSON
  numPratica:              string(255) [optional]   ← da NUM_PRATICA nel request JSON
  cfCliente:               string(255) [optional]   ← da CF_CLIENTE nel request JSON
  codiceCliente:           string(255) [optional]
  dataInserimentoRichiesta: dateTime   [optional]   ← da DATA_INSERIMENTO_RICHIESTA
  folder:                  int         [optional]
  dataScadenza:            dateTime    [optional]
  esitoSD:                 string(255) [optional]   ← esito operatore Scrivania Digitale
  dataEsitoSD:             dateTime    [optional]
}

BOA_ANC_Cliente {
  id:               int         [optional — @Id @GeneratedValue]
  idCase:           int         [optional]
  nome:             string(255) [optional]   ← da CLIENTE.NOME
  cognome:          string(255) [optional]   ← da CLIENTE.COGNOME
  codiceFiscale:    string(255) [optional]   ← da CF_CLIENTE (root) o CLIENTE.*
  sesso:            string(255) [optional]   ← da CLIENTE.SESSO
  dataNascita:      date        [optional]   ← da CLIENTE.DATANASCITA
  comuneNascita:    string(255) [optional]
  provinciaNascita: string(255) [optional]
  nazioneNascita:   string(255) [optional]
  cittadinanza:     string(255) [optional]
  cellulare:        string(255) [optional]
  telefono:         string(255) [optional]
  indirizzoResidenza: string(500) [optional]  ← da INDIRIZZO_DI_RESIDENZA (serializzato)
  luogoResidenza:   string(255) [optional]
  ... (altri campi residenza)
}

BOA_ANC_DatiCarta {
  id:               int         [optional — @Id @GeneratedValue]
  idCase:           int         [optional]
  numeroCarta:      string(255) [optional]   ← da DATI_CARTA_BLOCCATA.I_NUMERO_CARTA
  tipoCarta:        string(255) [optional]   ← da DATI_CARTA_BLOCCATA.I_TIPO_CARTA
  intestazioneCarta: string(255) [optional]  ← da DATI_CARTA_BLOCCATA.I_INTEST_CARTA
}

BOA_ANC_ContenutiDenuncia {
  id:           int          [optional — @Id @GeneratedValue]
  idCase:       int          [optional]
  nomeFile:     string(255)  [optional]   ← da CONTENUTI[n].NOME_FILE
  estensione:   string(255)  [optional]   ← da CONTENUTI[n].ESTENSIONE
  idDoc:        string(255)  [optional]   ← da CONTENUTI[n].ID_DOC
  linkDownload: string(2500) [optional]   ← da CONTENUTI[n].LINKDOWNLOAD — URL download documento
  idDocAppian:  int          [optional]   ← ID documento Appian dopo download
}

BOA_ANC_Documento {
  id:           int         [optional — @Id @GeneratedValue]
  idCase:       int         [optional]
  codiceDocId:  int         [optional]   ← da DOCUMENTI.CODICE_DOC_ID
  descrizione:  string(255) [optional]
}

BOA_ANC_Response_BPM_InvioCallback {
  esito:            boolean       [optional]   ← true se response["esito"]=="OK"
  descrizioneEsito: string(4000)  [optional]   ← messaggio errore se esito=false
}

BOA_ANC_ScodamentoStati {
  id:        int         [optional — @Id @GeneratedValue]
  idcase:    int         [optional]
  esito:     int         [optional]   ← valori: 0=attesa, 1=inviato, 2=errore, 3=scartato
  errore:    string(255) [optional]   ← descrizione errore
  timestamp: dateTime    [optional]
  retry:     int         [optional]   ← contatore tentativi (max: BOA_ANC_NUMBER_MAXRETRY)
}

BOA_ANC_GestioneScodamento {
  id:             int         [optional — @Id @GeneratedValue]
  inEsecuzione:   boolean     [optional]   ← semaforo batch (previene esecuzioni parallele)
  dataInizio:     dateTime    [optional]
  dataFine:       dateTime    [optional]
  numEsitiToSend: int         [optional]   ← numero esiti da processare nel batch
}

BOA_ANC_Debug_WsInput {
  id:               int         [optional — @Id @GeneratedValue]
  jsonbody:         LONGTEXT    [optional]   ← corpo raw del request JSON alla WebApi
  sdResponse:       LONGTEXT    [optional]   ← risposta SD restituita dalla WebApi
  timestampInput:   dateTime    [optional]
  timestampOutput:  dateTime    [optional]
  tipologia:        string(255) [optional]
  idWorkItem:       string(255) [optional]
  idCase:           int         [optional]
}
```

---

## STEP 5 — Costanti di Configurazione Tecnica

### Tabella Costanti Tecniche

| Nome Costante | UUID | Categoria | Valore / Tipo | Env-Specific | Usato in |
|---|---|---|---|---|---|
| BOA_ANC_PROCESSO_WEBAPI_CREAPRATICA | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9074201` | PM Reference | ProcessModel → `BOA_ANC_ProcessoWebApi_CreaPratica` (`0002e9f3-3d79-8000-5a84-7f0000014e7a`) | No | `BOA_ANC_WebApi_CreaPratica` (startProcess) |
| BOA_ANC_PROCESSO_CAMBIOSTATO | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9086106` | PM Reference | ProcessModel → `BOA_ANC_Processo_CambioStato` (`0002e9f4-c789-8000-5b72-7f0000014e7a`) | No | Invocato da task operatore per cambio stato pratica |
| BOA_ANC_PROCESSO_SALVATAGGIODATI | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9108645` | PM Reference | ProcessModel → `BOA_ANC_SalvataggioDati` (`0002e9fa-4ae6-8000-5d18-7f0000014e7a`) | No | Persistenza dati da operatore task |
| BOA_ANC_PROCESSO_SCARICASINGOLODOC | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9095576` | PM Reference | ProcessModel → `BOA_ANC_ScaricaSingoloDoc` (`0002e9f3-a766-8000-5af1-7f0000014e7a`) | No | Invocato da `BOA_ANC_DownloadContenuti_CreazionePratica` per ogni documento |
| BOA_ANC_PROCESSO_TIPIZZAZIONEDOC | `_a-0000ead1-5a62-8000-9c41-011c48011c48_10799895` | PM Reference | ProcessModel → `BOA_ANC_Processo_TipizzaDoc` (`0002eb1d-4057-8000-f320-7f0000014e7a`) | No | Tipizzazione documenti allegati |
| BOA_ANC_RELATIVEPATH_SCODAMENTOSTATI | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9109290` | URL / Endpoint | string (env-specific — non valorizzata nell'export) | **Sì** | `BOA_ANC_Integrazione_BPM_InvioCallback` come `ri!relativePath` |
| BOA_ANC_NUMBER_MAXRETRY | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9113710` | Soglia Numerica | int (env-specific) | **Sì** | `BOA_ANC_ScodamentoSingoloEsito` — limite massimo retry prima di scartare |
| BOA_ANC_NUMEROESITITOSEND | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9109114` | Soglia Numerica | int (env-specific) | **Sì** | `BOA_ANC_Batch_ScodamentoCallback` — numero esiti da processare per run batch |
| BOA_ANC_STATIINVIO | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9109016` | Chiave Simbolica | Integer?list `[0, 1, 2, 3]` (0=attesa, 1=inviato, 2=errore, 3=scartato) | No | `BOA_ANC_ScodamentoStati.esito` — enum stati scodamento |
| BOA_ANC_CANALE | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9072365` | Chiave Simbolica | Text?list `["APP", "WEB"]` | No | Validazione campo `CANALE` nel request JSON della WebApi |
| BOA_ANC_DEBUG_ATTIVABATCHCALLBACK | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9156854` | Flag / Toggle | boolean (env-specific) | **Sì** | `BOA_ANC_Batch_ScodamentoCallback` — abilita/disabilita batch scodamento |
| BOA_ANC_DEBUG_MOCK_CALLBACK | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9171971` | Flag / Toggle | boolean (env-specific) | **Sì** | `BOA_ANC_ChiamaWS_BPM_InvioCallback` — se true bypassa chiamata reale BPM |
| BOA_ANC_DEBUG_ABILITASERVICENOW | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9079307` | Flag / Toggle | boolean (env-specific) | **Sì** | Abilita funzionalità ServiceNow (integrazione esterna non nell'export) |
| BOA_ANC_DEBUG_VISUALIZZASEGNALAZIONISINERGIA | `_a-0000e9c7-d9c2-8000-9c35-011c48011c48_9079278` | Flag / Toggle | boolean (env-specific) | **Sì** | Abilita visualizzazione segnalazioni Sinergia (integrazione esterna non nell'export) |
| BOA_ANC_DEBUG_DEFAULTCODICEDOCID | `_a-0000ead1-5a62-8000-9c41-011c48011c48_10808176` | Flag / Toggle | boolean (env-specific) | **Sì** | Se true → forza `CODICE_DOC_ID = 3` ignorando valore dal JSON; se false → legge da JSON |

> **Note critiche per re-platform**:
> - Le costanti contrassegnate come `Env-Specific = Sì` **non** hanno valore nell'export e devono essere configurate per ambiente in fase di deploy.
> - `BOA_ANC_RELATIVEPATH_SCODAMENTOSTATI` è il path relativo dell'endpoint BPM per il callback — deve essere allineato con il team BPM destinatario.
> - `BOA_ANC_DEBUG_ATTIVABATCHCALLBACK = false` disabilita completamente il batch di scodamento.

---

## STEP 6 — Runtime Dependency Map

### Diagramma Testuale Runtime

```text
─── ENTRY POINT 1: WebApi sincrona ───────────────────────────────────────────

[WebApi: POST /suite/webapi/boaanccreapratica]
  → BOA_ANC_ProcessoWebApi_CreaPratica
      ├── [Sub-PM] BOA_ANC_ElaborazioneJsonInput_CreazionePratica
      │       └── (parsing JSON input → BOA_ANC_Pratica, BOA_ANC_Cliente,
      │             BOA_ANC_DatiCarta, BOA_ANC_ContenutiDenuncia, BOA_ANC_Documento)
      ├── [Sub-PM] BOA_ANC_DownloadContenuti_CreazionePratica
      │       └── [Loop per ogni contenuto in CONTENUTI[]]
      │             └── [Sub-PM] BOA_ANC_ScaricaSingoloDoc
      │                     └── [Integration: BOA_ANC_Integrazione_DownloadDocument]
      │                             → ConnectedSystem: Nessuno
      │                               → Sistema Esterno: ri!url (LINKDOWNLOAD dal JSON)
      │                                 Protocollo: HTTP GET
      │                                 Auth: None
      │                                 Response: binary → documento Appian
      ├── [Sub-PM] BOA_ANC_Processo_ScritturaTabellaDebugWS  (se debug attivo)
      │       └── (scrive su BOA_ANC_Debug_WsInput — tabella audit)
      └── → responseOut → httpResponse al chiamante


─── ENTRY POINT 2: Batch asincrono ───────────────────────────────────────────

[Batch: BOA_ANC_Batch_ScodamentoCallback]
  (attivato solo se BOA_ANC_DEBUG_ATTIVABATCHCALLBACK = true)
  → BOA_ANC_Batch_ScodamentoCallback
      ├── (legge BOA_ANC_ScodamentoStati con esito=0 — in attesa)
      ├── (verifica semaforo BOA_ANC_GestioneScodamento.inEsecuzione)
      └── [Loop fino a BOA_ANC_NUMEROESITITOSEND esiti]
            └── [Sub-PM] BOA_ANC_ScodamentoSingoloEsito
                    └── [Sub-PM] BOA_ANC_ChiamaWS_BPM_InvioCallback
                            (se BOA_ANC_DEBUG_MOCK_CALLBACK = false)
                            └── [Integration: BOA_ANC_Integrazione_BPM_InvioCallback]
                                    → ConnectedSystem: BOA_ANC_SistemaConnesso_BPM
                                      → Sistema Esterno:
                                          https://bpm-attivazione-carte-eap-bpm-attivazione-carte.apps.gen3gppito.cloudcoll.poste.it
                                          + BOA_ANC_RELATIVEPATH_SCODAMENTOSTATI
                                        Protocollo: HTTP POST / application/json
                                        Auth: OAuth2 Client Credentials
                                               Token URL: https://login.microsoftonline.com/
                                                          08b638e8-0676-45cc-b677-5c038b17d28a/oauth2/v2.0/token
                                               Scope: 24de986e-2c18-4207-8e3f-72f288595a7e/.default
                                        Response: BOA_ANC_Response_BPM_InvioCallback
                                        Retry: max BOA_ANC_NUMBER_MAXRETRY
                                        Scartato dopo max retry: esito=3

─── ENTRY POINT 3: Azione operatore (cambio stato) ───────────────────────────

[Operatore UI — Task lavorazione / riepilogo]
  → BOA_ANC_Processo_CambioStato
      └── (aggiorna stato pratica su DB)
          └── (inserisce record in BOA_ANC_ScodamentoStati con esito=0
               per scodamento callback verso BPM)

─── ENTRY POINT 4: Tipizzazione documenti ────────────────────────────────────

[Operatore UI — Task tipizzazione documenti]
  → BOA_ANC_Processo_TipizzaDoc
      └── (gestisce tipizzazione manuale documenti allegati alla pratica)
```

### Tabella Dipendenze Tecniche

| Caller | Tipo Caller | Integration Item | ConnectedSystem | Sistema Esterno | Protocollo | Auth |
|---|---|---|---|---|---|---|
| BOA_ANC_ScaricaSingoloDoc | ProcessModel | BOA_ANC_Integrazione_DownloadDocument | Nessuno | URL da `LINKDOWNLOAD` (campo JSON input WebApi) | HTTP GET · `application/octet-stream` | None |
| BOA_ANC_ChiamaWS_BPM_InvioCallback | ProcessModel | BOA_ANC_Integrazione_BPM_InvioCallback | BOA_ANC_SistemaConnesso_BPM | `https://bpm-attivazione-carte-eap-bpm-attivazione-carte.apps.gen3gppito.cloudcoll.poste.it` + `BOA_ANC_RELATIVEPATH_SCODAMENTOSTATI` | HTTP POST · `application/json` | OAuth2 Client Credentials (Azure AD tenant `08b638e8-...`) |

---

## Appendice A — Processi tecnici censiti (senza dettaglio BPM)

| Nome ProcessModel | UUID PM | Costante referente | Ruolo nel flusso tecnico |
|---|---|---|---|
| BOA_ANC_ProcessoWebApi_CreaPratica | `0002e9f3-3d79-8000-5a84-7f0000014e7a` | `BOA_ANC_PROCESSO_WEBAPI_CREAPRATICA` | Entry point principale — avviato da WebApi, coordina parsing JSON, download documenti, persistenza |
| BOA_ANC_ElaborazioneJsonInput_CreazionePratica | `0002e9f3-4e47-8000-5a89-7f0000014e7a` | — | Parsing e validazione del JSON di input; popola CDT Pratica, Cliente, DatiCarta, ContenutiDenuncia, Documento |
| BOA_ANC_DownloadContenuti_CreazionePratica | `0002e9f3-ab47-8000-5af7-7f0000014e7a` | — | Loop su array CONTENUTI — invoca BOA_ANC_ScaricaSingoloDoc per ogni elemento |
| BOA_ANC_ScaricaSingoloDoc | `0002e9f3-a766-8000-5af1-7f0000014e7a` | `BOA_ANC_PROCESSO_SCARICASINGOLODOC` | Invoca `BOA_ANC_Integrazione_DownloadDocument` per scaricare un singolo documento da URL esterno |
| BOA_ANC_Processo_CambioStato | `0002e9f4-c789-8000-5b72-7f0000014e7a` | `BOA_ANC_PROCESSO_CAMBIOSTATO` | Aggiorna stato pratica; inserisce record in `BOA_ANC_ScodamentoStati` per trigger callback BPM |
| BOA_ANC_SalvataggioDati | `0002e9fa-4ae6-8000-5d18-7f0000014e7a` | `BOA_ANC_PROCESSO_SALVATAGGIODATI` | Persistenza dati aggiornati da operatore |
| BOA_ANC_Batch_ScodamentoCallback | `0002e9fa-5b2a-8000-5d3d-7f0000014e7a` | — | Batch scheduler — estrae esiti in attesa e avvia scodamento verso BPM |
| BOA_ANC_ScodamentoSingoloEsito | `0002e9fa-5f0b-8000-5d4b-7f0000014e7a` | — | Gestisce un singolo invio callback; implementa retry logic (max `BOA_ANC_NUMBER_MAXRETRY`) |
| BOA_ANC_ChiamaWS_BPM_InvioCallback | `0002e9fa-610c-8000-5d5b-7f0000014e7a` | — | Invoca `BOA_ANC_Integrazione_BPM_InvioCallback`; se `BOA_ANC_DEBUG_MOCK_CALLBACK=true` → mocka la chiamata |
| BOA_ANC_Processo_ScritturaTabellaDebugWS | `0007e9fa-6393-8000-5d66-7f0000014e7a` | — | Scrive su `BOA_ANC_Debug_WsInput` — audit log dei request/response WebApi |
| BOA_ANC_Processo_TipizzaDoc | `0002eb1d-4057-8000-f320-7f0000014e7a` | `BOA_ANC_PROCESSO_TIPIZZAZIONEDOC` | Gestisce tipizzazione manuale documenti allegati |

---

## Appendice B — Gruppi di sicurezza rilevanti per le integrazioni

| Nome Gruppo | UUID | Ruolo tecnico |
|---|---|---|
| BOA ANC Administrators | `_e-0000e9c7-d9c1-8000-9b81-01075c01075c_2236` | Amministratori WebApi e ConnectedSystem |
| BOA ANC All Users | `_e-0000e9c7-d9c1-8000-9b81-01075c01075c_2234` | Viewer WebApi; reader ConnectedSystem BPM |
| Operatore WebApi ANC | `_e-0000e9c7-d9c1-8000-9b81-01075c01075c_2240` | Caller autorizzato WebApi `BOA_ANC_WebApi_CreaPratica` |

---

## Appendice C — Variabili ambiente da configurare per deploy

Le seguenti costanti sono `isEnvironmentSpecific=true` e **non hanno valore nell'export**:

| Costante | Tipo | Impatto se non configurata |
|---|---|---|
| `BOA_ANC_RELATIVEPATH_SCODAMENTOSTATI` | string | L'integration item `BOA_ANC_Integrazione_BPM_InvioCallback` non sa dove chiamare → nessun callback verso BPM |
| `BOA_ANC_NUMBER_MAXRETRY` | int | Il batch non sa quando scartare un esito — loop infinito o fallimento |
| `BOA_ANC_NUMEROESITITOSEND` | int | Il batch non sa quanti esiti processare per run |
| `BOA_ANC_DEBUG_ATTIVABATCHCALLBACK` | boolean | Se non impostato a `true`, il batch non parte e nessun callback viene inviato |
| `BOA_ANC_DEBUG_MOCK_CALLBACK` | boolean | Se non impostato, comportamento undefined (rischio chiamata BPM reale in ambienti non-prod) |
| `BOA_ANC_DEBUG_ABILITASERVICENOW` | boolean | Feature flag ServiceNow — se non impostato, integrazione ServiceNow non attiva |
| `BOA_ANC_DEBUG_VISUALIZZASEGNALAZIONISINERGIA` | boolean | Feature flag Sinergia — se non impostato, colonna non visibile |
| `BOA_ANC_DEBUG_DEFAULTCODICEDOCID` | boolean | Se non impostato, il valore letto da JSON potrebbe non corrispondere a un tipo documento attivo |
