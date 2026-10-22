# Task 1: 实现待办事项创建功能

## 需求描述

创建待办事项功能，帮助内部用户记录日常待办事项。

### 核心功能：仅创建

- 新增待办事项
- 事项信息：事项名称（name/title）和描述（description）

### 目标用户

内部用户

### 最小闭环

仅创建（不需要查询、更新、删除）

## 技术约束

- 语言：Java
- 框架：Spring Boot
- 构建工具：Maven（pom.xml）
- 存储：内存（ConcurrentHashMap）
- API：REST POST /api/todos

## 数据模型

```
Todo:
  - id: String (UUID)
  - name: String (必填，事项名称)
  - description: String (描述)
  - createdAt: String (ISO 8601 创建时间)
```

## API 规格

```
POST /api/todos
Content-Type: application/json

Request Body:
{
  "name": "string (必填)",
  "description": "string (可选)"
}

Response (201 Created):
{
  "id": "uuid-string",
  "name": "string",
  "description": "string",
  "createdAt": "2026-10-22T00:00:00Z"
}
```

## 验收标准

1. POST /api/todos 成功创建并返回 201 + Todo JSON
2. name 为空时返回 400 Bad Request
3. 每次创建生成唯一 UUID
4. 应用可启动并接受请求

## 文件清单

- pom.xml（Spring Boot + Web 依赖）
- src/main/java/com/example/todo/TodoApplication.java
- src/main/java/com/example/todo/model/Todo.java
- src/main/java/com/example/todo/controller/TodoController.java
- src/main/java/com/example/todo/service/TodoService.java