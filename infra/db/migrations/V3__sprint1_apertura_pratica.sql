-- =====================================================================
-- V3 - Sprint 1: Apertura Pratica E2E (BPM stub -> SD)
-- BC coinvolti: BC1 (practice), BC4 (bpm inbound), M-Audit (base)
-- =====================================================================

CREATE TABLE audit_event (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    occurred_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    actor_username VARCHAR(64)  NULL,
    event_type     VARCHAR(64)  NOT NULL,
    practice_id    BIGINT       NULL,
    correlation_id VARCHAR(64)  NULL,
    payload_json   JSON         NULL,
    PRIMARY KEY (id),
    KEY idx_audit_practice (practice_id),
    KEY idx_audit_type (event_type, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE practice (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    appian_ticket_id   VARCHAR(64)  NOT NULL,
    id_work_item       VARCHAR(64)  NOT NULL,
    stato              VARCHAR(32)  NOT NULL,
    data_apertura      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    data_chiusura      DATETIME(3)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_practice_appian_ticket (appian_ticket_id),
    UNIQUE KEY uk_practice_id_work_item (id_work_item),
    KEY idx_practice_stato_data (stato, data_apertura)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE client_data (
    practice_id    BIGINT       NOT NULL,
    cognome        VARCHAR(128) NULL,
    nome           VARCHAR(128) NULL,
    codice_fiscale VARCHAR(16)  NULL,
    documento_tipo VARCHAR(32)  NULL,
    documento_num  VARCHAR(32)  NULL,
    data_nascita   DATE         NULL,
    PRIMARY KEY (practice_id),
    CONSTRAINT fk_client_practice FOREIGN KEY (practice_id) REFERENCES practice(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE card_data (
    practice_id   BIGINT       NOT NULL,
    card_type     VARCHAR(32)  NULL,
    iban          VARCHAR(34)  NULL,
    pan_masked    VARCHAR(32)  NULL,
    note          VARCHAR(255) NULL,
    PRIMARY KEY (practice_id),
    CONSTRAINT fk_card_practice FOREIGN KEY (practice_id) REFERENCES practice(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE attachment (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    practice_id     BIGINT       NOT NULL,
    codice_doc_id   VARCHAR(8)   NOT NULL,
    file_name       VARCHAR(255) NOT NULL,
    mime_type       VARCHAR(64)  NOT NULL,
    size_bytes      BIGINT       NOT NULL,
    storage_uri     VARCHAR(512) NOT NULL,
    checksum_sha256 CHAR(64)     NULL,
    created_at      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_att_practice FOREIGN KEY (practice_id) REFERENCES practice(id),
    KEY idx_att_practice (practice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE practice_state_history (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    practice_id    BIGINT       NOT NULL,
    from_state     VARCHAR(32)  NULL,
    to_state       VARCHAR(32)  NOT NULL,
    occurred_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    actor_username VARCHAR(64)  NULL,
    correlation_id VARCHAR(64)  NULL,
    note           VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_psh_practice FOREIGN KEY (practice_id) REFERENCES practice(id),
    KEY idx_psh_practice_time (practice_id, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bpm_inbound_message (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    id_work_item   VARCHAR(64)  NOT NULL,
    received_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    payload_json   JSON         NOT NULL,
    result_code    INT          NOT NULL,
    result_message VARCHAR(255) NULL,
    practice_id    BIGINT       NULL,
    PRIMARY KEY (id),
    KEY idx_bpm_in_workitem (id_work_item, received_at),
    CONSTRAINT fk_bpm_in_practice FOREIGN KEY (practice_id) REFERENCES practice(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;