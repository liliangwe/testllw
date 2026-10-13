package com.antdigital.todo.common.context;

import org.springframework.stereotype.Component;

/**
 * 基于 ThreadLocal 的登录态/租户上下文实现。
 *
 * <p>平台统一登录拦截器在请求进入时调用 {@link #set(String, String)} 写入身份，
 * 请求结束时调用 {@link #clear()} 清理，避免线程复用造成串号。</p>
 *
 * @author AiWork
 * @date 2026/10/13
 */
@Component
public class ThreadLocalInvokerContext implements InvokerContext {

    /** 创建人上下文 */
    private static final ThreadLocal<String> INVOKER_HOLDER = new ThreadLocal<>();

    /** 租户上下文 */
    private static final ThreadLocal<String> TENANT_HOLDER = new ThreadLocal<>();

    /**
     * 写入当前请求的登录态与租户信息。
     *
     * @param invoker  创建人登录名/工号
     * @param tenantId 租户标识
     */
    public static void set(String invoker, String tenantId) {
        INVOKER_HOLDER.set(invoker);
        TENANT_HOLDER.set(tenantId);
    }

    /**
     * 清理当前线程上下文。
     */
    public static void clear() {
        INVOKER_HOLDER.remove();
        TENANT_HOLDER.remove();
    }

    @Override
    public String getInvoker() {
        return INVOKER_HOLDER.get();
    }

    @Override
    public String getTenantId() {
        return TENANT_HOLDER.get();
    }
}
