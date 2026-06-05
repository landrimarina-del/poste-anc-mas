**ARCHITETTURA BPM OPEN SOURCE**

Documento 1 di 3 --- Architettura di Riferimento

**Versione 1.0 --- Aprile 2026**

# Indice

- 1\. Scopo e destinatari

- 2\. Modello decisionale

- 3\. Architettura logica a layer

- 4\. Principi architetturali

- 5\. Requisiti non funzionali per profilo

- 6\. Framework di compliance

- 7\. Percorso di adozione incrementale

- 8\. Anti-pattern e scelte da evitare

# 1. Scopo e destinatari

## 1.1 Cosa è questo documento

Questo documento definisce l\'architettura di riferimento per una
piattaforma BPM basata su componenti open source. Contiene principi,
decisioni architetturali, requisiti non funzionali e percorsi di
adozione. Non contiene nomi di prodotto né versioni.

## 1.2 Cosa NON è

- Non è un manuale di implementazione: per le scelte tecnologiche
  consultare il Documento 2 (Catalogo Tecnologico).

- Non è un documento di sizing: per infrastruttura e costi consultare il
  Documento 3 (Sizing e Costi).

- Non è un capitolato di gara: è un riferimento per chi scrive
  capitolati.

## 1.3 A chi si rivolge

- Solution Architect e Tech Lead: per decisioni architetturali e
  selezione componenti.

- CTO e responsabili IT: per validare scelte strategiche e governance.

- Business Analyst e Product Owner: per comprendere capability e vincoli
  della piattaforma.

- Team commerciale: come riferimento per proposte tecniche e allegati di
  gara.

## 1.4 Come consultarlo

Il documento è modulare. Ogni sezione è autocontenuta. Si consiglia:

- Per una panoramica rapida: Sezione 2 (modello decisionale) + Sezione 4
  (principi).

- Per decisioni su un progetto specifico: Sezione 2 (profilo) → Sezione
  7 (percorso di adozione) → Documento 2 (catalogo).

- Per assessment di compliance: Sezione 6.

## 1.5 Relazione tra i 3 documenti

  ------------------------------------------------------------------------
  **Documento**   **Contenuto**             **Frequenza       **Durata
                                            aggiornamento**   utile**
  --------------- ------------------------- ----------------- ------------
  1 ---           Principi, layer,          Annuale o su      5-10 anni
  Architettura    decisioni, compliance,    cambio strategico 
                  percorso                                    

  2 --- Catalogo  Schede prodotto con       Semestrale        6-12 mesi
  Tecnologico     scelta, alternative,                        
                  scartati                                    

  3 --- Sizing e  Infrastruttura, prezzi    Semestrale o su   6-12 mesi
  Costi           cloud, TCO, licenze       variazione prezzi 
  ------------------------------------------------------------------------

# 2. Modello decisionale

La piattaforma si declina su tre profili organizzativi. Il profilo
determina quali capability architetturali sono obbligatorie,
raccomandate o opzionali.

## 2.1 Definizione dei profili

  -------------------------------------------------------------------------
  **Parametro**   **PMI**         **Mid-Market**    **Enterprise / PA**
  --------------- --------------- ----------------- -----------------------
  Utenti          50 -- 500       500 -- 5.000      5.000+

  Processi / mese ≤ 5.000         ≤ 100.000         Illimitati

  SLA             99,5%           99,5% -- 99,9%    ≥ 99,9%
  disponibilità                                     

  RTO             Best effort (\< \< 8h             \< 4h
                  24h)                              

  RPO             Best effort (\< \< 4h             \< 1h
                  24h)                              

  Modello deploy  Container       K8s               K8s multi-AZ + DR
                  managed         single-cluster    

  Team DevOps     0-1 (part-time) 1-2               3+

  Compliance      GDPR base       GDPR + AI Act     GDPR + AI Act + DORA
                                  possibile         

  Workflow        Opzionale       Frequente         Core business

  Integrazioni    Limitate        Moderate,         Elevate, multi-sistema
                                  asincrone         
  -------------------------------------------------------------------------

## 2.2 Criteri di selezione del profilo

Il profilo si determina in base alla variabile più restrittiva. Se
un\'organizzazione con 200 utenti ha requisiti DORA, opera sul profilo
Enterprise per la compliance anche se il volume è da PMI. La regola è:
il profilo è il massimo tra le dimensioni.

