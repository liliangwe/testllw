package com.example.organization.model.request;

import jakarta.validation.constraints.NotNull;

/**
 * 部门移动请求。
 */
public class MoveDepartmentRequest {

    @NotNull
    private Long newParentId;

    public Long getNewParentId() {
        return newParentId;
    }

    public void setNewParentId(Long newParentId) {
        this.newParentId = newParentId;
    }
}
