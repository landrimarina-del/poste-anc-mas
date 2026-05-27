-- V112: aggiunge colonna card_legibility_ok a checklist_carta per persistere la leggibilità CARTA separatamente
ALTER TABLE checklist_carta ADD COLUMN card_legibility_ok TINYINT(1) NULL;
