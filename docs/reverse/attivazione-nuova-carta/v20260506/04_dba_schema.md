---
app: "Attivazione Nuova Carta"
prefix: "BOA_ANC"
layer: "dba-schema"
export_version: "20260506"
analyzed: "2026-05-20"
output_dir: "docs/reverse/attivazione-nuova-carta/v20260506/"
---

# DBA Schema — Attivazione Nuova Carta

**Export version:** 20260506  
**Analysis date:** 2026-05-20  
**Source:** `appian-export/Attivazione Nuova Carta/`  
**Scope:** STEP 0–3 (Inventario, DataStore, Entity Model, CDT Field Mapping)

---

## STEP 0 — Ricognizione Risorse DBA

### Inventario Artefatti DBA

| Tipo Artefatto | Nome | File | Note |
|---|---|---|---|
| DataStore | BOA ANC DS | `dataStore/_a-..._9073630.xml` | JNDI: `jdbc/Appian`, autoUpdateSchema: false |
| DataStore | BOA ANC VIEW DS | `dataStore/_a-..._9079130.xml` | JNDI: `jdbc/Appian`, autoUpdateSchema: false |
| Entity CDT | BOA_ANC_Pratica | `datatype/{urn:...anc}BOA_ANC_Pratica.xsd` | Namespace: urn:com:appian:types:anc |
| Entity CDT | BOA_ANC_StatiPratica | `datatype/{urn:...anc}BOA_ANC_StatiPratica.xsd` | Namespace: urn:com:appian:types:anc |
| Entity CDT | BOA_ANC_Audit | `datatype/{urn:...anc}BOA_ANC_Audit.xsd` | Namespace: urn:com:appian:types:anc |
| Entity CDT | BOA_ANC_Cliente | `datatype/{urn:...anc}BOA_ANC_Cliente.xsd` | Namespace: urn:com:appian:types:anc |
| Entity CDT | BOA_ANC_DatiCarta | `datatype/{urn:...anc}BOA_ANC_DatiCarta.xsd` | Namespace: urn:com:appian:types:anc |
| Entity CDT | BOA_ANC_ContenutiDenuncia | `datatype/{urn:...anc}BOA_ANC_ContenutiDenuncia.xsd` | Namespace: urn:com:appian:types:anc |
| Entity CDT | BOA_ANC_FiltriUtente | `datatype/{urn:...anc}BOA_ANC_FiltriUtente.xsd` | Namespace: urn:com:appian:types:anc |
| Entity CDT | BOA_ANC_Documento | `datatype/{urn:...anc}BOA_ANC_Documento.xsd` | Namespace: urn:com:appian:types:anc |
| Entity CDT | BOA_ANC_TipiDocumento | `datatype/{urn:...anc}BOA_ANC_TipiDocumento.xsd` | Namespace: urn:com:appian:types:anc |
| Entity CDT | BOA_ANC_CaseNote | `datatype/{urn:...anc}BOA_ANC_CaseNote.xsd` | Namespace: urn:com:appian:types:anc |
| Entity CDT | BOA_ANC_GestioneScodamento | `datatype/{urn:...anc}BOA_ANC_GestioneScodamento.xsd` | Namespace: urn:com:appian:types:anc |
| Entity CDT | BOA_ANC_ScodamentoStati | `datatype/{urn:...anc}BOA_ANC_ScodamentoStati.xsd` | Namespace: urn:com:appian:types:anc |
| Entity CDT | BOA_ANC_Debug_WebServices | `datatype/{urn:...anc}BOA_ANC_Debug_WebServices.xsd` | Namespace: urn:com:appian:types:anc |
| Entity CDT | BOA_ANC_Debug_WsInput | `datatype/{urn:...anc}BOA_ANC_Debug_WsInput.xsd` | Namespace: urn:com:appian:types:anc |
| Entity CDT | BOA_ANC_CaseChecklist | `datatype/{urn:...anc}BOA_ANC_CaseChecklist.xsd` | Namespace: urn:com:appian:types:anc |
| Entity CDT | BOA_ANC_RefChecklist | `datatype/{urn:...anc}BOA_ANC_RefChecklist.xsd` | Namespace: urn:com:appian:types:anc |
| View CDT | BOA_ANC_V_Pratica | `datatype/{urn:...BOA_ANC}BOA_ANC_V_Pratica.xsd` | `@Table(name="v_boaancpratica")`, namespace: urn:com:appian:types:BOA_ANC |
| Non-entity CDT | BOA_ANC_Response_BPM_InvioCallback | `datatype/{urn:...anc}BOA_ANC_Response_BPM_InvioCallback.xsd` | Nessun @Id — non mappata su tabella |
| Query Rule | BOA_ANC_GetPraticaByIdCase | `content/_a-..._9073864.xml` | Tipo: READ |
| Query Rule | BOA_ANC_GetPraticaByIdWorkItem | `content/_a-..._9073876.xml` | Tipo: READ |
| Query Rule | BOA_ANC_VerificaPraticaDuplicataByIdWorkitem | `content/_a-..._9073998.xml` | Tipo: READ |
| Query Rule | BOA_ANC_Audit | `content/_a-..._9074650.xml` | Tipo: READ |
| Query Rule | BOA_ANC_GetClienteByIdCase | `content/_a-..._9074940.xml` | Tipo: READ |
| Query Rule | BOA_ANC_GetDatiPraticaByIdCase | `content/_a-..._9074993.xml` | Tipo: READ |
| Query Rule | BOA_ANC_GetContenutiDenunciaByIdCase | `content/_a-..._9075099.xml` | Tipo: READ |
| Query Rule | BOA_ANC_GetContenutoDenunciaById | `content/_a-..._9077278.xml` | Tipo: READ |
| Query Rule | BOA_ANC_ElaborazioneJsonCreaPratica_Pratica | `content/_a-..._9074417.xml` | Tipo: CREATE/UPDATE |
| Query Rule | BOA_ANC_ElaborazioneJsonCreaPratica_Cliente | `content/_a-..._9074822.xml` | Tipo: CREATE/UPDATE |
| Query Rule | BOA_ANC_ElaborazioneJsonCreaPratica_DatiCarta | `content/_a-..._9074980.xml` | Tipo: CREATE/UPDATE |
| Query Rule | BOA_ANC_ElaborazioneJsonCreaPratica_ContenutiDenuncia | `content/_a-..._9076710.xml` | Tipo: CREATE/UPDATE |
| Query Rule | BOA_ANC_ElaborazioneJsonCreaPratica_Documento | `content/_a-..._9106875.xml` | Tipo: CREATE/UPDATE |
| Query Rule | BOA_ANC_StatiPratica | `content/_a-..._9091416.xml` | Tipo: READ |
| Query Rule | BOA_ANC_GetFiltriUtenteByUser | `content/_a-..._9098295.xml` | Tipo: READ |
| Query Rule | BOA_ANC_GetDocumentoByIdCase | `content/_a-..._9106028.xml` | Tipo: READ |
| Query Rule | BOA_ANC_GetTipoDocumentoAttivoByCodiceDocumento | `content/_a-..._9106957.xml` | Tipo: READ |
| Query Rule | BOA_ANC_GetNotaByIdCase | `content/_a-..._9107932.xml` | Tipo: READ |
| Query Rule | BOA_ANC_GetGestioneScodamento | `content/_a-..._9108964.xml` | Tipo: READ |
| Query Rule | BOA_ANC_GetEsitiToSend | `content/_a-..._9109188.xml` | Tipo: READ |
| Query Rule | BOA_ANC_GetCaseCheckListVisibileByIdCase | `content/_a-..._9112079.xml` | Tipo: READ |
| Query Rule | BOA_ANC_GetCaseCheckListByIdCase | `content/_a-..._9118699.xml` | Tipo: READ |
| Query Rule | BOA_ANC_GetTipiDocumentoAttivi | `content/_a-..._10799202.xml` | Tipo: READ |
| Query Rule | BOA_ANC_GetNumeroPraticheDashBoard | `content/_a-..._9097173.xml` | Tipo: READ |
| Query Rule | BOA_ANC_GetLastIdFilter | `content/_a-..._9099239.xml` | Tipo: READ |
| Query Rule | BOA_ANC_GetPraticheGiornaliere | `content/_a-..._9113023.xml` | Tipo: READ |
| Query Rule | BOA_ANC_GetPraticheGiornaliereLavorate | `content/_a-..._9113047.xml` | Tipo: READ |
| Query Rule | BOA_ANC_GetPraticheByStato | `content/_a-..._9113158.xml` | Tipo: READ |
| Query Rule | BOA_ANC_GetAttivita | `content/_a-..._9115051.xml` | Tipo: READ |

