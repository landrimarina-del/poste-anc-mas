# Sprint 0 — Cross-Agent Issues

**Sprint**: Sprint 0 — Infrastruttura e POC Runtime  
**Data**: 2025-05-27  
**Stato**: Tutte le issue risolte in Sprint 0

---

## Issue #1 — `kogito-maven-plugin:10.2.0` goal `generateModel` bound a `process-classes`

**Gravità**: 🔴 Bloccante  
**Agenti coinvolti**: Backend, DevOps  
**Categoria**: Incompatibilità versione plugin

### Descrizione
Il goal `generateModel` del `kogito-maven-plugin:10.2.0` è bound alla fase Maven **`process-classes`** (dopo `compile`), non a `generate-sources` come nelle versioni precedenti Kogito 2.x.

Senza un `<execution>` esplicito nel `pom.xml`, il goal non veniva eseguito automaticamente, e la classe `ConfigBean` (generata da Kogito) non veniva prodotta, causando errori a runtime:
```
ClassNotFoundException: it.poste.anc.workflow.ConfigBean
```

### Fix applicato
Aggiunta esecuzione esplicita nel `pom.xml`:
```xml
<plugin>
    <groupId>org.kie.kogito</groupId>
    <artifactId>kogito-maven-plugin</artifactId>
    <version>${kogito.version}</version>
    <extensions>true</extensions>
    <executions>
        <execution>
            <goals>
                <goal>generateModel</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Regola operativa
**SEMPRE** usare `mvn clean package` per garantire la generazione di `ConfigBean`. Build incrementale (`mvn package` senza `clean`) può produrre JAR con stale files.

---

## Issue #2 — `kogito-data-index-postgresql:10.2.0` Kafka hardwired nel readiness check

**Gravità**: 🟡 Medio (bloccante per stack avvio)  
**Agenti coinvolti**: DevOps  
**Categoria**: Configurazione container

### Descrizione
L'immagine `apache/incubator-kie-kogito-data-index-postgresql:10.2.0` include un health check di readiness che verifica la connessione Kafka, anche quando Kafka non è configurato. La POC usa HTTP events (via `DataIndexEventPublisher.java` in Spring Boot), non Kafka.

Il container rimaneva permanentemente in stato **unhealthy** con:
```
/q/health/ready → {"status":"DOWN"} (kafka not reachable)
```

### Fix applicato
Modificato l'healthcheck del container in `docker-compose.kogito.yml` da `/q/health/ready` a `/q/health/live`:
```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8080/q/health/live"]
```
Aggiunta variabile d'ambiente: `QUARKUS_KAFKA_HEALTH_ENABLED=false`

### Regola operativa
Per deployment senza Kafka: usare **sempre** `/q/health/live` come healthcheck per `kogito-data-index`.

---

## Issue #3 — Dockerfile multi-stage non compatibile con `kogito-maven-plugin`

**Gravità**: 🔴 Bloccante  
**Agenti coinvolti**: DevOps, Backend  
**Categoria**: Processo di build

### Descrizione
Il Dockerfile multi-stage (con stage Maven per build interna) non generava la classe `ConfigBean` perché il contesto Docker non includeva i file BPMN (`src/main/resources/*.bpmn`) al momento della build Maven nel container.

Il `kogito-maven-plugin:10.2.0` richiede i file BPMN presenti nel classpath durante la fase `process-classes` per generare `ConfigBean`.

### Fix applicato
Adozione di Dockerfile **single-stage** che copia il JAR pre-buildato localmente:
```dockerfile
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app
COPY apps/kogito/backend/target/anc-backend-*.jar /app/app.jar
COPY infra/db/migrations /app/infra/db/migrations
```

### Regola operativa
La build Maven deve avvenire **localmente** (non nel Docker build context). Il processo è:
1. `mvn clean package` in locale
2. `docker compose build kogito-backend` (copia JAR già buildato)
3. `docker compose up -d kogito-backend`

---

## Issue #4 — Flyway V108 conflict da build incrementale stale

**Gravità**: 🟡 Medio  
**Agenti coinvolti**: Backend  
**Categoria**: Flyway migrations

### Descrizione
Durante sviluppo incrementale, il JAR conteneva sia `V108__rename_flowable_task_id_to_kogito_task_id.sql` (script stale da migrazione precedente nel classpath) che `V109__rename_flowable_task_id_to_kogito_task_id.sql` (script corrente).

Il path esterno filesystem conteneva `V108__checklist_verbale_structured_notes.sql`, causando conflict Flyway al boot:
```
Found more than one migration with version 108
```

### Fix applicato
1. `mvn clean` per pulire il JAR stale
2. Rimozione del file `V108__rename_*.sql` dal classpath
3. Rinomina mantenuta: script corrente è `V109__rename_flowable_task_id_to_kogito_task_id.sql`

### Regola operativa
**SEMPRE** `mvn clean package` (mai build incrementale) prima di `docker compose build`.

---

## Issue #5 — OneDrive sync interferisce con `mvn clean`

**Gravità**: 🟢 Basso (operativo)  
**Agenti coinvolti**: DevOps  
**Categoria**: Ambiente sviluppo

### Descrizione
Il workspace è sotto `OneDrive - LUTECH SPA`. L'esecuzione di `mvn clean` che elimina la directory `target/` può triggerare un dialog di OneDrive che chiede "Vuoi eliminare questi elementi anche da OneDrive?" con opzioni "Elimina" / "Mantieni elementi".

Scegliendo "Mantieni elementi", i file non vengono eliminati e il `clean` fallisce silenziosamente.

### Workaround
Escludere la directory `target/` dalla sincronizzazione OneDrive per il workspace:
```
OneDrive Settings → Account → Scegli cartelle → Escludi apps/kogito/backend/target
```

Oppure spostare il workspace fuori da OneDrive per lo sviluppo locale.

---

## Issue #6 — `UnitOfWorkManager` non risolvibile via `Application.unitOfWorkManager()`

**Gravità**: 🔴 Bloccante  
**Agenti coinvolti**: Backend  
**Categoria**: API Kogito 10.2.0

### Descrizione
In Kogito 10.2.0, la classe `Application` non espone più il metodo statico `unitOfWorkManager()`. Il codice precedente:
```java
Application.unitOfWorkManager()
```
causava `NoSuchMethodError` a runtime.

### Fix applicato
`UnitOfWorkManager` iniettato via costruttore Spring in `KogitoBpmEngineAdapter.java`:
```java
@Autowired
public KogitoBpmEngineAdapter(UnitOfWorkManager unitOfWorkManager, ...) {
    this.unitOfWorkManager = unitOfWorkManager;
}
```

---

## Riepilogo

| # | Issue | Gravità | Stato |
|---|-------|---------|-------|
| 1 | Plugin generateModel bound a process-classes | 🔴 Bloccante | ✅ Risolto |
| 2 | Data-index Kafka healthcheck hardwired | 🟡 Medio | ✅ Risolto |
| 3 | Dockerfile multi-stage incompatibile | 🔴 Bloccante | ✅ Risolto |
| 4 | Flyway V108 conflict da stale build | 🟡 Medio | ✅ Risolto |
| 5 | OneDrive interferisce con mvn clean | 🟢 Basso | ⚠️ Workaround |
| 6 | UnitOfWorkManager API rimossa in 10.2.0 | 🔴 Bloccante | ✅ Risolto |
