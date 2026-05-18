-- Sprint 7 ANC backend: supervisione task list e riassegnazione task.

CREATE TABLE IF NOT EXISTS task_assignment_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    assigned_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    assigned_by VARCHAR(64) NULL,
    assignment_type VARCHAR(20) NOT NULL,
    target_user_id BIGINT NULL,
    target_group_id BIGINT NULL,
    reason VARCHAR(255) NULL,
    PRIMARY KEY (id),
    KEY idx_tah_task_time (task_id, assigned_at),
    KEY idx_tah_target_user (target_user_id),
    KEY idx_tah_target_group (target_group_id),
    CONSTRAINT fk_tah_task FOREIGN KEY (task_id) REFERENCES task(id),
    CONSTRAINT fk_tah_target_user FOREIGN KEY (target_user_id) REFERENCES app_user(id),
    CONSTRAINT fk_tah_target_group FOREIGN KEY (target_group_id) REFERENCES user_group(id)
) ENGINE=InnoDB;

ALTER TABLE task_assignment_history
    ADD CONSTRAINT chk_tah_assignment_type
    CHECK (assignment_type IN ('INITIAL', 'REASSIGN_USER', 'REASSIGN_GROUP'));
