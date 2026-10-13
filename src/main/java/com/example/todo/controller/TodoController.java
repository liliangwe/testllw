package com.example.todo.controller;

import com.example.todo.common.BizException;
import com.example.todo.common.Result;
import com.example.todo.dto.TodoCreateRequestDTO;
import com.example.todo.dto.TodoCreateResultDTO;
import com.example.todo.service.TodoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 待办事项控制器
 *
 * @author AiWork
 * @date 2026/10/13
 */
@RestController
@RequestMapping("/api/todo")
public class TodoController {

    private static final Logger log = LoggerFactory.getLogger(TodoController.class);

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    /**
     * 新增待办事项
     *
     * @param request 创建请求
     * @return 创建结果
     */
    @PostMapping("/create")
    public ResponseEntity<Result<TodoCreateResultDTO>> createTodo(
            @Valid @RequestBody TodoCreateRequestDTO request) {
        long start = System.currentTimeMillis();
        try {
            TodoCreateResultDTO result = todoService.createTodo(request);
            long elapsed = System.currentTimeMillis() - start;
            log.info("POST /api/todo/create OK, elapsed={}ms, id={}", elapsed, result.getId());
            return ResponseEntity.ok(Result.success(result));
        } catch (BizException e) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn("POST /api/todo/create FAIL, code={}, msg={}, elapsed={}ms",
                    e.getErrorCode(), e.getMessage(), elapsed);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.fail(e.getErrorCode(), e.getMessage()));
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("POST /api/todo/create ERROR, elapsed={}ms", elapsed, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.fail("TODO_999", "系统内部错误"));
        }
    }
}