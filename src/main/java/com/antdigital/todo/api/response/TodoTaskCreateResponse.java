package com.antdigital.todo.api.response;

/**
 * 新增待办事项出参（系分 5.1.2 W01 出参 data）。
 *
 * @author AiWork
 * @date 2026/10/13
 */
public class TodoTaskCreateResponse {

    /** 新建待办事项 ID */
    private Long taskId;

    /** 落库后的事项名称（去空白后） */
    private String taskName;

    /** 创建时间，格式 yyyy-MM-dd HH:mm:ss */
    private String gmtCreate;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(String gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    @Override
    public String toString() {
        return "TodoTaskCreateResponse{taskId=" + taskId + ", taskName='" + taskName
                + "', gmtCreate='" + gmtCreate + "'}";
    }
}
