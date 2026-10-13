# Code Review Report

> **Change** 待办事项管理-核心功能 · **分支/Commit** `AI/task-DEV-...` / `1defc33` · **日期** 2026-10-13 · **审查者** AI
>
> **AI**：等级 **P0 / P1 / P2**；G/S 以 checklist 行内定义为准；Bug 模式以 `bug-pattern-checklist.md` 表头为准（Blocker→P0、Major→P1、Info→P2）。已运行 `scan-all-rules.sh` 并将结果并入 §5。

---

## §1 审查范围

| 项 | 值 |
|----|-----|
| `.java` 文件数 | 13 |
| 变更行数 | `+953 / -0`（新增文件） |

| 类/接口 | 路径 | 角色 |
|---------|------|------|
| `TodoApplication` | `src/main/java/com/example/todo/TodoApplication.java` | Spring Boot 启动类 |
| `BizException` | `src/main/java/com/example/todo/common/BizException.java` | 业务异常 |
| `Result<T>` | `src/main/java/com/example/todo/common/Result.java` | 统一响应结构 |
| `TodoController` | `src/main/java/com/example/todo/controller/TodoController.java` | REST 控制器 |
| `TodoCreateRequestDTO` | `src/main/java/com/example/todo/dto/TodoCreateRequestDTO.java` | 创建请求 DTO |
| `TodoCreateResultDTO` | `src/main/java/com/example/todo/dto/TodoCreateResultDTO.java` | 创建结果 DTO |
| `TodoItemDO` | `src/main/java/com/example/todo/entity/TodoItemDO.java` | 数据对象 |
| `TodoStatus` | `src/main/java/com/example/todo/entity/TodoStatus.java` | 状态枚举 |
| `TodoItemMapper` | `src/main/java/com/example/todo/mapper/TodoItemMapper.java` | MyBatis Mapper |
| `TodoService` | `src/main/java/com/example/todo/service/TodoService.java` | Service 接口 |
| `TodoServiceImpl` | `src/main/java/com/example/todo/service/impl/TodoServiceImpl.java` | Service 实现 |
| `TodoControllerTest` | `src/test/java/com/example/todo/controller/TodoControllerTest.java` | Controller 单元测试 |
| `TodoServiceImplTest` | `src/test/java/com/example/todo/service/TodoServiceImplTest.java` | Service 单元测试 |

---

## §2 问题计数

| P0 | P1 | P2 |
|----|----|-----|
| 2 | 4 | 3 |

---

## §3 Step 2 — 功能（REQ）

> REQ 来源：`design.md` §1 需求与范围 / §5.1.2 接口详细设计

### REQ-F01: 新增待办事项（POST /api/todo/create）

| Scenario | 结果 | Spec 证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 正常创建返回结果（含 id/name/desc/status/gmtCreate） | ✅ | §5.1.2 W01 出参 data 结构 | `TodoController.java:42-63` / `TodoServiceImpl.java:41-58` | 链路完整 |
| name 为空/纯空白 → 返回 TODO_001 | ❌ | §5.1.2 错误码 TODO_001 | — | **`@Valid` 拦截后返回 Spring 默认 400，非 TODO_001** |
| name > 200 字符 → 返回 TODO_002 | ❌ | §5.1.2 错误码 TODO_002 | — | **同上，`@Valid` 先于 Service 校验触发** |
| description > 2000 字符 → 返回 TODO_003 | ❌ | §5.1.2 错误码 TODO_003 | — | **同上** |
| status 默认 PENDING | ✅ | §5.1.1 表结构 status DEFAULT 'PENDING' | `TodoServiceImpl.java:85` / `schema.sql:6` | — |
| DB 异常 → 返回 TODO_999 | ✅ | §5.1.3.1 异常场景 | `TodoServiceImpl.java:54-56` / `TodoController.java:57-61` | — |
| 幂等性（相同参数不产生脏数据） | ❌ | §1 约束"接口幂等性考虑" | 无去重/幂等键实现 | **每次请求创建新记录** |

### REQ-F01 关键缺陷分析

**缺陷 #1（P0）：`@Valid` 注解与 Service 层手动校验冲突**

`TodoController.java:44` 使用了 `@Valid`，DTO 上标注了 `@NotBlank` / `@Size`，但 Controller 的 try-catch 仅捕获 `BizException` 和 `Exception`。当 `@Valid` 校验失败时，Spring 抛出 `MethodArgumentNotValidException`，**该异常在进入 Controller 方法体之前即被 Spring DispatcherServlet 处理**，Controller 内的 try-catch 根本无法拦截，最终返回 Spring 默认 400 错误格式（非本项目 `{code, msg, data}` 结构），且错误码丢失。

