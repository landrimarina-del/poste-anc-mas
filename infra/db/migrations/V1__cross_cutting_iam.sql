-- =====================================================================
-- V1 - Cross-cutting M-IAM (DBA 03_Schema_DDL.md §1)
-- Sprint 0 Foundation: solo tabelle utenti/ruoli/gruppi.
-- Le tabelle dei BC (practice, task, attachment, audit_event, ...) NON
-- vanno create in Sprint 0 e arriveranno con le migration V3+.
-- Engine InnoDB, charset utf8mb4, naming snake_case.
-- =====================================================================

CREATE TABLE app_user (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    username        VARCHAR(64)  NOT NULL,
    password_hash   VARCHAR(255) NULL,
    full_name       VARCHAR(128) NOT NULL,
    email           VARCHAR(128) NULL,
    active          TINYINT(1)   NOT NULL DEFAULT 1,
    created_at      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE role (
    id      BIGINT      NOT NULL AUTO_INCREMENT,
    code    VARCHAR(32) NOT NULL,
    name    VARCHAR(64) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES app_user(id),
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_group (
    id    BIGINT       NOT NULL AUTO_INCREMENT,
    code  VARCHAR(64)  NOT NULL,
    name  VARCHAR(128) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_group_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_group_member (
    user_id  BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, group_id),
    CONSTRAINT fk_ugm_user  FOREIGN KEY (user_id)  REFERENCES app_user(id),
    CONSTRAINT fk_ugm_group FOREIGN KEY (group_id) REFERENCES user_group(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
