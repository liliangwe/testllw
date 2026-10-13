# Code Review Report

> **Change** `todo-task-create`（待办事项新增能力 · 仅创建最小闭环） · **分支/Commit** `AI/task-DEV-966dcd0a-7905-11f1-9649-3b4281182f10-6bc6bbbf-9522-4210-a30b-0ea8a2ce3e12` / `46bbcfc`（编码实现）+ `02f1944`（系分） · **日期** 2026-10-13 · **审查者** AI
>
> **AI**：等级 **P0 / P1 / P2**；G/S 以 checklist 行内定义为准；Bug 模式以 `bug-pattern-checklist.md` 表头为准（Blocker→P0、Major→P1、Info→P2）。
> **执行顺序**：已先在仓库根目录运行 `references/script/scan-all-rules.sh`（输出并入 §5），再由 LLM 完成 Step 2–5 中脚本未覆盖项与误报复核。
> **驱动源**：`.agents/20261013-目标_帮助用户记录日常待办事项_核心功能/2026-10-13-todo-task-create-cr-checklist.md`（执行队列 28/28 已审，`⬜` 计数 = 0）。
> **验证方式声明（诚实性）**：本次评审**未运行** `mvn test` / 编译（当前运行环境无 JDK/Maven：`java: command not found`、`command -v mvn` 为空），故**不声称**执行过构建；单测结果引自编码实现阶段记录 `impl.md` §4 L2（`Tests run: 46, Failures: 0, Errors: 0`，来源 `target/surefire-reports/*.txt`），本次以**静态审查 + 规则预扫**为判定依据，编译/单测需在具备 JDK/Maven 的环境复跑核销。

---

## 1. 审查范围

| 项 | 值 |
|----|-----|
| `.java` 文件数 | 28（主代码 23 + 测试 5） |
| 变更行数 | `+1908 / -0`（`git diff --numstat e0ef4ab..HEAD -- '*.java'`） |
| 非 Java 变更 | `application.yml`、`db/schema/todo_task.sql`、`pom.xml`、`.gitignore`、`docs/**`、`.agents/**`（无 Java 审查列，作证据引用） |

| 类/接口 | 路径 | 角色 |
|---------|------|------|
| `TodoApplication` | `src/main/java/com/antdigital/todo/TodoApplication.java` | 启动类 |
| `TodoTaskController` | `src/main/java/com/antdigital/todo/api/controller/TodoTaskController.java` | W01 `POST /api/todo/task/create` |
| `TodoExceptionHandler` | `src/main/java/com/antdigital/todo/api/controller/TodoExceptionHandler.java` | 错误码翻译（`@RestControllerAdvice`） |
| `TodoTaskCreateRequest` | `src/main/java/com/antdigital/todo/api/request/TodoTaskCreateRequest.java` | 入参 + 声明式校验 |
| `ApiResult<T>` | `src/main/java/com/antdigital/todo/api/response/ApiResult.java` | oneapi 统一响应 |
| `TodoTaskCreateResponse` | `src/main/java/com/antdigital/todo/api/response/TodoTaskCreateResponse.java` | W01 出参 data |
| `TodoTimeConfig` | `src/main/java/com/antdigital/todo/common/config/TodoTimeConfig.java` | 时钟 Bean |
| `TodoResultConstants` / `TodoTaskConstants` / `TodoTipConstants` | `src/main/java/com/antdigital/todo/common/constant/*.java` | 常量 |
| `InvokerContext` / `ThreadLocalInvokerContext` | `src/main/java/com/antdigital/todo/common/context/*.java` | 登录态/租户上下文接缝 |
| `TodoErrorCodeEnum` / `TodoBizException` | `src/main/java/com/antdigital/todo/common/{enums,exception}/*.java` | 错误码与业务异常 |
| `TodoTaskCreateSwitch` / `TenantTailTodoTaskCreateSwitch` | `src/main/java/com/antdigital/todo/common/feature/**` | 应急开关 + 租户尾号灰度 |
| `TodoTaskMonitor` / `LogTodoTaskMonitor` | `src/main/java/com/antdigital/todo/common/monitor/*.java` | 埋点 |
| `TodoTaskDO` | `src/main/java/com/antdigital/todo/dao/entity/TodoTaskDO.java` | 表实体 |
| `TodoTaskMapper` / `TodoTaskMapperImpl` | `src/main/java/com/antdigital/todo/dao/mapper/**` | 单条参数化 INSERT |
| `TodoTaskService` / `TodoTaskServiceImpl` | `src/main/java/com/antdigital/todo/service/**` | 业务主干（S01） |
| 5 个测试类 | `src/test/java/com/antdigital/todo/**` | 业务/契约/校验/枚举/开关 |

