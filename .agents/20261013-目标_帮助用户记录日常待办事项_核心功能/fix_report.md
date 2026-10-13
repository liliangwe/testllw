# 问题修复报告（问题修复 · review 阶段）

> **Change** `todo-task-create`（待办事项新增能力 · 仅创建最小闭环） · **分支** `AI/task-DEV-966dcd0a-7905-11f1-9649-3b4281182f10-6bc6bbbf-9522-4210-a30b-0ea8a2ce3e12` · **日期** 2026-10-13 · **执行者** AI
>
> **驱动源**：`.agents/20261013-目标_帮助用户记录日常待办事项_核心功能/cr_report.md` §8「修复任务列表」（P0×2 / P1×2 / P2×6）。
> **规范依据**：`dtazziboot-java-coding-standards`（常量、注释、控制语句、异常日志、安全等规约）；规则 ID 沿用代码评审清单（G/S/M/A）。
> **验证方式声明（诚实性）**：本轮**实际运行**了编译与全量单测（`mvn -o test`，JDK 21 / Maven 3.9.9，离线）、重新运行 `scan-all-rules.sh`、以 PyYAML 与 SnakeYAML 双解析器校验 `application.yml`、并以**真实 Spring 上下文 + 真实 MVC 链路**完成运行期冒烟（临时冒烟用例跑完即删，仓库内不留痕）。§3 给出原始输出要点。

---

## 1. 修复清单（逐条闭环）

