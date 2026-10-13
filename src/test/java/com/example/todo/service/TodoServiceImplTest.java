package com.example.todo.service;

import com.example.todo.common.BizException;
import com.example.todo.dto.TodoCreateRequestDTO;
import com.example.todo.dto.TodoCreateResultDTO;
import com.example.todo.entity.TodoItemDO;
import com.example.todo.mapper.TodoItemMapper;
import com.example.todo.service.impl.TodoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TodoServiceImpl 单元测试
 *
 * @author AiWork
 * @date 2026/10/13
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TodoServiceImpl 单元测试")
class TodoServiceImplTest {

    @Mock
    private TodoItemMapper todoItemMapper;

    @InjectMocks
    private TodoServiceImpl todoService;

    private TodoCreateRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new TodoCreateRequestDTO();
        validRequest.setName("完成系分设计文档");
        validRequest.setDescription("完成待办事项模块的系统分析与设计文档编写");
    }

    // ======================== 正常路径 ========================

    @Test
    @DisplayName("正常请求 → 返回创建结果含自增ID")
    void should_returnResult_when_validRequest() {
        when(todoItemMapper.insert(any(TodoItemDO.class))).thenAnswer(inv -> {
            TodoItemDO item = inv.getArgument(0);
            item.setId(1L);
            return 1;
        });

        TodoCreateResultDTO result = todoService.createTodo(validRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("完成系分设计文档", result.getName());
        assertEquals("完成待办事项模块的系统分析与设计文档编写", result.getDescription());
        assertEquals("PENDING", result.getStatus());
        assertNotNull(result.getGmtCreate());

        ArgumentCaptor<TodoItemDO> captor = ArgumentCaptor.forClass(TodoItemDO.class);
        verify(todoItemMapper).insert(captor.capture());
        TodoItemDO captured = captor.getValue();
        assertEquals("完成系分设计文档", captured.getName());
        assertEquals("PENDING", captured.getStatus());
        assertNotNull(captured.getGmtCreate());
        assertNotNull(captured.getGmtModified());
    }

    // ======================== 参数校验 ========================

    @Test
    @DisplayName("name 为空 → 抛出 TODO_001 异常")
    void should_throwException_when_nameNull() {
        validRequest.setName(null);

        BizException ex = assertThrows(BizException.class,
                () -> todoService.createTodo(validRequest));
        assertEquals("TODO_001", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("事项名称不能为空"));

        verify(todoItemMapper, never()).insert(any());
    }

    @Test
    @DisplayName("name 为纯空白 → 抛出 TODO_001 异常")
    void should_throwException_when_nameBlank() {
        validRequest.setName("   ");

        BizException ex = assertThrows(BizException.class,
                () -> todoService.createTodo(validRequest));
        assertEquals("TODO_001", ex.getErrorCode());

        verify(todoItemMapper, never()).insert(any());
    }

    @Test
    @DisplayName("name 超过 200 字符 → 抛出 TODO_002 异常")
    void should_throwException_when_nameTooLong() {
        validRequest.setName("A".repeat(201));

        BizException ex = assertThrows(BizException.class,
                () -> todoService.createTodo(validRequest));
        assertEquals("TODO_002", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("200"));

        verify(todoItemMapper, never()).insert(any());
    }

    @Test
    @DisplayName("description 超过 2000 字符 → 抛出 TODO_003 异常")
    void should_throwException_when_descriptionTooLong() {
        validRequest.setDescription("A".repeat(2001));

        BizException ex = assertThrows(BizException.class,
                () -> todoService.createTodo(validRequest));
        assertEquals("TODO_003", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("2000"));

        verify(todoItemMapper, never()).insert(any());
    }

    // ======================== 边界值 ========================

    @Test
    @DisplayName("name 恰好 200 字符 → 正常创建")
    void should_succeed_when_nameExactly200() {
        validRequest.setName("A".repeat(200));

        when(todoItemMapper.insert(any(TodoItemDO.class))).thenAnswer(inv -> {
            TodoItemDO item = inv.getArgument(0);
            item.setId(1L);
            return 1;
        });

        TodoCreateResultDTO result = todoService.createTodo(validRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(todoItemMapper).insert(any(TodoItemDO.class));
    }

    @Test
    @DisplayName("description 恰好 2000 字符 → 正常创建")
    void should_succeed_when_descriptionExactly2000() {
        validRequest.setDescription("A".repeat(2000));

        when(todoItemMapper.insert(any(TodoItemDO.class))).thenAnswer(inv -> {
            TodoItemDO item = inv.getArgument(0);
            item.setId(1L);
            return 1;
        });

        TodoCreateResultDTO result = todoService.createTodo(validRequest);

        assertNotNull(result);
        verify(todoItemMapper).insert(any(TodoItemDO.class));
    }

    @Test
    @DisplayName("description 为 null → 正常创建")
    void should_succeed_when_descriptionNull() {
        validRequest.setDescription(null);

        when(todoItemMapper.insert(any(TodoItemDO.class))).thenAnswer(inv -> {
            TodoItemDO item = inv.getArgument(0);
            item.setId(1L);
            return 1;
        });

        TodoCreateResultDTO result = todoService.createTodo(validRequest);

        assertNotNull(result);
        assertNull(result.getDescription());
        verify(todoItemMapper).insert(any(TodoItemDO.class));
    }

    // ======================== 异常处理 ========================

    @Test
    @DisplayName("数据库写入异常 → 抛出 TODO_999")
    void should_throwException_when_dbError() {
        when(todoItemMapper.insert(any(TodoItemDO.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        BizException ex = assertThrows(BizException.class,
                () -> todoService.createTodo(validRequest));
        assertEquals("TODO_999", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("系统内部错误"));
    }

    // ======================== 幂等性 ========================

    @Test
    @DisplayName("提供 requestId 且已存在 → 直接返回已有记录，不执行 insert")
    void should_returnExistingRecord_when_requestIdExists() {
        validRequest.setRequestId("req-001");
        TodoItemDO existing = new TodoItemDO();
        existing.setId(5L);
        existing.setName("已有待办");
        existing.setDescription("已有描述");
        existing.setStatus("PENDING");
        existing.setGmtCreate(java.time.LocalDateTime.now());
        existing.setGmtModified(java.time.LocalDateTime.now());

        when(todoItemMapper.findByRequestId("req-001")).thenReturn(existing);

        TodoCreateResultDTO result = todoService.createTodo(validRequest);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("已有待办", result.getName());
        verify(todoItemMapper, never()).insert(any());
    }

    @Test
    @DisplayName("提供 requestId 且不存在 → 正常创建")
    void should_createNormally_when_requestIdNotExists() {
        validRequest.setRequestId("req-002");

        when(todoItemMapper.findByRequestId("req-002")).thenReturn(null);
        when(todoItemMapper.insert(any(TodoItemDO.class))).thenAnswer(inv -> {
            TodoItemDO item = inv.getArgument(0);
            item.setId(10L);
            return 1;
        });

        TodoCreateResultDTO result = todoService.createTodo(validRequest);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(todoItemMapper).insert(any(TodoItemDO.class));
    }
}