package com.antdigital.todo.service.impl;

import com.antdigital.todo.dao.mapper.TodoItemMapper;
import com.antdigital.todo.model.dto.TodoCreateRequest;
import com.antdigital.todo.model.dto.TodoItemVO;
import com.antdigital.todo.model.entity.TodoItemDO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TodoServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TodoServiceImpl 单元测试")
class TodoServiceImplTest {

    @Mock
    private TodoItemMapper todoItemMapper;

    @InjectMocks
    private TodoServiceImpl todoService;

    // ==================== createTodo 测试 ====================

    @Test
    @DisplayName("正常路径：有效请求应返回 TodoItemVO 并包含生成的 ID")
    void should_returnTodoItemVO_when_validRequest() {
        // Arrange
        TodoCreateRequest request = new TodoCreateRequest();
        request.setTodoName("完成系分设计文档");
        request.setDescription("输出待办事项模块的完整系分设计");

        when(todoItemMapper.insert(any(TodoItemDO.class))).thenAnswer(invocation -> {
            TodoItemDO arg = invocation.getArgument(0);
            arg.setId(1L);
            return 1;
        });

        // Act
        TodoItemVO result = todoService.createTodo(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTodoName()).isEqualTo("完成系分设计文档");
        assertThat(result.getDescription()).isEqualTo("输出待办事项模块的完整系分设计");
        assertThat(result.getGmtCreate()).isNotNull();

        ArgumentCaptor<TodoItemDO> captor = ArgumentCaptor.forClass(TodoItemDO.class);
        verify(todoItemMapper).insert(captor.capture());
        TodoItemDO captured = captor.getValue();
        assertThat(captured.getTodoName()).isEqualTo("完成系分设计文档");
        assertThat(captured.getDescription()).isEqualTo("输出待办事项模块的完整系分设计");
    }

    @Test
    @DisplayName("参数校验：todoName 为 null 应抛出 IllegalArgumentException")
    void should_throwException_when_todoNameIsNull() {
        // Arrange
        TodoCreateRequest request = new TodoCreateRequest();
        request.setTodoName(null);
        request.setDescription("描述");

        // Act & Assert
        assertThatThrownBy(() -> todoService.createTodo(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("事项名称不能为空");
    }

    @Test
    @DisplayName("参数校验：todoName 为空字符串应抛出 IllegalArgumentException")
    void should_throwException_when_todoNameIsEmpty() {
        // Arrange
        TodoCreateRequest request = new TodoCreateRequest();
        request.setTodoName("");
        request.setDescription("描述");

        // Act & Assert
        assertThatThrownBy(() -> todoService.createTodo(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("事项名称不能为空");
    }

    @Test
    @DisplayName("参数校验：todoName 为纯空格应抛出 IllegalArgumentException")
    void should_throwException_when_todoNameIsBlank() {
        // Arrange
        TodoCreateRequest request = new TodoCreateRequest();
        request.setTodoName("   ");
        request.setDescription("描述");

        // Act & Assert
        assertThatThrownBy(() -> todoService.createTodo(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("事项名称不能为空");
    }

    @Test
    @DisplayName("边界值：todoName 恰好 200 字符应成功创建")
    void should_createSuccessfully_when_todoNameAtMaxLength() {
        // Arrange
        TodoCreateRequest request = new TodoCreateRequest();
        request.setTodoName("A".repeat(200));
        request.setDescription("描述");

        when(todoItemMapper.insert(any(TodoItemDO.class))).thenAnswer(invocation -> {
            TodoItemDO arg = invocation.getArgument(0);
            arg.setId(1L);
            return 1;
        });

        // Act
        TodoItemVO result = todoService.createTodo(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTodoName()).hasSize(200);
    }

    @Test
    @DisplayName("边界值：description 为 null 应成功创建")
    void should_createSuccessfully_when_descriptionIsNull() {
        // Arrange
        TodoCreateRequest request = new TodoCreateRequest();
        request.setTodoName("测试事项");
        request.setDescription(null);

        when(todoItemMapper.insert(any(TodoItemDO.class))).thenAnswer(invocation -> {
            TodoItemDO arg = invocation.getArgument(0);
            arg.setId(1L);
            return 1;
        });

        // Act
        TodoItemVO result = todoService.createTodo(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isNull();
    }

    @Test
    @DisplayName("边界值：description 恰好 2000 字符应成功创建")
    void should_createSuccessfully_when_descriptionAtMaxLength() {
        // Arrange
        TodoCreateRequest request = new TodoCreateRequest();
        request.setTodoName("测试事项");
        request.setDescription("D".repeat(2000));

        when(todoItemMapper.insert(any(TodoItemDO.class))).thenAnswer(invocation -> {
            TodoItemDO arg = invocation.getArgument(0);
            arg.setId(1L);
            return 1;
        });

        // Act
        TodoItemVO result = todoService.createTodo(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDescription()).hasSize(2000);
    }
}