---

## STEP 1 — Datastore Configuration

### Tabella DataStore

| Nome DataStore | JNDI | autoUpdateSchema | Entity Count | Autori (gruppi) | Lettori (gruppi) |
|---|---|---|---|---|---|
| BOA ANC DS | jdbc/Appian | false | 16 | BOA ANC All Users | — |
| BOA ANC VIEW DS | jdbc/Appian | false | 1 | — | BOA ANC All Users |

> **Nota:** `autoUpdateSchema=false` su entrambi i datastore — il DDL fisico è gestito esternamente dal DBA. Lo schema dedotto negli step successivi è una ricostruzione logica basata sulle JPA annotations.

### Lista Entity per DataStore

#### BOA ANC DS

| Entity Name | CDT Type | UUID Entity | Note |
|---|---|---|---|
| BOA_ANC_Debug_WsInput | `{urn:com:appian:types:anc}BOA_ANC_Debug_WsInput` | ee938082-2ab2-443f-8921-eeed1151d8c6 | Tabella debug WebApi input |
| BOA_ANC_StatiPratica | `{urn:com:appian:types:anc}BOA_ANC_StatiPratica` | 19cff150-2fd5-4ea5-a58e-7478688c7004 | Storico stati della pratica |
| BOA_ANC_Pratica | `{urn:com:appian:types:anc}BOA_ANC_Pratica` | c3c8a0a3-2321-4a75-a8a4-9028b88f4c22 | Entità principale |
| BOA_ANC_Audit | `{urn:com:appian:types:anc}BOA_ANC_Audit` | fd9531c7-d3c7-4d0d-bedd-c51323ff0c78 | Log audit dei case |
| BOA_ANC_Cliente | `{urn:com:appian:types:anc}BOA_ANC_Cliente` | 56505ccd-f1fe-442a-ad31-2f2471ba60b8 | Dati anagrafici cliente |
| BOA_ANC_DatiCarta | `{urn:com:appian:types:anc}BOA_ANC_DatiCarta` | 38451128-b89f-49eb-b4ac-598b100d0b06 | Dati della carta di credito/debito |
| BOA_ANC_ContenutiDenuncia | `{urn:com:appian:types:anc}BOA_ANC_ContenutiDenuncia` | 90cf3128-7242-44ba-9a56-c7b281522cdc | Allegati denuncia (documenti) |
| BOA_ANC_FiltriUtente | `{urn:com:appian:types:anc}BOA_ANC_FiltriUtente` | 74777b61-5a97-4122-a145-87b74fd5142c | Preferenze filtro per utente |
| BOA_ANC_Documento | `{urn:com:appian:types:anc}BOA_ANC_Documento` | 7a1fb2d1-2a24-4cfe-99f1-42e90a203985 | Documenti caricati sul case |
| BOA_ANC_TipiDocumento | `{urn:com:appian:types:anc}BOA_ANC_TipiDocumento` | 96014187-4394-490c-9b32-065ef70ecedd | Anagrafica tipologie documento |
| BOA_ANC_CaseNote | `{urn:com:appian:types:anc}BOA_ANC_CaseNote` | 584e9927-5def-4e91-95ea-56fb1e619f74 | Note di chiusura pratica |
| BOA_ANC_GestioneScodamento | `{urn:com:appian:types:anc}BOA_ANC_GestioneScodamento` | 7ee6d0c5-ac76-48d9-bd3a-e226b26b3d90 | Stato esecuzione batch scodamento |
| BOA_ANC_ScodamentoStati | `{urn:com:appian:types:anc}BOA_ANC_ScodamentoStati` | ceb07ccd-f092-47c5-a57d-d130177ba3d8 | Code esiti da inviare al BPM |
| BOA_ANC_Debug_WebServices | `{urn:com:appian:types:anc}BOA_ANC_Debug_WebServices` | 47b26444-b380-4c24-8a6e-ab3ed7afa54b | Tabella debug chiamate WS |
| BOA_ANC_CaseChecklist | `{urn:com:appian:types:anc}BOA_ANC_CaseChecklist` | f5db99bb-c789-48da-b8c9-9d5a7e5d2d45 | Risposte checklist per case |
| BOA_ANC_RefChecklist | `{urn:com:appian:types:anc}BOA_ANC_RefChecklist` | 8219553e-dd06-42eb-a13a-f4d9fcccea56 | Anagrafica voci checklist |