## 2.3 Matrice delle capability architetturali

Legenda: ● obbligatorio ◐ raccomandato ○ opzionale --- non applicabile

  ---------------------------------------------------------------------------------------------
  **Capability        **PMI**   **Mid-Mkt**   **Enterprise**   **Criterio decisionale chiave**
  architetturale**                                             
  ------------------- --------- ------------- ---------------- --------------------------------
  Framework           ●         ●             ●                Baseline comune a tutti i
  applicativo backend                                          profili

  Framework           ●         ●             ●                Baseline comune a tutti i
  applicativo                                                  profili
  frontend                                                     

  Database            ●         ●             ●                Persistenza dati; HA da
  transazionale                                                Mid-Market

  Container runtime   ●         ●             ●                Standard di packaging e deploy

  Osservabilità       ●         ●             ●                Governo operativo minimo
  (metriche, log,                                              
  trace)                                                       

  Pipeline CI/CD      ●         ●             ●                Build, test e deploy automatici

  Motore BPM /        ◐         ●             ●                Se processo è auditabile con
  workflow                                                     stati, SLA, timer

  Identity & Access   ◐         ●             ●                IAM corporate se disponibile;
  Management                                                   autonomo altrimenti

  Object storage      ◐         ●             ●                Allegati e documenti --- base di
                                                               ogni livello documentale

  Message broker      ○         ◐             ◐                Code reali, retry,
  (code applicative)                                           disaccoppiamento

  Event streaming     ---       ○             ●                Solo volumi elevati o
                                                               multi-dominio a eventi

  Cache distribuita   ○         ◐             ●                Sessioni, rate limiting,
                                                               performance

  API Gateway         ○         ◐             ●                Governance API, throttling,
                                                               policy centralizzate

  Rule engine         ○         ◐             ●                Regole business esplicite, DMN,
                                                               multi-legislazione

  Integration layer   ---       ○             ●                Connettori legacy, SOAP, SAP,
  (EIP)                                                        sistemi eterogenei

  Gestione            ---       ○             ◐                Solo requisiti ECM espliciti e
  documentale                                                  continuativi
  avanzata (ECM)                                               

  Content extraction  ---       ○             ◐                Parsing multi-formato,
  / OCR                                                        pre-processing AI

  LLM inference       ○         ○             ◐                Classificazione, estrazione,
  self-hosted                                                  assistenza --- via API per PMI

  Agent orchestration ---       ○             ◐                Agenti AI con stato,
                                                               human-in-the-loop

  Vector store / RAG  ---       ○             ◐                Ricerca semantica documentale

  Process mining      ---       ○             ◐                Discovery, conformance checking,
                                                               KPI

  Container           ---       ◐             ●                Container managed per PMI; K8s
  orchestration (K8s)                                          da Mid-Market

  GitOps / CD         ---       ◐             ●                Deploy K8s da Git, audit trail
  dichiarativo                                                 

  Service mesh        ---       ---           ◐                mTLS, canary, traffic management

  Secrets management  ○         ◐             ●                PKI interna, cifratura PII,
                                                               gestione segreti

  Chaos testing       ---       ---           ◐                Test resilienza --- richiesto da
                                                               DORA

  SBOM /              ○         ◐             ●                Supply chain security,
  vulnerability                                                compliance fornitori ICT
  scanning                                                     

  Durable execution   ---       ○             ○                Orchestrazioni tecniche: saga,
  engine                                                       retry con stato, long-running
  ---------------------------------------------------------------------------------------------

# 3. Architettura logica a layer

La piattaforma è organizzata in 8 layer funzionali con responsabilità
distinte. Ogni layer comunica esclusivamente con i layer adiacenti
tramite contratti espliciti. La sostituzione di un componente
all\'interno di un layer non impatta gli altri.

