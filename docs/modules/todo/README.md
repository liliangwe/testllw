# todo 模块

## 模块职责

待办事项记录的创建与管理（当前最小闭环仅含创建功能）。

## 关键类

| 类 | 类型 | 说明 |
|---|------|------|
| `TodoItemDO` | 数据对象 | 映射 `todo_item` 表 |
| `TodoItemVO` | 视图对象 | 接口返回的待办事项信息 |
| `TodoCreateRequest` | 请求 DTO | 新增待办事项入参，含 `@NotBlank`/`@Size` 校验 |
| `TodoItemMapper` | MyBatis Mapper | 数据访问接口 |
| `TodoService` | 服务接口 | 业务逻辑契约 |
| `TodoServiceImpl` | 服务实现 | 业务逻辑实现 |
| `TodoController` | REST 控制器 | POST `/api/todo/create` |

## 依赖关系

- Controller → TodoService → TodoItemMapper → MySQL
- 无外部模块依赖

## API 接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/todo/create` | 新增待办事项 |

### 请求示例

```json
{
  "todoName": "完成系分设计文档",
  "description": "输出待办事项模块的完整系分设计"
}
```

### 响应示例

```json
{
  "result": "OK",
  "msg": "SUCCESS",
  "data": {
    "id": 1,
    "todoName": "完成系分设计文档",
    "description": "输出待办事项模块的完整系分设计",
    "gmtCreate": "2026-10-14 10:30:00"
  }
}
```

### 错误码

| 错误码 | 说明 |
|--------|------|
| PARAM_INVALID | 参数校验失败 |
| SYSTEM_ERROR | 系统内部错误 |

## 数据库

表名：`todo_item`（详见 `src/main/resources/db/migration/V1__create_todo_item.sql`）