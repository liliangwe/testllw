package com.antdigital.todo.api.controller;

import com.antdigital.todo.api.request.TodoTaskCreateRequest;
import com.antdigital.todo.api.response.ApiResult;
import com.antdigital.todo.api.response.TodoTaskCreateResponse;
import com.antdigital.todo.service.TodoTaskService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 待办事项控制器（系分 4.1 oneapi W01）。
 *
 * @author AiWork
 * @date 2026/10/13
 */
@RestController
@RequestMapping("/api/todo/task")
public class TodoTaskController {

    private final TodoTaskService todoTaskService;

    public TodoTaskController(TodoTaskService todoTaskService) {
        this.todoTaskService = todoTaskService;
    }

    /**
     * 新增待办事项（W01）。
     *
     * <p>入参校验（R01/R02）由 {@link Valid} 声明式完成，Service 侧再做兜底（R10）；
     * 创建人与租户标识取自登录态（R04/R05），不接受前端传入。</p>
     *
     * @param request 新增待办事项请求
     * @return oneapi 统一响应，data 为新建待办事项结果视图
     */
    @PostMapping("/create")
    public ApiResult<TodoTaskCreateResponse> createTodoTask(@Valid @RequestBody TodoTaskCreateRequest request) {
        return ApiResult.ok(todoTaskService.createTodoTask(request));
    }
}