#### BOA ANC VIEW DS

| Entity Name | CDT Type | UUID Entity | Note |
|---|---|---|---|
| BOA_ANC_V_Pratica | `{urn:com:appian:types:BOA_ANC}BOA_ANC_V_Pratica` | 4b431ba2-6244-4087-8626-fea4798a1b96 | View SQL read-only (`v_boaancpratica`) |

---

## STEP 2 — Entity Model

### Tabella Entity Model

| Entity Name | CDT | Namespace | Nome Tabella SQL | Tipo | DataStore |
|---|---|---|---|---|---|
| BOA_ANC_Pratica | BOA_ANC_Pratica | urn:com:appian:types:anc | BOA_ANC_Pratica | Table | BOA ANC DS |
| BOA_ANC_StatiPratica | BOA_ANC_StatiPratica | urn:com:appian:types:anc | BOA_ANC_StatiPratica | Table | BOA ANC DS |
| BOA_ANC_Audit | BOA_ANC_Audit | urn:com:appian:types:anc | BOA_ANC_Audit | Table | BOA ANC DS |
| BOA_ANC_Cliente | BOA_ANC_Cliente | urn:com:appian:types:anc | BOA_ANC_Cliente | Table | BOA ANC DS |
| BOA_ANC_DatiCarta | BOA_ANC_DatiCarta | urn:com:appian:types:anc | BOA_ANC_DatiCarta | Table | BOA ANC DS |
| BOA_ANC_ContenutiDenuncia | BOA_ANC_ContenutiDenuncia | urn:com:appian:types:anc | BOA_ANC_ContenutiDenuncia | Table | BOA ANC DS |
| BOA_ANC_FiltriUtente | BOA_ANC_FiltriUtente | urn:com:appian:types:anc | BOA_ANC_FiltriUtente | Table | BOA ANC DS |
| BOA_ANC_Documento | BOA_ANC_Documento | urn:com:appian:types:anc | BOA_ANC_Documento | Table | BOA ANC DS |
| BOA_ANC_TipiDocumento | BOA_ANC_TipiDocumento | urn:com:appian:types:anc | BOA_ANC_TipiDocumento | Table | BOA ANC DS |
| BOA_ANC_CaseNote | BOA_ANC_CaseNote | urn:com:appian:types:anc | BOA_ANC_CaseNote | Table | BOA ANC DS |
| BOA_ANC_GestioneScodamento | BOA_ANC_GestioneScodamento | urn:com:appian:types:anc | BOA_ANC_GestioneScodamento | Table | BOA ANC DS |
| BOA_ANC_ScodamentoStati | BOA_ANC_ScodamentoStati | urn:com:appian:types:anc | BOA_ANC_ScodamentoStati | Table | BOA ANC DS |
| BOA_ANC_Debug_WebServices | BOA_ANC_Debug_WebServices | urn:com:appian:types:anc | BOA_ANC_Debug_WebServices | Table | BOA ANC DS |
| BOA_ANC_Debug_WsInput | BOA_ANC_Debug_WsInput | urn:com:appian:types:anc | BOA_ANC_Debug_WsInput | Table | BOA ANC DS |
| BOA_ANC_CaseChecklist | BOA_ANC_CaseChecklist | urn:com:appian:types:anc | BOA_ANC_CaseChecklist | Table | BOA ANC DS |
| BOA_ANC_RefChecklist | BOA_ANC_RefChecklist | urn:com:appian:types:anc | BOA_ANC_RefChecklist | Table | BOA ANC DS |
| BOA_ANC_V_Pratica | BOA_ANC_V_Pratica | urn:com:appian:types:BOA_ANC | v_boaancpratica | View | BOA ANC VIEW DS |

