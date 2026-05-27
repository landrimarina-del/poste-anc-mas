# Sprint 0 — Execution Plan

**Sprint**: Sprint 0 — Infrastruttura e POC Runtime  
**Data**: 2025-05-27  
**Obiettivo**: Upgrade da Kogito 2.44.0.Alpha a Kogito 10.2.0 in architettura monolite Spring Boot, con Management Console e Task Console come container separati. Vertical slice BPM eseguibile localmente.

---

## Scope Sprint

Implementazione del refactor descritto in `docs/out-discovery-architect/13_Refactor_Kogito102_Monolite.md`.

### Deliverable

| # | Componente | Descrizione | Agente |
|---|-----------|-------------|--------|
| 1 | **Backend Java** | Upgrade Kogito BOM → 10.2.0, kogito-maven-plugin, dipendenze Spring Boot 3.3.5 | Backend |
| 2 | **Configurazione Kogito** | KogitoDataSourceConfig, DataIndexEventPublisher, KogitoBpmEngineAdapter, SecurityConfig CORS | Backend |
| 3 | **Docker Stack** | `docker-compose.kogito.yml` con data-index-db (PostgreSQL 16), data-index, management-console, task-console | DevOps |
| 4 | **Dockerfile** | Single-stage con JAR pre-buildato locale | DevOps |
| 5 | **Flyway Migrations** | Risoluzione conflict V108/V109, verifica dual-location | Backend |
| 6 | **Smoke Test** | Verifica endpoint salute di tutti i container | QA |

---

## Task Assignment

### Backend Agent

- [x] `pom.xml`: BOM `org.kie.kogito:kogito-bom:10.2.0`
- [x] `pom.xml`: `kogito-maven-plugin:10.2.0` con `<goal>generateModel</goal>` esplicito legato a `process-classes`
- [x] `pom.xml`: dipendenze `jbpm-spring-boot-starter`, `drools-decisions-spring-boot-starter`, `kie-addons-springboot-persistence-jdbc`, `flyway-mysql`
- [x] `KogitoDataSourceConfig.java`: import `JDBCProcessInstancesFactory`, costruttore 4 parametri
- [x] `DataIndexEventPublisher.java`: import `UserTaskInstanceDataEvent`
- [x] `KogitoBpmEngineAdapter.java`: `UnitOfWorkManager` via costruttore Spring
- [x] `ManagementProcessController.java`: endpoint `GET /{processId}/nodes/{nodeId}`
- [x] `SecurityConfig.java`: CORS per `localhost:8083`, `localhost:8084`; permitAll `/management/**` e `/anc_pratica/**`
- [x] `application.yml`: blocco `kogito:` con `service.url`, `dataindex.http.url`, `jobs-service.url`
- [x] `application-poc.yml`: URL container Kogito

### DevOps / Coordinator

- [x] `docker-compose.kogito.yml`: aggiunto `data-index-db` (PostgreSQL 16)
- [x] `docker-compose.kogito.yml`: `kogito-data-index` con QUARKUS_KAFKA_HEALTH_ENABLED=false, healthcheck su `/q/health/live`
- [x] `docker-compose.kogito.yml`: `kogito-management-console` porta 8083
- [x] `docker-compose.kogito.yml`: `kogito-task-console` porta 8084
- [x] `kogito-backend.Dockerfile`: single-stage con JAR pre-buildato

### QA Agent

- [x] Backend `GET /actuator/health/readiness` → HTTP 200 `{"status":"UP"}`
- [x] Data-Index `GET /q/health/live` → HTTP 200 `{"status":"UP"}`
- [x] Management Console `GET http://localhost:8083` → HTTP 200
- [x] Task Console `GET http://localhost:8084` → HTTP 200

---

## Dipendenze Sprint 0

```
data-index-db (PostgreSQL 16)
    └─► kogito-data-index
            └─► kogito-backend  (KOGITO_DATAINDEX_HTTP_URL)
                    ├─► kogito-management-console (Management Console → backend + data-index)
                    └─► kogito-task-console        (Task Console → backend)
```

---

## Criteri di Accettazione Sprint 0

- [x] `mvn clean package` BUILD SUCCESS con `kogito:generateModel` eseguito
- [x] ConfigBean presente nel JAR (verificato con `jar tf`)
- [x] Tutti i container healthy al `docker compose up`
- [x] Nessun errore Flyway al boot
- [x] Smoke test HTTP 200 su tutti e 4 gli endpoint
- [x] Vertical slice BPM eseguibile localmente