---

## 2. 问题计数

| P0 | P1 | P2 |
|----|----|-----|
| 2 | 2 | 6 |

---

## 3. Step 2 — 功能（REQ）

> REQ 全部来自 `design.md` 原文；`⚠️` 项见 §7.1 片段与 §8 修复清单。

### REQ-1（F01）: 新增待办事项（W01 `POST /api/todo/task/create`）

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 提交名称与描述 → 落库 1 行并返回 `{result:OK,msg:SUCCESS,data:{taskId,taskName,gmtCreate}}` | ✅ | design §4.1 W01 / §5.1.2 出参 / §5.1.3.1 时序图 | `TodoTaskController.java:38-41`、`TodoTaskServiceImpl.java:58-75`、`TodoTaskMapperImpl.java:39-59`、`ApiResult.java:31-37`；`TodoTaskControllerTest.java:53-65` | 出参三字段与系分一致，时间格式 `yyyy-MM-dd HH:mm:ss` |

### REQ-2（F02/R01）: 事项名称必填、strip 后 1~64

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 空/全空白/超长 → `TODO_001`；边界 64 通过 | ✅ | design §5.1.2 入参 / R01 | `TodoTaskCreateRequest.java:19-21,39-41`、`TodoTaskServiceImpl.java:111-120`；`TodoTaskCreateRequestTest.java:50-88` | setter 即 `strip()`，声明式校验与落库文本一致（D02） |

### REQ-3（F03/R02）: 事项描述选填、≤512、空存空串

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| null → 空串；512 通过；513 → `TODO_001` | ✅ | design §5.1.2 入参 / R02 | `TodoTaskCreateRequest.java:24`、`TodoTaskServiceImpl.java:128-136`；`TodoTaskServiceImplTest.java:119-134` | 与系分「不传或为空时按空串存储」一致 |

### REQ-4（F04/R04/R05/R09）: 创建人归属与租户隔离

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 创建人/租户取自上下文，入参不可覆盖；缺失 → `TODO_002`/`TODO_003` | ✅（实现符合） | design §5.1.3.2 R04/R05/R09、§6.4.2.1 | `TodoTaskServiceImpl.java:143-162`、`InvokerContext.java:11-25`；`TodoTaskServiceImplTest.java:198-229` | 写入侧水平权限边界成立；身份**写入方**缺失见 P1-1 |

### REQ-5（F05/R10）: 入参校验与错误提示（5 个错误码）

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 校验失败 → `TODO_001` + 字段提示；报文非法 → `TODO_001` 语义；兜底 `TODO_004` | ✅ | design §5.1.2 错误码表 / §5.1.3.3 R10 | `TodoErrorCodeEnum.java:12-24`、`TodoExceptionHandler.java:34-101`；`TodoTaskControllerTest.java:67-108`、`TodoErrorCodeEnumTest.java:18-44` | `TODO_001`~`TODO_005` 语义与系分逐条一致；Service 兜底独立于 Controller |

### REQ-6（F06）: 埋点与日志留痕

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 埋点 `todo.task.create`（结果/耗时/错误码）；日志不打印描述全文 | ✅ | design §6.5 / §7.1 / §6.4.3.2 | `LogTodoTaskMonitor.java:19-25`、`TodoTaskServiceImpl.java:66-68,71-72`、`TodoTaskDO.java:102-107` | 描述仅打印长度；成功 INFO / 拒绝 WARN / 系统异常 ERROR |

