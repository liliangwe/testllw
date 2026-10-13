package com.antdigital.todo.common.context;

/**
 * 登录态/租户上下文（系分 5.1.3.2 F04）。
 *
 * <p>创建人与租户标识只能取自登录态，禁止由前端入参传入；实现由平台统一登录拦截器写入。</p>
 *
 * @author AiWork
 * @date 2026/10/13
 */
public interface InvokerContext {

    /**
     * 获取当前请求的创建人（登录名/工号）。
     *
     * @return 创建人标识，未获取到时返回 null
     */
    String getInvoker();

    /**
     * 获取当前请求的租户标识。
     *
     * @return 租户标识，未获取到时返回 null
     */
    String getTenantId();
}
