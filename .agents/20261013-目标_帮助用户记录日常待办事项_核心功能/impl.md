> **文档元信息**
>
> | 项目 | 内容 |
> |------|------|
> | 文档类型 | 编码实现报告（编码实现阶段产出） |
> | 对应系分 | `.agents/20261013-目标_帮助用户记录日常待办事项_核心功能/design.md` |
> | 作者 | AiWork |
> | 创建日期 | 2026-10-13 |
> | 采用规范 | `dtazziboot-java-coding-standards`（naming / exception-logging / unit-testing / security / mysql / constants / frontend-backend / project-structure） |

# 待办事项（Todo）新增能力 编码实现报告

## 0. 模块进度追踪

| 序号 | 模块 | READ | TEST | IMPL | CHECK | DOCS | 状态 |
|:----:|------|:----:|:----:|:----:|:-----:|:----:|------|
| 1 | todo | ✅ | ✅ | ✅ | ✅ | ✅ | 已完成 |

---

## 1. READ（读取上下文）

### 📖 READ: todo

**模块职责**：待办事项创建受理、入参校验、创建人与租户归属、持久化、埋点留痕（系分 5.1 todo 模块）。

**关键类列表**：

- `TodoTaskController` - 控制器（W01 `POST /api/todo/task/create`）
- `TodoExceptionHandler` - 统一异常处理（错误码翻译）
- `TodoTaskCreateRequest` / `TodoTaskCreateResponse` / `ApiResult` - 请求、出参与统一响应
- `TodoTaskService` / `TodoTaskServiceImpl` - 业务服务（S01）
- `TodoTaskMapper` / `TodoTaskMapperImpl` - 数据访问（单表单条 INSERT）
- `TodoTaskDO` - 数据对象（表 `todo_task`）
- `TodoErrorCodeEnum` / `TodoTaskConstants` / `TodoResultConstants` / `TodoTipConstants` - 错误码与常量
- `InvokerContext` / `ThreadLocalInvokerContext` - 登录态与租户上下文
- `TodoTaskCreateSwitch` / `TenantTailTodoTaskCreateSwitch` - 应急开关与按租户尾号灰度
- `TodoTaskMonitor` / `LogTodoTaskMonitor` - 埋点 `todo.task.create`

**依赖关系**：MySQL/OB（JDBC，强依赖，异常时快速失败）；平台统一登录态与租户上下文（进程内）。无缓存、无 MQ、无外部系统集成。

**已加载规范**：

- [x] naming.md（类/方法/常量/分层命名、Impl 后缀、Enum 后缀）
- [x] exception-logging.md（错误码、自定义异常、SLF4J 占位符、禁止 System.out）
- [x] unit-testing.md（JUnit 5 + Mockito + AssertJ、AAA、回查验证、边界与分支覆盖）
- [x] security.md（SQL 参数化、创建人取登录态不可伪造、日志不打印描述全文）
- [x] mysql.md（表必备三字段、禁用 timestamp/float/enum、索引前缀）
- [x] constants.md（禁止魔法值、常量按功能归类、类内共享常量）
- [x] frontend-backend.md（lowerCamelCase key、空数据、时间格式 `yyyy-MM-dd HH:mm:ss`）
- [x] project-structure.md（api/service/dao/model 分层、接口与实现分离、common 包）

**仓库现状事实（影响决策）**：仓库此前的基线仅有根目录 `HelloWorld.java`，**无构建脚本、无源码目录结构、无任何第三方依赖**；系分假设 A02「沿用数科业务 Java（Spring Boot）标准技术栈」标注为「待确认」。

---

## 2. TEST（先写单测后写实现）

### 🧪 TEST: todo

**测试文件**：

