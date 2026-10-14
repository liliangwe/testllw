package com.antdigital.todo.service;

import com.antdigital.todo.model.dto.TodoCreateRequest;
import com.antdigital.todo.model.dto.TodoItemVO;

/**
 * 待办事项业务服务接口。
 */
public interface TodoService {

    /**
     * 新增待办事项。
     *
     * @param request 新增请求
     * @return 创建成功的待办事项视图
     * @throws IllegalArgumentException 参数校验失败时抛出
     */
    TodoItemVO createTodo(TodoCreateRequest request);
}