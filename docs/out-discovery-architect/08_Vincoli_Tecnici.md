# 08 - Vincoli Tecnici

> Regole vincolanti per il Development MAS. Derivano da: vincoli Poste, Catalogo Tecnologico, principi DOC1, esigenze del porting.

## 1. Naming convention

### Generali
- Lingua: **inglese** per codice, identificatori tecnici, log; **italiano** per UI e label business.
- Identificatori case:
  - package Java: `it.poste.anc.<bounded-context>` (lowercase).
  - classi: `PascalCase`; metodi/variabili: `camelCase`; costanti: `UPPER_SNAKE_CASE`.
  - tabelle / colonne SQL: `snake_case`, singolare (es. `practice`, `practice_state_history`).
  - endpoint REST: kebab-case nel path (`/api/v1/supervision/dashboard/daily-worked`).
  - eventi di dominio: `PascalCase` al passato (`PracticeOpened`, `OutcomeAckReceived`).
  - processi/task BPMN: `dotted.lowercase` (`anc.main`, `task.acceptPractice`).

### Specifiche di dominio
- `requestId` (camelCase) usato come chiave di riconciliazione cross-sistema della nuova piattaforma.
- `idWorkItem`: chiave di idempotenza in ingresso da BPM.
- Stati pratica in maiuscolo separati da underscore: `APERTA`, `IN_LAVORAZIONE`, `IN_ATTESA_CONFERMA_BPM`, `CHIUSA_OK`, `CHIUSA_KO`.
- Codici esito BPM: `OK`, `KO`; mapping causali → array `koCodes`.
- `resultCode`: numerico negativo per errori (`-4` payload non valido, `-5` idempotenza); `0` per success.

## 2. Regole di modularizzazione

- Ogni **bounded context** è un package Java/TypeScript dedicato. Nessun import diretto cross-context su classi `domain/` o `persistence/`.
- Comunicazione cross-context **solo tramite**:
  - API applicative del context (`api/`),
  - eventi di dominio (`events/`).
- Le tabelle DB di un context non possono essere accedute in JOIN da un altro context: query aggregate (es. supervisione) usano viste o servizi pubblicati.
- Modulo `shared/` contiene SOLO: tipi tecnici, security, audit, outbox, errori comuni, configurazione web. Vietato inserire logica di business.
- Frontend: `features/<area>/` non può importare da un'altra `features/` (riusare via `shared/`).

## 3. Dipendenze consentite / vietate

### Consentite (allineate al Catalogo Tecnologico e ai vincoli Poste)
- Backend: **Java/OpenJDK 21 LTS** (decisione Coordinator 2026-05-13, D6), Spring Boot 3.3+, Flowable 7 embedded, MariaDB driver, MinIO/S3 client.
- Frontend: React + TypeScript, libreria UI open source, libreria viewer documenti open source.
- Infra POC: Docker, Docker Compose, NGINX.
- Test: JUnit 5, AssertJ, Testcontainers (per integration test su MariaDB/MinIO).

### Vietate (allineate ai vincoli)
- Dipendenze Oracle (DB, JDK proprietario, IAM proprietario).
- Componenti SaaS proprietari (Auth0, Pinecone, Celonis, MuleSoft, ecc.).
- Licenze non OSI-approved (BUSL, SSPL): es. Vault HashiCorp, Redis ≥7.4 (usare Valkey se necessario).
- Camunda 7 CE (archiviato).
- Framework giovani/instabili o non documentati a livello enterprise.
- Cloud-specific managed services (AWS API Gateway, Azure APIM, ecc.).
- Microservizi prematuri in POC (mantenere modular monolith).

## 4. Principi implementativi

