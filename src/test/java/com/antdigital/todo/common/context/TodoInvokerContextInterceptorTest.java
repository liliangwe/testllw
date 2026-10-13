package com.antdigital.todo.common.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.antdigital.todo.api.controller.TodoExceptionHandler;
import com.antdigital.todo.api.controller.TodoTaskController;
import com.antdigital.todo.common.enums.TodoErrorCodeEnum;
import com.antdigital.todo.common.feature.TodoTaskCreateSwitch;
import com.antdigital.todo.common.monitor.TodoTaskMonitor;
import com.antdigital.todo.dao.entity.TodoTaskDO;
import com.antdigital.todo.dao.mapper.TodoTaskMapper;
import com.antdigital.todo.service.impl.TodoTaskServiceImpl;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * 登录态接线拦截器接入创建闭环的 MockMvc 测试（系分 6.4.2.3 / R04/R05、G8.5）。
 *
 * <p>使用 standalone MockMvc 挂载真实 Controller、Service 与 {@link TodoInvokerContextInterceptor}，
 * 校验：身份缺失时 fail-closed 返回 {@code TODO_002}/{@code TODO_003}；身份接线后创建人/租户取自
 * 登录态而非入参；请求结束（含异常路径）后 ThreadLocal 上下文已清理。</p>
 *
 * @author AiWork
 * @date 2026/10/13
 */
@ExtendWith(MockitoExtension.class)
class TodoInvokerContextInterceptorTest {

    private static final String CREATE_URI = "/api/todo/task/create";

    private static final String VALID_BODY = "{\"taskName\":\"整理季度复盘材料\",\"taskDesc\":\"收集各组数据\"}";

    private static final String INVOKER = "10086";

    private static final String TENANT_ID = "tenant001";

    private static final Long TASK_ID = 1000001L;

    @Mock
    private TodoTaskMapper todoTaskMapper;

    @Mock
    private TodoTaskCreateSwitch todoTaskCreateSwitch;

    @Mock
    private TodoTaskMonitor todoTaskMonitor;

