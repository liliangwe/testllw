-- 待办事项表
CREATE TABLE IF NOT EXISTS todo_item (
    id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '系统自增主键',
    todo_name    VARCHAR(200)  NOT NULL              COMMENT '事项名称',
    description  VARCHAR(2000) DEFAULT NULL          COMMENT '事项描述',
    gmt_create   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY pk_todo_item (id),
    INDEX idx_todo_item_gmt_create (gmt_create)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='待办事项表';