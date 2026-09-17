# Code Review Checklist

> **Change** `hello-world` · **分支/Commit** `AI/task-AUTO-root-d48f387a-13c7-43e7-af30-04430a38352b` / `d7afa4f3` · **日期** `2026-09-17`
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。
> **完成标准**：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。
>
> **执行顺序（强制）**：先运行 `scan-all-rules.sh` → 结果：「No findings. 52/222 rules scanned」→ 再用 LLM 完成 Step 2–5 中脚本未覆盖项及复核。

---

## Step 1 — 执行队列（产物 A）

> **Step4 列语义**：每个 **Sn / Gn** 表示「**本文件**在 Step4 审查中，对 `reliability-checklist.md` 第 **G\*n\*** 节、`security-checklist.md` 第 **S\*n\*** 节的扫描结论」。**Bug 模式（B/M/I）** 不在本表分列，在下方 **§4.1** 按清单 ID 核销（可与 `scan-all-rules.sh` 预扫结果对照）。与变更无关填 `N/A`；已扫无命中填 `✅`；命中风险填 `⚠️` 或 `❌`（并在 Step 4 明细表与 report 中写清 `Gx.x` / `Sx.x` + `path:line`）。

**列说明（与 references 章节对齐）**

| 列组 | 列名 | 对应清单章节 |
|------|------|----------------|
| 可靠性 | **G1** … **G17**（+ **G18** 仅明细表） | `reliability-checklist.md` — G1 并发 … G17 可应急；**G18** 安全补强在 Step 4.2 逐条核销，Step 1 可不单列 |
| 安全 | **S1** … **S10** | `security-checklist.md` — S1 SQL 注入 … S10 CSRF/CORS/跳转 |

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|----|----|----|----|----|----|----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|--------|
| 1 | `HelloWorld.java` | F01 / design | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ |

- 由 `git diff --name-only` 展开：仅 `HelloWorld.java`。
- **守卫**：通过（含 `.java` 文件）。
- **收口**：所有列均已非 `⬜`。

---

## Step 2 — 功能（产物 B）

> 仅从 spec/tasks 提 **REQ**，勿臆造。不符 spec 标 **P0**。
> 每个 REQ 都必须填写 **spec 证据** 与 **关联文件**；若命中 P0，代码证据需落到 `path:line`、测试或接口行为。

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-F01 | 当运行 `java HelloWorld` 时 → 应输出 "Hello, World!" 到控制台 → 程序正常退出 | design.md §5.1.3.1 「HelloWorld 程序入口（F01）」 | `HelloWorld.java` | ✅ | `HelloWorld.java:9` — `System.out.println("Hello, World!");` 满足功能需求 |

---

## Step 3 — 可读性检查（产物 C）

对照 `references/readability-checklist.md` A1–A7 逐节核销：

| ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | `HelloWorld.java` 文件名=类名；UTF-8；仅空格/换行。文件末尾缺少换行符（`\ No newline at end of file`），属 P2 风格建议 |
| A2 | 源文件结构/import 顺序 | ✅ | 无 package/import 声明，一个顶层类，结构正确 |
| A3 | 代码样式 | ✅ | K&R 大括号、4空格缩进、行宽 <120、运算符空格均合规 |
| A4 | 命名规范 | ✅ | `HelloWorld` UpperCamelCase ✓, `main` lowerCamelCase ✓, `args` lowerCamelCase ✓ |
| A5 | 编码实践 | ✅ | 无重写、无catch块、无finalize，合规 |
| A6 | 特定元素样式 | ✅ | `String[] args` 合规写法；无switch/数组/C风格 |
| A7 | Javadoc 规范 | ✅ | public 类有 Javadoc 注释（`@author`）✓ |

---

## Step 4 — 可靠性检查（产物 D）

> **逐条核销（强制）**：G/S 每个 ID **独占一行**，禁止合并为区间。**Bug 模式** 按 `bug-pattern-checklist.md` 中 **每条 B*/M*/I*** 独占一行核销（120 条）；无关变更可对该 ID 标 `N/A` 并写原因。报告等级：**Blocker→P0、Major→P1、Info→P2**。

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> 预扫结果：`scan-all-rules.sh` — **No findings**（52/222 rules scanned）。以下 LLM 补全脚本未覆盖的 170 条规则核销。

