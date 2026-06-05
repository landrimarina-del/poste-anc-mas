# Kogito Embedded — Comportamento e Analisi

**Data:** 2026-06-03  
**Applicazione analizzata:** `apps/kogito/backend`  
**Versione Kogito:** 10.2.0  
**Stack:** Spring Boot 3.3.5 + jBPM + Kogito embedded + MariaDB

---

## 0. Kogito vs Kogito Embedded — Differenze

Non sono due prodotti diversi — **Kogito è uno solo**. La differenza è nel **modo di deployment**.

### Kogito "standard" (Cloud Native / Quarkus)

Il modello per cui Kogito è stato progettato originariamente:

```
┌─────────────────────────────────────────────┐
│  Microservizio Kogito (Quarkus)             │
│  ┌──────────────┐  ┌──────────────────────┐ │
│  │ BPMN runtime │  │ REST API generata    │ │
│  └──────────────┘  └──────────────────────┘ │
└─────────────────────────────────────────────┘
         ↕ Kafka/events
┌─────────────────┐   ┌──────────────┐
│  Data Index     │   │  Job Service │
│  (Quarkus)      │   │  (Quarkus)   │
└─────────────────┘   └──────────────┘
         ↕
┌─────────────────────┐
│  Management Console │
└─────────────────────┘
```

- Il processo BPM **è** il microservizio — genera un'intera applicazione autonoma
- Pensato per **Kubernetes/OpenShift**
- Il Data Index e Job Service hanno addon **embedded Quarkus** (tutto in un container)
- Comunicazione via **Kafka** (cloud event bus)
- Resync dopo restart: automatico via Kafka replay

### Kogito "Embedded" (Spring Boot) — approccio usato in questo progetto

Il motore Kogito viene usato come **libreria dentro una Spring Boot app esistente**:

```
┌──────────────────────────────────────────────────────────┐
│  Spring Boot App (anc-backend)                          │
│  ┌──────────────────┐   ┌──────────────────────────────┐ │
│  │  Business logic  │   │  Kogito engine (libreria)    │ │
│  │  (practice, task,│   │  ┌──────────────────────────┐│ │
│  │   document, ecc.)│   │  │ jbpm-spring-boot-starter ││ │
│  └──────────────────┘   │  │ Process<Model> beans      ││ │
│                          │  │ UserTasksResource         ││ │
│                          │  └──────────────────────────┘│ │
│                          └──────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
         ↕ HTTP (no Kafka)
┌─────────────────┐   ┌──────────────────────┐
│  Data Index     │   │  Management Console  │
│  (container     │   │  (container separato)│
│   separato)     │   └──────────────────────┘
└─────────────────┘
```

- Il motore BPM è una **dipendenza Maven** dentro la tua app Spring Boot
- **Non** genera un'applicazione autonoma — genera solo classi Java che vivono dentro la tua app
- Il Data Index e Job Service **non hanno addon embedded per Spring Boot** (solo per Quarkus) — devono essere container separati
- Comunicazione col Data Index via **HTTP** (non Kafka), implementata a mano con `DataIndexEventPublisher`
- Resync dopo restart: manuale via `DataIndexResyncService`

### Tabella comparativa

| Aspetto | Kogito Cloud Native (Quarkus) | Kogito Embedded (Spring Boot) |
|---|---|---|
| Runtime base | Quarkus | Spring Boot |
| Il processo BPM è... | l'intera applicazione | una libreria nella tua app |
| Data Index addon | embedded nel container Quarkus | container separato obbligatorio |
| Job Service addon | embedded nel container Quarkus | container separato obbligatorio |
| Comunicazione eventi | Kafka (cloud events) | HTTP custom (`DataIndexEventPublisher`) |
| Persistence addon | embedded (automatico) | `kie-addons-springboot-persistence-jdbc` manuale |
| Supporto ufficiale | ✅ Design target | ⚠️ Supportato ma non il percorso primario |
| Complessità infra | Alta (Kafka, K8s) | Media (solo HTTP, compose) |
| Resync dopo restart | Automatico (Kafka replay) | Manuale (`DataIndexResyncService`) |

