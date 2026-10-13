package com.antdigital.todo.dao.entity;

import java.time.LocalDateTime;

/**
 * 待办事项数据对象，对应表 todo_task（系分 5.1.1.1）。
 *
 * @author AiWork
 * @date 2026/10/13
 */
public class TodoTaskDO {

    /** 主键ID，待办事项唯一标识 */
    private Long id;

    /** 租户标识，用于租户隔离 */
    private String tenantId;

    /** 事项名称，必填 */
    private String taskName;

    /** 事项描述，选填，空时存空串 */
    private String taskDesc;

    /** 创建人登录名/工号，取自登录态 */
    private String creator;

    /** 逻辑删除标记：0-未删除，1-已删除 */
    private Integer isDeleted;

    /** 创建时间，代码侧维护 */
    private LocalDateTime gmtCreate;

    /** 修改时间，代码侧维护（创建时与创建时间一致） */
    private LocalDateTime gmtModified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDesc() {
        return taskDesc;
    }

    public void setTaskDesc(String taskDesc) {
        this.taskDesc = taskDesc;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
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
        // 系分 6.4.3.2：描述为自由文本，日志只打印长度，不打印全文
        return "TodoTaskDO{id=" + id + ", tenantId='" + tenantId + "', taskName='" + taskName
                + "', taskDescLength=" + (taskDesc == null ? 0 : taskDesc.length())
                + ", creator='" + creator + "', isDeleted=" + isDeleted
                + ", gmtCreate=" + gmtCreate + ", gmtModified=" + gmtModified + '}';
    }
}
