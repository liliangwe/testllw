package com.example.organization.integration.impl;

import com.example.organization.integration.AuthServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 权限/账号服务客户端占位实现。
 */
@Service
public class AuthServiceClientImpl implements AuthServiceClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceClientImpl.class);

    @Override
    public void revokeLoginPermission(Long employeeId) {
        LOGGER.info("撤销登录权限，employeeId={}", employeeId);
    }

    @Override
    public void releaseLicense(Long employeeId) {
        LOGGER.info("释放账号许可，employeeId={}", employeeId);
    }
}
