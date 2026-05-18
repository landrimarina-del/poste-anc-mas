# 07 - Deployment Locale

## 1. Stack eseguibile sul PC dell'utente

Esecuzione tramite **Docker Compose** con un unico comando: `docker compose up -d`.

### Servizi
| Service | Ruolo | Porte host |
|---|---|---|
| `mariadb` | DB transazionale (vincolo Poste) | 3306 |
| `minio` | Object storage allegati (S3-compatible) | 9000 (S3), 9001 (console) |
| `backend` | Modular monolith Spring Boot (Flowable embedded) | 8081 |
| `frontend-builder` (one-shot) | Build statica React in volume condiviso | — |
| `reverse-proxy` | NGINX → serve FE + proxy `/api` → backend | **8080** |
| `bpm-stub` | Stub BPM esterno (in/out Interface Agreement BPM↔SD) | 8090 |
| `sinergia-stub` | Stub PIX | 8091 |

### Volumi
- `mariadb-data` → persistenza DB.
- `minio-data` → bucket allegati `anc-attachments`.
- `frontend-dist` → artefatti FE serviti da NGINX.
- `backend-logs` → log JSON.

### Rete
- Bridge `anc-net` (interna). Solo `reverse-proxy`, `minio` console, `bpm-stub`, `sinergia-stub` esposti su host.

## 2. Dipendenze runtime

| Componente | Requisito host |
|---|---|
| Docker Desktop | versione recente con Compose v2 |
| Risorse minime | 4 vCPU, 8 GB RAM, 10 GB disco |
| Browser | qualunque browser moderno per la UI |
| (opz.) Postman / curl | per esercitare manualmente `bpm-stub` |

Nessuna installazione di Java/Node/MariaDB sull'host: tutto in container.

## 3. Startup order servizi

```
   ┌──────────┐   ┌──────────┐
   │ mariadb  │   │  minio   │     (livello 0: storage)
   └────┬─────┘   └────┬─────┘
        │ healthy      │ healthy
        ▼              ▼
        ┌──────────────┐
        │   backend    │            (livello 1: BE; esegue migrazioni schema)
        └──────┬───────┘
               │ healthy /actuator/health
               ▼
   ┌──────────────────────────┐
   │ bpm-stub  sinergia-stub  │    (livello 2: integrazioni esterne stub)
   └──────────────┬───────────┘
                  │
                  ▼
        ┌──────────────────┐
        │ frontend-builder │       (one-shot: build assets statici)
        └────────┬─────────┘
                 │
                 ▼
        ┌──────────────────┐
        │  reverse-proxy   │       (livello 3: ingresso utente)
        └──────────────────┘
```

Implementazione: `depends_on` con `condition: service_healthy` su `mariadb`, `minio`, `backend`. `frontend-builder` è un service `restart: no` che esce a 0; `reverse-proxy` parte dopo.

## 4. Configurazione (profili)

- `application.yml` → tre profili: `local` (sviluppo IDE), `poc` (default Docker Compose), `target` (riferimento documentale).
- Variabili d'ambiente principali (POC):
  - `SPRING_PROFILES_ACTIVE=poc`
  - `DB_URL=jdbc:mariadb://mariadb:3306/anc`
  - `S3_ENDPOINT=http://minio:9000`, `S3_BUCKET=anc-attachments`
  - `BPM_STUB_BASE_URL=http://bpm-stub:8090`
  - `SINERGIA_STUB_BASE_URL=http://sinergia-stub:8091`
  - `AUTH_MODE=basic` (POC) → `oidc` (target)

## 5. Migrazioni schema

- Eseguite all'avvio del `backend` (Flyway-ready, owner: Discovery-DBA).
- Script in `infra/db/migrations/` versionati `V<n>__<descrizione>.sql`.
- Seed minimo: utenti demo, ruoli, gruppo `GRUPPO_OPERATORE_ANC` (dataset definito da DBA).

## 6. Smoke test post-deploy

1. `docker compose ps` → tutti i servizi healthy.
2. Apertura `http://localhost:8080` → UI di login.
3. Login OPERATORE / SUPERVISORE.
4. Trigger apertura pratica via `bpm-stub` (es. `POST /trigger/open-practice` mock).
5. Pratica visibile in repository, task in Lista Attività.
6. ACCETTA → tipizzazione → checklist → CHIUDI PRATICA → `bpm-stub /ack` → stato finale.

> Lo script di automazione è prevista come `scripts/smoke-anc.ps1` (responsabilità sviluppo, non Architect).

## 7. Note operative

- Nessun cluster, nessun K8s, nessun GitOps in POC.
- Nessuna pipeline CI/CD richiesta per l'esecuzione locale (basta `docker compose up`).
- Reset rapido: `docker compose down -v` (cancella i volumi, ripristina seed).
- Limitazione: il runtime POC non può essere usato in parallelo da più sviluppatori (vincolo §5 Vincoli).
