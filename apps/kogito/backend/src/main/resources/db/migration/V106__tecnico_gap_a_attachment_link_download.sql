-- ============================================================
-- V106 — TECNICO-GAP-A: espansione link_download da 1024 a 2500 chars
-- Nota: ingestion_status già aggiunto da V9 (Sprint 4) — non riaggiungere
-- ============================================================

ALTER TABLE attachment
    MODIFY COLUMN link_download VARCHAR(2500) NOT NULL
        COMMENT 'URL sorgente originale del documento (max 2500 chars)';
