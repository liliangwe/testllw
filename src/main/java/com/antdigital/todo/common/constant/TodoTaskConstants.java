package com.antdigital.todo.common.constant;

/**
 * 待办事项字段级常量（系分 5.1.1.2）。
 *
 * @author AiWork
 * @date 2026/10/13
 */
public final class TodoTaskConstants {

    /** 事项名称最大字符长度 */
    public static final int TASK_NAME_MAX_LEN = 64;

    /** 事项描述最大字符长度 */
    public static final int TASK_DESC_MAX_LEN = 512;

    /** 逻辑删除标记：未删除 */
    public static final int IS_DELETED_NO = 0;

    /** 逻辑删除标记：已删除（本期不产生该取值，为下期删除能力预留） */
    public static final int IS_DELETED_YES = 1;

    private TodoTaskConstants() {
        throw new IllegalStateException("Utility class");
    }
}
