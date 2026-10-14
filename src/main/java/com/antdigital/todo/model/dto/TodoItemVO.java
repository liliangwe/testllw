package com.antdigital.todo.model.dto;

/**
 * 待办事项视图对象，用于接口返回。
 */
public class TodoItemVO {

    /** 待办事项 ID */
    private Long id;
    /** 事项名称 */
    private String todoName;
    /** 事项描述 */
    private String description;
    /** 创建时间 (yyyy-MM-dd HH:mm:ss) */
    private String gmtCreate;

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

    public String getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(String gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    @Override
    public String toString() {
        return "TodoItemVO{id=" + id + ", todoName='" + todoName + "', description='" + description + "'}";
    }
}