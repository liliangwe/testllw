package com.example.todo.controller;

import com.example.todo.common.BizException;
import com.example.todo.common.Result;
import com.example.todo.dto.TodoCreateRequestDTO;
import com.example.todo.dto.TodoCreateResultDTO;
import com.example.todo.service.TodoService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * 待办事项控制器
 *
 * @author AiWork
 * @date 2026/10/13
 */
@RestController
@RequestMapping("/api/todo")
public class TodoController {

    private static final Logger log = LoggerFactory.getLogger(TodoController.class);

    private final TodoService todoService;
    private final Counter createCounter;
    private final Timer createTimer;
    private final Counter validationErrorCounter;
    private final Counter systemErrorCounter;

    @Value("${todo.create.enabled:true}")
    private boolean createEnabled;

    public TodoController(TodoService todoService, MeterRegistry meterRegistry) {
        this.todoService = todoService;
        this.createCounter = Counter.builder("todo.create.count")
                .description("待办创建请求总数")
                .register(meterRegistry);
        this.createTimer = Timer.builder("todo.create.duration")
                .description("待办创建耗时")
                .register(meterRegistry);
        this.validationErrorCounter = Counter.builder("todo.create.validation.error")
                .description("待办创建参数校验失败数")
                .register(meterRegistry);
        this.systemErrorCounter = Counter.builder("todo.db.connection.error")
                .description("待办创建系统异常数")
                .register(meterRegistry);
    }

    /**
     * 新增待办事项
     *
     * @param request 创建请求
     * @return 创建结果
     */
    @PostMapping("/create")
    public ResponseEntity<Result<TodoCreateResultDTO>> createTodo(
            @RequestBody TodoCreateRequestDTO request) {
        // 功能开关
        if (!createEnabled) {
            log.warn("POST /api/todo/create 功能已关闭");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Result.fail("TODO_998", "功能维护中"));
        }

        long start = System.currentTimeMillis();
        createCounter.increment();
        try {
            TodoCreateResultDTO result = todoService.createTodo(request);
            long elapsed = System.currentTimeMillis() - start;
            createTimer.record(elapsed, TimeUnit.MILLISECONDS);
            log.info("POST /api/todo/create OK, elapsed={}ms, id={}", elapsed, result.getId());
            return ResponseEntity.ok(Result.success(result));
        } catch (BizException e) {
            long elapsed = System.currentTimeMillis() - start;
            validationErrorCounter.increment();
            log.warn("POST /api/todo/create FAIL, code={}, msg={}, elapsed={}ms",
                    e.getErrorCode(), e.getMessage(), elapsed);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.fail(e.getErrorCode(), e.getMessage()));
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            systemErrorCounter.increment();
            log.error("POST /api/todo/create ERROR, elapsed={}ms", elapsed, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.fail("TODO_999", "系统内部错误"));
        }
    }
}