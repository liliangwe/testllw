# Code Review Report

> **Change** 日常待办事项 — 核心功能（新增待办事项） · **分支** `AI/task-DEV-966dcd0a` · **Commit** `72bd56a..caeaa97` · **日期** 2026-10-14 · **审查者** AI
>
> **AI**：等级 **P0 / P1 / P2**；G/S 以 checklist 行内定义为准；Bug 模式以 `bug-pattern-checklist.md` 表头为准（Blocker→P0、Major→P1、Info→P2）。预扫已通过 `scan-all-rules.sh` 执行，结果并入 §5。

---

## 1. 审查范围

| 项 | 值 |
|----|-----|
| `.java` 文件数 | 10 |
| 变更行数 | `+864 / -0` |

| 类/接口 | 路径 | 角色 |
|---------|------|------|
| `TodoApplication` | `src/main/java/com/antdigital/todo/TodoApplication.java` | Spring Boot 启动类 |
| `ApiResponse` | `src/main/java/com/antdigital/todo/common/ApiResponse.java` | 统一 API 响应体 |
| `TodoController` | `src/main/java/com/antdigital/todo/controller/TodoController.java` | REST 控制器 |
| `TodoItemMapper` | `src/main/java/com/antdigital/todo/dao/mapper/TodoItemMapper.java` | MyBatis Mapper 接口 |
| `TodoCreateRequest` | `src/main/java/com/antdigital/todo/model/dto/TodoCreateRequest.java` | 请求 DTO（含 JSR-303 校验） |
| `TodoItemVO` | `src/main/java/com/antdigital/todo/model/dto/TodoItemVO.java` | 视图对象 |
| `TodoItemDO` | `src/main/java/com/antdigital/todo/model/entity/TodoItemDO.java` | 数据对象 |
| `TodoService` | `src/main/java/com/antdigital/todo/service/TodoService.java` | 业务服务接口 |
| `TodoServiceImpl` | `src/main/java/com/antdigital/todo/service/impl/TodoServiceImpl.java` | 业务服务实现 |
| `TodoServiceImplTest` | `src/test/java/com/antdigital/todo/service/impl/TodoServiceImplTest.java` | 单元测试 |

---

## 2. 问题计数

| P0 | P1 | P2 |
|----|----|-----|
| 1 | 3 | 2 |

---

## 3. Step 2 — 功能（REQ）

> 来源：`design.md` §5.1.2/§5.1.3.1

### REQ-F01: 新增待办事项

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 正常路径：有效请求返回 TodoItemVO 含 id/todoName/description/gmtCreate | ✅ | design.md §5.1.2 出参定义 | `TodoController.java:37-42` / `TodoServiceImpl.java:31-59` / `TodoServiceImplTest.java:38-65` | 正常路径实现与 spec 一致 |
| R01: todoName 为空/null/blank 返回 PARAM_INVALID | ⚠️ | design.md §5.1.2 业务规则 R01: "返回 PARAM_INVALID，提示'事项名称不能为空'" | `TodoCreateRequest.java:12` (@NotBlank) / `TodoServiceImpl.java:33-34` | Controller 层 `@Valid` + `@NotBlank` 可拦截；Service 层 has null/isBlank 兜底 ✅。但见下方 P0 |
| R02: todoName 超 200 字符返回 PARAM_INVALID | ❌ | design.md §5.1.2 业务规则 R02: "返回 PARAM_INVALID，提示'事项名称不能超过200字符'" | `TodoCreateRequest.java:13` (@Size(max=200)) | **P0 问题**：`@Valid` 校验失败会抛出 `MethodArgumentNotValidException`，在 Controller 方法体**外部**触发，无法被 try-catch 捕获。实际返回的是 Spring 默认 400 响应（非 ApiResponse 格式），违反 spec 要求 |
| R03: description 超 2000 字符返回 PARAM_INVALID | ❌ | design.md §5.1.2 业务规则 R03: "返回 PARAM_INVALID，提示'事项描述不能超过2000字符'" | `TodoCreateRequest.java:17` (@Size(max=2000)) | 同上 P0 问题 |
| 响应格式：`{result, msg, data}` 包裹 | ✅ | design.md §5.1.2 出参定义 | `ApiResponse.java:20-26` (ok) / `TodoController.java:42` | 正常路径返回格式正确 |
| 数据库写入：使用 MyBatis #{} 预编译 INSERT | ✅ | design.md §5.1.1 DDL | `TodoItemMapper.xml:13-17` | `useGeneratedKeys=true` + `#{}` 参数化 ✅ |
| gmtCreate 格式化为 yyyy-MM-dd HH:mm:ss | ✅ | design.md §5.1.2 出参 | `TodoServiceImpl.java:22,58` | `DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")` ✅ |

