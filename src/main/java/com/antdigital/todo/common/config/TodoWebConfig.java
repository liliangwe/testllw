package com.antdigital.todo.common.config;

import com.antdigital.todo.common.context.TodoInvokerContextInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 层配置：将登录态上下文接线拦截器注册到待办事项接口路径上（系分 6.4.2.3）。
 *
 * <p>可通过 {@code todo.invoker.enabled=false} 关闭本模块拦截器（例如部署环境已由平台统一接线）。</p>
 *
 * @author AiWork
 * @date 2026/10/13
 */
@Configuration
@ConditionalOnProperty(name = "todo.invoker.enabled", havingValue = "true", matchIfMissing = true)
public class TodoWebConfig implements WebMvcConfigurer {

    /** 需要接入登录态上下文的接口路径前缀 */
    private static final String TODO_API_PATH_PATTERN = "/api/todo/**";

    private final TodoInvokerContextInterceptor todoInvokerContextInterceptor;

    public TodoWebConfig(TodoInvokerContextInterceptor todoInvokerContextInterceptor) {
        this.todoInvokerContextInterceptor = todoInvokerContextInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(todoInvokerContextInterceptor).addPathPatterns(TODO_API_PATH_PATTERN);
    }
}
