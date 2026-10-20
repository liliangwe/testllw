package com.example.demo.common;

public class ApiResponse<T> {

    public static final String CODE_SUCCESS = "0";
    public static final String MSG_SUCCESS = "SUCCESS";

    private String code;
    private String msg;
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(CODE_SUCCESS, MSG_SUCCESS, data);
    }

    public static <T> ApiResponse<T> failure(String code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}