### P0 详述：JSR-303 校验异常处理缺失

**问题**：Controller 仅在方法体内 try-catch `IllegalArgumentException` 和 `Exception`。但 `@Valid` 触发的 JSR-303 校验在 Spring 参数绑定时执行，失败时抛出 `MethodArgumentNotValidException`（或其子类），发生在进入方法体**之前**，因此：

- R02（名称超长）、R03（描述超长）的校验失败 → Spring 返回默认 400 响应，**不是** `ApiResponse` 格式
- R01（名称为空）的 `@NotBlank` 校验同样受影响（但 Service 层有 null/isBlank 兜底，Short-Circuit 场景下 Controller 的 `@NotBlank` 会先触发）

**Spec 证据**：design.md §5.1.2 明确要求 PARAM_INVALID 错误码 + `ApiResponse` 格式。

**修复建议**：添加 `@ExceptionHandler(MethodArgumentNotValidException.class)` 或全局 `@ControllerAdvice`，将校验异常转为 `ApiResponse.fail("PARAM_INVALID", ...)`。

---

## 4. Step 3 — 可读性检查

| ID | 检查项 | 结果 | 备注 |
|----|--------|------|------|
| A1 | 源文件格式 | ✅ | 所有文件 UTF-8、无 Tab、文件名与类名一致 |
| A2 | 源文件结构/import 顺序 | ✅ | 无 `import *`，按 ASCII 字典序排列 |
| A3 | 代码样式 | ✅ | K&R 大括号、4 空格缩进、行宽 ≤ 120 |
| A4 | 命名规范 | ✅ | 包名全小写、类名 UpperCamelCase、方法/字段 lowerCamelCase、测试类 `TodoServiceImplTest` |
| A5 | 编码实践 | ✅ | `@Override` 使用正确、无空 catch、无 `finalize()` |
| A6 | 特定元素样式 | ✅ | 修饰符顺序正确、无 C 风格数组、long 无小写 `l` |
| A7 | Javadoc 规范 | ⚠️ P2 | `ApiResponse.java` — public static 工厂方法 `ok()` 和 `fail()` 缺少 Javadoc（A7.1：public 成员须有 Javadoc） |

---

## 5. Step 4 — 可靠性检查

### 5.1 自动化预扫结果（`scan-all-rules.sh`）

```
=== Step 4 Rule Scan (B/M/I + A/S/G) ===
Engine: ripgrep — 52/222 rules scanned

[P0] G16.2 — CatchWithoutLogging: TodoController.java:43
[P0] G16.2 — CatchWithoutLogging: TodoController.java:46
[P1] M016 — JavaTimeDefaultTimeZone: TodoServiceImpl.java:45
[P1] M016 — JavaTimeDefaultTimeZone: TodoServiceImpl.java:46

=== Summary: 4 findings (P0=2, P1=2, P2=0) | 52/222 rules scanned ===
```

**LLM 复核结论**：

