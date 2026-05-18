-- =====================================================================
-- V5 - Allineamento schema practice con payload inbound Sprint 1
-- Aggiunge le colonne usate dal servizio di creazione pratica inbound.
-- =====================================================================

ALTER TABLE practice
    ADD COLUMN IF NOT EXISTS canale VARCHAR(64) NULL,
    ADD COLUMN IF NOT EXISTS num_pratica VARCHAR(64) NULL,
    ADD COLUMN IF NOT EXISTS cf_cliente VARCHAR(16) NULL,
    ADD COLUMN IF NOT EXISTS data_inserimento_richiesta DATETIME(3) NULL;

-- Backfill minimo per righe gia' presenti.
UPDATE practice
SET
    canale = COALESCE(canale, 'BPM'),
    num_pratica = COALESCE(num_pratica, request_id),
    data_inserimento_richiesta = COALESCE(data_inserimento_richiesta, data_apertura)
WHERE
    canale IS NULL
    OR num_pratica IS NULL
    OR data_inserimento_richiesta IS NULL;