package com.example.todo.service;

import com.example.todo.model.dto.TodoItemDTO;
import com.example.todo.model.request.CreateTodoRequest;

/**
 * 待办事项业务服务
 *
 * @author AiWork
 * @date 2026/10/14
 */
public interface TodoService {

    /**
     * 新增待办事项
     *
     * @param request  新增待办事项请求，包含事项名称与事项描述
     * @param tenantId 租户 ID，来自登录上下文，为空时抛出 TODO_003
     * @return 创建成功的待办事项，包含 ID、事项名称、事项描述与创建时间
     */
    TodoItemDTO createTodo(CreateTodoRequest request, String tenantId);
}
