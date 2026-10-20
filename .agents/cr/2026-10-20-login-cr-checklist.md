# Code Review Checklist

> **Change** login-impl · **分支/Commit** `AI/task-DEV-966dcd0a...` / `9ca8248` · **日期** `2026-10-20`
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。
> **scan-all-rules.sh 预扫结果**：2 findings → G16.2 (P0) / A3.4 (P2)，详见 Step 4 备注。

---

## Step 1 — 执行队列（产物 A）

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|--------|
| 1 | `src/main/java/com/example/Application.java` | 启动类 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ |
| 2 | `src/main/java/com/example/common/constant/LoginConstants.java` | 常量 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ |
| 3 | `src/main/java/com/example/common/exception/BusinessException.java` | 异常类 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ |
| 4 | `src/main/java/com/example/common/handler/GlobalExceptionHandler.java` | 全局异常处理 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ |
| 5 | `src/main/java/com/example/common/model/ApiResponse.java` | 响应封装 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ |
| 6 | `src/main/java/com/example/login/controller/LoginController.java` | 控制器 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | ✅ |
| 7 | `src/main/java/com/example/login/dao/mapper/UserMapper.java` | Mapper接口 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ |
| 8 | `src/main/java/com/example/login/model/dto/LoginRequest.java` | DTO | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | ✅ |
| 9 | `src/main/java/com/example/login/model/dto/LoginResponse.java` | DTO | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | N/A | ⚠️ |
| 10 | `src/main/java/com/example/login/model/entity/UserDO.java` | 实体 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ |
| 11 | `src/main/java/com/example/login/service/LoginService.java` | 服务接口 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ |
| 12 | `src/main/java/com/example/login/service/impl/LoginServiceImpl.java` | 核心实现 | ✅ | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | N/A | ⚠️ |
| 13 | `src/test/java/com/example/login/service/impl/LoginServiceImplTest.java` | 单元测试 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ |

---

## Step 2 — 功能（产物 B）

> 需求来源：`.agents/login-impl/impl.md` + 需求「写一个登录功能」

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 | 用户以正确用户名+密码登录，返回token | impl.md L19「用户登录认证（用户名/密码 → token）」 | LoginServiceImpl.java LoginController.java | ✅ | LoginServiceImpl.java:39-75; LoginController.java:35-39 |
| REQ-2 | 用户不存在时返回错误码+提示 | impl.md L27「用户不存在」 | LoginServiceImpl.java | ✅ | LoginServiceImpl.java:45-49; test: LoginServiceImplTest.java:78-88 |
| REQ-3 | 密码错误时返回错误码+提示 | impl.md L28「密码错误」 | LoginServiceImpl.java | ✅ | LoginServiceImpl.java:61-66; test: LoginServiceImplTest.java:92-108 |
| REQ-4 | 账号禁用时返回错误码+提示 | impl.md L29「账号禁用」 | LoginServiceImpl.java | ✅ | LoginServiceImpl.java:53-58; test: LoginServiceImplTest.java:112-128 |
| REQ-5 | POST /login 接口可用 | impl.md L84-86 | LoginController.java | ✅ | LoginController.java:35-39 (@PostMapping) |
| REQ-6 | 统一API响应格式 {code, message, data} | impl.md L99-118 | ApiResponse.java GlobalExceptionHandler.java | ✅ | ApiResponse.java:19-33; GlobalExceptionHandler.java:29-59 |

---

## Step 3 — 可读性检查（产物 C）

> 对照 `references/readability-checklist.md` A1–A7

| ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | 编码UTF-8，文件名与类名一致 |
| A2 | 源文件结构/import 顺序 | ✅ | package→import→类结构正确，无 `import *` |
| A3 | 代码样式 | ⚠️ | **A3.4**: LoginServiceImpl.java:86 行宽超过120字符（预扫命中） |
| A4 | 命名规范 | ✅ | 包名/类名/方法名/常量均符合规范 |
| A5 | 编码实践 | ✅ | `@Override` 使用正确，无空 catch，无 finalize 重写 |
| A6 | 特定元素样式 | ✅ | 无 switch、无数组声明问题 |
| A7 | Javadoc 规范 | ✅ | public 类/方法均有 Javadoc |

---

## Step 4 — 可靠性检查（产物 D）

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> 预扫 (`scan-all-rules.sh`) 未覆盖的 B/M/I 规则由 LLM 逐条核销。

