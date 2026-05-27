-- V111: aggiunge colonna card_expired a checklist_carta per la nuova regola DMN CARTA_SCADUTA
ALTER TABLE checklist_carta
    ADD COLUMN card_expired TINYINT(1) NULL DEFAULT NULL COMMENT 'Carta di identità scaduta (regola DMN CARTA_SCADUTA)';
