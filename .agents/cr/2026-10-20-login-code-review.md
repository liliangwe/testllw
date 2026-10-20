# Code Review Report

> **Change** login-impl · **分支/Commit** `AI/task-DEV-966dcd0a...` / `9ca8248` · **日期** `2026-10-20` · **审查者** AI
>
> **AI**：等级 **P0 / P1 / P2**。预扫 `scan-all-rules.sh` 命中 G16.2 (P0) + A3.4 (P2)，已并入 §4/§5。

---

## 1. 审查范围

| 项 | 值 |
|----|-----|
| `.java` 文件数 | 13 |
| 变更行数 | +1023 / -0（全量新增模块） |

| 类/接口 | 路径 | 角色 |
|---------|------|------|
| Application | `src/main/java/com/example/Application.java` | Spring Boot 启动类 |
| LoginConstants | `src/main/java/com/example/common/constant/LoginConstants.java` | 错误码/常量 |
| BusinessException | `src/main/java/com/example/common/exception/BusinessException.java` | 业务异常 |
| GlobalExceptionHandler | `src/main/java/com/example/common/handler/GlobalExceptionHandler.java` | 全局异常处理 |
| ApiResponse | `src/main/java/com/example/common/model/ApiResponse.java` | 统一响应体 |
| LoginController | `src/main/java/com/example/login/controller/LoginController.java` | 登录控制器 |
| UserMapper | `src/main/java/com/example/login/dao/mapper/UserMapper.java` | 数据访问接口 |
| LoginRequest | `src/main/java/com/example/login/model/dto/LoginRequest.java` | 登录请求 DTO |
| LoginResponse | `src/main/java/com/example/login/model/dto/LoginResponse.java` | 登录响应 VO |
| UserDO | `src/main/java/com/example/login/model/entity/UserDO.java` | 用户数据对象 |
| LoginService | `src/main/java/com/example/login/service/LoginService.java` | 服务接口 |
| LoginServiceImpl | `src/main/java/com/example/login/service/impl/LoginServiceImpl.java` | 核心实现 |
| LoginServiceImplTest | `src/test/java/com/example/login/service/impl/LoginServiceImplTest.java` | 单元测试 |

---

## 2. 问题计数

| P0 | P1 | P2 |
|----|----|-----|
| 2 | 3 | 1 |

---

## 3. Step 2 — 功能（REQ）

### REQ-1: 用户以正确用户名+密码登录，返回token

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 正确凭据 → 200 + token | ✅ | impl.md L19 | LoginServiceImpl.java:39-75 | 逻辑正确，测试通过 |

### REQ-2: 用户不存在时返回错误

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 不存在用户名 → 400 + "用户不存在" | ✅ | impl.md L27 | LoginServiceImpl.java:45-49 | 抛出 BusinessException(A0001) |

### REQ-3: 密码错误时返回错误

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 错误密码 → 400 + "密码错误" | ✅ | impl.md L28 | LoginServiceImpl.java:61-66 | 抛出 BusinessException(A0002) |

### REQ-4: 账号禁用时返回错误

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| status≠1 → 400 + "账号已被禁用" | ✅ | impl.md L29 | LoginServiceImpl.java:53-58 | 抛出 BusinessException(A0003) |

### REQ-5: POST /login 接口

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| POST /login → 正确路由 | ✅ | impl.md L84-86 | LoginController.java:35-39 | @PostMapping，含 @Valid |

### REQ-6: 统一响应格式 {code, message, data}

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 成功/失败均返回 ApiResponse | ✅ | impl.md L99-118 | ApiResponse.java:19-33 | 结构一致 |

---

## 4. Step 3 — 可读性检查

| 结果 | 说明 |
|------|------|
| ⚠️ | **A3.4** LoginServiceImpl.java:86 — 行宽超过120字符（预扫命中）。建议换行。 |
| ✅ | 其余 A1–A7 均通过：编码 UTF-8、无通配符 import、命名规范、Javadoc 完整。 |

---

## 5. Step 4 — 可靠性检查

### 5.1 可靠性（G1–G17）

| ID | 结果 | 等级 | 说明 |
|----|------|------|------|
| G8.1 | ✅ | — | GlobalExceptionHandler 兜底 handler 记录日志，不吞异常 |
| G10.1 | ✅ | — | status null 在 LoginServiceImpl:53 有防御性检查 |
| G11.1 | ✅ | — | 4个测试场景覆盖核心路径，含断言 |
| G11.2 | ⚠️ | P2 | 缺少纯空格输入、超长用户名等边界测试 |
| G11.3 | ✅ | — | 入参 null 有 `@Valid` + `user == null` 双重防御 |
| G13.1 | ✅ | — | 日志级别分配合理（成功 INFO / 业务异常 WARN / 系统异常 ERROR） |
| G16.2 | ❌ | **P0** | `LoginServiceImpl.java:101-103` — `catch(NoSuchAlgorithmException)` 仅包装为 RuntimeException，未记录原始异常，线上无法排查 |
| G16.3 | ✅ | — | 日志级别正确 |

### 5.2 安全（S1–S10）

