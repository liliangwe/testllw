package com.antdigital.todo.controller;

import com.antdigital.todo.common.ApiResponse;
import com.antdigital.todo.model.dto.TodoCreateRequest;
import com.antdigital.todo.model.dto.TodoItemVO;
import com.antdigital.todo.service.TodoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 待办事项 REST 控制器。
 */
@RestController
@RequestMapping("/api/todo")
public class TodoController {

    private static final Logger logger = LoggerFactory.getLogger(TodoController.class);

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    /**
     * 新增待办事项。
     *
     * @param request 新增请求
     * @return 统一响应体
     */
    @PostMapping("/create")
    public ApiResponse<TodoItemVO> create(@Valid @RequestBody TodoCreateRequest request) {
        logger.info("收到新增待办事项请求, todoName: {}", request.getTodoName());

        try {
            TodoItemVO vo = todoService.createTodo(request);
            return ApiResponse.ok(vo);
        } catch (IllegalArgumentException e) {
            logger.warn("参数校验失败: {}", e.getMessage());
            return ApiResponse.fail("PARAM_INVALID", e.getMessage());
        } catch (Exception e) {
            logger.error("创建待办事项失败", e);
            return ApiResponse.fail("SYSTEM_ERROR", "系统内部错误");
        }
    }
}