# todo 模块

> 更新日期：2026/10/14

## 职责

待办事项模块，负责内部用户待办事项的创建与管理。本期为最小闭环，仅实现「新增待办事项」（F01），
事项信息包含事项名称（必填）与事项描述（选填）。

## 关键类

| 类 | 职责 |
|----|------|
| `TodoController` | oneapi 接入层，`POST /api/todo`，执行 R01/R02 参数校验并透传租户 ID |
| `TodoService` / `TodoServiceImpl` | 业务服务，校验租户信息（R04）、组装实体、持久化、转换出参（R03） |
| `TodoItemMapper` / `TodoItemMapper.xml` | 数据访问，`saveTodoItem` 新增记录并回填自增主键 |
| `TodoItemDO` | 数据对象，对应表 `todo_item` |
| `TodoItemDTO` | 出参对象：`id` / `name` / `description` / `gmtCreate` |
| `CreateTodoRequest` | 入参对象：`name`（必填，≤200）/ `description`（选填，≤2000） |
| `ApiResponse` | 统一出参结构 `{code, msg, data}` |
| `TodoErrorCode` | 模块错误码与提示信息常量 |
| `BusinessException` / `GlobalExceptionHandler` | 业务异常与全局异常转换 |

## 依赖

- 向下依赖：`dao`（MyBatis + MySQL，表 `todo_item`）
- 向下依赖：`common`（错误码、异常、统一出参）、`model`（请求/传输对象）
- 外部依赖：统一认证拦截器提供的登录上下文（租户 ID，系分假设 A01，本期以请求头 `X-Tenant-Id` 透传）
- 不得依赖：其他业务模块（当前不存在）

## API 接口列表

| 编号 | 接口名称 | 方法 | 路径 | 入参 | 出参 |
|------|----------|------|------|------|------|
| W01 | 新增待办事项 | POST | `/api/todo` | Body：`name`（必填，≤200）、`description`（选填，≤2000）；Header：`X-Tenant-Id`（租户 ID） | `ApiResponse<TodoItemDTO>` |

请求示例：

```json
{
  "name": "完成系分设计文档",
  "description": "完成待办事项模块的系分设计并输出 design.md"
}
```

成功响应示例：

```json
{
  "code": "SUCCESS",
  "msg": "SUCCESS",
  "data": {
    "id": 1,
    "name": "完成系分设计文档",
    "description": "完成待办事项模块的系分设计并输出 design.md",
    "gmtCreate": "2026-10-14 10:00:00"
  }
}
```

失败响应示例（HTTP 200，参数校验失败）：

```json
{
  "code": "TODO_001",
  "msg": "事项名称不能为空",
  "data": null
}
```

## 数据模型

表 `todo_item`（待办事项表）：

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|----------|------|------|
| id | bigint | PK, 自增, NOT NULL | 系统自增主键 |
| tenant_id | varchar(32) | NOT NULL, idx_todo_item_tenant_id | 租户 ID |
| name | varchar(200) | NOT NULL | 事项名称 |
| description | varchar(2000) | NULL | 事项描述 |
| gmt_create | datetime | NOT NULL, idx_todo_item_gmt_create | 创建时间 |
| gmt_modified | datetime | NOT NULL | 修改时间 |

## 错误码

| 错误码 | 说明 | HTTP 状态码 |
|--------|------|-------------|
| SUCCESS | 操作成功 | 200 |
| TODO_001 | 参数校验失败：事项名称为空或超长 | 200 |
| TODO_002 | 参数校验失败：描述内容超长 | 200 |
| TODO_003 | 获取租户信息失败 | 200 |
| TODO_500 | 系统内部错误（含数据库访问异常） | 500 |

## 业务规则

| 规则编号 | 规则描述 | 校验位置 |
|----------|----------|----------|
| R01 | 事项名称不能为空 | `CreateTodoRequest`（`@NotBlank`） |
| R02 | 事项名称长度 ≤ 200 字符 | `CreateTodoRequest`（`@Size`） |
| R03 | 事项描述长度 ≤ 2000 字符 | `CreateTodoRequest`（`@Size`） |
| R04 | 创建成功后返回完整的待办事项信息 | `TodoServiceImpl.convertToDTO` |
| R05 | 租户 ID 必须来自登录上下文，缺失时返回 TODO_003 | `TodoServiceImpl.createTodo` |

## 实现偏差记录

| 偏差项 | 系分方案 | 实现 | 原因 |
|--------|----------|------|------|
| Service 方法签名 | `TodoItemDTO createTodo(CreateTodoRequest request)` | `TodoItemDTO createTodo(CreateTodoRequest request, String tenantId)` | 本期不实现认证（系分假设 A01），租户 ID 由 Web 层从登录上下文（当前以请求头 `X-Tenant-Id` 透传）解析后传入，Service 层不依赖 Web 上下文 |
| 错误码格式 | `TODO_{SEQ}`（系分 5. 功能模块设计） | 同系分 | 系分为本项目已验证契约，优先于通用规范中的 `A0001/B0001` 五位格式 |
