# Sprint 0 — Status

**Sprint**: Sprint 0 — Infrastruttura e POC Runtime  
**Data chiusura**: 2025-05-27  
**Stato**: ✅ COMPLETATO

---

## Riepilogo Stato Container

| Container | Immagine | Porta Host | Stato |
|-----------|---------|-----------|-------|
| `anc-kogito-data-index-db` | `postgres:16` | — | ✅ healthy |
| `anc-kogito-data-index` | `apache/incubator-kie-kogito-data-index-postgresql:10.2.0` | 8082 | ✅ healthy |
| `anc-kogito-mariadb` | `mariadb:10.6` | — | ✅ healthy |
| `anc-kogito-minio` | `minio/minio` | — | ✅ healthy |
| `anc-kogito-bpm-outbound-stub` | locale | — | ✅ healthy |
| `anc-kogito-backend` | `anc/kogito-backend:0.1.0` | 8081 | ✅ healthy |
| `anc-kogito-management-console` | `apache/incubator-kie-kogito-management-console:10.2.0` | 8083 | ✅ healthy |
| `anc-kogito-task-console` | `apache/incubator-kie-kogito-task-console:10.0.0` | 8084 | ✅ up |
| `anc-kogito-frontend` | locale | 3000 | ✅ healthy |
| `anc-kogito-reverse-proxy` | `nginx` | 80 | ✅ started |

---

## Smoke Test Results

| Endpoint | Metodo | HTTP | Risultato |
|----------|--------|------|-----------|
| `http://localhost:8081/actuator/health/readiness` | GET | 200 | `{"status":"UP"}` ✅ |
| `http://localhost:8082/q/health/live` | GET | 200 | `{"status":"UP","checks":[...]}` ✅ |
| `http://localhost:8083` | GET | 200 | Management Console HTML ✅ |
| `http://localhost:8084` | GET | 200 | Task Console HTML ✅ |

**Data smoke test**: 2025-05-27  
**Eseguito da**: Coordinator (orchestrazione)

---

## Risultati Build

| Step | Esito | Note |
|------|-------|------|
| `mvn clean package` | ✅ BUILD SUCCESS | Kogito 10.2.0 |
| `kogito:generateModel` | ✅ Eseguito | Fase `process-classes` |
| ConfigBean nel JAR | ✅ Verificato | `jar tf` |
| Nessun conflict Flyway | ✅ | V108 rimosso, V109 attivo |
| `docker compose build kogito-backend` | ✅ | Single-stage Dockerfile |
| `docker compose up` | ✅ | Tutti i container UP |

---

## Criteri di Accettazione — Verifica Finale

- [x] Backend funzionante (Spring Boot 3.3.5, Kogito 10.2.0)
- [x] Workflow Flowable funzionante (Kogito BPM engine attivo)
- [x] Frontend funzionante
- [x] Smoke test QA — tutti HTTP 200
- [x] Vertical slice BPM eseguibile localmente

---

## Note Operative

- Spring Boot aggiornato a **3.3.5** (NON 3.4.x — incompatibile con Kogito 10.2.0 / BAMOE)
- Flyway: dual location `classpath:db/migration` + `filesystem:${ANC_FLYWAY_SQL_LOCATION}` confermato operativo
- Processo di build: SEMPRE `mvn clean package` (mai build incrementale) per evitare stale files nel JAR
- Dockerfile: single-stage obbligatorio (multi-stage non supportato con kogito-maven-plugin)
