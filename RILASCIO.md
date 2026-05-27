# Installazione POC Scrivania Digitale ANC — Checklist Cliente

---

## Prerequisiti

- [ ] Docker Desktop (o Docker Engine + Docker Compose) installato e avviato
- [ ] Porte libere: **81**, **8081**, **8082**, **8083**, **8084**, **3306**

---

## Passo 1 — Copiare i file

Copiare sulla macchina cliente:
- `docker-compose.kogito.yml`
- `infra/`
- `apps/kogito/backend/target/anc-backend-*.jar`

---

## Passo 2 — Configurare l URL (solo se il browser accede da altra macchina)

Creare un file `.env` nella stessa cartella di `docker-compose.kogito.yml`:

```
ANC_CORS_ALLOWED_ORIGINS=http://[IP-CLIENTE]:81
```

> Se browser e Docker sono sullo stesso PC, questo passo non serve.

---

## Passo 3 — Avviare lo stack

```
docker compose -f docker-compose.kogito.yml up -d
```

---

## Passo 4 — Verificare lo stato

```
docker compose -f docker-compose.kogito.yml ps
```

Tutti i container devono essere **healthy** o **running**.

Verifica backend: `http://[IP-CLIENTE]:8081/actuator/health` deve rispondere `{"status":"UP"}`

---

## Passo 5 — Accedere

Browser: `http://[IP-CLIENTE]:81`

| Utente | Password | Ruolo |
|---|---|---|
| op.rossi | Demo1234! | Operatore ANC |
| op.bianchi | Demo1234! | Operatore ANC |
| sup.verdi | Demo1234! | Supervisore ANC |
| admin | Demo1234! | Admin |

---

## Note e Limitazioni Note

### Konsole Kogito (Management Console / Task Console)

Le console Kogito (`http://[IP-CLIENTE]:8083` e `http://[IP-CLIENTE]:8084`) sono strumenti di monitoraggio tecnico del workflow BPM, **non** fanno parte dell'interfaccia utente applicativa.

**Architettura persistenza — due database separati:**

| Database | Tecnologia | Scopo |
|---|---|---|
| `anc-kogito-mariadb` | MariaDB | Persistenza applicativa: pratiche, task, utenti, workflow BPM runtime |
| `anc-kogito-data-index-db` | PostgreSQL 15 Alpine | Read-model esclusivo per Management Console e Task Console |

**Perché PostgreSQL per il Data Index?**
L'immagine ufficiale `apache/incubator-kie-kogito-data-index-postgresql:10.2.0` supporta esclusivamente PostgreSQL. Non esiste una variante ufficiale MariaDB. Un'immagine alternativa `kogito-data-index-ephemeral:1.44.1` (PostgreSQL embedded) è stata valutata e scartata perché incompatibile con il formato CloudEvents di Kogito 10.2.0 (errore `ClassCastException` a runtime).

La scelta adottata — `postgres:15-alpine` dedicato solo al Data Index — è il minimo indispensabile per abilitare le console di monitoraggio mantenendo MariaDB come unico database applicativo.
