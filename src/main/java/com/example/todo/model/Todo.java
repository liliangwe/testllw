package com.example.todo.model;

/**
 * 待办事项数据模型。
 *
 * <p>不可变对象：创建后字段不可修改。</p>
 */
public class Todo {

    private final String id;
    private final String name;
    private final String description;
    private final String createdAt;

    public Todo(String id, String name, String description, String createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}