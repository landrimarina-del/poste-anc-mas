# ANC Backend — Sprint 0 Foundation

Modular monolith Spring Boot 3.3 (Java 21) per la Scrivania Digitale ANC.
In questo sprint sono attivi solo i moduli `shared/security`, `shared/common` e gli
endpoint M-IAM (`/auth/login`, `/auth/logout`, `/auth/me`). I 6 bounded context
applicativi (`practice`, `workflow`, `document`, `bpmgw`, `supervision`, `signals`)
sono pacchetti placeholder e verranno popolati negli sprint successivi.

## Prerequisiti

- Docker Desktop 24+ / Docker Engine + Docker Compose v2
- (opzionale, per build locale fuori docker) JDK 21 + Maven 3.9+

## Stack avviabile in locale

Dalla root del repository:

```bash
docker compose up --build
```

Servizi attivi:

| Servizio  | Porta host | Note |
|-----------|------------|------|
| `db`      | 3306       | MariaDB 10.11, volume persistente `anc-db-data` |
| `backend` | 8080       | Spring Boot, profilo `poc` |

Reset completo (cancella volume DB):

```bash
docker compose down -v
docker compose up --build
```

## Endpoint disponibili in Sprint 0

| Metodo | Path                            | Auth | Note |
|--------|---------------------------------|------|------|
| POST   | `/api/v1/auth/login`            | no   | body `{"username":"...","password":"..."}` |
| POST   | `/api/v1/auth/logout`           | si   | invalida sessione |
| GET    | `/api/v1/auth/me`               | si   | profilo + ruoli + gruppi utente corrente |
| GET    | `/actuator/health/liveness`     | no   | probe Kubernetes-style |
| GET    | `/actuator/health/readiness`    | no   | probe Kubernetes-style |

Tutte le response API hanno shape uniforme `{ resultCode, resultMessage, details }`
(`resultCode=0` ⇒ OK).

## Utenti demo (06_Dati_POC.md §1)

Password POC unica: `Demo1234!`

| Username     | Ruolo            | Gruppo                |
|--------------|------------------|-----------------------|
| `op.rossi`   | OPERATORE_ANC    | GRUPPO_OPERATORE_ANC  |
| `op.bianchi` | OPERATORE_ANC    | GRUPPO_OPERATORE_ANC  |
| `sup.verdi`  | SUPERVISORE_ANC  | GRUPPO_OPERATORE_ANC  |
| `admin`      | ADMIN            | —                     |

## Smoke test

```bash
# /auth/me con HTTP Basic
curl -u op.rossi:Demo1234! http://localhost:8080/api/v1/auth/me

# /auth/login (apre sessione)
curl -i -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"op.rossi","password":"Demo1234!"}'
```

## Run locale senza Docker (sviluppo BE)

1. Avviare solo il DB: `docker compose up -d db`
2. Avviare l'applicazione: `mvn -f apps/backend/pom.xml spring-boot:run`
   (profilo `local` di default, DB su `localhost:3306`).
