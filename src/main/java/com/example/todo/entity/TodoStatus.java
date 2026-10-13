package com.example.todo.entity;

/**
 * 待办事项状态枚举
 *
 * @author AiWork
 * @date 2026/10/13
 */
public enum TodoStatus {

    /** 待处理 */
    PENDING("PENDING", "待处理"),

    /** 已完成 */
    COMPLETED("COMPLETED", "已完成");

    private final String code;
    private final String desc;

    TodoStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据状态码获取枚举值
     *
     * @param code 状态码
     * @return 对应的枚举值
     * @throws IllegalArgumentException 状态码无效时抛出
     */
    public static TodoStatus fromCode(String code) {
        for (TodoStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的待办状态码: " + code);
    }
}