## 3.1 Mappa dei layer

  ----------------------------------------------------------------------------------
  **\#**   **Layer**        **Responsabilità**        **Contratto verso **Profilo
                                                      il layer          minimo**
                                                      superiore**       
  -------- ---------------- ------------------------- ----------------- ------------
  1        Presentation     Punto di contatto con     HTTP/REST + OIDC  PMI
                            l\'utente. Frontend       token             
                            multicanale (web, tablet,                   
                            mobile PWA). API Gateway                    
                            come reverse proxy,                         
                            autenticazione, rate                        
                            limiting.                                   

  2        Orchestration\   Esecuzione processi BPMN  REST API OpenAPI  PMI
           & BPM            2.0: ciclo di vita        3.1\              
                            istanze, task umani,      + eventi          
                            timer, SLA, eventi. Ogni  asincroni         
                            transizione di stato                        
                            genera un evento                            
                            consumabile.                                

  3        Decision\        Logica decisionale        REST API + DMN    Mid-Market
           & Rules          esternalizzata: regole    standard          
                            business, decision table                    
                            DMN, policy                                 
                            multi-legislazione.                         
                            Modificabili a runtime                      
                            senza rilasci.                              

  4        AI & Agentic     Classificazione           REST API\         Enterprise
                            documenti,                (compatibile      
                            pre-compilazione form,    OpenAI)           
                            chatbot (RAG), supporto                     
                            decisionale. Agenti                         
                            invocati come service                       
                            task BPMN con                               
                            human-in-the-loop                           
                            obbligatorio per                            
                            decisioni ad alto                           
                            rischio.                                    

  5        Document\        Ciclo di vita             CMIS 1.1 + REST   Mid-Market
           Management       documentale:              API               
                            acquisizione, estrazione                    
                            contenuto (OCR),                            
                            archiviazione,                              
                            versioning, ricerca                         
                            full-text, conformità                       
                            CMIS e protocollo                           
                            informatico CAD.                            

  6        Integration      Connessione con sistemi   REST/SOAP/JMS +   Mid-Market
                            esterni: pattern EIP      eventi            
                            (trasformazione, routing,                   
                            retry, dead letter). Il                     
                            message broker garantisce                   
                            disaccoppiamento                            
                            asincrono.                                  

  7        Data &\          Analisi event log:        Event log         Enterprise
           Process Mining   discovery processi reali, standard\         
                            conformance checking      (XES/OCEL 2.0)    
                            contro modelli attesi,                      
                            KPI, bottleneck, SLA                        
                            breach. Alimenta                            
                            dashboard BI.                               

  8        Infrastructure   Layer trasversale:        Kubernetes API\   PMI
                            orchestrazione container, + OpenTelemetry   
                            GitOps, service mesh                        
                            (mTLS), osservabilità                       
                            (metriche, log, tracing),                   
                            gestione segreti, CI/CD.                    
  ----------------------------------------------------------------------------------

## 3.2 Flusso di interazione tipico

Il percorso di una richiesta utente attraverso i layer:

- 1\. L\'utente interagisce con il Layer 1 (Presentation): compila un
  form, avvia un processo, completa un task.

- 2\. Il Layer 1 inoltra la richiesta tramite API Gateway al Layer 2
  (BPM), che avvia o avanza l\'istanza.

- 3\. Il Layer 2 può invocare il Layer 3 (Rules) per valutare condizioni
  a un gateway BPMN.

- 4\. Il Layer 2 può invocare il Layer 4 (AI) per classificazione,
  estrazione dati o supporto decisionale.

- 5\. Il Layer 2 interagisce con il Layer 5 (Document) per acquisire o
  recuperare documenti.

- 6\. Il Layer 6 (Integration) viene invocato per interazioni con
  sistemi esterni (ERP, CRM, legacy).

- 7\. Ogni transizione di stato del Layer 2 genera un evento che il
  Layer 7 (Process Mining) consuma.

- 8\. Il Layer 8 (Infrastructure) sostiene tutti i layer:
  orchestrazione, osservabilità, sicurezza.

## 3.3 Principio di attivazione progressiva

Non tutti i layer devono essere attivati contemporaneamente:

- PMI: Layer 1 + 2 + 8 --- frontend, BPM base e infrastruttura.
  Sufficiente per la maggior parte dei casi.

- PMI evoluto: + Layer 5 (documentale leggero) e Layer 6 (integrazione
  base).

- Mid-Market: tutti tranne Layer 4 (AI) e Layer 7 (Process Mining), che
  restano opzionali.

- Enterprise / PA: tutti gli 8 layer attivi, con HA, multi-AZ e
  compliance piena.

## 3.4 Contratti tra layer

Ogni interazione tra layer avviene tramite contratti espliciti:

