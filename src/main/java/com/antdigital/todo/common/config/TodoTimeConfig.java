package com.antdigital.todo.common.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 待办事项时间相关 Bean 配置，统一时钟来源，便于单元测试注入固定时钟。
 *
 * <p>时区显式指定（默认 {@code Asia/Shanghai}，与数据源 {@code serverTimezone} 保持一致），
 * 不依赖容器默认时区，避免应用侧 {@code gmt_create}/{@code gmt_modified} 与数据库时间口径漂移
 * （M016/G14.3）。</p>
 *
 * @author AiWork
 * @date 2026/10/13
 */
@Configuration
public class TodoTimeConfig {

    /** 默认时区，与数据源 serverTimezone 保持一致 */
    private static final String DEFAULT_ZONE_ID = "Asia/Shanghai";

    /**
     * 系统时钟，显式指定时区，与数据库 CURRENT_TIMESTAMP 口径保持一致。
     *
     * @param zoneId 时区标识（配置项 {@code todo.time.zone}）
     * @return 指定时区的系统时钟
     */
    @Bean
    public Clock clock(@Value("${todo.time.zone:" + DEFAULT_ZONE_ID + "}") String zoneId) {
        return Clock.system(ZoneId.of(zoneId));
    }
}
