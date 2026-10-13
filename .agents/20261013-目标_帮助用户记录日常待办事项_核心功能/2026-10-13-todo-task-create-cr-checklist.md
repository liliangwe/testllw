# Code Review Checklist

> **Change** `todo-task-create`（待办事项新增能力） · **分支/Commit** `AI/task-DEV-966dcd0a-7905-11f1-9649-3b4281182f10-6bc6bbbf-9522-4210-a30b-0ea8a2ce3e12` / `46bbcfc`（编码实现）+ `02f1944`（系分） · **日期** `2026-10-13`
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。
> **完成标准**：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。
>
> **执行顺序（强制）**：已先在仓库根目录对变更路径运行 `references/script/scan-all-rules.sh`（输出见 Step 3 / Step 4 备注），再用 LLM 完成 Step 2–5 中脚本未覆盖项及复核。
> **列值约定**：`N/A` 表示该文件不涉及该域（无并发/无 SQL/无 MQ/无缓存/无调度/无外部调用/无对应安全场景）。

---

## Step 1 — 执行队列（产物 A）

> **扫描范围**：`git diff --numstat e0ef4ab..HEAD -- '*.java'` → 28 个 `.java` 文件（`+1908 / -0`），另含 `application.yml` / `todo_task.sql` / `pom.xml`（非 Java，无 Java 审查列，标 `跳过(非 Java)`）。
> **Java 守卫**：存在 28 个 `.java` 文件，满足技能适用条件。

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|----|----|----|----|----|----|----|----|----|-----|--------|
| 1 | `src/main/java/com/antdigital/todo/TodoApplication.java` | 启动类 / design §2 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 2 | `src/main/java/com/antdigital/todo/api/controller/TodoTaskController.java` | REQ-1 W01 / design 4.1 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | ✅ | N/A | ⚠️ 已审有问题 |
| 3 | `src/main/java/com/antdigital/todo/api/controller/TodoExceptionHandler.java` | REQ-5 错误码翻译 / design 5.1.3.3 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | N/A | N/A | ✅ | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | ⚠️ 已审有问题 |
| 4 | `src/main/java/com/antdigital/todo/api/request/TodoTaskCreateRequest.java` | REQ-2/REQ-3 R01/R02 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | ✅ 已审 |
| 5 | `src/main/java/com/antdigital/todo/api/response/ApiResult.java` | REQ-1 W01 出参封装 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 6 | `src/main/java/com/antdigital/todo/api/response/TodoTaskCreateResponse.java` | REQ-1 W01 出参 data | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 7 | `src/main/java/com/antdigital/todo/common/config/TodoTimeConfig.java` | REQ-8 时钟注入 / G14 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ 已审有问题 |
| 8 | `src/main/java/com/antdigital/todo/common/constant/TodoResultConstants.java` | REQ-1 结果码常量 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 9 | `src/main/java/com/antdigital/todo/common/constant/TodoTaskConstants.java` | REQ-2/REQ-3/REQ-8 常量 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 10 | `src/main/java/com/antdigital/todo/common/constant/TodoTipConstants.java` | REQ-5 提示文案 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 11 | `src/main/java/com/antdigital/todo/common/context/InvokerContext.java` | REQ-4 F04 上下文接口 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | N/A | N/A | ⚠️ 已审有问题 |
| 12 | `src/main/java/com/antdigital/todo/common/context/ThreadLocalInvokerContext.java` | REQ-4 F04 上下文实现 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | N/A | N/A | ⚠️ 已审有问题 |
| 13 | `src/main/java/com/antdigital/todo/common/enums/TodoErrorCodeEnum.java` | REQ-5 错误码枚举 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 14 | `src/main/java/com/antdigital/todo/common/exception/TodoBizException.java` | REQ-5 业务异常 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 15 | `src/main/java/com/antdigital/todo/common/feature/TodoTaskCreateSwitch.java` | REQ-7 R07 开关接口 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 16 | `src/main/java/com/antdigital/todo/common/feature/impl/TenantTailTodoTaskCreateSwitch.java` | REQ-7 R07 + 7.2/7.3 灰度与应急 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | N/A | ❌ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ❌ 已审有问题 |
| 17 | `src/main/java/com/antdigital/todo/common/monitor/LogTodoTaskMonitor.java` | REQ-6 F06 埋点落地 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | ✅ 已审 |
| 18 | `src/main/java/com/antdigital/todo/common/monitor/TodoTaskMonitor.java` | REQ-6 F06 埋点接口 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 19 | `src/main/java/com/antdigital/todo/dao/entity/TodoTaskDO.java` | REQ-2/3/4/8 表实体 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | ✅ 已审 |
| 20 | `src/main/java/com/antdigital/todo/dao/mapper/TodoTaskMapper.java` | REQ-1 S01 数据访问接口 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 21 | `src/main/java/com/antdigital/todo/dao/mapper/impl/TodoTaskMapperImpl.java` | REQ-1 单条 INSERT 落库 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | N/A | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ 已审有问题 |
| 22 | `src/main/java/com/antdigital/todo/service/TodoTaskService.java` | REQ-1 S01 服务契约 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 23 | `src/main/java/com/antdigital/todo/service/impl/TodoTaskServiceImpl.java` | REQ-1/2/3/4/5/6 业务主干 | ✅ | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | ⚠️ | N/A | N/A | N/A | ✅ | ⚠️ | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | ⚠️ 已审有问题 |
| 24 | `src/test/java/com/antdigital/todo/api/controller/TodoTaskControllerTest.java` | REQ-1/REQ-5 契约测试 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 25 | `src/test/java/com/antdigital/todo/api/request/TodoTaskCreateRequestTest.java` | REQ-2/REQ-3 R01/R02 测试 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 26 | `src/test/java/com/antdigital/todo/common/enums/TodoErrorCodeEnumTest.java` | REQ-5 错误码测试 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 27 | `src/test/java/com/antdigital/todo/common/feature/impl/TenantTailTodoTaskCreateSwitchTest.java` | REQ-7 开关逻辑测试 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | ⚠️ | N/A | ❌ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ❌ 已审有问题 |
| 28 | `src/test/java/com/antdigital/todo/service/impl/TodoTaskServiceImplTest.java` | REQ-1–REQ-8 业务测试 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |

