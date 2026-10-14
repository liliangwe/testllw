package com.example.todo.common.exception;

/**
 * 业务异常，用于承载有业务含义的失败场景（参数校验、租户信息缺失、写入失败等）
 *
 * @author AiWork
 * @date 2026/10/14
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private final String errorCode;

    /**
     * 构造业务异常
     *
     * @param errorCode 错误码
     * @param message   提示信息
     */
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "BusinessException{errorCode='" + errorCode + "', message='" + getMessage() + "'}";
    }
}
