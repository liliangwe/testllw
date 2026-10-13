package com.antdigital.todo.dao.mapper.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antdigital.todo.dao.entity.TodoTaskDO;
import com.antdigital.todo.common.constant.TodoTaskConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.KeyHolder;

/**
 * {@link TodoTaskMapperImpl} 数据访问实现测试（G9.2/G11.2）。
 *
 * <p>以 Mock 的 {@link Connection}/{@link PreparedStatement} 捕获真实提交给驱动的 SQL 与参数绑定顺序，
 * 覆盖参数化 INSERT、自增主键回填、语句超时设置与「无自增主键」边界分支；
 * 真实 MySQL/OB 的 SQL 兼容性仍需集成测试（H2/Testcontainers）在具备环境时补充。</p>
 *
 * @author AiWork
 * @date 2026/10/13
 */
@ExtendWith(MockitoExtension.class)
class TodoTaskMapperImplTest {

    /** 语句超时（秒），与系分 6.3「连接与语句超时（建议 ≤ 2s）」一致 */
    private static final int EXPECTED_QUERY_TIMEOUT_SECONDS = 2;

    private static final Long GENERATED_KEY = 1000001L;

    private static final LocalDateTime GMT = LocalDateTime.of(2026, 10, 13, 10, 20, 30);

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    private TodoTaskMapperImpl todoTaskMapper;

    @BeforeEach
    void setUp() {
        todoTaskMapper = new TodoTaskMapperImpl(jdbcTemplate);
    }

    @Test
    void should_bindAllColumnsAndReturnGeneratedKey_when_insertSucceeded() throws Exception {
        // Arrange
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        givenInsertExecuted(GENERATED_KEY);

        // Act
        Long taskId = todoTaskMapper.insert(buildTodoTask());

        // Assert - 自增主键回填
        assertThat(taskId).isEqualTo(GENERATED_KEY);

        // Assert - SQL 为参数化语句（占位符 7 个，与字段一一对应，无字符串拼接）
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(connection).prepareStatement(sqlCaptor.capture(), eq(Statement.RETURN_GENERATED_KEYS));
        assertThat(sqlCaptor.getValue())
                .isEqualTo("INSERT INTO todo_task (tenant_id, task_name, task_desc, creator, is_deleted, "
                        + "gmt_create, gmt_modified) VALUES (?, ?, ?, ?, ?, ?, ?)");

        // Assert - 参数绑定顺序与字段顺序一致，并设置语句超时
        InOrder inOrder = inOrder(preparedStatement);
        inOrder.verify(preparedStatement).setString(1, "tenant001");
        inOrder.verify(preparedStatement).setString(2, "整理季度复盘材料");
        inOrder.verify(preparedStatement).setString(3, "收集各组数据");
        inOrder.verify(preparedStatement).setString(4, "10086");
        inOrder.verify(preparedStatement).setInt(5, TodoTaskConstants.IS_DELETED_NO);
        inOrder.verify(preparedStatement).setObject(6, GMT);
        inOrder.verify(preparedStatement).setObject(7, GMT);
        inOrder.verify(preparedStatement).setQueryTimeout(EXPECTED_QUERY_TIMEOUT_SECONDS);
    }

    @Test
    void should_returnNull_when_generatedKeyIsAbsent() throws Exception {
        // Arrange：驱动未回填自增主键
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        givenInsertExecuted(null);

        // Act
        Long taskId = todoTaskMapper.insert(buildTodoTask());

        // Assert
        assertThat(taskId).isNull();
    }

    /**
     * 模拟 JdbcTemplate 执行 insert：调用入参的 {@link PreparedStatementCreator} 以校验参数绑定，
     * 并按需向 {@link KeyHolder} 回填自增主键。
     *
     * @param generatedKey 需要回填的自增主键，null 表示不回填
     */
    private void givenInsertExecuted(Long generatedKey) {
        when(jdbcTemplate.update(any(PreparedStatementCreator.class), any(KeyHolder.class))).thenAnswer(invocation -> {
            PreparedStatementCreator preparedStatementCreator = invocation.getArgument(0);
            KeyHolder keyHolder = invocation.getArgument(1);
            preparedStatementCreator.createPreparedStatement(connection);
            if (generatedKey != null) {
                keyHolder.getKeyList().add(Collections.singletonMap("GENERATED_KEY", generatedKey));
            }
            return 1;
        });
    }

    private TodoTaskDO buildTodoTask() {
        TodoTaskDO todoTask = new TodoTaskDO();
        todoTask.setTenantId("tenant001");
        todoTask.setTaskName("整理季度复盘材料");
        todoTask.setTaskDesc("收集各组数据");
        todoTask.setCreator("10086");
        todoTask.setIsDeleted(TodoTaskConstants.IS_DELETED_NO);
        todoTask.setGmtCreate(GMT);
        todoTask.setGmtModified(GMT);
        return todoTask;
    }
}
