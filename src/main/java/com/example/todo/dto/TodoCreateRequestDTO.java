package com.example.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建待办事项请求 DTO
 *
 * @author AiWork
 * @date 2026/10/13
 */
public class TodoCreateRequestDTO {

    /** 待办名称 */
    @NotBlank(message = "TODO_001:事项名称不能为空")
    @Size(max = 200, message = "TODO_002:事项名称不能超过200字符")
    private String name;

    /** 待办描述 */
    @Size(max = 2000, message = "TODO_003:事项描述不能超过2000字符")
    private String description;

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
}