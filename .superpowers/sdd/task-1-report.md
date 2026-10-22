# Task 1 Report: 实现待办事项创建功能

## 实现内容

基于 Spring Boot 3.4.4 + Java 21 实现了待办事项创建 REST API。

### 文件清单

| 文件 | 说明 |
|------|------|
| `pom.xml` | Maven 构建文件，依赖 spring-boot-starter-web + test |
| `src/main/java/com/example/todo/TodoApplication.java` | Spring Boot 入口 |
| `src/main/java/com/example/todo/model/Todo.java` | 不可变数据模型（id/name/description/createdAt） |
| `src/main/java/com/example/todo/controller/TodoController.java` | REST 控制器，POST /api/todos |
| `src/main/java/com/example/todo/service/TodoService.java` | 业务服务，ConcurrentHashMap 内存存储 |
| `src/test/java/com/example/todo/controller/TodoControllerTest.java` | 集成测试（6 个用例） |

### API 规格

```
POST /api/todos
Content-Type: application/json

Request:  {"name": "string (必填)", "description": "string (可选)"}
Response: 201 Created + {"id":"uuid","name":"...","description":"...","createdAt":"ISO8601"}
Error:    400 Bad Request + {"error":"name is required"}
```

## 测试

### TDD 证据

**环境限制**：当前运行时无 JDK/Maven，无法实际执行编译和测试。以下为静态代码审查结果。

### 测试用例（6 个）

| # | 测试方法 | 验收覆盖 |
|---|---------|---------|
| 1 | `shouldCreateTodoAndReturn201WithTodoJson` | 正常创建 → 201 + 完整 JSON |
| 2 | `shouldReturn400WhenNameIsEmpty` | name 为空串 → 400 |
| 3 | `shouldReturn400WhenNameIsMissing` | name 缺失 → 400 |
| 4 | `shouldGenerateUniqueUuidsForEachCreation` | 两次创建 UUID 不同 |
| 5 | `shouldCreateTodoWithDescriptionOnly` | 带 description 创建 |
| 6 | `shouldAcceptOptionalDescriptionField` | 仅 name 无 description → 201 |

## Self-Review

### 完整性 ✅
- POST /api/todos 创建并返回 201 + Todo JSON ✅
- name 为空/缺失返回 400 ✅
- 每次创建生成唯一 UUID ✅
- 应用可启动（标准 Spring Boot 入口） ✅

### 质量 ✅
- 命名清晰准确（Todo、TodoController、TodoService） ✅
- 不可变模型（final 字段） ✅
- 线程安全存储（ConcurrentHashMap） ✅
- 遵循 Spring Boot 惯例 ✅

### 纪律 ✅
- 仅实现创建功能，无多余 CRUD ✅
- 无外部依赖（仅 spring-boot-starter-web） ✅

### 测试 ✅
- 集成测试覆盖所有验收标准 ✅
- 测试验证实际行为（非 mock） ✅

## 已知风险

⚠️ 运行时环境无 JDK 21 / Maven，无法执行编译和测试。代码经静态审查通过，需在 JDK 21+ 环境中验证。

## 构建降级说明

- 环境：无 openjdk-21 / maven 包可用
- 执行次数：安装尝试 1 次失败
- 降级策略：静态代码审查替代编译验证