| 编号 | 等级 / 规则 | 修复动作 | 涉及文件 | 验证证据 | 状态 |
|------|-------------|----------|----------|----------|------|
| P0-1 | P0 `G17.1` `G15.3` | 应急开关去除启动期固化：`TenantTailTodoTaskCreateSwitch` 改为**每次调用实时读取** `Environment`；总开关缺失/非布尔按**关闭**处理（fail-closed）；`application.yml` 的 `enabled` 由 `true` 改为 `false`，与系分 §7.3「默认关闭」一致 | `common/feature/impl/TenantTailTodoTaskCreateSwitch.java`、`application.yml` | 单测 `should_takeEffectWithoutRestart_when_switchPropertyChanged`（配置变更后下次调用即生效）、`should_disableAllTenants_when_globalSwitchAbsent`；上下文冒烟断言 `isEnabled("tenant001")=false` | ✅ |
| P0-2 | P0 `S9.1` | 数据库凭证外置：`username`/`password` 改为 `${TODO_DB_USERNAME}` / `${TODO_DB_PASSWORD}`，仓库内不落明文口令，缺失即启动失败（fail-fast） | `application.yml` | 解析校验：`username='***'`/`password='***'` 不再是字面量；`git diff` 可见明文已删除 | ✅ |
| P1-1 | P1 `S8.1` REQ-11 | 补齐登录态接线：新增 `TodoInvokerContextInterceptor`（请求进入写入 `ThreadLocalInvokerContext`）+ `TodoWebConfig`（注册到 `/api/todo/**`，`todo.invoker.enabled=false` 可关闭）；身份来自平台登录拦截器写入的**服务端请求属性**（属性名可配置），不臆造网关头协议、不接受前端入参；身份缺失由 Service 按 R04/R05 fail-closed | `common/context/TodoInvokerContextInterceptor.java`、`common/config/TodoWebConfig.java`、`application.yml` | `TodoInvokerContextInterceptorTest`：未接线→`TODO_002`、仅有创建人→`TODO_003`、接线后落库 creator/tenantId 取自属性、报文伪造 `creator`/`tenantId` 被忽略；上下文冒烟真实链路返回 `TODO_002` | ✅ |
| P1-2 | P1 `G9.2` 系分 §6.3 | 补齐语句/读超时：JDBC URL 增加 `connectTimeout=2000&socketTimeout=2000`，`TodoTaskMapperImpl` 对语句设置 `setQueryTimeout(2)`，与 Hikari `connection-timeout: 2000` 一起兑现「连接与语句超时 ≤ 2s」 | `application.yml`、`dao/mapper/impl/TodoTaskMapperImpl.java` | `TodoTaskMapperImplTest` 断言 `setQueryTimeout(2)`；`SnakeYAML` 解析出 URL 含两个超时参数 | ✅ |
| P2-1 | P2 `G10.1` | 协议层异常（405/415）新增专用 `@ExceptionHandler`，按参数错误语义返回 `TODO_001` 并记 WARN，系统侧未知异常仍收敛为 `TODO_004`，不再「同一字段兼表请求错误与系统异常」 | `api/controller/TodoExceptionHandler.java`、`common/constant/TodoTipConstants.java` | 上下文冒烟：`GET /api/todo/task/create` → `TODO_001`；`TodoTaskControllerTest` 既有 `TODO_004` 断言保持通过 | ✅ |
| P2-2 | P2 `M016` `G14.3` | 时钟时区显式指定：`Clock.system(ZoneId.of("${todo.time.zone:Asia/Shanghai}"))`，与数据源 `serverTimezone=Asia/Shanghai` 对齐，不依赖容器默认时区 | `common/config/TodoTimeConfig.java`、`application.yml` | 上下文冒烟断言 `clock.getZone()=Asia/Shanghai` | ✅ |
| P2-3 | P2 `G11.2` | 新增 `TodoTaskMapperImplTest`，以 Mock `Connection`/`PreparedStatement` 捕获真实提交给驱动的 SQL 与参数绑定顺序，覆盖参数化 INSERT（7 占位符）、自增主键回填、`setQueryTimeout` 与「无自增主键」分支 | `src/test/java/.../dao/mapper/impl/TodoTaskMapperImplTest.java` | 新用例 2 条全通过；**真实 SQL 兼容性（MySQL/OB）仍待集成测试**，见 §4 | ⚠️ 部分闭环 |
| P2-4 | P2 `G8.5` | 拦截器 `afterCompletion`（等价 `finally`）强制 `ThreadLocalInvokerContext.clear()`，线程归还池前清理上下文 | `common/context/TodoInvokerContextInterceptor.java` | `TodoInvokerContextInterceptorTest`：成功路径与**落库异常路径**请求后 `getInvoker()/getTenantId()` 均为 null | ✅ |
| P2-5 | P2 `S8.3` | 本期无读取路径，**不修改**主键策略（系分 §5.1.1.1 已明确本期不建二级索引）；风险与下期约束记录于 §4 | — | — | ⏸ 接受风险 |
| P2-6 | P2 `A3.4` | 数据源 URL 以 YAML 双引号折行拆分（物理行宽 ≤ 96），语义仍为单行 URL；同时清理新增测试中的超宽行 | `application.yml`、`src/test/java/.../TodoTaskMapperImplTest.java` | `scan-all-rules.sh` 复扫无 `A3.4` 命中；PyYAML/SnakeYAML 解析值与折行前一致 | ✅ |

---

## 2. 关键实现说明（设计取舍）

1. **P0-1 采用「每请求实时读取配置快照」而非 `@RefreshScope`**：仓库未引入 Spring Cloud（无配置中心 SDK），新增 `spring-cloud-context` 依赖在离线条件下不可解析且属越界改动。`cr_report.md` §8 明确接受该二选一方案。开关不再持有实例字段缓存，`isEnabled()` 每次经 `Environment#getProperty` 取值，因此任何能运行期刷新 `Environment` 的配置源（平台配置推送 / 动态 `PropertySource`）在下一次请求即生效，满足系分 §7.3「配置推送 ≤ 30s、不需重启/发布」；同时 fail-closed 保证配置异常时**不放量**。
2. **P1-1 不臆造网关头协议**（继承 impl.md D04 的约束）：拦截器只做「平台已校验身份 → 进程内上下文」的接线，**不做认证判定**；身份来源是服务端请求属性（默认 `todo.invoker` / `todo.tenant-id`，可配置覆盖），默认启用（`todo.invoker.enabled: true`），若部署环境已由平台统一接线可置 `false` 关闭，避免双重写入。
3. **超时双保险**：`socketTimeout` 覆盖读阻塞，`setQueryTimeout(2)` 覆盖语句执行，二者均 ≤ 2s，与系分 §6.3 一致；连接获取超时由 Hikari 既有配置承担。
4. **未改动**：接口契约（W01 出参三字段）、错误码语义（TODO_001~005）、入参校验、埋点与日志、表结构（DDL 未变），最大限度降低对已评审主链路的影响。

