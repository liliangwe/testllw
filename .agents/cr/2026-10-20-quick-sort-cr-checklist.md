# Code Review Checklist

> **Change** quick-sort · **分支/Commit** `AI/task-DEV-...` / `4f0fdd3` · **日期** `2026-10-20`
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。
> **完成标准**：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。
>
> **执行顺序（强制）**：已执行 `scan-all-rules.sh`，输出已并入 Step 3 和 Step 4。

---

## Step 1 — 执行队列（产物 A）

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|----|----|----|----|----|----|----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|--------|
| 1 | `src/main/java/com/example/QuickSort.java` | REQ-1~3 / 核心实现 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 2 | `src/test/java/com/example/QuickSortTest.java` | REQ-4 / 测试 | ✅ | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ 已审有问题 |

---

## Step 2 — 功能（产物 B）

> 需求来源：`<requirement_section>` "写一个快速排序算法"

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 | 对泛型数组进行原地升序快速排序 | "写一个快速排序算法" | `QuickSort.java` | ✅ | `quickSort(T[])` + `partition()` 实现 Lomuto 分区；`QuickSort.java:37-44, 55-61, 76-93` |
| REQ-2 | 元素必须实现 Comparable 接口，支持泛型 | 算法天然要求元素可比较 | `QuickSort.java` | ✅ | `QuickSort.java:37` — `<T extends Comparable<? super T>>` |
| REQ-3 | 处理 null 数组、空数组、单元素边界情况 | 防御性编程要求 | `QuickSort.java` | ✅ | `QuickSort.java:38-43` — null→IllegalArgumentException; length≤1→early return |
| REQ-4 | 单元测试覆盖正常/边界/异常/特殊场景 | 测试必须覆盖需求 | `QuickSortTest.java` | ✅ | 10 个测试方法覆盖：乱序、已排序、逆序、重复元素、单元素、空数组、null、字符串、两元素、全相同、大数组 |

---

## Step 3 — 可读性检查（产物 C）

| ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | A1.1–A1.3 均符合 |
| A2 | 源文件结构/import 顺序 | ⚠️ | A2.2: `QuickSortTest.java:6` — `import static org.junit.jupiter.api.Assertions.*`（通配符导入，脚本已命中，P2）；A2.4: `QuickSortTest.java:3-4` — `DisplayName` 应在 `Test` 之前（字典序），P2 |
| A3 | 代码样式 | ✅ | K&R 大括号、4空格缩进、行宽 ≤120、运算符空格均符合 |
| A4 | 命名规范 | ✅ | 包名/类名/方法名/泛型/测试类命名均符合规范 |
| A5 | 编码实践 | ✅ | 无违规项（无重写、无 catch 块、无 finalize） |
| A6 | 特定元素样式 | ✅ | 数组方括号在类型上、修饰符顺序正确 |
| A7 | Javadoc 规范 | ⚠️ | A7.2: `QuickSort.java:70-74` — `partition` 方法 Javadoc 中 `@param <T>` 应在 `@param arr` 之前（块标记顺序），P2 |

---

## Step 4 — 可靠性检查（产物 D）

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> 脚本预扫：0 条 B/M/I 命中；以下由 LLM 逐条核销。

