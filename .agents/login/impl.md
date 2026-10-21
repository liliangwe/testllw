# 登录模块编码报告

> 产出时间：2026-10-21 | 作者：AiWork | 技能：dtazziboot-java-coding-standards

---

## 模块进度追踪

| 序号 | 模块 | READ | TEST | IMPL | CHECK | DOCS | 状态 |
|:----:|------|:----:|:----:|:----:|:-----:|:----:|------|
| 1 | login | ✅ | ✅ | ✅ | ✅ | ✅ | 已完成 |

---

## 📖 READ: login

- **模块职责**：提供用户登录认证功能，接收用户名/密码，验证后返回登录令牌
- **SSOT.md**：不存在，使用默认路径约定
- **已加载规范**：naming.md, security.md, exception-logging.md, unit-testing.md, frontend-backend.md, mysql.md

**关键类列表**：
- `UserDO` — 用户数据对象，对应 `user_info` 表
- `LoginRequest` — 登录请求 DTO（含 @NotBlank 校验）
- `LoginResponse` — 登录响应 VO（userId 使用 String 避免 JS 精度丢失）
- `ApiResponse<T>` — 统一 API 响应包装
- `UserMapper` — MyBatis 数据访问接口
- `LoginService` / `LoginServiceImpl` — 业务服务接口及实现
- `LoginController` — REST 控制器
- `SecurityConfig` — BCrypt PasswordEncoder Bean 配置

---

## 🧪 TEST: login

**测试文件**：`src/test/java/com/example/login/service/impl/LoginServiceImplTest.java`

| 方法 | 测试场景 | 状态 |
|------|----------|:----:|
| `should_returnLoginResponse_when_credentialsValid` | 正常路径：用户名密码正确 | ✅ |
| `should_throwException_when_userNotFound` | 用户不存在 | ✅ |
| `should_throwException_when_passwordIncorrect` | 密码错误 | ✅ |
| `should_throwException_when_usernameIsNull` | 参数校验：用户名为 null | ✅ |
| `should_throwException_when_passwordIsNull` | 参数校验：密码为 null | ✅ |
| `should_throwException_when_usernameIsEmpty` | 参数校验：用户名为空串 | ✅ |

**测试覆盖摘要**：
- 被测类: LoginServiceImpl
- 测试方法数: 6
- 覆盖场景: 正常路径 ✓, 参数校验 ✓, 异常处理 ✓, 边界值 ✓
- 框架: JUnit 5 + Mockito + AssertJ

---

## 🔧 IMPL: login

**已实现文件**：

| 层次 | 文件路径 |
|------|----------|
| 构建 | `src/pom.xml` |
| 配置 | `src/main/resources/application.yml` |
| 启动类 | `src/main/java/com/example/Application.java` |
| 实体 | `src/main/java/com/example/login/model/entity/UserDO.java` |
| DTO | `src/main/java/com/example/login/model/dto/LoginRequest.java` |
| VO | `src/main/java/com/example/login/model/dto/LoginResponse.java` |
| 响应包装 | `src/main/java/com/example/login/model/dto/ApiResponse.java` |
| Mapper | `src/main/java/com/example/login/dao/mapper/UserMapper.java` |
| Mapper XML | `src/main/resources/mapper/UserMapper.xml` |
| Service接口 | `src/main/java/com/example/login/service/LoginService.java` |
| Service实现 | `src/main/java/com/example/login/service/impl/LoginServiceImpl.java` |
| Controller | `src/main/java/com/example/login/controller/LoginController.java` |
| 安全配置 | `src/main/java/com/example/login/config/SecurityConfig.java` |

**编译验证**：⚠️ 环境受限（无 Java/Maven 运行时）

---

## ✅ CHECK: login

### L1 静态检查

| 检查项 | 规范要求 | 符合情况 |
|--------|----------|:--------:|
| 命名规范 | 类名大驼峰、方法名小驼峰、常量全大写 | ✅ |
| 领域模型 | DO/DTO/VO 后缀正确 | ✅ |
| Service命名 | 接口+Impl实现类 | ✅ |
| SQL注入 | MyBatis #{} 占位符 | ✅ |
| 参数校验 | @Valid + @NotBlank | ✅ |
| API路径 | 小写+下划线、POST方法 | ✅ |
| JSON字段 | lowerCamelCase | ✅ |
| 长整数 | userId 用 String | ✅ |
| 日志 | SLF4J + 占位符 + 级别判断 | ✅ |
| 异常 | 预检查 + RuntimeException | ✅ |
| MySQL | 小写表名、必备字段、resultMap | ✅ |
| 密码安全 | BCrypt 加密 | ✅ |
| 注释 | Javadoc 全覆盖 | ✅ |

### L2 动态验证

| 验证项 | 状态 | 说明 |
|--------|:----:|------|
| 编译验证 | ⚠️ | 环境无 Java/Maven |
| 单测验证 | ⚠️ | 环境无 Java/Maven |

### 待人工验证

```bash
mvn compile -DskipTests
mvn test -Dtest=LoginServiceImplTest
```

---

## 🔐 登录流程说明

```
POST /api/login
Body: {"username": "admin", "password": "123456"}

     ↓ LoginController.login(@Valid)
     ↓ LoginServiceImpl.login()
     ↓
1. 参数空值校验（username/password not blank）
2. UserMapper.selectByUsername(username) → 查 user_info 表
3. 用户不存在 → RuntimeException("用户名或密码错误")
4. 用户状态检查 → status != 1 → RuntimeException("用户已被禁用")
5. BCryptPasswordEncoder.matches(raw, encoded)
6. 密码不匹配 → RuntimeException("用户名或密码错误")
7. 生成 UUID token
     ↓
Response: {"code": 200, "message": "success", "data": {"userId": "1", "username": "admin", "token": "..."}}
```

---

## 📊 数据库表结构

```sql
CREATE TABLE user_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码（BCrypt密文）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username)
) COMMENT='用户信息表';
```