-- Sprint 12: persistenza strutturata note checklist VERBALE.
ALTER TABLE checklist_verbale
    ADD COLUMN note_legibility TEXT NULL,
    ADD COLUMN note_formal_suitability TEXT NULL,
    ADD COLUMN note_client_data_consistency TEXT NULL,
    ADD COLUMN note_card_number_match TEXT NULL,
    ADD COLUMN final_note_practice TEXT NULL;
