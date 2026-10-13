# todo 模块（待办事项）

## 模块职责

面向内部用户提供「新增待办事项」能力（最小闭环：仅创建）：录入事项名称与描述，创建人与租户取自登录态，
单条 INSERT 落库后返回待办事项 ID、名称与创建时间。

## 关键类

| 分层 | 类 | 职责 |
|------|----|------|
| Web | `api.controller.TodoTaskController` | W01 `POST /api/todo/task/create` 入口，声明式参数校验（`@Valid`） |
| Web | `api.controller.TodoExceptionHandler` | 统一异常处理，将异常翻译为 oneapi 响应 |
| Web | `api.request.TodoTaskCreateRequest` | W01 入参（taskName 必填、taskDesc 选填、requestId 防重预留） |
| Web | `api.response.ApiResult` / `TodoTaskCreateResponse` | 统一响应结构与 W01 出参 data |
| Service | `service.TodoTaskService` / `impl.TodoTaskServiceImpl` | 业务主干：入参兜底校验、归属与租户取登录态、应急开关、落库、埋点 |
| DAO | `dao.mapper.TodoTaskMapper` / `impl.TodoTaskMapperImpl` | 单表单条参数化 INSERT，返回自增主键 |
| DAO | `dao.entity.TodoTaskDO` | `todo_task` 表数据对象 |
| 公共 | `common.context.InvokerContext` | 登录态/租户上下文接缝（平台登录拦截器写入） |
| 公共 | `common.context.TodoInvokerContextInterceptor` | 登录态接线拦截器：把平台写入的请求属性接入上下文，请求结束 `afterCompletion` 清理 |
| 公共 | `common.config.TodoWebConfig` | 注册登录态接线拦截器到 `/api/todo/**`（`todo.invoker.enabled=false` 可关闭） |
| 公共 | `common.feature.TodoTaskCreateSwitch` | 创建应急开关与按租户尾号灰度 |
| 公共 | `common.enums.TodoErrorCodeEnum` | 错误码 TODO_001 ~ TODO_005 |
| 公共 | `common.monitor.TodoTaskMonitor` | 埋点 `todo.task.create` 上报 |

## 依赖关系

- 强依赖：MySQL/OB（创建必须落库，异常时快速失败返回 `TODO_004`，不重试）。
- 进程内依赖：平台统一登录态与租户上下文（`InvokerContext`；平台登录拦截器校验票据后写入请求属性，
  由 `TodoInvokerContextInterceptor` 接入，属性缺失时 fail-closed 返回 `TODO_002`/`TODO_003`）。
- 无缓存、无 MQ、无外部系统集成。

## 配置项（`src/main/resources/application.yml`）

| 配置键 | 默认值 | 说明 |
|--------|--------|------|
| `spring.datasource.username` / `password` | 无（必填） | 由环境变量 `TODO_DB_USERNAME` / `TODO_DB_PASSWORD` 注入，仓库不落明文口令 |
| `spring.datasource.url` | — | 含 `connectTimeout=2000`、`socketTimeout=2000`，与 Hikari `connection-timeout` 一起兑现「连接与语句超时 ≤ 2s」 |
| `todo.time.zone` | `Asia/Shanghai` | 应用时钟时区，须与数据源 `serverTimezone` 一致 |
| `todo.invoker.enabled` | `true` | 是否启用本模块登录态接线拦截器 |
| `todo.invoker.invoker-attribute` | `todo.invoker` | 创建人请求属性名（按平台约定覆盖） |
| `todo.invoker.tenant-attribute` | `todo.tenant-id` | 租户标识请求属性名（按平台约定覆盖） |
| `todo.task.create.enabled` | `false` | 创建应急开关（系分 7.3 默认关闭）；**每次调用实时读取**，配置推送后免重启/发布生效 |
| `todo.task.create.tenant-tail-list` | `0,1,2` | 灰度租户尾号列表，为空表示灰度批次全量放通 |

## API 接口列表

| 编号 | 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|------|
| W01 | 新增待办事项 | POST | `/api/todo/task/create` | 入参 `taskName`（必填，≤64）、`taskDesc`（选填，≤512） |
| S01 | 新增待办事项 | - | `TodoTaskService#createTodoTask` | 返回 `TodoTaskCreateResponse`（taskId/taskName/gmtCreate） |

### 错误码

| 错误码 | 说明 |
|--------|------|
| TODO_001 | 参数校验失败（名称为空/超长、描述超长） |
| TODO_002 | 未获取到登录用户，登录态失效 |
| TODO_003 | 未获取到租户信息 |
| TODO_004 | 系统繁忙，待办事项保存失败 |
| TODO_005 | 创建功能已关闭（应急开关关闭） |

> 请求方式不支持（405）、内容类型不支持（415）等协议层错误按 `TODO_001` 返回并记 WARN，
> 与系统异常 `TODO_004` 区分（G10.1）。

## 数据模型

表 `todo_task`（建表脚本：`src/main/resources/db/schema/todo_task.sql`）：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键，自增 |
| tenant_id | varchar(32) | 租户标识，租户隔离维度 |
| task_name | varchar(64) | 事项名称，必填 |
| task_desc | varchar(512) | 事项描述，选填，空时存空串 |
| creator | varchar(64) | 创建人，取自登录态 |
| is_deleted | tinyint | 逻辑删除标记：0-未删除，1-已删除 |
| gmt_create | datetime | 创建时间，代码侧维护 |
| gmt_modified | datetime | 修改时间，代码侧维护 |

索引：`pk_todo_task (id)`；本期无二级索引（无检索诉求）。

## 测试

```
mvn test
```

覆盖：`TodoTaskServiceImplTest`（正常/校验/归属/开关/落库异常）、`TodoTaskControllerTest`（W01 契约与错误码映射）、
`TodoTaskCreateRequestTest`（声明式校验边界）、`TodoErrorCodeEnumTest`、
`TenantTailTodoTaskCreateSwitchTest`（灰度/应急开关，含配置变更免重启生效）、
`TodoInvokerContextInterceptorTest`（登录态接线、身份不可由入参覆盖、上下文清理）、
`TodoTaskMapperImplTest`（参数绑定顺序、语句超时、自增主键回填与缺失分支）。
