package com.antdigital.todo.api.controller;

import com.antdigital.todo.api.response.ApiResult;
import com.antdigital.todo.common.constant.TodoTipConstants;
import com.antdigital.todo.common.enums.TodoErrorCodeEnum;
import com.antdigital.todo.common.exception.TodoBizException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 待办事项统一异常处理，将异常翻译为 oneapi 响应结构（系分 5.1.3.3 异常场景）。
 *
 * @author AiWork
 * @date 2026/10/13
 */
@RestControllerAdvice
public class TodoExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TodoExceptionHandler.class);

    /**
     * 业务异常：按错误码返回，WARN 留痕（系分 6.5）。
     *
     * @param exception 业务异常
     * @return 失败响应
     */
    @ExceptionHandler(TodoBizException.class)
    public ApiResult<Void> handleTodoBizException(TodoBizException exception) {
        LOGGER.warn("todo task create biz rejected, errorCode:{}, msg:{}",
                exception.getErrorCode(), exception.getMessage());
        return ApiResult.fail(TodoErrorCodeEnum.getByCode(exception.getErrorCode()), exception.getMessage());
    }

    /**
     * 声明式参数校验失败：返回 TODO_001 并附带具体字段提示。
     *
     * @param exception 参数校验异常
     * @return 失败响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String msg = fieldError == null ? TodoTipConstants.REQUEST_REQUIRED : fieldError.getDefaultMessage();
        LOGGER.warn("todo task create param invalid, msg:{}", msg);
        return ApiResult.fail(TodoErrorCodeEnum.PARAM_INVALID, msg);
    }

    /**
     * 请求体非法或字段类型不匹配：按参数错误语义返回。
     *
     * @param exception 报文解析异常
     * @return 失败响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResult<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        LOGGER.warn("todo task create body unreadable, errorMessage:{}", exception.getMessage());
        return ApiResult.fail(TodoErrorCodeEnum.PARAM_INVALID, TodoTipConstants.REQUEST_REQUIRED);
    }

    /**
     * 方法级校验失败（如 Service 入参约束）：返回 TODO_001。
     *
     * @param exception 约束校验异常
     * @return 失败响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResult<Void> handleConstraintViolation(ConstraintViolationException exception) {
        LOGGER.warn("todo task create constraint violation, msg:{}", exception.getMessage());
        return ApiResult.fail(TodoErrorCodeEnum.PARAM_INVALID, TodoTipConstants.REQUEST_REQUIRED);
    }

    /**
     * 请求方式不支持：协议层错误按参数错误语义返回 TODO_001，与系统异常 TODO_004 区分
     * （同一字段不得兼表「请求错误」与「系统异常」，G10.1）。
     *
     * @param exception 请求方式异常
     * @return 失败响应
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResult<Void> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        LOGGER.warn("todo task create method not supported, method:{}, errorMessage:{}",
                exception.getMethod(), exception.getMessage());
        return ApiResult.fail(TodoErrorCodeEnum.PARAM_INVALID, TodoTipConstants.REQUEST_METHOD_UNSUPPORTED);
    }

    /**
     * 请求内容类型不支持：协议层错误按参数错误语义返回 TODO_001，与系统异常 TODO_004 区分（G10.1）。
     *
     * @param exception 内容类型异常
     * @return 失败响应
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ApiResult<Void> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException exception) {
        LOGGER.warn("todo task create media type not supported, contentType:{}, errorMessage:{}",
                exception.getContentType(), exception.getMessage());
        return ApiResult.fail(TodoErrorCodeEnum.PARAM_INVALID, TodoTipConstants.REQUEST_CONTENT_TYPE_UNSUPPORTED);
    }

    /**
     * 数据访问异常：返回 TODO_004，ERROR 留痕并告警（系分 6.3/6.5）。
     *
     * @param exception 数据访问异常
     * @return 失败响应
     */
    @ExceptionHandler(DataAccessException.class)
    public ApiResult<Void> handleDataAccessException(DataAccessException exception) {
        LOGGER.error("todo task create data access failed, errorMessage:{}", exception.getMessage(), exception);
        return ApiResult.fail(TodoErrorCodeEnum.SAVE_FAILED);
    }

    /**
     * 兜底异常：仅收敛系统侧未知异常（协议层异常已由更具体的处理器匹配），
     * 避免异常栈直接暴露给调用方，统一返回 TODO_004。
     *
     * @param exception 未知异常
     * @return 失败响应
     */
    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleException(Exception exception) {
        LOGGER.error("todo task create unexpected error, errorMessage:{}", exception.getMessage(), exception);
        return ApiResult.fail(TodoErrorCodeEnum.SAVE_FAILED);
    }
}
