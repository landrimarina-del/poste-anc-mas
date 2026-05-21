---
app: "Attivazione Nuova Carta"
prefix: "BOA_ANC"
layer: "dba"
export_version: "20260506"
analyzed: "2026-05-20"
output_dir: "docs/reverse/attivazione-nuova-carta/v20260506/"
---

# DBA Schema — Attivazione Nuova Carta

> Evidence-only. Estratto da `datatype/*.xsd`, `dataStore/*.xml`, `content/*.xml`, `processModel/*.xml`.  
> Fonti: export Appian versione 20260506 · analisi: 2026-05-20.

---

## STEP 0 — Entity Catalogue

| Entity | Tabella SQL | Tipo | DataStore |
|---|---|---|---|
| BOA_ANC_Pratica | BOA_ANC_Pratica | table | BOA ANC DS |
| BOA_ANC_StatiPratica | BOA_ANC_StatiPratica | table | BOA ANC DS |
| BOA_ANC_Cliente | BOA_ANC_Cliente | table | BOA ANC DS |
| BOA_ANC_DatiCarta | BOA_ANC_DatiCarta | table | BOA ANC DS |
| BOA_ANC_ContenutiDenuncia | BOA_ANC_ContenutiDenuncia | table | BOA ANC DS |
| BOA_ANC_Documento | BOA_ANC_Documento | table | BOA ANC DS |
| BOA_ANC_Audit | BOA_ANC_Audit | table | BOA ANC DS |
| BOA_ANC_CaseNote | BOA_ANC_CaseNote | table | BOA ANC DS |
| BOA_ANC_FiltriUtente | BOA_ANC_FiltriUtente | table | BOA ANC DS |
| BOA_ANC_GestioneScodamento | BOA_ANC_GestioneScodamento | table | BOA ANC DS |
| BOA_ANC_ScodamentoStati | BOA_ANC_ScodamentoStati | table | BOA ANC DS |
| BOA_ANC_CaseChecklist | BOA_ANC_CaseChecklist | table | BOA ANC DS |
| BOA_ANC_RefChecklist | BOA_ANC_RefChecklist | table | BOA ANC DS |
| BOA_ANC_TipiDocumento | BOA_ANC_TipiDocumento | table | BOA ANC DS |
| BOA_ANC_Debug_WsInput | BOA_ANC_Debug_WsInput | table | BOA ANC DS |
| BOA_ANC_Debug_WebServices | BOA_ANC_Debug_WebServices | table | BOA ANC DS |
| BOA_ANC_V_Pratica | v_boaancpratica | view | BOA ANC VIEW DS |

---

## STEP 1 — Schema per Entity

### BOA_ANC_Pratica  →  tabella: BOA_ANC_Pratica

| Campo | Colonna SQL | Tipo SQL | PK | Nullable | FK hint |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | — |
| idCase | idCase | INT | | YES | Appian Case ID |
| dataApertura | dataApertura | DATETIME | | YES | — |
| dataUltimaModifica | dataUltimaModifica | DATETIME | | YES | — |
| operatoreUltimaModifica | operatoreUltimaModifica | VARCHAR(255) | | YES | — |
| stato | stato | VARCHAR(255) | | YES | — |
| dataChiusura | dataChiusura | DATETIME | | YES | — |
| canale | canale | VARCHAR(255) | | YES | — |
| idWorkitem | idWorkitem | VARCHAR(255) | | YES | — |
| numPratica | numPratica | VARCHAR(255) | | YES | — |
| cfCliente | cfCliente | VARCHAR(255) | | YES | — |
| codiceCliente | codiceCliente | VARCHAR(255) | | YES | — |
| dataInserimentoRichiesta | dataInserimentoRichiesta | DATETIME | | YES | — |
| folder | folder | INT | | YES | — |
| dataScadenza | dataScadenza | DATETIME | | YES | — |
| esitoSD | esitoSD | VARCHAR(255) | | YES | — |
| dataEsitoSD | dataEsitoSD | DATETIME | | YES | — |

---

### BOA_ANC_StatiPratica  →  tabella: BOA_ANC_StatiPratica

| Campo | Colonna SQL | Tipo SQL | PK | Nullable | FK hint |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | — |
| idCase | idCase | INT | | YES | → BOA_ANC_Pratica.idCase |
| stato | stato | VARCHAR(255) | | YES | — |
| dataInizio | dataInizio | DATETIME | | YES | — |
| operatore | operatore | VARCHAR(255) | | YES | — |

