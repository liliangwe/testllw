# Code Review Report

> **Change** 待办事项核心功能-新增待办事项 · **分支** AI/task-DEV-966dcd0a · **日期** 2026-10-14 · **审查者** AI

---

## 1. 审查范围

| 项 | 值 |
|----|-----|
| `.java` 文件数 | 13 |
| 变更行数 | `+1396 / -0`（全量新增） |

| 类/接口 | 路径 | 角色 |
|---------|------|------|
| `TodoApplication` | `src/main/java/com/example/todo/TodoApplication.java` | Spring Boot 启动类 |
| `ApiResponse` | `src/main/java/com/example/todo/common/ApiResponse.java` | 统一出参结构 |
| `TodoErrorCode` | `src/main/java/com/example/todo/common/constant/TodoErrorCode.java` | 错误码常量 |
| `BusinessException` | `src/main/java/com/example/todo/common/exception/BusinessException.java` | 业务异常 |
| `GlobalExceptionHandler` | `src/main/java/com/example/todo/common/exception/GlobalExceptionHandler.java` | 全局异常处理 |
| `TodoController` | `src/main/java/com/example/todo/controller/TodoController.java` | oneapi 控制器 (POST /api/todo) |
| `TodoItemDO` | `src/main/java/com/example/todo/dao/entity/TodoItemDO.java` | 数据对象 |
| `TodoItemMapper` | `src/main/java/com/example/todo/dao/mapper/TodoItemMapper.java` | MyBatis 数据访问 |
| `TodoItemDTO` | `src/main/java/com/example/todo/model/dto/TodoItemDTO.java` | 出参 DTO |
| `CreateTodoRequest` | `src/main/java/com/example/todo/model/request/CreateTodoRequest.java` | 入参请求对象 |
| `TodoService` | `src/main/java/com/example/todo/service/TodoService.java` | 业务服务接口 |
| `TodoServiceImpl` | `src/main/java/com/example/todo/service/impl/TodoServiceImpl.java` | 业务服务实现 |
| `TodoServiceImplTest` | `src/test/java/com/example/todo/service/impl/TodoServiceImplTest.java` | 单元测试 |

---

## 2. 问题计数

| P0 | P1 | P2 |
|----|----|----|
| 0 | 2 | 5 |

> P0 = 阻塞（阻止合并）；P1 = 推荐（合并前应修复）；P2 = 参考（可选改进）

---

## 3. Step 2 — 功能（REQ）

### REQ-F01: 新增待办事项（系分 F01 / W01）

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| R01: name 不能为空且 ≤200 字符 | ✅ | design §5.1.2 入参表 + R01/R02 | `CreateTodoRequest.java:27-28` `@NotBlank`+`@Size(max=200)`; `GlobalExceptionHandler.java:100-103` 映射 TODO_001 | 校验消息"事项名称长度不能超过200字符"与系分"事项名称过长"措辞略有差异，语义等价 |
| R02: description ≤2000 字符 | ✅ | design §5.1.2 入参表 + R03 | `CreateTodoRequest.java:32` `@Size(max=2000)`; `GlobalExceptionHandler.java:100-103` 映射 TODO_002 | — |
| R03: 创建成功后返回完整待办事项信息 | ✅ | design §5.1.2 出参表（id/name/description/gmtCreate） | `TodoServiceImpl.java:58,85-97 convertToDTO` 返回完整 TodoItemDTO | — |
| R04: tenant_id 从登录上下文获取，缺失返回 TODO_003 | ✅ | design §5.1.3 R04 + 异常场景表 | `TodoController.java:51` 读取 X-Tenant-Id; `TodoServiceImpl.java:43-45` 空值校验 → TODO_003 | 系分 D01/D02 偏差：本期无认证组件，租户由请求头透传（非上下文）；标记为已知设计权衡 |
| 错误码映射完整 | ✅ | design §5.1.2 错误码表（SUCCESS/TODO_001~003/TODO_500） | `TodoErrorCode.java:14-26` 5 个错误码 + 3 个提示语常量全覆盖 | — |
| HTTP 状态码 | ✅ | design §6.4.2.3 未登录返回 401（本期采用 TODO_003 替代） | `GlobalExceptionHandler.java:38-45` 业务异常 200 + 错误码; 系统异常 500 | 已知偏差，与系分 D02 一致 |
| API 路径匹配 | ✅ | design §4.1 W01: POST /api/todo | `TodoController.java:23,49` `@RequestMapping("/api/todo")` + `@PostMapping` | — |
| 表结构与系分一致 | ✅ | design §5.1.1.1 todo_item 表（6 字段 + 3 索引） | `TodoItemDO.java` 6 字段; `schema.sql:4-13` 字段/类型/默认值一致 | schema.sql 缺少 `gmt_modified ON UPDATE CURRENT_TIMESTAMP`（见注） |

