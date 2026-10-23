# todo-service 架构文档

## 1. 项目概述

待办事项管理服务，提供日常待办事项的记录能力。当前最小闭环聚焦"新增待办事项"。

- **技术栈**：Spring Boot 3.2 + MyBatis + MySQL
- **Java 版本**：JDK 21
- **包路径**：`com.antdigital.todo`

## 2. 模块边界

| 模块 | 职责 | 依赖 |
|------|------|------|
| todo | 待办事项 CRUD（当前仅 Create），业务校验，数据持久化 | MyBatis Mapper、MySQL |

## 3. 分层架构

```
com.antdigital.todo
├── controller/       # Web 层：REST 接口
├── service/          # Service 层：业务逻辑
│   └── impl/         # 服务实现
├── dao/mapper/       # DAO 层：数据访问
├── model/
│   ├── entity/       # 数据对象 (DO)
│   └── dto/          # 视图对象 (VO) / 请求对象
└── common/           # 公共类
```

## 4. 模块列表

| 模块文档 | 路径 |
|----------|------|
| todo | [docs/modules/todo/README.md](modules/todo/README.md) |

## 5. 约束

- 数据库命名：全小写 + 下划线，主键 `pk_` 前缀，普通索引 `idx_` 前缀
- 每表必备 `id`、`gmt_create`、`gmt_modified` 字段
- 接口遵循 oneapi REST 风格，JSON key lowerCamelCase
- 响应体统一使用 `ApiResponse<T>` 封装