# POC E2E - Evidenze e Runbook su DB Pulito (Sprint 10)

Data esecuzione evidenze: 2026-05-16  
Ambiente: stack Docker locale ANC (backend + frontend + db + minio + reverse-proxy + bpm-stub + bpm-outbound-fake)

## 1) Cosa e stato validato end-to-end

Processi E2E validati:
1. Inbound BPM apertura pratica su DB vuoto.
2. Intake operativo completo: presa in carico task, tipizzazione, checklist, chiusura pratica.
3. ACK BPM outcome fino a stato finale CHIUSA_OK.
4. Capability Sprint 10: export Excel, help checklist, related-actions, audit trail/history, favorites CRUD.
5. Security/rule enforcement: endpoint signals con 200 per supervisore e 403 per operatore.

## 2) Evidenze raccolte oggi (DB pulito reale)

### 2.1 Reset completo e rebuild stack

Comandi eseguiti:
- docker compose down -v --remove-orphans
- docker compose up -d --build
- docker compose ps

Esito sintetico:
- Volumi rimossi e ricreati (DB realmente pulito).
- Servizi core healthy: anc-backend, anc-frontend, anc-db, anc-minio.
- Reverse proxy avviato su porta 80.

### 2.2 Smoke iniziale su DB pulito

Esiti:
- AUTH_OP=200
- EXPORT=200
- FAV_GET=200
- FAV_CREATE_CODE=0
- FAV_DELETE=200
- SIG_SUP=200
- SIG_OP=403

Nota: RELATED/HELP/HISTORY danno 404 prima di creare una pratica (comportamento atteso su DB vuoto).

### 2.3 Creazione pratica da BPM inbound

Chiamata:
- POST /api/v1/bpm/practices

Esito:
- resultCode=0
- details.practiceId=1
- details.state=APERTA

### 2.4 Ciclo intake + chiusura + ack BPM

Sequenza eseguita su practiceId=1:
1. GET /api/v1/tasks -> taskId=1, taskState=IN_CODA
2. POST /api/v1/tasks/1/accept -> 200
3. POST /api/v1/practices/1/intake/typing (VERBALE) -> resultCode=0
4. PUT /api/v1/practices/1/intake/checklist -> resultCode=0
5. GET /api/v1/practices/1/intake/checklist/help/DOCUMENTPRESENT -> 200
6. POST /api/v1/practices/1/intake/close -> 200
7. POST /api/v1/bpm/outcome-ack (outcome OK) -> finalState=CHIUSA_OK

Evidenze stato/audit dopo ACK:
- STATES_AFTER_ACK=4
- HISTORY_AFTER_ACK=12
- RELATED_AFTER_ACK=200

### 2.5 Evidenza database finale

Query eseguite su MariaDB:
- SELECT id, request_id, stato, document_type, data_chiusura FROM practice ORDER BY id;
- SELECT COUNT(*) FROM practice_state_history WHERE practice_id=1;
- SELECT COUNT(*) FROM audit_event WHERE practice_id=1;
- SELECT COUNT(*) FROM attachment WHERE practice_id=1;

Risultati:
- practice.id=1, request_id=POC-PRAT-0001, stato=CHIUSA_OK, document_type=VERBALE
- state_history_count=4
- audit_count=8
- attachment_count=1

## 3) Runbook manuale ripetibile su DB pulito

## Prerequisiti

- Docker Desktop avviato.
- Porta 80 libera.
- Esecuzione dalla root repository.

## Step A - Reset DB e ambiente

```powershell
docker compose down -v --remove-orphans
docker compose up -d --build
docker compose ps
```

Check atteso:
- anc-db healthy
- anc-backend healthy
- anc-frontend healthy
- anc-reverse-proxy up

## Step B - Verifica autenticazione e baseline

Credenziali demo:
- Operatore: op.rossi / Demo1234!
- Supervisore: sup.verdi / Demo1234!

```powershell
$base='http://localhost'
curl.exe -s -u 'op.rossi:Demo1234!' -o NUL -w "%{http_code}`n" "$base/api/v1/auth/me"
curl.exe -s -u 'sup.verdi:Demo1234!' -o NUL -w "%{http_code}`n" "$base/api/v1/signals"
curl.exe -s -u 'op.rossi:Demo1234!' -o NUL -w "%{http_code}`n" "$base/api/v1/signals"
```

Atteso:
- auth/me = 200
- signals supervisore = 200
- signals operatore = 403

## Step C - Apri una pratica via BPM inbound

```powershell
$base='http://localhost'
$payload = @'
{
  "CANALE": "APP_POSTEPAY",
  "ID_WORKITEM": "POC-WI-0001",
  "NUM_PRATICA": "POC-PRAT-0001",
  "CF_CLIENTE": "RSSMRA80A01H501U",
  "CODICE_CLIENTE": "CLT001",
  "DATA_INSERIMENTO_RICHIESTA": "16/05/2026 10:20:00",
  "CLIENTE": {
    "COGNOME": "ROSSI",
    "NOME": "MARIO",
    "CODICE_FISCALE": "RSSMRA80A01H501U",
    "DATA_NASCITA": "01/01/1980",
    "SESSO": "M",
    "COMUNENASCITA": "ROMA",
    "PROVINCIANASCITA": "RM",
    "NAZIONENASCITA": "ITALIA",
    "CITTADINANZA": "ITALIANA"
  },
  "DATI_CARTA_BLOCCATA": {
    "I_NUMERO_CARTA": "1234567890123456",
    "I_TIPO_CARTA": "POSTEPAY"
  },
  "DOCUMENTI": [
    {
      "CODICE_DOC_ID": 1,
      "CONTENUTI": [
        {
          "NOME_FILE": "sample",
          "ESTENSIONE": "pdf",
          "ID_DOC": "DOC-POC-001",
          "LINKDOWNLOAD": "http://bpm-stub/files/sample.pdf"
        }
      ]
    }
  ]
}
'@

