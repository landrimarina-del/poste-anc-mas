-- =====================================================================
-- V4 - Sprint 1: allineamento nomenclatura appian* -> request*
-- Obiettivo: rinomina colonna practice.appian_ticket_id in request_id
-- =====================================================================

ALTER TABLE practice
    CHANGE COLUMN appian_ticket_id request_id VARCHAR(64) NOT NULL;

ALTER TABLE practice
    DROP INDEX uk_practice_appian_ticket,
    ADD UNIQUE KEY uk_practice_request_id (request_id);
