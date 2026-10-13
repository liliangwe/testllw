package com.antdigital.todo.dao.mapper.impl;

import com.antdigital.todo.dao.entity.TodoTaskDO;
import com.antdigital.todo.dao.mapper.TodoTaskMapper;
import java.sql.PreparedStatement;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/**
 * 待办事项数据访问实现，基于 JdbcTemplate 的参数化 SQL 写入。
 *
 * <p>SQL 全部使用占位符（?），禁止字符串拼接，避免 SQL 注入（安全规约）。</p>
 *
 * @author AiWork
 * @date 2026/10/13
 */
@Repository
public class TodoTaskMapperImpl implements TodoTaskMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(TodoTaskMapperImpl.class);

    /** 新增待办事项 SQL，字段与 todo_task 表结构一一对应 */
    private static final String INSERT_SQL = "INSERT INTO todo_task "
            + "(tenant_id, task_name, task_desc, creator, is_deleted, gmt_create, gmt_modified) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    /** 语句执行超时（秒），与系分 6.3「连接与语句超时（建议 ≤ 2s）」保持一致（G9.2） */
    private static final int INSERT_QUERY_TIMEOUT_SECONDS = 2;

    private final JdbcTemplate jdbcTemplate;

    public TodoTaskMapperImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long insert(TodoTaskDO todoTask) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, todoTask.getTenantId());
            statement.setString(2, todoTask.getTaskName());
            statement.setString(3, todoTask.getTaskDesc());
            statement.setString(4, todoTask.getCreator());
            statement.setInt(5, todoTask.getIsDeleted());
            statement.setObject(6, todoTask.getGmtCreate());
            statement.setObject(7, todoTask.getGmtModified());
            statement.setQueryTimeout(INSERT_QUERY_TIMEOUT_SECONDS);
            return statement;
        }, keyHolder);

        Number generatedKey = keyHolder.getKey();
        if (generatedKey == null) {
            LOGGER.error("insert todo_task returned no generated key, tenantId:{}, creator:{}",
                    todoTask.getTenantId(), todoTask.getCreator());
            return null;
        }
        return generatedKey.longValue();
    }
}