**非 Java 变更（跳过，仅在相关行引用）**

| 文件 | 处理 |
|------|------|
| `src/main/resources/application.yml` | 跳过(非 Java)；作为 S9.1(P0)、G17.1(P0)、G9.2(P1)、A3.4(P2) 证据 |
| `src/main/resources/db/schema/todo_task.sql` | 跳过(非 Java)；作为 REQ-8/REQ-9、G15.1 证据 |
| `pom.xml` | 跳过(非 Java)；依赖与构建基线 |
| `.gitignore` / `docs/ARCHITECTURE.md` / `docs/modules/todo/README.md` / `.agents/**` | 跳过(非 Java，文档/配置) |

---

## Step 2 — 功能（产物 B）

> 仅从系分 `design.md` 提取 REQ，不发明范围外功能点。

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1（F01） | Given 内部用户已登录；When `POST /api/todo/task/create` 提交 taskName/taskDesc；Then 落库 1 行并返回 `{result:OK,msg:SUCCESS,data:{taskId,taskName,gmtCreate}}` | design §4.1 W01 / §5.1.2「入参/出参」/ §5.1.3.1 时序图 | `TodoTaskController.java`、`TodoTaskServiceImpl.java`、`TodoTaskMapperImpl.java`、`ApiResult.java` | ✅ | `TodoTaskController.java:38-41`；`TodoTaskServiceImpl.java:58-75`；`TodoTaskMapperImpl.java:39-59`；`TodoTaskControllerTest.java:53-65` |
| REQ-2（F02/R01） | 名称为空/全空白 → 拒绝并返回 `TODO_001`；名称 strip 后 ≤64；超长拒绝 | design §5.1.2 入参「去除首尾空白后长度 1~64」/ R01 | `TodoTaskCreateRequest.java`、`TodoTaskServiceImpl.java` | ✅ | `TodoTaskCreateRequest.java:19-21,39-41`（setter strip + `@NotBlank`/`@Size(64)`）；`TodoTaskServiceImpl.java:111-120`（兜底）；`TodoTaskCreateRequestTest.java:50-88` |
| REQ-3（F03/R02） | 描述选填，>512 拒绝；不传或为空时按空串存 | design §5.1.2 入参 taskDesc / R02 | `TodoTaskCreateRequest.java`、`TodoTaskServiceImpl.java` | ✅ | `TodoTaskCreateRequest.java:24`；`TodoTaskServiceImpl.java:128-136`；`TodoTaskServiceImplTest.java:119-134` |
| REQ-4（F04/R04/R05） | 创建人/租户强制取自登录态，禁止入参覆盖；缺失返回 `TODO_002`/`TODO_003` | design §5.1.3.2 R04/R05/R09、§6.4.2.1 | `TodoTaskServiceImpl.java`、`InvokerContext.java`、`ThreadLocalInvokerContext.java` | ✅（实现符合） | `TodoTaskServiceImpl.java:143-162`；`InvokerContext.java:11-25`；`TodoTaskServiceImplTest.java:198-229`。身份写入方缺失见 P1-1 |
| REQ-5（F05/R10） | 校验失败返回 `TODO_001` + 明确提示；5 个错误码语义与系分一致；Service 兜底防绕过 | design §5.1.2 错误码表 / §5.1.3.3 R10 | `TodoExceptionHandler.java`、`TodoErrorCodeEnum.java`、`TodoTipConstants.java` | ✅ | `TodoErrorCodeEnum.java:12-24`；`TodoExceptionHandler.java:34-101`；`TodoTaskServiceImpl.java:83-90`；`TodoTaskControllerTest.java:67-108` |
| REQ-6（F06） | 埋点 `todo.task.create`（结果/耗时/错误码）；日志不打印描述全文 | design §6.5 / §7.1 / §6.4.3.2 | `TodoTaskMonitor.java`、`LogTodoTaskMonitor.java`、`TodoTaskServiceImpl.java`、`TodoTaskDO.java` | ✅ | `LogTodoTaskMonitor.java:19-25`；`TodoTaskServiceImpl.java:66-68,71-72`；`TodoTaskDO.java:102-107`（仅打印描述长度） |
| REQ-7（R07/§7.2/§7.3） | 应急开关关闭时返回 `TODO_005` 且不落库；按租户尾号灰度；开关无重启即可生效（配置推送 ≤30s） | design §7.3「一键关闭立即止损」「配置推送 ≤ 30s」/ R07 | `TenantTailTodoTaskCreateSwitch.java`、`application.yml` | ⚠️ | 开关与灰度逻辑已实现：`TenantTailTodoTaskCreateSwitch.java:37-52`；但为启动期静态注入，无 `@RefreshScope`/配置中心监听 → 不满足「不需重启/发布」，见 P0-1 |
| REQ-8（R08） | `is_deleted` 初始 0，`gmt_create`/`gmt_modified` 由代码赋值，异常返回 `TODO_004` | design §5.1.3.1 R08、§5.1.1.1 | `TodoTaskServiceImpl.java`、`todo_task.sql` | ✅ | `TodoTaskServiceImpl.java:93-101`；`todo_task.sql:10-12`（默认值兜底）；`TodoTaskServiceImplTest.java:101-116` |
| REQ-9（R06） | 同名待办允许重复，不做唯一性校验 | design §5.1.3.1 R06 | `todo_task.sql` | ✅ | `todo_task.sql:13-18`（仅主键，无唯一索引） |
| REQ-10（R03） | 单次请求仅创建一条 | design §5.1.3.1 R03 | `TodoTaskController.java` | ✅ | `TodoTaskController.java:39`（仅接受单个 `TodoTaskCreateRequest`） |
| REQ-11（§6.4.2.3） | 登录态检查实现：`/api/todo/**` 非白名单，未登录拒绝 `TODO_002` | design §6.4.2.3「是否检查登录态：实现」 | `InvokerContext.java`、`ThreadLocalInvokerContext.java` | ⚠️ | 仓库内无拦截器/过滤器实现，`set()` 无调用方（`rg` 全仓仅类定义）；impl.md D04 记载为平台范围，见 P1-1 |

