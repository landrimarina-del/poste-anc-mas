# 03 - Schema DDL Iniziale

> DDL **MariaDB 10.11** (decisione Coordinator 2026-05-13 — D1 risolta). Sintassi compatibile con il sotto-set SQL portabile (D4: no `ENUM`, no SEQUENCE, no trigger DB). Engine: `InnoDB`, charset `utf8mb4`, collation `utf8mb4_unicode_ci`. Naming `snake_case` come da convenzione cap. 08 Architect. Schema campi cliente/carta/documenti derivato da `docs/requirements/source-of-truth/InterfaceAgreement.md`.

> Lo schema è organizzato per bounded context. Le tabelle Flowable (`ACT_*`) sono autogestite dall'engine e non incluse qui.

---

## 1. Cross-cutting (M-IAM, M-Audit)

```sql
-- Utenti applicativi (POC: login locale; Target: SSO via Keycloak/AD)
CREATE TABLE app_user (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    username        VARCHAR(64)  NOT NULL,
    password_hash   VARCHAR(255) NULL,                 -- BCrypt; NULL se SSO esclusivo
    full_name       VARCHAR(128) NOT NULL,
    email           VARCHAR(128) NULL,
    active          TINYINT(1)   NOT NULL DEFAULT 1,
    created_at      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_user_username (username)
) ENGINE=InnoDB;

CREATE TABLE role (
    id      BIGINT      NOT NULL AUTO_INCREMENT,
    code    VARCHAR(32) NOT NULL,                       -- OPERATORE_ANC, SUPERVISORE_ANC, ADMIN
    name    VARCHAR(64) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (code)
) ENGINE=InnoDB;

CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES app_user(id),
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES role(id)
) ENGINE=InnoDB;

CREATE TABLE user_group (
    id    BIGINT      NOT NULL AUTO_INCREMENT,
    code  VARCHAR(64) NOT NULL,                         -- GRUPPO_OPERATORE_ANC
    name  VARCHAR(128) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_group_code (code)
) ENGINE=InnoDB;

CREATE TABLE user_group_member (
    user_id  BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, group_id),
    CONSTRAINT fk_ugm_user  FOREIGN KEY (user_id)  REFERENCES app_user(id),
    CONSTRAINT fk_ugm_group FOREIGN KEY (group_id) REFERENCES user_group(id)
) ENGINE=InnoDB;

-- Audit trail trasversale
CREATE TABLE audit_event (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    occurred_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    actor_username VARCHAR(64)  NULL,
    event_type     VARCHAR(64)  NOT NULL,                -- es. PRACTICE_OPENED, TASK_ACCEPTED
    practice_id    BIGINT       NULL,
    correlation_id VARCHAR(64)  NULL,
    payload_json   JSON         NULL,
    PRIMARY KEY (id),
    KEY idx_audit_practice (practice_id),
    KEY idx_audit_type     (event_type, occurred_at)
) ENGINE=InnoDB;
```

---

## 2. BC1 — Practice Management

