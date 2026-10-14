package com.example.todo.controller;

import com.example.todo.common.ApiResponse;
import com.example.todo.model.dto.TodoItemDTO;
import com.example.todo.model.request.CreateTodoRequest;
import com.example.todo.service.TodoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 待办事项控制器，对外提供 oneapi 接口（/api 前缀）
 *
 * @author AiWork
 * @date 2026/10/14
 */
@RestController
@RequestMapping("/api/todo")
public class TodoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TodoController.class);

    /**
     * 租户 ID 请求头
     *
     * <p>登录态与租户信息由统一认证拦截器写入登录上下文后透传，本期不实现认证（系分假设 A01），
     * 缺少该请求头时返回 TODO_003。</p>
     */
    private static final String TENANT_ID_HEADER = "X-Tenant-Id";

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    /**
     * 新增待办事项（W01）
     *
     * @param request  新增待办事项请求，包含事项名称与事项描述
     * @param tenantId 租户 ID，由登录上下文透传
     * @return 创建成功的待办事项
     */
    @PostMapping
    public ApiResponse<TodoItemDTO> createTodo(@Valid @RequestBody CreateTodoRequest request,
            @RequestHeader(name = TENANT_ID_HEADER, required = false) String tenantId) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("新增待办事项请求, name:{}, tenantId:{}", request.getName(), tenantId);
        }

        TodoItemDTO todoItemDTO = todoService.createTodo(request, tenantId);
        return ApiResponse.success(todoItemDTO);
    }
}
