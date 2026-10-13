# todo 模块

## 模块职责

待办事项的创建与持久化，通过 REST API 对外提供服务。

## 关键类

| 类 | 类型 | 说明 |
|------|------|------|
| TodoApplication | 启动类 | Spring Boot 应用入口 |
| TodoController | Controller | REST 接口，处理 POST /api/todo/create |
| TodoService | 接口 | 待办服务接口 |
| TodoServiceImpl | Service 实现 | 业务逻辑：参数校验、实体构建、持久化 |
| TodoItemMapper | Mapper | MyBatis 数据访问 |
| TodoItemDO | Entity | 数据对象，映射 todo_item 表 |
| TodoStatus | Enum | 状态枚举：PENDING / COMPLETED |
| TodoCreateRequestDTO | DTO | 创建请求体 |
| TodoCreateResultDTO | DTO | 创建响应体 |
| Result | 通用类 | 统一响应结构 |
| BizException | 异常类 | 业务异常，含 errorCode |

## 依赖关系

- MySQL 数据库（JDBC via MyBatis）
- 无外部服务依赖

## API 接口

### POST /api/todo/create

**请求示例**:
```json
{
  "name": "完成系分设计文档",
  "description": "完成待办事项模块的系统分析与设计文档编写"
}
```

**响应示例**:
```json
{
  "code": "OK",
  "msg": "SUCCESS",
  "data": {
    "id": 1,
    "name": "完成系分设计文档",
    "description": "完成待办事项模块的系统分析与设计文档编写",
    "status": "PENDING",
    "gmtCreate": "2026-10-13T10:30:00"
  }
}
```

**错误码**:

| 错误码 | 说明 |
|--------|------|
| TODO_001 | 事项名称不能为空 |
| TODO_002 | 事项名称不能超过200字符 |
| TODO_003 | 事项描述不能超过2000字符 |
| TODO_999 | 系统内部错误 |