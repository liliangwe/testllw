package com.example.demo.common;

/**
 * 业务异常：携带业务错误码，由 GlobalExceptionHandler 统一转换为 {code,msg,data} 失败响应。
 */
public class BizException extends RuntimeException {

    private final String code;

    public BizException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}