---

## Step 3 — 可读性检查（产物 C）

| ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | A1.1 文件名=顶层类名 ✅（28/28）；A1.2 UTF-8 ✅；A1.3 无 Tab（`grep -P '\t'` 仅命中测试内 `"\t"` 字符串字面量，非缩进）✅ |
| A2 | 源文件结构/import 顺序 | ✅ | A2.1 顺序正确；A2.2 无 `import *` ✅；A2.3 静态 import 单独分组且组间空行（各测试类）✅；A2.4 组内 ASCII 字典序 ✅；A2.5 重载方法（`TodoBizException`、`ApiResult.fail`）连续 ✅ |
| A3 | 代码样式 | ⚠️ | A3.1 K&R ✅；A3.2 无空 `catch/finally {}` ✅；A3.3 缩进 4 空格 ✅；**A3.4 行宽**：`application.yml:9` 长 122 > 120（非 Java 配置行，建议 P2 优化）；A3.5–A3.8 ✅ |
| A4 | 命名规范 | ✅ | A4.1 包名全小写 ✅；A4.2 类名 UpperCamelCase ✅（`TodoTaskDO`/`TodoErrorCodeEnum` 符合 Impl/Enum 约定）；A4.3/A4.4/A4.5 ✅；A4.7 测试类名=`被测类名+Test` ✅（5/5） |
| A5 | 编码实践 | ✅ | A5.1 `@Override` 齐备（实现类与测试无遗漏）✅；A5.2 无空 catch ✅；A5.3 静态方法类名调用 ✅；A5.4 无 `finalize()` ✅ |
| A6 | 特定元素样式 | ✅ | A6.1 无 C 风格数组 ✅；A6.2 无 switch ✅（`TodoErrorCodeEnum.getByCode` 用 for 循环，无 fall-through 问题）；A6.3 修饰符顺序 ✅；A6.4 注解每行一个 ✅；A6.5 无 long 字面量（`1000001L` 大写 L ✅） |
| A7 | Javadoc 规范 | ✅ | A7.1 public 类/成员均有 Javadoc ✅；A7.2 块标记顺序 `@param`→`@return`→`@throws` 正确 ✅；A7.3 简单 getter 省略合理 ✅；A7.4 多段落用 `<p>`（`TodoTaskController.java:32`、`ThreadLocalInvokerContext.java:8`）✅ |

