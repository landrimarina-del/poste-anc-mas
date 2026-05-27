-- ---------------------------------------------------------------
-- Init Kogito JDBC persistence su MariaDB.
-- Questo script viene eseguito al primo bootstrap del volume DB.
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

-- MariaDB/InnoDB non consente un indice pieno su VARCHAR(4000) utf8mb4.
-- Si usano prefissi per restare entro il limite chiave (3072 byte).
CREATE INDEX idx_process_instances_process_id
    ON process_instances (process_id(191), id, process_version(191));

FLUSH PRIVILEGES;
