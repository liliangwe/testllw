package com.example;

/**
 * 图片生成错误码枚举，用于快速溯源与标准化沟通。
 * <p>
 * 错误码与系分方案 5.1.2 中的 IMG_001/IMG_002/IMG_003 一一对应。
 * </p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public enum ImageGenErrorCodeEnum {

    /** 输出目录不存在或不可写 */
    OUTPUT_DIR_NOT_WRITABLE("IMG_001", "输出目录不存在或不可写"),

    /** 图片编码或写盘失败 */
    ENCODE_WRITE_FAILED("IMG_002", "图片编码或写盘失败"),

    /** 参数非法 */
    INVALID_PARAMETER("IMG_003", "参数非法");

    private final String code;

    private final String message;

    ImageGenErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}