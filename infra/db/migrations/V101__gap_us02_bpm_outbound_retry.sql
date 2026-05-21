-- V15 — GAP-US-02 — Aggiunta colonne retry al pattern outbound BPM sincrono
-- Pattern idempotente: ADD COLUMN IF NOT EXISTS (MariaDB 10.2+)

ALTER TABLE bpm_outbound_message
    ADD COLUMN IF NOT EXISTS retry_count      INT           NOT NULL DEFAULT 0
        COMMENT '0=mai tentato; N=numero tentativi effettuati',
    ADD COLUMN IF NOT EXISTS max_retry        INT           NOT NULL DEFAULT 3,
    ADD COLUMN IF NOT EXISTS stato_invio      TINYINT       NOT NULL DEFAULT 0
        COMMENT '0=attesa 1=inviato_ok 2=errore_transiente 3=scartato',
    ADD COLUMN IF NOT EXISTS response_json    TEXT          NULL,
    ADD COLUMN IF NOT EXISTS error_message    VARCHAR(1000) NULL,
    ADD COLUMN IF NOT EXISTS last_attempt_at  DATETIME(3)   NULL;

SELECT COUNT(*) INTO @cnt FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name   = 'bpm_outbound_message'
  AND index_name   = 'idx_bom_stato_invio';
SET @s = IF(@cnt = 0,
    'CREATE INDEX idx_bom_stato_invio ON bpm_outbound_message(stato_invio)',
    'DO 0');
PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
