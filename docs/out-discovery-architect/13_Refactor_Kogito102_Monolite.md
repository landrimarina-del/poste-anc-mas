# 13 - Refactor Kogito 2.44 → 10.2 — Monolite con Management Console e Task Console

> **Destinatario**: Development MAS  
> **App target**: `apps/kogito` — le modifiche Java/Maven/YAML riguardano esclusivamente `apps/kogito/backend/`. Il file docker-compose da modificare è `docker-compose.kogito.yml` nella **root del workspace** (non dentro `apps/kogito`).  
> **Scope**: upgrade Kogito dalla versione `2.44.0.Alpha` alla `10.2.0.Final` nel modulo `apps/kogito/backend`. L'architettura rimane **monolite Spring Boot**. Si aggiunge supporto per Management Console e Task Console Kogito tramite container separati (`kogito-data-index`, `kogito-management-console`, `kogito-task-console`). In Kogito 10.2 con Spring Boot, Data Index e Job Service sono container separati — non esistono addon embedded per Spring Boot (esistono solo per Quarkus).  
> **Struttura directory**:  
> - `apps/kogito/backend/` — Spring Boot monolite (modifiche Java, pom.xml, YAML)  
> - `docker-compose.kogito.yml` (root workspace) — update immagini container Kogito da 1.44.1 a 10.2  
> - `infra/db/init/` (root workspace) — script DB init  
> **Invarianti**: logica di business, BPMN, DMN, API REST verso frontend, stati pratica, schema `anc` su MariaDB.  
> **NON è in scope**: microservizi, modifica workflow, nuove feature funzionali, cambio framework frontend. Non modificare altre app del workspace (`apps/flowable`, `apps/frontend`, ecc.).

---

## 1. Riepilogo modifiche per file

| File | Tipo modifica | Priorità |
|---|---|---|
| `pom.xml` | artifact names Kogito 10.2 (`org.kie`/`org.jbpm`) + versioni + Flyway | obbligatoria |
| `KogitoDataSourceConfig.java` | import `JDBCProcessInstancesFactory` — package rinominato in 10.2 | obbligatoria |
| `DataIndexEventPublisher.java` | aggiornare property URL → container Data Index esterno | obbligatoria |
| `KogitoBpmEngineAdapter.java` | verifica import + `UnitOfWorkManager` injection | obbligatoria |
| `ManagementProcessController.java` | aggiornare path API per compatibilità console 10.2 | obbligatoria |
| `application.yml` | property Kogito aggiornate, URL Data Index container esterno | obbligatoria |
| `application-poc.yml` | URL Data Index container esterno (non self-reference) | obbligatoria |
| `META-INF/kogito.properties` | property codegen aggiornate | da verificare |
| `SecurityConfig.java` | aggiungere CORS per Management Console e Task Console | obbligatoria |
| `docker-compose.yml` | aggiungere Data Index, Job Service, Management Console, Task Console | obbligatoria |
| `infra/db/init/01_create_schemas.sql` | aggiungere schema `kogito` con GRANT se non presente | da verificare |

---

## 1b. Chiarimenti su gap implementativi

### Task Console — endpoint backend chiamati

Task Console 10.0.0 **non espone endpoint REST propri** nel backend. Il flusso è:
1. Task Console legge la lista task da Data Index GraphQL (`/graphql`)
2. Ogni record task nel Data Index contiene il campo `endpoint` — URL del Kogito runtime per quel task
3. Task Console chiama direttamente l'URL in `endpoint` per claim/complete:

```
POST {serviceUrl}/anc_pratica/{instanceId}/tasks/{taskId}/claim
POST {serviceUrl}/anc_pratica/{instanceId}/tasks/{taskId}/complete
POST {serviceUrl}/anc_pratica/{instanceId}/tasks/{taskId}/release
```

Questi path sono **generati automaticamente** dal `kogito-maven-plugin` a partire dal BPMN. Non occorre aggiungere controller: esistono già nel runtime Kogito compilato.