### REQ-7（R07/§7.2/§7.3）: 应急开关与按租户尾号灰度

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 开关关闭 → `TODO_005` 且不落库；尾号命中才放通 | ✅ | design §7.2 / §7.3 / R07 | `TenantTailTodoTaskCreateSwitch.java:37-52`、`TodoTaskServiceImpl.java:169-173`；`TenantTailTodoTaskCreateSwitchTest.java:15-70` | 逻辑正确 |
| 关闭**不需重启/发布**，配置推送 ≤ 30s | ❌ | design §7.3「一键关闭立即止损」「配置推送 ≤ 30s」 | `TenantTailTodoTaskCreateSwitch.java:37-41`、`application.yml:19-25` | 静态 `@Value` 构造注入，无 `@RefreshScope`/配置中心 → **P0-1** |

### REQ-8（R08）: `is_deleted=0`、审计时间代码赋值

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 创建时置 0，`gmt_create`/`gmt_modified` 同值，失败 → `TODO_004` | ✅ | design §5.1.3.1 R08 / §5.1.1.1 | `TodoTaskServiceImpl.java:93-101`、`todo_task.sql:10-12`；`TodoTaskServiceImplTest.java:101-116` | 代码赋值 + DDL 默认值双重保证 |

### REQ-9（R06）: 同名允许重复

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 不建唯一约束 | ✅ | design §5.1.3.1 R06 | `todo_task.sql:13-18`（仅 `pk_todo_task`，无 `uk_`） | 与系分一致 |

### REQ-10（R03）: 单次仅创建一条

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 接口仅接受单对象 | ✅ | design §5.1.3.1 R03 | `TodoTaskController.java:39` | 无批量入口 |

### REQ-11（§6.4.2.3）: 登录态检查

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| `/api/todo/**` 非白名单，未登录拒绝 `TODO_002` | ⚠️ | design §6.4.2.3「是否检查登录态：实现」 | `ThreadLocalInvokerContext.java:29-40`（`set()` 全仓无调用方，`rg` 结果仅类定义）；impl.md D04 | 仓库内无拦截器/过滤器 → **P1-1** |

---

## 4. Step 3 — 可读性检查

| 结果 | 说明（违规写 Ax.x 与 `path:行`） |
|------|--------------------------------|
| ⚠️ | A1/A2/A4/A5/A6/A7 全部通过（文件名=类名、无 `import *`、静态 import 单独分组且组内字典序、缩进 4 空格、无 Tab 缩进、`@Override` 齐备、public 成员 Javadoc 完整、块标记顺序正确）。唯一违规：**A3.4** `application.yml:9` 行宽 122 > 120（非 Java 配置行）→ **P2-6**（checklist Step 3 / §4 备注） |

---

## 5. Step 4 — 可靠性检查

**自动化预扫（先于 LLM 审查执行）**：

```text
$ bash <skill>/references/script/scan-all-rules.sh src/main/java src/test/java src/main/resources/db/schema src/main/resources/application.yml
=== Step 4 Rule Scan (B/M/I + A/S/G) ===
Targets: src/main/java src/test/java src/main/resources/db/schema src/main/resources/application.yml
Engine:  ripgrep

[P0] G16.2 — CatchWithoutLogging: src/main/java/com/antdigital/todo/service/impl/TodoTaskServiceImpl.java:190
[P0] G16.2 — CatchWithoutLogging: src/main/java/com/antdigital/todo/service/impl/TodoTaskServiceImpl.java:70
[P2] A3.4 — LineWidthExceeded: src/main/resources/application.yml:9

=== Summary: 3 findings (P0=2, P1=0, P2=1) | 52/222 rules scanned ===
```

**复核**：2 处 `G16.2` 均为**误报**（`TodoTaskServiceImpl.java:190-193` 有 `LOGGER.error` 且带堆栈；`:70-73` 上报埋点后 rethrow，由 `TodoExceptionHandler.java:36-38` 记 WARN）；`A3.4` 成立（P2-6）。脚本**漏报**经 LLM 补扫命中：`S9.1`(P0-2)、`G17.1`/`G15.3`(P0-1)、`G9.2`(P1-2)、`G8.5`(P2-4)、`M016`/`G14.3`(P2-2)、`G11.2`(P2-3)。

