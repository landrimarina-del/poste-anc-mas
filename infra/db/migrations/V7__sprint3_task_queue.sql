-- =====================================================================
-- V7 - Sprint 3: tabella task per coda attivita operatore
-- Crea la tabella applicativa usata da TaskManagementService.
-- =====================================================================

CREATE TABLE IF NOT EXISTS task (
    id BIGINT NOT NULL AUTO_INCREMENT,
    practice_id BIGINT NOT NULL,
    flowable_task_id VARCHAR(64) NULL,
    tipo_pratica VARCHAR(32) NOT NULL DEFAULT 'ANC',
    stato VARCHAR(16) NOT NULL,
    candidate_group_id BIGINT NOT NULL,
    owner_user_id BIGINT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    accepted_at DATETIME(3) NULL,
    completed_at DATETIME(3) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_task_practice FOREIGN KEY (practice_id) REFERENCES practice(id),
    CONSTRAINT fk_task_group FOREIGN KEY (candidate_group_id) REFERENCES user_group(id),
    CONSTRAINT fk_task_owner FOREIGN KEY (owner_user_id) REFERENCES app_user(id),
    CONSTRAINT chk_task_stato CHECK (stato IN ('IN_CODA','IN_CARICO','COMPLETATO')),
    KEY idx_task_owner_stato (owner_user_id, stato),
    KEY idx_task_group_stato (candidate_group_id, stato),
    KEY idx_task_flowable (flowable_task_id),
    KEY idx_task_practice_stato (practice_id, stato)
) ENGINE=InnoDB;
