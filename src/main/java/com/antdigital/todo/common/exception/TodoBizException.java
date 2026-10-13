package com.antdigital.todo.common.exception;

import com.antdigital.todo.common.enums.TodoErrorCodeEnum;

/**
 * 待办事项业务异常（unchecked）。
 *
 * @author AiWork
 * @date 2026/10/13
 */
public class TodoBizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 业务错误码，取自 {@link TodoErrorCodeEnum} */
    private final String errorCode;

    /**
     * 使用错误码默认提示构造异常。
     *
     * @param errorCodeEnum 错误码枚举
     */
    public TodoBizException(TodoErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum.getMessage());
        this.errorCode = errorCodeEnum.getCode();
    }

    /**
     * 使用自定义提示构造异常。
     *
     * @param errorCodeEnum 错误码枚举
     * @param message       用户可见提示
     */
    public TodoBizException(TodoErrorCodeEnum errorCodeEnum, String message) {
        super(message);
        this.errorCode = errorCodeEnum.getCode();
    }

    /**
     * 使用自定义提示与根因构造异常。
     *
     * @param errorCodeEnum 错误码枚举
     * @param message       用户可见提示
     * @param cause         根因异常
     */
    public TodoBizException(TodoErrorCodeEnum errorCodeEnum, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCodeEnum.getCode();
    }

    /**
     * 获取业务错误码。
     *
     * @return 错误码，形如 TODO_001
     */
    public String getErrorCode() {
        return errorCode;
    }
}
