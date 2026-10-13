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
| 公共 | `common.feature.TodoTaskCreateSwitch` | 创建应急开关与按租户尾号灰度 |
| 公共 | `common.enums.TodoErrorCodeEnum` | 错误码 TODO_001 ~ TODO_005 |
| 公共 | `common.monitor.TodoTaskMonitor` | 埋点 `todo.task.create` 上报 |

## 依赖关系

- 强依赖：MySQL/OB（创建必须落库，异常时快速失败返回 `TODO_004`，不重试）。
- 进程内依赖：平台统一登录态与租户上下文（`InvokerContext`）。
- 无缓存、无 MQ、无外部系统集成。

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
`TodoTaskCreateRequestTest`（声明式校验边界）、`TodoErrorCodeEnumTest`、`TenantTailTodoTaskCreateSwitchTest`。
