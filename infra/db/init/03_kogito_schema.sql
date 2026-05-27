-- ---------------------------------------------------------------
-- Init Kogito JDBC persistence su MariaDB.
-- Questo script viene eseguito al primo bootstrap del volume DB.
-- DDL allineato a kie-addons-persistence-jdbc:10.2.0
-- ---------------------------------------------------------------

CREATE DATABASE IF NOT EXISTS kogito
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON kogito.* TO 'anc'@'%';

USE kogito;

CREATE TABLE IF NOT EXISTS process_instances
(
    id              CHAR(36)      NOT NULL,
    payload         BLOB          NOT NULL,
    process_id      VARCHAR(4000) NOT NULL,
    version         BIGINT(19),
    process_version VARCHAR(4000),
    CONSTRAINT process_instances_pkey PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_process_instances_process_id
    ON process_instances (process_id(191), id, process_version(191));

-- V10.0.0 — business key mapping
CREATE TABLE IF NOT EXISTS business_key_mapping (
    business_key        VARCHAR(255) NOT NULL,
    process_instance_id VARCHAR(36)  NOT NULL,
    CONSTRAINT business_key_primary_key PRIMARY KEY (business_key),
    CONSTRAINT fk_process_instances
        FOREIGN KEY (process_instance_id)
        REFERENCES process_instances(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_business_key_process_instance_id
    ON business_key_mapping (process_instance_id);

-- V10.0.1 — correlation instances
CREATE TABLE IF NOT EXISTS correlation_instances
(
    id                     CHAR(36)         NOT NULL,
    encoded_correlation_id VARCHAR(36)      NOT NULL UNIQUE,
    correlated_id          VARCHAR(36)      NOT NULL,
    correlation            VARCHAR(8000)    NOT NULL,
    version                BIGINT,
    CONSTRAINT correlation_instances_pkey PRIMARY KEY (id)
);

-- V10.0.2 — event types (richiesta da Kogito 10.2 al momento di avvio processo)
CREATE TABLE IF NOT EXISTS event_types
(
    process_instance_id CHAR(36)     NOT NULL,
    event_type          VARCHAR(256) NOT NULL,
    CONSTRAINT event_types_pk PRIMARY KEY (process_instance_id, event_type)
);

FLUSH PRIVILEGES;
