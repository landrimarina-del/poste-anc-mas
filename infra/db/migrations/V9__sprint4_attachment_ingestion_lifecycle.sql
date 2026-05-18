-- =====================================================================
-- V9 - Sprint 4 PATCH GAP-BLOCKER-001 (Document Transport)
-- Pattern: metadata + pull-through SINCRONO su MinIO in svc.openPractice.
-- Aggiunge il lifecycle di ingestione allegati e rende NULLABLE i campi
-- binario (storage_uri/mime_type/size_bytes) per modellare lo stato
-- "metadata-only / binario non ancora scaricato" (PENDING).
--
-- Idempotente: re-run = no-op.
-- NON modifica V1..V8.
--
-- NOTA SUL BACKFILL:
-- I dati Sprint 1..3 sono stati inseriti con size_bytes = 0 (placeholder
-- prima dell'introduzione del pull-through). Il backfill in coda promuove
-- a AVAILABLE solo righe con tutti i campi binario valorizzati e
-- size_bytes > 0; le righe pre-esistenti restano quindi in PENDING.
-- =====================================================================

-- 1) ALTER colonne binario a NULLABLE (allinea schema al lifecycle).
ALTER TABLE attachment MODIFY storage_uri VARCHAR(512) NULL;
ALTER TABLE attachment MODIFY mime_type   VARCHAR(64)  NULL;
ALTER TABLE attachment MODIFY size_bytes  BIGINT       NULL;

-- 2) Nuove colonne lifecycle (idempotente via IF NOT EXISTS).
ALTER TABLE attachment
    ADD COLUMN IF NOT EXISTS ingestion_status VARCHAR(16) NOT NULL DEFAULT 'PENDING' AFTER checksum_sha256;

ALTER TABLE attachment
    ADD COLUMN IF NOT EXISTS ingested_at DATETIME(3) NULL AFTER ingestion_status;

ALTER TABLE attachment
    ADD COLUMN IF NOT EXISTS ingestion_error VARCHAR(500) NULL AFTER ingested_at;

-- 3) CHECK constraint condizionale (pattern V8: PREPARE/EXECUTE su
--    information_schema.table_constraints per garantire idempotenza).
SELECT COUNT(*) INTO @cnt_chk
FROM information_schema.table_constraints
WHERE table_schema = DATABASE()
  AND table_name = 'attachment'
  AND constraint_name = 'chk_att_ingestion_status_s4';

SET @sql_chk = IF(@cnt_chk = 0,
    'ALTER TABLE attachment ADD CONSTRAINT chk_att_ingestion_status_s4 CHECK (ingestion_status IN (''PENDING'',''AVAILABLE'',''FAILED''))',
    'DO 0');

PREPARE stmt_chk FROM @sql_chk;
EXECUTE stmt_chk;
DEALLOCATE PREPARE stmt_chk;

-- 4) Indice idempotente su ingestion_status (guard su information_schema.statistics).
SELECT COUNT(*) INTO @cnt_idx
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name = 'attachment'
  AND index_name = 'idx_att_ingest_status';

SET @sql_idx = IF(@cnt_idx = 0,
    'CREATE INDEX idx_att_ingest_status ON attachment (ingestion_status)',
    'DO 0');

PREPARE stmt_idx FROM @sql_idx;
EXECUTE stmt_idx;
DEALLOCATE PREPARE stmt_idx;

-- 5) Backfill: promuovi a AVAILABLE solo righe con tutti i campi binario
--    realmente valorizzati. Le righe Sprint 1..3 con size_bytes=0 restano
--    in PENDING (volutamente, vedi nota in testa al file).
UPDATE attachment
   SET ingestion_status = 'AVAILABLE',
       ingested_at = COALESCE(ingested_at, created_at)
 WHERE ingestion_status = 'PENDING'
   AND storage_uri IS NOT NULL
   AND mime_type IS NOT NULL
   AND size_bytes IS NOT NULL
   AND size_bytes > 0;
