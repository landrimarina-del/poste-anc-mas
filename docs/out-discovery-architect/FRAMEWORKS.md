# Framework utilizzati — apps/kogito

## Backend — `apps/kogito/backend` (Java / Maven)

| Livello | Framework / Libreria | Versione | Ruolo |
|---|---|---|---|
| **Runtime** | Spring Boot | 3.3.5 | Application framework principale |
| **Web** | spring-boot-starter-web | (BOM) | REST API layer |
| **Security** | spring-boot-starter-security | (BOM) | Autenticazione/Autorizzazione |
| **Persistenza ORM** | spring-boot-starter-data-jpa | (BOM) | JPA / Hibernate |
| **Validazione** | spring-boot-starter-validation | (BOM) | Bean Validation (JSR-380) |
| **Monitoring** | spring-boot-starter-actuator | (BOM) | Health / metrics endpoints |
| **BPM Engine** | Kogito (kogito-processes-spring-boot-starter) | 2.44.0.Alpha | Esecuzione processi BPMN embedded |
| **BPM Persistence** | kogito-addons-springboot-persistence-jdbc | 2.44.0.Alpha | Persistenza instance BPMN su JDBC |
| **Decisioni DMN** | kogito-decisions-spring-boot-starter | 2.44.0.Alpha | Runtime regole DMN |
| **API Docs** | springdoc-openapi-starter-webmvc-ui | 2.6.0 | Swagger UI / OpenAPI 3 |
| **DB Migration** | Flyway (flyway-core + flyway-mysql) | (BOM) | Schema migration |
| **Database** | MariaDB (mariadb-java-client) | (BOM) | RDBMS runtime |
| **Object Storage** | AWS SDK S3 v2 | 2.25.50 | Client MinIO (persistenza binari) |
| **Retry** | spring-retry | (BOM) | Retry sincrono outbound BPM |
| **Office** | Apache POI (poi-ooxml) | 5.2.5 | Lettura/scrittura file Excel |
| **Java** | Java 21 | 21 | Runtime JVM |
| **Build** | kogito-maven-plugin | 2.44.0.Alpha | Code generation BPMN → Java a compile-time |
| **Test** | spring-boot-starter-test, H2, spring-security-test | (BOM) | Unit / integration test |

---

## Frontend — `apps/kogito/frontend` (Node.js / Vite)

| Livello | Framework / Libreria | Versione | Ruolo |
|---|---|---|---|
| **UI Framework** | React | ^18.3.1 | Component model SPA |
| **DOM Rendering** | react-dom | ^18.3.1 | Rendering browser |
| **Routing** | react-router-dom | ^6.30.1 | Client-side routing |
| **Build Tool** | Vite | ^5.4.10 | Dev server + bundler |
| **Plugin React** | @vitejs/plugin-react | ^4.3.3 | JSX/Fast Refresh integration |
| **Testing** | Vitest | ^2.1.4 | Unit test runner |
| **Testing** | @testing-library/react | ^16.0.1 | Component testing utilities |
| **Linting** | ESLint 9 + plugin react/hooks | ^9.13.0 | Analisi statica codice |
| **Tipi** | @types/react, @types/react-dom | (devDep) | TypeScript type definitions |

---

## Stack sintetico

```
Backend:  Java 21 + Spring Boot 3.3.x + Kogito 2.44 (BPMN/DMN embedded) + MariaDB + MinIO
Frontend: React 18 + Vite 5 + react-router-dom 6
```
