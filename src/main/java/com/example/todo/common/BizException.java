package com.example.todo.common;

/**
 * 业务异常
 *
 * @author AiWork
 * @date 2026/10/13
 */
public class BizException extends RuntimeException {

    private final String errorCode;

    public BizException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}