> **注**：`schema.sql` 中 `gmt_modified` 定义为 `DEFAULT CURRENT_TIMESTAMP`，未含 `ON UPDATE CURRENT_TIMESTAMP`。系分设计未明确要求自动更新，实现通过代码 `buildTodoItemDO:75` 手动设置 `gmtModified=now`，逻辑闭环，无功能缺陷。

**结论**：F01 功能全覆盖，所有业务规则（R01–R04）均已实现，错误码与系分完全对应。

---

## 4. Step 3 — 可读性检查

| 结果 | 说明 |
|------|------|
| ✅ | A1–A7 全部通过。脚本预扫无 A 类命中。所有文件编码 UTF-8、K&R 大括号、4 空格缩进、行宽 ≤120 字符、import 分组有序、命名规范（UpperCamelCase/lowerCamelCase/UPPER_SNAKE_CASE）、public 类与方法均含 Javadoc、无通配符 import、无 Tab。 |

> **仅 P2 备注**：`TodoServiceImpl.java` 中私有方法名 `buildTodoItemDO`、`convertToDTO` 中 "DO"/"DTO" 为全大写缩写，按阿里巴巴规约 A4.2/A4.3 建议 `buildTodoItemDo` / `convertToDto`。但 Java 社区约定俗成 DO/DTO 为大写缩写，不影响可读性，建议 P2 级别保留现状。

---

## 5. Step 4 — 可靠性检查

### 5.1 脚本预扫结果

`scan-all-rules.sh` 扫描 `src/main/java/com/example/todo src/test/java/com/example/todo`：

```
[P1] M016 — JavaTimeDefaultTimeZone: src/main/java/com/example/todo/service/impl/TodoServiceImpl.java:69
```

**覆盖范围**：52/222 条规则（B:25, M:6, I:2, A:8, S:7, G:4），P0=0, P1=1, P2=0。

### 5.2 综合可靠性结论

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|------|
| 可靠性 G1–G17 | `reliability-checklist.md` | ⚠️ | P1/P2 | 详见 5.3 |
| 安全 S1–S10 | `security-checklist.md` | ✅ | — | 详见 5.4 |
| Bug 模式 B/M/I | `bug-pattern-checklist.md` | ⚠️ | P1 | M016 命中（脚本已报）；其余 119 条无命中 |

### 5.3 可靠性明细

