package com.example.organization.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 员工调动请求。
 */
public class TransferEmployeeRequest {

    @NotNull
    private Long newDeptId;

    @NotBlank
    private String newPosition;

    private String reason;

    public Long getNewDeptId() {
        return newDeptId;
    }

    public void setNewDeptId(Long newDeptId) {
        this.newDeptId = newDeptId;
    }

    public String getNewPosition() {
        return newPosition;
    }

    public void setNewPosition(String newPosition) {
        this.newPosition = newPosition;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
