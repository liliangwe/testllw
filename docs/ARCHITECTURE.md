# ARCHITECTURE.md

> 项目：待办事项管理系统（todo）
> 更新日期：2026/10/14
> 需求来源：`.agents/20261014-目标_帮助用户记录日常待办事项_核心功能/design.md`

## 模块边界与依赖方向

```
controller (Web 层) → service (Service 层) → dao (DAO 层)
        ↘                  ↘                  ↙
              common（公共层） / model（模型层）
```

本工程为单体 Spring Boot 应用（系分方案 2. 架构与模块 推荐方案），暂不拆分独立模块，按包划分分层边界。

| 分层 | 包路径 | 职责 | 不得包含 |
|------|--------|------|----------|
| Web 层 | `com.example.todo.controller` | REST 接口、入参校验、登录上下文透传 | 直接 SQL、业务编排 |
| Service 层 | `com.example.todo.service` / `service.impl` | 业务规则校验、实体组装、DTO 转换 | HTTP 处理、直接访问数据库连接 |
| DAO 层 | `com.example.todo.dao`（`entity` / `mapper`） | 数据库读写（MyBatis） | 业务逻辑、Controller 依赖 |
| 模型层 | `com.example.todo.model`（`request` / `dto`） | 请求对象、数据传输对象 | 业务逻辑、持久化注解 |
| 公共层 | `com.example.todo.common`（`constant` / `exception`） | 错误码常量、统一出参、业务异常、全局异常处理 | 业务逻辑、Spring Bean 之外的框架依赖 |

**依赖方向约束**：controller → service → dao 单向依赖；公共层与模型层可被各层依赖，禁止反向依赖与循环依赖。

## 配置分层

application.yml < 环境变量（`DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USERNAME` / `DB_PASSWORD`）< 启动参数

敏感配置（数据库密码、密钥）必须通过环境变量或密钥管理服务注入，禁止写入配置文件。

## 错误处理

- 所有业务失败统一抛出 `BusinessException`（含 `errorCode` + `message`），经 `GlobalExceptionHandler` 转换为统一出参 `{code, msg, data}`。
- 错误码集中定义在 `TodoErrorCode`（格式 `TODO_{SEQ}`），禁止在业务代码中硬编码错误码字符串。
- HTTP 状态码约定：业务规则类异常（TODO_001 ~ TODO_003）返回 200，由响应体错误码承载语义；系统级异常（TODO_500）返回 500，便于监控告警。
- 禁止吞掉异常；数据库访问异常由 `DataAccessException` 处理器统一转换为 TODO_500。

## 数据模型

```sql
CREATE TABLE todo_item
(
    id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    tenant_id    VARCHAR(32)   NOT NULL COMMENT '租户ID',
    name         VARCHAR(200)  NOT NULL COMMENT '事项名称',
    description  VARCHAR(2000) DEFAULT NULL COMMENT '事项描述',
    gmt_create   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    INDEX idx_todo_item_tenant_id (tenant_id),
    INDEX idx_todo_item_gmt_create (gmt_create)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '待办事项表';
```

测试库表结构见 `src/test/resources/schema.sql`，字段类型与约束必须与上表保持一致。

## 安全约束

- 所有入参通过 `@Valid` + JSR-303 注解校验，超长/空值在接入层拦截，不进入数据库。
- SQL 全部使用 `#{}` 参数占位符，禁止 `${}` 拼接，禁止 `SELECT *`。
- 租户隔离依赖 `tenant_id`，数据写入必须携带登录上下文解析出的租户 ID，缺失时返回 TODO_003。
- 日志禁止输出未脱敏的 PII 字段，禁止输出数据库密码等凭证，禁止 `System.out` / `printStackTrace`。
- 所有 REST 端点必须经过统一认证拦截器（系分假设 A01：复用现有内部认证体系，本期不实现）。

## 测试策略

- Service 层：纯 JUnit 5 + Mockito 单元测试（`@ExtendWith(MockitoExtension.class)`），Mock DAO，覆盖正常路径、边界值、异常路径，覆盖率 ≥ 60%。
- DAO 层：H2 内存数据库（MySQL 兼容模式）+ `src/test/resources/schema.sql` 验证 MyBatis XML 映射（配置见 `application-test.yml`）。
- Web 层：`@WebMvcTest` + MockMvc 验证入参校验与统一出参结构。
- 禁止在单元测试中连接远程数据库或外部服务。

## AI 编码约束

- 改动前先读本文件与 `docs/modules/todo/README.md`。
- 不跨层调用（Controller 禁止直接调用 Mapper）。
- 新增 REST 端点必须同步更新 `docs/modules/todo/README.md` 的接口列表与权限说明。
- 新增错误码必须登记到 `TodoErrorCode` 与模块文档错误码表。
- 数据库 Schema 变更必须同步 `src/test/resources/schema.sql`。
- 新增依赖必须由 `pom.xml` 显式声明，禁止隐式传递依赖。