> **Nota nomi tabella impliciti:** nessuno dei CDT entity in BOA ANC DS espone `@Table(name=...)` a livello di `xsd:complexType`. Appian usa il nome del CDT come nome tabella. Il case-sensitivity dipende dal DBMS target (MySQL: case-insensitive; PostgreSQL: case-sensitive con quoting).

---

## STEP 3 — CDT Field Mapping

### CDT: BOA_ANC_Pratica
**Tabella:** BOA_ANC_Pratica  
**DataStore:** BOA ANC DS

| Campo CDT | Colonna SQL | Tipo SQL | PK | Nullable | Constraint / Annotation |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | `@Id @GeneratedValue` |
| idCase | idCase | INT | | YES | — |
| dataApertura | dataApertura | DATETIME | | YES | — |
| dataUltimaModifica | dataUltimaModifica | DATETIME | | YES | — |
| operatoreUltimaModifica | operatoreUltimaModifica | VARCHAR(255) | | YES | `@Column(length=255)` |
| stato | stato | VARCHAR(255) | | YES | `@Column(length=255)` |
| dataChiusura | dataChiusura | DATETIME | | YES | — |
| canale | canale | VARCHAR(255) | | YES | `@Column(length=255)` |
| idWorkitem | idWorkitem | VARCHAR(255) | | YES | `@Column(length=255)` |
| numPratica | numPratica | VARCHAR(255) | | YES | `@Column(length=255)` |
| cfCliente | cfCliente | VARCHAR(255) | | YES | `@Column(length=255)` |
| codiceCliente | codiceCliente | VARCHAR(255) | | YES | `@Column(length=255)` |
| dataInserimentoRichiesta | dataInserimentoRichiesta | DATETIME | | YES | — |
| folder | folder | INT | | YES | — |
| dataScadenza | dataScadenza | DATETIME | | YES | — |
| esitoSD | esitoSD | VARCHAR(255) | | YES | `@Column(length=255)` |
| dataEsitoSD | dataEsitoSD | DATETIME | | YES | — |