#### Blocker（B001–B081）

| ID | 状态 | 备注（命中写 `path:line`；预扫可粘贴脚本摘要） |
|----|------|--------------------------------------------------|
| B001 | N/A | 无 parse/of 调用 |
| B002 | N/A | 无数组比较 |
| B003 | N/A | 无 Arrays.fill |
| B004 | N/A | 无数组 toString |
| B005 | N/A | 无 Arrays.asList(primitive) |
| B006 | N/A | 无 JUnit assertEquals |
| B007 | N/A | 无 catch Throwable |
| B008 | N/A | 无 Executors 调用 |
| B009 | N/A | 无移位操作 |
| B010 | N/A | 无 BigDecimal(double) |
| B011 | N/A | 无包装类型 == 比较 |
| B012 | N/A | 无 Calendar 操作 |
| B013 | N/A | 无 Calendar set |
| B014 | N/A | 无集合类型不兼容查询 |
| B015 | N/A | 无 toArray 调用 |
| B016 | N/A | 无 Comparable 实现 |
| B017 | N/A | 无 this == null |
| B018 | N/A | 无三目数值提升 |
| B019 | N/A | 无 Money 类操作 |
| B020 | N/A | 无常量溢出 |
| B021 | N/A | 无 Jedis 使用 |
| B022 | N/A | 无 SimpleDateFormat |
| B023 | N/A | 无 DeadException |
| B024 | N/A | 无 DeadThread |
| B025 | N/A | 无双括号初始化 |
| B026 | N/A | 无 equals(null) |
| B027 | N/A | 无 equals 比较错误字段 |
| B028 | N/A | 无 DateUtil 使用 |
| B029 | N/A | 无 setter 赋值错误字段 |
| B030 | N/A | 无浮点 == 比较 |
| B031 | N/A | 无 format 占位符问题 |
| B032 | N/A | 无注解 getClass |
| B033 | N/A | 无 Unsafe 操作 |
| B034 | N/A | 无 Hashtable 使用 |
| B035 | N/A | 无恒等二元表达式 |
| B036 | N/A | 无 IdentityHashMap |
| B037 | N/A | 无可变参数条件表达式 |
| B038 | N/A | 无递归调用 |
| B039 | N/A | 无 indexOf 参数颠倒 |
| B040 | N/A | 无 isInstance 调用 |
| B041 | N/A | 无 JDBC 连接操作 |
| B042 | N/A | 无 JUnit3 测试方法 |
| B043 | N/A | 无 JUnit4 内部类测试 |
| B044 | N/A | 无 JUnit 混用 |
| B045 | N/A | 无锁包装类型 |
| B046 | N/A | 无循环条件未更新 |
| B047 | N/A | 无 compare 精度损失 |
| B048 | N/A | 无 Math.round(int) |
| B049 | N/A | 无日期格式 DD 误用 |
| B050 | N/A | 无 hh/HH 误用 |
| B051 | N/A | 无 Boolean.getBoolean |
| B052 | N/A | 无 YYYY 误用 |
| B053 | N/A | 无 MissingFail |
| B054 | N/A | 无 EqualsTester 使用 |
| B055 | N/A | 无 Mockito 误用 |
| B056 | N/A | 无 Arrays.asList 修改操作 |
| B057 | N/A | 无增强 for 循环修改集合 |
| B058 | N/A | 无集合自修改 |
| B059 | N/A | 无 nCopies 参数颠倒 |
| B060 | N/A | 无三目拆箱 NPE |
| B061 | N/A | 无 sun.misc.BASE64Encoder |
| B062 | N/A | 无 ClassLoader 转型 |
| B063 | N/A | 无 javax.xml 使用 |
| B064 | N/A | 无 Optional == 比较 |
| B065 | N/A | 无 Pojo 自赋值 |
| B066 | N/A | 无 Math.random 强转 int |
| B067 | N/A | 无 Random.nextInt 取余 |
| B068 | N/A | 无变量自赋值 |
| B069 | N/A | 无 SelfComparison |
| B070 | N/A | 无 SelfEquals |
| B071 | N/A | 无 size>=0 判断 |
| B072 | N/A | 无 Stream toString |
| B073 | N/A | 无 StringBuilder(char) |
| B074 | N/A | 无 substring(0) |
| B075 | N/A | 无可疑 for 循环 |
| B076 | N/A | 无 @Transactional |
| B077 | N/A | 无 try-catch Throwable |
| B078 | N/A | 无 TruthSelfEquals |
| B079 | N/A | 无 @Mock 显式赋值 |
| B080 | N/A | 无 JUnit 无断言 |
| B081 | N/A | 无 UnusedCollection |