```sql
CREATE TABLE practice (
    id                          BIGINT       NOT NULL AUTO_INCREMENT,
    request_id                  VARCHAR(64)  NOT NULL,           -- chiave funzionale cross-system (generata dalla SD)
    id_work_item                VARCHAR(64)  NOT NULL,           -- ID_WORKITEM da BPM (Interface Agreement) — idempotenza
    canale                      VARCHAR(32)  NOT NULL,           -- CANALE (IA testata)
    num_pratica                 VARCHAR(64)  NOT NULL,           -- NUM_PRATICA (IA testata, da BPM)
    cf_cliente                  VARCHAR(16)  NOT NULL,           -- CF_CLIENTE (IA testata)
    codice_cliente              VARCHAR(64)  NULL,               -- CODICE_CLIENTE (Id AUC, facoltativo IA)
    data_inserimento_richiesta  DATETIME(3)  NOT NULL,           -- DATA_INSERIMENTO_RICHIESTA (IA testata)
    document_type               VARCHAR(16)  NULL,               -- VERBALE | CARTA (irreversibile)
    stato                       VARCHAR(32)  NOT NULL,           -- APERTA | IN_LAVORAZIONE | IN_ATTESA_CONFERMA_BPM | CHIUSA_OK | CHIUSA_KO
    data_apertura               DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    data_chiusura               DATETIME(3)  NULL,
    bpm_inbound_msg_id          BIGINT       NULL,               -- riferimento al messaggio inbound originario
    version                     INT          NOT NULL DEFAULT 0, -- optimistic locking
    PRIMARY KEY (id),
    UNIQUE KEY uk_practice_request      (request_id),
    UNIQUE KEY uk_practice_id_work_item (id_work_item),
    KEY idx_practice_stato_data         (stato, data_apertura),
    KEY idx_practice_cf_cliente         (cf_cliente),
    KEY idx_practice_num_pratica        (num_pratica),
    CONSTRAINT chk_practice_stato CHECK (stato IN
        ('APERTA','IN_LAVORAZIONE','IN_ATTESA_CONFERMA_BPM','CHIUSA_OK','CHIUSA_KO')),
    CONSTRAINT chk_practice_doctype CHECK (document_type IS NULL OR document_type IN ('VERBALE','CARTA'))
) ENGINE=InnoDB;

CREATE TABLE client_data (
    practice_id        BIGINT       NOT NULL,
    nome               VARCHAR(128) NOT NULL,                    -- CLIENTE.NOME (IA, obbl.)
    cognome            VARCHAR(128) NOT NULL,                    -- CLIENTE.COGNOME (IA, obbl.)
    sesso              CHAR(1)      NULL,                        -- CLIENTE.SESSO (IA, facolt.) M/F/X
    data_nascita       DATE         NOT NULL,                    -- CLIENTE.DATANASCITA (IA, obbl.)
    comune_nascita     VARCHAR(128) NOT NULL,                    -- CLIENTE.COMUNENASCITA (IA, obbl.)
    provincia_nascita  VARCHAR(8)   NOT NULL,                    -- CLIENTE.PROVINCIANASCITA (IA, obbl.)
    nazione_nascita    VARCHAR(64)  NOT NULL,                    -- CLIENTE.NAZIONENASCITA (IA, obbl.)
    cittadinanza       VARCHAR(64)  NOT NULL,                    -- CLIENTE.CITTADINANZA (IA, obbl.)
    cellulare          VARCHAR(32)  NULL,                        -- CLIENTE.CELLULARE (IA, facolt.)
    telefono           VARCHAR(32)  NULL,                        -- CLIENTE.TELEFONO (IA, facolt.)
    PRIMARY KEY (practice_id),
    CONSTRAINT fk_client_practice FOREIGN KEY (practice_id) REFERENCES practice(id)
) ENGINE=InnoDB;

CREATE TABLE client_address (
    practice_id   BIGINT       NOT NULL,                          -- CLIENTE.INDIRIZZO_DI_RESIDENZA (IA, facolt.)
    luogo         VARCHAR(255) NULL,                              -- DUG, toponimo, civico (campo libero IA)
    comune        VARCHAR(128) NULL,
    provincia     VARCHAR(8)   NULL,
    nazione       VARCHAR(64)  NULL,
    cap           VARCHAR(16)  NULL,
    civico        VARCHAR(16)  NULL,
    PRIMARY KEY (practice_id),
    CONSTRAINT fk_client_address_practice FOREIGN KEY (practice_id) REFERENCES practice(id)
) ENGINE=InnoDB;

CREATE TABLE card_data (
    practice_id     BIGINT       NOT NULL,
    pan             VARCHAR(32)  NOT NULL,                        -- DATI_CARTA_BLOCCATA.I_NUMERO_CARTA (IA, obbl., in chiaro)
    tipo_carta      VARCHAR(64)  NOT NULL,                        -- DATI_CARTA_BLOCCATA.I_TIPO_CARTA (IA, obbl.)
    intestazione    VARCHAR(255) NULL,                            -- DATI_CARTA_BLOCCATA.I_INTEST_CARTA (IA, facolt.)
    PRIMARY KEY (practice_id),
    CONSTRAINT fk_card_practice FOREIGN KEY (practice_id) REFERENCES practice(id)
) ENGINE=InnoDB;

CREATE TABLE practice_state_history (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    practice_id    BIGINT       NOT NULL,
    from_state     VARCHAR(32)  NULL,
    to_state       VARCHAR(32)  NOT NULL,
    occurred_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    actor_username VARCHAR(64)  NULL,                     -- NULL se transizione di sistema
    correlation_id VARCHAR(64)  NULL,
    note           VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_psh_practice FOREIGN KEY (practice_id) REFERENCES practice(id),
    KEY idx_psh_practice_time (practice_id, occurred_at)
) ENGINE=InnoDB;

CREATE TABLE related_action (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    practice_id    BIGINT       NOT NULL,
    action_code    VARCHAR(64)  NOT NULL,
    action_label   VARCHAR(255) NOT NULL,
    target_url     VARCHAR(512) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ra_practice FOREIGN KEY (practice_id) REFERENCES practice(id)
) ENGINE=InnoDB;
```

