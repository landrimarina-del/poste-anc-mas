# 02 - Architettura Runtime POC

> Modalità: **Light Local** eseguibile sul PC dell'utente. Mantiene invariati: bounded context, contratti applicativi, workflow, modularizzazione. Riduce solo: complessità infrastrutturale e dipendenze enterprise non disponibili localmente.

## 1. Stack runtime POC (allineato ai vincoli)

| Slot | POC (light) | Target enterprise (riferimento) |
|---|---|---|
| Frontend | React (build statica servita dal reverse proxy) | React + meta-framework (DOC2) |
| Reverse Proxy / Gateway | NGINX (reverse proxy + serve FE) | Apache APISIX (DOC2) |
| Backend | Java 21 / Spring Boot 3 (modular monolith) | Spring Boot 3 — few-services (DOC2) |
| BPM engine (interno a SD) | Flowable 7 embedded nel monolite | Flowable 7 servizio dedicato (DOC2) |
| DB transazionale | **MariaDB** (vincolo Poste) | **MariaDB** (vincolo Poste; DOC2 prevede PostgreSQL → segnalata divergenza, vedi `Vincoli Tecnici §5`) |
| Storage allegati | MinIO (S3-compatible, single-node) | Object storage S3-compatible enterprise / proprietario |
| IAM | Basic Auth locale + ruoli su DB (fallback dichiarato dai vincoli) | Keycloak + integrazione AD/Entra (DOC2 + Vincoli) |
| BPM esterno (Poste) | **Stub container** (Mock REST) | Sistema BPM Poste reale |
| Sinergia/PIX | **Stub container** | Sistema Sinergia reale |
| Data Lake | **Non presente in POC** (placeholder log) | Pipeline reale verso Data Lake |
| Containerizzazione | Docker + Docker Compose | Kubernetes (DOC2 / Vincoli) |
| Observability | Log JSON su stdout + Spring Boot Actuator | Stack metriche/log/trace centralizzato |

## 2. Componenti runtime POC

```
┌────────────────────────────────────────────────────────────┐
│                     Browser utente                          │
└────────────────────────────────────────────────────────────┘
                          │ HTTPS (self-signed in dev)
                          ▼
┌──────────────────────┐
│  reverse-proxy        │ NGINX → /api → backend; / → FE
└──────────────────────┘
       │                                  │
       ▼                                  ▼
┌──────────────┐               ┌──────────────────────────────┐
│  frontend     │               │  backend (modular monolith)   │
│  (React)      │               │  - M-Practice                 │
└──────────────┘               │  - M-Workflow (Flowable emb.) │
                                │  - M-Document                 │
                                │  - M-BPM Integration          │
                                │  - M-Supervision              │
                                │  - M-Signal                   │
                                │  - M-IAM (Basic + DB)         │
                                │  - M-Audit                    │
                                └──────────────────────────────┘
                                   │              │           │
                                   ▼              ▼           ▼
                              ┌─────────┐  ┌──────────┐  ┌────────────┐
                              │ MariaDB │  │  MinIO   │  │ stubs ext. │
                              │         │  │ (S3 OSS) │  │ bpm-stub   │
                              └─────────┘  └──────────┘  │ sinergia-  │
                                                          │ stub       │
                                                          └────────────┘
```

## 3. Servizi (Docker Compose)

| Service | Immagine (riferimento) | Porte | Note |
|---|---|---|---|
| `reverse-proxy` | nginx:alpine | 8080 | reverse proxy + serve FE statico |
| `frontend` | build React (multi-stage) | — | servito da `reverse-proxy` |
| `backend` | OpenJDK 21 + jar Spring Boot | 8081 (interno) | embedded Flowable |
| `mariadb` | mariadb:11 LTS | 3306 (interno) | volume persistente |
| `minio` | minio/minio | 9000 / 9001 | bucket `anc-attachments` |
| `bpm-stub` | container Java/Spring stub | 8090 | simula BPM (apertura, ack esito) |
| `sinergia-stub` | container Java/Spring stub | 8091 | simula PIX |

Tutti i servizi su rete bridge `anc-net`. Volumi persistenti per `mariadb` e `minio`.

## 4. Componenti mockati o semplificati (rispetto al target)

| Componente target | Sostituzione POC | Motivazione | Impatto evolutivo |
|---|---|---|---|
| Kubernetes / GitOps | Docker Compose | Esecuzione locale singola | Manifest K8s da introdurre; bounded context invariati |
| Apache APISIX | NGINX reverse proxy | Routing minimale sufficiente per POC | Aggiungere policy/auth a livello gateway nel target |
| Keycloak + AD/Entra | Basic Auth + ruoli su tabella `users` | Nessuna AD locale | Integrare Keycloak: token JWT al posto di Basic, contratti API invariati |
| BPM Poste (sistema esterno) | `bpm-stub` REST | Sistema non disponibile localmente | Sostituire endpoint stub con BPM reale: contratto Interface Agreement BPM↔SD invariato |
| Sinergia/PIX | `sinergia-stub` | Stesso motivo | Idem |
| Data Lake | Log su file | Stesso motivo | Aggiungere produttore eventi nel target |
| Object storage enterprise | MinIO single-node | Compatibile S3 | Cambio endpoint/credenziali, API S3 invariata |
| MariaDB HA / replica | MariaDB single-node | Esecuzione locale | Configurazione HA aggiunta nel target, schema invariato |
| Broker eventi (audit/notifiche) | Tabella `event_outbox` + dispatcher in-process | Evita Kafka/RabbitMQ in POC | Pattern outbox: switch a broker mantiene API produttori invariate |
| Observability stack | Log JSON + Actuator | Local-only | Aggiungere agent OTel; istrumentazione applicativa invariata |

## 5. Stub contratti

- `bpm-stub`: espone gli endpoint **funzionalmente equivalenti** all'Interface Agreement BPM↔SD (`InterfaceAgreement.md` + `BPM_SubmitWorkItem_v1.1.yaml`) per (a) inviare apertura pratica verso SD; (b) ricevere esiti da SD; (c) inviare ack di chiusura. Espone una mini-UI/CLI per scatenare scenari (apertura nominale, KO multipli, idempotenza, ack ritardato).
- `sinergia-stub`: espone endpoint per apertura ticket, recupero pratiche, recupero dettaglio, conformi al perimetro emerso nei test ST57–ST60.

## 6. Coerenza POC ↔ Target

- **Invariati**: bounded context (cap. 01), confini di responsabilità, contratti applicativi (cap. 05), workflow BPMN (cap. 04), lifecycle stati (cap. 06), schema dati logico.
- **Variabili POC**: deploy unit (monolith vs services), provider IAM, gateway, observability, broker, HA storage. Tutte sostituzioni configurazione/infrastruttura, non refactoring di dominio.
