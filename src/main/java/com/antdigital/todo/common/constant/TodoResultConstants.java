package com.antdigital.todo.common.constant;

/**
 * oneapi 响应结果常量（系分 5.1.2 出参）。
 *
 * @author AiWork
 * @date 2026/10/13
 */
public final class TodoResultConstants {

    /** 成功结果码 */
    public static final String RESULT_OK = "OK";

    /** 成功提示信息 */
    public static final String MSG_SUCCESS = "SUCCESS";

    private TodoResultConstants() {
        throw new IllegalStateException("Utility class");
    }
}