| ID | 结果 | 说明 |
|----|------|------|
| G1.1–G1.4 | N/A | 创建操作为独立 INSERT，无读后写/锁竞争场景 |
| G2.1 | P2 | 创建接口无幂等键；本期为内部使用的最小闭环，无重试/MQ，风险可控 |
| G2.2–G2.3 | N/A | 无重试/定时任务/MQ |
| G3.1–G3.2 | N/A | 单库单表 INSERT，无分布式事务；无 `@Transactional` 冗余 |
| G4.1–G4.3 | N/A | 仅简单 INSERT，无复杂查询；索引列无函数/隐式转换 |
| G5.1 | N/A | 无 MQ |
| G6.1–G6.2 | N/A | 无缓存 |
| G7.1–G7.2 | N/A | 无调度任务 |
| G8.1 | ✅ | `GlobalExceptionHandler` 覆盖 BusinessException / MethodArgumentNotValidException / DataAccessException / Exception 四级异常，各路径均有日志输出 |
| G8.2 | ✅ | 核心链路仅依赖 MySQL，无外部 RPC |
| G8.3 | N/A | 无 I/O 流/连接/锁 |
| G8.4–G8.6 | N/A | 无自定义线程池 |
| G9.1 | ✅ | DB 异常由 `DataAccessException` 统一捕获（`GlobalExceptionHandler.java:71-76`）并返回 TODO_500 |
| G9.2 | ✅ | HikariCP `connection-timeout: 3000`（`application.yml:16`）; MySQL 连接串未设 `socketTimeout`，3s 连接超时 + 30min max-lifetime 对内部低并发场景足够 |
| G9.3 | N/A | 无重试逻辑 |
| G10.1 | ✅ | ApiResponse.data 在错误时为 null，语义清晰 |
| G10.2 | N/A | 首版，无版本兼容需求 |
| G11.1 | ✅ | 5 个测试方法（执行 7 用例），含断言（assertThat/assertThatThrownBy） |
| G11.2 | P2 | 缺少 Controller 层 `@WebMvcTest` 切片测试（姓名/描述边界值校验与错误码映射），impl.md 已自述 |
| G11.3 | ✅ | tenantId 空值校验（`TodoServiceImpl.java:43`） |
| G11.4 | N/A | 无数值运算 |
| G12.1–G12.2 | N/A | 非资损场景 |
| G13.1 | ✅ | 日志级别：业务校验 WARN、系统异常 ERROR、成功 INFO（含 `isInfoEnabled` 守卫） |
| **G14.2** | **P1** | **租户 ID 来自用户可控请求头 `X-Tenant-Id`**（`TodoController.java:51`）。系分设计要求从登录上下文获取（§6.4.2.1），实现因无认证组件（系分假设 A01）以请求头透传占位。内部用户场景风险可控，后续接入认证后仅需替换取值方式 |
| **G14.3** | **P1** | **`LocalDateTime.now()` 未指定时区**（`TodoServiceImpl.java:69`）。不同部署环境的 JVM 默认时区可能导致时间不一致。建议改用 `LocalDateTime.now(ZoneId.of("Asia/Shanghai"))` 或 `Instant.now()` + UTC 存储（与 `application.yml` `serverTimezone=Asia/Shanghai` 对齐） |
| G14.4 | ✅ | `DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")` 用于格式化 `LocalDateTime`，该类型不含时区信息，无需指定 ZoneId |
| G15.1–G15.3 | N/A | 首版全新表与接口，无兼容性需求 |
| G16.1 | P2 | 代码无埋点/指标（调用量、成功率、耗时），系分 §7.1 已规划但本期未实现，内部小闭环可接受 |
| G16.2 | P2 | `GlobalExceptionHandler.handleMethodArgumentNotValidException`（第 61 行）日志仅含 field + message，缺少 tenantId 等请求上下文，排障定位困难 |
| G16.3 | ✅ | 日志级别正确 |
| G16.4 | ✅ | 无空 catch / printStackTrace |
| G17.1–G17.3 | P2 | 无功能开关/降级预案，系分 §7.3 已规划但本期未实现 |

### 5.4 安全明细

| ID | 结果 | 说明 |
|----|------|------|
| S1.1 | ✅ | SQL 全部 `#{}` 参数化（`TodoItemMapper.xml:9`），无 `${}` |
| S1.2–S1.3 | N/A | 无动态排序/表名/LIKE/IN |
| S2.1–S2.3 | N/A | API 纯 JSON 后端，无 HTML 渲染 |
| S3.1–S3.3 | N/A | 无外部 URL 请求 |
| S4.1–S4.2 | N/A | 无命令执行 |
| S5.1–S5.2 | N/A | 无 XML 解析 |
| S6.1–S6.3 | N/A | 无自定义反序列化 |
| S7.1–S7.3 | N/A | 无文件上传/下载 |
| S8.1 | P2 | 本期无认证（系分假设 A01），租户由请求头透传。内部用户闭环可接受 |
| S8.2 | ✅ | 创建使用 POST |
| S8.3 | P2 | 自增 ID 可预测；内部工具，非安全敏感场景 |
| S8.4 | N/A | 无 Cookie 操作 |
| **S9.1** | ✅ | 数据库账号密码通过环境变量 `${DB_USERNAME}` / `${DB_PASSWORD}` 注入（`application.yml:11-12`），未硬编码 |
| S9.2 | ✅ | 日志仅输出 name/tenantId（设计确认为普通数据），无密码/token 等敏感信息 |
| S9.3–S9.4 | N/A | 内部服务，无传输加密/随机数需求 |
| S10.1–S10.3 | N/A | API 后端，无 CSRF/CORS/跳转场景 |

---

## 6. Step 5 — 自定义扩展检查

