# 待办事项应用 — 架构文档

## 1. 项目概述

**项目名称**: todo-app（待办事项管理应用）

**技术栈**: Java 21 + Spring Boot 3.2 + MyBatis 3.0 + MySQL 8.0

**目标**: 提供轻量级日常待办事项记录功能，当前最小闭环为创建待办事项。

## 2. 模块边界

| 模块 | 职责 | 依赖 |
|------|------|------|
| todo | 待办事项创建与持久化，提供 REST API | MySQL 数据库 |

## 3. 分层架构

```
┌─────────────────────────────────────────┐
│              Controller 层               │
│  TodoController — REST 接口入口          │
├─────────────────────────────────────────┤
│              Service 层                  │
│  TodoService / TodoServiceImpl — 业务逻辑│
├─────────────────────────────────────────┤
│              Mapper 层                   │
│  TodoItemMapper — 数据访问               │
├─────────────────────────────────────────┤
│              Entity 层                   │
│  TodoItemDO / TodoStatus — 数据实体      │
└─────────────────────────────────────────┘
```

## 4. 工程结构

```
todo-app/
├── pom.xml
├── src/main/java/com/example/todo/
│   ├── TodoApplication.java          # 启动类
│   ├── common/
│   │   ├── Result.java               # 统一响应
│   │   └── BizException.java         # 业务异常
│   ├── controller/
│   │   └── TodoController.java       # 控制器
│   ├── dto/
│   │   ├── TodoCreateRequestDTO.java # 创建请求
│   │   └── TodoCreateResultDTO.java  # 创建结果
│   ├── entity/
│   │   ├── TodoItemDO.java           # 数据对象
│   │   └── TodoStatus.java           # 状态枚举
│   ├── mapper/
│   │   └── TodoItemMapper.java       # Mapper 接口
│   └── service/
│       ├── TodoService.java          # 服务接口
│       └── impl/
│           └── TodoServiceImpl.java  # 服务实现
├── src/main/resources/
│   ├── application.yml               # 应用配置
│   ├── db/schema.sql                 # 建表 DDL
│   └── mapper/TodoItemMapper.xml     # MyBatis XML
└── src/test/java/com/example/todo/
    ├── controller/
    │   └── TodoControllerTest.java   # Controller 测试
    └── service/
        └── TodoServiceImplTest.java  # Service 测试
```

## 5. 数据模型

### todo_item（待办事项表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | 主键 |
| name | VARCHAR(200) | NOT NULL | 待办名称 |
| description | VARCHAR(2000) | NULL | 待办描述 |
| status | VARCHAR(32) | NOT NULL DEFAULT 'PENDING' | 状态 |
| gmt_create | DATETIME | NOT NULL | 创建时间 |
| gmt_modified | DATETIME | NOT NULL | 修改时间 |

## 6. API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/todo/create | 新增待办事项 |

## 7. 约束与约定

- 错误码格式: `TODO_{SEQ}`（如 TODO_001）
- 响应结构: `{ "code": "OK", "msg": "SUCCESS", "data": {} }`
- JSON key: lowerCamelCase
- 参数校验: Controller 层 `@Valid` + Service 层手动校验
- 事务: Service 层 `@Transactional(rollbackFor = Exception.class)`