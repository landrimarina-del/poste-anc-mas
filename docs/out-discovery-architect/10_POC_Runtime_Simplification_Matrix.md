# 10 - POC Runtime Simplification Matrix

> Matrice esplicita delle sostituzioni adottate nella POC rispetto all'architettura target. Ogni riga documenta: cosa prevede il target, cosa adotta la POC, perché, e l'impatto evolutivo per arrivare al target senza rifondare.

| # | Componente architetturale (target) | Framework / servizio enterprise previsto | Sostituzione adottata in POC | Motivazione | Impatto evolutivo verso target |
|---|---|---|---|---|---|
| 1 | Container orchestration | Kubernetes (Vincoli §4 + DOC2) | Docker + Docker Compose | Esecuzione locale singola, nessun K8s sul PC | Aggiungere manifest K8s/Helm; bounded context, immagini, healthcheck e variabili d'ambiente già pronti |
| 2 | API Gateway | Apache APISIX (DOC2) | NGINX reverse proxy | APISIX richiede etcd + plugin runtime; oversized per POC | Sostituzione del reverse proxy con APISIX: routing e header invariati; aggiunte policy (rate limit, OIDC) |
| 3 | IAM | Keycloak + AD/Entra (DOC2 + Vincoli §6) | Basic Auth + tabella `user` con ruoli | AD non disponibile localmente; vincoli ammettono fallback Basic per test | Introduzione Keycloak: l'AUTH cambia da Basic a JWT/OIDC. I controller usano già un'astrazione `Principal` con ruoli → modifica solo del filtro di sicurezza |
| 4 | BPM esterno (sistema BPM Poste) | Sistema BPM Poste reale (Interface Agreement BPM↔SD) | `bpm-stub` container REST | Sistema non disponibile localmente | Sostituire base URL del client e credenziali; payload Interface Agreement BPM↔SD invariato |
| 5 | Sinergia / PIX | Sistema Sinergia reale | `sinergia-stub` container | Sistema non disponibile localmente | Idem (cambio endpoint/credenziali) |
| 6 | Data Lake | Pipeline reale verso Data Lake | Placeholder log su file | Non disponibile localmente; FUTURE_ENTERPRISE per la POC | Aggiungere produttore eventi sul `event_outbox`; nessun cambio del modello |
| 7 | Object storage | Object storage S3 enterprise / proprietario (Vincoli §2) | MinIO single-node (S3-compatible) | Compatibilità API S3 garantita | Cambiare endpoint/credenziali S3; nessun refactoring |
| 8 | DB transazionale (HA) | MariaDB cluster HA (Vincoli §1) | MariaDB single-node | Esecuzione locale, niente cluster | Configurazione HA aggiunta in target; schema applicativo invariato |
| 9 | Broker eventi (audit/notifiche) | Message broker / event streaming (DOC2) | Pattern **outbox** + dispatcher in-process | Evita Kafka/RabbitMQ in POC | Aggiungere broker (es. RabbitMQ/Kafka): produttori invariati, consumer rimpiazzati da subscriber del broker |
| 10 | BPM engine deploy model | Flowable 7 servizio dedicato (DOC2) | Flowable 7 **embedded** nel monolite | Riduce numero di container; mantiene contratti | Estrazione del modulo `workflow/` come servizio separato, definizioni BPMN già condivise via `workflows/bpmn/` |
| 11 | Modello applicativo | Few-services per macro-modulo | Modular monolith con i 6 BC come moduli interni | Riduce overhead operativo della POC | Split per package Maven/Gradle in moduli runtime; API REST già pubblicate, schema DB già isolato per BC |
| 12 | mTLS / Service mesh | Cilium / Istio (DOC2) | Solo TLS sul reverse proxy (opzionale in POC) | Service mesh ingiustificato sotto K8s assente | Introduzione service mesh quando si passa a K8s con ≥10 servizi (allineato all'anti-pattern DOC1) |
| 13 | Observability stack | Stack centralizzato metriche/log/trace (DOC2) | Log JSON su stdout + Spring Actuator | Locale; tracing predisposto ma non attivato | Aggiungere agent OpenTelemetry; istrumentazione applicativa invariata |
| 14 | Pipeline CI/CD | CI/CD + GitOps (ArgoCD) (DOC2) | Build locale via `docker compose` | Nessuna pipeline necessaria all'esecuzione POC | Introduzione pipeline + GitOps; Dockerfile e immagini riutilizzabili |
| 15 | Secrets management | OpenBao / Vault (DOC2) | Variabili d'ambiente + `.env` locale | Locale | Sostituzione del meccanismo di lettura secret; codice applicativo invariato |
| 16 | Cache distribuita | Valkey / Redis (DOC2) | Cache in-process (Caffeine) o assente | Volumi POC bassi | Introduzione cache distribuita: pattern di accesso invariato (interfaccia repository/cache) |
| 17 | Multi-utente runtime condiviso | Ambiente centralizzato per demo (Vincoli §5) | Singola istanza locale (un dev alla volta) | Limite esplicito dei vincoli | Deploy su ambiente demo condiviso seguendo vincolo "non parallelo" o passaggio a istanze multiple |
| 18 | Frontend meta-framework | Next.js (DOC2) | React SPA buildata staticamente, servita da NGINX | SSR non richiesto dal dominio (UI interna BO) | Migrazione a Next.js opzionale; struttura `features/` già modulare |
| 19 | Conservazione documentale a norma | ECM / archiviazione long-term | Object storage MinIO senza politiche di retention | OUT_OF_SCOPE per la POC (BA) | Integrazione ECM o policy di retention; metadati allegati già strutturati |
| 20 | Process mining | PM4Py + dashboard (DOC2) | Non presente in POC | FUTURE_ENTERPRISE | Eventi di dominio già emessi: producer per event log XES/OCEL aggiungibile senza refactoring di workflow |

## Coerenza con principi DOC1

- **P1 No vendor lock-in**: tutte le sostituzioni POC sono OSI-approved; nessuna API cloud-proprietaria.
- **P2 Cloud-native**: container come unità di deploy mantenuta; passaggio a K8s solo configurazione/manifest.
- **P4 API-first**: contratti applicativi e Interface Agreement BPM↔SD invariati nelle sostituzioni 3, 4, 5, 7, 9, 10.
- **P8 Scalabilità progressiva**: il monolith modulare (riga 11) è esattamente il pattern raccomandato (DOC1 §3.5 / anti-pattern "Microservizi dallo Sprint 1").
- **P9 DevOps**: la mancanza di pipeline (riga 14) è una semplificazione esplicita, non un debito architetturale: la struttura repository è già pronta.

## Cosa NON viene semplificato (invariante)

- Bounded context e responsabilità (cap. 01).
- Workflow BPMN ANC e human task (cap. 04).
- Lifecycle pratica + ownership transizioni (cap. 06).
- Contratti API verso FE e contratto Interface Agreement BPM↔SD verso BPM (cap. 05).
- Schema dati logico (cap. 09; modello fisico a cura DBA).
- Convenzioni di naming, modularizzazione e dipendenze (cap. 08).
