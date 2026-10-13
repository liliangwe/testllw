package com.example.todo.service;

import com.example.todo.dto.TodoCreateRequestDTO;
import com.example.todo.dto.TodoCreateResultDTO;

/**
 * 待办事项服务接口
 *
 * @author AiWork
 * @date 2026/10/13
 */
public interface TodoService {

    /**
     * 创建待办事项
     *
     * @param request 创建请求
     * @return 创建结果
     */
    TodoCreateResultDTO createTodo(TodoCreateRequestDTO request);
}