---

### CDT: BOA_ANC_StatiPratica
**Tabella:** BOA_ANC_StatiPratica  
**DataStore:** BOA ANC DS

| Campo CDT | Colonna SQL | Tipo SQL | PK | Nullable | Constraint / Annotation |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | `@Id @GeneratedValue` |
| idCase | idCase | INT | | YES | — |
| stato | stato | VARCHAR(255) | | YES | `@Column(length=255)` |
| dataInizio | dataInizio | DATETIME | | YES | — |
| operatore | operatore | VARCHAR(255) | | YES | `@Column(length=255)` |

---

### CDT: BOA_ANC_Audit
**Tabella:** BOA_ANC_Audit  
**DataStore:** BOA ANC DS

| Campo CDT | Colonna SQL | Tipo SQL | PK | Nullable | Constraint / Annotation |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | `@Id @GeneratedValue` |
| timestamp | timestamp | DATETIME | | YES | — |
| user | user | VARCHAR(255) | | YES | `@Column(length=255)` |
| type | type | VARCHAR(255) | | YES | `@Column(length=255)` |
| description | description | VARCHAR(5000) | | YES | `@Column(length=5000)` |
| recordId | recordId | INT | | YES | — |

---

### CDT: BOA_ANC_Cliente
**Tabella:** BOA_ANC_Cliente  
**DataStore:** BOA ANC DS

| Campo CDT | Colonna SQL | Tipo SQL | PK | Nullable | Constraint / Annotation |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | `@Id @GeneratedValue` |
| idCase | idCase | INT | | YES | — |
| nome | nome | VARCHAR(255) | | YES | `@Column(length=255)` |
| cognome | cognome | VARCHAR(255) | | YES | `@Column(length=255)` |
| codiceFiscale | codiceFiscale | VARCHAR(255) | | YES | `@Column(length=255)` |
| sesso | sesso | VARCHAR(255) | | YES | `@Column(length=255)` |
| dataNascita | dataNascita | DATE | | YES | — |
| comuneNascita | comuneNascita | VARCHAR(255) | | YES | `@Column(length=255)` |
| provinciaNascita | provinciaNascita | VARCHAR(255) | | YES | `@Column(length=255)` |
| nazioneNascita | nazioneNascita | VARCHAR(255) | | YES | `@Column(length=255)` |
| cittadinanza | cittadinanza | VARCHAR(255) | | YES | `@Column(length=255)` |
| cellulare | cellulare | VARCHAR(255) | | YES | `@Column(length=255)` |
| telefono | telefono | VARCHAR(255) | | YES | `@Column(length=255)` |
| indirizzoResidenza | indirizzoResidenza | VARCHAR(500) | | YES | `@Column(length=500)` |
| luogoResidenza | luogoResidenza | VARCHAR(255) | | YES | `@Column(length=255)` |
| comuneResidenza | comuneResidenza | VARCHAR(255) | | YES | `@Column(length=255)` |
| provinciaResidenza | provinciaResidenza | VARCHAR(255) | | YES | `@Column(length=255)` |
| nazioneResidenza | nazioneResidenza | VARCHAR(255) | | YES | `@Column(length=255)` |
| cap | cap | VARCHAR(255) | | YES | `@Column(length=255)` |
| civico | civico | VARCHAR(255) | | YES | `@Column(length=255)` |

---

### CDT: BOA_ANC_DatiCarta
**Tabella:** BOA_ANC_DatiCarta  
**DataStore:** BOA ANC DS

| Campo CDT | Colonna SQL | Tipo SQL | PK | Nullable | Constraint / Annotation |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | `@Id @GeneratedValue` |
| idCase | idCase | INT | | YES | — |
| numeroCarta | numeroCarta | VARCHAR(255) | | YES | `@Column(length=255)` |
| tipoCarta | tipoCarta | VARCHAR(255) | | YES | `@Column(length=255)` |
| intestazioneCarta | intestazioneCarta | VARCHAR(255) | | YES | `@Column(length=255)` |

