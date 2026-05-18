CREATE TABLE IF NOT EXISTS favorite_link (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    titolo VARCHAR(120) NOT NULL,
    url VARCHAR(1000) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_favorite_link_user FOREIGN KEY (user_id) REFERENCES app_user(id)
);

CREATE INDEX idx_favorite_link_user_updated
    ON favorite_link (user_id, updated_at);
