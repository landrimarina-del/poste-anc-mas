# 01 - Architettura Applicativa

> Riferimenti normativi: [_DOC1_Architettura_di_Riferimento.txt](../requisitiArchitetturali/_DOC1_Architettura_di_Riferimento.txt), [_DOC2_Catalogo_Tecnologico.txt](../requisitiArchitetturali/_DOC2_Catalogo_Tecnologico.txt), [_Vincoli Architetturali Emersi.txt](../requisitiArchitetturali/_Vincoli%20Architetturali%20Emersi.txt).
> Input funzionale: deliverable BA in [docs/out-discovery-business-analist](../out-discovery-business-analist); baseline di dominio: `docs/requirements/source-of-truth/Attivazione nuova carta_Discovery.md`.
> Contratto applicativo verso BPM: `docs/requirements/source-of-truth/InterfaceAgreement.md` + `BPM_SubmitWorkItem_v1.1.yaml`.
> Profilo target di riferimento: **Mid-Market / Enterprise** (Poste Italiane). Profilo eseguito in POC: **Light Local**.

## 1. Bounded Context

Si individuano **6 bounded context** allineati alle famiglie di capability del BA. Ogni context ha responsabilità applicative, persistenza propria e contratti di interfaccia espliciti.

| # | Bounded Context | Responsabilità | Capability BA |
|---|---|---|---|
| BC1 | **Practice Management** | Ciclo di vita pratica (Aperta → Chiusa OK/KO), dati cliente/carta, dettaglio, repository, audit di stato. Ownership dell'identificativo `requestId`. | C1.x, C2.x, C9.1–C9.2 |
| BC2 | **Workflow / Task Orchestration** | Generazione task, lista attività, presa in carico, riassegnazione, modellazione workflow ANC come processo BPMN. | C3.x, C6.1–C6.4 |
| BC3 | **Document & Checklist** | Storage allegati, viewer, tipizzazione, checklist Verbale/Carta, calcolo esito. | C4.x, C5.1–C5.2 |
| BC4 | **BPM Integration Gateway** | Adattatore verso BPM esterno: ricezione apertura, validazione, idempotenza, invio esiti, ricezione conferme. Owner di `resultCode` e contratti Interface Agreement BPM↔SD. | C1.1–C1.5, C5.3–C5.7 |
| BC5 | **Supervision & Reporting** | Dashboard supervisore: contatori real-time, istogrammi, riassegnazione segnalazioni, lettura aggregata. | C6.5–C6.8 |
| BC6 | **Signal Management (Sinergia)** | Modulo segnalazioni: invio/visualizzazione/riassegnazione, integrazione (stub) Sinergia/PIX. | C7.x |

Capability trasversali: **IAM/AuthZ**, **Audit Trail**, **Notification/Event Bus interno**, **Frontend Shell** (tab Home/Attività/Pratiche, componenti UI condivisi).

## 2. Macro-Moduli (allineamento ai layer DOC1)

| Macro-modulo | Layer DOC1 | Bounded context coperti | Responsabilità |
|---|---|---|---|
| **M-FE Scrivania Digitale** | Layer 1 – Presentation | tutti (UI) | SPA con tab Home/Attività/Pratiche, viewer documenti, checklist, dashboard supervisore. |
| **M-API Gateway** | Layer 1/8 | tutti | Reverse proxy, autenticazione, rate limiting, routing verso BE. |
| **M-Practice Service** | Layer 2 – Orchestration | BC1 | API di repository pratiche, dettaglio, audit, stati. |
| **M-Workflow Engine** | Layer 2 – Orchestration & BPM | BC2 | Motore BPMN per orchestrazione ciclo pratica + task. |
| **M-Document Service** | Layer 5 – Document Management | BC3 | Storage e metadati allegati, viewer, checklist, calcolo esito. |
| **M-BPM Integration** | Layer 6 – Integration | BC4 | Adattatore in/out verso BPM Poste; idempotenza; codici resultCode. |
| **M-Supervision Service** | Layer 2 / 7 | BC5 | Aggregati real-time, dataset istogrammi, riassegnazione. |
| **M-Signal Service** | Layer 6 – Integration | BC6 | Segnalazioni Sinergia (stub in POC). |
| **M-IAM** | Layer 1/8 | trasversale | Autenticazione, ruoli (OPERATORE, SUPERVISORE), autorizzazione per tab/azione. |
| **M-Audit Service** | Layer 8 | trasversale | Cronologia attività + storico stati pratica. |
| **M-Persistence** | Layer 8 | trasversale | DB transazionale + object storage allegati. |
| **M-Observability** | Layer 8 | trasversale | Log strutturato, metriche, tracing minimale. |