---

### CDT: BOA_ANC_ContenutiDenuncia
**Tabella:** BOA_ANC_ContenutiDenuncia  
**DataStore:** BOA ANC DS

| Campo CDT | Colonna SQL | Tipo SQL | PK | Nullable | Constraint / Annotation |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | `@Id @GeneratedValue` |
| idCase | idCase | INT | | YES | — |
| nomeFile | nomeFile | VARCHAR(255) | | YES | `@Column(length=255)` |
| estensione | estensione | VARCHAR(255) | | YES | `@Column(length=255)` |
| idDoc | idDoc | VARCHAR(255) | | YES | `@Column(length=255)` |
| linkDownload | linkDownload | VARCHAR(2500) | | YES | `@Column(length=2500)` |
| idDocAppian | idDocAppian | INT | | YES | — |

---

### CDT: BOA_ANC_FiltriUtente
**Tabella:** BOA_ANC_FiltriUtente  
**DataStore:** BOA ANC DS

| Campo CDT | Colonna SQL | Tipo SQL | PK | Nullable | Constraint / Annotation |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | `@Id @GeneratedValue` |
| user | user | VARCHAR(255) | | YES | `@Column(length=255)` |
| statoAttivita | statoAttivita | VARCHAR(255) | | YES | `@Column(length=255)` |
| tipoProcesso | tipoProcesso | VARCHAR(255) | | YES | `@Column(length=255)` |
| codicePratica | codicePratica | INT | | YES | — |
| nomeAttivita | nomeAttivita | VARCHAR(255) | | YES | `@Column(length=255)` |
| dataScadenzaDA | dataScadenzaDA | DATE | | YES | — |
| dataScadenzaA | dataScadenzaA | DATE | | YES | — |
| assegnatario | assegnatario | VARCHAR(255) | | YES | `@Column(length=255)` |
| assegnatarioString | assegnatarioString | VARCHAR(255) | | YES | `@Column(length=255)` |
| owner | owner | VARCHAR(255) | | YES | `@Column(length=255)` |
| userFiltro | userFiltro | VARCHAR(255) | | YES | `@Column(length=255)` |

---

### CDT: BOA_ANC_Documento
**Tabella:** BOA_ANC_Documento  
**DataStore:** BOA ANC DS

| Campo CDT | Colonna SQL | Tipo SQL | PK | Nullable | Constraint / Annotation |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | `@Id @GeneratedValue` |
| idCase | idCase | INT | | YES | — |
| codiceDocId | codiceDocId | INT | | YES | — |
| descrizione | descrizione | VARCHAR(255) | | YES | `@Column(length=255)` |

---

### CDT: BOA_ANC_TipiDocumento
**Tabella:** BOA_ANC_TipiDocumento  
**DataStore:** BOA ANC DS

| Campo CDT | Colonna SQL | Tipo SQL | PK | Nullable | Constraint / Annotation |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | `@Id @GeneratedValue` |
| codiceDocId | codiceDocId | INT | | YES | — |
| descrizione | descrizione | VARCHAR(255) | | YES | `@Column(length=255)` |
| categoria | categoria | VARCHAR(255) | | YES | `@Column(length=255)` |
| attivo | attivo | BOOLEAN | | YES | — |

---

### CDT: BOA_ANC_CaseNote
**Tabella:** BOA_ANC_CaseNote  
**DataStore:** BOA ANC DS

| Campo CDT | Colonna SQL | Tipo SQL | PK | Nullable | Constraint / Annotation |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | `@Id @GeneratedValue` |
| idCase | idCase | INT | | YES | — |
| testoNota | testoNota | VARCHAR(5000) | | YES | `@Column(length=5000)` |
| idStato | idStato | INT | | YES | — |
| operatore | operatore | VARCHAR(255) | | YES | `@Column(length=255)` |
| datacreazione | datacreazione | DATETIME | | YES | — |

---

### CDT: BOA_ANC_GestioneScodamento
**Tabella:** BOA_ANC_GestioneScodamento  
**DataStore:** BOA ANC DS

| Campo CDT | Colonna SQL | Tipo SQL | PK | Nullable | Constraint / Annotation |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | `@Id @GeneratedValue` |
| inEsecuzione | inEsecuzione | BOOLEAN | | YES | — |
| dataInizio | dataInizio | DATETIME | | YES | — |
| dataFine | dataFine | DATETIME | | YES | — |
| numEsitiToSend | numEsitiToSend | INT | | YES | — |

---

