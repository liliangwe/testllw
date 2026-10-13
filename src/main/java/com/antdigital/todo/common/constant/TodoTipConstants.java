package com.antdigital.todo.common.constant;

/**
 * 待办事项用户提示常量（系分 R01/R02 提示文案）。
 *
 * @author AiWork
 * @date 2026/10/13
 */
public final class TodoTipConstants {

    /** 事项名称提示：必填且不超过 64 字符 */
    public static final String TASK_NAME_INVALID = "事项名称不能为空且不超过 64 字符";

    /** 事项描述提示：不超过 512 字符 */
    public static final String TASK_DESC_TOO_LONG = "事项描述不能超过 512 字符";

    /** 请求体缺失提示 */
    public static final String REQUEST_REQUIRED = "请求参数不能为空";

    private TodoTipConstants() {
        throw new IllegalStateException("Utility class");
    }
}