| 域 | 参考 | 结果 | 说明 |
|----|------|------|------|
| 自定义扩展 | `customized-checklist.md` | N/A(未启用自定义规则) | `customized-checklist.md` 仅含示例项 U1.1，无项目级自定义规则 |

---

## 7. 结论

- **合并建议**：修复后合并（P1 项 2 条，P2 项 5 条）
- **P0**：无
- **P1**：
  1. M016 / G14.3 — `LocalDateTime.now()` 未指定时区：`TodoServiceImpl.java:69`
  2. G14.2 — 租户 ID 来自用户可控请求头：`TodoController.java:51`
- **P2**：
  1. G2.1 — 创建接口无幂等键
  2. G11.2 — 缺少 Controller 层切片测试
  3. G16.1 — 无监控埋点
  4. G16.2 — 参数校验异常日志缺少请求上下文
  5. G17.1 — 无功能开关
- **一句话**：功能完整覆盖系分全部业务规则与错误码，代码规范良好；两项 P1 为已知设计权衡（租户透传方式）和时区一致性隐患，建议接入认证后修复租户方案、补充显式时区。

---

## 7.1 问题片段

### P1 问题

- **P1** `M016` / `G14.3` `src/main/java/com/example/todo/service/impl/TodoServiceImpl.java:69` — `LocalDateTime.now()` 未显式指定时区，依赖 JVM 默认时区，跨环境部署时可能存在时间不一致。

  片段范围：`src/main/java/com/example/todo/service/impl/TodoServiceImpl.java:66-77`

```java
L66|     * @return 待办事项数据对象
L67|     */
L68|    private TodoItemDO buildTodoItemDO(CreateTodoRequest request, String tenantId) {
L69|        LocalDateTime now = LocalDateTime.now();
L70|        TodoItemDO todoItemDO = new TodoItemDO();
L71|        todoItemDO.setTenantId(tenantId);
L72|        todoItemDO.setName(request.getName());
L73|        todoItemDO.setDescription(request.getDescription());
L74|        todoItemDO.setGmtCreate(now);
L75|        todoItemDO.setGmtModified(now);
L76|        return todoItemDO;
L77|    }
```

- **P1** `G14.2` `src/main/java/com/example/todo/controller/TodoController.java:51` — 租户 ID 从请求头 `X-Tenant-Id` 获取（用户可控），系分设计要求从登录上下文获取。本期因无认证组件采用占位方案。

  片段范围：`src/main/java/com/example/todo/controller/TodoController.java:49-58`

```java
L49|    @PostMapping
L50|    public ApiResponse<TodoItemDTO> createTodo(@Valid @RequestBody CreateTodoRequest request,
L51|            @RequestHeader(name = TENANT_ID_HEADER, required = false) String tenantId) {
L52|        if (LOGGER.isInfoEnabled()) {
L53|            LOGGER.info("新增待办事项请求, name:{}, tenantId:{}", request.getName(), tenantId);
L54|        }
L55|
L56|        TodoItemDTO todoItemDTO = todoService.createTodo(request, tenantId);
L57|        return ApiResponse.success(todoItemDTO);
L58|    }
```

---

## 8. 修复任务列表

### P0

- 无待修复项。

### P1

- [ ] **P1** `src/main/java/com/example/todo/service/impl/TodoServiceImpl.java:69` — 将 `LocalDateTime.now()` 改为 `LocalDateTime.now(ZoneId.of("Asia/Shanghai"))`，与 `application.yml` 时区配置对齐
- [ ] **P1** `src/main/java/com/example/todo/controller/TodoController.java:51` — 接入统一认证拦截器后，将租户 ID 取值从 `@RequestHeader("X-Tenant-Id")` 改为从 `SecurityContext` 获取

### P2

- [ ] **P2** `G2.1` — 评估是否为创建接口引入幂等键（如客户端生成 requestId），防止网络重试导致重复创建
- [ ] **P2** `G11.2` — 补充 `TodoController` 切片测试（`@WebMvcTest` + MockMvc），覆盖 name/description 边界值与错误码映射
- [ ] **P2** `G16.1` — 接入监控体系，为 POST /api/todo 添加调用量/成功率/耗时埋点
- [ ] **P2** `G16.2` — `GlobalExceptionHandler.java:61` 日志补充 tenantId 等请求上下文信息
- [ ] **P2** `G17.1` — 为创建接口添加配置中心开关，支持紧急关闭