---

### BOA_ANC_Cliente  →  tabella: BOA_ANC_Cliente

| Campo | Colonna SQL | Tipo SQL | PK | Nullable | FK hint |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | — |
| idCase | idCase | INT | | YES | → BOA_ANC_Pratica.idCase |
| nome | nome | VARCHAR(255) | | YES | — |
| cognome | cognome | VARCHAR(255) | | YES | — |
| codiceFiscale | codiceFiscale | VARCHAR(255) | | YES | — |
| sesso | sesso | VARCHAR(255) | | YES | — |
| dataNascita | dataNascita | DATE | | YES | — |
| comuneNascita | comuneNascita | VARCHAR(255) | | YES | — |
| provinciaNascita | provinciaNascita | VARCHAR(255) | | YES | — |
| nazioneNascita | nazioneNascita | VARCHAR(255) | | YES | — |
| cittadinanza | cittadinanza | VARCHAR(255) | | YES | — |
| cellulare | cellulare | VARCHAR(255) | | YES | — |
| telefono | telefono | VARCHAR(255) | | YES | — |
| indirizzoResidenza | indirizzoResidenza | VARCHAR(500) | | YES | — |
| luogoResidenza | luogoResidenza | VARCHAR(255) | | YES | — |
| comuneResidenza | comuneResidenza | VARCHAR(255) | | YES | — |
| provinciaResidenza | provinciaResidenza | VARCHAR(255) | | YES | — |
| nazioneResidenza | nazioneResidenza | VARCHAR(255) | | YES | — |
| cap | cap | VARCHAR(255) | | YES | — |
| civico | civico | VARCHAR(255) | | YES | — |

---

### BOA_ANC_DatiCarta  →  tabella: BOA_ANC_DatiCarta

| Campo | Colonna SQL | Tipo SQL | PK | Nullable | FK hint |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | — |
| idCase | idCase | INT | | YES | → BOA_ANC_Pratica.idCase |
| numeroCarta | numeroCarta | VARCHAR(255) | | YES | — |
| tipoCarta | tipoCarta | VARCHAR(255) | | YES | — |
| intestazioneCarta | intestazioneCarta | VARCHAR(255) | | YES | — |

---

### BOA_ANC_ContenutiDenuncia  →  tabella: BOA_ANC_ContenutiDenuncia

| Campo | Colonna SQL | Tipo SQL | PK | Nullable | FK hint |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | — |
| idCase | idCase | INT | | YES | → BOA_ANC_Pratica.idCase |
| nomeFile | nomeFile | VARCHAR(255) | | YES | — |
| estensione | estensione | VARCHAR(255) | | YES | — |
| idDoc | idDoc | VARCHAR(255) | | YES | — |
| linkDownload | linkDownload | VARCHAR(2500) | | YES | — |
| idDocAppian | idDocAppian | INT | | YES | — |

---

### BOA_ANC_Documento  →  tabella: BOA_ANC_Documento

| Campo | Colonna SQL | Tipo SQL | PK | Nullable | FK hint |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | — |
| idCase | idCase | INT | | YES | → BOA_ANC_Pratica.idCase |
| codiceDocId | codiceDocId | INT | | YES | → BOA_ANC_TipiDocumento.codiceDocId |
| descrizione | descrizione | VARCHAR(255) | | YES | — |

---

### BOA_ANC_Audit  →  tabella: BOA_ANC_Audit

| Campo | Colonna SQL | Tipo SQL | PK | Nullable | FK hint |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | — |
| timestamp | timestamp | DATETIME | | YES | — |
| user | user | VARCHAR(255) | | YES | — |
| type | type | VARCHAR(255) | | YES | — |
| description | description | VARCHAR(5000) | | YES | — |
| recordId | recordId | INT | | YES | → BOA_ANC_Pratica.idCase |

---

### BOA_ANC_CaseNote  →  tabella: BOA_ANC_CaseNote

| Campo | Colonna SQL | Tipo SQL | PK | Nullable | FK hint |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | — |
| idCase | idCase | INT | | YES | → BOA_ANC_Pratica.idCase |
| testoNota | testoNota | VARCHAR(5000) | | YES | — |
| idStato | idStato | INT | | YES | — |
| operatore | operatore | VARCHAR(255) | | YES | — |
| datacreazione | datacreazione | DATETIME | | YES | — |