### Perché questa app usa l'approccio embedded

La scelta è stata fatta per il contesto **POC Spring Boot**: piuttosto che avere un microservizio Kogito separato con la sua API, il motore BPM vive dentro il backend ANC esistente. Il costo è la gestione manuale degli eventi verso il Data Index (il `DataIndexEventPublisher` scritto a mano) e il resync al restart — cose che nel modello Quarkus nativo sono automatiche via Kafka.

---

## 1. Architettura generale

Kogito è integrato come libreria **embedded dentro Spring Boot** (non è un container separato). Il `kogito-maven-plugin` genera classi Java a **compile-time** partendo dai file BPMN presenti in `src/main/resources/processes/`.

```
BUILD TIME:  anc_pratica.bpmn2 ──► kogito-maven-plugin ──► classi in target/generated-sources/kogito/
RUNTIME:     Spring Boot carica le classi generate come @Bean gestiti da Spring
```

**Dipendenze chiave nel `pom.xml`:**

| Artefatto | Ruolo |
|---|---|
| `org.jbpm:jbpm-spring-boot-starter` | Engine BPM embedded |
| `org.kie:kie-addons-springboot-persistence-jdbc` | Persistenza JDBC istanze di processo |
| `org.drools:drools-decisions-spring-boot-starter` | Runtime DMN/Decisions |
| `org.kie.kogito:kogito-maven-plugin:10.2.0` | Generazione classi Java da BPMN |

---

## 2. Classi generate da Kogito

### 2.1 Dal file `anc_pratica.bpmn2` — package `it.poste.anc.bpm`

| Classe generata | Ruolo |
|---|---|
| `Anc_praticaProcess` | **Bean Spring** con nome `anc_pratica` — è il `Process<Model>` cercato da `KogitoBpmEngineAdapter.resolveProcess()` |
| `Anc_praticaModel` | Contenitore variabili di processo (practiceId, numPratica, canale, cfCliente, idWorkItem, esito) |
| `Anc_praticaModelInput` / `Anc_praticaModelOutput` | DTOs per le variabili di processo |
| `Anc_praticaProcessInstance` | Wrapper runtime dell'istanza di processo |
| `Anc_praticaResource` | REST controller generato per `/anc_pratica` (list/start/get istanze) |
| `Anc_pratica_Task_lavorazione` | Rappresentazione del UserTask "Lavorazione Pratica" |
| `Anc_pratica_task_lavorazione_TaskInput/Output/Model` | Dati I/O del task |
| `Anc_pratica_TaskModelFactory` | Factory per la creazione dei modelli di task |

### 2.2 Dal file `sprint0_foundation_placeholder.bpmn2` — package `org.drools.bpmn2`

| Classe generata | Ruolo |
|---|---|
| `Tech_foundation_placeholderProcess` | Bean processo placeholder Sprint 0 |
| `Tech_foundation_placeholderModel/Input/Output` | Modelli variabili placeholder |
| `Tech_foundation_placeholderResource` | REST controller placeholder |

### 2.3 Classi di infrastruttura Kogito — package `org.kie.kogito.app`

