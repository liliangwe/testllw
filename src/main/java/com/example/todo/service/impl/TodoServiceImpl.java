package com.example.todo.service.impl;

import com.example.todo.common.BizException;
import com.example.todo.dto.TodoCreateRequestDTO;
import com.example.todo.dto.TodoCreateResultDTO;
import com.example.todo.entity.TodoItemDO;
import com.example.todo.entity.TodoStatus;
import com.example.todo.mapper.TodoItemMapper;
import com.example.todo.service.TodoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 待办事项服务实现
 *
 * @author AiWork
 * @date 2026/10/13
 */
@Service
public class TodoServiceImpl implements TodoService {

    private static final Logger log = LoggerFactory.getLogger(TodoServiceImpl.class);
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private static final int NAME_MAX_LENGTH = 200;
    private static final int DESCRIPTION_MAX_LENGTH = 2000;

    private final TodoItemMapper todoItemMapper;

    public TodoServiceImpl(TodoItemMapper todoItemMapper) {
        this.todoItemMapper = todoItemMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TodoCreateResultDTO createTodo(TodoCreateRequestDTO request) {
        // 参数校验
        validateRequest(request);

        try {
            TodoItemDO todoItem = buildTodoItem(request);
            todoItemMapper.insert(todoItem);

            log.info("待办事项创建成功, id={}, name={}", todoItem.getId(), todoItem.getName());

            return buildResult(todoItem);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("待办事项创建失败, name={}", request.getName(), e);
            throw new BizException("TODO_999", "系统内部错误");
        }
    }

    /**
     * 参数校验
     */
    private void validateRequest(TodoCreateRequestDTO request) {
        // R01: name 不能为空或纯空白
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BizException("TODO_001", "事项名称不能为空");
        }
        // R02: name 长度 ≤ 200
        if (request.getName().length() > NAME_MAX_LENGTH) {
            throw new BizException("TODO_002", "事项名称不能超过" + NAME_MAX_LENGTH + "字符");
        }
        // R03: description 长度 ≤ 2000
        if (request.getDescription() != null && request.getDescription().length() > DESCRIPTION_MAX_LENGTH) {
            throw new BizException("TODO_003", "事项描述不能超过" + DESCRIPTION_MAX_LENGTH + "字符");
        }
    }

    /**
     * 构建数据对象
     */
    private TodoItemDO buildTodoItem(TodoCreateRequestDTO request) {
        TodoItemDO item = new TodoItemDO();
        item.setName(request.getName().trim());
        item.setDescription(request.getDescription());
        item.setStatus(TodoStatus.PENDING.getCode());
        LocalDateTime now = LocalDateTime.now();
        item.setGmtCreate(now);
        item.setGmtModified(now);
        return item;
    }

    /**
     * 构建响应 DTO
     */
    private TodoCreateResultDTO buildResult(TodoItemDO todoItem) {
        TodoCreateResultDTO result = new TodoCreateResultDTO();
        result.setId(todoItem.getId());
        result.setName(todoItem.getName());
        result.setDescription(todoItem.getDescription());
        result.setStatus(todoItem.getStatus());
        result.setGmtCreate(todoItem.getGmtCreate().format(ISO_FORMATTER));
        return result;
    }
}