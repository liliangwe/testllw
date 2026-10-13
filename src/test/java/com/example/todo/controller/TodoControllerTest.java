package com.example.todo.controller;

import com.example.todo.common.BizException;
import com.example.todo.common.Result;
import com.example.todo.dto.TodoCreateRequestDTO;
import com.example.todo.dto.TodoCreateResultDTO;
import com.example.todo.service.TodoService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * TodoController 单元测试
 *
 * @author AiWork
 * @date 2026/10/13
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TodoController 单元测试")
class TodoControllerTest {

    @Mock
    private TodoService todoService;

    private TodoController todoController;

    @BeforeEach
    void setUp() {
        todoController = new TodoController(todoService, new SimpleMeterRegistry());
        ReflectionTestUtils.setField(todoController, "createEnabled", true);
    }

    // ======================== 正常路径 ========================

    @Test
    @DisplayName("正常请求 → 返回 200 及创建结果")
    void should_return200_when_validRequest() {
        TodoCreateRequestDTO request = buildRequest("测试待办", "测试描述");
        TodoCreateResultDTO svcResult = buildResult(1L, "测试待办", "测试描述");
        when(todoService.createTodo(any(TodoCreateRequestDTO.class))).thenReturn(svcResult);

        ResponseEntity<Result<TodoCreateResultDTO>> response = todoController.createTodo(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("OK", response.getBody().getCode());
        assertEquals(1L, response.getBody().getData().getId());
    }

    // ======================== 异常路径 ========================

    @Test
    @DisplayName("Service 抛出 BizException → 返回 400")
    void should_return400_when_bizException() {
        TodoCreateRequestDTO request = buildRequest("", null);
        when(todoService.createTodo(any(TodoCreateRequestDTO.class)))
                .thenThrow(new BizException("TODO_001", "事项名称不能为空"));

        ResponseEntity<Result<TodoCreateResultDTO>> response = todoController.createTodo(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TODO_001", response.getBody().getCode());
    }

    @Test
    @DisplayName("未知异常 → 返回 500")
    void should_return500_when_unknownError() {
        TodoCreateRequestDTO request = buildRequest("测试", null);
        when(todoService.createTodo(any(TodoCreateRequestDTO.class)))
                .thenThrow(new RuntimeException("unexpected"));

        ResponseEntity<Result<TodoCreateResultDTO>> response = todoController.createTodo(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TODO_999", Objects.requireNonNull(response.getBody()).getCode());
    }

    @Test
    @DisplayName("功能开关关闭 → 返回 503")
    void should_return503_when_featureDisabled() {
        ReflectionTestUtils.setField(todoController, "createEnabled", false);
        TodoCreateRequestDTO request = buildRequest("测试", "测试");

        ResponseEntity<Result<TodoCreateResultDTO>> response = todoController.createTodo(request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TODO_998", response.getBody().getCode());
    }

    // ======================== helpers ========================

    private TodoCreateRequestDTO buildRequest(String name, String description) {
        TodoCreateRequestDTO req = new TodoCreateRequestDTO();
        req.setName(name);
        req.setDescription(description);
        return req;
    }

    private TodoCreateResultDTO buildResult(Long id, String name, String description) {
        TodoCreateResultDTO result = new TodoCreateResultDTO();
        result.setId(id);
        result.setName(name);
        result.setDescription(description);
        result.setStatus("PENDING");
        result.setGmtCreate("2026-10-13T10:30:00");
        return result;
    }
}