| Classe generata | Ruolo |
|---|---|
| `ProcessConfig` | Registra tutti i `Process<?>` bean e inietta la lista di `EventPublisher` (include `DataIndexEventPublisher`) |
| `UserTaskConfig` | Configura il lifecycle dei UserTask (`DefaultUserTaskLifeCycle`) |
| `UserTasksResource` | REST endpoint `/usertasks/instance` per claim/start/complete task |
| `UserTasks` / `UserTaskLifeCycles` | Registri dei UserTask definiti nei BPMN |
| `ConfigBean` | Aggregatore configurazione runtime Kogito |
| `ProcessCloudEventMetaFactory` | Factory per i CloudEvent header degli eventi di processo |
| `DecisionConfig` / `DecisionModels` / `DecisionModelResourcesProvider` | Infra DMN runtime |
| `ProjectModel` / `ProjectRuntime` | Modello del progetto Kogito (tutti i processi registrati) |
| `GlobalObjectMapper` | ObjectMapper condiviso Kogito |
| `ExceptionHandlerTransaction` | Handler transazioni per operazioni di processo |

---

## 3. Classi scritte a mano (non generate)

### 3.1 Layer engine — `it.poste.anc.workflow.engine`

| Classe | Ruolo |
|---|---|
| `BpmEngineAdapter` | **Interfaccia porta** — astrae le operazioni BPM dall'engine specifico |
| `KogitoBpmEngineAdapter` | Implementazione Kogito di `BpmEngineAdapter` |
| `KogitoDataSourceConfig` | DataSource dedicato schema `kogito` (MariaDB) + `JDBCProcessInstancesFactory` |
| `DataIndexEventPublisher` | Implementa `EventPublisher` Kogito — pubblica eventi verso Data Index via HTTP |
| `DataIndexResyncService` | Ripubblica istanze attive verso Data Index all'avvio (recovery dopo restart) |

### 3.2 Dettaglio `KogitoBpmEngineAdapter`

Il `KogitoBpmEngineAdapter` cerca il bean del processo dinamicamente nell'`ApplicationContext`:

```java
private Process resolveProcess(String processKey) {
    return (Process<?>) applicationContext.getBean(processKey);
    // il bean si chiama "anc_pratica" — stesso nome dell'id BPMN
}
```

Il formato del `taskId` usato internamente è `"processInstanceId::workItemId"`.

Operazioni supportate:

| Metodo | Comportamento |
|---|---|
| `startProcess(processKey, businessKey, variables)` | Crea istanza via `Process.createInstance().start()` — usa `UnitOfWork` |
| `createUserTask(...)` | Per task standalone (recovery): restituisce UUID con prefisso `manual-`, non interagisce con Kogito |
| `claimTask(taskId, username)` | Trova `userTaskInstanceId` via GET `/usertasks/instance`, poi POST transizione `claim` |
| `startTask(taskId, username)` | POST transizione `start` su `/usertasks/instance/{id}/transition` |
| `completeTask(taskId, variables)` | POST transizione `complete` con le variabili di output |
| `getKogitoWorkItemId(processKey, processInstanceId)` | Recupera il primo WorkItem attivo via Java API Kogito |

---

## 4. Perché NON si vede l'avanzamento nella Management Console

### 4.1 Flusso degli eventi (come dovrebbe funzionare)

La Management Console **non legge direttamente dall'engine embedded** ma dal **Data Index**, che è un **container separato**:

```
Management Console ──► Data Index ──► riceve eventi HTTP da ──► DataIndexEventPublisher (dentro il backend)
                                                                          │
                                                                    fire-and-forget
                                                                    (CompletableFuture asincrono)
```

Il flusso completo di un processo avviato:

```
1. POST /anc_pratica  (via Anc_praticaResource generato)
        ↓
2. Anc_praticaProcess.createInstance(businessKey, model).start()
        ↓ (via ProcessConfig → EventManager → EventPublisher chain)
3. DataIndexEventPublisher.publish(ProcessInstanceDataEvent)
        ──HTTP POST──► Data Index /processes
        ↓ (il processo si ferma sul UserTask "Lavorazione Pratica")
4. DataIndexEventPublisher.publish(UserTaskInstanceDataEvent)
        ──HTTP POST──► Data Index /tasks
        ↓
5. Management Console interroga Data Index → vede istanza ACTIVE con task "Lavorazione Pratica"
```

