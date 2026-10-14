package com.antdigital.todo.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 新增待办事项请求 DTO。
 */
public class TodoCreateRequest {

    /** 事项名称，1-200 字符 */
    @NotBlank(message = "事项名称不能为空")
    @Size(max = 200, message = "事项名称不能超过200字符")
    private String todoName;

    /** 事项描述，0-2000 字符 */
    @Size(max = 2000, message = "事项描述不能超过2000字符")
    private String description;

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

    @Override
    public String toString() {
        return "TodoCreateRequest{todoName='" + todoName + "', description='" + description + "'}";
    }
}