- API REST: contratti OpenAPI 3.1, versionati, repository GitOps,
  generazione tipi automatica.

- Eventi asincroni: contratti AsyncAPI, schema registry, topic
  versionati.

- Autenticazione: OIDC end-to-end, Authorization Code + PKCE per canali
  web, JWT validato al gateway.

- Tracing: propagazione contesto OpenTelemetry dal frontend al backend e
  ai broker.

## 3.5 Pattern architetturali per profilo

  --------------------------------------------------------------------------
  **Pattern**      **PMI**           **Mid-Market**      **Enterprise**
  ---------------- ----------------- ------------------- -------------------
  Modello          Modular monolith  Few services        Microservizi
  applicativo                                            selettivi

  Comunicazione    REST sincrono     REST + code         REST + event
                                     asincrone           streaming

  Orchestrazione   BPM engine        BPM engine dedicato BPM engine
  processi         embedded                              cluster + HPA

  Documentale      Object storage +  DMS leggero         ECM completo
                   metadati                              

  Deploy           Docker Compose /  K8s single-cluster  K8s multi-AZ + DR
                   managed                               

  Resilienza       Backup + 2        HA locale +         Multi-AZ + chaos
                   repliche          failover            testing
  --------------------------------------------------------------------------

# 4. Principi architetturali

9 principi governano tutte le decisioni. Ogni principio include un
trade-off esplicito: ciò che si guadagna e ciò che si accetta di
perdere.

## P1 --- No vendor lock-in

Enunciato: Adozione esclusiva di tecnologie open source con licenze
OSI-approved e standard aperti (OpenAPI, AsyncAPI, OIDC, CMIS, BPMN 2.0,
DMN). Ogni componente deve essere sostituibile senza riscrittura.

Benefici: Autonomia decisionale, portabilità, negoziazione con i
fornitori, competenze reperibili sul mercato.

Trade-off: Alcune feature enterprise non disponibili nella versione OSS.
Nessun supporto commerciale incluso: va acquistato separatamente se
necessario.

Implicazioni: Preferire Apache 2.0 / MIT. Evitare BUSL, licenze con
restrizioni commerciali, componenti con feature-gating significativo tra
versione free e a pagamento.

## P2 --- Cloud-native & modulare

Enunciato: Container come unità di deploy, indipendenza dal cloud
provider, modularità per bounded context. Partire semplice (Docker
Compose) e crescere verso K8s senza rifondare.

Benefici: Elasticità, portabilità multi-cloud, evoluzione incrementale.

Trade-off: Curva di apprendimento più alta rispetto a deploy
tradizionale VM. Container e K8s richiedono competenze specifiche nel
team.

Implicazioni: Bounded context, naming coerente con il business. Ogni
modulo ha API e schema separati.

## P3 --- Security by Zero Trust

Enunciato: Nessuna fiducia implicita: ogni interazione autenticata,
autorizzata e cifrata. GDPR, AI Act e DORA sono requisiti strutturali,
non verifiche a valle.

Benefici: Superficie di attacco ridotta, auditabilità completa,
conformità strutturale.

Trade-off: Overhead di autenticazione su ogni chiamata interna.
Complessità di gestione certificati e policy. \~5-10% latenza aggiuntiva
per mTLS inter-servizio.

Implicazioni: mTLS tra servizi, policy-as-code, secret management
centralizzato, JWT su ogni API, SAST/DAST in CI.

## P4 --- API-first & event-driven

Enunciato: Integrazione fondata su contratti espliciti e
disaccoppiamento. API versionati, broker ed eventi per asincronia.
Contract-first: la specifica precede l\'implementazione.

Benefici: Integrazione rapida, indipendenza dei team, evoluzione
parallela frontend/backend.

Trade-off: Tooling aggiuntivo (schema registry, generazione codice).
Debugging asincrono più complesso. Richiede disciplina contrattuale nel
team.

Implicazioni: OpenAPI 3.1, AsyncAPI, type-safety end-to-end, generazione
automatica tipi e client.

## P5 --- Observability by design

Enunciato: Logging strutturato, metriche, tracing distribuito e alerting
non sono accessori ma condizioni necessarie per governare SLA e
diagnosticare anomalie.

Benefici: Governo operativo, riduzione tempi diagnosi (MTTR), SLA
dimostrabili.

