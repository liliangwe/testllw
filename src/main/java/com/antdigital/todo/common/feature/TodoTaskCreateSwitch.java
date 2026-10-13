package com.antdigital.todo.common.feature;

/**
 * 待办事项创建应急开关（系分 R07 / 7.3）。
 *
 * @author AiWork
 * @date 2026/10/13
 */
public interface TodoTaskCreateSwitch {

    /**
     * 判断指定租户当前是否允许创建待办事项。
     *
     * @param tenantId 租户标识
     * @return true 允许创建；false 拒绝创建（返回 TODO_005）
     */
    boolean isEnabled(String tenantId);
}