**Dipendenza critica**: `kogito.service.url` nel docker-compose deve essere `http://kogito-backend:8080` (nome service Docker definito in `docker-compose.kogito.yml`), non `http://localhost:8081`. Il `DataIndexEventPublisher` usa `serviceUrl` per popolare il campo `source` degli eventi, da cui Data Index ricava il campo `endpoint` di ogni task. Se `serviceUrl` è errato, Task Console non riesce a raggiungere il backend.

> In `docker-compose.kogito.yml` la variabile `KOGITO_SERVICE_URL: http://kogito-backend:8080` è già impostata correttamente — non modificarla.

### CORS — configurazione obbligatoria

Il `SecurityConfig.java` attuale **non configura CORS**. Management Console (`:8085`) e Task Console (`:8086`) sono origin diverse dal backend (`:8080`) e riceveranno errori CORS su ogni chiamata.

Aggiungere in `SecurityConfig.java`:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration cfg = new CorsConfiguration();
    cfg.setAllowedOrigins(List.of(
        "http://localhost:8083",  // Management Console (porta 8083 da docker-compose.kogito.yml)
        "http://localhost:8084",  // Task Console (porta 8084 da docker-compose.kogito.yml)
        "http://kogito-management-console:8080",
        "http://kogito-task-console:8080"
    ));
    cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
    cfg.setAllowedHeaders(List.of("*"));
    cfg.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
}
```

E nel `filterChain`, aggiungere `.cors(cors -> cors.configurationSource(corsConfigurationSource()))` prima di `.csrf(...)`.

Aggiornare anche il `SecurityConfig` per permettere senza autenticazione i path generati da Kogito per le console:
```java
.requestMatchers(new AntPathRequestMatcher("/management/**")).permitAll()
.requestMatchers(new AntPathRequestMatcher("/anc_pratica/**")).permitAll() // task operations da Task Console
```

### `DataIndexEventPublisher` — rischio import in 10.2

Il publisher usa `instanceof ProcessInstanceDataEvent` e `instanceof UserTaskInstanceDataEvent`. Questi import sono in `org.kie.kogito.event.process`. In Kogito 10.2 il package potrebbe essere invariato, ma esiste il rischio che `UserTaskInstanceDataEvent` sia rinominata.

**Rischio**: se i `instanceof` non matchano, nessun evento viene pubblicato → Data Index resta vuoto → console bianche. Non viene catturato dal compilatore se la classe padre è compatibile.

**Azione al Step 2**: dopo `mvn compile`, cercare nel classpath la classe effettiva:
```bash
jar tf ~/.m2/repository/org/kie/kogito/kogito-api/10.2.0.Final/*.jar | grep -i "UserTask.*DataEvent"
```
Se il nome è cambiato, aggiornare `instanceof` e import nel publisher.

---

## 2. `pom.xml` — modifiche puntuali

### 2.1 Versione Kogito e Spring Boot

```xml
<properties>
    <java.version>21</java.version>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <!-- DA: 2.44.0.Alpha  A: 10.2.0.Final -->
    <kogito.version>10.2.0.Final</kogito.version>
</properties>

<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <!-- Versione certificata BAMOE (IBM Business Automation Manager Open Editions)
         per Kogito 10.2. Non aggiornare a 3.4.x: gli addon Spring Boot Kogito 10.2
         sono certificati su 3.3.5. -->
    <version>3.3.5</version>
    <relativePath/>
</parent>
```

### 2.2 Dipendenze Kogito — rinomina artifact (breaking change 10.2)

In Kogito 10.2 i groupId e artifactId degli starter e degli addon Spring Boot cambiano. Non si tratta di aggiungere dipendenze nuove ma di **aggiornare quelle esistenti** con i nuovi coordinate.

**Starter processo BPMN** — groupId e artifactId cambiano:
```xml
<!-- DA -->
<dependency>
    <groupId>org.kie.kogito</groupId>
    <artifactId>kogito-processes-spring-boot-starter</artifactId>
</dependency>

<!-- A -->
<dependency>
    <groupId>org.jbpm</groupId>
    <artifactId>jbpm-spring-boot-starter</artifactId>
</dependency>
```

**Starter decisioni DMN** — groupId e artifactId cambiano:
```xml
<!-- DA -->
<dependency>
    <groupId>org.kie.kogito</groupId>
    <artifactId>kogito-decisions-spring-boot-starter</artifactId>
</dependency>

<!-- A -->
<dependency>
    <groupId>org.kie</groupId>
    <artifactId>drools-decisions-spring-boot-starter</artifactId>
</dependency>
```

**Addon persistenza JDBC** — groupId e artifactId cambiano:
```xml
<!-- DA -->
<dependency>
    <groupId>org.kie.kogito</groupId>
    <artifactId>kogito-addons-springboot-persistence-jdbc</artifactId>
</dependency>

<!-- A -->
<dependency>
    <groupId>org.kie</groupId>
    <artifactId>kie-addons-springboot-persistence-jdbc</artifactId>
</dependency>
```

> **Nota su Data Index e Job Service**: in Kogito 10.2 con Spring Boot, gli addon embedded per Data Index e Job Service **non esistono** (esistono solo per Quarkus). Data Index e Job Service sono **container separati** nel docker-compose. Non aggiungere dipendenze `data-index-persistence-jdbc` o `jobs-service-embedded`: non sono nel BOM Spring Boot 10.2.

**Flyway** — modulo MariaDB rinominato in Flyway 10.x:
```xml
<!-- DA -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>

<!-- A -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-mariadb</artifactId>
</dependency>
```

> Se `flyway-database-mariadb` non risolve dal BOM Spring Boot 3.3.5, mantenere `flyway-mysql`: verificare la versione Flyway transitiva prima di cambiare.

### 2.3 Plugin Maven

```xml
<plugin>
    <groupId>org.kie.kogito</groupId>
    <artifactId>kogito-maven-plugin</artifactId>
    <!-- DA: 2.44.0.Alpha  A: 10.2.0.Final — stessa sintassi, nessun cambio config -->
    <version>${kogito.version}</version>
    <extensions>true</extensions>
</plugin>
```

---

## 3. `KogitoDataSourceConfig.java` — aggiornamento import

Il file mantiene la sua struttura attuale con **due datasource**: `anc` (business) e `kogito` (process instances). Non è necessario aggiungere un terzo datasource: in Kogito 10.2 con Spring Boot, il Data Index è un container separato e non richiede un datasource nell'applicazione.

### 3.1 Correzione import `JDBCProcessInstancesFactory`

In Kogito 10.2 il package dell'addon persistenza JDBC è rinominato insieme all'artifactId. L'import corretto va determinato a compilazione.

```java
// DA (2.44)
import org.kie.kogito.persistence.springboot.JDBCProcessInstancesFactory;

// Possibili path in 10.2 — compilare e usare quello che risolve:
// import org.kie.persistence.jdbc.springboot.JDBCProcessInstancesFactory;
// import org.kie.kogito.addons.persistence.jdbc.JDBCProcessInstancesFactory;
```

**Procedura**: compilare con `mvn compile` dopo aver aggiornato l'artifact `kie-addons-springboot-persistence-jdbc` nel `pom.xml`. Correggere solo l'import segnalato come non risolto. Il resto del file — bean `kogitoDataSource`, bean `processInstancesFactory` — rimane invariato.

---

## 4. `DataIndexEventPublisher.java` — MANTENERE, aggiornare property URL

Il file implementa `org.kie.kogito.event.EventPublisher` e pubblica gli eventi di processo via HTTP POST verso il Data Index. In Kogito 10.2 con Spring Boot il Data Index è un **container esterno** (non embedded): la logica del publisher è corretta e va mantenuta. L'unica modifica è la property da cui legge l'URL.

**Azione**: aggiornare la property letta dal costruttore da `kogito.data-index-url` a `kogito.dataindex.http.url`, allineandola alla convention Kogito 10.2 e alla configurazione in `application.yml`.

```java
// DA (proprietà attuale nel costruttore)
@Value("${kogito.data-index-url:http://kogito-data-index:8080}")
private String dataIndexUrl;

// A — property allineata alla convention Kogito 10.2
@Value("${kogito.dataindex.http.url:http://kogito-data-index:8080}")
private String dataIndexUrl;
```

Gli endpoint POST `/processes` e `/tasks` usati dal publisher sono invariati tra 2.44 e 10.2.

**Verifica post-modifica**: avviare il backend e verificare nei log che il publisher non emetta errori di connessione verso il container Data Index. Il container Data Index deve essere avviato prima del backend (vedere sezione 11 per la configurazione docker-compose).

---

## 5. `KogitoBpmEngineAdapter.java` — verifiche mirate

### 5.1 `UnitOfWorkManager` — injection diretta

Il costruttore attuale ottiene `UnitOfWorkManager` da `Application.unitOfWorkManager()`. In Kogito 10.2 è più robusto iniettarlo direttamente come bean Spring:

```java
// DA (costruttore attuale)
public KogitoBpmEngineAdapter(ApplicationContext applicationContext,
                               Application kogitoApplication,
                               @Value("${kogito.data-index-url:...}") String dataIndexUrl) {
    this.unitOfWorkManager = kogitoApplication.unitOfWorkManager();
    // ...
}

// A — injection diretta, invariante tra 2.44 e 10.2
public KogitoBpmEngineAdapter(ApplicationContext applicationContext,
                               UnitOfWorkManager unitOfWorkManager,
                               @Value("${kogito.service.url:http://localhost:8080}") String serviceUrl) {
    this.applicationContext = applicationContext;
    this.unitOfWorkManager = unitOfWorkManager;
    this.restTemplate = new RestTemplate();
    this.serviceUrl = serviceUrl;
}
```

Nota: il campo `dataIndexUrl` non serve più in `KogitoBpmEngineAdapter` perché la pubblicazione eventi verso Data Index è delegata a `DataIndexEventPublisher`. Sostituire con `serviceUrl` per gli URL di claim/complete task che puntano all'endpoint locale del processo.

### 5.2 URL claim/complete task

I metodi `claimTask`, `completeTask` usano URL hardcoded `http://localhost:8080/anc_pratica/...`. In 10.2 il path generato dal plugin Maven resta identico — **non modificare** i path a meno che la compilazione non segnali errori.

### 5.3 Import da verificare a compilazione

```java
// Stabili in 10.2 — non modificare preventivamente:
import org.kie.kogito.Application;
import org.kie.kogito.Model;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.uow.UnitOfWork;
import org.kie.kogito.uow.UnitOfWorkManager;
```

Compilare e correggere solo gli import segnalati come non risolti.

---

## 6. `ManagementProcessController.java` — compatibilità console 10.2

Il controller attuale espone `/management/processes/{processId}/source` e `/management/processes/{processId}/nodes` per la Management Console 1.44.x.

La Management Console 10.2 usa gli stessi endpoint di base ma **aggiunge** `/management/processes/{processId}/nodes/{nodeId}` per il dettaglio nodo. Aggiungere il terzo endpoint:

```java
/**
 * Dettaglio nodo singolo — richiesto da Management Console 10.2.
 */