**缺陷 #2（P0）：幂等性未实现**

设计文档 §1 明确要求"接口幂等性考虑（相同参数重复调用不应产生脏数据）"。当前实现无任何去重机制（如唯一约束、幂等键表、请求 ID），相同参数每次调用均创建独立记录，违反设计规格。

---

## §4 Step 3 — 可读性检查

| ID | 结果 | 定位 | 说明 |
|----|------|------|------|
| A2.2 | ❌ P2 | `TodoServiceImplTest.java:18,20` | `import static org.junit.jupiter.api.Assertions.*` / `import static org.mockito.Mockito.*` — 通配符导入 |
| A2.2 | ❌ P2 | `TodoControllerTest.java:19,21` | 同上 |
| A4.1 | ✅ | 全部 | 包名全小写：`com.example.todo.*` |
| A4.2 | ✅ | 全部 | 类名 UpperCamelCase |
| A4.3 | ✅ | 全部 | 方法名 lowerCamelCase |
| A4.4 | ✅ | `TodoServiceImpl.java:30-31` | 常量 `NAME_MAX_LENGTH` / `DESCRIPTION_MAX_LENGTH` 使用 UPPER_SNAKE_CASE |
| A5.1 | ✅ | `TodoServiceImpl.java:39` | `@Override` 标注正确 |
| A7.1 | ✅ | 全部 public 类/方法 | 均有 Javadoc |
| A1–A7 其余项 | ✅ | — | 无违规：无 Tab、K&R 大括号、4 空格缩进、≤120 字符行宽 |

---

## §5 Step 4 — 可靠性检查

### 可靠性（G1–G17）

| 域 | 结果 | 等级 | 说明 |
|----|------|------|------|
| G1 并发控制 | N/A | — | 纯新增操作，无共享数据竞争 |
| G2 幂等拦截 | ❌ | P0 | **G2.1**: 写接口无幂等键。design.md §1 明确要求幂等性，未实现 |
| G3 事务控制 | ✅ | — | `@Transactional(rollbackFor=Exception.class)` 正确，无外部 I/O 在事务内 |
| G4 SQL 与索引 | ✅ | — | MyBatis 使用 `#{ }` 预编译，无 `${}` 拼接；schema.sql 索引与设计一致 |
| G5 消息(MQ) | N/A | — | 无 MQ |
| G6 缓存 | N/A | — | 无缓存 |
| G7 调度任务 | N/A | — | 无定时任务 |
| G8 防御编程 | ⚠️ | P2 | 见下方 G16.2 |
| G9 网络调用 | ❌ | P1 | **G9.2**: `application.yml` JDBC URL 未设置 `connectTimeout`/`socketTimeout` |
| G10 接口契约 | ✅ | — | 响应结构与设计一致 |
| G11 开发自测 | ✅ | — | 12 个测试方法覆盖正常/边界/异常路径，AAA 模式，断言齐全 |
| G12 资损防控 | N/A | — | 非资金场景 |
| G13 监控核对 | ✅ | — | 日志级别正确（info 成功/warn 业务异常/error 系统异常） |
| G14 国际化/多租户 | N/A | — | 单用户最小闭环 |
| G15 可灰度 | N/A | — | 纯新增无旧逻辑 |
| G16 可监控 | ❌/⚠️ | P1/P2 | 见下方明细 |
| G17 可应急 | ⚠️ | P2 | 设计 §7.3 提到功能开关 `todo.create.enabled`，代码未实现 |

**G16 可监控明细：**

| ID | 定位 | 结果 | 说明 |
|----|------|------|------|
| G16.1 | — | ❌ P1 | 设计 §7 定义了监控埋点（Counter/Histogram），代码未接入任何指标框架 |
| G16.2 | `TodoServiceImpl.java:52` | ⚠️ P2 | `catch (BizException e)` 仅 re-throw，无任何日志（脚本命中，复核确认）；而 `TodoController.java:51,57` 和 `TodoServiceImpl.java:54` 的 catch 块**均有日志输出**，脚本误报 |
| G16.3 | 全部 | ✅ | 日志级别正确 |

### 安全（S1–S10）

| 域 | 结果 | 等级 | 说明 |
|----|------|------|------|
| S1 SQL 注入 | ✅ | — | MyBatis XML 全部 `#{}`，无 `${}` |
| S8 访问控制 | N/A | — | design §6.4.2 明确排除登录认证 |
| S9 数据安全 | ❌ | P1 | **S9.1**: `application.yml:10` 数据库密码明文硬编码 `password: root` |

