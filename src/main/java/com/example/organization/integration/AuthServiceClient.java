package com.example.organization.integration;

/**
 * 权限/账号服务客户端。
 */
public interface AuthServiceClient {

    /**
     * 撤销登录权限。
     *
     * @param employeeId 员工ID
     */
    void revokeLoginPermission(Long employeeId);

    /**
     * 释放账号许可。
     *
     * @param employeeId 员工ID
     */
    void releaseLicense(Long employeeId);
}
