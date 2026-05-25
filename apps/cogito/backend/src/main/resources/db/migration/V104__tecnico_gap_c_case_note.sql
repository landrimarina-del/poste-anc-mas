-- ============================================================
-- V104 — TECNICO-GAP-C: note intermediate associate alla pratica
-- ============================================================
CREATE TABLE IF NOT EXISTS case_note (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    practice_id BIGINT       NOT NULL,
    autore      VARCHAR(64)  NOT NULL COMMENT 'username operatore o SYSTEM',
    testo       TEXT         NOT NULL,
    tipo        VARCHAR(20)  NOT NULL COMMENT 'LAVORAZIONE | CAMBIO_STATO | CHIUSURA',
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_cn_practice FOREIGN KEY (practice_id) REFERENCES practice(id),
    CONSTRAINT chk_cn_tipo    CHECK (tipo IN ('LAVORAZIONE','CAMBIO_STATO','CHIUSURA')),
    KEY idx_cn_practice_time (practice_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Note intermediate pratica — equivalente BOA_ANC_ENTITY_CASENOTE';
