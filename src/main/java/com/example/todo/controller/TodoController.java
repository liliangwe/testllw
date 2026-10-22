package com.example.todo.controller;

import com.example.todo.model.Todo;
import com.example.todo.service.TodoService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 待办事项 REST 控制器。
 */
@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    /**
     * 创建新的待办事项。
     *
     * @param body 请求体，包含 name（必填）和 description（可选）
     * @return 201 Created + 创建的 Todo JSON；name 为空时 400 Bad Request
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "name is required"));
        }
        String description = body.getOrDefault("description", "");
        Todo todo = todoService.create(name, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(todo);
    }
}