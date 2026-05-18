# 03 - Package Structure

> La struttura riflette i 6 bounded context (vedi `01_Architettura_Applicativa.md`). Nel POC è un **modular monolith** (un processo, package separati); nel target ogni modulo applicativo può estrarsi a servizio mantenendo lo stesso package layout.

## 1. Repository monorepo

```
poste-anc-mas/
├── apps/
│   ├── frontend/              # React SPA
│   ├── backend/               # Spring Boot modular monolith
│   ├── bpm-stub/              # stub BPM esterno
│   └── sinergia-stub/         # stub Sinergia/PIX
├── infra/
│   ├── docker/                # Dockerfile e compose override
│   ├── db/migrations/         # script SQL versionati (Flyway-ready)
│   └── nginx/                 # configurazione reverse proxy
├── workflows/
│   └── bpmn/                  # definizioni BPMN ANC e human task
├── contracts/
│   ├── openapi/               # contratti API esposte da SD
│   └── ia-v1.4/               # contratto interno BPM (riferimento funzionale)
├── scripts/                   # smoke-test, seed, utility
└── docs/                      # output Discovery & deliverable
```

## 2. Frontend (`apps/frontend`)

```
src/
├── app/
│   ├── shell/                 # layout, tab Home/Attività/Pratiche, navbar
│   ├── routes.ts              # routing principale
│   └── auth/                  # login, guard di rotta, gestione sessione
├── shared/
│   ├── ui/                    # componenti base, design system
│   ├── viewer/                # viewer documenti + fallback download
│   ├── tables/                # liste filtrabili/ordinabili/paginate
│   └── charts/                # istogrammi supervisore
├── features/
│   ├── home/                  # contatori real-time, link favoriti, widget
│   ├── practices/             # repository pratiche + dettaglio (Riepilogo/Cronologia/Stati/Azioni Correlate)
│   ├── activities/            # Lista Attività + accettazione task
│   ├── intake/                # tipizzazione + viewer
│   ├── checklist/             # checklist Verbale / Carta + esito
│   ├── supervisor/            # Riassegna Attività + dashboard istogrammi
│   └── signals/               # Dashboard Segnalazioni Sinergia
└── core/
    ├── api/                   # client API per BC
    ├── i18n/                  # IT (POC) — predisposto multi-lingua
    └── observability/         # logger, error boundary
```

## 3. Backend (`apps/backend`)

```
src/main/java/it/poste/anc/
├── BackendApplication.java
├── shared/
│   ├── audit/                 # M-Audit Service (eventi dominio → log/cronologia/stati)
│   ├── security/              # M-IAM (auth, RBAC, ruoli OPERATORE/SUPERVISORE)
│   ├── outbox/                # event outbox (POC) → broker (target)
│   └── common/                # types, errors, validation, web config
├── practice/                  # BC1 - Practice Management
│   ├── api/                   # controller REST
│   ├── domain/                # entità Practice, ClientData, CardData
│   ├── application/           # use case
│   ├── persistence/           # repository, JPA entity, query
│   └── events/                # eventi di dominio
├── workflow/                  # BC2 - Workflow / Task Orchestration
│   ├── engine/                # configurazione Flowable embedded
│   ├── delegates/             # service task delegate
│   ├── api/                   # controller task (lista, accept, reassign)
│   ├── application/           # use case task management
│   └── persistence/           # tabelle owner task & assegnazioni
├── document/                  # BC3 - Document & Checklist
│   ├── api/                   # upload, download, viewer metadata
│   ├── domain/                # Attachment, DocumentType, ChecklistVerbale, ChecklistCarta
│   ├── application/           # tipizzazione, calcolo esito
│   ├── persistence/
│   └── storage/               # adapter object storage (MinIO/S3)
├── bpmgw/                     # BC4 - BPM Integration Gateway
│   ├── inbound/               # ricezione apertura pratica (resultCode -4/-5)
│   ├── outbound/              # invio esiti / ricezione ack
│   ├── mapping/               # Interface Agreement BPM↔SD ⇄ modello interno
│   └── idempotency/
├── supervision/               # BC5 - Supervision & Reporting
│   ├── api/                   # contatori, istogrammi, riassegnazione
│   ├── application/
│   └── persistence/           # query aggregate
└── signals/                   # BC6 - Signal Management (Sinergia)
    ├── api/                   # invio/visualizzazione/riassegnazione segnalazioni
    ├── domain/
    ├── persistence/
    └── outbound/              # client stub Sinergia

src/main/resources/
├── application.yml            # profili: local, poc, target
├── db/migration/              # link a /infra/db/migrations
├── bpmn/                      # processi BPMN ANC (link a /workflows/bpmn)
└── openapi/                   # spec esposte (link a /contracts/openapi)
```

## 4. BPM / Workflow (`workflows/bpmn`)

```
workflows/bpmn/
├── anc_main.bpmn              # processo principale ANC: Aperta → In Lavorazione → In Attesa Conferma BPM → Chiusa OK/KO
├── anc_intake.bpmn            # sotto-processo: tipizzazione + checklist + esito
└── anc_signal.bpmn            # processo segnalazioni Sinergia
```

Convenzioni:
- ID processo: `anc.main`, `anc.intake`, `anc.signal`.
- Human task: `task.acceptPractice`, `task.typeAndChecklist`.
- Service task: `svc.openPractice`, `svc.computeOutcome`, `svc.sendOutcomeToBpm`, `svc.finalizeOnAck`.
- Variabili processo: `practiceId`, `requestId`, `idWorkItem`, `documentType`, `outcome`, `koCodes[]`.

## 5. Moduli condivisi

| Modulo | Frontend | Backend |
|---|---|---|
| Audit | logger struttura eventi UI | `shared/audit` (consumer eventi domain) |
| Auth | `app/auth` | `shared/security` |
| Errori/Result | `core/api` | `shared/common` (mapping `resultCode`) |
| i18n | `core/i18n` | `shared/common/i18n` |

## 6. Coerenza POC ↔ Target

Nel target, ciascun package del backend (`practice`, `workflow`, `document`, `bpmgw`, `supervision`, `signals`) è candidato a estrazione in microservizio dedicato senza modificare:
- API esposte (cap. 05),
- Schema dati logico (cap. 06),
- Definizioni BPMN (cap. 04).

L'estrazione richiede solo: split dei moduli Maven/Gradle, separazione DB schema (già logicamente isolati), introduzione del broker reale al posto dell'`outbox` in-process.