### CDT: BOA_ANC_ScodamentoStati
**Tabella:** BOA_ANC_ScodamentoStati  
**DataStore:** BOA ANC DS

| Campo CDT | Colonna SQL | Tipo SQL | PK | Nullable | Constraint / Annotation |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | `@Id @GeneratedValue` |
| idcase | idcase | INT | | YES | — |
| esito | esito | INT | | YES | — |
| errore | errore | VARCHAR(255) | | YES | `@Column(length=255)` |
| timestamp | timestamp | DATETIME | | YES | — |
| retry | retry | INT | | YES | — |

---

### CDT: BOA_ANC_Debug_WebServices
**Tabella:** BOA_ANC_Debug_WebServices  
**DataStore:** BOA ANC DS

| Campo CDT | Colonna SQL | Tipo SQL | PK | Nullable | Constraint / Annotation |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | `@Id @GeneratedValue` |
| idCase | idCase | INT | | YES | — |
| servizio | servizio | VARCHAR(255) | | YES | `@Column(length=255)` |
| parametri | parametri | VARCHAR(255) | | YES | `@Column(length=255)` |
| request | request | LONGTEXT | | YES | `@Column(columndefinition="LONGTEXT")` |
| response | response | LONGTEXT | | YES | `@Column(columndefinition="LONGTEXT")` |
| timestampChiamata | timestampChiamata | DATETIME | | YES | — |

---

### CDT: BOA_ANC_Debug_WsInput
**Tabella:** BOA_ANC_Debug_WsInput  
**DataStore:** BOA ANC DS

| Campo CDT | Colonna SQL | Tipo SQL | PK | Nullable | Constraint / Annotation |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | `@Id @GeneratedValue` |
| jsonbody | jsonbody | LONGTEXT | | YES | `@Column(columndefinition="LONGTEXT")` |
| sdResponse | sdResponse | LONGTEXT | | YES | `@Column(columndefinition="LONGTEXT")` |
| timestampInput | timestampInput | DATETIME | | YES | — |
| timestampOutput | timestampOutput | DATETIME | | YES | — |
| tipologia | tipologia | VARCHAR(255) | | YES | `@Column(length=255)` |
| idWorkItem | idWorkItem | VARCHAR(255) | | YES | `@Column(length=255)` |
| idCase | idCase | INT | | YES | — |

---

### CDT: BOA_ANC_CaseChecklist
**Tabella:** BOA_ANC_CaseChecklist  
**DataStore:** BOA ANC DS

| Campo CDT | Colonna SQL | Tipo SQL | PK | Nullable | Constraint / Annotation |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | `@Id @GeneratedValue` |
| idRefCheckList | idRefCheckList | INT | | YES | — |
| idCase | idCase | INT | | YES | — |
| categoria | categoria | VARCHAR(255) | | YES | `@Column(length=255)` |
| ordinamento | ordinamento | INT | | YES | — |
| descrizione | descrizione | VARCHAR(255) | | YES | `@Column(length=255)` |
| note | note | VARCHAR(255) | | YES | `@Column(length=255)` |
| timestamp | timestamp | DATETIME | | YES | — |
| flagPresenza | flagPresenza | BOOLEAN | | YES | — |
| flagConformita | flagConformita | BOOLEAN | | YES | — |
| flagRichiesto | flagRichiesto | BOOLEAN | | YES | — |
| flagObbligatorio | flagObbligatorio | BOOLEAN | | YES | — |
| flagBloccante | flagBloccante | BOOLEAN | | YES | — |
| idDipendenza | idDipendenza | INT | | YES | — |
| info | info | VARCHAR(2048) | | YES | `@Column(length=2048)` |
| utente | utente | VARCHAR(255) | | YES | `@Column(length=255)` |
| codice | codice | VARCHAR(255) | | YES | `@Column(length=255)` |
| descrizioneCodice | descrizioneCodice | VARCHAR(255) | | YES | `@Column(length=255)` |
| visibile | visibile | BOOLEAN | | YES | — |

---

### CDT: BOA_ANC_RefChecklist
**Tabella:** BOA_ANC_RefChecklist  
**DataStore:** BOA ANC DS

> Questo CDT usa `@Column(columnDefinition=...)` con nomi colonna espliciti per diversi campi.

