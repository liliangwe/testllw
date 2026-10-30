package com.example.demo.common;

/**
 * 业务异常，携带错误码（格式 {MODULE}_{SEQ}）。
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
