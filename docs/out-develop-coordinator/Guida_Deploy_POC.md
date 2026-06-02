# Guida Deploy POC — Scrivania Digitale ANC

---

## Installare Docker sul PC del cliente

Prima di procedere, chiedere al cliente:
1. **Che OS ha la macchina di demo?** (Windows / Linux / macOS)
2. **Ha già Docker installato?** (molti PC aziendali con WSL2 ce l'hanno già)
3. **È una macchina personale o un server condiviso?** (impatta sulla scelta licenza)

### Windows (più probabile in contesto Poste)
Installare **Docker Desktop per Windows**:
- Scaricabile da https://www.docker.com/products/docker-desktop/
- Richiede Windows 10/11 con **WSL2** abilitato (funziona su tutte le edizioni, inclusa Home)
- Prima di installare Docker Desktop, abilitare WSL2 da PowerShell come amministratore e riavviare:
  ```powershell
  wsl --install
  ```
- Dopo l'installazione di Docker Desktop, dal terminale funziona direttamente `docker compose up`

> ⚠️ Docker Desktop richiede **licenza a pagamento** per uso aziendale con >250 dipendenti. Per una POC interna/demo è generalmente tollerato, ma da verificare con il cliente.

### Linux (server aziendale, VM)
```bash
curl -fsSL https://get.docker.com | sh
```
Gratuito, nessuna licenza richiesta.

### macOS
Docker Desktop per Mac — stessa situazione licenza di Windows.

---

## Prerequisiti

### Sul tuo PC (chi prepara il pacchetto)
- Java 21
- Maven 3.x
- Docker Desktop

### Sul PC del cliente (chi esegue la demo)
- **Solo Docker Desktop** (Windows/Mac) o **Docker Engine** (Linux)
- Nient'altro — né Java, né Maven, né Node.js

---

## Passo 1 — Build del JAR (sul tuo PC, una volta sola)

```powershell
cd apps\kogito\backend
mvn clean package -DskipTests
```

Verifica che esista il file:
```
apps/kogito/backend/target/anc-backend-*.jar
```

---

## Passo 2 — Preparare il pacchetto da consegnare

Comprimi in uno ZIP i seguenti file/cartelle:

```
docker-compose.kogito.yml
infra/
apps/kogito/backend/target/anc-backend-*.jar
```

> Il JAR è un file JVM standard: gira uguale su Windows, Linux e macOS.

---

## Passo 3 — Installazione sul PC del cliente

1. Copiare e decomprimere lo ZIP
2. Aprire un terminale nella cartella estratta
3. Avviare lo stack:

```bash
docker compose -f docker-compose.kogito.yml up -d
```

> La prima esecuzione scarica le immagini Docker da internet (richiede connessione).
> Le esecuzioni successive funzionano offline.

---

## Passo 4 — Verificare lo stato

```bash
docker compose -f docker-compose.kogito.yml ps
```

Tutti i container devono essere **healthy** o **running**.

Verifica backend:
```
http://localhost:8081/actuator/health
```
Deve rispondere: `{"status":"UP"}`

---

## Passo 5 — Accedere all'applicazione

Browser: **`http://localhost:81`**

| Utente | Password | Ruolo |
|---|---|---|
| op.rossi | Demo1234! | Operatore ANC |
| op.bianchi | Demo1234! | Operatore ANC |
| sup.verdi | Demo1234! | Supervisore ANC |
| admin | Demo1234! | Admin |

---

## Accesso da un altro PC sulla stessa rete (collega/cliente)

### 1. Trova il tuo IP locale

```powershell
ipconfig
# cerca "Indirizzo IPv4"  es. 192.168.1.45
```

### 2. Configura il CORS prima di avviare lo stack

Crea un file `.env` nella stessa cartella di `docker-compose.kogito.yml`:

```env
ANC_CORS_ALLOWED_ORIGINS=http://192.168.1.45:81
```

### 3. Avvia (o riavvia) lo stack

```bash
docker compose -f docker-compose.kogito.yml up -d
```

### 4. Il collega apre il browser

```
http://192.168.1.45:81
```

### 5. Se il collega non riesce a connettersi — sblocca il firewall Windows

```powershell
# Esegui come Amministratore
netsh advfirewall firewall add rule name="POC ANC" dir=in action=allow protocol=TCP localport=81
```

---

## Porte utilizzate dallo stack

| Porta | Servizio |
|---|---|
| **81** | Frontend (UI applicativa) |
| **8081** | Backend REST API |
| **8082** | MinIO Console (object storage allegati) |
| **8083** | Kogito Management Console (monitoraggio BPM tecnico) |
| **8084** | Kogito Task Console (monitoraggio BPM tecnico) |
| **3306** | MariaDB (DB applicativo) |

---

## Fermare lo stack

```bash
docker compose -f docker-compose.kogito.yml down
```

Per fermare **e cancellare i dati** (reset completo):

```bash
docker compose -f docker-compose.kogito.yml down -v
```

---

## Note e Limitazioni

- **Un utente alla volta**: la POC non è progettata per sessioni multiple in parallelo
- **Management Console** (`http://localhost:8083`) e **Task Console** (`http://localhost:8084`) sono console tecniche BPM, non parte dell'UI applicativa
- Il DB applicativo è **MariaDB**; il Data Index Kogito usa **PostgreSQL** separato (solo per le console di monitoraggio)
- Lo stack non richiede internet una volta scaricate le immagini Docker

---

## Compatibilità OS

| Sistema Operativo | Supportato | Note |
|---|---|---|
| Windows 10/11 | ✅ | Docker Desktop |
| Linux (Ubuntu, RHEL, ecc.) | ✅ | Docker Engine + Docker Compose |
| macOS | ✅ | Docker Desktop |

Il `docker-compose.kogito.yml` funziona identico su tutti e tre i sistemi operativi.
