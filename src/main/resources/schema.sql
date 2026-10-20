CREATE TABLE IF NOT EXISTS t_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(64)  NOT NULL,
    password    VARCHAR(256) NOT NULL COMMENT 'BCrypt encrypted',
    salt        VARCHAR(64)  DEFAULT '',
    status      TINYINT      DEFAULT 1 COMMENT '1: normal, 0: disabled',
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_username UNIQUE (username)
);

-- 默认测试用户（密码: 123456）
INSERT INTO t_user (username, password, status) VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 1);