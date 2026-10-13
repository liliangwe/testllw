-- 待办事项表
CREATE TABLE IF NOT EXISTS todo_item (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name        VARCHAR(200) NOT NULL                COMMENT '待办名称',
    description VARCHAR(2000) DEFAULT NULL           COMMENT '待办描述',
    status      VARCHAR(32)  NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待处理, COMPLETED-已完成',
    gmt_create  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    INDEX idx_todo_item_status (status),
    INDEX idx_todo_item_gmt_create (gmt_create)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='待办事项表';