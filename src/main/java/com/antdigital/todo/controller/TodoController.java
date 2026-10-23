package com.antdigital.todo.controller;

import com.antdigital.todo.common.ApiResponse;
import com.antdigital.todo.model.dto.TodoCreateRequest;
import com.antdigital.todo.model.dto.TodoItemVO;
import com.antdigital.todo.service.TodoService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 待办事项 REST 控制器。
 */
@RestController
@RequestMapping("/api/todo")
public class TodoController {

    private static final Logger logger = LoggerFactory.getLogger(TodoController.class);

    private final TodoService todoService;
    private final boolean createEnabled;
    private final Counter createCallCounter;
    private final Counter createSuccessCounter;
    private final Counter createFailureCounter;
    private final Timer createTimer;

    public TodoController(TodoService todoService,
                          @Value("${todo.create.enabled:true}") boolean createEnabled,
                          MeterRegistry meterRegistry) {
        this.todoService = todoService;
        this.createEnabled = createEnabled;
        this.createCallCounter = meterRegistry.counter("todo.create.calls");
        this.createSuccessCounter = meterRegistry.counter("todo.create.success");
        this.createFailureCounter = meterRegistry.counter("todo.create.failure");
        this.createTimer = meterRegistry.timer("todo.create.timer");
    }

    /**
     * 新增待办事项。
     *
     * @param request 新增请求
     * @return 统一响应体
     */
    @PostMapping("/create")
    public ApiResponse<TodoItemVO> create(@Valid @RequestBody TodoCreateRequest request) {
        logger.info("收到新增待办事项请求, todoName: {}", request.getTodoName());

        createCallCounter.increment();
        Timer.Sample sample = createTimer.start();

        if (!createEnabled) {
            logger.warn("新增待办事项功能已关闭");
            createFailureCounter.increment();
            return ApiResponse.fail("FEATURE_DISABLED", "功能暂不可用");
        }

        try {
            TodoItemVO vo = todoService.createTodo(request);
            createSuccessCounter.increment();
            return ApiResponse.ok(vo);
        } catch (IllegalArgumentException e) {
            createFailureCounter.increment();
            logger.warn("参数校验失败", e);
            return ApiResponse.fail("PARAM_INVALID", e.getMessage());
        } catch (Exception e) {
            createFailureCounter.increment();
            logger.error("创建待办事项失败", e);
            return ApiResponse.fail("SYSTEM_ERROR", "系统内部错误");
        } finally {
            sample.stop(createTimer);
        }
    }

    /**
     * 处理 JSR-303 参数校验异常，返回统一的 PARAM_INVALID 响应。
     *
     * @param e 校验异常
     * @return 统一错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException e) {
        createCallCounter.increment();
        createFailureCounter.increment();
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("参数校验失败");
        logger.warn("参数校验失败: {}", message, e);
        return ApiResponse.fail("PARAM_INVALID", message);
    }
}