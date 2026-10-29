package com.example.organization.model.entity;

import java.time.LocalDateTime;

/**
 * 调动记录实体。
 */
public class TransferRecordDO extends BaseDO {

    private Long employeeId;
    private Long oldDeptId;
    private Long newDeptId;
    private String newPosition;
    private String reason;
    private Long operatorId;
    private LocalDateTime transferTime;

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Long getOldDeptId() {
        return oldDeptId;
    }

    public void setOldDeptId(Long oldDeptId) {
        this.oldDeptId = oldDeptId;
    }

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

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public LocalDateTime getTransferTime() {
        return transferTime;
    }

    public void setTransferTime(LocalDateTime transferTime) {
        this.transferTime = transferTime;
    }
}