| 测试类 | 被测类 | 说明 |
|--------|--------|------|
| `src/test/java/com/antdigital/todo/service/impl/TodoTaskServiceImplTest.java` | `TodoTaskServiceImpl` | 业务主干：正常路径、入参兜底、归属与租户、应急开关、落库异常 |
| `src/test/java/com/antdigital/todo/api/controller/TodoTaskControllerTest.java` | `TodoTaskController` + `TodoExceptionHandler` | W01 请求/响应契约与错误码映射（MockMvc standalone） |
| `src/test/java/com/antdigital/todo/api/request/TodoTaskCreateRequestTest.java` | `TodoTaskCreateRequest` | 声明式校验（R01/R02）与名称规范化边界 |
| `src/test/java/com/antdigital/todo/common/enums/TodoErrorCodeEnumTest.java` | `TodoErrorCodeEnum` | 错误码双向映射与枚举值冻结 |
| `src/test/java/com/antdigital/todo/common/feature/impl/TenantTailTodoTaskCreateSwitchTest.java` | `TenantTailTodoTaskCreateSwitch` | 灰度尾号与总开关逻辑 |

**测试方法列表**：

| 方法 | 测试场景 | 状态 |
|------|----------|:----:|
| should_persistAllFieldsAndReturnResultView_when_requestValid | 正常路径：出参视图 + 回查落库实体（归属/审计字段）+ 埋点 | ✅ |
| should_trimTaskNameAndStoreEmptyDesc_when_descIsNullAndNameHasSpaces | 名称去空白、描述为空存空串 | ✅ |
| should_throwParamInvalid_when_taskNameIsBlank | 参数校验：null/空串/空白/全角空格（参数化） | ✅ |
| should_throwParamInvalid_when_taskNameExceedsMaxLength | 边界：名称 65 字符 | ✅ |
| should_throwParamInvalid_when_taskDescExceedsMaxLength | 边界：描述 513 字符 | ✅ |
| should_throwParamInvalid_when_requestIsNull | 异常：请求体缺失 | ✅ |
| should_throwInvokerMissing_when_creatorIsBlank | 登录态缺失 → TODO_002 | ✅ |
| should_throwTenantMissing_when_tenantAbsent | 租户缺失 → TODO_003 | ✅ |
| should_throwCreateDisabled_when_switchClosed | 应急开关关闭 → TODO_005 | ✅ |
| should_throwSaveFailed_when_mapperThrowsDataAccessException | 落库异常 → TODO_004 + 根因保留 + 失败埋点 | ✅ |
| should_throwSaveFailed_when_mapperReturnsNullId | 自增主键缺失 → TODO_004 | ✅ |
| should_returnOkResult_when_createSuccess | W01 成功响应结构 `{result:OK, msg:SUCCESS, data:{taskId,taskName,gmtCreate}}` | ✅ |
| should_returnTodo001_when_taskNameIsBlank | 声明式校验失败 → TODO_001 + 字段提示 | ✅ |
| should_returnTodo001_when_bodyUnreadable | 报文非法 → TODO_001 语义 | ✅ |
| should_returnTodo005_when_createSwitchClosed | 业务异常 → TODO_005 映射 | ✅ |
| should_returnTodo004_when_unexpectedExceptionOccurs | 未知异常兜底 → TODO_004 | ✅ |
| should_haveNoViolation_when_requestValid | 校验通过 | ✅ |
| should_stripSurroundingSpaces_when_setTaskName | 写入时去首尾空白 | ✅ |
| should_violate_when_taskNameIsBlank / ExceedsMaxLength | 校验失败（含全角空格） | ✅ |
| should_notViolate_when_taskNameLengthIsMaxBoundary | 边界：名称 64 字符 | ✅ |
| should_violate_when_taskDescExceedsMaxLength | 边界：描述 513 字符 | ✅ |
| should_notViolate_when_taskDescIsNull / LengthIsMaxBoundary | 选填与边界：null、512 字符 | ✅ |
| should_returnSameEnum_when_getByCode | 错误码双向映射（全枚举参数化） | ✅ |
| should_keepFrozenErrorCodeSize_when_enumEvolves | 枚举值数量冻结 | ✅ |
| should_throwIllegalArgumentException_when_codeUnknown / codeIsNull | 无效错误码处理 | ✅ |
| should_disableAllTenants_when_globalSwitchClosed | 总开关关闭：全部拒绝 | ✅ |
| should_enableAllTenants_when_tailListEmpty | 灰度列表为空：全量放通 | ✅ |
| should_enableTenant_when_tailMatched / disableTenant_when_tailNotMatched | 尾号命中/未命中 | ✅ |
| should_disableTenant_when_tenantIdIsBlank | 租户为空 | ✅ |
| should_ignoreIllegalTailItem_when_configIllegal | 非法配置项忽略 | ✅ |

