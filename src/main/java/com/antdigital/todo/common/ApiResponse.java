package com.antdigital.todo.common;

/**
 * 统一 API 响应体，遵循 oneapi 规范。
 *
 * @param <T> data 泛型
 */
public class ApiResponse<T> {

    /** 结果码，"OK" 表示成功 */
    private String result;
    /** 提示信息 */
    private String msg;
    /** 业务数据 */
    private T data;

    private ApiResponse() {
    }

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.result = "OK";
        response.msg = "SUCCESS";
        response.data = data;
        return response;
    }

    public static <T> ApiResponse<T> fail(String result, String msg) {
        ApiResponse<T> response = new ApiResponse<>();
        response.result = result;
        response.msg = msg;
        response.data = null;
        return response;
    }

    public String getResult() {
        return result;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    @Override
    public String toString() {
        return "ApiResponse{result='" + result + "', msg='" + msg + "', data=" + data + '}';
    }
}