### 4.2 Causa A — `kogito.dataindex.http.url` non configurato o non raggiungibile

In `DataIndexEventPublisher.publish()` esiste questo guard:

```java
if (dataIndexUrl == null || dataIndexUrl.isBlank()) {
    log.debug("Data Index URL not configured, skipping event publish");
    return;  // ← nessun evento inviato, nessun log a livello INFO
}
```

Il valore di default è `http://localhost:8082`. Se il Data Index è su una porta o host diverso, gli eventi vengono persi silenziosamente (solo `warn` nel log).

**Verifica:** cercare nei log del backend:
```
INFO DataIndexEventPublisher - Publishing ProcessInstanceDataEvent to http://.../processes
```
Se questa riga non compare, il problema è l'URL.

### 4.3 Causa B — Data Index riavviato: DB PostgreSQL ricreato da zero

Il Data Index usa `QUARKUS_HIBERNATE_ORM_DATABASE_GENERATION=create` che ricrea il DB ad ogni restart del container.  
`DataIndexResyncService` risolve questo problema pubblicando tutte le istanze attive all'avvio di Spring Boot, ma **solo se il Data Index è già up** quando il backend parte.

Se il backend si avvia prima del Data Index, il resync fallisce. Verificare il log:
```
INFO DataIndexResyncService: resync 'anc_pratica' — N istanze pubblicate
```
Se `N=0` con istanze attive nel DB, il Data Index non era raggiungibile al momento del resync.

### 4.4 Causa C — Disallineamento URL Management Console / Data Index / Backend

La Management Console usa due URL distinti:
- `RUNTIME_TOOLS_MANAGEMENT_CONSOLE_KOGITO_ENV_DATAINDEX_HTTP_URL` → per le query sulle istanze
- `RUNTIME_TOOLS_MANAGEMENT_CONSOLE_KOGITO_ENV_KOGITO_SERVICE_URL` → per le operazioni dirette sul backend

Se uno dei due è sbagliato nel `docker-compose`, le query funzionano ma le azioni (claim, complete) non si propagano, o viceversa.

### 4.5 Causa D — `kogito.service.url` sbagliato: transizioni task silently skipped

`claimTask`, `startTask`, `completeTask` usano `serviceUrl` per chiamare `/usertasks/instance/{id}/transition`. Se `serviceUrl` punta a un host/porta errata, il try/catch nel `KogitoBpmEngineAdapter` logga solo `warn` e non lancia eccezione:

```java
} catch (Exception e) {
    log.warn("claimTask: errore propagazione claim a Kogito per task '{}': {}", ...);
    // ← nessuna eccezione propagata, il chiamante non sa che è fallito
}
```

---

## 5. Come verificare il corretto funzionamento

### 5.1 Log da cercare al momento dell'avvio di un processo

```
INFO  it.poste.anc.workflow.engine.KogitoBpmEngineAdapter - Starting Kogito process 'anc_pratica' businessKey='...'
INFO  it.poste.anc.workflow.engine.KogitoBpmEngineAdapter - Process 'anc_pratica' started, instanceId='<UUID>'
INFO  it.poste.anc.workflow.engine.DataIndexEventPublisher - Publishing ProcessInstanceDataEvent to http://.../processes
INFO  it.poste.anc.workflow.engine.DataIndexEventPublisher - Publishing UserTaskInstanceDataEvent to http://.../tasks
```

### 5.2 Log da cercare al momento del resync all'avvio

```
INFO DataIndexResyncService: avvio resync verso http://...
INFO DataIndexResyncService: ProcessDefinition 'anc_pratica' pubblicato via /definitions
INFO DataIndexResyncService: resync 'anc_pratica' — N istanze pubblicate
INFO DataIndexResyncService: resync completato. Pubblicati=N task=M errori-processo=0
```

### 5.3 Endpoint da testare manualmente

