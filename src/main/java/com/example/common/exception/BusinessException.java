package com.example.common.exception;

/**
 * 业务异常。
 *
 * @author AiWork
 * @date 2026/10/20
 */
public class BusinessException extends RuntimeException {

    private final String errorCode;

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}