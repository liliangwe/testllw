package com.antdigital.todo.service.impl;

import com.antdigital.todo.dao.mapper.TodoItemMapper;
import com.antdigital.todo.model.dto.TodoCreateRequest;
import com.antdigital.todo.model.dto.TodoItemVO;
import com.antdigital.todo.model.entity.TodoItemDO;
import com.antdigital.todo.service.TodoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 待办事项业务服务实现。
 */
@Service
public class TodoServiceImpl implements TodoService {

    private static final Logger logger = LoggerFactory.getLogger(TodoServiceImpl.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TodoItemMapper todoItemMapper;

    public TodoServiceImpl(TodoItemMapper todoItemMapper) {
        this.todoItemMapper = todoItemMapper;
    }

    @Override
    public TodoItemVO createTodo(TodoCreateRequest request) {
        // 业务规则校验：R01 — todoName 不能为空
        if (request.getTodoName() == null || request.getTodoName().isBlank()) {
            throw new IllegalArgumentException("事项名称不能为空");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("创建待办事项, todoName: {}", request.getTodoName());
        }

        // 构建实体
        TodoItemDO todoItem = new TodoItemDO();
        todoItem.setTodoName(request.getTodoName());
        todoItem.setDescription(request.getDescription());
        todoItem.setGmtCreate(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
        todoItem.setGmtModified(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));

        // 持久化
        todoItemMapper.insert(todoItem);

        logger.info("创建待办事项成功, id: {}, todoName: {}", todoItem.getId(), todoItem.getTodoName());

        // 组装 VO 返回
        TodoItemVO vo = new TodoItemVO();
        vo.setId(todoItem.getId());
        vo.setTodoName(todoItem.getTodoName());
        vo.setDescription(todoItem.getDescription());
        vo.setGmtCreate(todoItem.getGmtCreate().format(FORMATTER));
        return vo;
    }
}