curl.exe -s -H "Content-Type: application/json" -H "X-SD-API-Key: anc-poc-bpm-inbound-key" -d $payload "$base/api/v1/bpm/practices"
```

Atteso:
- resultCode = 0
- details.practiceId valorizzato
- details.state = APERTA

## Step D - Completa intake end-to-end

```powershell
$base='http://localhost'
$op='op.rossi:Demo1234!'
$practiceId=1

# 1) Recupera task e accetta
$tasks = curl.exe -s -u $op "$base/api/v1/tasks" | ConvertFrom-Json
$taskId = $tasks.details[0].taskId
curl.exe -s -u $op -X POST "$base/api/v1/tasks/$taskId/accept"

# 2) Tipizza documento
curl.exe -s -u $op -H "Content-Type: application/json" -d '{"documentType":"VERBALE"}' "$base/api/v1/practices/$practiceId/intake/typing"

# 3) Compila checklist
$chk='{"documentPresent":true,"readabilityOk":true,"formalOk":true,"customerDataOk":true,"cardNumberMatchRequired":false,"internalNotes":"POC checklist ok"}'
curl.exe -s -u $op -X PUT -H "Content-Type: application/json" -d $chk "$base/api/v1/practices/$practiceId/intake/checklist"

# 4) Help contestuale e related actions
curl.exe -s -u $op "$base/api/v1/practices/$practiceId/intake/checklist/help/DOCUMENTPRESENT"
curl.exe -s -u $op "$base/api/v1/practices/$practiceId/related-actions"

# 5) Chiusura intake
curl.exe -s -u $op -X POST "$base/api/v1/practices/$practiceId/intake/close"
```

Atteso:
- accept 200
- typing resultCode 0
- checklist resultCode 0
- help 200
- close 200

## Step E - ACK BPM e stato finale

```powershell
$base='http://localhost'
$op='op.rossi:Demo1234!'
$ack='{"correlationId":"POC-ACK-0001","practiceId":1,"requestId":"POC-PRAT-0001","outcome":"OK","koCodes":[]}'

curl.exe -s -u $op -H "Content-Type: application/json" -d $ack "$base/api/v1/bpm/outcome-ack"
curl.exe -s -u $op "$base/api/v1/practices/1/states"
curl.exe -s -u $op "$base/api/v1/practices/1/history"
```

Atteso:
- ACK resultCode 0
- finalState CHIUSA_OK
- states/history con eventi incrementati

## Step F - Sprint 10 capability check

```powershell
$base='http://localhost'
$op='op.rossi:Demo1234!'

# Export Excel
curl.exe -s -u $op -o NUL -w "%{http_code}`n" "$base/api/v1/practices/export"

# Favorites CRUD
curl.exe -s -u $op "$base/api/v1/favorites"
curl.exe -s -u $op -H "Content-Type: application/json" -d '{"titolo":"POC Link","url":"https://www.poste.it","tipo":"ESTERNO"}' "$base/api/v1/favorites"
```

Atteso:
- export 200
- favorites GET 200
- favorites CREATE resultCode 0

## Step G - Verifica finale DB (opzionale ma consigliata per demo)

```powershell
docker exec anc-db mariadb -uanc -panc anc -e "SELECT id,request_id,stato,document_type,data_chiusura FROM practice ORDER BY id;"
docker exec anc-db mariadb -uanc -panc anc -e "SELECT COUNT(*) AS state_history_count FROM practice_state_history WHERE practice_id=1;"
docker exec anc-db mariadb -uanc -panc anc -e "SELECT COUNT(*) AS audit_count FROM audit_event WHERE practice_id=1;"
docker exec anc-db mariadb -uanc -panc anc -e "SELECT COUNT(*) AS attachment_count FROM attachment WHERE practice_id=1;"
```

## 4) Criterio di successo POC

POC considerata riuscita se:
1. apertura pratica inbound da DB vuoto restituisce resultCode 0.
2. intake arriva a close senza errori di stato/ruolo.
3. outcome-ack porta la pratica in CHIUSA_OK o CHIUSA_KO.
4. history/states mostrano progressione coerente del lifecycle.
5. export/help/related-actions/favorites sono disponibili runtime.