---

## Step 4 — 可靠性检查（产物 D）

> **自动化预扫（强制，先行）**：`bash <skill>/references/script/scan-all-rules.sh src/main/java src/test/java src/main/resources/db/schema src/main/resources/application.yml`
>
> **完整终端输出**（52/222 条规则）：
> ```text
> === Step 4 Rule Scan (B/M/I + A/S/G) ===
> Targets: src/main/java src/test/java src/main/resources/db/schema src/main/resources/application.yml
> Engine:  ripgrep
>
> [P0] G16.2 — CatchWithoutLogging: src/main/java/com/antdigital/todo/service/impl/TodoTaskServiceImpl.java:190
> [P0] G16.2 — CatchWithoutLogging: src/main/java/com/antdigital/todo/service/impl/TodoTaskServiceImpl.java:70
> [P2] A3.4 — LineWidthExceeded: src/main/resources/application.yml:9
>
> === Summary: 3 findings (P0=2, P1=0, P2=1) | 52/222 rules scanned ===
> ```
>
> **预扫复核结论（LLM 复核误报/漏报）**：
> - `G16.2 @ TodoTaskServiceImpl.java:190` — **误报**：catch 体内 `191-193` 行有 `LOGGER.error(...)` 且携带 `exception` 堆栈 → 记 ✅。
> - `G16.2 @ TodoTaskServiceImpl.java:70` — **误报**：catch 体内调用埋点 `reportCreate(...)` 上报失败错误码与耗时，并向上 rethrow，由 `TodoExceptionHandler.java:36-38` 记 WARN → 记 ✅。
> - `A3.4 @ application.yml:9`（122 字符 > 120）— **成立**，记 ⚠️ P2（非 Java 配置行）。
> - **脚本漏报（LLM 补扫命中）**：`S9.1`（`application.yml:10-11` 明文凭证）、`G17.1`（`TenantTailTodoTaskCreateSwitch.java:37-41` 静态注入开关）、`G9.2`（`application.yml:12-17` 缺语句/读超时）、`G8.5`、`M016`、`G11.2` — 详见下表与报告 §5。