**测试覆盖摘要**：

- 被测类：`TodoTaskServiceImpl`（主）、`TodoTaskController`、`TodoTaskCreateRequest`、`TodoErrorCodeEnum`、`TenantTailTodoTaskCreateSwitch`
- 测试方法数：34 个方法（含参数化展开后共执行 46 个用例）
- 覆盖场景：正常路径 ✓、参数校验 ✓、异常处理 ✓、边界值 ✓、分支覆盖 ✓、枚举值全量 ✓
- 未覆盖/建议补充：真实 MySQL 的 `TodoTaskMapperImpl` SQL 执行（需内存库/Testcontainers，见第 5 节待人工验证）

---

## 3. IMPL（实现代码）

### 🔧 IMPL: todo

**已实现文件**：

构建与配置：

- `pom.xml`（Spring Boot 3.2.5 / Java 21 / web + validation + jdbc + mysql + test）
- `src/main/resources/application.yml`（数据源、连接超时、应急开关与灰度配置）
- `src/main/resources/db/schema/todo_task.sql`（建表 DDL）
- `.gitignore`

主代码（`src/main/java/com/antdigital/todo/`）：

- `TodoApplication.java`
- `api/controller/TodoTaskController.java`、`api/controller/TodoExceptionHandler.java`
- `api/request/TodoTaskCreateRequest.java`
- `api/response/ApiResult.java`、`api/response/TodoTaskCreateResponse.java`
- `service/TodoTaskService.java`、`service/impl/TodoTaskServiceImpl.java`
- `dao/entity/TodoTaskDO.java`、`dao/mapper/TodoTaskMapper.java`、`dao/mapper/impl/TodoTaskMapperImpl.java`
- `common/constant/TodoTaskConstants.java`、`common/constant/TodoResultConstants.java`、`common/constant/TodoTipConstants.java`
- `common/enums/TodoErrorCodeEnum.java`、`common/exception/TodoBizException.java`
- `common/context/InvokerContext.java`、`common/context/ThreadLocalInvokerContext.java`
- `common/feature/TodoTaskCreateSwitch.java`、`common/feature/impl/TenantTailTodoTaskCreateSwitch.java`
- `common/config/TodoTimeConfig.java`
- `common/monitor/TodoTaskMonitor.java`、`common/monitor/LogTodoTaskMonitor.java`

测试代码（`src/test/java/com/antdigital/todo/`）：见第 2 节测试文件表。

**需求（功能点）落点对照**：

| 编号 | 功能点 | 实现落点 |
|------|--------|----------|
| F01 | 新增待办事项 | `TodoTaskController#createTodoTask` → `TodoTaskServiceImpl#createTodoTask` → `TodoTaskMapper#insert` |
| F02 | 录入事项名称并持久化 | `TodoTaskCreateRequest#taskName`（`@NotBlank`/`@Size`）+ `normalizeTaskName`（strip、≤64） |
| F03 | 录入事项描述并持久化 | `TodoTaskCreateRequest#taskDesc`（`@Size` ≤512）+ `normalizeTaskDesc`（空→空串） |
| F04 | 创建人归属与租户隔离 | `requireCreator`（TODO_002）、`requireTenantId`（TODO_003），取自 `InvokerContext`，不信任入参 |
| F05 | 创建入参校验与错误提示 | Controller `@Valid` + Service 兜底（R10）+ `TodoExceptionHandler` 错误码翻译 |
| F06 | 埋点与日志留痕 | `TodoTaskMonitor#reportCreate`（`todo.task.create`）+ 成功 INFO / 异常 WARN、ERROR（均不打印描述全文） |

