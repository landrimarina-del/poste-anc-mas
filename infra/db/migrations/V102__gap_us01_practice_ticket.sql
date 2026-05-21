-- V16 — GAP-US-01 — Aggiunta ticket_id a practice per integrazione ticketing mock

ALTER TABLE practice
    ADD COLUMN IF NOT EXISTS ticket_id VARCHAR(100) NULL
        COMMENT 'ID ticket sistema di ticketing esterno (mock in POC)';

SELECT COUNT(*) INTO @cnt FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name   = 'practice'
  AND index_name   = 'idx_practice_ticket';
SET @s = IF(@cnt = 0,
    'CREATE INDEX idx_practice_ticket ON practice(ticket_id)',
    'DO 0');
PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
