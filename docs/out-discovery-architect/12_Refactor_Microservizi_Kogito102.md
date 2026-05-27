# 12 - Proposta di Refactor a Microservizi — Kogito 10.2 + MariaDB

> **Scope**: proposta architetturale per l'evoluzione di `apps/kogito` da modular monolith a microservizi.  
> **Profilo target**: architettura few-services eseguibile su orchestratore container (k3s / Kubernetes).  
> **Invarianti funzionali**: i bounded context, i contratti API verso il frontend, i workflow BPMN, gli stati pratica e le regole DMN restano invariati rispetto ai documenti `01`–`10`.  
> **Non è un ridisegno funzionale**: nessuna capability nuova, nessun workflow modificato.

---

## 1. Principi guida del refactor

| # | Principio | Applicazione concreta |
|---|---|---|
| P-R1 | **Confini = Bounded Context** | Ogni microservizio corrisponde a uno e un solo BC dell'architettura applicativa (doc `01`). Nessuna aggregazione o scissione dei BC. |
| P-R2 | **Contratti API invarianti** | Le API REST verso il frontend (`/api/v1/...`) restano identiche. Il frontend React non richiede modifiche. |
| P-R3 | **Runtime-neutral** | Le classi di dominio (`application/`, `domain/`) non dipendono da classi Spring o Quarkus. Il binding al framework è confinato a `api/` e `config/`. Lo swap Spring Boot → Quarkus richiede solo sostituzione del modulo di bootstrap e delle dipendenze BOM nel `pom.xml`. |
| P-R4 | **Persistenza isolata per servizio** | Ogni microservizio ha il proprio schema MariaDB. Nessuna JOIN cross-servizio sul DB. Query aggregate tramite API o viste pubblicate. |
| P-R5 | **Kogito 10.2 standalone** | Kogito 10.2 adotta il modello operator-based: Data Index e Job Service sono processi separati, non embedded. Il `workflow-service` è l'unico modulo con dipendenza da `kogito-processes-spring-boot-starter 10.2`. |
| P-R6 | **Progressione senza rifondare** | Il refactor segue la sequenza: (1) separazione in moduli Maven distinti, (2) deploy su docker-compose, (3) manifest Kubernetes per k3s. Ogni fase è validabile autonomamente. |

---

## 2. Mapping Bounded Context → Microservizio

| Bounded Context (doc `01`) | Microservizio | Package attuale migrato |
|---|---|---|
| BC1 – Practice Management | **practice-service** | `it.poste.anc.practice` |
| BC2 – Workflow / Task Orchestration | **workflow-service** | `it.poste.anc.workflow` |
| BC3 – Document & Checklist | **document-service** | `it.poste.anc.document` |
| BC4 – BPM Integration Gateway | **bpmgw-service** | `it.poste.anc.bpmgw` |
| BC5 – Supervision & Reporting | **supervision-service** | `it.poste.anc.supervision` |
| BC6 – Signal Management | **signals-service** | `it.poste.anc.signals` |
| Trasversale – IAM / Security | **auth-service** (o sidecar IAM) | `it.poste.anc.shared.security` |
| Trasversale – Shared contracts | **anc-commons** (libreria, non servizio) | `it.poste.anc.shared.common` |

---

## 3. Stack tecnologico target

### 3.1 Backend per microservizio

| Layer | Tecnologia | Versione | Note |
|---|---|---|---|
| JVM | OpenJDK | 21 LTS | invariato |
| Framework applicativo | Spring Boot | 3.4.x | compatibile Kogito 10.2 BOM |
| BPM Engine | Kogito processes | 10.2.x | solo in `workflow-service` |
| Decisioni DMN | Kogito DMN | 10.2.x | solo in `document-service` (calcolo esito) |
| Persistence ORM | Spring Data JPA / Hibernate | (BOM SB 3.4) | per tutti i servizi tranne `workflow-service` |
| Database | MariaDB | 10.11 LTS | schema dedicato per servizio |
| DB Migration | Flyway | (BOM SB 3.4) | init container in k3s |
| Object Storage | AWS SDK S3 v2 + MinIO | 2.25.x | solo `document-service` |
| Retry outbound | spring-retry | (BOM SB 3.4) | `bpmgw-service` |
| Excel export | Apache POI (poi-ooxml) | 5.2.x | `practice-service` |
| API Docs | springdoc-openapi-starter-webmvc-ui | 2.6.x | tutti i servizi |
| Validazione | spring-boot-starter-validation | (BOM) | tutti i servizi |
| Monitoring | spring-boot-starter-actuator | (BOM) | tutti i servizi |

