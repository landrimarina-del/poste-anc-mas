-- ============================================================
-- V105 — GAP-US-05: filtri salvati Lista Attività per utente
-- ============================================================

CREATE TABLE IF NOT EXISTS user_task_filter (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    username    VARCHAR(64)  NOT NULL
        COMMENT 'username operatore proprietario del filtro',
    filter_name VARCHAR(100) NULL
        COMMENT 'Nome opzionale del filtro salvato',
    filter_json TEXT         NOT NULL
        COMMENT 'Payload JSON dei parametri di filtro',
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_utf_username_time (username, created_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Filtri salvati Lista Attivita per utente — GAP-US-05';
