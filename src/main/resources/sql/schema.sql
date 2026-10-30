CREATE DATABASE IF NOT EXISTS demo_track DEFAULT CHARACTER SET utf8mb4;

USE demo_track;

-- 接口调用埋点表
CREATE TABLE IF NOT EXISTS api_invoke_track (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '系统自增主键',
    api_code      VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '接口编码：hello/hash/bubble_sort/export',
    api_path      VARCHAR(128) NOT NULL DEFAULT '' COMMENT '请求路径',
    caller_id     VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '调用人工号/ID',
    caller_name   VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '调用人姓名',
    person_type   VARCHAR(16)  NOT NULL DEFAULT '' COMMENT '人员类型：FORMAL/OUTSOURCE/INTERN',
    person_level  VARCHAR(8)   NOT NULL DEFAULT '' COMMENT '人员层级：P5-P9',
    person_dept   VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '人员部门',
    invoke_result VARCHAR(16)  NOT NULL DEFAULT '' COMMENT '调用结果：SUCCESS/FAIL',
    cost_ms       BIGINT       NOT NULL DEFAULT 0 COMMENT '处理耗时（毫秒）',
    invoke_time   DATETIME     NOT NULL COMMENT '调用时间',
    gmt_create    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY pk_api_invoke_track (id),
    KEY idx_track_time (invoke_time),
    KEY idx_track_dept_time (person_dept, invoke_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '接口调用埋点表';
