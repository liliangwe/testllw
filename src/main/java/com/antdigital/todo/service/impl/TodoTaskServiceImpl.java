package com.antdigital.todo.service.impl;

import com.antdigital.todo.api.request.TodoTaskCreateRequest;
import com.antdigital.todo.api.response.TodoTaskCreateResponse;
import com.antdigital.todo.common.constant.TodoResultConstants;
import com.antdigital.todo.common.constant.TodoTaskConstants;
import com.antdigital.todo.common.constant.TodoTipConstants;
import com.antdigital.todo.common.context.InvokerContext;
import com.antdigital.todo.common.enums.TodoErrorCodeEnum;
import com.antdigital.todo.common.exception.TodoBizException;
import com.antdigital.todo.common.feature.TodoTaskCreateSwitch;
import com.antdigital.todo.common.monitor.TodoTaskMonitor;
import com.antdigital.todo.dao.entity.TodoTaskDO;
import com.antdigital.todo.dao.mapper.TodoTaskMapper;
import com.antdigital.todo.service.TodoTaskService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 * 待办事项服务实现（系分 5.1.3.1 F01/F02/F03、5.1.3.2 F04、5.1.3.3 F05）。
 *
 * @author AiWork
 * @date 2026/10/13
 */
@Service
public class TodoTaskServiceImpl implements TodoTaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TodoTaskServiceImpl.class);

    /** 响应中的创建时间格式，前后端统一 yyyy-MM-dd HH:mm:ss */
    private static final DateTimeFormatter GMT_CREATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TodoTaskMapper todoTaskMapper;

    private final InvokerContext invokerContext;

    private final TodoTaskCreateSwitch todoTaskCreateSwitch;

    private final TodoTaskMonitor todoTaskMonitor;

    private final Clock clock;

    public TodoTaskServiceImpl(TodoTaskMapper todoTaskMapper, InvokerContext invokerContext,
            TodoTaskCreateSwitch todoTaskCreateSwitch, TodoTaskMonitor todoTaskMonitor, Clock clock) {
        this.todoTaskMapper = todoTaskMapper;
        this.invokerContext = invokerContext;
        this.todoTaskCreateSwitch = todoTaskCreateSwitch;
        this.todoTaskMonitor = todoTaskMonitor;
        this.clock = clock;
    }

    @Override
    public TodoTaskCreateResponse createTodoTask(TodoTaskCreateRequest request) {
        long startTimeMillis = System.currentTimeMillis();
        try {
            TodoTaskDO todoTask = buildTodoTask(request);
            Long taskId = insertTodoTask(todoTask);

            TodoTaskCreateResponse response = buildResponse(taskId, todoTask);
            long costMillis = System.currentTimeMillis() - startTimeMillis;
            todoTaskMonitor.reportCreate(todoTask.getTenantId(), TodoResultConstants.RESULT_OK, null, costMillis);
            LOGGER.info("todo task created, tenantId:{}, creator:{}, taskId:{}, costMillis:{}",
                    todoTask.getTenantId(), todoTask.getCreator(), taskId, costMillis);
            return response;
        } catch (TodoBizException exception) {
            todoTaskMonitor.reportCreate(invokerContext.getTenantId(), exception.getErrorCode(),
                    exception.getMessage(), System.currentTimeMillis() - startTimeMillis);
            throw exception;
        }
    }

    /**
     * 组装并校验落库实体：入参兜底校验（R01/R02/R10）→ 创建人（R04）→ 租户（R05）→ 应急开关（R07）。
     *
     * @param request 新增请求
     * @return 待落库实体，审计时间与逻辑删除标记由代码赋值（R08）
     */
    private TodoTaskDO buildTodoTask(TodoTaskCreateRequest request) {
        if (request == null) {
            throw new TodoBizException(TodoErrorCodeEnum.PARAM_INVALID, TodoTipConstants.REQUEST_REQUIRED);
        }
        String taskName = normalizeTaskName(request.getTaskName());
        String taskDesc = normalizeTaskDesc(request.getTaskDesc());
        String creator = requireCreator();
        String tenantId = requireTenantId();
        requireCreateEnabled(tenantId);

        LocalDateTime now = LocalDateTime.now(clock);
        TodoTaskDO todoTask = new TodoTaskDO();
        todoTask.setTenantId(tenantId);
        todoTask.setTaskName(taskName);
        todoTask.setTaskDesc(taskDesc);
        todoTask.setCreator(creator);
        todoTask.setIsDeleted(TodoTaskConstants.IS_DELETED_NO);
        todoTask.setGmtCreate(now);
        todoTask.setGmtModified(now);
        return todoTask;
    }

    /**
     * 事项名称兜底校验：去除首尾空白（含全角空格）后非空且不超过 64 字符。
     *
     * @param taskName 原始事项名称
     * @return 规范化后的事项名称
     */
    private String normalizeTaskName(String taskName) {
        String strippedTaskName = taskName == null ? null : taskName.strip();
        if (strippedTaskName == null || strippedTaskName.isEmpty()) {
            throw new TodoBizException(TodoErrorCodeEnum.PARAM_INVALID, TodoTipConstants.TASK_NAME_INVALID);
        }
        if (strippedTaskName.length() > TodoTaskConstants.TASK_NAME_MAX_LEN) {
            throw new TodoBizException(TodoErrorCodeEnum.PARAM_INVALID, TodoTipConstants.TASK_NAME_INVALID);
        }
        return strippedTaskName;
    }

    /**
     * 事项描述兜底校验：选填，不传或为空时按空串存储，最大 512 字符。
     *
     * @param taskDesc 原始事项描述
     * @return 落库用的事项描述
     */
    private String normalizeTaskDesc(String taskDesc) {
        if (taskDesc == null) {
            return "";
        }
        if (taskDesc.length() > TodoTaskConstants.TASK_DESC_MAX_LEN) {
            throw new TodoBizException(TodoErrorCodeEnum.PARAM_INVALID, TodoTipConstants.TASK_DESC_TOO_LONG);
        }
        return taskDesc;
    }

    /**
     * 创建人校验：只能取自登录态，禁止入参传入或覆盖。
     *
     * @return 创建人标识
     */
    private String requireCreator() {
        String creator = invokerContext.getInvoker();
        if (creator == null || creator.strip().isEmpty()) {
            throw new TodoBizException(TodoErrorCodeEnum.INVOKER_MISSING);
        }
        return creator.strip();
    }

    /**
     * 租户校验：取自登录态上下文，作为水平权限边界写入记录。
     *
     * @return 租户标识
     */
    private String requireTenantId() {
        String tenantId = invokerContext.getTenantId();
        if (tenantId == null || tenantId.strip().isEmpty()) {
            throw new TodoBizException(TodoErrorCodeEnum.TENANT_MISSING);
        }
        return tenantId.strip();
    }

    /**
     * 应急开关校验：开关关闭时快速失败，不落库。
     *
     * @param tenantId 租户标识
     */
    private void requireCreateEnabled(String tenantId) {
        if (!todoTaskCreateSwitch.isEnabled(tenantId)) {
            throw new TodoBizException(TodoErrorCodeEnum.CREATE_DISABLED);
        }
    }

    /**
     * 落库并返回自增主键，数据库异常统一转换为 TODO_004 快速失败（不重试）。
     *
     * @param todoTask 待落库实体
     * @return 新建待办事项 ID
     */
    private Long insertTodoTask(TodoTaskDO todoTask) {
        try {
            Long taskId = todoTaskMapper.insert(todoTask);
            if (taskId == null || taskId <= 0L) {
                LOGGER.error("todo task insert returned invalid id, tenantId:{}, creator:{}, taskName:{}, taskId:{}",
                        todoTask.getTenantId(), todoTask.getCreator(), todoTask.getTaskName(), taskId);
                throw new TodoBizException(TodoErrorCodeEnum.SAVE_FAILED);
            }
            return taskId;
        } catch (DataAccessException exception) {
            LOGGER.error("todo task insert failed, tenantId:{}, creator:{}, taskName:{}, errorMessage:{}",
                    todoTask.getTenantId(), todoTask.getCreator(), todoTask.getTaskName(),
                    exception.getMessage(), exception);
            throw new TodoBizException(TodoErrorCodeEnum.SAVE_FAILED, TodoErrorCodeEnum.SAVE_FAILED.getMessage(),
                    exception);
        }
    }

    /**
     * 组装出参视图。
     *
     * @param taskId   新建待办事项 ID
     * @param todoTask 落库实体
     * @return W01 出参 data
     */
    private TodoTaskCreateResponse buildResponse(Long taskId, TodoTaskDO todoTask) {
        TodoTaskCreateResponse response = new TodoTaskCreateResponse();
        response.setTaskId(taskId);
        response.setTaskName(todoTask.getTaskName());
        response.setGmtCreate(GMT_CREATE_FORMATTER.format(todoTask.getGmtCreate()));
        return response;
    }
}