| 域 | 参考 | 结果 | 等级 | 说明（列命中 ID 或「已扫无命中」） |
|----|------|------|------|-------------------------------------|
| 可靠性 | `reliability-checklist.md` G1–G17 | ❌ | **P0**（2 项）、**P1**（1 项）、**P2**（3 项） | `G17.1`+`G15.3`(P0-1 开关不可运行期关闭/默认值不符)、`G9.2`(P1-2 缺语句超时)、`G10.1`(P2-1 兜底语义混用)、`M016`/`G14.3`(P2-2 时区依赖)、`G8.5`(P2-4 清理待接入验证)、`G11.2`(P2-3 Mapper 无测试)；其余 G1–G8/G11–G16 已扫无命中（`G6/G5/G7/G12` 类目为 N/A：本期无缓存/MQ/调度/资损） |
| 安全 | `security-checklist.md` S1–S10 | ❌ | **P0**（1 项）、**P1**（1 项）、**P2**（1 项） | `S9.1`(P0-2 明文凭证)、`S8.1`(P1-1 无鉴权/身份注入实现)、`S8.3`(P2-5 自增 ID 可枚举)；`S1.1` 参数化 SQL ✅、`S9.2` 日志不打印描述全文 ✅、`S6.2` 无反序列化多态 ✅；S2/S3/S4/S5/S7/S10 均 N/A（无对应场景） |
| Bug 模式 | `bug-pattern-checklist.md` B/M/I（120） | ⚠️ | **P2**（1 项） | 预扫：`scan-all-rules.sh` 3 findings（2 误报已复核）；逐条核销 118 条：命中 `M016`(P2-2)；`B022/B049/B050/B052/B055/B080/M004/M007/M008/M020/M027/I004` 等经核对为 ✅ 合规；其余 N/A（无对应代码模式） |

---

## 6. Step 5 — 自定义扩展检查

| 域 | 参考 | 结果 | 等级 | 说明（列命中 ID 或「未启用自定义规则」） |
|----|------|------|------|------------------------------------------|
| 自定义扩展 | `customized-checklist.md` U* | N/A | — | 未启用自定义规则（`U1.1`–`U2.3` 全为未启用示例项；其中示例项「Controller 入参 `@Valid`」在本实现中恰好满足，`TodoTaskController.java:39`） |

---

## 7. 结论

- **合并建议**：**阻止合并**（修复 P0 后合并）
- **P0**：
  1. **P0-1** `G17.1`/`G15.3` — 应急/灰度开关为启动期静态注入，无法运行期一键关闭，与系分 §7.3「配置推送 ≤ 30s / 不需重启·发布」冲突；且 `application.yml:23 enabled: true` 与系分「默认关闭」不符。（`TenantTailTodoTaskCreateSwitch.java:37-41`、`application.yml:19-25`）
  2. **P0-2** `S9.1` — 数据库凭证明文硬编码。（`application.yml:10-11`）
- **P1**：
  1. **P1-1** `S8.1`/REQ-11 — 仓库内无鉴权与身份注入实现，创建闭环依赖平台拦截器（须部署前确认）。
  2. **P1-2** `G9.2`/系分 §6.3 — 仅设连接获取超时，未设语句/读超时，慢 SQL 可占满线程池。
- **P2**：`G10.1` 兜底异常语义混用；`M016`/`G14.3` 容器时区依赖；`G11.2` Mapper 真实 SQL 无测试；`G8.5` ThreadLocal 清理待接入验证；`S8.3` 自增 ID 可枚举；`A3.4` `application.yml:9` 行宽 122。
- **一句话**：主链路（W01 契约、R01–R10 规则、错误码、埋点、单表单条 INSERT）实现质量高、可读性与单测覆盖良好，无功能性缺陷；但「可应急（动态开关）」与「凭证不落明文」两项未达系分/安全底线，须修复后再合并。

