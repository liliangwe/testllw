-- MySQL 部署 DDL（demo_api_tracking，与 src/main/resources/schema.sql 的 H2 版本保持一致）
CREATE TABLE IF NOT EXISTS demo_api_tracking (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    api_name      VARCHAR(32)  NOT NULL,
    caller_id     VARCHAR(64)  NOT NULL DEFAULT 'anonymous',
    person_type   VARCHAR(32)  NOT NULL DEFAULT 'unknown',
    person_level  VARCHAR(32)  NOT NULL DEFAULT 'unknown',
    department    VARCHAR(64)  NOT NULL DEFAULT 'unknown',
    call_time     DATETIME     NOT NULL,
    gmt_create    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_tracking_api_time (api_name, call_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 维度查询左前缀索引（person_type, call_time），与 design 一致
CREATE INDEX idx_tracking_dim ON demo_api_tracking (person_type, call_time);