---

## 3. 验证证据

### 3.1 编译与单元测试（实际执行）

```text
$ mvn -o test            # JDK 21.0.12.1 (Corretto) / Maven 3.9.9（离线）
[INFO] Tests run: 9,  Failures: 0, Errors: 0 -- TenantTailTodoTaskCreateSwitchTest
[INFO] Tests run: 5,  Failures: 0, Errors: 0 -- TodoInvokerContextInterceptorTest
[INFO] Tests run: 8,  Failures: 0, Errors: 0 -- TodoErrorCodeEnumTest
[INFO] Tests run: 2,  Failures: 0, Errors: 0 -- TodoTaskMapperImplTest
[INFO] Tests run: 5,  Failures: 0, Errors: 0 -- TodoTaskControllerTest
[INFO] Tests run: 12, Failures: 0, Errors: 0 -- TodoTaskCreateRequestTest
[INFO] Tests run: 15, Failures: 0, Errors: 0 -- TodoTaskServiceImplTest
[INFO] Tests run: 56, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

用例数 46 → **56**（+10：开关 +3、拦截器接线 +5、Mapper +2），无既有用例回归。

### 3.2 运行期冒烟（真实 Spring 上下文 + 真实 MVC 链路）

`mvn -o spring-boot:run` 因离线仓库缺少插件依赖不可用，改用临时 `@SpringBootTest(webEnvironment=MOCK) + @AutoConfigureMockMvc` 用例启动**真实上下文**（跑完即删除，仓库内无残留）：

```text
[INFO] Tests run: 1, Failures: 0, Errors: 0 -- ContextWiringScratchTest
断言结果：clock.getZone()=Asia/Shanghai
          todoTaskCreateSwitch 实例为 TenantTailTodoTaskCreateSwitch 且 isEnabled("tenant001")=false（默认关闭）
          未接线身份 → result=TODO_002（fail-closed）
          接线身份（请求属性）→ result=TODO_005（证明拦截器已在真实 MVC 链路注册，且开关默认关闭）
          GET /api/todo/task/create → result=TODO_001（协议错误不再映射 TODO_004）
