package com.example.todo.common.exception;

import com.example.todo.common.ApiResponse;
import com.example.todo.common.constant.TodoErrorCode;
import com.example.todo.model.request.CreateTodoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，将异常统一转换为 {@link ApiResponse} 出参结构
 *
 * <p>HTTP 状态码约定：业务规则类异常返回 200，由响应体错误码承载语义；系统级异常（TODO_500）
 * 返回 500，便于监控告警（详见系分方案 7.1 可监控）。</p>
 *
 * @author AiWork
 * @date 2026/10/14
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        if (TodoErrorCode.SYSTEM_ERROR.equals(e.getErrorCode())) {
            LOGGER.error("业务处理失败, errorCode:{}, errorMessage:{}", e.getErrorCode(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
        }

        LOGGER.warn("业务校验失败, errorCode:{}, errorMessage:{}", e.getErrorCode(), e.getMessage());
        return ResponseEntity.ok(ApiResponse.error(e.getErrorCode(), e.getMessage()));
    }

    /**
     * 处理请求体参数校验异常
     *
     * @param e 参数校验异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String field = fieldError == null ? "" : fieldError.getField();
        String message = fieldError == null ? TodoErrorCode.MSG_PARAM_INVALID : fieldError.getDefaultMessage();

        LOGGER.warn("请求参数校验失败, field:{}, errorMessage:{}", field, message);
        return ResponseEntity.ok(ApiResponse.error(resolveValidationErrorCode(field), message));
    }

    /**
     * 处理数据库访问异常，如连接失败、写入失败
     *
     * @param e 数据库访问异常
     * @return 错误响应
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccessException(DataAccessException e) {
        LOGGER.error("数据库访问异常, errorMessage:{}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(TodoErrorCode.SYSTEM_ERROR, TodoErrorCode.MSG_SYSTEM_ERROR));
    }

    /**
     * 兜底处理未预期的异常
     *
     * @param e 未预期异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        LOGGER.error("系统内部错误, errorMessage:{}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(TodoErrorCode.SYSTEM_ERROR, TodoErrorCode.MSG_SYSTEM_ERROR));
    }

    /**
     * 将校验失败的字段映射为错误码
     *
     * <p>事项名称（name）校验失败映射 TODO_001，其余字段（当前仅事项描述 description）
     * 校验失败映射 TODO_002。</p>
     *
     * @param field 校验失败的字段名
     * @return 错误码
     */
    private String resolveValidationErrorCode(String field) {
        return CreateTodoRequest.FIELD_NAME.equals(field)
                ? TodoErrorCode.PARAM_NAME_INVALID
                : TodoErrorCode.PARAM_DESCRIPTION_INVALID;
    }
}
