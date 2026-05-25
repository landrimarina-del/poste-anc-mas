-- ============================================================
-- V107 — TECNICO-GAP-B: campo SLA target su task
-- ============================================================

SET @col_sla = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'task'
    AND COLUMN_NAME = 'sla_due_date');
SET @sql_sla = IF(@col_sla = 0,
    'ALTER TABLE task ADD COLUMN sla_due_date DATETIME(3) NULL COMMENT ''Data/ora target SLA (5 giorni lavorativi da created_at)'' AFTER completed_at',
    'SELECT 1');
PREPARE stmt FROM @sql_sla; EXECUTE stmt; DEALLOCATE PREPARE stmt;