---

## 7.1 问题片段（必填）

### P0-1 `G17.1` `G15.3` — 应急开关不可运行期关闭，且默认值与系分不符

- **P0** `G17.1` `src/main/java/com/antdigital/todo/common/feature/impl/TenantTailTodoTaskCreateSwitch.java:35-41` — 开关与灰度列表经构造器 `@Value` 一次性注入并在构造期解析，无 `@RefreshScope`、无配置中心监听、无 getter 动态读取；运行期修改配置**不会生效**，只能重启/重新发布，无法满足系分 §7.3「一键关闭立即止损」「配置推送 ≤ 30s」，使「可应急」在设计层面落空。
  片段范围：`src/main/java/com/antdigital/todo/common/feature/impl/TenantTailTodoTaskCreateSwitch.java:25-41`

```java
L25|    /** 创建功能总开关，默认关闭（系分 7.3） */
L26|    private final boolean enabled;
L27|
L28|    /** 灰度租户尾号集合，为空表示全量放通 */
L29|    private final Set<Character> tenantTailSet;
L30|
L31|    /**
L32|     * 构造创建开关。
L33|     */
L37|    public TenantTailTodoTaskCreateSwitch(@Value("${todo.task.create.enabled:false}") boolean enabled,
L38|            @Value("${todo.task.create.tenant-tail-list:}") String tenantTailList) {
L39|        this.enabled = enabled;                      // 问题：启动期固化，运行期改配置无效
L40|        this.tenantTailSet = parseTenantTailList(tenantTailList);
L41|    }
```

- 关联**配置证据**（问题不在 `.java`，以下为对应 `application.yml` 片段）— 总开关默认值 `true` 与系分 §7.3「配置项 `todo.task.create.enabled`（默认关闭）」不符，未显式覆盖即部署等于默认放量。
  片段范围：`src/main/resources/application.yml:19-25`

```yaml
L19|todo:
L20|  task:
L21|    create:
L22|      # 系分 7.3 应急开关：一键关闭创建入口，关闭后接口返回 TODO_005
L23|      enabled: true            # 问题：与系分「默认关闭」不符，且无 @RefreshScope 无法运行期关闭
L24|      # 按租户尾号灰度（系分 7.2），为空表示灰度批次全量放通
L25|      tenant-tail-list: 0,1,2
```

### P0-2 `S9.1` — 数据库凭证明文硬编码

- **P0** `S9.1` `src/main/resources/application.yml:10-11` — 数据库账号口令以明文写入随包发布的配置文件，未走环境变量/配置中心外部化；口令变更需改代码仓库并重新发布，且存在凭据随制品外泄风险。规则等级 S9.1 = P0。
  片段范围：`src/main/resources/application.yml:7-17`（非 Java 文件，提供对应配置文件片段）

```yaml
L 7|  datasource:
L 8|    driver-class-name: com.mysql.cj.jdbc.Driver
L 9|    url: jdbc:mysql://127.0.0.1:3306/todo?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
L10|    username: todo          # 问题：明文凭证硬编码（S9.1）
L11|    password: todo          # 问题：明文凭证硬编码（S9.1）
L12|    hikari:
L13|      # 系分 6.3：连接与语句超时保护，避免慢 SQL 拖垮线程池
L14|      connection-timeout: 2000
L15|      validation-timeout: 1000
L16|      maximum-pool-size: 20
L17|      minimum-idle: 5
```

### P1-1 `S8.1` / REQ-11 — 无鉴权与身份注入实现（登录态接缝未接线）

- **P1** `S8.1` `src/main/java/com/antdigital/todo/common/context/ThreadLocalInvokerContext.java:29-40` — 提供了 `set/clear` 接缝，但全仓无 `HandlerInterceptor`/`Filter`/`WebMvcConfigurer` 等调用方（`rg` 结果仅类定义），身份与租户在生产环境完全依赖平台拦截器；未接入时 `/api/todo/task/create` 恒返回 `TODO_002`（fail-closed，**不构成越权可利用路径**，故定级 P1 而非 P0）。impl.md D04 已记录为平台范围，但部署前必须确认拦截器已在 `/api/todo/**` 上生效并在 `finally` 中 `clear()`。
  片段范围：`src/main/java/com/antdigital/todo/common/context/ThreadLocalInvokerContext.java:29-40`

