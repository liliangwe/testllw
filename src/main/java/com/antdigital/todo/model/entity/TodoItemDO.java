package com.antdigital.todo.model.entity;

import java.time.LocalDateTime;

/**
 * 待办事项数据对象，映射 todo_item 表。
 */
public class TodoItemDO {

    /** 系统自增主键 */
    private Long id;
    /** 事项名称 */
    private String todoName;
    /** 事项描述 */
    private String description;
    /** 创建时间 */
    private LocalDateTime gmtCreate;
    /** 修改时间 */
    private LocalDateTime gmtModified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTodoName() {
        return todoName;
    }

    public void setTodoName(String todoName) {
        this.todoName = todoName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public LocalDateTime getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(LocalDateTime gmtModified) {
        this.gmtModified = gmtModified;
    }

    @Override
    public String toString() {
        return "TodoItemDO{id=" + id + ", todoName='" + todoName + "', description='" + description + "'}";
    }
}