> S2–S7, S10 均 N/A（无 XSS/SSRF/命令执行/XXE/反序列化/文件上传/CSRF 场景）。

### Bug 模式（B/M/I）— `scan-all-rules.sh` + LLM 复核

| ID | 定位 | 等级 | 说明 |
|----|------|------|------|
| M016 | `TodoServiceImpl.java:86` | P1 | `LocalDateTime.now()` 依赖系统默认时区，应显式指定 `ZoneId`（如 `ZoneId.of("Asia/Shanghai")`） |

> 脚本扫描 52/222 条规则，其余 B/M/I 项经 LLM 核对未命中或 N/A。

---

## §6 Step 5 — 自定义扩展检查

| 域 | 结果 | 说明 |
|----|------|------|
| U1 团队工程约束 | ⚠️ P1 | **U1.1**: `TodoController.java:44` Controller 使用了 `@Valid` 但未配合全局异常处理器或 `@ExceptionHandler` 来正确映射校验错误码，导致 `@Valid` 的错误码信息丢失 |
| U2 业务红线 | N/A | 未启用自定义业务红线规则 |

---

## §7 结论

- **合并建议**：🔴 **阻止合并** — 存在 2 个 P0 阻塞问题
- **P0**：
  1. `@Valid` 校验失败时错误码丢失，返回 Spring 默认 400 而非 `{code:"TODO_00x"}` — `TodoController.java:44` → 需添加 `MethodArgumentNotValidException` 全局处理或移除 DTO 层 `@Valid` 由 Service 统一校验
  2. 幂等性未实现 — `TodoServiceImpl.java:41-58` → 需按 design.md §1 要求加入去重机制
- **P1**：4 项（M016 时区、G9.2 DB 超时、S9.1 明文密码、G16.1 缺失监控）
- **P2**：3 项（A2.2 通配符导入 ×4、G16.2 BizException re-throw 无 trace log、G17.1 功能开关未实现）
- **一句话**：核心功能代码链路正确、测试覆盖充分，但 `@Valid` 与 Service 校验的双层设计导致错误码丢失，且幂等性规格未落实，属于阻塞性问题。

---

## §7.1 问题片段

### P0-1: `@Valid` 导致错误码丢失

- **P0** `功能缺陷` `TodoController.java:42-62` — `@Valid` 校验失败时错误码 TODO_001/002/003 丢失，Spring 默认 400 响应格式与本项目 `{code,msg,data}` 不兼容。
  片段范围：`TodoController.java:42-62`

```java
L42|    @PostMapping("/create")
L43|    public ResponseEntity<Result<TodoCreateResultDTO>> createTodo(
L44|            @Valid @RequestBody TodoCreateRequestDTO request) {   // ← @Valid 拦截后抛 MethodArgumentNotValidException
L45|        long start = System.currentTimeMillis();
L46|        try {
L47|            TodoCreateResultDTO result = todoService.createTodo(request);
L48|            long elapsed = System.currentTimeMillis() - start;
L49|            log.info("POST /api/todo/create OK, elapsed={}ms, id={}", elapsed, result.getId());
L50|            return ResponseEntity.ok(Result.success(result));
L51|        } catch (BizException e) {                               // ← 不匹配 MethodArgumentNotValidException
L52|            long elapsed = System.currentTimeMillis() - start;
L53|            log.warn("POST /api/todo/create FAIL, code={}, msg={}, elapsed={}ms",
L54|                    e.getErrorCode(), e.getMessage(), elapsed);
L55|            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
L56|                    .body(Result.fail(e.getErrorCode(), e.getMessage()));
L57|        } catch (Exception e) {                                  // ← 进入此分支，但 Controller 方法体本身不会被调用
L58|            long elapsed = System.currentTimeMillis() - start;
L59|            log.error("POST /api/todo/create ERROR, elapsed={}ms", elapsed, e);
L60|            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
L61|                    .body(Result.fail("TODO_999", "系统内部错误"));
L62|        }
```

> **根因**：Spring MVC 的 `@Valid` 校验在 `DispatcherServlet` 参数解析阶段执行，校验失败时抛出 `MethodArgumentNotValidException`，该异常**在进入 Controller 方法体之前即被 Spring 默认异常处理器捕获**，Controller 内的 try-catch 根本不会执行。DTO 注解 message 中的错误码（`"TODO_001:事项名称不能为空"`）完全未被使用。

---

### P0-2: 幂等性缺失

