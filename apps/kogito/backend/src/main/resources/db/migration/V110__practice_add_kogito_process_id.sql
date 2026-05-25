-- =====================================================================
-- V110 — Aggiunge kogito_process_id alla tabella practice
-- Memorizza il UUID dell'istanza di processo Kogito avviata per la
-- pratica, necessario per completare i WorkItem via API Kogito.
-- =====================================================================

ALTER TABLE practice
    ADD COLUMN kogito_process_id VARCHAR(36) NULL COMMENT 'UUID istanza processo Kogito (process_instances.id)' AFTER ticket_id,
    ADD KEY idx_practice_kogito_process_id (kogito_process_id);