---

### BOA_ANC_FiltriUtente  →  tabella: BOA_ANC_FiltriUtente

| Campo | Colonna SQL | Tipo SQL | PK | Nullable | FK hint |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | — |
| user | user | VARCHAR(255) | | YES | — |
| statoAttivita | statoAttivita | VARCHAR(255) | | YES | — |
| tipoProcesso | tipoProcesso | VARCHAR(255) | | YES | — |
| codicePratica | codicePratica | INT | | YES | — |
| nomeAttivita | nomeAttivita | VARCHAR(255) | | YES | — |
| dataScadenzaDA | dataScadenzaDA | DATE | | YES | — |
| dataScadenzaA | dataScadenzaA | DATE | | YES | — |
| assegnatario | assegnatario | VARCHAR(255) | | YES | — |
| assegnatarioString | assegnatarioString | VARCHAR(255) | | YES | — |
| owner | owner | VARCHAR(255) | | YES | — |
| userFiltro | userFiltro | VARCHAR(255) | | YES | — |

---

### BOA_ANC_GestioneScodamento  →  tabella: BOA_ANC_GestioneScodamento

| Campo | Colonna SQL | Tipo SQL | PK | Nullable | FK hint |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | — |
| inEsecuzione | inEsecuzione | BOOLEAN | | YES | — |
| dataInizio | dataInizio | DATETIME | | YES | — |
| dataFine | dataFine | DATETIME | | YES | — |
| numEsitiToSend | numEsitiToSend | INT | | YES | — |

---

### BOA_ANC_ScodamentoStati  →  tabella: BOA_ANC_ScodamentoStati

| Campo | Colonna SQL | Tipo SQL | PK | Nullable | FK hint |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | — |
| idcase | idcase | INT | | YES | → BOA_ANC_Pratica.idCase |
| esito | esito | INT | | YES | — |
| errore | errore | VARCHAR(255) | | YES | — |
| timestamp | timestamp | DATETIME | | YES | — |
| retry | retry | INT | | YES | — |

---

### BOA_ANC_CaseChecklist  →  tabella: BOA_ANC_CaseChecklist

| Campo | Colonna SQL | Tipo SQL | PK | Nullable | FK hint |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | — |
| idRefCheckList | idRefCheckList | INT | | YES | → BOA_ANC_RefChecklist.id |
| idCase | idCase | INT | | YES | → BOA_ANC_Pratica.idCase |
| categoria | categoria | VARCHAR(255) | | YES | — |
| ordinamento | ordinamento | INT | | YES | — |
| descrizione | descrizione | VARCHAR(255) | | YES | — |
| note | note | VARCHAR(255) | | YES | — |
| timestamp | timestamp | DATETIME | | YES | — |
| flagPresenza | flagPresenza | BOOLEAN | | YES | — |
| flagConformita | flagConformita | BOOLEAN | | YES | — |
| flagRichiesto | flagRichiesto | BOOLEAN | | YES | — |
| flagObbligatorio | flagObbligatorio | BOOLEAN | | YES | — |
| flagBloccante | flagBloccante | BOOLEAN | | YES | — |
| idDipendenza | idDipendenza | INT | | YES | — |
| info | info | VARCHAR(2048) | | YES | — |
| utente | utente | VARCHAR(255) | | YES | — |
| codice | codice | VARCHAR(255) | | YES | — |
| descrizioneCodice | descrizioneCodice | VARCHAR(255) | | YES | — |
| visibile | visibile | BOOLEAN | | YES | — |

---

### BOA_ANC_RefChecklist  →  tabella: BOA_ANC_RefChecklist

Anagrafica statica delle domande della checklist.

| Campo | Colonna SQL | Tipo SQL | PK | Nullable | FK hint |
|---|---|---|---|---|---|
| id | id | INT (unique, NOT NULL) | ✓ | NO | — |
| idRef | idRef | INT | | YES | — |
| categoria | categoria | VARCHAR(255) | | YES | — |
| descrizione | descrizione | VARCHAR(255) | | YES | — |
| attivo | attivo | BIT | | YES | — |
| validitaDal | validitaDal | DATE | | YES | — |
| validitaAl | validitaAl | DATE | | YES | — |
| obbligatorio | obbligatorio | BIT | | YES | — |
| locked | locked | BIT | | YES | — |
| ordinamento | ordinamento | INT | | YES | — |
| idDipendenza | iddipendenza | INT | | YES | — |
| info | info | VARCHAR(2048) | | YES | — |
| codice | codice | VARCHAR(255) | | YES | — |
| descrizioneCodice | descrizionecodice | VARCHAR(255) | | YES | — |