### 3.2 Kogito 10.2 — Componenti infrastrutturali separati

| Componente | Ruolo | Dipendenza di `workflow-service` |
|---|---|---|
| **Kogito Data Index** | Persistenza e query su istanze di processo e task (GraphQL API) | client GraphQL generato da schema |
| **Kogito Job Service** | Gestione timer BPMN (scadenze, deadlines) | HTTP callback verso `workflow-service` |
| **Kogito Maven Plugin 10.2** | Code generation BPMN/DMN → Java a compile-time | plugin Maven, nessun runtime aggiunto |

### 3.3 Neutralità Spring Boot / Quarkus

Per abilitare lo swap, le seguenti regole devono essere rispettate nel codice:

1. **Classi `application/` e `domain/`**: nessuna annotazione `@Service`, `@Component`, `@Repository` Spring. Usare costruttori espliciti o CDI (`jakarta.inject.@Inject`).
2. **Repositories**: interfacce `jakarta.persistence` + JPA standard. `@Repository` Spring confinato nell'implementazione infrastrutturale.
3. **Config**: ogni microservizio espone un `*Config` record con i parametri di configurazione. Implementazioni:
   - Spring Boot: `@ConfigurationProperties`
   - Quarkus: `@ConfigProperty`
4. **BOM dipendenze**: il BOM Kogito 10.2 è condiviso tra Spring Boot e Quarkus. La sostituzione richiede di cambiare:
   - `kogito-processes-spring-boot-starter` → `kogito-quarkus-processes`
   - `spring-boot-starter-*` → dipendenze Quarkus equivalenti
   - nessuna modifica alla logica di dominio.

---

## 4. Struttura Maven Multi-Module