Trade-off: 10-15% di risorse infrastrutturali dedicate. Storage
aggiuntivo per log e metriche. Complessità di correlazione in
architetture distribuite.

Implicazioni: Tre pilastri correlati tramite trace ID: metriche, log
strutturati, tracing distribuito.

## P6 --- AI-ready, not AI-first

Enunciato: Lo stack è predisposto all\'AI (API, dati strutturati, vector
store) ma l\'AI non è nella baseline. Si introduce quando esiste un caso
d\'uso concreto e misurabile.

Benefici: Piattaforma pronta per AI senza dipendenza. Nessun costo AI se
non serve.

Trade-off: Benefici AI ritardati rispetto a chi li integra subito.
Rischio di \"predisporre troppo\" senza mai attivare.

Implicazioni: Data-readiness, API compatibili, slot architetturale
riservato nel Layer 4.

## P7 --- UX multicanale & accessibile

Enunciato: Esperienza coerente desktop/tablet/mobile con approccio
mobile-first e PWA. Accessibilità WCAG 2.1 AA nativa nel design system
(obbligatoria per PA).

Benefici: Maggiore adozione utenti, compliance accessibilità, esperienza
uniforme.

Trade-off: Maggiore effort frontend rispetto a UI solo desktop. Test
cross-device e accessibilità aggiungono \~15-20% al ciclo di QA.

Implicazioni: Design system coerente, componenti ARIA-compliant,
breakpoint mobile-first.

## P8 --- Scalabilità progressiva

Enunciato: L\'architettura deve partire semplice per PMI e crescere
verso Enterprise senza riscrivere. Ogni componente opzionale si aggiunge
senza impattare quelli esistenti.

Benefici: Time-to-market breve per il primo rilascio. Nessun big-bang
architetturale.

Trade-off: Alcune ottimizzazioni enterprise (sharding, multi-tenancy)
vanno pianificate, non implementate, fin dall\'inizio per evitare
blocchi futuri.

Implicazioni: Modular monolith → few services → microservizi selettivi.
Upgrade progressivo del deploy.

## P9 --- DevOps & rilascio continuo

Enunciato: Pipeline CI/CD, IaC, GitOps, rollback automatico come parte
integrante dell\'architettura, non come postfazione.

Benefici: Rilasci affidabili, time-to-market breve, riduzione rischio
operativo.

Trade-off: Investimento iniziale in pipeline e automazione (\~3-4
settimane di setup). Richiede cultura DevOps nel team, non solo
strumenti.

Implicazioni: Build automatiche, quality gate, vulnerability scan,
environment promotion, audit trail dei deploy.

# 5. Requisiti non funzionali per profilo

  --------------------------------------------------------------------------------
  **Requisito**            **PMI**         **Mid-Market**    **Enterprise / PA**
  ------------------------ --------------- ----------------- ---------------------
  Disponibilità            99,5%           99,5% -- 99,9%    ≥ 99,9%

  RTO                      Best effort (\< \< 8h             \< 4h (DORA)
                           24h)                              

  RPO                      Best effort (\< \< 4h             \< 1h (DORA)
                           24h)                              

  Latenza API (p95)        \< 500ms        \< 300ms          \< 200ms

  Concorrenza              50-100 sessioni 500-1.000         5.000+ sessioni
                                           sessioni          

  Accessibilità            Best effort     WCAG 2.1 A        WCAG 2.1 AA (obbligo
                                                             PA)

  Internazionalizzazione   Italiano        IT + EN           Multi-lingua +
                                                             multi-legislazione

  Backup                   Giornaliero     Ogni 4h           Ogni 1h + replica
                                                             sincrona

  Restore testato          Semestrale      Trimestrale       Mensile (DORA)

  Deploy zero-downtime     Non richiesto   Rolling update    Canary + blue/green

  Scanning vulnerabilità   Manuale         Ad ogni build CI  Ad ogni build + SBOM
                                                             obbligatorio

  Penetration test         Opzionale       Annuale           Annuale + DORA test
                                                             resilienza
  --------------------------------------------------------------------------------

# 6. Framework di compliance

Questa sezione traduce i requisiti normativi in capability
architetturali richieste. Le scelte tecnologiche specifiche sono nel
Documento 2.