- **P0** `G2.1` `TodoServiceImpl.java:41-58` — 创建接口无幂等键，相同参数每次产生新记录，违反 design.md §1"接口幂等性考虑（相同参数重复调用不应产生脏数据）"。
  片段范围：`TodoServiceImpl.java:40-57`

```java
L40|    @Transactional(rollbackFor = Exception.class)
L41|    public TodoCreateResultDTO createTodo(TodoCreateRequestDTO request) {
L42|        // 参数校验
L43|        validateRequest(request);
L44|
L45|        try {
L46|            TodoItemDO todoItem = buildTodoItem(request);
L47|            todoItemMapper.insert(todoItem);         // ← 无去重，直接 INSERT
L48|
L49|            log.info("待办事项创建成功, id={}, name={}", todoItem.getId(), todoItem.getName());
L50|
L51|            return buildResult(todoItem);
L52|        } catch (BizException e) {
L53|            throw e;
L54|        } catch (Exception e) {
L55|            log.error("待办事项创建失败, name={}", request.getName(), e);
L56|            throw new BizException("TODO_999", "系统内部错误");
L57|        }
L58|    }
```

> **根因**：未实现任何幂等机制（如请求 ID 去重表、数据库唯一约束、分布式锁），与设计规格矛盾。

---

### P1: M016 — 系统默认时区

- **P1** `M016` `TodoServiceImpl.java:86` — `LocalDateTime.now()` 依赖系统默认时区，应显式指定。
  片段范围：`TodoServiceImpl.java:81-89`

```java
L81|    private TodoItemDO buildTodoItem(TodoCreateRequestDTO request) {
L82|        TodoItemDO item = new TodoItemDO();
L83|        item.setName(request.getName().trim());
L84|        item.setDescription(request.getDescription());
L85|        item.setStatus(TodoStatus.PENDING.getCode());
L86|        LocalDateTime now = LocalDateTime.now();     // ← 无显式 ZoneId
L87|        item.setGmtCreate(now);
L88|        item.setGmtModified(now);
L89|        return item;
```

---

### P1: S9.1 — 明文数据库密码

- **P1** `S9.1` `src/main/resources/application.yml:9-10` — 数据库用户名/密码明文硬编码。
  片段范围：`application.yml:7-11`

```yaml
L7 |  datasource:
L8 |    url: jdbc:mysql://localhost:3306/todo_db?useSSL=false&...
L9 |    username: root
L10|    password: root                                  # ← 明文硬编码
L11|    driver-class-name: com.mysql.cj.jdbc.Driver
```

---

### P2: A2.2 — 通配符导入

- **P2** `A2.2` `TodoServiceImplTest.java:18,20` — `import static *`
- **P2** `A2.2` `TodoControllerTest.java:19,21` — `import static *`

---

## §8 修复任务列表

### P0

- [ ] **P0** `TodoController.java:42-62` — 移除 `@Valid`，由 `TodoServiceImpl.validateRequest()` 统一校验并返回正确错误码；或添加 `@ExceptionHandler(MethodArgumentNotValidException.class)` 解析 DTO 注解 message 中的错误码
- [ ] **P0** `TodoServiceImpl.java:41-58` — 按 design.md §1 要求实现幂等性：增加请求 ID 字段 + 数据库唯一约束/去重表，或通过业务键（name+description hash）防重复提交

### P1

- [ ] **P1** `M016` `TodoServiceImpl.java:86` — `LocalDateTime.now()` → `LocalDateTime.now(ZoneId.of("Asia/Shanghai"))`
- [ ] **P1** `G9.2` `application.yml:8` — JDBC URL 追加 `connectTimeout=3000&socketTimeout=5000`
- [ ] **P1** `S9.1` `application.yml:9-10` — 数据库密码改为环境变量/配置中心引用（如 `${DB_PASSWORD}`）
- [ ] **P1** `G16.1` `TodoController.java` — 接入 Micrometer/Prometheus 埋点，实现 design §7 定义的 `todo.create.count` / `todo.create.duration` 指标

### P2

- [ ] **P2** `A2.2` `TodoServiceImplTest.java:18,20` `TodoControllerTest.java:19,21` — 将 `import static xxx.*` 展开为具体静态方法导入
- [ ] **P2** `G16.2` `TodoServiceImpl.java:52-53` — `catch (BizException e) { throw e; }` 增加 `log.trace("BizException re-thrown", e)` 或加注释说明意图
- [ ] **P2** `G17.1` — 按 design §7.3 实现 `todo.create.enabled` 功能开关（配置中心或 application.yml 动态属性）