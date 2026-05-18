-- Sprint 4: consolidamento vincoli tipizzazione documento.
-- Nota: l'irreversibilita e' enforce principalmente lato applicativo (no trigger in POC).

ALTER TABLE practice
    ADD COLUMN IF NOT EXISTS document_type VARCHAR(16) NULL;

ALTER TABLE practice
    MODIFY document_type VARCHAR(16) NULL;

SET @chk_exists := (
    SELECT COUNT(1)
    FROM information_schema.table_constraints
    WHERE constraint_schema = DATABASE()
      AND table_name = 'practice'
      AND constraint_name = 'chk_practice_document_type_s4'
);

SET @ddl := IF(
    @chk_exists = 0,
    'ALTER TABLE practice ADD CONSTRAINT chk_practice_document_type_s4 CHECK (document_type IS NULL OR document_type IN (''VERBALE'', ''CARTA''))',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