- **API-first**: contratto OpenAPI definito prima del codice (`contracts/openapi/`).
- **Contract-first verso BPM**: schema dei payload Interface Agreement BPM↔SD versionato e immutabile in POC.
- **Idempotenza** end-to-end: ogni mutazione critica (apertura pratica, ack esito) supporta retry sicuro.
- **Pure functions per esito checklist**: nessun side-effect; output deterministico dalle risposte.
- **Outbox pattern** per eventi di dominio: persistere evento nella stessa transazione del cambio stato; dispatcher in-process in POC, broker nel target.
- **Audit by default**: ogni mutazione produce evento; il consumatore audit non è opzionale.
- **No dato sensibile in log**: PII oscurata; log JSON strutturato.
- **Test pyramid**: unit > integration > smoke E2E; TestContainers per integration su MariaDB/MinIO.
- **Configurabilità**: nessun valore di ambiente hardcoded; tutto via `application.yml` + variabili.
- **Sicurezza minima POC**: TLS opzionale in locale ma supportato; in target obbligatorio. Auth Basic accettabile solo come fallback dichiarato dai vincoli.

## 5. Divergenze — stato

> Decisioni del Coordinator del 2026-05-13 recepite. Le voci risolte restano tracciate per audit; quelle aperte sono da gestire prima dell'on-boarding target.

### Risolte (recepite in POC/target)

- **D1 — DB transazionale** ✅ Decisione: **MariaDB** confermato (vincolo Poste). DOC2 (PostgreSQL) lasciato come riferimento di catalogo; lo schema applicativo resta SQL standard per non precludere alternative future.
- **D2 — IAM** ✅ Decisione: target **Keycloak + AD/Entra** confermato. POC mantiene Basic Auth come fallback dichiarato dai vincoli.
- **D3 — API Gateway** ✅ Decisione: target **APISIX** confermato (DOC2). POC mantiene NGINX reverse proxy. Le criticità emerse nei vincoli §7 verranno tracciate sul tema delivery.
- **D4 — Compatibilità DB** ✅ Decisione: sotto-set SQL ammesso da formalizzare a cura del DBA (in carico Discovery-DBA).
- **D5 — Modello applicativo** ✅ Decisione: percorso **modular monolith → few services → microservizi selettivi** (P8/DOC1) confermato. POC adotta modular monolith con i 6 BC come moduli interni a confini espliciti.
- **D6 — Versione Java** ✅ Decisione: **Java 21 LTS**. Aggiornare runtime POC da Java 17 a Java 21 (Spring Boot 3.3+). Cambio di base image Docker (`eclipse-temurin:21-jre`), `pom.xml` `<java.version>21</java.version>`, abilitazione Virtual Threads dove appropriato. Nessun impatto su bounded context, contratti o schema.
- **D7 — Object storage** ✅ Decisione: MinIO (AGPL 3.0) accettato per POC; nel target si userà storage S3-compatible enterprise/proprietario (vincoli §2). Il client S3 applicativo resta invariato.
- **D8 — TLS "2.x"** ✅ Decisione: lettura come refuso; baseline operativa “TLS moderno (≥1.2, preferibilmente 1.3)”.
- **D9 — Documento Discovery ANC** ✅ Risolto: il documento di riferimento è `Attivazione nuova carta_Discovery.md` ed è presente in `docs/requirements/source-of-truth/`. Aggiornati i riferimenti negli output Architect.
- **D10 — Process orchestration target** ✅ Decisione: per ANC è sufficiente **Flowable 7** (BPMN + human task); Temporal NON adottato. Resta come riferimento di catalogo per scenari tecnici futuri (saga long-running) se emergeranno.
- **D11 — Profilo compliance Enterprise/PA** ✅ Confermato perimetro Enterprise. Gap della POC light (mTLS inter-servizio, chaos testing DORA Art. 25, SBOM in CI DORA Art. 28, zero-downtime deploy) restano predisposti come slot e vanno introdotti nel target. Non bloccanti per la POC.

### Aperte

Nessuna divergenza aperta al momento. Eventuali nuove inconsistenze cross-agent verranno aggiunte qui per essere risolte dal Coordinator (Consistency Rule).

## 6. Vincoli di compliance preservati (Poste / regolamentari)

- TLS sui canali esterni.
- Auditabilità completa (ogni azione tracciata: cap. 04 §6, cap. 06 §3).
- Separazione ambiente sviluppo locale vs runtime condiviso (vincolo §5).
- RBAC su gruppi gerarchici stile Appian preservato (ruoli OPERATORE, SUPERVISORE; gruppo GRUPPO_OPERATORE_ANC).