### 4.1 Bug 模式（`bug-pattern-checklist.md`，B001–B081 / M001–M027 / I001–I010）

| ID | 状态 | 备注（命中写 `path:line`；预扫可粘贴脚本摘要） |
|----|------|--------------------------------------------------|
| B001 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B002 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B003 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B004 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B005 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B006 | N/A | 未使用 JUnit `assertEquals`（统一 AssertJ `assertThat`） |
| B007 | N/A | 测试无 `catch (Throwable/Error)` |
| B008 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B009 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B010 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B011 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B012 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B013 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B014 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B015 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B016 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B017 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B018 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B019 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B020 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B021 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B022 | ✅ | 日期格式化使用 `DateTimeFormatter`（`TodoTaskServiceImpl.java:36`），非 `SimpleDateFormat` |
| B023 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B024 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B025 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B026 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B027 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B028 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B029 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B030 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B031 | N/A | 未使用 `String.format` |
| B032 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B033 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B034 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B035 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B036 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B037 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B038 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B039 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B040 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B041 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B042 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B043 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B044 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B045 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B046 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B047 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B048 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B049 | ✅ | 格式串 `yyyy-MM-dd HH:mm:ss` 正确（`TodoTaskServiceImpl.java:36`），无 `DD`/`YYYY`/`hh` 误用 |
| B050 | ✅ | 24 小时制 `HH` 未误配 `a`（`TodoTaskServiceImpl.java:36`） |
| B051 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B052 | ✅ | 未使用 ISO 周年 `YYYY`（`TodoTaskServiceImpl.java:36`） |
| B053 | N/A | 期望抛异常的用例使用 `assertThrows`/`assertThatThrownBy`，无需 `fail()` |
| B054 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B055 | ✅ | Mockito 用法正确：`when(...).thenReturn(...)`；`verify(mock, times(n)).method(...)`（`TodoTaskServiceImplTest.java:90,103,114,268`） |
| B056 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B057 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B058 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B059 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B060 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B061 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B062 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B063 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B064 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B065 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B066 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B067 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B068 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B069 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B070 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B071 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B072 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B073 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B074 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B075 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B076 | N/A | 全仓无 `@Transactional`；单表单条 INSERT 无事务需求 |
| B077 | N/A | 测试无 `catch (Throwable/Error)` |
| B078 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B079 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| B080 | ✅ | 5 个测试类全部含断言（AssertJ / MockMvc jsonPath / verify） |
| B081 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| M001 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| M002 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| M003 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| M004 | ✅ | 全仓无 `printStackTrace()`（改用 SLF4J 占位符 + 异常对象） |
| M005 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| M006 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| M007 | ✅ | 全仓无空 catch；`TodoTaskServiceImpl.java:70,190` 两处 catch 均有埋点/日志（见上方误报复核） |
| M008 | ✅ | 未重写 `equals()`（仅重写 `toString()`：`TodoTaskDO.java:101`、`ApiResult.java:90`、`TodoTaskCreateResponse.java:44`） |
| M009 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| M010 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| M011 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| M012 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| M013 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| M014 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| M015 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| M016 | ⚠️ | P2：`Clock.systemDefaultZone()` 依赖容器默认时区（`TodoTimeConfig.java:23`）→ 见报告 P2-2 |
| M017 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| M018 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| M019 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| M020 | ✅ | 实现类对接口方法均带 `@Override`（`TodoTaskMapperImpl.java:38`、`TodoTaskServiceImpl.java:57` 等） |
| M021 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| M022 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| M023 | N/A | 未对未重写 `toString()` 的类做打印输出；关键 POJO 已重写 `toString()` |
| M024 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| M025 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| M026 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| M027 | ✅ | `ThreadLocal` 声明为 `static final`（`ThreadLocalInvokerContext.java:18,21`） |
| I001 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| I002 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| I003 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| I004 | ✅ | 未使用 `java.util.Date`（统一 `java.time`） |
| I005 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| I006 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| I007 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| I008 | N/A | 无对应代码模式（本变更无该规则涉及的 API/语法/测试惯用法） |
| I009 | ✅ | 单测方法统计：34 方法 / 46 用例（impl.md §4 L2） |
| I010 | ✅ | 未使用 Spring/Pandora 容器 Runner；单测为纯 Mock 单元测试 |

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1 | N/A | 无事务内读后写/无共享行更新；单表单条 INSERT |
| G1.2 | N/A | 无加锁/无 loadWithLock 场景 |
| G1.3 | N/A | 无乐观锁与重试 |
| G1.4 | N/A | 无多资源加锁 |
| G2.1 | ✅ | 已核对：无服务端幂等键，但系分 §5.1.3.1 并发控制显式决策采用方案②（前端防重 + `requestId` 预留，重复记录为可接受冗余），非实现偏差；`requestId` 预留见 `TodoTaskCreateRequest.java:27-28` |
| G2.2 | N/A | 无重试/定时任务/MQ 重投 |
| G2.3 | N/A | 无上游幂等键约定 |
| G3.1 | N/A | 无分布式事务/跨库写入 |
| G3.2 | ✅ | 无 `@Transactional`，不存在事务内含外部 I/O 的长事务（`TodoTaskMapperImpl.java:39-59` 为单语句 INSERT） |
| G4.1 | N/A | 无复杂业务 SQL（仅单条参数化 INSERT） |
| G4.2 | N/A | 无查询条件/无索引列函数转换 |
| G4.3 | N/A | 本期无列表查询 |
| G4.4 | N/A | 参考清单 `reliability-checklist.md` 未定义该 ID（模板占位项） |
| G5.1 | N/A | 本期不引入 MQ（系分 §1 排除范围） |
| G6.1 | N/A | 本期不引入缓存 |
| G6.2 | N/A | 本期不引入缓存 |
| G7.1 | N/A | 本期无定时任务 |
| G7.2 | N/A | 本期无定时任务 |
| G8.1 | ✅ | 异常不吞：`TodoTaskServiceImpl.java:190-196` 记 ERROR 并包装 `TODO_004` 抛出；`TodoExceptionHandler.java:85-101` 记 ERROR 后返回错误码 |
| G8.2 | N/A | 唯一强依赖为 DB，系分 §6.1 明确快速失败不降级（已按设计实现） |
| G8.3 | N/A | 无自建 I/O 流/连接/锁；JDBC 连接由 `JdbcTemplate` 管理并释放 |
| G8.4 | N/A | 无自建线程池/定时任务 |
| G8.5 | ⚠️ | P2：`ThreadLocal` 清理（`remove()`）已实现于 `ThreadLocalInvokerContext.java:37-40`，但仓库内无调用方（无拦截器），无法在 finally 中验证；接入平台拦截器时必须 `finally clear()` → 见报告 P2-4 |
| G8.6 | N/A | 无 `Executors` 线程池 |
| G8.7 | N/A | 参考清单 `reliability-checklist.md` 未定义该 ID（模板占位项） |
| G9.1 | ✅ | DB 异常按 `DataAccessException` 分类捕获并统一映射 `TODO_004`，不重试、不放大（`TodoTaskServiceImpl.java:181-197`） |
| G9.2 | ⚠️ | P1：仅设置连接获取超时（`application.yml:14 connection-timeout: 2000`），**未设置语句/读超时**，慢 SQL 可长期占用线程池（maximum-pool-size=20）→ 见报告 P1-2 |
| G9.3 | ✅ | 系分 §5.1.3.1 明确不重试，代码无重试逻辑 |
| G10.1 | ⚠️ | P2：`TodoExceptionHandler.java:97-101` 兜底 `Exception` → 统一 `TODO_004`，框架异常（405/404/415 等）被翻译为业务错误码，语义混用 → 见报告 P2-1 |
| G10.2 | ✅ | 新增接口，无旧调用方与契约变更（系分附录 A「兼容性检查（接口）不适用」） |
| G11.1 | ✅ | 主链路由 5 个测试类覆盖（34 方法 / 46 用例，均含断言） |
| G11.2 | ⚠️ | P2：`TodoTaskMapperImpl` 真实 SQL 与自增主键回填无测试（impl.md §2 已声明未覆盖）；`TodoExceptionHandler#handleDataAccessException` 无直接单测 → 见报告 P2-3 |
| G11.3 | ✅ | 入参空值有防御：`TodoTaskServiceImpl.java:84-90,111-120,143-162`（null 请求、空白名称、缺失创建人/租户） |
| G11.4 | N/A | 无金额/数值运算 |
| G12.1 | N/A | 不涉及资金/库存/积分 |
| G12.2 | ✅ | 止血手段为配置开关，返回 `TODO_005` 且不落库（`TenantTailTodoTaskCreateSwitch.java:44-52`、`TodoTaskServiceImpl.java:169-173`）；开关生效方式问题另见 P0-1 |
| G13.1 | ✅ | 日志级别正确：成功 INFO（`TodoTaskServiceImpl.java:67`）、业务拒绝 WARN（`TodoExceptionHandler.java:36,51,63,75`）、系统异常 ERROR（`TodoExceptionHandler.java:87,99`） |
| G14.1 | N/A | 无金额与币种 |
| G14.2 | ✅ | 租户取自上下文而非入参（`TodoTaskServiceImpl.java:156-162`），写入 `tenant_id` 作为隔离边界，无越权入参路径 |
| G14.3 | ⚠️ | P2：时间以 `LocalDateTime` + 容器默认时区写入（`TodoTimeConfig.java:23`、`TodoTaskServiceImpl.java:93`），依赖部署环境与 DB `serverTimezone=Asia/Shanghai` 一致 → 见报告 P2-2 |
| G14.4 | ✅ | 格式化 `DateTimeFormatter` 仅用于 `LocalDateTime`，不涉跨区解析/时区缺失 |
| G15.1 | ✅ | 纯新增表 `todo_task`，无删列/改类型（`todo_task.sql:4-14`），向前兼容 |
| G15.2 | N/A | 新增接口，无新旧接口共存 |
| G15.3 | ❌ | P0：应急/灰度开关为启动期静态注入（`TenantTailTodoTaskCreateSwitch.java:37-41` 构造器 `@Value`，无 `@RefreshScope`/配置中心监听），不满足系分 §7.3「一键关闭立即止损 / 配置推送 ≤ 30s」；且 `application.yml:23 enabled: true` 与系分「默认关闭」不符 → 见报告 P0-1 |
| G16.1 | ✅ | 核心链路有埋点：`todo.task.create` 上报结果、错误码、耗时（`LogTodoTaskMonitor.java:22-25`，调用点 `TodoTaskServiceImpl.java:66,71`） |
| G16.2 | ✅ | 预扫 2 处命中经复核为误报；异常路径均有日志或告警且携带 tenantId/creator/taskName 上下文（`TodoTaskServiceImpl.java:185-193`、`TodoExceptionHandler.java:36-38,87,99`） |
| G16.3 | ✅ | 日志级别正确（见 G13.1） |
| G16.4 | ✅ | 无空 `catch`、无 `printStackTrace()`、捕获后均记录并处理 |
| G17.1 | ❌ | P0：开关不支持运行期关闭（无 `@RefreshScope`/配置中心），需重启/发布 → 见报告 P0-1 |
| G17.2 | ✅ | 按系分 §6.1 显式设计：DB 为强依赖、快速失败返回 `TODO_004`，不降级 |
| G17.3 | N/A | 本期仅新增写入，不改存量数据，无需回滚脚本（系分 §7.3） |
| G18.1 | N/A | `reliability-checklist.md` 未定义 G18 节（安全补强节为空），无对应检查项 |
| G18.2 | N/A | `reliability-checklist.md` 未定义 G18 节（安全补强节为空），无对应检查项 |
| G18.3 | N/A | `reliability-checklist.md` 未定义 G18 节（安全补强节为空），无对应检查项 |

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1 | ✅ | SQL 全为 `?` 占位符，无 `${}`/字符串拼接（`TodoTaskMapperImpl.java:28-30,41-51`） |
| S1.2 | N/A | 无动态表名/字段名/order by 拼接 |
| S1.3 | N/A | 无 like / in 查询 |
| S2.1 | N/A | 无 HTML/JS/URL 输出（JSON 响应由框架序列化） |
| S2.2 | N/A | 无富文本 |
| S2.3 | N/A | 无模板引擎 |
| S3.1 | N/A | 无外部 URL 请求 |
| S3.2 | N/A | 无 302 跳转 |
| S3.3 | N/A | 无外部 HTTP 调用 |
| S4.1 | N/A | 无系统命令执行 |
| S4.2 | N/A | 无文件/图片外部命令调用 |
| S5.1 | N/A | 无 XML 解析 |
| S5.2 | N/A | 无 XPath |
| S6.1 | N/A | 无 Java 原生反序列化 |
| S6.2 | ✅ | JSON 入参为单态 POJO（`TodoTaskCreateRequest`），无 `@JsonTypeInfo` 多态反序列化 |
| S6.3 | N/A | 无敏感字段（本期不落敏感数据，系分 §6.4.3.1） |
| S7.1 | N/A | 无文件上传/下载 |
| S7.2 | N/A | 无文件路径处理 |
| S7.3 | N/A | 无文件存储 |
| S8.1 | ⚠️ | P1：仓库内无鉴权/身份注入实现（无 `HandlerInterceptor`/`Filter`；`ThreadLocalInvokerContext.set` 无调用方），实际鉴权由平台拦截器承担（impl.md D04）；未接入时接口 fail-closed 返回 `TODO_002`，无越权可利用路径，故定级 P1 而非 P0 → 见报告 P1-1 |
| S8.2 | ✅ | 新增为 `POST /api/todo/task/create`，无 GET 增删改（`TodoTaskController.java:38`） |
| S8.3 | ⚠️ | P2：主键为自增 `BIGINT`，ID 可预测/可枚举（`todo_task.sql:5`）；本期无查询接口故无越权读取路径，下期查询需带 `tenant_id` 条件 → 见报告 P2-5 |
| S8.4 | N/A | 未使用 Cookie/Session（无状态接口） |
| S9.1 | ❌ | P0：数据库凭证明文硬编码于 `application.yml:10-11`（`username: todo` / `password: todo`）→ 见报告 P0-2 |
| S9.2 | ✅ | 日志不记录敏感信息：`TodoTaskDO.java:102-107`、`TodoTaskCreateRequest.java:60-64` 仅打印描述长度；`TodoTaskServiceImpl.java:67-68` 不打印描述全文（符合系分 §6.4.3.2） |
| S9.3 | N/A | 传输加密由接入层/网关承担，本仓库无 TLS 配置范围 |
| S9.4 | N/A | 无随机数使用 |
| S10.1 | N/A | 无 Cookie/Session 认证，CSRF 防护由平台与前端承担 |
| S10.2 | N/A | 无 CORS 配置 |
| S10.3 | N/A | 无 URL 跳转 |