```
apps/kogito/
│
├── anc-parent/                        ← BOM parent
│   └── pom.xml                           Kogito 10.2 BOM + Spring Boot 3.4 BOM
│                                         gestione versioni centralizzata
│
├── anc-commons/                       ← Libreria condivisa (non deployata autonomamente)
│   └── src/main/java/it/poste/anc/shared/
│       ├── common/                       ApiResponse, GlobalExceptionHandler
│       ├── security/                     AppUser, Role, UserGroup (contratti, non impl)
│       └── events/                       domini event types (PracticeOpened, ecc.)
│
├── practice-service/                  ← BC1
│   ├── src/main/java/it/poste/anc/practice/
│   │   ├── api/                          PracticeController, DTO
│   │   ├── application/                  PracticeQueryService
│   │   └── persistence/                  JPA entities, repositories
│   └── pom.xml                           dep: anc-commons, data-jpa, poi-ooxml, springdoc
│
├── document-service/                  ← BC3
│   ├── src/main/java/it/poste/anc/document/
│   │   ├── api/                          Intake, Checklist, Attachment, CaseNote controllers
│   │   ├── application/                  IntakeChecklistService, OutcomeDmnService, ecc.
│   │   ├── ingestion/                    AttachmentFetcher, AttachmentStorage
│   │   └── persistence/
│   └── pom.xml                           dep: anc-commons, kogito-dmn 10.2, s3/minio, poi
│
├── workflow-service/                  ← BC2 — unico con Kogito BPM engine
│   ├── src/main/
│   │   ├── java/it/poste/anc/workflow/
│   │   │   ├── api/                      TaskController, ManagementProcessController, ecc.
│   │   │   ├── application/              TaskManagementService, UserTaskFilterService
│   │   │   └── engine/                   BpmEngineAdapter, KogitoBpmEngineAdapter (aggiornato 10.2)
│   │   └── resources/processes/
│   │       ├── anc_main.bpmn             (invariato)
│   │       ├── anc_intake.bpmn           (invariato)
│   │       └── anc_signal.bpmn           (invariato)
│   └── pom.xml                           dep: anc-commons, kogito-processes-spring-boot-starter 10.2
│
├── bpmgw-service/                     ← BC4
│   ├── src/main/java/it/poste/anc/bpmgw/
│   │   ├── inbound/                      BpmPracticeInboundController, BpmOutcomeAckController
│   │   └── outbound/                     BpmOutboundService, HttpBpmOutcomeOutboundGateway
│   └── pom.xml                           dep: anc-commons, spring-retry, springdoc
│
├── supervision-service/               ← BC5
│   ├── src/main/java/it/poste/anc/supervision/
│   │   ├── api/                          SupervisionDashboardController, SupervisionTaskController
│   │   └── application/                  SupervisionDashboardService, SupervisionTaskService
│   └── pom.xml                           dep: anc-commons, data-jpa, springdoc
│
├── signals-service/                   ← BC6
│   ├── src/main/java/it/poste/anc/signals/
│   │   ├── api/                          SignalController
│   │   ├── application/                  SignalService, SinergiaStubGateway
│   │   └── persistence/
│   └── pom.xml                           dep: anc-commons, data-jpa, springdoc
│
├── auth-service/                      ← IAM trasversale
│   ├── src/main/java/it/poste/anc/auth/
│   │   ├── api/                          AuthController
│   │   └── security/                     AppUserDetailsService, SecurityConfig, BpmInboundApiKeyFilter
│   └── pom.xml                           dep: anc-commons, spring-security, data-jpa
│
├── frontend/                          ← React SPA (invariata)
│   └── src/                              nessuna modifica richiesta
│
├── k8s/                               ← Manifest Kubernetes (fase k3s)
│   ├── namespaces.yaml
│   ├── infra/
│   │   ├── mariadb.yaml
│   │   └── minio.yaml
│   ├── kogito/
│   │   ├── data-index.yaml
│   │   └── job-service.yaml
│   └── services/
│       ├── practice-service/
│       ├── document-service/
│       ├── workflow-service/
│       ├── bpmgw-service/
│       ├── supervision-service/
│       ├── signals-service/
│       └── auth-service/
│
├── docker-compose.yml                 ← Fase 1: sviluppo e integration test
└── k3s-setup.sh                       ← Fase 2: bootstrap cluster locale
```

---

## 5. Schema di Persistenza per Servizio (MariaDB)

| Microservizio | Schema MariaDB | Tabelle principali |
|---|---|---|
| `practice-service` | `anc_practice` | `practice`, `practice_state_history`, `client_data`, `card_data`, `related_action`, `audit_event` |
| `document-service` | `anc_document` | `attachment`, `checklist_response`, `checklist_help_text`, `practice_outcome`, `case_note` |
| `workflow-service` | `anc_workflow` (+ Kogito Data Index su schema proprio) | `user_task_filter` + tabelle generate da Kogito: `process_instance`, `user_task_instance` |
| `bpmgw-service` | `anc_bpmgw` | `bpm_inbound_message`, `bpm_outbound_message` |
| `supervision-service` | (viste su `anc_practice` tramite API, no schema proprio) | — read-only via API practice-service |
| `signals-service` | `anc_signals` | `signal`, `signal_state_history` |
| `auth-service` | `anc_auth` | `app_user`, `user_group`, `user_role` |

> **Regola**: `supervision-service` non accede direttamente alle tabelle di `practice-service`. Interroga `practice-service` via REST API dedicata o tramite viste di aggregazione pubblicate da `practice-service` su endpoint `/api/v1/supervision/...`.

---

## 6. Comunicazione inter-servizio

### 6.1 Canali sincroni (REST)

