-- Sprint 9 ANC backend: dashboard segnalazioni + integrazione stub Sinergia.

CREATE TABLE IF NOT EXISTS signal_case (
    id BIGINT NOT NULL AUTO_INCREMENT,
    practice_id BIGINT NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    owner_user_id BIGINT NULL,
    candidate_group_id BIGINT NULL,
    stato VARCHAR(20) NOT NULL,
    subject VARCHAR(120) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    sinergia_ticket_id VARCHAR(80) NULL,
    sinergia_forwarded_at TIMESTAMP(3) NULL,
    closed_at TIMESTAMP(3) NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_signal_case_practice (practice_id),
    KEY idx_signal_case_state (stato),
    KEY idx_signal_case_owner (owner_user_id),
    KEY idx_signal_case_created_at (created_at),
    CONSTRAINT fk_signal_case_practice FOREIGN KEY (practice_id) REFERENCES practice(id),
    CONSTRAINT fk_signal_case_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
    CONSTRAINT fk_signal_case_owner FOREIGN KEY (owner_user_id) REFERENCES app_user(id),
    CONSTRAINT fk_signal_case_group FOREIGN KEY (candidate_group_id) REFERENCES user_group(id)
) ENGINE=InnoDB;

ALTER TABLE signal_case
    ADD CONSTRAINT chk_signal_case_state
    CHECK (stato IN ('IN_CODA', 'IN_LAVORAZIONE', 'CHIUSO'));
