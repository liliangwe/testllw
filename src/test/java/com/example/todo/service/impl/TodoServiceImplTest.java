package com.example.todo.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.todo.common.constant.TodoErrorCode;
import com.example.todo.common.exception.BusinessException;
import com.example.todo.dao.entity.TodoItemDO;
import com.example.todo.dao.mapper.TodoItemMapper;
import com.example.todo.model.dto.TodoItemDTO;
import com.example.todo.model.request.CreateTodoRequest;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * {@link TodoServiceImpl} 单元测试
 *
 * @author AiWork
 * @date 2026/10/14
 */
@ExtendWith(MockitoExtension.class)
class TodoServiceImplTest {

    /** 测试租户 ID */
    private static final String TENANT_ID = "tenant_0001";

    /** 测试事项名称 */
    private static final String NAME = "完成系分设计文档";

    /** 测试事项描述 */
    private static final String DESCRIPTION = "完成待办事项模块的系分设计并输出 design.md";

    /** 数据库回填的自增主键 */
    private static final Long GENERATED_ID = 1001L;

    /** 写入成功的受影响行数 */
    private static final int AFFECTED_ROWS_SUCCESS = 1;

    /** 写入失败的受影响行数 */
    private static final int AFFECTED_ROWS_FAILED = 0;

    /** 与待办事项模块一致的输出时间格式 */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Mock
    private TodoItemMapper todoItemMapper;

    @InjectMocks
    private TodoServiceImpl todoService;

    // ==================== createTodo 测试 ====================

    /**
     * 正常路径：合法请求 + 合法租户，返回完整的待办事项信息（R03）
     */
    @Test
    void should_returnTodoItemDTO_when_requestIsValid() {
        // Arrange
        CreateTodoRequest request = buildRequest();
        when(todoItemMapper.saveTodoItem(any(TodoItemDO.class))).thenAnswer(invocation -> {
            TodoItemDO todoItemDO = invocation.getArgument(0);
            todoItemDO.setId(GENERATED_ID);
            return AFFECTED_ROWS_SUCCESS;
        });

        // Act
        TodoItemDTO result = todoService.createTodo(request, TENANT_ID);

        // Assert
        ArgumentCaptor<TodoItemDO> captor = ArgumentCaptor.forClass(TodoItemDO.class);
        verify(todoItemMapper, times(1)).saveTodoItem(captor.capture());
        TodoItemDO savedTodoItem = captor.getValue();
        assertThat(savedTodoItem.getTenantId()).isEqualTo(TENANT_ID);
        assertThat(savedTodoItem.getName()).isEqualTo(NAME);
        assertThat(savedTodoItem.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(savedTodoItem.getGmtCreate()).isNotNull();
        assertThat(savedTodoItem.getGmtModified()).isNotNull();

        assertThat(result.getId()).isEqualTo(GENERATED_ID);
        assertThat(result.getName()).isEqualTo(NAME);
        assertThat(result.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(result.getGmtCreate())
                .as("创建时间应按 yyyy-MM-dd HH:mm:ss 输出")
                .isEqualTo(DATE_TIME_FORMATTER.format(savedTodoItem.getGmtCreate()));
    }

    /**
     * 边界值：事项描述为选填，为空时可正常创建
     */
    @Test
    void should_saveNullDescription_when_descriptionIsNotProvided() {
        // Arrange
        CreateTodoRequest request = buildRequest();
        request.setDescription(null);
        when(todoItemMapper.saveTodoItem(any(TodoItemDO.class))).thenReturn(AFFECTED_ROWS_SUCCESS);

        // Act
        TodoItemDTO result = todoService.createTodo(request, TENANT_ID);

        // Assert
        ArgumentCaptor<TodoItemDO> captor = ArgumentCaptor.forClass(TodoItemDO.class);
        verify(todoItemMapper, times(1)).saveTodoItem(captor.capture());
        assertThat(captor.getValue().getDescription()).isNull();
        assertThat(result.getDescription()).isNull();
        assertThat(result.getName()).isEqualTo(NAME);
    }

    /**
     * 异常路径：租户信息缺失（null/空串/空白串）时抛出 TODO_003，且不写入数据库（R04）
     *
     * @param tenantId 非法租户 ID
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void should_throwBusinessException_when_tenantIdIsBlank(String tenantId) {
        // Arrange
        CreateTodoRequest request = buildRequest();

        // Act & Assert
        assertThatThrownBy(() -> todoService.createTodo(request, tenantId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", TodoErrorCode.TENANT_INFO_MISSING);
        verify(todoItemMapper, never()).saveTodoItem(any(TodoItemDO.class));
    }

    /**
     * 异常路径：写入影响行数不为 1 时抛出 TODO_500
     */
    @Test
    void should_throwBusinessException_when_insertAffectsNoRow() {
        // Arrange
        CreateTodoRequest request = buildRequest();
        when(todoItemMapper.saveTodoItem(any(TodoItemDO.class))).thenReturn(AFFECTED_ROWS_FAILED);

        // Act & Assert
        assertThatThrownBy(() -> todoService.createTodo(request, TENANT_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", TodoErrorCode.SYSTEM_ERROR);
    }

    /**
     * 异常路径：数据库不可用时异常向上抛出，由全局异常处理器转换为 TODO_500
     */
    @Test
    void should_propagateException_when_databaseIsUnavailable() {
        // Arrange
        CreateTodoRequest request = buildRequest();
        when(todoItemMapper.saveTodoItem(any(TodoItemDO.class)))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));

        // Act & Assert
        assertThatThrownBy(() -> todoService.createTodo(request, TENANT_ID))
                .isInstanceOf(DataAccessResourceFailureException.class);
    }

    /**
     * 构造合法的创建请求
     *
     * @return 新增待办事项请求
     */
    private CreateTodoRequest buildRequest() {
        CreateTodoRequest request = new CreateTodoRequest();
        request.setName(NAME);
        request.setDescription(DESCRIPTION);
        return request;
    }
}