| Chiamante | Destinatario | Scopo | Endpoint |
|---|---|---|---|
| `bpmgw-service` | `practice-service` | apertura pratica + cambio stato | `POST /internal/practices` |
| `bpmgw-service` | `workflow-service` | avvio processo `anc.main` | `POST /internal/workflow/start` |
| `workflow-service` | `document-service` | recupero esito checklist | `GET /internal/document/practices/{id}/outcome` |
| `workflow-service` | `bpmgw-service` | invio esito a BPM esterno | `POST /internal/bpmgw/outcome` |
| `supervision-service` | `practice-service` | KPI aggregati | `GET /internal/practices/aggregates` |
| `supervision-service` | `workflow-service` | lista task per riassegnazione | `GET /internal/workflow/tasks` |

> Gli endpoint `/internal/...` sono separati dai path `/api/v1/...` esposti al frontend. Non sono esposti dall'API Gateway verso l'esterno.

### 6.2 Canali asincroni (POC: HTTP callback; target: Kafka + CloudEvents)

| Evento | Produttore | Consumatore | Payload |
|---|---|---|---|
| `PracticeOpened` | `bpmgw-service` | `practice-service`, `workflow-service` | `requestId`, `idWorkItem`, dati pratica |
| `OutcomeAckReceived` | `bpmgw-service` | `practice-service`, `workflow-service` | `requestId`, `ackCode` (OK/KO) |
| `TaskCompleted` | `workflow-service` | `practice-service` (cambio stato) | `taskId`, `requestId`, `outcome` |

In POC: callback HTTP sincrono con retry (`spring-retry`). La struttura dei payload (CloudEvents schema) è invariante rispetto alla sostituzione del canale.

---

## 7. Aggiornamento Kogito 2.44 → 10.2 per `workflow-service`

### 7.1 Differenze impattanti

| Aspetto | Kogito 2.44 (attuale) | Kogito 10.2 (target) |
|---|---|---|
| Persistence engine | `kogito-addons-springboot-persistence-jdbc` embedded | **Data Index** separato (schema `kogito_dataindex` su MariaDB) |
| Query istanze/task | In-process via `ProcessInstances` API | **Data Index GraphQL API** (`/graphql`) |
| Timer / deadline | In-process | **Job Service** separato |
| Human Task API | REST generata da plugin sul path `/anc_main/{id}/task.typeAndChecklist` | stessa generazione da plugin 10.2, path invariati |
| Maven plugin | `kogito-maven-plugin 2.44` | `kogito-maven-plugin 10.2` (stessa sintassi BPMN) |
| Spring Boot starter | `kogito-processes-spring-boot-starter 2.44` | `kogito-processes-spring-boot-starter 10.2` |

### 7.2 Classi da aggiornare in `workflow-service`

| Classe | Modifica richiesta |
|---|---|
| `KogitoBpmEngineAdapter` | Aggiornare le chiamate di query su istanze di processo: da API in-process a client GraphQL Data Index |
| `KogitoDataSourceConfig` | Configurare il DataSource separato per schema Kogito (`anc_workflow`) distinto dal DataSource applicativo |
| `BpmEngineAdapter` (interfaccia) | Invariata — è già un'interfaccia; le implementazioni cambiano, non il contratto |
| `DataIndexEventPublisher` | Adattare al modello eventi Kogito 10.2 (CloudEvents over HTTP) |

### 7.3 File BPMN/DMN — nessuna modifica

I file `anc_main.bpmn`, `anc_intake.bpmn`, `anc_signal.bpmn`, `anc_outcome_carta.dmn`, `anc_outcome_verbale.dmn` sono **invariati**: il formato BPMN 2.0 e DMN 1.3 è supportato senza modifiche da Kogito 10.2.

---

## 8. Sequenza di refactor raccomandata

### Fase 1 — Separazione moduli Maven (senza cambiare runtime)

**Obiettivo**: trasformare il monolite in moduli Maven distinti, ciascuno deployabile autonomamente, mantenendo il docker-compose attuale.