**业务规则落点**：R01 `normalizeTaskName`；R02 `normalizeTaskDesc`；R03 接口仅接受单条对象；R04 `requireCreator`；R05 `requireTenantId`；R06 不做唯一性校验（DDL 无唯一索引）；R07 `requireCreateEnabled`；R08 `buildTodoTask` 赋值 `is_deleted=0` 与 `gmt_create/gmt_modified`；R09 租户写入即水平权限边界；R10 Service 兜底校验独立于 Controller。

**编译验证**：见第 4 节 L2 动态验证。

---

## 4. CHECK（规范检查）

### ✅ CHECK: todo

#### L1 静态检查

| 检查项 | 规范要求 | 符合情况 |
|--------|----------|:--------:|
| 命名规范 | 类名大驼峰、方法/变量小驼峰、常量全大写、Service 实现类 `Impl` 后缀、枚举 `Enum` 后缀 | ✅ |
| 分层与包结构 | api/service/dao/common 分层，接口与实现分离，下层不依赖上层 | ✅ |
| 数科网关/接口 | 路径 `/api/todo/task/create`、响应结构 `{result, msg, data}`、字段 lowerCamelCase、时间 `yyyy-MM-dd HH:mm:ss` | ✅ |
| 异常日志 | `TodoBizException`（unchecked 自定义异常）、SLF4J 占位符、无 `System.out`/`printStackTrace`、POJO 覆写 `toString` | ✅ |
| 安全规范 | INSERT 全部使用 `?` 占位符；创建人/租户取登录态（不可伪造）；描述不入日志全文 | ✅ |
| MySQL 规范 | 表/字段小写下划线、必备 `id/gmt_create/gmt_modified`、`datetime` 替代 `timestamp`、`tinyint` 替代 `enum`、`pk_todo_task` 命名、字段注释齐全 | ✅ |
| 常量规范 | 无魔法值（长度、逻辑删除标记、结果码、提示文案均常量化并按功能归类） | ✅ |
| 单元测试 | 5 个测试类、AAA 结构、断言具体值、写操作回查验证、Mock 仅用于外部依赖 | ✅ |

#### L2 动态验证

| 验证项 | 状态 | 说明 |
|--------|:----:|------|
| 编译验证 | ✅ | 主代码与测试代码均编译通过（`target/classes`、`target/test-classes`） |
| 单测验证 | ✅ | `mvn -B -ntp test`：**Tests run: 46, Failures: 0, Errors: 0, Skipped: 0**，`BUILD SUCCESS`，总耗时 3:09 min（含首次依赖下载） |

**执行环境说明**：运行环境初始无 JDK/Maven（`java: command not found`），普通包管理安装被沙箱拒绝（`apt-get: seteuid ... Operation not permitted`），
故通过直接下载方式临时安装 **Amazon Corretto JDK 21.0.12.1 + Apache Maven 3.9.9 到 `/tmp/tools`**（不进入仓库、不影响交付物）后执行构建与单测。

**单测分项结果**（来源 `target/surefire-reports/*.txt`）：

| 测试类 | 用例数 | 失败 | 错误 | 跳过 |
|--------|:------:|:----:|:----:|:----:|
| `TodoTaskServiceImplTest` | 15 | 0 | 0 | 0 |
| `TodoTaskControllerTest` | 5 | 0 | 0 | 0 |
| `TodoTaskCreateRequestTest` | 12 | 0 | 0 | 0 |
| `TodoErrorCodeEnumTest` | 8 | 0 | 0 | 0 |
| `TenantTailTodoTaskCreateSwitchTest` | 6 | 0 | 0 | 0 |
| **合计** | **46** | **0** | **0** | **0** |

#### 📋 待人工验证

以下内容**未在本环境验证**，请在具备 MySQL/OB 实例的环境执行：

```bash
# 1. 建表
mysql -h<host> -u<user> -p <database> < src/main/resources/db/schema/todo_task.sql

# 2. 全量编译与单测（已验证通过，可复跑）
mvn compile -DskipTests
mvn test

# 3. 真实 DB 写入链路（Repo 层 SQL / 自增主键回填）建议补充内存库或 Testcontainers 用例后再验证：
#    TodoTaskMapperImpl 使用 JdbcTemplate 参数化 INSERT + GeneratedKeyHolder，
#    本环境无数据库实例，故未执行真实 SQL（对应系分 5.1.3.1「单表单条 INSERT」）。
```

