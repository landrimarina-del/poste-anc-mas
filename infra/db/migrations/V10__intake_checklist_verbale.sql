CREATE TABLE IF NOT EXISTS checklist_verbale (
    practice_id BIGINT NOT NULL PRIMARY KEY,
    document_present TINYINT(1) NOT NULL,
    readability_ok TINYINT(1) NULL,
    formal_ok TINYINT(1) NULL,
    customer_data_ok TINYINT(1) NULL,
    card_number_match_required TINYINT(1) NOT NULL DEFAULT 0,
    card_number_match_ok TINYINT(1) NULL,
    ko_reasons_json JSON NULL,
    internal_notes TEXT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_checklist_verbale_practice FOREIGN KEY (practice_id) REFERENCES practice(id),
    CONSTRAINT ck_checklist_verbale_status CHECK (status IN ('NON_INIZIATA', 'BOZZA', 'RIAPERTA', 'CONSOLIDATA'))
);

CREATE TABLE IF NOT EXISTS practice_outcome (
    practice_id BIGINT NOT NULL PRIMARY KEY,
    outcome VARCHAR(20) NOT NULL,
    ko_codes_json JSON NULL,
    computed_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    computed_by VARCHAR(100) NOT NULL,
    CONSTRAINT fk_practice_outcome_practice FOREIGN KEY (practice_id) REFERENCES practice(id),
    CONSTRAINT ck_practice_outcome_value CHECK (outcome IN ('APPROVATA', 'RESPINTA'))
);
