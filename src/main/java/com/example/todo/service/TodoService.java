package com.example.todo.service;

import com.example.todo.model.Todo;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * 待办事项业务服务。
 *
 * <p>使用内存 {@link ConcurrentHashMap} 存储，无持久化。</p>
 */
@Service
public class TodoService {

    private final ConcurrentHashMap<String, Todo> store = new ConcurrentHashMap<>();

    /**
     * 创建一条新的待办事项。
     *
     * @param name        事项名称（必填）
     * @param description 事项描述（可选）
     * @return 创建成功的 Todo 对象
     */
    public Todo create(String name, String description) {
        String id = UUID.randomUUID().toString();
        String createdAt = Instant.now().toString();
        Todo todo = new Todo(id, name, description, createdAt);
        store.put(id, todo);
        return todo;
    }
}