| ID | 状态 | 备注 |
|----|------|------|
| B001 | N/A | 无字面量 parse/of 调用 |
| B002 | N/A | 无 array.equals() |
| B003 | N/A | 无 Arrays.fill |
| B004 | N/A | 无 array.toString() |
| B005 | N/A | 无 Arrays.asList 基本类型 |
| B006 | ✅ | assertEquals/assertArrayEquals 参数顺序正确：expected 在前 |
| B007 | N/A | 无 catch(Throwable) |
| B008 | N/A | 无 Executors |
| B009 | N/A | 无移位操作 |
| B010 | N/A | 无 BigDecimal |
| B011 | ✅ | 无包装类型 == 比较；compareTo 返回 int 比较正确 |
| B012 | N/A | 无 Calendar |
| B013 | N/A | 无 Calendar.HOUR |
| B014 | N/A | 无集合泛型查询 |
| B015 | N/A | 无 Collection.toArray(T[]) |
| B016 | ✅ | `Comparable<? super T>` 泛型参数正确 |
| B017 | N/A | 无 this==null |
| B018 | N/A | 无三目运算符数值分支 |
| B019 | N/A | 无 Money 类 |
| B020 | N/A | 无编译期常量乘法 |
| B021 | N/A | 无 Jedis |
| B022 | N/A | 无 SimpleDateFormat |
| B023 | N/A | 无 DeadException |
| B024 | N/A | 无 DeadThread |
| B025 | N/A | 无双括号初始化 |
| B026 | N/A | 无 equals(null) |
| B027 | N/A | 无 equals 方法 |
| B028 | N/A | 无 DateUtil |
| B029 | N/A | 无 Pojo setter |
| B030 | ✅ | 无浮点 == 比较 |
| B031 | N/A | 无 String.format |
| B032 | N/A | 无注解 getClass() |
| B033 | N/A | 无 Unsafe |
| B034 | N/A | 无 Hashtable |
| B035 | N/A | 无恒等二元表达式 |
| B036 | N/A | 无 IdentityHashMap |
| B037 | N/A | 无可变参数条件表达式 |
| B038 | ✅ | 递归有终止条件 `low < high:56` |
| B039 | N/A | 无 String.indexOf(int,int) |
| B040 | N/A | 无 Class.isInstance |
| B041 | N/A | 无 JDBC |
| B042 | N/A | 非 JUnit3 |
| B043 | N/A | 无内部类 @Test |
| B044 | N/A | 非 JUnit3/JUnit4 混用 |
| B045 | N/A | 无包装类型加锁 |
| B046 | ✅ | for 循环 `j++` 与 `j < high` 一致 |
| B047 | N/A | 无数值 compare 精度损失 |
| B048 | N/A | 无 Math.round |
| B049 | N/A | 无日期格式 |
| B050 | N/A | 无 12/24 小时制格式 |
| B051 | N/A | 无 Boolean.getBoolean |
| B052 | N/A | 无 ISO 周年格式 |
| B053 | ✅ | 异常测试使用 assertThrows 正确 |
| B054 | N/A | 无 EqualsTester |
| B055 | N/A | 无 Mockito |
| B056 | N/A | 无 Arrays.asList 写操作 |
| B057 | N/A | 无集合增强 for 修改 |
| B058 | N/A | 无集合自操作 |
| B059 | N/A | 无 Collections.nCopies |
| B060 | N/A | 无 null 三目自动拆箱 |
| B061 | N/A | 无 sun.misc.BASE64Encoder |
| B062 | N/A | 无 URLClassLoader 强转 |
| B063 | N/A | 无 javax.xml.bind |
| B064 | N/A | 无 Optional == 比较 |
| B065 | N/A | 无 Pojo 自赋值 |
| B066 | N/A | 无 Math.random() 强转 |
| B067 | N/A | 无 Random.nextInt() % n |
| B068 | N/A | 无变量自赋值 |
| B069 | N/A | 无 compareTo 自比较 |
| B070 | N/A | 无 equals 自比较 |
| B071 | N/A | 无 size() >= 0 |
| B072 | N/A | 无 Stream.toString() |
| B073 | N/A | 无 StringBuilder(char) |
| B074 | N/A | 无 substring(0) |
| B075 | ✅ | for 循环退出条件正确 |
| B076 | N/A | 无 @Transactional |
| B077 | N/A | 无 catch(Throwable) 吞 AssertionError |
| B078 | N/A | 无 Truth assertThat(x).isEqualTo(x) |
| B079 | N/A | 无 @Mock |
| B080 | ✅ | 所有测试方法均有 assert* 断言 |
| B081 | N/A | 无新建集合 sort 后未使用 |
| M001 | N/A | 无重复条件判断 |
| M002 | N/A | 无 instanceof 子类/父类 |
| M003 | N/A | 无包装类 new 构造器 |
| M004 | N/A | 无 printStackTrace() |
| M005 | N/A | QuickSortTest 非内部类 |
| M006 | N/A | 无编译期布尔常量表达式 |
| M007 | N/A | 无空 catch 块 |
| M008 | N/A | 无 equals/hashCode 重写 |
| M009 | N/A | 无不可能类型 equals |
| M010 | N/A | 无位运算常量 |
| M011 | N/A | 无 switch |
| M012 | N/A | 无 finally return/throw |
| M013 | N/A | 无浮点强转运算 |
| M014 | N/A | 无枚举 getClass() |
| M015 | N/A | 无父子类字段同名 |
| M016 | N/A | 无 java.time 默认时区调用 |
| M017 | N/A | 所有测试方法均有 @Test |
| M018 | N/A | 无显式锁 |
| M019 | N/A | 无 switch 枚举 |
| M020 | N/A | 无重写方法（工具类无继承） |
| M021 | N/A | 无 equals 方法 |
| M022 | N/A | 无 Optional.of(null) |
| M023 | N/A | 无 Object.toString() 依赖 |
| M024 | N/A | 无 Optional.get() 空检查后调用 |
| M025 | N/A | 无 final 类 protected 成员 |
| M026 | N/A | 无 @Mock |
| M027 | N/A | 无 ThreadLocal |
| I001 | N/A | 异常测试已 assertThrows + assertEquals message |
| I002 | N/A | 无 @DoNotMock |
| I003 | N/A | 无 @AutoValue |
| I004 | N/A | 无 java.util.Date |
| I005 | N/A | 非 JUnit3 |
| I006 | N/A | 非 JUnit4 setUp |
| I007 | N/A | 非 JUnit4 tearDown |
| I008 | N/A | 无 @DataProvider |
| I009 | N/A | 统计用 |
| I010 | N/A | 无 Spring 容器 |

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1 | N/A | 无事务/并发场景 |
| G1.2 | N/A | 无加锁后更新 |
| G1.3 | N/A | 无乐观锁 |
| G1.4 | N/A | 无多资源加锁 |
| G2.1 | N/A | 无写接口/消息消费 |
| G2.2 | N/A | 无重试/定时任务 |
| G2.3 | N/A | 无幂等键 |
| G3.1 | N/A | 无分布式事务 |
| G3.2 | N/A | 无 @Transactional |
| G4.1 | N/A | 无 SQL |
| G4.2 | N/A | 无 SQL 索引 |
| G4.3 | N/A | 无查询 |
| G5.1 | N/A | 无 MQ |
| G6.1 | N/A | 无缓存 |
| G6.2 | N/A | 无缓存双写 |
| G7.1 | N/A | 无调度任务 |
| G7.2 | N/A | 无调度任务 |
| G8.1 | ✅ | 异常路径已处理（null→IAE，空/单元素→early return），无吞异常 |
| G8.2 | N/A | 无核心链路依赖 |
| G8.3 | N/A | 无 I/O 资源 |
| G8.4 | N/A | 无线程池 |
| G8.5 | N/A | 无 ThreadLocal |
| G8.6 | N/A | 无线程池 |
| G9.1 | N/A | 无外部调用 |
| G9.2 | N/A | 无外部调用 |
| G9.3 | N/A | 无外部重试 |
| G10.1 | N/A | 无接口 null 语义歧义 |
| G10.2 | N/A | 无契约变更 |
| G11.1 | ✅ | 所有测试均有断言 |
| G11.2 | ✅ | 覆盖空、null、单元素、大数组(100)、全相同等边界 |
| G11.3 | ✅ | null 参数抛 IllegalArgumentException |
| G11.4 | N/A | 无数值运算/金额 |
| G12.1 | N/A | 无资金场景 |
| G12.2 | N/A | 无资金场景 |
| G13.1 | N/A | 无日志（工具类） |
| G14.1 | N/A | 无金额 |
| G14.2 | N/A | 无多租户 |
| G14.3 | N/A | 无时区 |
| G14.4 | N/A | 无日期格式化 |
| G15.1 | N/A | 无 DB 变更 |
| G15.2 | N/A | 无接口共存 |
| G15.3 | N/A | 无开关逻辑 |
| G16.1 | N/A | 无核心链路（工具类） |
| G16.2 | N/A | 无异常日志场景 |
| G16.3 | N/A | 无日志 |
| G16.4 | N/A | 无 catch 块 |
| G17.1 | N/A | 无功能开关 |
| G17.2 | N/A | 无降级场景 |
| G17.3 | N/A | 无数据变更 |

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1 | N/A | 无 SQL |
| S1.2 | N/A | 无动态 SQL |
| S1.3 | N/A | 无 like/in 查询 |
| S2.1 | N/A | 无 Web 输出 |
| S2.2 | N/A | 无富文本 |
| S2.3 | N/A | 无模板引擎 |
| S3.1 | N/A | 无外部 URL 请求 |
| S3.2 | N/A | 无 302 跳转 |
| S3.3 | N/A | 无 HTTP 调用 |
| S4.1 | N/A | 无系统命令 |
| S4.2 | N/A | 无文件操作 |
| S5.1 | N/A | 无 XML 解析 |
| S5.2 | N/A | 无 XPath |
| S6.1 | N/A | 无反序列化 |
| S6.2 | N/A | 无 JSON 多态反序列化 |
| S6.3 | N/A | 无序列化敏感字段 |
| S7.1 | N/A | 无文件上传 |
| S7.2 | N/A | 无文件路径 |
| S7.3 | N/A | 无文件存储 |
| S8.1 | N/A | 无 Web 接口 |
| S8.2 | N/A | 无 HTTP 接口 |
| S8.3 | N/A | 无数据 ID |
| S8.4 | N/A | 无 Cookie |
| S9.1 | N/A | 无密钥/凭证 |
| S9.2 | N/A | 无日志 |
| S9.3 | N/A | 无网络传输 |
| S9.4 | N/A | 无随机数 |
| S10.1 | N/A | 无 CSRF 场景 |
| S10.2 | N/A | 无 CORS |
| S10.3 | N/A | 无 URL 跳转 |

---

## Step 5 — 自定义扩展检查（产物 E）

### 5.1 自定义扩展（`customized-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| U1.1 | N/A | 无 Controller（纯工具类） |
| U2.1 | N/A | 无业务逻辑 |
| U2.2 | N/A | 无业务逻辑 |
| U2.3 | N/A | 无业务逻辑 |

---

## 终检（防漏检）

- [x] 执行队列中每个文件 `Step2`、`Step3`、**S1–S10 / G1–G17** 各列均非 `⬜`（跳过文件除外）；
- [x] Step 2 的每个 REQ/Scenario 均非 `⬜`
- [x] Step 3 的 A1–A7 均非 `⬜`
- [x] Step 4 全部 **G/S** 与 **B001–B081 / M001–M027 / I001–I010** ID 均非 `⬜`（允许 `N/A`，但有原因）
- [x] Step 5 全部 U* ID 均非 `⬜`（允许 `N/A(未启用自定义规则)`）
- [x] 所有 `❌/⚠️` 已写入 report，且包含 `ID + path:line`