---

## 3. BC2 — Workflow / Task Orchestration

```sql
CREATE TABLE task (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    practice_id         BIGINT       NOT NULL,
    flowable_task_id    VARCHAR(64)  NULL,                -- riferimento a ACT_RU_TASK.ID_
    tipo_pratica        VARCHAR(16)  NOT NULL DEFAULT 'ANC',
    stato               VARCHAR(16)  NOT NULL,            -- IN_CODA | IN_CARICO | COMPLETATO
    candidate_group_id  BIGINT       NULL,                -- FK user_group
    owner_user_id       BIGINT       NULL,                -- FK app_user
    created_at          DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    accepted_at         DATETIME(3)  NULL,
    completed_at        DATETIME(3)  NULL,
    version             INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_task_practice FOREIGN KEY (practice_id)        REFERENCES practice(id),
    CONSTRAINT fk_task_group    FOREIGN KEY (candidate_group_id) REFERENCES user_group(id),
    CONSTRAINT fk_task_owner    FOREIGN KEY (owner_user_id)      REFERENCES app_user(id),
    CONSTRAINT chk_task_stato   CHECK (stato IN ('IN_CODA','IN_CARICO','COMPLETATO')),
    KEY idx_task_owner_stato    (owner_user_id, stato),
    KEY idx_task_group_stato    (candidate_group_id, stato),
    KEY idx_task_flowable       (flowable_task_id)
) ENGINE=InnoDB;

CREATE TABLE task_assignment_history (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    task_id             BIGINT       NOT NULL,
    assigned_at         DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    assigned_by         VARCHAR(64)  NULL,                -- username supervisore o 'SYSTEM'
    assignment_type     VARCHAR(16)  NOT NULL,            -- INITIAL | REASSIGN_USER | REASSIGN_GROUP
    target_user_id      BIGINT       NULL,
    target_group_id     BIGINT       NULL,
    reason              VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_tah_task         FOREIGN KEY (task_id)         REFERENCES task(id),
    CONSTRAINT fk_tah_target_user  FOREIGN KEY (target_user_id)  REFERENCES app_user(id),
    CONSTRAINT fk_tah_target_group FOREIGN KEY (target_group_id) REFERENCES user_group(id),
    CONSTRAINT chk_tah_type        CHECK (assignment_type IN ('INITIAL','REASSIGN_USER','REASSIGN_GROUP')),
    KEY idx_tah_task_time          (task_id, assigned_at)
) ENGINE=InnoDB;
```

---

## 4. BC3 — Document & Checklist

