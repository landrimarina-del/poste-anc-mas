-- ============================================================
-- V103 — GAP-US-11: catalogo causali KO + FK su checklist_verbale e checklist_carta
-- Adattato da GAP-DBA V17+V18: checklist_item_catalog NON presente nel POC
-- (POC usa schema flat: checklist_verbale + checklist_carta)
-- ============================================================

CREATE TABLE IF NOT EXISTS ref_causali_checklist (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    codice      VARCHAR(20)  NOT NULL,
    descrizione VARCHAR(500) NOT NULL,
    categoria   VARCHAR(16)  NOT NULL
        COMMENT 'VERBALE | CARTA — corrisponde al document_type',
    attivo      TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_causali_codice_cat (codice, categoria),
    CONSTRAINT chk_causali_categoria CHECK (categoria IN ('VERBALE','CARTA'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Catalogo causali KO checklist per tipo documento';

-- Seed causali
INSERT IGNORE INTO ref_causali_checklist (codice, descrizione, categoria, attivo) VALUES
    ('VERB_DOC_ILLEGGIBILE',   'Documento illeggibile o parzialmente leggibile', 'VERBALE', 1),
    ('VERB_FIRMA_MANCANTE',    'Firma del dichiarante assente',                  'VERBALE', 1),
    ('VERB_DATI_DISCORDANTI',  'Dati anagrafici discordanti con i sistemi',      'VERBALE', 1),
    ('CARTA_DOC_SCADUTO',      'Documento di identità scaduto',                  'CARTA',   1),
    ('CARTA_DOC_ILLEGGIBILE',  'Documento di identità illeggibile',              'CARTA',   1),
    ('CARTA_FIRMA_MANCANTE',   'Firma del titolare assente',                     'CARTA',   1);

-- Aggiunta codice_causale_id a checklist_verbale (idempotente)
SET @col_v = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'checklist_verbale'
    AND COLUMN_NAME = 'codice_causale_id');
SET @sql_v = IF(@col_v = 0,
    'ALTER TABLE checklist_verbale ADD COLUMN codice_causale_id BIGINT NULL COMMENT ''FK a ref_causali_checklist.id — causale KO formale (opzionale)'' AFTER ko_reasons_json, ADD CONSTRAINT fk_cv_causale FOREIGN KEY (codice_causale_id) REFERENCES ref_causali_checklist(id)',
    'SELECT 1');
PREPARE stmt FROM @sql_v; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Aggiunta codice_causale_id a checklist_carta (idempotente)
SET @col_c = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'checklist_carta'
    AND COLUMN_NAME = 'codice_causale_id');
SET @sql_c = IF(@col_c = 0,
    'ALTER TABLE checklist_carta ADD COLUMN codice_causale_id BIGINT NULL COMMENT ''FK a ref_causali_checklist.id — causale KO formale (opzionale)'' AFTER card_conformity_ok, ADD CONSTRAINT fk_cc_causale FOREIGN KEY (codice_causale_id) REFERENCES ref_causali_checklist(id)',
    'SELECT 1');
PREPARE stmt FROM @sql_c; EXECUTE stmt; DEALLOCATE PREPARE stmt;
