package com.example.todo.dto;

/**
 * 创建待办事项请求 DTO
 *
 * @author AiWork
 * @date 2026/10/13
 */
public class TodoCreateRequestDTO {

    /** 待办名称 */
    private String name;

    /** 待办描述 */
    private String description;

    /** 请求幂等键（可选），相同 requestId 的重复请求直接返回已有记录 */
    private String requestId;

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

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}