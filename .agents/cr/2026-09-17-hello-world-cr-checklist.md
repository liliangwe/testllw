# Code Review Checklist

> **Change** HelloWorld · **分支/Commit** AI/task-AUTO-root-58782a4b-4a90-4b19-848a-bf6b476e0616 / 88da6385 · **日期** 2026-09-17
>
> **AI**：唯一进度源；状态仅用 ⬜ ✅ ❌ ⚠️ N/A。
> **完成标准**：所有核销项必须从 ⬜ 变为其他状态；N/A 需写原因。

---

## Step 1 — 执行队列（产物 A）

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | 总状态 |
|---|----------------------|----------|-------|-------|--------|
| 1 | `HelloWorld.java` | F01/F02 — 程序入口 | ✅ | ✅ | ✅ |

---

## Step 2 — 功能（产物 B）

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 (F01) | 程序运行后向标准输出打印 "Hello, World!" | 系分设计 §5.1.4.1：`System.out.println("Hello, World!")` | `HelloWorld.java` | ✅ | `HelloWorld.java:17` — `System.out.println("Hello, World!");` |
| REQ-2 (F02) | 程序正常退出，返回码 0 | 系分设计 §5.1.4.1 业务规则 R02 | `HelloWorld.java` | ✅ | 无异常路径，JVM 正常退出返回 0 |

---

## Step 3 — 可读性检查（产物 C）

| ID | 检查项 | 状态 | 备注（命中写 path:line） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | A1.1 文件名=类名 ✅ `HelloWorld.java`; A1.2 UTF-8 ✅; A1.3 无Tab ✅ |
| A2 | 源文件结构/import 顺序 | ✅ | A2.1 文件顺序(无package/import) ✅; A2.2-A2.5 N/A(无import) |
| A3 | 代码样式 | ✅ | A3.1 K&R大括号 ✅; A3.3 缩进4空格 ✅; A3.4 行宽≤120 ✅; 其余N/A |
| A4 | 命名规范 | ✅ | A4.2 类名UpperCamelCase `HelloWorld` ✅; A4.3 方法名 lowerCamelCase `main` ✅; A6.1 `String[] args` ✅ |
| A5 | 编码实践 | ✅ | N/A(无Override/无catch/无finalize) |
| A6 | 特定元素样式 | ✅ | A6.1 `String[] args` ✅(数组方括号属类型); 其余N/A |
| A7 | Javadoc 规范 | ✅ | A7.1 public类+方法均有Javadoc ✅; A7.2 `@param`顺序正确 ✅; A7.4 多段落分隔 ✅ |

---

## Step 4 — 可靠性检查（产物 D）

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> 预扫运行结果：`scan-all-rules.sh ./HelloWorld.java` — **无发现**（52/222 rules scanned）