## 3. Principi architetturali applicati

Selezione dai 9 principi DOC1 effettivamente vincolanti per la POC:

- **P1 – No vendor lock-in**: solo componenti OSI-approved; nessuna API cloud-proprietaria.
- **P2 – Cloud-native & modulare**: container come unità di deploy; bounded context con confini espliciti.
- **P3 – Security Zero Trust** *(parziale in POC)*: TLS sui canali integrazione, autenticazione su ogni API; mTLS inter-servizio rinviato a target.
- **P4 – API-first & event-driven**: contratti espliciti; eventi interni per audit/notifiche.
- **P5 – Observability by design**: log + metriche minimali in POC, tracing predisposto.
- **P8 – Scalabilità progressiva**: in POC modular monolith con confini di context preservati; evoluzione verso few-services nel target.
- **P9 – DevOps & rilascio continuo**: pipeline CI minimale in POC; GitOps target.

Esclusi dalla POC (predisposti per il target): **P6 AI-ready**, **P7 UX multicanale completa** (mobile-first/PWA), **P3 mTLS completo**.

## 4. Modello applicativo

- **Target enterprise**: few-services per macro-modulo (M-Practice, M-Workflow, M-Document, M-BPM Integration, M-Supervision, M-Signal); deploy su orchestratore container; HA del DB; broker eventi per audit e notifiche.
- **POC light**: **modular monolith** con i 6 bounded context come moduli interni a confini espliciti (package separati, persistence isolata per schema/tabelle); UI separata; BPM integration come modulo dedicato; stub esterni in container separati. Conserva la stessa modularizzazione → evoluzione a few-services senza rifondare.

## 5. Confini e contratti tra moduli

- **FE ⇄ Gateway ⇄ BE**: API sincrone con contratti versionati; autenticazione tramite token emesso dall'IAM.
- **BC4 (BPM Integration) ⇄ BC1 (Practice)**: contratto applicativo interno (creazione pratica, registrazione esito); idempotenza e mappa `resultCode` confinate in BC4.
- **BC2 (Workflow) ⇄ BC1 / BC3**: il motore di workflow modella stati pratica e task; chiama BC1/BC3 come service task.
- **BC5 / BC6 ⇄ BC1**: read-only su repository pratiche per aggregati e contestualizzazione segnalazioni.
- **Audit (M-Audit)**: consumo di eventi di dominio (cambio stato, presa in carico, tipizzazione, riassegnazione, chiusura) emessi dai context.

## 6. Vincoli applicativi (da Vincoli Architetturali Emersi)

- Backend Java/OpenJDK (no dipendenze Oracle).
- Frontend React.
- DB MariaDB (con SQL standard, compatibile con altri DB).
- Storage allegati: pluggable (S3-compatible / filesystem / enterprise).
- IAM: integrazione Active Directory / IAM enterprise nel target; fallback Basic Auth in POC test.
- Autorizzazione: RBAC su gruppi gerarchici.
- API: OAuth 2.0 + Basic fallback + supporto mTLS.
- Trasporto: TLS.
- Integrazione esterni: REST, SFTP, code/messaging.
- Workflow: trigger timer + eventi; processi modellati come grafi (BPMN).

## 7. Mapping bounded context → BA capability (sintesi)

| BC | Capability BA |
|---|---|
| BC1 | C1.3 (idempotenza ID_WORKITEM, persistenza), C2.1, C2.6–C2.9, C9.1, C9.2 |
| BC2 | C3.1, C3.2, C3.3, C3.5, C6.1, C6.2, C6.3, C6.4 |
| BC3 | C4.1, C4.2, C4.3, C4.4, C4.5, C4.6, C4.7, C4.8, C4.9, C4.10, C4.11, C4.12, C5.1, C5.2 |
| BC4 | C1.1, C1.2, C1.4, C1.5, C5.3, C5.4, C5.5, C5.6, C5.7 |
| BC5 | C6.5, C6.6, C6.7, C6.8 |
| BC6 | C7.1, C7.2, C7.3, C7.4, C7.5 |
| Trasversali | C2.2, C2.3, C2.4, C2.5 (lista), C8.1–C8.3, C9.3 |