```java
L29|    public static void set(String invoker, String tenantId) {
L30|        INVOKER_HOLDER.set(invoker);      // 问题：全仓无调用方（无登录拦截器实现）
L31|        TENANT_HOLDER.set(tenantId);      // 问题：租户上下文来源未接线
L32|    }
L33|
L34|    /**
L35|     * 清理当前线程上下文。
L36|     */
L37|    public static void clear() {
L38|        INVOKER_HOLDER.remove();
L39|        TENANT_HOLDER.remove();
L40|    }
```

### P1-2 `G9.2` / 系分 §6.3 — DB 仅设置连接获取超时，未设置语句/读超时

- **P1** `G9.2` `src/main/resources/application.yml:12-17` + `src/main/java/com/antdigital/todo/dao/mapper/impl/TodoTaskMapperImpl.java:41-51` — 仅配置了 Hikari `connection-timeout: 2000`/`validation-timeout`，未设置 JDBC `socketTimeout`/`queryTimeout`，系分 §6.3 要求「连接与语句超时（建议 ≤ 2s）」；DB 侧慢 SQL 或网络半开时执行语句可无限期挂起，逐条耗尽 20 个连接导致接口整体不可用（未降级路径）。
  片段范围：`src/main/java/com/antdigital/todo/dao/mapper/impl/TodoTaskMapperImpl.java:38-51`

```java
L38|    @Override
L39|    public Long insert(TodoTaskDO todoTask) {
L40|        KeyHolder keyHolder = new GeneratedKeyHolder();
L41|        jdbcTemplate.update(connection -> {              // 问题：未设置 queryTimeout/socketTimeout（G9.2）
L42|            PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
L43|            statement.setString(1, todoTask.getTenantId());
L44|            statement.setString(2, todoTask.getTaskName());
L45|            statement.setString(3, todoTask.getTaskDesc());
L46|            statement.setString(4, todoTask.getCreator());
L47|            statement.setInt(5, todoTask.getIsDeleted());
L48|            statement.setObject(6, todoTask.getGmtCreate());
L49|            statement.setObject(7, todoTask.getGmtModified());
L50|            return statement;
L51|        }, keyHolder);
```

### P2-1 `G10.1` — 兜底 `Exception` 统一映射 `TODO_004`，语义混用

- **P2** `G10.1` `src/main/java/com/antdigital/todo/api/controller/TodoExceptionHandler.java:97-101` — 兜底处理器把框架类异常（如 405/415/404/参数绑定外的容器异常）一并翻译为业务错误码 `TODO_004` 且返回 HTTP 200，调用方无法区分「系统繁忙」与「请求方式/路径错误」，与 G10.1「同一字段不得兼表无数据与系统异常」相悖；建议对已知框架异常保留其语义（或至少区分日志级别与错误码）。
  片段范围：`src/main/java/com/antdigital/todo/api/controller/TodoExceptionHandler.java:91-101`

```java
L 91|    /**
L 92|     * 兜底异常：避免异常栈直接暴露给调用方，返回 TODO_004。
L 93|     */
L 97|    @ExceptionHandler(Exception.class)
L 98|    public ApiResult<Void> handleException(Exception exception) {
L 99|        LOGGER.error("todo task create unexpected error, errorMessage:{}", exception.getMessage(), exception);
L100|        return ApiResult.fail(TodoErrorCodeEnum.SAVE_FAILED);   // 问题：405/415 等框架异常也被译为 TODO_004
L101|    }
```

### P2-2 `M016` / `G14.3` — 时钟依赖容器默认时区

