package com.example.demo.config;

import com.example.demo.track.TrackInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置：注册埋点拦截器、异步线程池、跨域。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TrackInterceptor trackInterceptor;

    public WebConfig(TrackInterceptor trackInterceptor) {
        this.trackInterceptor = trackInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(trackInterceptor).addPathPatterns("/api/demo/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST")
                .allowedHeaders("*");
    }

    /**
     * 埋点异步线程池：核心 2 / 最大 4 / 队列 500，CallerRuns 退化同步。
     */
    @Bean("trackTaskExecutor")
    public TaskExecutor trackTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("track-async-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