@GetMapping("/{processId}/nodes/{nodeId}")
public ResponseEntity<Map<String, Object>> getNode(
        @PathVariable String processId,
        @PathVariable String nodeId) {
    List<Map<String, Object>> nodes = getNodes(processId).getBody();
    if (nodes == null) return ResponseEntity.notFound().build();
    return nodes.stream()
            .filter(n -> nodeId.equals(n.get("id")))
            .findFirst()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}
```

> La Management Console 10.2 interroga anche il Data Index GraphQL per i dati di istanza. Il `ManagementProcessController` rimane necessario solo per source BPMN e nodi statici — la console combina i due.

---

## 7. `META-INF/kogito.properties` — aggiornamento

```properties
# DA (2.44)
kogito.generate.processes=true
kogito.codegen.processes.enabled=true

# A (10.2) — il plugin Maven 10.2 gestisce codegen tramite estensione Maven.
# Le property nel file sono opzionali; mantenere solo quelle riconosciute da 10.2.
kogito.codegen.resources.disable=false
```

> Se in fase di compilazione il plugin 10.2 emette warning su property sconosciute, rimuovere quelle non riconosciute. Non modificare preventivamente le property senza aver osservato i warning del compilatore.

---

## 8. `application.yml` — property Kogito aggiornate

Sostituire il blocco `kogito:` esistente con:

```yaml
kogito:
  # DataSource per process instances (schema kogito su MariaDB) — invariato
  datasource:
    jdbc-url: ${KOGITO_DATASOURCE_URL:jdbc:mariadb://localhost:3307/kogito}
    username: ${KOGITO_DATASOURCE_USERNAME:anc}
    password: ${KOGITO_DATASOURCE_PASSWORD:anc}
    driver-class-name: org.mariadb.jdbc.Driver

  persistence:
    type: jdbc

  # URL del servizio (usato dalla Management Console per operazioni dirette)
  service:
    url: ${KOGITO_SERVICE_URL:http://localhost:8080}

  # Data Index: container esterno separato (non embedded in Spring Boot 10.2)
  dataindex:
    http:
      url: ${KOGITO_DATAINDEX_HTTP_URL:http://kogito-data-index:8080}

  # Job Service: container esterno separato
  jobs-service:
    url: ${KOGITO_JOBS_SERVICE_URL:http://kogito-jobs-service:8080}
```

> La property `kogito.data-index-url` usata nell'attuale `application-poc.yml` va allineata a `kogito.dataindex.http.url` che è la convention Kogito 10.2 e la stessa letta da `DataIndexEventPublisher`.

---

## 9. `application-poc.yml` — aggiornamento URL Data Index

```yaml
# DA
kogito:
  data-index-url: ${KOGITO_DATAINDEX_HTTP_URL:http://kogito-data-index:8080}

# A — allineamento property alla convention Kogito 10.2
#       il container Data Index è un servizio separato nel compose
kogito:
  service:
    url: ${KOGITO_SERVICE_URL:http://anc-backend:8080}
  dataindex:
    http:
      url: ${KOGITO_DATAINDEX_HTTP_URL:http://kogito-data-index:8080}
  jobs-service:
    url: ${KOGITO_JOBS_SERVICE_URL:http://kogito-jobs-service:8080}
```

---

## 10. Schema MariaDB — nessuna modifica applicativa

In Kogito 10.2 con Spring Boot, il Data Index è un container autonomo che gestisce il proprio schema sul proprio database (tipicamente PostgreSQL nella distribuzione ufficiale). L'applicazione non gestisce lo schema Data Index.

Lo schema `kogito` su MariaDB (process instances) è già presente e viene gestito dal DDL Kogito. L'unica verifica necessaria è che il DDL sia compatibile con la versione 10.2.

### 10.1 Schema MariaDB risultante (invariato)

```
MariaDB instance
├── anc          ← schema business (invariato, gestito da Flyway)
└── kogito       ← process instances (DDL Kogito 10.2 — verificare compatibilità)
```

### 10.2 Verifica DDL Kogito 10.2

Dopo l'aggiornamento Maven, verificare che le tabelle dello schema `kogito` siano compatibili con il DDL 10.2:

```bash
# Estrarre il DDL Kogito 10.2 per MariaDB
jar tf ~/.m2/repository/org/kie/kie-addons-springboot-persistence-jdbc/10.2.0/*.jar | grep sql
```

Se il DDL 10.2 aggiunge colonne o tabelle rispetto al DDL 2.44, applicare le differenze sullo schema `kogito` esistente.

---

## 11. `docker-compose.kogito.yml` (root workspace) — update container esistenti

Il file `docker-compose.kogito.yml` esiste già nella **root del workspace** e contiene già i container Kogito alla versione `1.44.1` con immagini `quay.io/kiegroup/`. Questo step è un **update** delle definizioni esistenti, non un'aggiunta.

### 11.1 Aggiungere container `data-index-db` (PostgreSQL — nuovo)

Il Data Index 10.2 richiede PostgreSQL come persistence. Il Data Index 1.44.1 attuale è ephemeral (in-memory). Aggiungere il container prima di `kogito-data-index`:

```yaml
  # DB PostgreSQL dedicato al Data Index 10.2 (il container 10.2 non supporta MariaDB)
  data-index-db:
    image: postgres:16-alpine
    container_name: anc-kogito-data-index-db
    restart: unless-stopped
    environment:
      POSTGRES_DB: kogito_dataindex
      POSTGRES_USER: kogito
      POSTGRES_PASSWORD: kogito
    volumes:
      - anc-kogito-dataindex-db-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD", "pg_isready", "-U", "kogito"]
      interval: 5s
      timeout: 3s
      retries: 10
```

Aggiungere il volume in fondo al file: `anc-kogito-dataindex-db-data:`.

### 11.2 Sostituire `kogito-data-index` (ephemeral → postgresql 10.2)

```yaml
  # DA (1.44.1 ephemeral — in-memory, nessun DB)
  kogito-data-index:
    image: quay.io/kiegroup/kogito-data-index-ephemeral:1.44.1
    environment:
      QUARKUS_HTTP_PORT: "8080"
      KOGITO_DATAINDEX_QUARKUS_PROFILE: http-events-support

  # A (10.2.0 postgresql — persistenza su data-index-db)
  kogito-data-index:
    image: apache/incubator-kie-kogito-data-index-postgresql:10.2.0
    container_name: anc-kogito-data-index
    restart: unless-stopped
    environment:
      QUARKUS_HTTP_PORT: "8080"
      KOGITO_DATAINDEX_QUARKUS_PROFILE: http-events-support
      QUARKUS_DATASOURCE_JDBC_URL: jdbc:postgresql://data-index-db:5432/kogito_dataindex
      QUARKUS_DATASOURCE_USERNAME: kogito
      QUARKUS_DATASOURCE_PASSWORD: kogito
    ports:
      - "8082:8080"
    depends_on:
      data-index-db:
        condition: service_healthy
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:8080/q/health/ready || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 20
```

### 11.3 Sostituire `kogito-management-console` (1.44.1 → 10.2.0)

**Rimuovere i `volumes` con jar-patch** — erano fix manuali per 1.44.1, non compatibili con 10.2:

```yaml
  # DA (1.44.1 con jar-patch)
  kogito-management-console:
    image: quay.io/kiegroup/kogito-management-console:1.44.1
    volumes:
      - ./infra/kogito/console-config/application.properties:/home/kogito/config/application.properties:ro
      - ./infra/kogito/console-config/jar-patch/management-console-patched.jar:/home/kogito/bin/...

  # A (10.2.0 — nessun jar-patch, variabili ambiente cambiate)
  kogito-management-console:
    image: apache/incubator-kie-kogito-management-console:10.2.0
    container_name: anc-kogito-management-console
    restart: unless-stopped
    depends_on:
      kogito-data-index:
        condition: service_healthy
    environment:
      RUNTIME_TOOLS_MANAGEMENT_CONSOLE_KOGITO_ENV_MODE: "DEV"
      RUNTIME_TOOLS_MANAGEMENT_CONSOLE_DATA_INDEX_ENDPOINT: http://localhost:8082/graphql
      RUNTIME_TOOLS_MANAGEMENT_CONSOLE_KOGITO_SERVICE_URL: http://localhost:8081
    ports:
      - "8083:8080"
```

> Le URL in `RUNTIME_TOOLS_*` devono essere raggiungibili dal **browser** dell'utente — usare `localhost` con le porte mappate, non i nomi interni Docker.

### 11.4 Sostituire `kogito-task-console` (1.44.1 → 10.0.0)

Tag `10.2.0` non pubblicato su Docker Hub — massimo disponibile è `10.0.0` (verificato).

```yaml
  # DA (1.44.1 con jar-patch)
  kogito-task-console:
    image: quay.io/kiegroup/kogito-task-console:1.44.1
    volumes:
      - ./infra/kogito/console-config/application.properties:/home/kogito/config/application.properties:ro
      - ./infra/kogito/console-config/jar-patch/task-console-patched.jar:/home/kogito/bin/...

  # A (10.0.0 — nessun jar-patch)
  kogito-task-console:
    image: apache/incubator-kie-kogito-task-console:10.0.0
    container_name: anc-kogito-task-console
    restart: unless-stopped
    depends_on:
      kogito-data-index:
        condition: service_healthy
    environment:
      RUNTIME_TOOLS_TASK_CONSOLE_KOGITO_ENV_MODE: "DEV"
      RUNTIME_TOOLS_TASK_CONSOLE_DATA_INDEX_ENDPOINT: http://localhost:8082/graphql
      RUNTIME_TOOLS_TASK_CONSOLE_KOGITO_SERVICE_URL: http://localhost:8081
    ports:
      - "8084:8080"
```

### Porte di accesso (invariate rispetto allo stack 1.44.1)

| Componente | URL locale |
|---|---|
| Backend ANC (API REST) | `http://localhost:8081` |
| Swagger UI | `http://localhost:8081/swagger-ui.html` |
| Data Index (GraphQL) | `http://localhost:8082/graphql` |
| Kogito Management Console | `http://localhost:8083` |
| Kogito Task Console | `http://localhost:8084` |

> **Tag Docker verificati**: `management-console:10.2.0` ✅, `data-index-postgresql:10.2.0` ✅, `task-console:10.0.0` ✅ (10.2.0 non pubblicato), `jobs-service-allinone:10.0.x-20260329` ✅ (10.2.0 non pubblicato).

---

## 12. Sequenza di esecuzione del refactor

Eseguire nell'ordine indicato. Ogni step deve compilare senza errori prima di procedere al successivo.

### Step 1 — Aggiornare `pom.xml`

1. Cambiare `kogito.version` a `10.2.0.Final`.
2. Rinominare artifact starter e addon come descritto nella sezione 2.2 (`jbpm-spring-boot-starter`, `drools-decisions-spring-boot-starter`, `kie-addons-springboot-persistence-jdbc`).
3. Sostituire `flyway-mysql` con `flyway-database-mariadb`.
4. Aggiornare `kogito-maven-plugin` a `10.2.0.Final`.
5. **Non aggiungere** addon embedded per Data Index o Job Service: non esistono per Spring Boot 10.2.
6. Eseguire `mvn dependency:resolve` per verificare che il BOM risolva senza conflitti.

### Step 2 — Risolvere errori di compilazione

1. Eseguire `mvn compile`.
2. Correggere gli import non risolti: attesi in `KogitoDataSourceConfig.java` (import `JDBCProcessInstancesFactory`) e in `KogitoBpmEngineAdapter.java`.
3. Non modificare altro finché la compilazione non è pulita.

### Step 3 — Aggiornare `DataIndexEventPublisher.java`

1. Sostituire la property `kogito.data-index-url` con `kogito.dataindex.http.url` (sezione 4).
2. Compilare e verificare che non ci siano errori.

### Step 4 — Aggiornare `KogitoBpmEngineAdapter.java`

1. Sostituire `Application.unitOfWorkManager()` con `@Autowired UnitOfWorkManager`.
2. Rimuovere eventuale campo `dataIndexUrl` e la relativa `@Value` se presente (già gestita in `DataIndexEventPublisher`).
3. Compilare e verificare che i test del modulo `workflow` passino.

### Step 5 — Aggiornare configurazione YAML

1. Applicare le modifiche a `application.yml` e `application-poc.yml` come descritto nelle sezioni 8 e 9.
2. URL Data Index: `http://kogito-data-index:8080` (container separato, non self-reference).

### Step 6 — Aggiornare `META-INF/kogito.properties`

1. Solo se `mvn compile` emette warning su property non riconosciute da 10.2.
2. Rimuovere solo le property che generano warning espliciti — non rimuovere preventivamente.

### Step 7 — Aggiornare `ManagementProcessController.java`

1. Aggiungere l'endpoint `/nodes/{nodeId}` (sezione 6).
2. Compilare e verificare il test `WorkflowReadinessEndpointTest`.

### Step 8 — Aggiornare `docker-compose.yml`

1. Aggiungere container `data-index-db` (PostgreSQL — necessario per Data Index).
2. Aggiungere container `kogito-data-index` (sezione 11).
3. Aggiungere container `kogito-jobs-service` (opzionale per POC se non ci sono timer BPMN).
4. Aggiungere container `kogito-management-console` e `kogito-task-console` (sezione 11).
5. Verificare i tag Docker disponibili prima di usare `10.2.0`.

### Step 9 — Test end-to-end

1. Avviare lo stack completo: `docker compose up -d`.
2. Verificare che il backend risponda su `http://localhost:8080/actuator/health`.
3. Verificare che il Data Index risponda su `http://localhost:8082/graphql`.
4. Aprire `http://localhost:8085` (Management Console) — deve caricare e connettersi al Data Index.
5. Aprire `http://localhost:8086` (Task Console) — deve caricare e listare i task attivi.
6. Eseguire il flusso completo: apertura pratica via BPM stub → task visibile in Task Console → completamento → stato aggiornato in Management Console.

---

## 13. File invariati — non modificare

| File / Directory | Motivazione |
|---|---|
| `processes/anc_pratica.bpmn2` | BPMN 2.0 invariante tra Kogito versioni |
| `processes/sprint0_foundation_placeholder.bpmn2` | idem |
| `processes/anc_outcome_carta.dmn` | DMN 1.3 invariante |
| `processes/anc_outcome_verbale.dmn` | idem |
| `db/migration/V100–V111` | migrazioni schema `anc` — nessuna dipendenza Kogito |
| Tutti i package `practice`, `document`, `signals`, `supervision`, `bpmgw` | nessuna dipendenza da Kogito runtime |
| `shared/security/*` | nessuna dipendenza da Kogito runtime |
| `frontend/` | indipendente dal backend runtime |
| API REST `/api/v1/...` | contratti invarianti |

---

## 14. Rischi e punti di attenzione

| Rischio | Probabilità | Mitigazione |
|---|---|---|
| Package `JDBCProcessInstancesFactory` rinominato in 10.2 | alta | compilare e correggere l'import segnalato; non anticipare |
| Artifact `jbpm-spring-boot-starter` non risolve dal BOM senza repository aggiuntivo | media | verificare che il BOM `kogito-bom:10.2.0` sia correttamente importato come `dependencyManagement`; il BOM 10.2.0 è su Maven Central |
| Tag Docker `10.2.0` non pubblicato per i container console/data-index | media | verificare su Docker Hub i tag disponibili per `apache/incubator-kie-kogito-*` prima dello step 8 |
| Data Index usa PostgreSQL, non MariaDB | certa | aggiungere container `postgres:16-alpine` dedicato al Data Index; il MariaDB applicativo rimane invariato |
| `kogito.properties` contiene property non riconosciute da 10.2 | bassa | rimuovere solo le property che generano warning espliciti a compilazione |
| DDL schema `kogito` incompatibile con 10.2 | bassa | estrarre DDL 10.2 da `kie-addons-springboot-persistence-jdbc` jar e verificare diff con schema esistente |
| `DataIndexEventPublisher` property name non allineata | bassa | allineare `kogito.data-index-url` a `kogito.dataindex.http.url` come descritto nella sezione 4 |