| Campo CDT | Colonna SQL | Tipo SQL | PK | Nullable | Constraint / Annotation |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | `@Id @GeneratedValue @Column(nullable=false, unique=true, columnDefinition="INT")` |
| idRef | idRef | INT | | YES | `@Column(columnDefinition="INT")` |
| categoria | categoria | VARCHAR(255) | | YES | `@Column(columnDefinition="VARCHAR(255)")` |
| descrizione | descrizione | VARCHAR(255) | | YES | `@Column(columnDefinition="VARCHAR(255)")` |
| attivo | attivo | BIT | | YES | `@Column(columnDefinition="BIT")` |
| validitaDal | validitaDal | DATE | | YES | `@Column(columnDefinition="DATE")` |
| validitaAl | validitaAl | DATE | | YES | `@Column(columnDefinition="DATE")` |
| obbligatorio | obbligatorio | BIT | | YES | `@Column(columnDefinition="BIT")` |
| locked | locked | BIT | | YES | `@Column(columnDefinition="BIT")` |
| ordinamento | ordinamento | INT | | YES | `@Column(columnDefinition="INT")` |
| idDipendenza | iddipendenza | INT | | YES | `@Column(name="iddipendenza", columnDefinition="INT")` |
| info | info | VARCHAR(2048) | | YES | `@Column(name="info", columnDefinition="VARCHAR(2048)")` |
| codice | codice | VARCHAR(255) | | YES | `@Column(name="codice", columnDefinition="VARCHAR(255)")` |
| descrizioneCodice | descrizionecodice | VARCHAR(255) | | YES | `@Column(name="descrizionecodice", columnDefinition="VARCHAR(255)")` |

---

### CDT: BOA_ANC_V_Pratica
**Tabella (View):** v_boaancpratica  
**DataStore:** BOA ANC VIEW DS  
**Tipo:** View SQL read-only — schema esterno, DDL non gestito da Appian

> Tutti i campi espongono `@Column(name=..., columnDefinition=...)` espliciti — convenzione lowercase per i nomi colonna della view.

| Campo CDT | Colonna SQL | Tipo SQL | PK | Nullable | Constraint / Annotation |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | `@Id @Column(name="id", nullable=false, columnDefinition="INT")` |
| idCase | idcase | INT | | YES | `@Column(name="idcase", columnDefinition="INT")` |
| dataApertura | dataapertura | DATETIME | | YES | `@Column(name="dataapertura", columnDefinition="DATETIME")` |
| dataUltimaModifica | dataultimamodifica | DATETIME | | YES | `@Column(name="dataultimamodifica", columnDefinition="DATETIME")` |
| operatoreUltimaModifica | operatoreultimamodifica | VARCHAR(255) | | YES | `@Column(name="operatoreultimamodifica", columnDefinition="VARCHAR(255)")` |
| stato | stato | VARCHAR(255) | | YES | `@Column(name="stato", columnDefinition="VARCHAR(255)")` |
| dataChiusura | datachiusura | DATETIME | | YES | `@Column(name="datachiusura", columnDefinition="DATETIME")` |
| canale | canale | VARCHAR(255) | | YES | `@Column(name="canale", columnDefinition="VARCHAR(255)")` |
| idWorkitem | idworkitem | VARCHAR(255) | | YES | `@Column(name="idworkitem", columnDefinition="VARCHAR(255)")` |
| numPratica | numPratica | VARCHAR(255) | | YES | `@Column(length=255)` (nome implicito) |
| cfCliente | cfcliente | VARCHAR(255) | | YES | `@Column(name="cfcliente", columnDefinition="VARCHAR(255)")` |
| codiceCliente | codicecliente | VARCHAR(255) | | YES | `@Column(name="codicecliente", columnDefinition="VARCHAR(255)")` |
| dataInserimentoRichiesta | datainserimentorichiesta | DATETIME | | YES | `@Column(name="datainserimentorichiesta", columnDefinition="DATETIME")` |
| folder | folder | INT | | YES | `@Column(name="folder", columnDefinition="INT")` |
| dataScadenza | datascadenza | DATETIME | | YES | `@Column(name="datascadenza", columnDefinition="DATETIME")` |
| esitoSD | esitoSD | VARCHAR(255) | | YES | `@Column(length=255)` (nome implicito) |
| dataEsitoSD | dataEsitoSD | DATETIME | | YES | — (nome implicito) |
| segnalazioni | Segnalazioni | INT | | NO | `@Column(name="Segnalazioni", nullable=false, columnDefinition="INT")` |
| ticketSN | ticketSN | INT | | NO | `@Column(name="ticketSN", nullable=false, columnDefinition="INT")` |

---

*Fine PASSO A — STEP 0/1/2/3. Continuare con PASSO B per STEP 4 (DDL), STEP 5 (Query Rules), STEP 6 (CRUD Map), STEP 7 (ER Map).*