| 预扫命中 | 预扫等级 | 复核等级 | 复核说明 |
|----------|---------|---------|---------|
| G16.2 @ `TodoController.java:43` | P0 | **P1** | catch 块**有**日志（`logger.warn("参数校验失败: {}", e.getMessage())`），但仅输出了 `e.getMessage()` 未传递异常对象，丢失堆栈信息。非空 catch，降级为 P1 |
| G16.2 @ `TodoController.java:46` | P0 | **误报** | catch 块内 `logger.error("创建待办事项失败", e)` 已正确传递异常对象 `e`，日志完整。脚本误报，忽略 |
| M016 @ `TodoServiceImpl.java:45` | P1 | **P1** | `LocalDateTime.now()` 未指定 `ZoneId`，使用系统默认时区。在多时区部署场景下可能导致时间不一致 |
| M016 @ `TodoServiceImpl.java:46` | P1 | **P1** | 同上（`gmtModified` 赋值） |

### 5.2 可靠性（`reliability-checklist.md`）

| ID | 检查项 | 结果 | 等级 | 备注 |
|----|--------|------|------|------|
| G1.1–G1.4 | 并发控制 | N/A | — | 纯 INSERT 无读后写场景 |
| G2.1–G2.3 | 幂等拦截 | N/A | — | 每次创建为新记录，无重复提交风险（后续若有业务幂等需求需补） |
| G3.1–G3.2 | 事务控制 | ✅ | — | 单条 INSERT，无分布式事务，无事务内外部 I/O |
| G4.1–G4.3 | SQL与索引 | ✅ | — | 简单 INSERT，`#{}` 预编译，索引 `idx_todo_item_gmt_create` 合理 |
| G5.1 | 消息(MQ) | N/A | — | 无 MQ |
| G6.1–G6.2 | 缓存 | N/A | — | 无缓存 |
| G7.1–G7.2 | 调度任务 | N/A | — | 无定时任务 |
| G8.1–G8.6 | 防御编程 | ✅ | — | catch 均有日志/返回，无 ThreadLocal，无自定义线程池 |
| G9.1–G9.3 | 网络调用 | N/A | — | 无外部 RPC/HTTP 调用（DB 由 MyBatis 连接池管理） |
| G10.1–G10.2 | 接口契约 | ✅ | — | 字段含义明确，无 null 歧义 |
| G11.1–G11.4 | 开发自测 | ✅ | — | 7 个测试方法覆盖正常路径 + null/empty/blank + 边界值 200/2000，断言充分 |
| G12.1–G12.2 | 资损防控 | N/A | — | 不涉及资金 |
| G13.1 | 监控核对 | ✅ | — | 日志级别正确：INFO（正常）、WARN（参数校验）、ERROR（系统异常） |
| G14.1–G14.4 | 国际化/多租户/时区 | ⚠️ | P1 | M016 命中：`LocalDateTime.now()` 无显式 ZoneId（§5.1） |
| G15.1–G15.3 | 可灰度 | ✅ | — | 新增表为纯增量 DDL（`CREATE TABLE IF NOT EXISTS`），向前兼容 |
| G16.1 | 核心链路关键指标埋点 | ⚠️ | P1 | 代码中无 Metrics 埋点（QPS/RT/错误率），spec §7.1 有要求但未落地 |
| G16.2 | 异常路径日志 + traceId/bizId | ⚠️ | P1 | Controller 第 43 行 catch 仅输出 `e.getMessage()`，未传递异常对象（§5.1） |
| G16.3–G16.4 | 日志级别 / 空 catch | ✅ | — | 无空 catch，级别正确 |
| G17.1 | 功能开关支持紧急关闭 | ⚠️ | P1 | design.md §7.3 要求 `todo.create.enabled` 开关，代码中未实现 |
| G17.2–G17.3 | 降级预案 / 回滚 | N/A | — | 最小闭环，纯增量功能，回滚仅需下线部署包 |

### 5.3 安全（`security-checklist.md`）