---

### BOA_ANC_TipiDocumento  →  tabella: BOA_ANC_TipiDocumento

Anagrafica statica dei tipi di documento.

| Campo | Colonna SQL | Tipo SQL | PK | Nullable | FK hint |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | — |
| codiceDocId | codiceDocId | INT | | YES | — |
| descrizione | descrizione | VARCHAR(255) | | YES | — |
| categoria | categoria | VARCHAR(255) | | YES | — |
| attivo | attivo | BOOLEAN | | YES | — |

---

### BOA_ANC_Debug_WsInput  →  tabella: BOA_ANC_Debug_WsInput

Tabella di debug per le WebAPI esposte (request/response raw).

| Campo | Colonna SQL | Tipo SQL | PK | Nullable | FK hint |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | — |
| jsonbody | jsonbody | LONGTEXT | | YES | — |
| sdResponse | sdResponse | LONGTEXT | | YES | — |
| timestampInput | timestampInput | DATETIME | | YES | — |
| timestampOutput | timestampOutput | DATETIME | | YES | — |
| tipologia | tipologia | VARCHAR(255) | | YES | — |
| idWorkItem | idWorkItem | VARCHAR(255) | | YES | — |
| idCase | idCase | INT | | YES | — |

---

### BOA_ANC_Debug_WebServices  →  tabella: BOA_ANC_Debug_WebServices

Tabella di debug per le chiamate ai web service esterni.

| Campo | Colonna SQL | Tipo SQL | PK | Nullable | FK hint |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | — |
| idCase | idCase | INT | | YES | — |
| servizio | servizio | VARCHAR(255) | | YES | — |
| parametri | parametri | VARCHAR(255) | | YES | — |
| request | request | LONGTEXT | | YES | — |
| response | response | LONGTEXT | | YES | — |
| timestampChiamata | timestampChiamata | DATETIME | | YES | — |

---

### BOA_ANC_V_Pratica  →  VIEW: v_boaancpratica

View read-only (namespace `{urn:com:appian:types:BOA_ANC}`, `@Table(name="v_boaancpratica")`).  
Espone i campi di BOA_ANC_Pratica arricchiti con contatori da sistema esterno.  
Nessun CREATE / UPDATE / DELETE.

| Campo | Colonna SQL | Tipo SQL | PK | Nullable | Note |
|---|---|---|---|---|---|
| id | id | INT | ✓ | NO | — |
| idCase | idcase | INT | | YES | — |
| dataApertura | dataapertura | DATETIME | | YES | — |
| dataUltimaModifica | dataultimamodifica | DATETIME | | YES | — |
| operatoreUltimaModifica | operatoreultimamodifica | VARCHAR(255) | | YES | — |
| stato | stato | VARCHAR(255) | | YES | — |
| dataChiusura | datachiusura | DATETIME | | YES | — |
| canale | canale | VARCHAR(255) | | YES | — |
| idWorkitem | idworkitem | VARCHAR(255) | | YES | — |
| numPratica | numPratica | VARCHAR(255) | | YES | — |
| cfCliente | cfcliente | VARCHAR(255) | | YES | — |
| codiceCliente | codicecliente | VARCHAR(255) | | YES | — |
| dataInserimentoRichiesta | datainserimentorichiesta | DATETIME | | YES | — |
| folder | folder | INT | | YES | — |
| dataScadenza | datascadenza | DATETIME | | YES | — |
| esitoSD | esitoSD | VARCHAR(255) | | YES | — |
| dataEsitoSD | dataEsitoSD | DATETIME | | YES | — |
| segnalazioni | Segnalazioni | INT | | NO | contatore esterno |
| ticketSN | ticketSN | INT | | NO | contatore ServiceNow |

---

## STEP 2 — Entity Relationships

### Diagramma relazioni

