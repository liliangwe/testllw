package com.antdigital.todo.common.enums;

/**
 * 待办事项错误码枚举（系分 5.1.2 错误码表）。
 *
 * @author AiWork
 * @date 2026/10/13
 */
public enum TodoErrorCodeEnum {

    /** 参数校验失败（名称为空/超长、描述超长） */
    PARAM_INVALID("TODO_001", "请求参数不合法"),

    /** 未获取到登录用户，登录态失效 */
    INVOKER_MISSING("TODO_002", "登录态失效，请重新登录"),

    /** 未获取到租户信息 */
    TENANT_MISSING("TODO_003", "未获取到租户信息"),

    /** 系统繁忙，待办事项保存失败 */
    SAVE_FAILED("TODO_004", "系统繁忙，请稍后重试"),

    /** 创建功能已关闭（应急开关关闭） */
    CREATE_DISABLED("TODO_005", "创建功能已临时关闭");

    private final String code;

    private final String message;

    TodoErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取错误码。
     *
     * @return 错误码，形如 TODO_001
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取用户可见的提示信息。
     *
     * @return 提示信息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 根据错误码获取枚举，未知错误码直接抛出异常，避免静默兜底。
     *
     * @param code 错误码
     * @return 匹配的枚举
     * @throws IllegalArgumentException 错误码为 null 或未定义时抛出
     */
    public static TodoErrorCodeEnum getByCode(String code) {
        for (TodoErrorCodeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        throw new IllegalArgumentException("unknown todo error code: " + code);
    }
}
