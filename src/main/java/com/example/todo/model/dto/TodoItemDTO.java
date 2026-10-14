package com.example.todo.model.dto;

/**
 * 待办事项数据传输对象，作为新增待办事项接口的出参
 *
 * @author AiWork
 * @date 2026/10/14
 */
public class TodoItemDTO {

    /** 待办事项 ID */
    private Long id;

    /** 事项名称 */
    private String name;

    /** 事项描述 */
    private String description;

    /** 创建时间，格式 yyyy-MM-dd HH:mm:ss */
    private String gmtCreate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        return "TodoItemDTO{id=" + id + ", name='" + name + "', description='" + description + "', gmtCreate='"
                + gmtCreate + "'}";
    }
}
