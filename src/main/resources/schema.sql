CREATE TABLE IF NOT EXISTS demo_api_tracking (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    api_name      VARCHAR(32)  NOT NULL,
    caller_id     VARCHAR(64)  NOT NULL DEFAULT 'anonymous',
    person_type   VARCHAR(32)  NOT NULL DEFAULT 'unknown',
    person_level  VARCHAR(32)  NOT NULL DEFAULT 'unknown',
    department    VARCHAR(64)  NOT NULL DEFAULT 'unknown',
    call_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_create    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tracking_api_time ON demo_api_tracking (api_name, call_time);
CREATE INDEX IF NOT EXISTS idx_tracking_dim ON demo_api_tracking (person_type, call_time);