#### Major（M001–M027）

| ID | 状态 | 备注 |
|----|------|------|
| M001 | N/A | 无重复条件判断 |
| M002 | N/A | 无 instanceof 使用 |
| M003 | N/A | 无包装类构造器 |
| M004 | N/A | 无 printStackTrace |
| M005 | N/A | 无内部类 |
| M006 | N/A | 无布尔常量表达式 |
| M007 | N/A | 无 catch 块 |
| M008 | N/A | 无 equals/hashCode 重写 |
| M009 | N/A | 无 equals 不兼容类型 |
| M010 | N/A | 无位运算 |
| M011 | N/A | 无 switch |
| M012 | N/A | 无 finally |
| M013 | N/A | 无 FloatCast |
| M014 | N/A | 无枚举 getClass |
| M015 | N/A | 无字段隐藏 |
| M016 | N/A | 无 LocalDateTime.now() 不带时区 |
| M017 | N/A | 无 JUnit4 测试方法 |
| M018 | N/A | 无 Lock 使用 |
| M019 | N/A | 无枚举 switch |
| M020 | N/A | 无 @Override |
| M021 | N/A | 无 equals 重载 |
| M022 | N/A | 无 Optional 操作 |
| M023 | N/A | 无 Object toString |
| M024 | N/A | 无 OptionalNotPresent |
| M025 | N/A | 无 final 类 protected 成员 |
| M026 | N/A | 无 @Mock static |
| M027 | N/A | 无 ThreadLocal |

#### Info（I001–I010）

