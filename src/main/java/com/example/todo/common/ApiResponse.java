package com.example.todo.common;

import com.example.todo.common.constant.TodoErrorCode;

/**
 * 统一出参结构，所有接口响应均使用该结构包装
 *
 * @param <T> 业务数据类型
 * @author AiWork
 * @date 2026/10/14
 */
public class ApiResponse<T> {

    /** 结果码，SUCCESS 表示成功 */
    private String code;

    /** 提示信息 */
    private String msg;

    /** 业务数据，可为 null */
    private T data;

    private ApiResponse(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 构造成功响应
     *
     * @param data 业务数据
     * @param <T>  业务数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(TodoErrorCode.SUCCESS, TodoErrorCode.SUCCESS, data);
    }

    /**
     * 构造失败响应，业务数据固定为 null
     *
     * @param code 错误码
     * @param msg  提示信息
     * @param <T>  业务数据类型
     * @return 失败响应
     */
    public static <T> ApiResponse<T> error(String code, String msg) {
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

    @Override
    public String toString() {
        return "ApiResponse{code='" + code + "', msg='" + msg + "', data=" + data + '}';
    }
}