```sql
CREATE TABLE attachment (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    practice_id     BIGINT       NOT NULL,
    codice_doc_id   VARCHAR(2)   NOT NULL,                 -- DOCUMENTI.CODICE_DOC_ID (IA) — dominio {1,2,3}
    id_doc          VARCHAR(64)  NOT NULL,                 -- DOCUMENTI.CONTENUTI[].ID_DOC (univoco IA)
    file_name       VARCHAR(255) NOT NULL,                 -- DOCUMENTI.CONTENUTI[].NOME_FILE
    estensione      VARCHAR(8)   NOT NULL,                 -- DOCUMENTI.CONTENUTI[].ESTENSIONE — dominio {pdf, jpeg, jpg, png}
    link_download   VARCHAR(1024) NOT NULL,                -- DOCUMENTI.CONTENUTI[].LINKDOWNLOAD (URL sorgente IA)
    mime_type       VARCHAR(64)  NULL,                     -- valorizzato dopo download
    size_bytes      BIGINT       NULL,                     -- valorizzato dopo download
    storage_uri     VARCHAR(512) NULL,                     -- minio://bucket/path (popolato dopo persistenza binario)
    checksum_sha256 CHAR(64)     NULL,
    created_at      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_att_practice FOREIGN KEY (practice_id) REFERENCES practice(id),
    CONSTRAINT chk_att_codice_doc CHECK (codice_doc_id IN ('1','2','3')),
    CONSTRAINT chk_att_estensione CHECK (estensione IN ('pdf','jpeg','jpg','png')),
    KEY idx_att_practice (practice_id),
    UNIQUE KEY uk_att_practice_iddoc (practice_id, id_doc)
) ENGINE=InnoDB;

CREATE TABLE checklist_item_catalog (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    code            VARCHAR(64)  NOT NULL,                -- es. VERB_INTESTAZIONE_CORRETTA
    document_type   VARCHAR(16)  NOT NULL,                -- VERBALE | CARTA
    label           VARCHAR(255) NOT NULL,
    help_text       TEXT         NULL,
    ordering        INT          NOT NULL DEFAULT 0,
    triggers_cascade TINYINT(1)  NOT NULL DEFAULT 0,      -- "documento presente" → cascata KO
    active          TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_chk_item_code (code),
    CONSTRAINT chk_chk_item_doctype CHECK (document_type IN ('VERBALE','CARTA'))
) ENGINE=InnoDB;

CREATE TABLE checklist_response (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    practice_id    BIGINT       NOT NULL,
    item_id        BIGINT       NOT NULL,
    answer         VARCHAR(8)   NULL,                     -- SI | NO | NA
    ko_reason_code VARCHAR(64)  NULL,                     -- causale KO (codice baseline)
    note           VARCHAR(500) NULL,
    stato          VARCHAR(16)  NOT NULL DEFAULT 'BOZZA', -- BOZZA | RIAPERTA | CONSOLIDATA
    revision       INT          NOT NULL DEFAULT 1,
    updated_at     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    updated_by     VARCHAR(64)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_chk_resp_practice_item (practice_id, item_id),
    CONSTRAINT fk_cr_practice FOREIGN KEY (practice_id) REFERENCES practice(id),
    CONSTRAINT fk_cr_item     FOREIGN KEY (item_id)     REFERENCES checklist_item_catalog(id),
    CONSTRAINT chk_cr_answer  CHECK (answer IS NULL OR answer IN ('SI','NO','NA')),
    CONSTRAINT chk_cr_stato   CHECK (stato IN ('BOZZA','RIAPERTA','CONSOLIDATA'))
) ENGINE=InnoDB;

CREATE TABLE practice_outcome (
    practice_id     BIGINT       NOT NULL,
    outcome         VARCHAR(16)  NOT NULL,                -- APPROVATA | RESPINTA
    ko_codes_csv    VARCHAR(512) NULL,                    -- codici causale concatenati per invio BPM
    notes           TEXT         NULL,
    computed_at     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    computed_by     VARCHAR(64)  NULL,
    PRIMARY KEY (practice_id),
    CONSTRAINT fk_po_practice FOREIGN KEY (practice_id) REFERENCES practice(id),
    CONSTRAINT chk_po_outcome CHECK (outcome IN ('APPROVATA','RESPINTA'))
) ENGINE=InnoDB;
```

---

## 5. BC4 — BPM Integration Gateway

