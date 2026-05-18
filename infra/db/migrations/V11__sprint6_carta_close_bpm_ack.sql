-- Sprint 6 ANC backend: checklist carta, close workflow e ACK BPM.

CREATE TABLE IF NOT EXISTS checklist_carta (
    practice_id BIGINT NOT NULL,
    card_present BOOLEAN NOT NULL,
    card_conformity_ok BOOLEAN NULL,
    status VARCHAR(20) NOT NULL,
    internal_notes VARCHAR(2000) NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (practice_id),
    CONSTRAINT fk_checklist_carta_practice FOREIGN KEY (practice_id) REFERENCES practice(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bpm_outbound_message (
    id BIGINT NOT NULL AUTO_INCREMENT,
    practice_id BIGINT NOT NULL,
    request_id VARCHAR(64) NULL,
    correlation_id VARCHAR(128) NOT NULL,
    payload_json TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    response_body VARCHAR(2000) NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    sent_at TIMESTAMP(3) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_bpm_outbound_corr (correlation_id),
    KEY idx_bpm_outbound_practice (practice_id),
    CONSTRAINT fk_bpm_outbound_practice FOREIGN KEY (practice_id) REFERENCES practice(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bpm_outcome_ack (
    id BIGINT NOT NULL AUTO_INCREMENT,
    practice_id BIGINT NOT NULL,
    request_id VARCHAR(64) NULL,
    correlation_id VARCHAR(128) NOT NULL,
    final_state VARCHAR(20) NOT NULL,
    ack_payload_json TEXT NOT NULL,
    processed_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_bpm_ack_corr (correlation_id),
    KEY idx_bpm_ack_request (request_id),
    KEY idx_bpm_ack_practice (practice_id),
    CONSTRAINT fk_bpm_ack_practice FOREIGN KEY (practice_id) REFERENCES practice(id)
) ENGINE=InnoDB;

ALTER TABLE practice
    ADD COLUMN IF NOT EXISTS data_chiusura TIMESTAMP(3) NULL;