---

## Step 5 — 自定义扩展检查（产物 E）

### 5.1 自定义扩展（`customized-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| U1.1 | N/A(未启用自定义规则) | `customized-checklist.md` 为未启用的示例项；本仓库未配置团队自定义规则。注：示例项语义（Controller 入参使用 `@Valid`）在当前实现中恰好满足（`TodoTaskController.java:39`） |
| U1.2 | N/A(未启用自定义规则) | 同上，无团队工程约束规则配置 |
| U1.3 | N/A(未启用自定义规则) | 同上 |
| U2.1 | N/A(未启用自定义规则) | 同上，无业务红线规则配置 |
| U2.2 | N/A(未启用自定义规则) | 同上 |
| U2.3 | N/A(未启用自定义规则) | 同上 |

---

## 终检（防漏检）

- [x] 执行队列中每个文件 `Step2`、`Step3`、**S1–S10 / G1–G17** 各列均非 `⬜`（跳过文件除外）
- [x] Step 2 的每个 REQ/Scenario 均非 `⬜`（REQ-1 ~ REQ-11）
- [x] Step 3 的 A1–A7 均非 `⬜`
- [x] Step 4 全部 **G/S** 与 **B001–B081 / M001–M027 / I001–I010** ID 均非 `⬜`（`N/A` 均已注明原因）
- [x] Step 5 全部 U* ID 均非 `⬜`（`N/A(未启用自定义规则)`）
- [x] 执行队列 `⬜ 待审` 计数 = 0；已审队列（28 个 `.java`）与报告 §1 审查范围一致
- [x] 所有 `❌/⚠️` 已写入 report §3–§8，且包含 `ID + path:line`