## 6.1 Applicabilità per profilo

  ----------------------------------------------------------------------------------
  **Normativa**     **PMI**         **Mid-Market**             **Enterprise / PA**
  ----------------- --------------- -------------------------- ---------------------
  GDPR (UE          Sempre          Sempre                     Sempre
  2016/679)                                                    

  AI Act (UE        Solo se AI      Probabile                  Sempre (se AI
  2024/1689)        attivata                                   presente)

  DORA (UE          Solo se settore Se                         Sempre per enti
  2022/2554)        finanziario     finanziario/assicurativo   regolati

  CAD + linee guida Se PA           Se PA                      Se PA
  AgID                                                         
  ----------------------------------------------------------------------------------

## 6.2 GDPR → Capability architetturali

  -----------------------------------------------------------------------
  **Requisito       **Capability architetturale richiesta**
  GDPR**            
  ----------------- -----------------------------------------------------
  Art. 17 ---       API di data subject request con cancellazione a
  Diritto alla      cascata. Log immutabile. Form di richiesta nell\'area
  cancellazione     personale.

  Art. 25 ---       Raccolta minima nei form. Nessun dato personale in
  Privacy by design storage client-side. No tracking analytics terze
                    parti.

  Art. 32 ---       Cifratura at-rest (AES-256) e in-transit (TLS 1.3).
  Sicurezza         Secret management per PII. HSTS obbligatorio.
  trattamento       

  Art. 30 ---       Generazione automatica del registro dai modelli di
  Registro          processo e definizioni form.
  trattamenti       

  Art. 35 --- DPIA  Dashboard per il DPO con accesso ai log di audit e ai
                    trattamenti.
  -----------------------------------------------------------------------

## 6.3 AI Act → Capability architetturali

  -----------------------------------------------------------------------
  **Requisito AI    **Capability architetturale richiesta**
  Act**             
  ----------------- -----------------------------------------------------
  Art. 9 ---        Registro dei rischi: scheda tecnica per ogni modello
  Gestione rischi   AI deployato.

  Art. 13 ---       Trace di attivazione regole e decisioni AI esposta
  Spiegabilità      nel pannello task del frontend.

  Art. 14 ---       Human-in-the-loop obbligatorio per decisioni ad alto
  Supervisione      rischio: nodo di interruzione negli agent workflow.
  umana             

  Art. 15 ---       Monitoraggio drift modelli ML. Alert automatico su
  Robustezza        degradazione performance.
  -----------------------------------------------------------------------

## 6.4 DORA → Capability architetturali

  -----------------------------------------------------------------------
  **Requisito       **Capability architetturale richiesta**
  DORA**            
  ----------------- -----------------------------------------------------
  Art. 9.4 ---      Ogni deploy tracciato con autore, approvazione e
  Change management rollback. GitOps obbligatorio.

  Art. 11 ---       RTO \< 4h, RPO \< 1h. Database HA, replica messaggi,
  Continuità        backup con retention ≥ 90 gg.
  operativa         

  Art. 19 ---       Alerting → ticket automatico → post-mortem
  Incident          documentato.
  management        

  Art. 25 --- Test  Chaos testing trimestrale: simulazione failure nodi,
  resilienza        latenza, perdita messaggi.

  Art. 28 ---       SBOM generato e scansionato ad ogni build CI.
  Fornitori ICT     Tracciabilità dipendenze.
  -----------------------------------------------------------------------

# 7. Percorso di adozione incrementale

4 step autocontenuti. Ogni step produce una piattaforma funzionante e
verificabile. Il passaggio al successivo è giustificato da un criterio
oggettivo, non da una timeline.

## Step 1 --- Piattaforma base

**Capability attivate:**

- Framework backend + frontend + database + container + CI/CD +
  osservabilità base.

- Autenticazione OIDC, API REST con contratti OpenAPI.

**Risultato: applicazione web funzionante con deploy automatico e
monitoring.**

**Criterio di passaggio allo Step 2:**

- Il business richiede un workflow con stati espliciti, approvazioni,
  timer, SLA o audit trail.

## Step 2 --- Aggiunta BPM e IAM

**Capability attivate:**

- \+ Motore BPM con BPMN 2.0, human task, worklist, form engine
  dinamico.

- \+ IAM autonomo (se non disponibile IAM corporate).

- \+ Object storage per documenti base.