```
BOA_ANC_Pratica (PK: id, corr: idCase)
  ├── BOA_ANC_StatiPratica     (FK: idCase → BOA_ANC_Pratica.idCase)
  ├── BOA_ANC_Cliente          (FK: idCase → BOA_ANC_Pratica.idCase)
  ├── BOA_ANC_DatiCarta        (FK: idCase → BOA_ANC_Pratica.idCase)
  ├── BOA_ANC_ContenutiDenuncia(FK: idCase → BOA_ANC_Pratica.idCase)
  ├── BOA_ANC_Documento        (FK: idCase → BOA_ANC_Pratica.idCase)
  ├── BOA_ANC_CaseNote         (FK: idCase → BOA_ANC_Pratica.idCase)
  ├── BOA_ANC_ScodamentoStati  (FK: idcase → BOA_ANC_Pratica.idCase)
  ├── BOA_ANC_CaseChecklist    (FK: idCase → BOA_ANC_Pratica.idCase)
  ├── BOA_ANC_Audit            (FK: recordId → BOA_ANC_Pratica.idCase)
  ├── BOA_ANC_Debug_WsInput    (FK: idCase → BOA_ANC_Pratica.idCase)
  └── BOA_ANC_Debug_WebServices(FK: idCase → BOA_ANC_Pratica.idCase)

BOA_ANC_RefChecklist (PK: id)
  └── BOA_ANC_CaseChecklist    (FK: idRefCheckList → BOA_ANC_RefChecklist.id)

BOA_ANC_TipiDocumento (PK: id, business key: codiceDocId)
  └── BOA_ANC_Documento        (FK: codiceDocId → BOA_ANC_TipiDocumento.codiceDocId)
```

> **Nota su idCase**: campo Appian BPM (Case ID). La correlazione è per valore su `idCase`.  
> Non è presente `@JoinColumn` esplicito nell'XSD; la relazione è osservabile dai process model che scrivono entità figlie con lo stesso `idCase` della pratica padre.

### Tabella FK

| Entity figlio | Campo FK | Entity padre | Campo ref | Osservabile da |
|---|---|---|---|---|
| BOA_ANC_StatiPratica | idCase | BOA_ANC_Pratica | idCase | XSD field name + process model BOA_ANC_Processo_CambioStato |
| BOA_ANC_Cliente | idCase | BOA_ANC_Pratica | idCase | XSD field name + process model BOA_ANC_ElaborazioneJsonInput_CreazionePratica |
| BOA_ANC_DatiCarta | idCase | BOA_ANC_Pratica | idCase | XSD field name + process model BOA_ANC_ElaborazioneJsonInput_CreazionePratica |
| BOA_ANC_ContenutiDenuncia | idCase | BOA_ANC_Pratica | idCase | XSD field name + process model BOA_ANC_ElaborazioneJsonInput_CreazionePratica |
| BOA_ANC_Documento | idCase | BOA_ANC_Pratica | idCase | XSD field name + process model BOA_ANC_ElaborazioneJsonInput_CreazionePratica |
| BOA_ANC_CaseNote | idCase | BOA_ANC_Pratica | idCase | XSD field name + process model BOA_ANC_SalvataggioDati |
| BOA_ANC_ScodamentoStati | idcase | BOA_ANC_Pratica | idCase | XSD field name + process model BOA_ANC_ScodamentoSingoloEsito |
| BOA_ANC_CaseChecklist | idCase | BOA_ANC_Pratica | idCase | XSD field name + process model BOA_ANC_SalvataggioDati |
| BOA_ANC_CaseChecklist | idRefCheckList | BOA_ANC_RefChecklist | id | XSD field name (idRefCheckList) |
| BOA_ANC_Audit | recordId | BOA_ANC_Pratica | idCase | XSD field name + interface BOA_ANC_Audit (filter: recordId = idCase) |
| BOA_ANC_Documento | codiceDocId | BOA_ANC_TipiDocumento | codiceDocId | XSD field name (codiceDocId) |
| BOA_ANC_Debug_WsInput | idCase | BOA_ANC_Pratica | idCase | XSD field name |
| BOA_ANC_Debug_WebServices | idCase | BOA_ANC_Pratica | idCase | XSD field name |

---

## STEP 3 — CRUD Ownership

> Legenda fonti: **R** = content rule/expression rule · **P** = process model