    private ThreadLocalInvokerContext invokerContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        invokerContext = new ThreadLocalInvokerContext();
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        Clock fixedClock = Clock.fixed(LocalDateTime.of(2026, 10, 13, 10, 20, 30).atZone(zoneId).toInstant(), zoneId);
        TodoTaskServiceImpl todoTaskService = new TodoTaskServiceImpl(todoTaskMapper, invokerContext,
                todoTaskCreateSwitch, todoTaskMonitor, fixedClock);
        mockMvc = MockMvcBuilders.standaloneSetup(new TodoTaskController(todoTaskService))
                .setControllerAdvice(new TodoExceptionHandler())
                .addInterceptors(new TodoInvokerContextInterceptor(
                        TodoInvokerContextInterceptor.DEFAULT_INVOKER_ATTRIBUTE,
                        TodoInvokerContextInterceptor.DEFAULT_TENANT_ATTRIBUTE))
                .build();
    }

    @AfterEach
    void tearDown() {
        ThreadLocalInvokerContext.clear();
    }

    @Test
    void should_returnTodo002AndSkipPersist_when_identityAbsent() throws Exception {
        // Act & Assert：平台未接线身份 → fail-closed
        mockMvc.perform(post(CREATE_URI).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(TodoErrorCodeEnum.INVOKER_MISSING.getCode()));

        verify(todoTaskMapper, never()).insert(any(TodoTaskDO.class));
        assertThat(invokerContext.getInvoker()).isNull();
        assertThat(invokerContext.getTenantId()).isNull();
    }

    @Test
    void should_returnTodo003_when_tenantAbsent() throws Exception {
        // Act & Assert：仅有创建人、无租户 → 拒绝写入
        mockMvc.perform(post(CREATE_URI).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY)
                        .requestAttr(TodoInvokerContextInterceptor.DEFAULT_INVOKER_ATTRIBUTE, INVOKER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(TodoErrorCodeEnum.TENANT_MISSING.getCode()));

        verify(todoTaskMapper, never()).insert(any(TodoTaskDO.class));
        assertThat(invokerContext.getInvoker()).isNull();
    }

    @Test
    void should_persistIdentityFromRequestAttribute_when_interceptorWired() throws Exception {
        // Arrange
        when(todoTaskCreateSwitch.isEnabled(TENANT_ID)).thenReturn(true);
        when(todoTaskMapper.insert(any(TodoTaskDO.class))).thenReturn(TASK_ID);

        // Act
        mockMvc.perform(post(CREATE_URI).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY)
                        .requestAttr(TodoInvokerContextInterceptor.DEFAULT_INVOKER_ATTRIBUTE, INVOKER)
                        .requestAttr(TodoInvokerContextInterceptor.DEFAULT_TENANT_ATTRIBUTE, TENANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("OK"))
                .andExpect(jsonPath("$.data.taskId").value(TASK_ID));

        // Assert：创建人与租户取自登录态上下文（R04/R05）
        ArgumentCaptor<TodoTaskDO> captor = ArgumentCaptor.forClass(TodoTaskDO.class);
        verify(todoTaskMapper).insert(captor.capture());
        assertThat(captor.getValue().getCreator()).isEqualTo(INVOKER);
        assertThat(captor.getValue().getTenantId()).isEqualTo(TENANT_ID);

        // Assert：请求结束后上下文已清理（G8.5）
        assertThat(invokerContext.getInvoker()).isNull();
        assertThat(invokerContext.getTenantId()).isNull();
    }

    @Test
    void should_ignoreClientSuppliedIdentity_when_bodyCarriesIdentityFields() throws Exception {
        // Arrange：报文试图传入身份字段，应被忽略
        when(todoTaskCreateSwitch.isEnabled(TENANT_ID)).thenReturn(true);
        when(todoTaskMapper.insert(any(TodoTaskDO.class))).thenReturn(TASK_ID);
        String spoofBody = "{\"taskName\":\"整理季度复盘材料\",\"creator\":\"spoof\",\"tenantId\":\"other\"}";

        // Act & Assert
        mockMvc.perform(post(CREATE_URI).contentType(MediaType.APPLICATION_JSON).content(spoofBody)
                        .requestAttr(TodoInvokerContextInterceptor.DEFAULT_INVOKER_ATTRIBUTE, INVOKER)
                        .requestAttr(TodoInvokerContextInterceptor.DEFAULT_TENANT_ATTRIBUTE, TENANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("OK"));

        ArgumentCaptor<TodoTaskDO> captor = ArgumentCaptor.forClass(TodoTaskDO.class);
        verify(todoTaskMapper).insert(captor.capture());
        assertThat(captor.getValue().getCreator()).isEqualTo(INVOKER);
        assertThat(captor.getValue().getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    void should_clearInvokerContext_when_persistFailed() throws Exception {
        // Arrange：落库异常路径
        when(todoTaskCreateSwitch.isEnabled(TENANT_ID)).thenReturn(true);
        when(todoTaskMapper.insert(any(TodoTaskDO.class)))
                .thenThrow(new DataAccessResourceFailureException("db unavailable"));

        // Act & Assert
        mockMvc.perform(post(CREATE_URI).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY)
                        .requestAttr(TodoInvokerContextInterceptor.DEFAULT_INVOKER_ATTRIBUTE, INVOKER)
                        .requestAttr(TodoInvokerContextInterceptor.DEFAULT_TENANT_ATTRIBUTE, TENANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(TodoErrorCodeEnum.SAVE_FAILED.getCode()));

        // Assert：异常路径同样清理上下文（等价 finally，G8.5）
        assertThat(invokerContext.getInvoker()).isNull();
        assertThat(invokerContext.getTenantId()).isNull();
    }
}