| ID | 状态 | 备注 |
|----|------|------|
| I001 | N/A | 无异常断言 |
| I002 | N/A | 无 @DoNotMock |
| I003 | N/A | 无 @AutoValue |
| I004 | N/A | 无 java.util.Date 使用 |
| I005 | N/A | 无 JUnit 混用 |
| I006 | N/A | 无 setUp 方法 |
| I007 | N/A | 无 tearDown 方法 |
| I008 | N/A | 无 dataProvider |
| I009 | N/A | 无单测统计 |
| I010 | N/A | 无 TestRunner |

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1 | N/A | 单线程 main，无并发场景 |
| G1.2 | N/A | 同上 |
| G1.3 | N/A | 同上 |
| G1.4 | N/A | 同上 |
| G2.1 | N/A | 无写接口/消息消费 |
| G2.2 | N/A | 同上 |
| G2.3 | N/A | 同上 |
| G3.1 | N/A | 无事务操作 |
| G3.2 | N/A | 同上 |
| G4.1 | N/A | 无 SQL 操作 |
| G4.2 | N/A | 同上 |
| G4.3 | N/A | 同上 |
| G5.1 | N/A | 无 MQ 操作 |
| G6.1 | N/A | 无缓存操作 |
| G6.2 | N/A | 同上 |
| G7.1 | N/A | 无调度任务 |
| G7.2 | N/A | 同上 |
| G8.1 | N/A | 无异常处理 |
| G8.2 | N/A | 无外部依赖 |
| G8.3 | N/A | 无 I/O 流/连接 |
| G8.4 | N/A | 无线程池 |
| G8.5 | N/A | 无 ThreadLocal |
| G8.6 | N/A | 无线程池 |
| G9.1 | N/A | 无网络调用 |
| G9.2 | N/A | 同上 |
| G9.3 | N/A | 同上 |
| G10.1 | N/A | 无接口定义 |
| G10.2 | N/A | 同上 |
| G11.1 | N/A | 极简 hello world，无测试要求 |
| G11.2 | N/A | 同上 |
| G11.3 | N/A | 无入参校验场景（main args 未使用） |
| G11.4 | N/A | 无数值运算 |
| G12.1 | N/A | 无资金相关场景 |
| G12.2 | N/A | 同上 |
| G13.1 | N/A | 无日志输出 |
| G14.1 | N/A | 无金额操作 |
| G14.2 | N/A | 无多租户 |
| G14.3 | N/A | 无时区操作 |
| G14.4 | N/A | 无日期格式化 |
| G15.1 | N/A | 无数据库变更 |
| G15.2 | N/A | 无接口共存场景 |
| G15.3 | N/A | 无开关控制 |
| G16.1 | N/A | 无核心链路埋点需求 |
| G16.2 | N/A | 无异常路径 |
| G16.3 | N/A | 无日志输出 |
| G16.4 | N/A | 无空 catch |
| G17.1 | N/A | 无在线服务 |
| G17.2 | N/A | 同上 |
| G17.3 | N/A | 无数据变更 |
| G18.1 | N/A | 无安全补强场景 |
| G18.2 | N/A | 同上 |
| G18.3 | N/A | 同上 |

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1 | N/A | 无 SQL 操作 |
| S1.2 | N/A | 同上 |
| S1.3 | N/A | 同上 |
| S2.1 | N/A | 无 HTML/JS 输出 |
| S2.2 | N/A | 同上 |
| S2.3 | N/A | 同上 |
| S3.1 | N/A | 无 URL 请求 |
| S3.2 | N/A | 同上 |
| S3.3 | N/A | 同上 |
| S4.1 | N/A | 无命令执行 |
| S4.2 | N/A | 同上 |
| S5.1 | N/A | 无 XML 解析 |
| S5.2 | N/A | 同上 |
| S6.1 | N/A | 无反序列化 |
| S6.2 | N/A | 同上 |
| S6.3 | N/A | 同上 |
| S7.1 | N/A | 无文件上传 |
| S7.2 | N/A | 同上 |
| S7.3 | N/A | 同上 |
| S8.1 | N/A | 无鉴权需求 |
| S8.2 | N/A | 同上 |
| S8.3 | N/A | 同上 |
| S8.4 | N/A | 同上 |
| S9.1 | N/A | 无密钥/凭证 |
| S9.2 | N/A | 无敏感日志 |
| S9.3 | N/A | 无传输加密需求 |
| S9.4 | N/A | 无随机数使用 |
| S10.1 | N/A | 无 Web 表单操作 |
| S10.2 | N/A | 无 CORS |
| S10.3 | N/A | 无跳转 |

---

## Step 5 — 自定义扩展检查（产物 E）

> 按 `customized-checklist.md` 逐条核销；若未启用可整节写 `N/A(未启用自定义规则)`。

### 5.1 自定义扩展（`customized-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| U1.1 | N/A(未启用自定义规则) | 示例项，未启用 |
| U1.2 | N/A(未启用自定义规则) | 示例项，未启用 |
| U1.3 | N/A(未启用自定义规则) | 示例项，未启用 |
| U2.1 | N/A(未启用自定义规则) | 示例项，未启用 |
| U2.2 | N/A(未启用自定义规则) | 示例项，未启用 |
| U2.3 | N/A(未启用自定义规则) | 示例项，未启用 |

---

## 终检（防漏检）

- [x] 执行队列中每个文件 `Step2`、`Step3`、**S1–S10 / G1–G17** 各列均非 `⬜`（跳过文件除外）；
- [x] Step 2 的每个 REQ/Scenario 均非 `⬜`
- [x] Step 3 的 A1–A7 均非 `⬜`
- [x] Step 4 全部 **G/S** 与 **B001–B081 / M001–M027 / I001–I010** ID 均非 `⬜`（允许 `N/A`，但有原因）
- [x] Step 5 全部 U* ID 均非 `⬜`（允许 `N/A(未启用自定义规则)`）
- [x] 所有 `❌/⚠️` 已写入 report，且包含 `ID + path:line`