- **P2** `M016` `src/main/java/com/antdigital/todo/common/config/TodoTimeConfig.java:22-24` — `Clock.systemDefaultZone()` 依赖容器 TZ，`gmtCreate/gmtModified` 由应用侧写入（`TodoTaskServiceImpl.java:93,100-101`），而 DDL 默认值为 DB `CURRENT_TIMESTAMP`（`todo_task.sql:11-12`）；若应用容器 TZ 与 DB `serverTimezone=Asia/Shanghai`（`application.yml:9`）不一致，将产生跨环境时间偏移。
  片段范围：`src/main/java/com/antdigital/todo/common/config/TodoTimeConfig.java:16-24`

```java
L16|    /**
L17|     * 系统时钟，使用默认时区，与数据库 CURRENT_TIMESTAMP 口径保持一致。
L18|     */
L21|    @Bean
L22|    public Clock clock() {
L23|        return Clock.systemDefaultZone();   // 问题：容器 TZ 依赖，需约束部署环境时区（M016/G14.3）
L24|    }
```

### P2-3 `G11.2` — 数据访问层真实 SQL/自增主键回填无测试

- **P2** `G11.2` `src/main/java/com/antdigital/todo/dao/mapper/impl/TodoTaskMapperImpl.java:38-59` — 全仓测试仅覆盖 Mock 化的 Mapper 接口（`TodoTaskServiceImplTest.java:90` 等），`GeneratedKeyHolder` 回填、`setObject(LocalDateTime)` 与 DDL 字段长度约束均未经真实 SQL 验证（impl.md §2/§5 已声明「暂未覆盖」）；建议以 H2/Testcontainers 补一条集成测试。
  片段范围：`src/main/java/com/antdigital/todo/dao/mapper/impl/TodoTaskMapperImpl.java:52-59`

```java
L52|
L53|        Number generatedKey = keyHolder.getKey();
L54|        if (generatedKey == null) {                          // 问题：该分支与 SQL 正确性均无测试覆盖（G11.2）
L55|            LOGGER.error("insert todo_task returned no generated key, tenantId:{}, creator:{}",
L56|                    todoTask.getTenantId(), todoTask.getCreator());
L57|            return null;
L58|        }
L59|        return generatedKey.longValue();
```

### P2-4 `G8.5` — ThreadLocal 清理依赖调用方，当前无 `finally` 可验证

- **P2** `G8.5` `src/main/java/com/antdigital/todo/common/context/ThreadLocalInvokerContext.java:37-40` — `clear()` 已实现 `remove()`（合规，未用 `set(null)`），但仓库内无调用方，无法证明其在请求线程的 `finally` 中执行；线程池复用场景下若漏调将造成身份/租户串号。接入平台拦截器时须强制 `finally { ThreadLocalInvokerContext.clear(); }` 并补一条验证用例。
  片段范围：`src/main/java/com/antdigital/todo/common/context/ThreadLocalInvokerContext.java:34-45`

```java
L34|    /**
L35|     * 清理当前线程上下文。
L36|     */
L37|    public static void clear() {
L38|        INVOKER_HOLDER.remove();       // 问题：无调用方，无法验证在 finally 中执行（G8.5）
L39|        TENANT_HOLDER.remove();
L40|    }
L41|
L42|    @Override
L43|    public String getInvoker() {
L44|        return INVOKER_HOLDER.get();
L45|    }
```

### P2-5 `S8.3` — 自增主键可预测/可枚举

- **P2** `S8.3` `src/main/resources/db/schema/todo_task.sql:5,13` — 主键为自增 `BIGINT`，ID 可枚举；本期无查询接口故暂无越权读取路径，系分 §5.1.1.1 亦明确本期不建二级索引、下期为查询能力预留 `idx_todo_task_tenant_creator`；下期实现查询/详情时须同时施加 `tenant_id` 条件并考虑不可枚举 ID。
  片段范围：`src/main/resources/db/schema/todo_task.sql:4-14`（非 Java 文件，提供 SQL 片段）