| Endpoint | Descrizione |
|---|---|
| `GET http://backend:8081/anc_pratica` | Lista istanze di processo (via `Anc_praticaResource` generato) |
| `GET http://backend:8081/usertasks/instance?user=...&group=GRUPPO_OPERATORE_ANC` | Lista UserTask attivi |
| `GET http://data-index:8082/graphql` | Interfaccia GraphQL del Data Index per query dirette |

---

## 6. Schema DataSource

L'applicazione usa **due DataSource distinti**:

| DataSource | Schema | Gestito da |
|---|---|---|
| `dataSource` (primario) | `anc` su MariaDB:3307 | Spring Boot / Flyway |
| `kogitoDataSource` | `kogito` su MariaDB:3307 | `KogitoDataSourceConfig` + `JDBCProcessInstancesFactory` |

Lo schema `kogito` deve contenere 4 tabelle (gestite da Kogito via auto-DDL):
- `process_instances`
- `business_key_mapping`
- `correlation_instances`
- `event_types`

Se la tabella `event_types` manca → `RuntimeException` all'avvio di un processo.

---

## 7. Management Console: perché non vede i processi — analisi root cause

### 7.1 Intuizione corretta: MariaDB vs PostgreSQL

La Management Console NON legge da MariaDB. Ecco la separazione reale dei database nel compose:

```
┌──────────────────────────────────────────────────────────────────────┐
│  MariaDB (kogito-mariadb:3306)                                      │
│  ├── schema "anc"     ← dati business ANC (practice, task, user...) │
│  └── schema "kogito"  ← BPM state: process_instances, work_items    │
│                          SOURCE OF TRUTH per il motore Kogito        │
└──────────────────────────────────────────────────────────────────────┘
         ↕ NESSUNA CONNESSIONE DIRETTA
┌──────────────────────────────────────────────────────────────────────┐
│  PostgreSQL (data-index-db:5432)                                    │
│  └── schema "kogito"  ← READ MODEL per Management Console           │
│                          popolato SOLO via HTTP events               │
└──────────────────────────────────────────────────────────────────────┘
         ↑ GraphQL queries
┌──────────────────────────────────────────────────────────────────────┐
│  Management Console / Task Console (browser SPA)                    │
│  → interroga SOLO il Data Index (PostgreSQL)                        │
│  → NON sa nulla di MariaDB                                          │
└──────────────────────────────────────────────────────────────────────┘
```

**Il problema fondamentale:** MariaDB (source of truth BPM) e PostgreSQL (Data Index read model) sono **due database completamente separati**. Il solo ponte tra loro è il meccanismo HTTP di `DataIndexEventPublisher`. Se questo ponte fallisce, la Management Console mostra **zero processi** anche se MariaDB ha tutti i dati.

### 7.2 Il bridge che può rompersi: `DataIndexEventPublisher`

```java
// Fire-and-forget asincrono: se fallisce, nessuno lo sa
CompletableFuture.runAsync(() -> doPublish(event, endpoint));
```

Il meccanismo è **fire-and-forget**: se il Data Index non è raggiungibile nel momento in cui il processo viene avviato, l'evento è **perso per sempre**. Non c'è retry automatico, non c'è queue locale.

### 7.3 Problema specifico nel `docker-compose.kogito.yml`: URL sbagliati nella console

Nel compose attuale le console usano `localhost`:

```yaml
kogito-management-console:
  environment:
    RUNTIME_TOOLS_MANAGEMENT_CONSOLE_DATA_INDEX_ENDPOINT: http://localhost:8082/graphql   # ← localhost?
    RUNTIME_TOOLS_MANAGEMENT_CONSOLE_KOGITO_SERVICE_URL: http://localhost:8081            # ← localhost?

kogito-task-console:
  environment:
    RUNTIME_TOOLS_TASK_CONSOLE_DATA_INDEX_ENDPOINT: http://localhost:8082/graphql         # ← localhost?
    RUNTIME_TOOLS_TASK_CONSOLE_KOGITO_SERVICE_URL: http://localhost:8081                  # ← localhost?
```

