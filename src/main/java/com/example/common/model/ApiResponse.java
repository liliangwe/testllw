package com.example.common.model;

/**
 * 统一 API 响应封装。
 *
 * @param <T> 响应数据类型
 * @author AiWork
 * @date 2026/10/20
 */
public class ApiResponse<T> {

    private Integer code;
    private String message;
    private T data;

    private ApiResponse() {
    }

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = 200;
        response.message = "success";
        response.data = data;
        return response;
    }

    public static <T> ApiResponse<T> error(Integer code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = code;
        response.message = message;
        response.data = null;
        return response;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}