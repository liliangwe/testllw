package com.antdigital.todo.api.response;

import com.antdigital.todo.common.constant.TodoResultConstants;
import com.antdigital.todo.common.enums.TodoErrorCodeEnum;

/**
 * oneapi 统一响应结构（系分 5.1.2 W01 出参）。
 *
 * @param <T> 业务数据类型
 * @author AiWork
 * @date 2026/10/13
 */
public class ApiResult<T> {

    /** 结果 code，成功为 OK，失败为 TODO_xxx */
    private String result;

    /** 提示信息，成功为 SUCCESS */
    private String msg;

    /** 业务数据 */
    private T data;

    /**
     * 构造成功响应。
     *
     * @param data 业务数据
     * @param <T>  业务数据类型
     * @return 成功响应
     */
    public static <T> ApiResult<T> ok(T data) {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.setResult(TodoResultConstants.RESULT_OK);
        apiResult.setMsg(TodoResultConstants.MSG_SUCCESS);
        apiResult.setData(data);
        return apiResult;
    }

    /**
     * 构造失败响应，使用错误码默认提示。
     *
     * @param errorCode 错误码枚举
     * @param <T>       业务数据类型
     * @return 失败响应
     */
    public static <T> ApiResult<T> fail(TodoErrorCodeEnum errorCode) {
        return fail(errorCode, errorCode.getMessage());
    }

    /**
     * 构造失败响应。
     *
     * @param errorCode 错误码枚举
     * @param msg       用户可见提示
     * @param <T>       业务数据类型
     * @return 失败响应
     */
    public static <T> ApiResult<T> fail(TodoErrorCodeEnum errorCode, String msg) {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.setResult(errorCode.getCode());
        apiResult.setMsg(msg);
        apiResult.setData(null);
        return apiResult;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
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
        return "ApiResult{result='" + result + "', msg='" + msg + "', data=" + data + '}';
    }
}
