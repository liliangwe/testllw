package com.example.organization.integration;

/**
 * 审批流服务客户端。
 */
public interface ApprovalServiceClient {

    /**
     * 更新员工默认审批节点。
     *
     * @param employeeId 员工ID
     * @param newDeptId  目标部门ID
     */
    void updateDefaultApprover(Long employeeId, Long newDeptId);
}
