package com.example.todo.service.impl;

import com.example.todo.common.constant.TodoErrorCode;
import com.example.todo.common.exception.BusinessException;
import com.example.todo.dao.entity.TodoItemDO;
import com.example.todo.dao.mapper.TodoItemMapper;
import com.example.todo.model.dto.TodoItemDTO;
import com.example.todo.model.request.CreateTodoRequest;
import com.example.todo.service.TodoService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 待办事项业务服务实现
 *
 * @author AiWork
 * @date 2026/10/14
 */
@Service
public class TodoServiceImpl implements TodoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TodoServiceImpl.class);

    /** 待办事项时间字段输出格式 */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 新增成功时的受影响行数 */
    private static final int AFFECTED_ROWS_SUCCESS = 1;

    private final TodoItemMapper todoItemMapper;

    public TodoServiceImpl(TodoItemMapper todoItemMapper) {
        this.todoItemMapper = todoItemMapper;
    }

    @Override
    public TodoItemDTO createTodo(CreateTodoRequest request, String tenantId) {
        // 业务规则 R04：租户 ID 必须来自登录上下文
        if (!StringUtils.hasText(tenantId)) {
            throw new BusinessException(TodoErrorCode.TENANT_INFO_MISSING, TodoErrorCode.MSG_TENANT_INFO_MISSING);
        }

        // 组装待办事项数据对象并持久化
        TodoItemDO todoItemDO = buildTodoItemDO(request, tenantId);
        int affectedRows = todoItemMapper.saveTodoItem(todoItemDO);
        if (AFFECTED_ROWS_SUCCESS != affectedRows) {
            LOGGER.error("新增待办事项失败, affectedRows:{}, tenantId:{}", affectedRows, tenantId);
            throw new BusinessException(TodoErrorCode.SYSTEM_ERROR, TodoErrorCode.MSG_SYSTEM_ERROR);
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("新增待办事项成功, id:{}, tenantId:{}", todoItemDO.getId(), tenantId);
        }
        return convertToDTO(todoItemDO);
    }

    /**
     * 组装待办事项数据对象
     *
     * @param request  新增待办事项请求
     * @param tenantId 租户 ID
     * @return 待办事项数据对象
     */
    private TodoItemDO buildTodoItemDO(CreateTodoRequest request, String tenantId) {
        LocalDateTime now = LocalDateTime.now();
        TodoItemDO todoItemDO = new TodoItemDO();
        todoItemDO.setTenantId(tenantId);
        todoItemDO.setName(request.getName());
        todoItemDO.setDescription(request.getDescription());
        todoItemDO.setGmtCreate(now);
        todoItemDO.setGmtModified(now);
        return todoItemDO;
    }

    /**
     * 将待办事项数据对象转换为数据传输对象
     *
     * @param todoItemDO 待办事项数据对象
     * @return 待办事项数据传输对象，入参为空时返回 null
     */
    private TodoItemDTO convertToDTO(TodoItemDO todoItemDO) {
        if (todoItemDO == null) {
            return null;
        }

        TodoItemDTO todoItemDTO = new TodoItemDTO();
        todoItemDTO.setId(todoItemDO.getId());
        todoItemDTO.setName(todoItemDO.getName());
        todoItemDTO.setDescription(todoItemDO.getDescription());
        todoItemDTO.setGmtCreate(todoItemDO.getGmtCreate() == null
                ? null : DATE_TIME_FORMATTER.format(todoItemDO.getGmtCreate()));
        return todoItemDTO;
    }
}
