-- 待办事项表（系分 5.1.1.1）
-- 约定：表名/字段名全小写下划线；必备 id/gmt_create/gmt_modified；禁用 timestamp、float/double、enum；
--      索引前缀 pk_/uk_/idx_；不使用外键、触发器、视图、存储过程。
CREATE TABLE IF NOT EXISTS todo_task (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID，待办事项唯一标识',
    tenant_id    VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '租户标识，用于租户隔离',
    task_name    VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '事项名称，必填',
    task_desc    VARCHAR(512) NOT NULL DEFAULT '' COMMENT '事项描述，选填，空时存空串',
    creator      VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建人登录名/工号，取自登录态',
    is_deleted   TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    gmt_create   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，代码侧维护',
    gmt_modified DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间，代码侧维护（创建时与创建时间一致）',
    CONSTRAINT pk_todo_task PRIMARY KEY (id)
) COMMENT = '待办事项表';

-- 本期仅创建路径，无按租户/创建人的检索诉求，故不建二级索引；
-- 下期新增列表查询时按左前缀原则补充：
-- ALTER TABLE todo_task ADD INDEX idx_todo_task_tenant_creator (tenant_id, creator);
