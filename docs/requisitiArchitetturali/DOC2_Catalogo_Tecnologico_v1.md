**CATALOGO TECNOLOGICO\
BPM OPEN SOURCE**

Documento 2 di 3 --- Schede Componenti

**Versione 1.0 --- Aprile 2026\
Prossima verifica: Ottobre 2026**

# Sommario {#sommario .TOC-Heading}

[📦 Framework applicativo backend
[4](#framework-applicativo-backend)](#framework-applicativo-backend)

[📦 Framework applicativo frontend
[5](#framework-applicativo-frontend)](#framework-applicativo-frontend)

[📦 Meta-framework frontend (SSR/SSG)
[6](#meta-framework-frontend-ssrssg)](#meta-framework-frontend-ssrssg)

[📦 Build tool frontend [7](#build-tool-frontend)](#build-tool-frontend)

[📦 CSS / Design system [8](#css-design-system)](#css-design-system)

[📦 Motore BPM / workflow
[9](#motore-bpm-workflow)](#motore-bpm-workflow)

[📦 Motore BPM / workflow (alternativa cloud-native)
[10](#motore-bpm-workflow-alternativa-cloud-native)](#motore-bpm-workflow-alternativa-cloud-native)

[📦 Durable execution engine
[12](#durable-execution-engine)](#durable-execution-engine)

[📦 Rule engine [13](#rule-engine)](#rule-engine)

[📦 API Gateway [14](#api-gateway)](#api-gateway)

[📦 Identity & Access Management
[15](#identity-access-management)](#identity-access-management)

[📦 Message broker (code applicative)
[16](#message-broker-code-applicative)](#message-broker-code-applicative)

[📦 Event streaming [17](#event-streaming)](#event-streaming)

[📦 Database transazionale
[18](#database-transazionale)](#database-transazionale)

[📦 Cache distribuita [19](#cache-distribuita)](#cache-distribuita)

[📦 Gestione documentale base
[20](#gestione-documentale-base)](#gestione-documentale-base)

[📦 ECM enterprise [21](#ecm-enterprise)](#ecm-enterprise)

[📦 Content extraction / OCR
[22](#content-extraction-ocr)](#content-extraction-ocr)

[📦 Integration layer (EIP)
[23](#integration-layer-eip)](#integration-layer-eip)

[📦 Vector store / RAG [24](#vector-store-rag)](#vector-store-rag)

[📦 LLM inference self-hosted
[25](#llm-inference-self-hosted)](#llm-inference-self-hosted)

[📦 Agent orchestration
[26](#agent-orchestration)](#agent-orchestration)

[📦 Process mining [27](#process-mining)](#process-mining)

[📦 Service mesh [28](#service-mesh)](#service-mesh)

[📦 Container orchestration
[29](#container-orchestration)](#container-orchestration)

[📦 GitOps / CD [30](#gitops-cd)](#gitops-cd)

[📦 Secrets management [31](#secrets-management)](#secrets-management)

Questo catalogo contiene una scheda per ogni slot architetturale
definito nel Documento 1. Ogni scheda è autocontenuta e aggiornabile
indipendentemente.

**Struttura di ogni scheda:**

- Scelta primaria: componente raccomandato con versione, licenza e
  motivazione.

- Alternativa: quando preferirla rispetto alla scelta primaria.

- Scartati: componenti valutati e non selezionati, con motivazione.

- Anti-pattern: errori da evitare con questo slot.

- Date: ultima verifica e prossima verifica programmata.

# 📦 Framework applicativo backend

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             2 --- Orchestration & BPM
  architetturale    

  Profilo minimo    PMI (tutti i profili)

  Scelta primaria   Spring Boot 3.3+ / Java 21 LTS

  Licenza           Apache 2.0
  -----------------------------------------------------------------------

Perché: Ecosistema maturo, Virtual Threads (Project Loom), integrazione
nativa con BPM, regole, broker e IAM. Supporto LTS fino al 2031.
Comunità enorme, competenze reperibili.

Rischio noto: Footprint applicativo più pesante rispetto a framework
leggeri (Quarkus, Micronaut).

**Alternative:**

- Quarkus 3.x (Apache 2.0) --- Startup \< 50ms, GraalVM native.
  Preferibile per funzioni serverless o sidecar leggeri. Non consigliato
  come framework principale per la piattaforma BPM dato il minore
  ecosistema di librerie rispetto a Spring.

**Scartati:**

- Node.js / NestJS: Ecosistema meno maturo per BPM, rule engine e
  integrazioni enterprise.

- .NET / C#: Ecosistema open source BPM più limitato. Vendor-dipendente
  (Microsoft).

⛔ Anti-pattern: Non mescolare framework backend (es. Spring Boot +
Quarkus) nello stesso layer senza un confine di servizio chiaro.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 Framework applicativo frontend

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             1 --- Presentation
  architetturale    

  Profilo minimo    PMI (tutti i profili)

  Scelta primaria   React 19.x + TypeScript 5.7+

  Licenza           MIT / Apache 2.0
  -----------------------------------------------------------------------

Perché: Ecosistema più ampio per componenti UI, state management, form
engine. React 19: Server Components, Actions, use() hook. TypeScript
5.7+: type inference migliorata.

Rischio noto: Ecosystem churn: aggiornamenti frequenti dell\'ecosistema
React richiedono manutenzione.

**Alternative:**

- Vue 3.x + TypeScript (MIT) --- Curva di apprendimento più dolce.
  Preferibile se il team ha esperienza Vue. Ecosistema BPM-specifico
  (bpmn-js) meno integrato.

**Scartati:**

- Angular 17+: Opinioned framework con curva ripida. Ciclo di release
  con breaking changes. Ecosistema BPM-specific meno ricco.

- Svelte/SvelteKit: Troppo giovane per enterprise. Ecosistema librerie
  limitato.

⛔ Anti-pattern: Non usare React class components in un nuovo progetto
2026. Usare esclusivamente function components + hooks.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 Meta-framework frontend (SSR/SSG)

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             1 --- Presentation
  architetturale    

  Profilo minimo    PMI (tutti i profili)

  Scelta primaria   Next.js 15.x

  Licenza           MIT
  -----------------------------------------------------------------------

Perché: SSR per pagine pubbliche, client-side per moduli BPM. Code
splitting automatico. App Router, React Server Components, partial
prerendering.

Rischio noto: Vercel-centric: alcune ottimizzazioni funzionano meglio su
Vercel Cloud. Mitigazione: deploy standalone su qualsiasi hosting
Node.js.

**Alternative:**

- Vite SPA puro (MIT) --- Per moduli interamente client-side senza SSR.
  Più semplice, meno opinioni. Preferibile per applicazioni interne
  senza requisiti SEO.

**Scartati:**

- Remix / React Router 7: Meno adottato in contesti enterprise. Rischio
  di nicchia.

⛔ Anti-pattern: Non usare Next.js Pages Router per nuovi progetti: App
Router è il modello raccomandato.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 Build tool frontend

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             1 --- Presentation
  architetturale    

  Profilo minimo    PMI (tutti i profili)

  Scelta primaria   Vite 6.x

  Licenza           MIT
  -----------------------------------------------------------------------

Perché: Dev server HMR istantaneo, bundle ottimizzato con Rollup, plugin
ecosystem maturo. Vite 6 stabile; v7/v8 disponibili ma non raccomandate
per stabilità enterprise.

Rischio noto: Se si usa Next.js, il bundler è integrato (Turbopack).
Vite serve per moduli SPA standalone.

**Scartati:**

- webpack 5: Lento per sviluppo. Considerato legacy per nuovi progetti
  2026.

- Turbopack: Integrato in Next.js, non standalone. Non un\'alternativa
  diretta.

⛔ Anti-pattern: Non usare webpack per nuovi progetti. Vite è diventato
lo standard de facto.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 CSS / Design system

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             1 --- Presentation
  architetturale    

  Profilo minimo    PMI (tutti i profili)

  Scelta primaria   Tailwind CSS 4.x + shadcn/ui + Radix UI

  Licenza           MIT
  -----------------------------------------------------------------------

Perché: Tailwind 4.0: riscrittura completa, performance migliorate,
CSS-first config. shadcn/ui: componenti copiati nel progetto,
completamente modificabili. Radix UI: primitive headless ARIA-compliant
per accessibilità.

Rischio noto: Tailwind 4 non retrocompatibile con config v3. Migrazione
richiede effort.

**Alternative:**

- Material UI / Joy UI (MIT) --- Design system completo ready-made. Meno
  flessibile, più opinionato.

**Scartati:**

- Bootstrap 5: Paradigma diverso (componenti precostituiti vs.
  utility-first). Meno flessibile.

- Styled Components / CSS-in-JS: Performance runtime inferiore. Tailwind
  è zero-runtime.

⛔ Anti-pattern: Non costruire un design system custom da zero: usare
shadcn/ui come base e personalizzare.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 Motore BPM / workflow

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             2 --- Orchestration & BPM
  architetturale    

  Profilo minimo    PMI (raccomandato) / Mid-Market+ (obbligatorio)

  Scelta primaria   Flowable 7.x

  Licenza           Apache 2.0
  -----------------------------------------------------------------------

Perché: BPMN 2.0 completo, CMMN 1.1, DMN 1.3. REST API completa, Spring
Boot Starter. Human Task con form engine integrato. Clustering HA
tramite database condiviso. Community attiva, progetto open source
genuino senza feature-gating aggressivo.

Rischio noto: Supporto commerciale (Flowable Enterprise) costoso: da
€50.000/anno.

**Alternative:**

- Camunda 8 SaaS --- Per team che preferiscono BPM-as-a-Service senza
  gestione operativa. ATTENZIONE: Camunda 8 è cloud-only con Zeebe
  engine, modello pricing non open source. Rappresenta un trade-off
  consapevole rispetto al principio P1 (no lock-in).

**Scartati:**

- Camunda 7 CE: ⚠ ARCHIVIATO il 4 novembre 2025. Repository read-only,
  nessuna nuova release, nessun aggiornamento di sicurezza. Non adottare
  per nuovi progetti.

- jBPM: Meno attivo, community ridotta, integrazione Spring meno matura.

- Activiti: Fork storico, sviluppo rallentato. Flowable ne è
  l\'evoluzione.

⛔ Anti-pattern: Non usare un motore BPM come scheduler. Se il processo
non ha stati, human task o requisiti di audit: usare lo scheduler del
framework.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 Motore BPM / workflow (alternativa cloud-native)

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             2 --- Orchestration & BPM
  architetturale    

  Profilo minimo    Mid-Market (opzionale selettivo) / Enterprise
                    (opzionale selettivo)

  Alternativa       Kogito (Quarkus / Spring Boot)
  complementare     

  Licenza           Apache 2.0
  -----------------------------------------------------------------------

Perché: rappresenta un approccio **cloud-native e microservizi-first**
alla gestione di processi BPMN e decisioni DMN, basato su runtime
leggeri embedded nelle applicazioni. A differenza dei motori BPM
tradizionali, non richiede un engine centralizzato ma integra i processi
direttamente nei servizi applicativi.

È preferibile in architetture:

- **event-driven** con forte uso di broker o streaming

- **microservizi autonomi** con bounded context separati

- workflow **tecnici** (orchestrazioni API, integrazioni, retry)

- contesti **developer-centric**, senza necessità di strumenti BPM per
  utenti business

L'integrazione nativa con Drools e DMN consente di unificare logica
decisionale e orchestrazione all'interno dello stesso servizio.

**Rischio noto:**\
Kogito non è una piattaforma BPM completa "out-of-the-box":

- assenza di worklist utenti e human task pronti

- nessun form engine integrato

- necessità di sviluppare UI e gestione task custom

- governance distribuita (processi non centralizzati)

Il catalogo privilegia **Spring Boot come framework principale**, mentre
Kogito è ottimizzato principalmente per Quarkus.

**Quando preferire Kogito rispetto a Flowable:**

- orchestrazioni tecniche tra microservizi

- processi guidati da eventi (Kafka, messaging)

- workflow senza human task o approvazioni

- necessità di deploy indipendente e scalabilità per servizio

**Quando NON usarlo:**

- workflow con utenti, approvazioni o SLA visibili

- piattaforme BPM centralizzate per il business

- scenari con requisiti di audit, compliance e governance completa

- esigenze di form engine o strumenti low-code

**Pattern di utilizzo raccomandato:**

Architettura ibrida con separazione dei ruoli:

- motore BPM (Flowable) → workflow business e processi core

- Kogito → orchestrazioni tecniche e servizi distribuiti

- event streaming (Kafka) → backbone di integrazione

⛔ Anti-pattern:

- usare Kogito come piattaforma BPM enterprise completa

<!-- -->

- implementare human task complessi senza framework dedicato

- adottare Kogito in architetture monolitiche o non event-driven

- duplicare logica BPM tra engine centrale e microservizi

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 Durable execution engine

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             2 --- Orchestration & BPM (complementare)
  architetturale    

  Profilo minimo    Mid-Market (opzionale) / Enterprise (opzionale)

  Scelta primaria   Temporal.io 1.30.x

  Licenza           MIT
  -----------------------------------------------------------------------

Perché: Orchestrazione di workflow tecnici con stato persistente e retry
automatico. Ogni workflow sopravvive a failure di processo, nodo o rete.
19.500+ stars, 268 contributor, usato da NVIDIA, Salesforce, OpenAI,
Cloudflare. SDK nativi per Java, Go, TypeScript, Python.

Rischio noto: Non sostituisce un motore BPMN visuale (Flowable) per
processi con human task e modellazione grafica per il business.

**Scartati:**

- Cadence (Uber): Precursore di Temporal, meno attivo, community più
  piccola.

⛔ Anti-pattern: Non usare Temporal per processi business con human
task, approvazioni e modellazione BPMN: quello è il dominio del motore
BPM.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 Rule engine

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             3 --- Decision & Rules
  architetturale    

  Profilo minimo    Mid-Market (raccomandato) / Enterprise (obbligatorio)

  Scelta primaria   Drools 8.x

  Licenza           Apache 2.0
  -----------------------------------------------------------------------

Perché: DRL, DMN 1.4, Decision Tables, PMML. Hot-reload regole. KIE
Server come microservizio REST. Integrazione nativa Flowable.

Rischio noto: Curva di apprendimento significativa per regole complesse.

**Alternative:**

- OpenL Tablets 5.x (Apache 2.0) --- Regole business in formato Excel.
  Preferibile quando utenti di business mantengono autonomamente le
  regole. Può coesistere con Drools.

**Scartati:**

- Easy Rules: Troppo semplice per esigenze enterprise. No DMN.

⛔ Anti-pattern: Non esternalizzare regole in un rule engine se sono \<
5 e cambiano raramente: codice applicativo è più chiaro.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 API Gateway

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             1 --- Presentation / Access
  architetturale    

  Profilo minimo    Mid-Market (raccomandato) / Enterprise (obbligatorio)

  Scelta primaria   Apache APISIX 3.x

  Licenza           Apache 2.0
  -----------------------------------------------------------------------

Perché: 100% plugin open source (a differenza di Kong che sposta feature
nella versione Enterprise). Performance: 18.000 QPS single-core con
plugin attivi vs \~1.700 di Kong. Hot-reload configurazione senza
restart, basato su etcd. Apache Foundation governance. Supporto nativo
AI Gateway per LLM workloads.

Rischio noto: Community più piccola di Kong. Meno plugin di terze parti.

**Alternative:**

- Kong OSS 3.x (Apache 2.0) --- Più adottato, ecosistema plugin più
  ampio. Preferibile se il team ha già esperienza Kong. Rischio: Kong
  Inc. sposta progressivamente funzionalità nella versione Enterprise.

- Traefik v3 (MIT) --- Per PMI o single-tenant. Semplice, nativo
  Docker/K8s. Non adatto come API Gateway enterprise.

**Scartati:**

- AWS API Gateway / Azure APIM: Lock-in cloud. Viola principio P1.

- Envoy standalone: Troppo low-level come API Gateway. Meglio come data
  plane sotto APISIX o Istio.

⛔ Anti-pattern: Non usare l\'API Gateway come ESB. Il gateway gestisce
routing, auth e policy --- non trasformazioni dati complesse.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 Identity & Access Management

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             1 --- Presentation / Access
  architetturale    

  Profilo minimo    PMI (raccomandato) / Mid-Market+ (obbligatorio)

  Scelta primaria   Keycloak 24.x

  Licenza           Apache 2.0
  -----------------------------------------------------------------------

Perché: OIDC/OAuth 2.0 (FAPI 1.0), SAML 2.0, RBAC/ABAC, MFA (TOTP,
WebAuthn/FIDO2). Multi-tenancy (Realm). SPID e CIE per PA (plugin AgID).

Rischio noto: Pesante in termini di risorse. Richiede gestione
self-hosted.

**Alternative:**

- Microsoft Entra ID --- Per PMI già Microsoft 365. Evita la gestione
  self-hosted. Preferibile quando l\'organizzazione ha già un IAM
  corporate Microsoft-centric.

**Scartati:**

- Auth0: SaaS proprietario. Lock-in cloud, costi crescenti.

- Authelia: Troppo semplice per esigenze BPM enterprise (no SAML, no
  SPID, no multi-tenant).

⛔ Anti-pattern: Non implementare autenticazione custom nel backend
applicativo. Sempre delegare a un IAM dedicato.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 Message broker (code applicative)

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             6 --- Integration
  architetturale    

  Profilo minimo    PMI (opzionale) / Mid-Market (raccomandato)

  Scelta primaria   RabbitMQ 3.13+

  Licenza           MPL 2.0
  -----------------------------------------------------------------------

Perché: Code applicative, retry con dead letter, disaccoppiamento
produttore/consumatore. Maturo, semplice da operare, client Spring
nativi.

Rischio noto: Non adatto per event streaming ad alto volume.

**Alternative:**

- NATS 2.x (Apache 2.0 --- CNCF Incubating) --- Singolo binario,
  pub/sub + request/reply + streaming (JetStream), key-value e object
  store integrati, latenza sub-millisecondo. 18.000+ stars. Preferibile
  per PMI con esigenze di code semplici: footprint minimo, zero
  dipendenze.

**Scartati:**

- ActiveMQ: Legacy rispetto a RabbitMQ. Community meno attiva.

⛔ Anti-pattern: Non usare un broker di messaggi per job schedulati
semplici. Lo scheduler applicativo (Spring \@Scheduled, Quartz) è
sufficiente.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 Event streaming

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             6 --- Integration
  architetturale    

  Profilo minimo    Enterprise (obbligatorio) / Mid-Market (opzionale
                    selettivo)

  Scelta primaria   Apache Kafka 3.7.x (KRaft mode)

  Licenza           Apache 2.0
  -----------------------------------------------------------------------

Perché: Event backbone: ogni transizione BPM genera un evento.
Multi-consumatore, replay, streaming. KRaft mode (senza ZooKeeper).
Schema Registry OSS. Kafka Streams per KPI real-time.

Rischio noto: Operativamente complesso. Richiede almeno 3 broker per
produzione.

**Alternative:**

- Apache Pulsar 3.x (Apache 2.0) --- Separazione compute/storage,
  multi-tenancy nativa, geo-replication. Meno adottato di Kafka,
  community più piccola.

**Scartati:**

- Amazon Kinesis / Azure Event Hubs: Lock-in cloud. Viola principio P1.

⛔ Anti-pattern: Non usare Kafka se i volumi sono \< 10.000
eventi/giorno. Un message broker tradizionale è 10x più semplice.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 Database transazionale

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             Trasversale
  architetturale    

  Profilo minimo    PMI (tutti i profili)

  Scelta primaria   PostgreSQL 16.x

  Licenza           PostgreSQL License (permissiva)
  -----------------------------------------------------------------------

Perché: Database open source più completo: JSON, full-text search,
geospatial, partitioning. HA con Patroni da Mid-Market. Supporto LTS,
comunità enorme.

Rischio noto: Query optimizer meno avanzato di Oracle/SQL Server per
workload analitici.

**Alternative:**

- TimescaleDB 2.14.x (Apache 2.0) --- Estensione PostgreSQL per
  time-series. Usare insieme a PostgreSQL per event log e process
  mining.

**Scartati:**

- MySQL/MariaDB: Meno feature enterprise (JSON, CTE ricorsivi, window
  functions meno maturi).

- MongoDB: Document store: non adatto come database transazionale
  primario per BPM.

⛔ Anti-pattern: Non usare PostgreSQL come sostituto di un message
broker (LISTEN/NOTIFY non scala come un broker dedicato).

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 Cache distribuita

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             Trasversale
  architetturale    

  Profilo minimo    PMI (opzionale) / Mid-Market (raccomandato) /
                    Enterprise (obbligatorio)

  Scelta primaria   Redis 7.x (o Valkey)

  Licenza           BSD-3 / SSPL (Redis 7.4+) --- Valkey: BSD-3
  -----------------------------------------------------------------------

Perché: Cache, sessioni distribuite, lock, rate limiting. Performance
sub-millisecondo. Nota: Redis ha cambiato licenza a SSPL da v7.4. Valkey
(Linux Foundation fork, BSD-3) è l\'alternativa open source pura,
API-compatible.

Rischio noto: Valkey è relativamente giovane (2024). Redis SSPL non è
OSI-approved.

**Alternative:**

- KeyDB (BSD-3) --- Fork Redis multithreaded. Performance superiore su
  hardware multi-core.

**Scartati:**

- Memcached: Feature set troppo limitato rispetto a Redis (no
  persistence, no data structures).

⛔ Anti-pattern: Non usare Redis come database primario. È una cache: i
dati devono essere ricostruibili dal database.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 Gestione documentale base

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             5 --- Document Management
  architetturale    

  Profilo minimo    PMI (raccomandato) / Mid-Market (obbligatorio)

  Scelta primaria   Object storage (S3/Blob/MinIO) + metadati applicativi
                    su PostgreSQL

  Licenza           AGPL 3.0 (MinIO)
  -----------------------------------------------------------------------

Perché: Livello base sufficiente per allegati, versioning semplice,
ricerca per metadati. Nessun componente aggiuntivo da gestire.

Rischio noto: Nessuna full-text search nativa. No OCR. No workflow
documentale.

**Alternative:**

- Mayan EDMS (Apache 2.0) --- DMS leggero con OCR, workflow documentale
  dedicato, permessi granulari. Livello intermedio tra object storage e
  ECM enterprise.

⛔ Anti-pattern: Non implementare un DMS custom su PostgreSQL BLOB:
usare object storage per i binari e PostgreSQL per i metadati.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 ECM enterprise

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             5 --- Document Management
  architetturale    

  Profilo minimo    Enterprise (raccomandato --- se requisiti ECM
                    espliciti)

  Scelta primaria   Alfresco CE 23.x

  Licenza           LGPL 3.0
  -----------------------------------------------------------------------

Perché: Repository CMIS 1.1, versioning, full-text search (Solr), OCR
(Tika + Tesseract), protocollo informatico PA (CAD/DPCM). Unico ECM OSS
con maturità enterprise.

Rischio noto: Pesante: richiede Solr, Tika Server, Transform Engine.
Community Edition limitata rispetto a Enterprise.

**Alternative:**

- Nuxeo (Apache 2.0) --- Architettura più moderna (MongoDB/Elasticsearch
  backend). Meno adottato nella PA italiana.

**Scartati:**

- OpenKM: Community più piccola. Meno maturo di Alfresco.

⛔ Anti-pattern: Non installare Alfresco se servono solo allegati e
versioning. Object storage + metadati è 10x più semplice.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 Content extraction / OCR

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             5 --- Document Management
  architetturale    

  Profilo minimo    Mid-Market (opzionale) / Enterprise (raccomandato)

  Scelta primaria   Apache Tika 2.9.x

  Licenza           Apache 2.0
  -----------------------------------------------------------------------

Perché: Parsing ed estrazione metadati da 1.400+ formati. Tika Server
per pipeline AI.

Rischio noto: OCR richiede Tesseract aggiuntivo. Qualità OCR inferiore a
soluzioni commerciali.

⛔ Anti-pattern: Non usare Tika per OCR di alta qualità su documenti
scansionati di bassa qualità: valutare OCR cloud (Google Vision, Azure
Form Recognizer) come fallback.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 Integration layer (EIP)

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             6 --- Integration
  architetturale    

  Profilo minimo    Enterprise (obbligatorio) / Mid-Market (opzionale)

  Scelta primaria   Apache Camel 4.x LTS

  Licenza           Apache 2.0
  -----------------------------------------------------------------------

Perché: 300+ componenti EIP: SOAP, REST, JMS, JDBC, FTP, SAP RFC, file,
email. Integrazione nativa Spring Boot 3. Camel Quarkus per sidecar
leggeri.

Rischio noto: DSL proprietaria (anche se aperta). Curva di apprendimento
per pattern EIP.

**Scartati:**

- MuleSoft: Proprietario (Salesforce). Viola principio P1.

- Spring Integration: Meno componenti di Camel. Più adatto per
  integrazioni semplici.

⛔ Anti-pattern: Non usare Camel per integrazioni REST-to-REST semplici:
Axios/RestTemplate è sufficiente.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 Vector store / RAG

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             4 --- AI & Agentic
  architetturale    

  Profilo minimo    Enterprise (raccomandato) / Mid-Market (opzionale)

  Scelta primaria   Qdrant 1.x

  Licenza           Apache 2.0
  -----------------------------------------------------------------------

Perché: Scritto interamente in Rust con SIMD: performance superiori nei
benchmark. Ricerca ibrida nativa (dense + sparse): BM25, SPLADE++,
miniCOIL. Filtraggio one-stage durante traversal HNSW. Quantizzazione
fino a 64x. 25.000+ GitHub stars. SOC2 e HIPAA compliant. Deploy
on-prem/hybrid/edge.

Rischio noto: Più giovane di Weaviate. Meno documentazione per casi
d\'uso avanzati.

**Alternative:**

- Weaviate 1.25.x (BSD-3) --- Alternativa matura con buon ecosistema.
  BSD-3 è meno permissiva di Apache 2.0. Community più orientata al
  SaaS.

**Scartati:**

- Pinecone: SaaS proprietario. Lock-in. Viola principio P1.

- Chroma: Più semplice ma meno scalabile e meno feature enterprise.

⛔ Anti-pattern: Non introdurre un vector store se non esiste un caso
d\'uso RAG concreto. La ricerca full-text (Solr/OpenSearch) copre molti
scenari.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 LLM inference self-hosted

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             4 --- AI & Agentic
  architetturale    

  Profilo minimo    Enterprise (raccomandato) / Mid-Market (opzionale)

  Scelta primaria   Ollama 0.6.x

  Licenza           MIT
  -----------------------------------------------------------------------

Perché: LLM self-hosted (Llama 3.x, Mistral, Phi-3, Gemma 2). API REST
compatibile OpenAI. Dati nel perimetro organizzativo: nessun rischio
GDPR.

Rischio noto: Qualità inferenza inferiore a GPT-4/Claude su task
complessi.

**Alternative:**

- vLLM (Apache 2.0) --- Performance superiore per GPU multi-nodo
  (continuous batching, PagedAttention). Preferibile per cluster GPU
  enterprise.

**Scartati:**

- OpenAI API / Claude API: Dati inviate a terze parti. Problema
  GDPR/compliance per dati sensibili.

⛔ Anti-pattern: Non deployare Ollama su GPU se il volume di inferenze è
\< 100/giorno: usare API cloud con anonimizzazione dei dati.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 Agent orchestration

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             4 --- AI & Agentic
  architetturale    

  Profilo minimo    Enterprise (raccomandato)

  Scelta primaria   LangGraph 0.3.x

  Licenza           MIT
  -----------------------------------------------------------------------

Perché: Agenti AI con stato persistente tramite grafo orientato. Nodi di
interruzione per human-in-the-loop (AI Act Art. 14). Integrazione con
service task BPMN.

Rischio noto: Ecosistema LangChain/LangGraph evolve molto rapidamente.
Rischio di breaking changes.

**Alternative:**

- CrewAI (MIT) --- Agenti multi-ruolo. Più semplice per scenari con
  ruoli specializzati.

**Scartati:**

- AutoGPT: Troppo sperimentale per produzione enterprise.

⛔ Anti-pattern: Non usare agenti AI per automazioni che possono essere
espresse come regole Drools: le regole sono deterministiche, gli agenti
no.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 Process mining

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             7 --- Data & Process Mining
  architetturale    

  Profilo minimo    Enterprise (raccomandato)

  Scelta primaria   PM4Py 2.7.x

  Licenza           GPL/LGPL 3.0
  -----------------------------------------------------------------------

Perché: Alpha Miner, Inductive Miner, conformance checking, OCEL 2.0.
Event log su PostgreSQL + TimescaleDB.

Rischio noto: Licenza GPL può essere vincolante. Usare come servizio
isolato.

**Alternative:**

- Apache Superset 4.x (Apache 2.0) --- Dashboard BI e visualizzazione.
  Complementare a PM4Py per la visualizzazione dei risultati.

**Scartati:**

- Celonis: Proprietario, molto costoso. Decine di migliaia di euro/anno.

⛔ Anti-pattern: Non implementare process mining senza event log
strutturati dal BPM engine. Il log deve essere conforme a IEEE XES fin
dall\'origine.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 Service mesh

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             8 --- Infrastructure
  architetturale    

  Profilo minimo    Enterprise (raccomandato)

  Scelta primaria   Cilium 1.19.x

  Licenza           Apache 2.0 --- CNCF Graduated
  -----------------------------------------------------------------------

Perché: Basato su eBPF: nessun sidecar proxy, overhead risorse -15-25%
vs Istio. Unifica CNI + service mesh + observability + security. 24.000+
stars. Adottato da Google GKE, Azure AKS, AWS EKS, TikTok, Bloomberg.
mTLS trasparente, network policy avanzate, runtime security (Tetragon).

Rischio noto: Richiede kernel Linux 5.x+ con supporto eBPF. Non funziona
su Windows.

**Alternative:**

- Istio 1.22.x (Apache 2.0) --- Più maturo, più adottato storicamente.
  Preferibile se il team ha competenze Envoy consolidate. Overhead
  sidecar più alto.

**Scartati:**

- Linkerd: License cambiata a non-OSI nel 2024. Rischio lock-in.

- Consul Connect: HashiCorp ha cambiato licenza a BUSL. Viola principio
  P1.

⛔ Anti-pattern: Non installare un service mesh per un cluster \< 10
servizi. Network policy K8s native sono sufficienti.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 Container orchestration

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             8 --- Infrastructure
  architetturale    

  Profilo minimo    Mid-Market (raccomandato) / Enterprise (obbligatorio)

  Scelta primaria   Kubernetes 1.30+

  Licenza           Apache 2.0
  -----------------------------------------------------------------------

Perché: Standard de facto per orchestrazione container. HPA, rolling
update, namespace isolation.

Rischio noto: Complessità operativa significativa. Richiede almeno 1
DevOps dedicato.

**Alternative:**

- Container managed (Azure Container Apps / AWS ECS Fargate) --- Per
  PMI. Container senza gestione K8s. Semplicità operativa, 0 DevOps.

- OKD 4.x (Apache 2.0) --- Distribuzione community upstream di
  OpenShift. Console ricca, SCC securizzati. Preferibile se percorso
  verso OpenShift Enterprise.

⛔ Anti-pattern: Non usare K8s per un team senza competenze DevOps.
Container managed costa meno e funziona meglio per PMI.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 GitOps / CD

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             8 --- Infrastructure
  architetturale    

  Profilo minimo    Mid-Market (raccomandato) / Enterprise (obbligatorio)

  Scelta primaria   ArgoCD 2.11.x

  Licenza           Apache 2.0
  -----------------------------------------------------------------------

Perché: Deploy K8s dichiarativo da Git. Rollback, audit trail,
multi-cluster.

Rischio noto: Serve solo per deploy K8s. Per PMI senza K8s: CI/CD
tradizionale.

**Alternative:**

- Flux CD 2.x (Apache 2.0) --- Alternativa CNCF. Più gitops-oriented,
  meno UI.

⛔ Anti-pattern: Non fare deploy manuali in produzione. Anche senza
GitOps, usare almeno una pipeline CI/CD con promotion e rollback.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026

# 📦 Secrets management

  -----------------------------------------------------------------------
  **Campo**         **Valore**
  ----------------- -----------------------------------------------------
  Layer             8 --- Infrastructure
  architetturale    

  Profilo minimo    Enterprise (obbligatorio) / Mid-Market (raccomandato)

  Scelta primaria   OpenBao 0.3.x

  Licenza           Apache 2.0
  -----------------------------------------------------------------------

Perché: Fork Apache 2.0 di HashiCorp Vault (che ha cambiato licenza a
BUSL). PKI interna, cifratura PII, gestione segreti, dynamic secrets.

Rischio noto: Fork giovane (2024). Community in crescita ma più piccola
di Vault.

**Alternative:**

- HashiCorp Vault 1.16.x (BUSL 1.1) --- Più maturo ma licenza non
  OSI-approved. Accettabile in contesti dove P1 è meno prioritario.

**Scartati:**

- Kubernetes Secrets: Non cifrati by default. Non adatti per PII o PKI.

⛔ Anti-pattern: Non salvare segreti in environment variables o config
files in Git. Usare sempre un secrets manager.

Ultima verifica: aprile 2026 · Prossima verifica: ottobre 2026
