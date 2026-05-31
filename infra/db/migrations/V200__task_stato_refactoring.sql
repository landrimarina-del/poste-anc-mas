-- V200: refactoring stati task
-- Aggiorna il CHECK constraint per supportare i nuovi stati di chiusura:
--   CHIUSA_SD_OK  / CHIUSA_SD_KO  = chiusura confermata lato Scrivania Digitale
--   CHIUSA_EXT_OK / CHIUSA_EXT_KO = chiusura confermata dopo ACK BPM esterno
-- Rimuove lo stato COMPLETATO (non più usato).
-- NOTA: il constraint potrebbe essere già stato aggiornato manualmente; la DROP
--       viene ignorata se il constraint non esiste con il nome originale.

ALTER TABLE task DROP CONSTRAINT IF EXISTS chk_task_stato;
ALTER TABLE task ADD CONSTRAINT chk_task_stato
    CHECK (stato IN ('IN_CODA','IN_CARICO','CHIUSA_SD_OK','CHIUSA_SD_KO','CHIUSA_EXT_OK','CHIUSA_EXT_KO'));