| ID | 状态 | 备注 |
|----|------|------|
| B001 | N/A | 无 parse/of 调用 |
| B002 | N/A | 无数组比较 |
| B003 | N/A | 无 Arrays.fill |
| B004 | N/A | 无数组 toString |
| B005 | N/A | 无 Arrays.asList |
| B006 | N/A | 无 JUnit assertEquals |
| B007 | N/A | 无 catch Throwable |
| B008 | N/A | 无 Executors |
| B009 | N/A | 无移位运算 |
| B010 | N/A | 无 BigDecimal |
| B011 | N/A | 无包装类型 == |
| B012 | N/A | 无 Calendar |
| B013 | N/A | 无 Calendar |
| B014 | N/A | 无集合操作 |
| B015 | N/A | 无 toArray |
| B016 | N/A | 无 Comparable |
| B017 | N/A | 无 this==null |
| B018 | N/A | 无三目数值分支 |
| B019 | N/A | 无 Money API |
| B020 | N/A | 无常量溢出 |
| B021 | N/A | 无 Jedis |
| B022 | N/A | 无 DateFormat |
| B023 | N/A | 无未抛异常 |
| B024 | N/A | 无 Thread |
| B025 | N/A | 无双括号初始化 |
| B026 | N/A | 无 equals(null) |
| B027 | N/A | 无 equals |
| B028 | N/A | 无 DateUtil |
| B029 | N/A | 无 setter |
| B030 | N/A | 无浮点 == |
| B031 | N/A | 无 format |
| B032 | N/A | 无注解 getClass |
| B033 | N/A | 无 Unsafe |
| B034 | N/A | 无 Hashtable |
| B035 | N/A | 无二元表达式 |
| B036 | N/A | 无 IdentityHashMap |
| B037 | N/A | 无可变参数 |
| B038 | N/A | 无递归 |
| B039 | N/A | 无 indexOf |
| B040 | N/A | 无 isInstance |
| B041 | N/A | 无 JDBC |
| B042 | N/A | 无 JUnit3 |
| B043 | N/A | 无内部类测试 |
| B044 | N/A | 无 JUnit3+4混用 |
| B045 | N/A | 无锁 |
| B046 | N/A | 无循环 |
| B047 | N/A | 无 compare |
| B048 | N/A | 无 Math.round |
| B049 | N/A | 无日期格式 |
| B050 | N/A | 无小时格式 |
| B051 | N/A | 无 Boolean.getBoolean |
| B052 | N/A | 无周年格式 |
| B053 | N/A | 无测试 try |
| B054 | N/A | 无 EqualsTester |
| B055 | N/A | 无 Mockito |
| B056 | N/A | 无 Arrays.asList 修改 |
| B057 | N/A | 无增强for修改 |
| B058 | N/A | 无集合自操作 |
| B059 | N/A | 无 nCopies |
| B060 | N/A | 无 null 三目 |
| B061 | N/A | 无 BASE64Encoder |
| B062 | N/A | 无 ClassLoader 强转 |
| B063 | N/A | 无 javax.xml |
| B064 | N/A | 无 Optional == |
| B065 | N/A | 无 setter 自赋值 |
| B066 | N/A | 无 Math.random 强转 |
| B067 | N/A | 无 Random |
| B068 | N/A | 无变量自赋值 |
| B069 | N/A | 无 compareTo 自比较 |
| B070 | N/A | 无 equals 自比较 |
| B071 | N/A | 无 size()>=0 |
| B072 | N/A | 无 Stream.toString |
| B073 | N/A | 无 StringBuilder |
| B074 | N/A | 无 substring(0) |
| B075 | N/A | 无 for 循环 |
| B076 | N/A | 无 @Transactional |
| B077 | N/A | 无单测 catch Throwable |
| B078 | N/A | 无 Truth 自等 |
| B079 | N/A | 无 @Mock 赋值 |
| B080 | N/A | 无单测断言 |
| B081 | N/A | 无 Collections.sort |
| M001 | N/A | 无重复条件判断 |
| M002 | N/A | 无 instanceof |
| M003 | N/A | 无包装类构造器 |
| M004 | N/A | 无 printStackTrace |
| M005 | N/A | 无内部类 |
| M006 | N/A | 无布尔常量表达式 |
| M007 | N/A | 无空 catch |
| M008 | N/A | 无 equals/hashCode |
| M009 | N/A | 无跨类型 equals |
| M010 | N/A | 无比特运算 |
| M011 | N/A | 无 switch |
| M012 | N/A | 无 finally |
| M013 | N/A | 无类型转换 |
| M014 | N/A | 无枚举 getClass |
| M015 | N/A | 无继承字段隐藏 |
| M016 | N/A | 无 LocalDateTime.now |
| M017 | N/A | 无测试方法 |
| M018 | N/A | 无 Lock |
| M019 | N/A | 无 switch 枚举 |
| M020 | N/A | 无 @Override |
| M021 | N/A | 无 equals(Object) |
| M022 | N/A | 无 Optional.of |
| M023 | N/A | 无 toString |
| M024 | N/A | 无 Optional.get |
| M025 | N/A | 无 final 类 protected |
| M026 | N/A | 无 @Mock static |
| M027 | N/A | 无 ThreadLocal |
| I001 | N/A | 无单测断言异常 |
| I002 | N/A | 无 @DoNotMock |
| I003 | N/A | 无 @AutoValue |
| I004 | N/A | 无 java.util.Date |
| I005 | N/A | 无 JUnit3/4 混用 |
| I006 | N/A | 无 setUp |
| I007 | N/A | 无 tearDown |
| I008 | N/A | 无 dataProvider |
| I009 | N/A | 无单测统计 |
| I010 | N/A | 无测试运行器 |

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1-G1.4 | N/A(无可并发场景) | 单线程顺序执行 |
| G2.1-G2.3 | N/A(无写操作) | 仅 println 只读操作 |
| G3.1-G3.2 | N/A(无事务) | 无数据库操作 |
| G4.1-G4.3 | N/A(无SQL) | 无数据库操作 |
| G5.1 | N/A(无 MQ) | 无消息队列 |
| G6.1-G6.2 | N/A(无缓存) | 无缓存操作 |
| G7.1-G7.2 | N/A(无调度) | 无定时任务 |
| G8.1-G8.6 | N/A(无I/O流/无异常处理) | 无需释放资源 |
| G9.1-G9.3 | N/A(无网络调用) | 无外部调用 |
| G10.1-G10.2 | N/A(无接口) | 无接口定义 |
| G11.1-G11.4 | N/A(无单测) | 本次变更不含单测 |
| G12.1-G12.2 | N/A(无资损场景) | HelloWorld 不涉及资金 |
| G13.1 | N/A(无日志) | 无日志输出 |
| G14.1-G14.4 | N/A(无国际化) | 无不涉及时区/币种 |
| G15.1-G15.3 | N/A(无DB变更) | 无数据库变更 |
| G16.1-G16.4 | N/A(无异常路径) | 无异常逻辑 |
| G17.1-G17.3 | N/A(无需应急) | 非生产部署程序 |

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1-S1.3 | N/A(无SQL) | 无数据库操作 |
| S2.1-S2.3 | N/A(无XSS风险) | 无用户输入/HTML输出 |
| S3.1-S3.3 | N/A(无SSRF风险) | 无外部URL请求 |
| S4.1-S4.2 | N/A(无命令执行) | 无系统命令调用 |
| S5.1-S5.2 | N/A(无XXE风险) | 无XML解析 |
| S6.1-S6.3 | N/A(无反序列化) | 无反序列化操作 |
| S7.1-S7.3 | N/A(无文件操作) | 无文件上传下载 |
| S8.1-S8.4 | N/A(无访问控制) | 非Web应用 |
| S9.1-S9.4 | N/A(无敏感数据) | 无双密/凭证/敏感信息 |
| S10.1-S10.3 | N/A(无CSRF风险) | 非Web应用 |

---

## Step 5 — 自定义扩展检查（产物 E）

### 5.1 自定义扩展（`customized-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| U1.1 | N/A(未启用自定义规则) | 示例项，本仓库未配置团队自定义规则 |
| U1.2 | N/A(未启用自定义规则) | 同上 |
| U1.3 | N/A(未启用自定义规则) | 同上 |
| U2.1 | N/A(未启用自定义规则) | 同上 |
| U2.2 | N/A(未启用自定义规则) | 同上 |
| U2.3 | N/A(未启用自定义规则) | 同上 |

---

## 终检（防漏检）

- [x] 执行队列中每个文件 Step2、Step3、各列均非 ⬜（跳过文件除外）；
- [x] Step 2 的每个 REQ/Scenario 均非 ⬜
- [x] Step 3 的 A1–A7 均非 ⬜
- [x] Step 4 全部 G/S 与 B001–B081 / M001–M027 / I001–I010 ID 均非 ⬜（允许 N/A，但有原因）
- [x] Step 5 全部 U* ID 均非 ⬜（允许 N/A(未启用自定义规则)）
- [x] 所有 ❌/⚠️ 已写入 report，且包含 ID + path:line