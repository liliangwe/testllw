-- 待办事项模块测试表结构，与生产库 todo_item 保持一致（H2 MySQL 兼容模式）
DROP TABLE IF EXISTS todo_item;

CREATE TABLE todo_item
(
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    tenant_id    VARCHAR(32)   NOT NULL,
    name         VARCHAR(200)  NOT NULL,
    description  VARCHAR(2000) DEFAULT NULL,
    gmt_create   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_todo_item_tenant_id ON todo_item (tenant_id);
CREATE INDEX IF NOT EXISTS idx_todo_item_gmt_create ON todo_item (gmt_create);