---

## 5. 自主决策与偏差说明

| 编号 | 事项 | 系分表述 | 实现决策 | 理由 |
|------|------|----------|----------|------|
| D01 | S01 返回类型 | `Long createTodoTask(TodoTaskCreateRequest)` | `TodoTaskCreateResponse createTodoTask(...)`（内含 taskId） | 系分 5.1.2 W01 出参要求返回 `taskId/taskName/gmtCreate` 三项，`Long` 无法承载；以单一方法返回结果视图，避免重复写路径与二次查询（本期无查询能力）。语义上仍满足 S01「新增待办事项」契约 |
| D02 | 名称去空白方式 | 「去首尾空白」（异常场景含全角空格） | 使用 `String.strip()` 而非 `trim()` | `trim()` 仅处理 ≤ U+0020 的字符，无法拦截全角空格 U+3000；`strip()` 覆盖 Unicode 空白，并且名称写入 setter 时即规范化，使声明式校验与落库文本一致 |
| D03 | 应急开关 | 配置项 `todo.task.create.enabled` 默认关闭、按租户尾号灰度 | 抽象为 `TodoTaskCreateSwitch` 接口 + `TenantTailTodoTaskCreateSwitch` 实现，`application.yml` 中 `enabled: true`、`tenant-tail-list: 0,1,2` | 开关可测（接口注入，不依赖 Spring 上下文）；配置默认值按系分 7.2 首批灰度租户（尾号 0-2）设定 |
| D04 | 登录态拦截器 | 由全局统一拦截器校验登录态（系分 6.4.2.3） | 不实现 SSO 拦截器，仅提供 `InvokerContext` 接缝与 `ThreadLocalInvokerContext.set/clear` | 系分明确「本功能不自实现登录」；平台登录态接入不在本仓库范围，避免臆造网关头协议。上下文缺失时按 R04/R05 返回 `TODO_002`/`TODO_003` |
| D05 | 持久化组件 | 「Mapper」（Repository/MyBatis 语义） | `TodoTaskMapper` 接口 + `TodoTaskMapperImpl`（`JdbcTemplate` 参数化 INSERT + `GeneratedKeyHolder` 取自增主键） | 仓库无 MyBatis 配置基线；单表单条写入用 `JdbcTemplate` 即可满足，且保持 Mapper 分层与参数化 SQL 规范；后续如需 XML 映射可平滑替换实现 |
| D06 | 埋点落地形式 | `todo.task.create` 埋点上报 | `TodoTaskMonitor` 接口 + `LogTodoTaskMonitor`（日志落地） | 平台监控 SDK 不在本仓库范围，以接口隔离便于替换为真实埋点上报 |
| D07 | 构建基线 | 系分假设 A02：Spring Boot 标准技术栈（待确认） | 按 Spring Boot 3.2.x + Maven 标准目录结构落地（`pom.xml`、`src/main/java`、`src/test/java`） | 系分为 Controller–Service–Repository 分层设计，Spring Boot 为与之匹配的行业标准技术栈；原仓库仅有裸 `HelloWorld.java`，本次补齐构建基线 |

---

## 6. DOCS（文档与产出）

### 📝 DOCS: todo

**文档操作**：

- 架构文档：`docs/ARCHITECTURE.md`（新建，记录分层、模块清单、技术栈、全局约束）
- 模块文档：`docs/modules/todo/README.md`（新建，记录模块职责、关键类、依赖、API 列表、错误码、表结构）
- 编码报告：本文件 `.agents/20261013-目标_帮助用户记录日常待办事项_核心功能/impl.md`

---

## 7. 模块完成总结

## ✅ 模块 todo 完成

| 阶段 | 状态 |
|------|:----:|
| READ | ✅ |
| TEST | ✅ |
| IMPL | ✅ |
| CHECK | ✅ |
| DOCS | ✅ |

**下一步**：进入代码评审（stage: 代码评审）与问题修复环节。