| Entity | CREATE | READ | UPDATE | DELETE |
|---|---|---|---|---|
| BOA_ANC_Pratica | P: BOA_ANC_ElaborazioneJsonInput_CreazionePratica | R: BOA_ANC_GetPraticaByIdCase, BOA_ANC_GetPraticaByIdWorkItem, BOA_ANC_VerificaPraticaDuplicataByIdWorkitem | P: BOA_ANC_Processo_CambioStato, BOA_ANC_SalvataggioDati, BOA_ANC_ScodamentoSingoloEsito | — |
| BOA_ANC_StatiPratica | P: BOA_ANC_ElaborazioneJsonInput_CreazionePratica, BOA_ANC_Processo_CambioStato, BOA_ANC_ScodamentoSingoloEsito | R: BOA_ANC_StatiPratica (content/interface) | — | — |
| BOA_ANC_Cliente | P: BOA_ANC_ElaborazioneJsonInput_CreazionePratica | R: BOA_ANC_GetClienteByIdCase | — | — |
| BOA_ANC_DatiCarta | P: BOA_ANC_ElaborazioneJsonInput_CreazionePratica | R: BOA_ANC_GetDatiPraticaByIdCase | — | — |
| BOA_ANC_ContenutiDenuncia | P: BOA_ANC_ElaborazioneJsonInput_CreazionePratica | R: BOA_ANC_GetContenutiDenunciaByIdCase, BOA_ANC_GetContenutoDenunciaById; P: BOA_ANC_ScaricaSingoloDoc (lettura) | — | — |
| BOA_ANC_Documento | P: BOA_ANC_ElaborazioneJsonInput_CreazionePratica, BOA_ANC_Processo_TipizzaDoc | R: BOA_ANC_GetDocumentoByIdCase | P: BOA_ANC_Processo_TipizzaDoc | — |
| BOA_ANC_Audit | P: BOA_ANC_ScriviAudit, BOA_ANC_Processo_TipizzaDoc | R: BOA_ANC_Audit (interface) | — | — |
| BOA_ANC_CaseNote | P: BOA_ANC_SalvataggioDati | R: BOA_ANC_GetNotaByIdCase | — | P: BOA_ANC_SalvataggioDati (delete+recreate) |
| BOA_ANC_FiltriUtente | P: BOA_ANC_SalvaFiltriUtente | R: BOA_ANC_GetFiltriUtenteByUser, BOA_ANC_GetLastIdFilter | P: BOA_ANC_SalvaFiltriUtente | — |
| BOA_ANC_GestioneScodamento | — | R: BOA_ANC_GetGestioneScodamento | P: BOA_ANC_Batch_ScodamentoCallback | — |
| BOA_ANC_ScodamentoStati | P: BOA_ANC_SalvataggioDati, BOA_ANC_ScodamentoSingoloEsito | R: BOA_ANC_GetEsitiToSend | P: BOA_ANC_ScodamentoSingoloEsito | — |
| BOA_ANC_CaseChecklist | P: BOA_ANC_SalvataggioDati | R: BOA_ANC_GetCaseCheckListByIdCase, BOA_ANC_GetCaseCheckListVisibileByIdCase | P: BOA_ANC_SalvataggioDati; R: BOA_ANC_AbilitaVisibilitaCheckList, BOA_ANC_CorreggiCheckList | — |
| BOA_ANC_RefChecklist | — (dati statici) | R: referenziata da operazioni CaseChecklist | — | — |
| BOA_ANC_TipiDocumento | — (dati statici) | R: BOA_ANC_GetTipoDocumentoAttivoByCodiceDocumento, BOA_ANC_GetTipiDocumentoAttivi | — | — |
| BOA_ANC_Debug_WsInput | P: BOA_ANC_ProcessoWebApi_CreaPratica | — | P: BOA_ANC_ProcessoWebApi_CreaPratica (update sdResponse, timestampOutput) | — |
| BOA_ANC_Debug_WebServices | P: BOA_ANC_Processo_ScritturaTabellaDebugWS | — | — | — |
| BOA_ANC_V_Pratica (view) | — | R: BOA_ANC_GetNumeroPraticheDashBoard (SP: boagetnumeropratichedashboard), BOA_ANC_GetPraticheGiornaliere (SP: boaancgetnumeropratichegiornaliere), BOA_ANC_GetPraticheGiornaliereLavorate (SP: boaancgetnumeropratichegiornalierelav), BOA_ANC_GetPraticheByStato (SP: boaancgetpratichebystato) | — | — |

> SP = stored procedure su `jdbc/Appian`.  
> BOA_ANC_V_Pratica è una view — nessun INSERT/UPDATE/DELETE applicabile.
