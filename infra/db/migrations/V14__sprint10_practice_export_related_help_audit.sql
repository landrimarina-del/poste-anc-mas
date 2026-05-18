-- Sprint 10 ANC backend: C2.9 azioni correlate.

CREATE TABLE IF NOT EXISTS related_action (
    id BIGINT NOT NULL AUTO_INCREMENT,
    practice_id BIGINT NOT NULL,
    action_code VARCHAR(64) NOT NULL,
    action_label VARCHAR(255) NOT NULL,
    target_url VARCHAR(512) NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_related_action_practice (practice_id),
    CONSTRAINT fk_related_action_practice FOREIGN KEY (practice_id) REFERENCES practice(id)
) ENGINE=InnoDB;
