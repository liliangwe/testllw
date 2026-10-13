package com.antdigital.todo.common.context;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录态上下文接线拦截器（系分 6.4.2.3 登录态检查、5.1.3.2 R04/R05）。
 *
 * <p>平台全局登录拦截器完成登录票据校验后，把创建人与租户标识写入请求属性；
 * 本拦截器在请求进入时将其接入 {@link ThreadLocalInvokerContext}，并在请求结束
 * （成功或异常均会执行 {@code afterCompletion}，等价于 {@code finally}）时清理上下文，
 * 避免线程池复用导致身份/租户串号（G8.5）。</p>
 *
 * <p>本拦截器不做认证判定：认证仍由平台统一登录方案承担。身份属性缺失时不写入任何身份，
 * 由 Service 按 R04/R05 快速失败返回 {@code TODO_002}/{@code TODO_003}（fail-closed），
 * 不会因接线缺失而放通。身份来源为服务端请求属性，不接受前端入参传入（R04/R05）。</p>
 *
 * @author AiWork
 * @date 2026/10/13
 */
@Component
public class TodoInvokerContextInterceptor implements HandlerInterceptor {

    /** 默认的创建人请求属性名，可按平台约定覆盖（配置项 todo.invoker.invoker-attribute） */
    public static final String DEFAULT_INVOKER_ATTRIBUTE = "todo.invoker";

    /** 默认的租户标识请求属性名，可按平台约定覆盖（配置项 todo.invoker.tenant-attribute） */
    public static final String DEFAULT_TENANT_ATTRIBUTE = "todo.tenant-id";

    private final String invokerAttribute;

    private final String tenantAttribute;

    public TodoInvokerContextInterceptor(
            @Value("${todo.invoker.invoker-attribute:" + DEFAULT_INVOKER_ATTRIBUTE + "}") String invokerAttribute,
            @Value("${todo.invoker.tenant-attribute:" + DEFAULT_TENANT_ATTRIBUTE + "}") String tenantAttribute) {
        this.invokerAttribute = invokerAttribute;
        this.tenantAttribute = tenantAttribute;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String invoker = resolveAttribute(request, invokerAttribute);
        String tenantId = resolveAttribute(request, tenantAttribute);
        if (invoker != null || tenantId != null) {
            ThreadLocalInvokerContext.set(invoker, tenantId);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception exception) {
        // 等价于 finally：请求线程归还线程池前必须清理上下文（G8.5）
        ThreadLocalInvokerContext.clear();
    }

    /**
     * 读取请求属性中的身份值，非字符串或空白按缺失处理。
     *
     * @param request       当前请求
     * @param attributeName 属性名
     * @return 去空白后的身份值，缺失时返回 null
     */
    private static String resolveAttribute(HttpServletRequest request, String attributeName) {
        Object value = request.getAttribute(attributeName);
        if (value instanceof String attributeValue && !attributeValue.strip().isEmpty()) {
            return attributeValue.strip();
        }
        return null;
    }
}