```

### 3.3 规则预扫复扫

```text
$ bash <skill>/references/script/scan-all-rules.sh src/main/java src/test/java src/main/resources/db/schema src/main/resources/application.yml
[P0] G16.2 — CatchWithoutLogging: .../TodoTaskServiceImpl.java:190
[P0] G16.2 — CatchWithoutLogging: .../TodoTaskServiceImpl.java:70
[P0] S1.1 — MyBatisSqlInjection: src/main/resources/application.yml:13
[P0] S1.1 — MyBatisSqlInjection: src/main/resources/application.yml:14
=== Summary: 4 findings (P0=4) | 52/222 rules scanned ===
```

4 条均为**误报**（无新增真实问题，`A3.4` 行宽命中已清零）：

- `G16.2 ×2`：`TodoTaskServiceImpl:190-193` 有 `LOGGER.error` 且带堆栈；`:70-73` 上报埋点后 rethrow，由 `TodoExceptionHandler` 记 WARN —— 与 cr_report.md §5 复核结论一致（非本轮引入）。
- `S1.1 ×2`：`application.yml:13-14` 为 Spring 属性占位符 `${TODO_DB_USERNAME}` / `${TODO_DB_PASSWORD}`；`S1.1` 针对 MyBatis XML `${}` SQL 拼接（`rg --type xml`），且**显式传入文件路径时 ripgrep 不施加类型过滤**，故 YAML 被纳入扫描 —— 属扫描器口径问题，非 SQL 注入。

### 3.4 配置解析与行宽

```text
PyYAML   : url 折叠后 152 字符单行、无换行/空白；enabled=False；zone=Asia/Shanghai；yml 最大物理行宽 96
SnakeYAML: 与 PyYAML 解析结果一致（Spring Boot 实际使用的解析器）
```

---

## 4. 未闭环项与遗留风险（部署侧待确认）

| 项 | 说明 | 建议 |
|----|------|------|
| `G11.2` 真实 SQL 集成测试 | 未补 H2/Testcontainers（需新增依赖，离线环境无法解析）；本轮以 Mock 驱动捕获补齐实现级覆盖（SQL 文本、参数顺序、超时、主键回填/缺失分支） | 具备网络或测试库的环境补一条集成测试，校验 DDL 字段长度与 MySQL/OB 方言 |
| `S8.3` 自增 ID 可枚举 | 本期仅创建、无读取路径，暂不可利用 | 下期查询/详情接口落地时 SQL 强制携带 `tenant_id`，并评估不可枚举 ID |
| **P1-1 部署确认（必须）** | 平台全局登录拦截器须把身份写入本模块约定的请求属性（`todo.invoker` / `todo.tenant-id`），或通过 `todo.invoker.*-attribute` 对齐平台既有约定；若平台直接写入本模块上下文，可将 `todo.invoker.enabled` 置 `false` | 部署前与平台登录方案对齐并验证未登录拒绝路径 |
| **P0-2 部署确认（必须）** | `TODO_DB_USERNAME` / `TODO_DB_PASSWORD` 必填；未注入时应用启动即失败（fail-fast，符合安全预期） | 发布流水线注入环境变量/密钥管理，禁止回填明文 |
| **P0-1 应急边界** | 「配置推送 ≤ 30s、免重启」依赖部署侧具备可运行期刷新 `Environment` 的配置源（配置中心推送 / 动态 PropertySource）；仓库内未引入 Spring Cloud 依赖 | 部署前确认配置中心推送链路；演练关闭开关 `todo.task.create.enabled=false` 验证生效时延与 `TODO_005` 提示 |
| 时区约定 | 应用侧固定 `Asia/Shanghai`，须与数据源 `serverTimezone` 保持同值 | 如部署环境使用其他时区，同步调整 `todo.time.zone` 与 JDBC `serverTimezone` |

---

## 5. 变更文件清单

| 类型 | 文件 |
|------|------|
| 新增 | `src/main/java/com/antdigital/todo/common/context/TodoInvokerContextInterceptor.java` |
| 新增 | `src/main/java/com/antdigital/todo/common/config/TodoWebConfig.java` |
| 新增 | `src/test/java/com/antdigital/todo/common/context/TodoInvokerContextInterceptorTest.java` |
| 新增 | `src/test/java/com/antdigital/todo/dao/mapper/impl/TodoTaskMapperImplTest.java` |
| 修改 | `src/main/java/com/antdigital/todo/common/feature/impl/TenantTailTodoTaskCreateSwitch.java` |
| 修改 | `src/main/java/com/antdigital/todo/common/config/TodoTimeConfig.java` |
| 修改 | `src/main/java/com/antdigital/todo/api/controller/TodoExceptionHandler.java` |
| 修改 | `src/main/java/com/antdigital/todo/common/constant/TodoTipConstants.java` |
| 修改 | `src/main/java/com/antdigital/todo/dao/mapper/impl/TodoTaskMapperImpl.java` |
| 修改 | `src/main/resources/application.yml` |
| 修改 | `src/test/java/com/antdigital/todo/common/feature/impl/TenantTailTodoTaskCreateSwitchTest.java` |
| 修改 | `docs/modules/todo/README.md`、`docs/ARCHITECTURE.md` |

> 未执行任何 Git 写操作；未改动 `pom.xml`、DDL（`db/schema/todo_task.sql`）与接口契约。
