package com.antdigital.todo.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.antdigital.todo.api.request.TodoTaskCreateRequest;
import com.antdigital.todo.api.response.TodoTaskCreateResponse;
import com.antdigital.todo.common.constant.TodoResultConstants;
import com.antdigital.todo.common.constant.TodoTipConstants;
import com.antdigital.todo.common.enums.TodoErrorCodeEnum;
import com.antdigital.todo.common.exception.TodoBizException;
import com.antdigital.todo.service.TodoTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * {@link TodoTaskController} + {@link TodoExceptionHandler} 接口契约测试（W01）。
 *
 * <p>使用 MockMvc standalone 模式，不启动 Spring 容器与数据库。</p>
 *
 * @author AiWork
 * @date 2026/10/13
 */
@ExtendWith(MockitoExtension.class)
class TodoTaskControllerTest {

    private static final String CREATE_URI = "/api/todo/task/create";

    private static final String VALID_BODY = "{\"taskName\":\"整理季度复盘材料\",\"taskDesc\":\"收集各组数据\"}";

    @Mock
    private TodoTaskService todoTaskService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TodoTaskController(todoTaskService))
                .setControllerAdvice(new TodoExceptionHandler())
                .build();
    }

    @Test
    void should_returnOkResult_when_createSuccess() throws Exception {
        // Arrange
        when(todoTaskService.createTodoTask(any(TodoTaskCreateRequest.class))).thenReturn(buildResponse());

        // Act & Assert
        mockMvc.perform(post(CREATE_URI).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(TodoResultConstants.RESULT_OK))
                .andExpect(jsonPath("$.msg").value(TodoResultConstants.MSG_SUCCESS))
                .andExpect(jsonPath("$.data.taskId").value(1000001))
                .andExpect(jsonPath("$.data.taskName").value("整理季度复盘材料"))
                .andExpect(jsonPath("$.data.gmtCreate").value("2026-10-13 10:20:30"));
    }

    @Test
    void should_returnTodo001_when_taskNameIsBlank() throws Exception {
        // Act & Assert
        mockMvc.perform(post(CREATE_URI).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"taskName\":\"   \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(TodoErrorCodeEnum.PARAM_INVALID.getCode()))
                .andExpect(jsonPath("$.msg").value(TodoTipConstants.TASK_NAME_INVALID));
    }

    @Test
    void should_returnTodo001_when_bodyUnreadable() throws Exception {
        // Act & Assert
        mockMvc.perform(post(CREATE_URI).contentType(MediaType.APPLICATION_JSON).content("{invalid json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(TodoErrorCodeEnum.PARAM_INVALID.getCode()));
    }

    @Test
    void should_returnTodo005_when_createSwitchClosed() throws Exception {
        // Arrange
        when(todoTaskService.createTodoTask(any(TodoTaskCreateRequest.class)))
                .thenThrow(new TodoBizException(TodoErrorCodeEnum.CREATE_DISABLED));

        // Act & Assert
        mockMvc.perform(post(CREATE_URI).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(TodoErrorCodeEnum.CREATE_DISABLED.getCode()))
                .andExpect(jsonPath("$.msg").value(TodoErrorCodeEnum.CREATE_DISABLED.getMessage()));
    }

    @Test
    void should_returnTodo004_when_unexpectedExceptionOccurs() throws Exception {
        // Arrange
        when(todoTaskService.createTodoTask(any(TodoTaskCreateRequest.class)))
                .thenThrow(new IllegalStateException("unexpected"));

        // Act & Assert
        mockMvc.perform(post(CREATE_URI).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(TodoErrorCodeEnum.SAVE_FAILED.getCode()));
    }

    private TodoTaskCreateResponse buildResponse() {
        TodoTaskCreateResponse response = new TodoTaskCreateResponse();
        response.setTaskId(1000001L);
        response.setTaskName("整理季度复盘材料");
        response.setGmtCreate("2026-10-13 10:20:30");
        return response;
    }
}