| ID | 状态 | 备注 |
|----|------|------|
| B001 | N/A | 无字面量 parse/of 调用 |
| B002 | N/A | 无数组比较 |
| B003 | N/A | 无 Arrays.fill |
| B004 | N/A | 无数组 toString |
| B005 | N/A | 无 Arrays.asList 基本类型 |
| B006 | N/A | LoginServiceImplTest 使用 assertThat，参数顺序正确 |
| B007 | N/A | 无捕获 Throwable |
| B008 | N/A | 无线程池创建 |
| B009 | N/A | 无移位运算 |
| B010 | N/A | 无 BigDecimal 构造 |
| B011 | N/A | 使用 String 比较，无包装类型 == |
| B012 | N/A | 无 Calendar |
| B013 | N/A | 无 Calendar |
| B014 | N/A | 无集合查询类型不兼容 |
| B015–B019 | N/A | 不涉及 |
| B020 | N/A | 无常量乘法溢出 |
| B021 | N/A | 无 Jedis |
| B022 | N/A | 无 SimpleDateFormat |
| B023 | N/A | 无非抛出的异常实例 |
| B024 | N/A | 无 Thread 创建 |
| B025 | N/A | 无双括号初始化 |
| B026 | N/A | 无 x.equals(null) |
| B027–B029 | N/A | 不涉及 |
| B030 | N/A | 无浮点 == 比较 |
| B031 | N/A | 无 String.format |
| B032–B035 | N/A | 不涉及 |
| B036 | N/A | 无 IdentityHashMap |
| B037–B040 | N/A | 不涉及 |
| B041 | N/A | 无 JDBC |
| B042–B044 | N/A | JUnit5，非 JUnit3/4 混合 |
| B045 | N/A | 无包装类加锁 |
| B046 | N/A | 无死循环条件 |
| B047–B052 | N/A | 不涉及 |
| B053 | N/A | 测试已使用 assertThatThrownBy |
| B054–B055 | N/A | 不涉及 |
| B056 | N/A | 无 Arrays.asList 修改 |
| B057 | N/A | 无增强 for 修改集合 |
| B058 | N/A | 无集合自操作 |
| B059 | N/A | 无 nCopies |
| B060 | N/A | 无 null 三元拆箱 |
| B061 | N/A | 使用 java.util.Base64 (LoginServiceImpl:113) |
| B062–B063 | N/A | 无过时 JDK API |
| B064 | N/A | 无 Optional == |
| B065 | N/A | 无 Pojo 自赋值 |
| B066 | N/A | 无 Math.random 强转 |
| B067 | N/A | 无 Random.nextInt % |
| B068 | N/A | LoginRequest 构造器使用 this.x=x，无误 |
| B069–B075 | N/A | 不涉及 |
| B076 | N/A | 无 @Transactional |
| B077 | N/A | 无捕获 Throwable |
| B078 | N/A | 不涉及 |
| B079 | N/A | @Mock 未显式赋值 ✓ |
| B080 | N/A | 测试含断言 (assertThat) |
| B081 | N/A | 无集合原地修改 |
| M001 | N/A | 无连续相同条件判断 |
| M002 | N/A | 无多余 instanceof |
| M003 | N/A | 无包装类构造器 |
| M004 | N/A | 无 printStackTrace |
| M005 | N/A | @Nested 必须是内部类 |
| M006 | N/A | 无编译期常量布尔 |
| M007 | N/A | 无空 catch |
| M008 | N/A | UserDO 未重写 equals/hashCode |
| M009 | N/A | 不涉及 |
| M010 | N/A | 无位运算 |
| M011 | N/A | 无 switch |
| M012 | N/A | 无 finally return |
| M013 | N/A | 无浮点强转 |
| M014 | N/A | 无枚举 getClass |
| M015 | N/A | 无字段隐藏 |
| M016 | N/A | 无依赖默认时区的 now() |
| M017 | N/A | 测试方法均有 @Test |
| M018 | N/A | 无显式锁 |
| M019 | N/A | 无 switch 枚举 |
| M020 | ✅ | LoginServiceImpl:38 有 @Override |
| M021 | N/A | UserDO 未重写 equals |
| M022 | N/A | 无 Optional |
| M023 | N/A | UserDO/LoginRequest 有自定义 toString |
| M024 | N/A | 无 Optional |
| M025 | N/A | 无 final 类 protected |
| M026 | N/A | @Mock 未声明 static |
| M027 | N/A | 无 ThreadLocal |
| I001–I010 | N/A | JUnit5 项目，非 I* 场景 |

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1 | N/A | 无事务内写后读无锁场景 |
| G1.2 | N/A | 无锁内未二次校验 |
| G1.3 | N/A | 无乐观锁 |
| G1.4 | N/A | 无多资源加锁 |
| G2.1 | N/A | 登录为读操作+token生成，非写接口 |
| G2.2 | N/A | 无重试/定时任务/MQ |
| G2.3 | N/A | 无幂等键约定 |
| G3.1 | N/A | 无分布式事务 |
| G3.2 | N/A | 无 @Transactional |
| G4.1 | N/A | SQL 简单，无复杂子查询 |
| G4.2 | N/A | WHERE username=#{username}，索引友好 |
| G4.3 | N/A | selectByUsername 单条查询 |
| G5.1 | N/A | 无 MQ |
| G6.1 | N/A | 无缓存 |
| G6.2 | N/A | 无缓存 |
| G7.1 | N/A | 无调度任务 |
| G7.2 | N/A | 无调度任务 |
| G8.1 | ✅ | GlobalExceptionHandler 兜底 catch Exception 并记日志 |
| G8.2 | N/A | 无外部依赖强依赖 |
| G8.3 | N/A | 无 I/O 流/连接/锁 |
| G8.4 | N/A | 无线程池 |
| G8.5 | N/A | 无 ThreadLocal |
| G8.6 | N/A | 无 Executors 线程池 |
| G9.1 | N/A | 无外部 RPC/HTTP 调用（仅本地 DB 查询） |
| G9.2 | N/A | 同上 |
| G9.3 | N/A | 无重试逻辑 |
| G10.1 | ✅ | status 的 null 在 LoginServiceImpl:53 做了防御 |
| G10.2 | N/A | 无契约变更 |
| G11.1 | ✅ | 有单测且含断言（4个场景） |
| G11.2 | ⚠️ | 缺少：用户名/密码为纯空格、超长输入等边界场景 |
| G11.3 | ✅ | LoginServiceImpl:45 user==null 有防御; LoginController 有 @Valid |
| G11.4 | N/A | 无数值运算 |
| G12.1 | N/A | 非资金场景 |
| G12.2 | N/A | 非资金场景 |
| G13.1 | ✅ | 成功用 INFO，业务异常用 WARN，系统异常用 ERROR |
| G14.1 | N/A | 无金额 |
| G14.2 | N/A | 无多租户 |
| G14.3 | N/A | LocalDateTime 存库，非跨区展示 |
| G14.4 | N/A | 无 SimpleDateFormat/DateTimeFormatter |
| G15.1 | N/A | schema.sql 为初始建表，非变更 |
| G15.2 | N/A | 无旧接口 |
| G15.3 | N/A | 无不兼容逻辑 |
| G16.1 | N/A | 无埋点（小模块可接受） |
| G16.2 | ❌ | **预扫命中**：LoginServiceImpl:101-103 — `catch(NoSuchAlgorithmException)` 仅包装 RuntimeException，未记录原始异常日志 |
| G16.3 | ✅ | 日志级别正确（info/warn/error） |
| G16.4 | N/A | 无空 catch 或 printStackTrace |
| G17.1 | N/A | 无功能开关 |
| G17.2 | N/A | 无核心链路降级 |
| G17.3 | N/A | schema.sql 为初始建表 |

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1 | ✅ | UserMapper.xml 使用 `#{username}` 预编译 |
| S1.2 | N/A | 无动态排序/表名 |
| S1.3 | N/A | 无 like/in 查询 |
| S2.1 | N/A | 无 HTML/JS 输出 |
| S2.2 | N/A | 无富文本 |
| S2.3 | N/A | 无模板引擎 |
| S3.1 | N/A | 无外部 URL 请求 |
| S3.2 | N/A | 同上 |
| S3.3 | N/A | 同上 |
| S4.1 | N/A | 无系统命令 |
| S4.2 | N/A | 同上 |
| S5.1 | N/A | 无 XML 解析 |
| S5.2 | N/A | 同上 |
| S6.1 | N/A | 无反序列化 |
| S6.2 | N/A | 同上 |
| S6.3 | N/A | 同上 |
| S7.1 | N/A | 无文件上传/下载 |
| S7.2 | N/A | 同上 |
| S8.1 | ⚠️ | /login 为公开入口，无可厚非；但无暴力破解防护（无验证码/限流） |
| S8.2 | ✅ | 使用 POST |
| S8.3 | N/A | 不涉及数据ID |
| S8.4 | N/A | 无 Cookie |
| S9.1 | ❌ | **LoginServiceImpl:86** — BCrypt 验证逻辑硬编码 `rawPassword.equals("123456")`，实质等效密码旁路 |
| S9.2 | ⚠️ | **LoginResponse:40** — toString() 暴露 token；**LoginServiceImpl:98-99** — `getBytes()` 使用平台默认字符集 |
| S9.3 | N/A | 无传输加密相关 |
| S9.4 | ✅ | LoginServiceImpl:110 使用 SecureRandom |
| S10.1 | N/A | 无 CSRF |
| S10.2 | N/A | 无 CORS |
| S10.3 | N/A | 无 URL 跳转 |

---

## Step 5 — 自定义扩展检查（产物 E）

### 5.1 自定义扩展（`customized-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| U1.1 | ✅ | LoginController:36 使用 @Valid |
| U1.2 | N/A | 无额外自定义规则 |
| U1.3 | N/A | 无额外自定义规则 |
| U2.1 | N/A | 无业务红线规则 |
| U2.2 | N/A | 无业务红线规则 |
| U2.3 | N/A | 无业务红线规则 |

> **Step 5 结论**：仅 U1.1 命中已通过；其余自定义规则未启用，整体标记 `N/A(未启用自定义规则)`。

---

## 终检（防漏检）

- [x] 执行队列中每个文件 `Step2`、`Step3`、**S1–S10 / G1–G17** 各列均非 `⬜`
- [x] Step 2 的每个 REQ/Scenario 均非 `⬜`
- [x] Step 3 的 A1–A7 均非 `⬜`
- [x] Step 4 全部 **G/S** 与 **B001–B081 / M001–M027 / I001–I010** ID 均非 `⬜`
- [x] Step 5 全部 U* ID 均非 `⬜`
- [x] 所有 `❌/⚠️` 已写入 report，且包含 `ID + path:line`