Queste variabili configurano la **SPA React che gira nel browser dell'utente** — non chiamate server-side. Da un browser sulla macchina host, `localhost:8082` è corretto (la porta è esposta dal compose). Questo non è il problema.

Il problema è invece nel backend: `KOGITO_SERVICE_URL: http://kogito-backend:8080` — il backend chiama sé stesso via nome Docker per le transizioni task. Se il networking Docker non è configurato correttamente o il container non ha risoluzione DNS interna, `claimTask`/`completeTask` falliscono silenziosamente.

### 7.4 Scenario di failure tipico: ordine di avvio

Il compose dichiara:
```yaml
kogito-backend:
  depends_on:
    kogito-data-index:
      condition: service_healthy
```

Il backend parte solo dopo che il Data Index è healthy. **Tuttavia** il `DataIndexResyncService` aspetta 5 secondi fissi (`Thread.sleep(5000)`) prima di iniziare il resync — se il Data Index impiega più di 5 secondi a diventare pienamente operativo dopo il healthcheck, il resync fallisce parzialmente.

```java
// DataIndexResyncService — attesa fissa non sufficiente
try { Thread.sleep(5000); } catch (InterruptedException e) { ... }
```

### 7.5 Schema della catena di sincronizzazione completa

```
1. mvn clean package → genera Anc_praticaProcess bean

2. docker compose up:
   MariaDB → Data Index DB (PostgreSQL) → Data Index → Backend → Console

3. Backend avvio:
   DataIndexResyncService.resyncOnStartup()
     └─ legge istanze da MariaDB (Process.instances().stream())
     └─ pubblica ProcessInstanceDataEvent → HTTP POST → Data Index /processes
     └─ resync UserTask → HTTP POST → Data Index /tasks

4. Nuova pratica aperta:
   KogitoBpmEngineAdapter.startProcess()
     └─ Anc_praticaProcess.createInstance().start()
     └─ ProcessConfig.EventManager.publish(ProcessInstanceDataEvent)   [via Kogito internals]
     └─ DataIndexEventPublisher.publish()  [fire-and-forget async]
          └─ HTTP POST → http://kogito-data-index:8080/processes

5. Management Console (browser):
   → GraphQL query → http://localhost:8082/graphql (Data Index)
   → legge da PostgreSQL

Se il passo 4 fallisce → PostgreSQL vuoto → Console non mostra nulla
anche se MariaDB ha l'istanza di processo correttamente salvata.
```

### 7.6 Come verificare lo stato reale

**Verificare MariaDB (source of truth BPM):**
```sql
-- Da MariaDB schema "kogito"
SELECT id, process_id, status, start_date FROM process_instances;
-- status: 1=ACTIVE, 2=COMPLETED, 3=ABORTED
```

**Verificare PostgreSQL (Data Index read model):**
```bash
# Dalla Management Console oppure via GraphQL diretto:
curl -X POST http://localhost:8082/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ ProcessInstances { id, processId, state } }"}'
```

**Se MariaDB ha righe ma GraphQL ritorna array vuoto** → il bridge HTTP events non funziona.  
**Se entrambi sono vuoti** → il processo non è mai stato avviato correttamente.

### 7.7 Fix consigliati

| Problema | Fix |
|---|---|
| Event perso al primo avvio | Aumentare `Thread.sleep` in `DataIndexResyncService` o usare retry con backoff |
| Event perso durante il runtime | Aggiungere retry in `DataIndexEventPublisher` con almeno 3 tentativi |
| Data Index riavviato (PostgreSQL svuotato) | Triggerare manualmente il resync via endpoint `/actuator` oppure riavviare il backend |
| `completeTask` fallisce silenziosamente | Propagare l'eccezione invece di swallowarlo in `warn` per il debug |
