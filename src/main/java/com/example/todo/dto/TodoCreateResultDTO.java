package com.example.todo.dto;

/**
 * 创建待办事项结果 DTO
 *
 * @author AiWork
 * @date 2026/10/13
 */
public class TodoCreateResultDTO {

    /** 待办事项ID */
    private Long id;

    /** 待办名称 */
    private String name;

    /** 待办描述 */
    private String description;

    /** 状态 */
    private String status;

    /** 创建时间 */
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(String gmtCreate) {
        this.gmtCreate = gmtCreate;
    }
}