| ID | 检查项 | 结果 | 备注 |
|----|--------|------|------|
| S1.1–S1.3 | SQL 注入 | ✅ | MyBatis XML 全部使用 `#{}` 预编译，无 `${}` |
| S2.1–S2.3 | XSS | N/A | 后端 API，无 HTML/JS 输出 |
| S3.1–S3.3 | SSRF | N/A | 无外部 URL 请求 |
| S4.1–S4.2 | 命令执行 | N/A | 无系统命令执行 |
| S5.1–S5.2 | XXE | N/A | 无 XML 解析 |
| S6.1–S6.3 | 反序列化 | N/A | 无自定义反序列化 |
| S7.1–S7.3 | 文件上传/下载 | N/A | 无文件操作 |
| S8.1–S8.4 | 访问控制 | ✅ | design.md §6.4 明确登录态由网关/拦截器处理，本模块不单独实现 |
| S9.1–S9.4 | 数据安全 | ✅ | DB 密码使用 `${DB_PASSWORD:}` 环境变量占位，无硬编码密钥 |
| S10.1–S10.3 | CSRF/CORS/跳转 | N/A | 内部 API，无浏览器直接调用场景 |

### 5.4 Bug 模式（汇总）

> 仅 M016 命中（已通过预扫捕获并在 §5.2 中说明）。其余 B001–B081 / M001–M027 / I001–I010 经 LLM 逐条复核，与当前变更无关，均标 N/A（本次变更为简单 CRUD，不涉及集合操作、异常处理模式、并发结构等）。

---

## 6. Step 5 — 自定义扩展检查

| ID | 检查项 | 结果 | 备注 |
|----|--------|------|------|
| U1.1 | Controller 入参使用统一校验注解（`@Valid`） | ✅ | `TodoController.java:37` 已使用 `@Valid` |
| U1.2–U2.x | 其余自定义规则 | N/A | `customized-checklist.md` 仅含 U1.1 示例项，其余未启用 |

---

## 7. 结论

- **合并建议**：**修复后合并**（1 个 P0 阻塞项需修复）
- **P0**：
  1. `TodoController` 缺少 JSR-303 校验异常处理器 — `@Valid` 校验失败时 `MethodArgumentNotValidException` 无法被方法内 try-catch 捕获，导致返回 Spring 默认 400 而非 spec 要求的 `ApiResponse("PARAM_INVALID", ...)`
- **P1**：
  1. G16.2 `TodoController.java:43` — catch 块仅输出 `e.getMessage()` 未传递异常对象，丢堆栈
  2. M016 `TodoServiceImpl.java:45-46` — `LocalDateTime.now()` 未指定 `ZoneId`，多时区部署场景有风险
  3. G16.1 缺少核心链路 Metrics 埋点（spec §7.1 要求）
  4. G17.1 未实现 `todo.create.enabled` 功能开关（spec §7.3 要求）
- **P2**：
  1. A7 `ApiResponse.java` — `ok()` 和 `fail()` 缺少 Javadoc
- **一句话**：核心功能实现正确，但 JSR-303 校验异常未被 Controller 捕获导致错误响应格式不符合 spec，属于功能级阻塞问题；另有 3 个 P1 可观测性/时区改进项。

---

## 7.1 问题片段（必填）

### P0 — JSR-303 校验异常处理缺失

- **P0** `REQ-F01` `src/main/java/com/antdigital/todo/controller/TodoController.java:36-50` — `MethodArgumentNotValidException` 在参数绑定时抛出，发生在 try-catch 外部，无法被捕获。

```java
L36|    @PostMapping("/create")
L37|    public ApiResponse<TodoItemVO> create(@Valid @RequestBody TodoCreateRequest request) {
L38|        logger.info("收到新增待办事项请求, todoName: {}", request.getTodoName());
L39|
L40|        try {
L41|            TodoItemVO vo = todoService.createTodo(request);
L42|            return ApiResponse.ok(vo);
L43|        } catch (IllegalArgumentException e) {
L44|            logger.warn("参数校验失败: {}", e.getMessage());
L45|            return ApiResponse.fail("PARAM_INVALID", e.getMessage());
L46|        } catch (Exception e) {
L47|            logger.error("创建待办事项失败", e);
L48|            return ApiResponse.fail("SYSTEM_ERROR", "系统内部错误");
L49|        }
L50|    }
```