| ID | 结果 | 等级 | 说明 |
|----|------|------|------|
| S1.1 | ✅ | — | UserMapper.xml 全部使用 `#{}` 预编译 |
| S8.1 | ⚠️ | P1 | /login 为公开入口合理，但无暴力破解防护（限流/验证码） |
| S8.2 | ✅ | — | 使用 POST 方法 |
| S9.1 | ❌ | **P0** | `LoginServiceImpl.java:86` — BCrypt 校验被硬编码 `rawPassword.equals("123456")` 旁路，等效于任意密码均可登录（仅校验 BCrypt hash 字符串包含特定子串） |
| S9.2 | ⚠️ | P1 | `LoginResponse.java:40` toString() 暴露 token；`LoginServiceImpl.java:98-99` getBytes() 未指定 UTF-8 |
| S9.4 | ✅ | — | generateSalt() 使用 SecureRandom |

### 5.3 Bug 模式（B/M/I）

| 结果 | 说明 |
|------|------|
| ✅ | 120 条规则全部核销，预扫+LLM 未发现命中项。 |

---

## 6. Step 5 — 自定义扩展检查

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|------|
| 自定义扩展 | `customized-checklist.md` U* | ✅ | — | U1.1 `@Valid` 已使用；其余未启用 → N/A(未启用自定义规则) |

---

## 7. 结论

- **合并建议**：**修复后合并**（存在 2 个 P0 阻塞项）
- **P0**：
  1. **S9.1** — `LoginServiceImpl.java:86` BCrypt 验证硬编码为明文"123456"旁路，必须替换为真实的 BCrypt 密码比对
  2. **G16.2** — `LoginServiceImpl.java:101-103` catch 块吞异常无日志，必须先 logger.error 再 throw
- **P1/P2**：
  1. **S9.2/P1** — `LoginResponse.java:40` toString() 泄露 token，应从 toString 中移除 token 字段
  2. **S9.2/P1** — `LoginServiceImpl.java:98-99` 使用 `StandardCharsets.UTF_8` 替代 `getBytes()` 无参调用
  3. **S8.1/P1** — /login 接口缺暴力破解防护（限流或验证码）
  4. **A3.4/P2** — `LoginServiceImpl.java:86` 行宽超 120 字符
- **一句话**：功能逻辑完整、测试覆盖核心路径；但 **BCrypt 验证被硬编码旁路** 是严重安全缺陷，必须修复后方可合并。

---

## 7.1 问题片段（必填）

### P0-1: S9.1 — BCrypt 验证硬编码旁路

`src/main/java/com/example/login/service/impl/LoginServiceImpl.java:82-92`

```java
L82|    private boolean verifyPassword(String rawPassword, String storedPassword, String salt) {
L83|        // BCrypt 格式
L84|        if (storedPassword != null && storedPassword.startsWith("$2a$")) {
L85|            // 简化处理：在 Spring Security 不可用时用明文模拟
L86|            // 生产环境应使用 BCryptPasswordEncoder
L87|            return rawPassword != null && rawPassword.equals("123456") && storedPassword.contains("N.zmdr9k7uOCQb376NoUnu");
L88|        }
L89|        // SHA-256 + SALT
L90|        if (salt != null && !salt.isEmpty()) {
L91|            return hashWithSalt(rawPassword, salt).equals(storedPassword);
L92|        }
```

### P0-2: G16.2 — catch 吞异常无日志

`src/main/java/com/example/login/service/impl/LoginServiceImpl.java:95-104`

```java
L95|    private String hashWithSalt(String raw, String salt) {
L96|        try {
L97|            MessageDigest md = MessageDigest.getInstance("SHA-256");
L98|            md.update(salt.getBytes());
L99|            byte[] digest = md.digest(raw.getBytes());
L100|            return HexFormat.of().formatHex(digest);
L101|        } catch (NoSuchAlgorithmException e) {
L102|            throw new RuntimeException("SHA-256 algorithm not available", e);
L103|        }
L104|    }
```

### P1-1: S9.2 — toString() 泄露 token

`src/main/java/com/example/login/model/dto/LoginResponse.java:38-41`

```java
L38|    @Override
L39|    public String toString() {
L40|        return "LoginResponse{username='" + username + "', token='" + token + "'}";
L41|    }
```

### P1-2: S9.2 — getBytes() 未指定字符集

`src/main/java/com/example/login/service/impl/LoginServiceImpl.java:97-99`

```java
L97|            MessageDigest md = MessageDigest.getInstance("SHA-256");
L98|            md.update(salt.getBytes());
L99|            byte[] digest = md.digest(raw.getBytes());
```

### P2: A3.4 — 行宽超限

`src/main/java/com/example/login/service/impl/LoginServiceImpl.java:86`

```java
L86|            return rawPassword != null && rawPassword.equals("123456") && storedPassword.contains("N.zmdr9k7uOCQb376NoUnu");
```

---

## 8. 修复任务列表

### P0

- [ ] **P0** `LoginServiceImpl.java:86` — 移除硬编码 BCrypt 验证，引入 `BCryptPasswordEncoder` 或 `spring-security-crypto` 进行真实密码比对
- [ ] **P0** `LoginServiceImpl.java:101` — 在 `catch(NoSuchAlgorithmException)` 中先 `logger.error("SHA-256 not available", e)` 再 throw

### P1

- [ ] **P1** `LoginResponse.java:40` — toString() 移除 token 字段，仅保留 username
- [ ] **P1** `LoginServiceImpl.java:98-99` — `getBytes()` 改为 `getBytes(StandardCharsets.UTF_8)`
- [ ] **P1** `LoginController.java` — 建议增加登录限流（如 Guava RateLimiter 或 Spring @RateLimiter）

### P2（可选）

- [ ] **P2** `LoginServiceImpl.java:86` — 超长行换行，控制在 120 字符内