package com.antdigital.todo.common.monitor;

/**
 * 待办事项埋点上报（系分 F06 / 7.1 / 6.5）。
 *
 * @author AiWork
 * @date 2026/10/13
 */
public interface TodoTaskMonitor {

    /**
     * 上报新增待办事项调用埋点，埋点名称 todo.task.create。
     *
     * @param tenantId   租户标识，用于租户维度统计
     * @param result     处理结果，成功为 OK，失败为具体错误码
     * @param errorCode  失败错误码，成功时为 null
     * @param costMillis 处理耗时（毫秒）
     */
    void reportCreate(String tenantId, String result, String errorCode, long costMillis);
}