**Risultato: piattaforma process-driven con workflow modellati, task
assegnati e tracciabilità.**

**Criterio di passaggio allo Step 3:**

- Presenza di integrazioni asincrone, code reali, cache distribuita, o
  governance API centralizzata.

## Step 3 --- Asincronia, integrazione, regole

**Capability attivate:**

- \+ Message broker e/o event streaming per disaccoppiamento e retry.

- \+ Cache distribuita per sessioni e performance.

- \+ API Gateway per governance, throttling, policy centralizzate.

- \+ Rule engine per regole business esternalizzate (se dominio
  complesso).

- \+ Integration layer per connettori legacy (se presenti).

**Risultato: piattaforma integrata con sistemi terzi, asincrona, con
regole governabili.**

**Criterio di passaggio allo Step 4:**

- Casi d\'uso AI concreti e misurabili, oppure volumi che richiedono
  process mining.

## Step 4 --- AI, analytics, compliance avanzata

**Capability attivate:**

- \+ LLM inference (classificazione, estrazione, chatbot RAG).

- \+ Agent orchestration con human-in-the-loop.

- \+ Vector store per ricerca semantica documentale.

- \+ Process mining: discovery, conformance checking, KPI dashboard.

- \+ Secrets management avanzato, chaos testing, SBOM.

**Risultato: piattaforma completa con AI, analytics e compliance DORA/AI
Act.**

# 8. Anti-pattern e scelte da evitare

Errori ricorrenti nella progettazione di piattaforme BPM open source:

## ⛔ Kubernetes per un team senza DevOps

Regola: Kubernetes richiede almeno 1 DevOps dedicato. Per PMI con team
\< 3 persone: container managed (Container Apps, ECS Fargate) è
sufficiente e più sostenibile.

Rischio se ignorato: Team PMI sovraccaricato dalla gestione
infrastrutturale, release bloccati.

## ⛔ Kafka quando bastano le code

Regola: Kafka ha senso per event streaming multi-consumatore ad alto
volume. Per disaccoppiamento e retry: un message broker tradizionale è
10x più semplice da operare.

Rischio se ignorato: Infrastruttura sovra-dimensionata, competenze non
disponibili, tempi di delivery estesi.

## ⛔ BPM engine per sostituire uno scheduler

Regola: Un motore BPMN serve per processi con stati, human task,
eccezioni e audit. Per job schedulati e retry: lo scheduler applicativo
del framework è sufficiente.

Rischio se ignorato: Complessità ingiustificata per operazioni batch.

## ⛔ ECM enterprise senza requisiti documentali

Regola: Un sistema ECM completo (repository, CMIS, fascicoli, OCR) serve
solo con requisiti documentali forti. Per allegati semplici: object
storage + metadati applicativi.

Rischio se ignorato: Costi infrastrutturali e di gestione sproporzionati
per esigenze base.

## ⛔ AI nella baseline senza caso d\'uso

Regola: Predisporre l\'architettura per l\'AI (P6) non significa
deployare LLM, vector store e agent framework dallo Step 1. L\'AI si
introduce allo Step 4 con un caso d\'uso concreto.

Rischio se ignorato: GPU allocate senza utilizzo, costi cloud elevati,
competenze non formate.

## ⛔ Service mesh per cluster \< 10 servizi

Regola: Il service mesh aggiunge mTLS e traffic management ma con
overhead significativo. Per cluster con pochi servizi: network policy
Kubernetes native sono sufficienti.

Rischio se ignorato: Overhead risorse 15-25%, complessità di debug,
formazione aggiuntiva.

## ⛔ Componente non verificato da oltre 12 mesi

Regola: L\'ecosistema open source evolve rapidamente. Un componente
raccomandato oggi può essere deprecato tra 6 mesi (esempio: Camunda 7 CE
archiviato nov 2025). Il Documento 2 ha date di verifica per questo.

Rischio se ignorato: Lock-in involontario su progetto abbandonato,
vulnerabilità non patchate.

## ⛔ Microservizi dallo Sprint 1

Regola: Partire con microservizi moltiplica la complessità senza
beneficio per i volumi PMI/Mid-Market. Il modello consigliato (P8) è:
modular monolith → few services → microservizi selettivi.

Rischio se ignorato: Delivery lento, debugging distribuito complesso,
overhead di comunicazione inter-servizio.