1. Creare `anc-parent/pom.xml` con BOM Kogito 10.2 + Spring Boot 3.4.
2. Creare `anc-commons/` estraendo `shared/common`, `shared/security` (contratti), event types.
3. Creare un modulo Maven per ciascun bounded context, spostando i package corrispondenti.
4. Configurare `docker-compose.yml` con un container per servizio + MariaDB multi-schema + MinIO.
5. Aggiungere endpoint `/internal/...` per la comunicazione inter-servizio.
6. Validare funzionalmente: tutti i test esistenti devono passare senza modifiche ai casi di test.

### Fase 2 — Upgrade Kogito 10.2 in `workflow-service`

**Obiettivo**: aggiornare solo `workflow-service` al nuovo runtime Kogito.

1. Aggiornare `pom.xml` di `workflow-service`: `kogito-maven-plugin 10.2`, `kogito-processes-spring-boot-starter 10.2`.
2. Aggiungere Data Index e Job Service al docker-compose.
3. Aggiornare `KogitoBpmEngineAdapter` e `KogitoDataSourceConfig`.
4. Validare: workflow `anc.main` e `anc.intake` eseguibili end-to-end.

### Fase 3 — Manifest Kubernetes (k3s)

**Obiettivo**: deployare l'intera architettura su k3s locale.

1. Installare Kogito Operator su k3s.
2. Creare manifest Kubernetes in `k8s/` per ogni microservizio.
3. Sostituire docker-compose con `kubectl apply` per l'ambiente di integration test.
4. Validare: demo completa su k3s con Traefik Ingress.

---

## 9. Vincoli tecnici aggiuntivi per il refactor

| Vincolo | Regola |
|---|---|
| **Naming package** | `it.poste.anc.<bc-name>` — ogni microservizio in package distinto, senza cross-import su `domain/` |
| **DB schema isolation** | Nessuna FK cross-schema. Le relazioni logiche cross-servizio sono gestite per chiave applicativa (`requestId`, `taskId`). |
| **API Gateway** | Tutti i path `/api/v1/...` verso il frontend transitano dall'API Gateway (nginx in POC). Gli endpoint `/internal/...` sono inaccessibili dall'esterno. |
| **Kogito BPM** | Solo `workflow-service` può dipendere da `kogito-processes-spring-boot-starter`. Nessun altro microservizio importa Kogito process runtime. |
| **DMN engine** | `document-service` usa `kogito-decisions-spring-boot-starter 10.2` per calcolo esito. Non usa il BPM engine. |
| **Idempotenza** | Invariante: ogni mutazione critica (`openPractice`, `outcomeAck`) supporta retry sicuro come definito nel doc `06`. |
| **Outbox pattern** | Ogni microservizio che produce eventi di dominio mantiene la propria tabella `event_outbox` nello stesso schema. |
| **Test** | Ogni microservizio ha una suite di test autonoma. I test di integration usano Testcontainers con MariaDB e, per `workflow-service`, un container Kogito Data Index. |

---

## 10. Componenti non modificati dal refactor

| Componente | Motivazione |
|---|---|
| File BPMN (`anc_main.bpmn`, `anc_intake.bpmn`, `anc_signal.bpmn`) | Formato BPMN 2.0 invariante tra Kogito versioni |
| File DMN (`anc_outcome_carta.dmn`, `anc_outcome_verbale.dmn`) | Formato DMN 1.3 invariante |
| API REST verso frontend (`/api/v1/...`) | Contratti invarianti per principio P-R2 |
| Frontend React (`apps/kogito/frontend/`) | Nessuna dipendenza da runtime backend |
| Stati pratica (`APERTA`, `IN_LAVORAZIONE`, ecc.) | Definiti nel dominio funzionale (doc `06`) |
| Ownership delle transizioni di stato | Invariante per BC (doc `06` §2) |
| Contratto Interface Agreement BPM↔SD | Immutabile per definizione (doc `08` §4) |
| Ruoli e gruppi (`OPERATORE`, `SUPERVISORE`) | Definiti dal dominio funzionale |
