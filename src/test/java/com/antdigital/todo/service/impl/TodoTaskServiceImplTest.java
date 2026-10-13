package com.antdigital.todo.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * {@link TodoTaskServiceImpl} 单元测试。
 *
 * <p>依赖全部 Mock，不启动 Spring 容器；时钟使用 {@link Clock#fixed} 保证结果可重复。</p>
 *
 * @author AiWork
 * @date 2026/10/13
 */
@ExtendWith(MockitoExtension.class)
class TodoTaskServiceImplTest {

    private static final String TENANT_ID = "tenant001";

    private static final String CREATOR = "10086";

    private static final Long TASK_ID = 1000001L;

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 10, 13, 10, 20, 30);

    private static final String EXPECTED_GMT_CREATE = "2026-10-13 10:20:30";

    @Mock
    private TodoTaskMapper todoTaskMapper;

    @Mock
    private InvokerContext invokerContext;

    @Mock
    private TodoTaskCreateSwitch todoTaskCreateSwitch;

    @Mock
    private TodoTaskMonitor todoTaskMonitor;

    private TodoTaskServiceImpl todoTaskService;

    @BeforeEach
    void setUp() {
        ZoneId zoneId = ZoneId.systemDefault();
        Clock fixedClock = Clock.fixed(FIXED_NOW.atZone(zoneId).toInstant(), zoneId);
        todoTaskService = new TodoTaskServiceImpl(todoTaskMapper, invokerContext, todoTaskCreateSwitch,
                todoTaskMonitor, fixedClock);
    }

    // ==================== createTodoTask 正常路径 ====================

    @Test
    void should_persistAllFieldsAndReturnResultView_when_requestValid() {
        // Arrange
        givenInvokerContext();
        givenCreateEnabled();
        when(todoTaskMapper.insert(any(TodoTaskDO.class))).thenReturn(TASK_ID);
        TodoTaskCreateRequest request = buildRequest("整理季度复盘材料", "收集各组数据，输出复盘文档初稿");

        // Act
        TodoTaskCreateResponse response = todoTaskService.createTodoTask(request);

        // Assert - 出参视图
        assertThat(response.getTaskId()).isEqualTo(TASK_ID);
        assertThat(response.getTaskName()).isEqualTo("整理季度复盘材料");
        assertThat(response.getGmtCreate()).isEqualTo(EXPECTED_GMT_CREATE);

        // Assert - 回查落库实体：归属字段与审计字段均由服务侧赋值（R04/R05/R08）
        ArgumentCaptor<TodoTaskDO> captor = ArgumentCaptor.forClass(TodoTaskDO.class);
        verify(todoTaskMapper, times(1)).insert(captor.capture());
        TodoTaskDO saved = captor.getValue();
        assertThat(saved.getTenantId()).isEqualTo(TENANT_ID);
        assertThat(saved.getCreator()).isEqualTo(CREATOR);
        assertThat(saved.getTaskName()).isEqualTo("整理季度复盘材料");
        assertThat(saved.getTaskDesc()).isEqualTo("收集各组数据，输出复盘文档初稿");
        assertThat(saved.getIsDeleted()).isEqualTo(TodoTaskConstants.IS_DELETED_NO);
        assertThat(saved.getGmtCreate()).isEqualTo(FIXED_NOW);
        assertThat(saved.getGmtModified()).isEqualTo(FIXED_NOW);

        // Assert - 埋点上报
        verify(todoTaskMonitor, times(1))
                .reportCreate(eq(TENANT_ID), eq(TodoResultConstants.RESULT_OK), isNull(), anyLong());
    }

    @Test
    void should_trimTaskNameAndStoreEmptyDesc_when_descIsNullAndNameHasSpaces() {
        // Arrange
        givenInvokerContext();
        givenCreateEnabled();
        when(todoTaskMapper.insert(any(TodoTaskDO.class))).thenReturn(TASK_ID);
        TodoTaskCreateRequest request = buildRequest("  写周报  ", null);

        // Act
        todoTaskService.createTodoTask(request);

        // Assert
        ArgumentCaptor<TodoTaskDO> captor = ArgumentCaptor.forClass(TodoTaskDO.class);
        verify(todoTaskMapper).insert(captor.capture());
        assertThat(captor.getValue().getTaskName()).isEqualTo("写周报");
        assertThat(captor.getValue().getTaskDesc()).isEmpty();
    }

    // ==================== 入参兜底校验（R01/R02/R10） ====================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "　"})
    void should_throwParamInvalid_when_taskNameIsBlank(String taskName) {
        // Arrange
        TodoTaskCreateRequest request = buildRequest(taskName, "描述");

        // Act
        TodoBizException exception = assertThrows(TodoBizException.class,
                () -> todoTaskService.createTodoTask(request));

        // Assert
        assertThat(exception.getErrorCode()).isEqualTo(TodoErrorCodeEnum.PARAM_INVALID.getCode());
        assertThat(exception.getMessage()).isEqualTo(TodoTipConstants.TASK_NAME_INVALID);
        verify(todoTaskMapper, never()).insert(any(TodoTaskDO.class));
    }

    @Test
    void should_throwParamInvalid_when_taskNameExceedsMaxLength() {
        // Arrange
        TodoTaskCreateRequest request = buildRequest("a".repeat(TodoTaskConstants.TASK_NAME_MAX_LEN + 1), null);

        // Act
        TodoBizException exception = assertThrows(TodoBizException.class,
                () -> todoTaskService.createTodoTask(request));

        // Assert
        assertThat(exception.getErrorCode()).isEqualTo(TodoErrorCodeEnum.PARAM_INVALID.getCode());
        assertThat(exception.getMessage()).isEqualTo(TodoTipConstants.TASK_NAME_INVALID);
        verify(todoTaskMapper, never()).insert(any(TodoTaskDO.class));
    }

    @Test
    void should_throwParamInvalid_when_taskDescExceedsMaxLength() {
        // Arrange
        TodoTaskCreateRequest request = buildRequest("整理季度复盘材料",
                "d".repeat(TodoTaskConstants.TASK_DESC_MAX_LEN + 1));

        // Act
        TodoBizException exception = assertThrows(TodoBizException.class,
                () -> todoTaskService.createTodoTask(request));

        // Assert
        assertThat(exception.getErrorCode()).isEqualTo(TodoErrorCodeEnum.PARAM_INVALID.getCode());
        assertThat(exception.getMessage()).isEqualTo(TodoTipConstants.TASK_DESC_TOO_LONG);
        verify(todoTaskMapper, never()).insert(any(TodoTaskDO.class));
    }

    @Test
    void should_throwParamInvalid_when_requestIsNull() {
        // Act
        TodoBizException exception = assertThrows(TodoBizException.class, () -> todoTaskService.createTodoTask(null));

        // Assert
        assertThat(exception.getErrorCode()).isEqualTo(TodoErrorCodeEnum.PARAM_INVALID.getCode());
        assertThat(exception.getMessage()).isEqualTo(TodoTipConstants.REQUEST_REQUIRED);
    }

    // ==================== 创建人归属与租户隔离（R04/R05） ====================

    @Test
    void should_throwInvokerMissing_when_creatorIsBlank() {
        // Arrange
        when(invokerContext.getInvoker()).thenReturn("  ");
        TodoTaskCreateRequest request = buildRequest("整理季度复盘材料", null);

        // Act
        TodoBizException exception = assertThrows(TodoBizException.class,
                () -> todoTaskService.createTodoTask(request));

        // Assert
        assertThat(exception.getErrorCode()).isEqualTo(TodoErrorCodeEnum.INVOKER_MISSING.getCode());
        assertThat(exception.getMessage()).isEqualTo(TodoErrorCodeEnum.INVOKER_MISSING.getMessage());
        verify(todoTaskMapper, never()).insert(any(TodoTaskDO.class));
    }

    @Test
    void should_throwTenantMissing_when_tenantAbsent() {
        // Arrange
        when(invokerContext.getInvoker()).thenReturn(CREATOR);
        when(invokerContext.getTenantId()).thenReturn(null);
        TodoTaskCreateRequest request = buildRequest("整理季度复盘材料", null);

        // Act
        TodoBizException exception = assertThrows(TodoBizException.class,
                () -> todoTaskService.createTodoTask(request));

        // Assert
        assertThat(exception.getErrorCode()).isEqualTo(TodoErrorCodeEnum.TENANT_MISSING.getCode());
        assertThat(exception.getMessage()).isEqualTo(TodoErrorCodeEnum.TENANT_MISSING.getMessage());
        verify(todoTaskMapper, never()).insert(any(TodoTaskDO.class));
    }

    // ==================== 应急开关（R07） ====================

    @Test
    void should_throwCreateDisabled_when_switchClosed() {
        // Arrange
        givenInvokerContext();
        when(todoTaskCreateSwitch.isEnabled(TENANT_ID)).thenReturn(false);
        TodoTaskCreateRequest request = buildRequest("整理季度复盘材料", null);

        // Act
        TodoBizException exception = assertThrows(TodoBizException.class,
                () -> todoTaskService.createTodoTask(request));

        // Assert
        assertThat(exception.getErrorCode()).isEqualTo(TodoErrorCodeEnum.CREATE_DISABLED.getCode());
        assertThat(exception.getMessage()).isEqualTo(TodoErrorCodeEnum.CREATE_DISABLED.getMessage());
        verify(todoTaskMapper, never()).insert(any(TodoTaskDO.class));
    }

    // ==================== 落库异常（R08 异常场景） ====================

    @Test
    void should_throwSaveFailed_when_mapperThrowsDataAccessException() {
        // Arrange
        givenInvokerContext();
        givenCreateEnabled();
        when(todoTaskMapper.insert(any(TodoTaskDO.class)))
                .thenThrow(new DataAccessResourceFailureException("db unavailable"));
        TodoTaskCreateRequest request = buildRequest("整理季度复盘材料", null);

        // Act
        TodoBizException exception = assertThrows(TodoBizException.class,
                () -> todoTaskService.createTodoTask(request));

        // Assert
        assertThat(exception.getErrorCode()).isEqualTo(TodoErrorCodeEnum.SAVE_FAILED.getCode());
        assertThat(exception.getCause()).isInstanceOf(DataAccessResourceFailureException.class);
        verify(todoTaskMonitor, times(1))
                .reportCreate(eq(TENANT_ID), eq(TodoErrorCodeEnum.SAVE_FAILED.getCode()), any(), anyLong());
    }

    @Test
    void should_throwSaveFailed_when_mapperReturnsNullId() {
        // Arrange
        givenInvokerContext();
        givenCreateEnabled();
        when(todoTaskMapper.insert(any(TodoTaskDO.class))).thenReturn(null);
        TodoTaskCreateRequest request = buildRequest("整理季度复盘材料", null);

        // Act
        TodoBizException exception = assertThrows(TodoBizException.class,
                () -> todoTaskService.createTodoTask(request));

        // Assert
        assertThat(exception.getErrorCode()).isEqualTo(TodoErrorCodeEnum.SAVE_FAILED.getCode());
    }

    private void givenInvokerContext() {
        when(invokerContext.getInvoker()).thenReturn(CREATOR);
        when(invokerContext.getTenantId()).thenReturn(TENANT_ID);
    }

    private void givenCreateEnabled() {
        when(todoTaskCreateSwitch.isEnabled(TENANT_ID)).thenReturn(true);
    }

    private TodoTaskCreateRequest buildRequest(String taskName, String taskDesc) {
        TodoTaskCreateRequest request = new TodoTaskCreateRequest();
        request.setTaskName(taskName);
        request.setTaskDesc(taskDesc);
        return request;
    }
}
