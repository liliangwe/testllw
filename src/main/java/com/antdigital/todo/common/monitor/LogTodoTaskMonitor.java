package com.antdigital.todo.common.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 基于日志落地的埋点实现，后续可替换为平台监控 SDK 上报（系分 6.5）。
 *
 * @author AiWork
 * @date 2026/10/13
 */
@Component
public class LogTodoTaskMonitor implements TodoTaskMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogTodoTaskMonitor.class);

    /** 埋点名称：新增待办事项（系分 7.1） */
    private static final String MONITOR_NAME = "todo.task.create";

    @Override
    public void reportCreate(String tenantId, String result, String errorCode, long costMillis) {
        LOGGER.info("monitor[{}], tenantId:{}, result:{}, errorCode:{}, costMillis:{}",
                MONITOR_NAME, tenantId, result, errorCode, costMillis);
    }
}