**问题说明**：当 `@Valid` 检测到 `@NotBlank`/`@Size` 违规时，Spring 在进入 `create()` 方法体之前就抛出 `MethodArgumentNotValidException`，此时 L40–L49 的 try-catch 尚未生效。结果：R01/R02/R03 校验失败时返回的是 Spring 默认 400 JSON（非 `ApiResponse` 格式），不符合 design.md §5.1.2 要求的 `{result:"PARAM_INVALID", msg:"..."}`。

**修复方向**：添加全局 `@ControllerAdvice` 或 Controller 内 `@ExceptionHandler(MethodArgumentNotValidException.class)`，提取 `BindingResult` 中的 `message` 并包装为 `ApiResponse.fail("PARAM_INVALID", ...)`。

---

### P1 — catch 未传递异常对象

- **P1** `G16.2` `src/main/java/com/antdigital/todo/controller/TodoController.java:43-45` — catch 仅输出 `e.getMessage()`，丢堆栈。

```java
L43|        } catch (IllegalArgumentException e) {
L44|            logger.warn("参数校验失败: {}", e.getMessage());
L45|            return ApiResponse.fail("PARAM_INVALID", e.getMessage());
```

**问题说明**：仅记录 `e.getMessage()` 而未将 `e` 作为 Throwable 传递给 logger，线上排查时无法看到完整调用链。

---

### P1 — LocalDateTime.now() 无 ZoneId

- **P1** `M016` `src/main/java/com/antdigital/todo/service/impl/TodoServiceImpl.java:45-46` — 未指定时区。

```java
L44|        todoItem.setDescription(request.getDescription());
L45|        todoItem.setGmtCreate(LocalDateTime.now());
L46|        todoItem.setGmtModified(LocalDateTime.now());
```

**问题说明**：`LocalDateTime.now()` 使用 JVM 默认时区。虽然 `application.yml` 中 JDBC URL 指定了 `serverTimezone=Asia/Shanghai`，但应用层时间与 DB 层可能不一致。建议使用 `LocalDateTime.now(ZoneId.of("Asia/Shanghai"))` 或统一使用 `Instant`/UTC。

---

## 8. 修复任务列表

### P0

- [ ] **P0** `src/main/java/com/antdigital/todo/controller/TodoController.java:36` — 添加 `@ExceptionHandler(MethodArgumentNotValidException.class)` 或全局 `@ControllerAdvice`，将 JSR-303 校验异常转为 `ApiResponse.fail("PARAM_INVALID", fieldErrorMessage)`

### P1

- [ ] **P1** `src/main/java/com/antdigital/todo/controller/TodoController.java:44` — 将 `logger.warn("参数校验失败: {}", e.getMessage())` 改为 `logger.warn("参数校验失败", e)`，保留异常堆栈
- [ ] **P1** `src/main/java/com/antdigital/todo/service/impl/TodoServiceImpl.java:45-46` — `LocalDateTime.now()` 改为 `LocalDateTime.now(ZoneId.of("Asia/Shanghai"))` 或统一时区策略
- [ ] **P1** `G16.1` — 为 `POST /api/todo/create` 添加 Metrics 埋点（调用次数、成功/失败计数、P99 耗时），对齐 design.md §7.1
- [ ] **P1** `G17.1` — 实现 `todo.create.enabled` 配置开关，紧急情况下可关闭创建能力，对齐 design.md §7.3

### P2

- [ ] **P2** `src/main/java/com/antdigital/todo/common/ApiResponse.java:20,28` — 为 `ok()` 和 `fail()` 添加 Javadoc