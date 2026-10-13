package com.example.todo.common;

/**
 * 统一响应结构
 *
 * @param <T> 数据类型
 * @author AiWork
 * @date 2026/10/13
 */
public class Result<T> {

    /** 结果码 */
    private String code;

    /** 提示信息 */
    private String msg;

    /** 业务数据 */
    private T data;

    private Result() {
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = "OK";
        result.msg = "SUCCESS";
        result.data = data;
        return result;
    }

    public static <T> Result<T> fail(String code, String msg) {
        Result<T> result = new Result<>();
        result.code = code;
        result.msg = msg;
        result.data = null;
        return result;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }
}