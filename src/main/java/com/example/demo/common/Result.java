package com.example.demo.common;

/**
 * 通用出参结构：{code, msg, data}。
 *
 * @param <T> 数据类型
 */
public record Result<T>(String code, String msg, T data) {

    public static final String CODE_OK = "OK";

    public static <T> Result<T> success(T data) {
        return new Result<>(CODE_OK, "SUCCESS", data);
    }

    public static <T> Result<T> fail(String code, String msg) {
        return new Result<>(code, msg, null);
    }
}
