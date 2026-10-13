package com.antdigital.todo.common.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 待办事项时间相关 Bean 配置，统一时钟来源，便于单元测试注入固定时钟。
 *
 * @author AiWork
 * @date 2026/10/13
 */
@Configuration
public class TodoTimeConfig {

    /**
     * 系统时钟，使用默认时区，与数据库 CURRENT_TIMESTAMP 口径保持一致。
     *
     * @return 系统默认时钟
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