```sql
L 4|CREATE TABLE IF NOT EXISTS todo_task (
L 5|    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID，待办事项唯一标识',
L 6|    tenant_id    VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '租户标识，用于租户隔离',
L 7|    task_name    VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '事项名称，必填',
L 8|    task_desc    VARCHAR(512) NOT NULL DEFAULT '' COMMENT '事项描述，选填，空时存空串',
L 9|    creator      VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建人登录名/工号，取自登录态',
L10|    is_deleted   TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
L11|    gmt_create   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，代码侧维护',
L12|    gmt_modified DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间，代码侧维护（创建时与创建时间一致）',
L13|    CONSTRAINT pk_todo_task PRIMARY KEY (id)   -- 问题：自增 ID 可枚举（S8.3，本期无读取路径）
L14|) COMMENT = '待办事项表';
```

### P2-6 `A3.4` — `application.yml:9` 行宽 122 > 120

- **P2** `A3.4` `src/main/resources/application.yml:9` — 数据源 URL 单行 122 字符，超出 A3.4 行宽上限（非 Java 配置行，豁免不适用），建议以 `spring.datasource.hikari`/多行 YAML 折叠或 `url: >-` 拆行。
  片段范围：`src/main/resources/application.yml:7-11`（非 Java 文件，提供对应配置文件片段）

```yaml
L 7|  datasource:
L 8|    driver-class-name: com.mysql.cj.jdbc.Driver
L 9|    url: jdbc:mysql://127.0.0.1:3306/todo?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai   # 问题：122 字符 > 120（A3.4）
L10|    username: todo
L11|    password: todo
```

---

## 8. 修复任务列表

### P0

- [ ] **P0** `G17.1` `G15.3` — 将 `todo.task.create.enabled`/`tenant-tail-list` 改为可运行期生效的读取方式（`@RefreshScope` + 配置中心/配置推送，或每请求读取配置快照），使应急关闭满足系分 §7.3「配置推送 ≤ 30s、不需重启/发布」；同时把 `application.yml:23` 的 `enabled` 调整为与系分一致的「默认关闭」并由环境显式开启。
- [ ] **P0** `S9.1` — 外置 `application.yml:10-11` 数据库凭证为环境变量/配置中心占位（如 `${TODO_DB_USERNAME}` / `${TODO_DB_PASSWORD}`），仓库内不落明文口令。

### P1

- [ ] **P1** `S8.1` — 在仓库内补齐（或明确以 `@Configuration` 声明并纳管）登录态拦截器，于请求进入时写入 `ThreadLocalInvokerContext.set(...)`、`finally` 中 `clear()`；部署前确认 `/api/todo/**` 已被鉴权覆盖并补一条 MockMvc 鉴权用例（未接入场景断言 `TODO_002`）。
- [ ] **P1** `G9.2` — 为 DB 访问补齐语句/读超时（JDBC URL 增加 `connectTimeout`/`socketTimeout` 或 `JdbcTemplate.setQueryTimeout(2)`），兑现系分 §6.3「连接与语句超时（建议 ≤ 2s）」。

### P2（可选）

- [ ] **P2** `G10.1` — 在 `TodoExceptionHandler` 中区分框架异常（405/415/404 等）与业务异常，避免统一翻译为 `TODO_004`；可用更具体的 `@ExceptionHandler` 先行匹配。
- [ ] **P2** `M016` `G14.3` — 显式约定应用容器时区（如启动参数 `-Duser.timezone=Asia/Shanghai` 或改为显式 `ZoneId` 注入），并保证与 DB `serverTimezone` 一致；在部署文档中固化该约束。
- [ ] **P2** `G11.2` — 为 `TodoTaskMapperImpl` 补 H2/Testcontainers 集成测试，覆盖参数化 INSERT、自增主键回填与 `task_name/task_desc` 长度边界。
- [ ] **P2** `G8.5` — 拦截器接入时强制 `finally { ThreadLocalInvokerContext.clear(); }`，并补一条线程复用/清理验证用例。
- [ ] **P2** `S8.3` — 下期查询能力落地时，SQL 强制携带 `tenant_id` 条件，并评估不可枚举 ID 或对外暴露 ID 映射。
- [ ] **P2** `A3.4` — 拆分 `application.yml:9` 数据源 URL 行，控制在 120 字符内。
