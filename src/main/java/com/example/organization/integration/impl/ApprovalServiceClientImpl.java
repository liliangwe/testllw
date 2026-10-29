package com.example.organization.integration.impl;

import com.example.organization.integration.ApprovalServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 审批流服务客户端占位实现。
 */
@Service
public class ApprovalServiceClientImpl implements ApprovalServiceClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApprovalServiceClientImpl.class);

    @Override
    public void updateDefaultApprover(Long employeeId, Long newDeptId) {
        LOGGER.info("更新默认审批节点，employeeId={}, newDeptId={}", employeeId, newDeptId);
    }
}
