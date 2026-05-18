-- ============================================================================
-- Cleanup dominio pratiche/audit Sprint 1 (FK-safe)
-- ATTENZIONE: non tocca tabelle IAM (app_user, role, user_role, user_group, user_group_member)
-- ============================================================================

USE anc;

-- 1) Tabelle figlie della pratica
DELETE FROM bpm_inbound_message;
DELETE FROM practice_state_history;
DELETE FROM attachment;
DELETE FROM card_data;
DELETE FROM client_data;
DELETE FROM audit_event;

-- 2) Tabella padre
DELETE FROM practice;

-- 3) Reset auto increment dove sensato
ALTER TABLE bpm_inbound_message AUTO_INCREMENT = 1;
ALTER TABLE practice_state_history AUTO_INCREMENT = 1;
ALTER TABLE attachment AUTO_INCREMENT = 1;
ALTER TABLE audit_event AUTO_INCREMENT = 1;
ALTER TABLE practice AUTO_INCREMENT = 1;