```sql
CREATE TABLE bpm_inbound_message (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    id_work_item   VARCHAR(64)  NOT NULL,
    received_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    payload_json   JSON         NOT NULL,
    result_code    INT          NOT NULL,                 -- 0 | -4 | -5
    result_message VARCHAR(255) NULL,
    practice_id    BIGINT       NULL,                     -- popolato se result_code=0
    PRIMARY KEY (id),
    KEY idx_bpm_in_workitem (id_work_item, received_at),
    CONSTRAINT fk_bpm_in_practice FOREIGN KEY (practice_id) REFERENCES practice(id)
) ENGINE=InnoDB;

CREATE TABLE bpm_outbound_message (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    practice_id     BIGINT       NOT NULL,
    payload_json    JSON         NOT NULL,
    outcome         VARCHAR(16)  NOT NULL,                -- APPROVATA | RESPINTA
    send_status     VARCHAR(16)  NOT NULL DEFAULT 'PENDING', -- PENDING | SENT | FAILED
    sent_at         DATETIME(3)  NULL,
    ack_status      VARCHAR(16)  NULL,                    -- ACK_OK | ACK_KO
    ack_at          DATETIME(3)  NULL,
    correlation_id  VARCHAR(64)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_bpm_out_correlation (correlation_id),
    CONSTRAINT fk_bpm_out_practice FOREIGN KEY (practice_id) REFERENCES practice(id),
    CONSTRAINT chk_bpm_out_send    CHECK (send_status IN ('PENDING','SENT','FAILED')),
    CONSTRAINT chk_bpm_out_ack     CHECK (ack_status IS NULL OR ack_status IN ('ACK_OK','ACK_KO')),
    KEY idx_bpm_out_practice (practice_id)
) ENGINE=InnoDB;

CREATE TABLE event_outbox (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    aggregate_type VARCHAR(64)  NOT NULL,                 -- practice | task | signal | ...
    aggregate_id   VARCHAR(64)  NOT NULL,
    event_type     VARCHAR(64)  NOT NULL,                 -- PracticeOpened, TaskAccepted, ...
    payload_json   JSON         NOT NULL,
    occurred_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    status         VARCHAR(16)  NOT NULL DEFAULT 'PENDING', -- PENDING | DISPATCHED | FAILED
    dispatched_at  DATETIME(3)  NULL,
    attempt_count  INT          NOT NULL DEFAULT 0,
    last_error     VARCHAR(500) NULL,
    PRIMARY KEY (id),
    KEY idx_outbox_status_time (status, occurred_at),
    CONSTRAINT chk_outbox_status CHECK (status IN ('PENDING','DISPATCHED','FAILED'))
) ENGINE=InnoDB;
```

---

## 6. BC6 — Signal Management

```sql
CREATE TABLE signal (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    practice_id       BIGINT       NOT NULL,
    created_by        VARCHAR(64)  NOT NULL,
    created_at        DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    subject           VARCHAR(255) NOT NULL,
    body              TEXT         NULL,
    stato             VARCHAR(16)  NOT NULL DEFAULT 'IN_CODA', -- IN_CODA | IN_LAVORAZIONE | CHIUSO
    owner_user_id     BIGINT       NULL,
    closed_at         DATETIME(3)  NULL,
    sinergia_ack_at   DATETIME(3)  NULL,                  -- da stub
    PRIMARY KEY (id),
    CONSTRAINT fk_sig_practice FOREIGN KEY (practice_id)   REFERENCES practice(id),
    CONSTRAINT fk_sig_owner    FOREIGN KEY (owner_user_id) REFERENCES app_user(id),
    CONSTRAINT chk_sig_stato   CHECK (stato IN ('IN_CODA','IN_LAVORAZIONE','CHIUSO')),
    KEY idx_sig_practice_time (practice_id, created_at),
    KEY idx_sig_owner_stato   (owner_user_id, stato)
) ENGINE=InnoDB;

CREATE TABLE signal_state_history (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    signal_id      BIGINT       NOT NULL,
    from_state     VARCHAR(16)  NULL,
    to_state       VARCHAR(16)  NOT NULL,
    occurred_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    actor_username VARCHAR(64)  NULL,
    note           VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ssh_signal FOREIGN KEY (signal_id) REFERENCES signal(id),
    KEY idx_ssh_signal_time (signal_id, occurred_at)
) ENGINE=InnoDB;
```

---

## 7. Indici aggiuntivi a supporto delle query principali

| Query/funzionalità | Indice |
|---|---|
| Repository pratiche con filtri stato + data | `practice(stato, data_apertura)` |
| Lista Attività utente | `task(owner_user_id, stato)` |
| Lista Attività gruppo | `task(candidate_group_id, stato)` |
| Cronologia stati pratica | `practice_state_history(practice_id, occurred_at)` |
| Idempotenza inbound BPM | UNIQUE `practice(id_work_item)` |
| Audit per pratica | `audit_event(practice_id)` |
| Outbox dispatch | `event_outbox(status, occurred_at)` |
| Riconciliazione esiti BPM | UNIQUE `bpm_outbound_message(correlation_id)` |

> Indici aggiuntivi (analytics, reporting BI) sono out-of-scope POC.
