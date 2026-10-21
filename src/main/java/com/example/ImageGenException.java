package com.example;

/**
 * 图片生成业务异常，携带错误码便于调用方快速定位问题。
 * <p>
 * 遵循「使用有业务含义的自定义异常」规范，不直接抛出 RuntimeException。
 * </p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class ImageGenException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ImageGenErrorCodeEnum errorCode;

    public ImageGenException(ImageGenErrorCodeEnum errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ImageGenException(ImageGenErrorCodeEnum errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ImageGenErrorCodeEnum getErrorCode() {
        return errorCode;
    }

    public String getCode() {
        return errorCode.getCode();
    }
}