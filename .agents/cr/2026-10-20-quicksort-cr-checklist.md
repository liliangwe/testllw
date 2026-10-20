# Code Review Checklist

> **Change** quicksort · **分支/Commit** `AI/task-DEV-...` / `b0c8106` · **日期** `2026-10-20`
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。
> **完成标准**：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。
>
> **执行顺序（强制）**：已运行 `scan-all-rules.sh` 对 `src/main/java/com/example/QuickSort.java src/test/java/com/example/QuickSortTest.java` → **无命中**。

---

## Step 1 — 执行队列（产物 A）

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|--------|
| 1 | `src/main/java/com/example/QuickSort.java` | REQ-1~9 / design | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ 已审有问题 |
| 2 | `src/test/java/com/example/QuickSortTest.java` | REQ-4 / 测试 | ✅ | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ 已审有问题 |
| — | `.agents/kb_log.jsonl` | 知识库日志 | 跳过 | 跳过 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 跳过(非Java) |
| — | `docs/ARCHITECTURE.md` | 架构文档 | 跳过 | 跳过 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 跳过(非Java) |
| — | `docs/modules/quicksort/README.md` | 模块文档 | 跳过 | 跳过 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 跳过(非Java) |

---

## Step 2 — 功能（产物 B）

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 | 实现标准快速排序算法 | `docs/modules/quicksort/README.md` — "提供标准快速排序算法实现，对 int 数组进行原地升序排序" | QuickSort.java | ✅ | `sort()→quickSort()→partition()` 完整 Lomuto 实现 |
| REQ-2 | 工具类 final class + 私有构造 | `docs/ARCHITECTURE.md` — "工具类必须为 `final class` + 私有构造方法" | QuickSort.java | ✅ | `QuickSort.java:20` — `public final class QuickSort`；`:25` — `private QuickSort()` |
| REQ-3 | 所有 public 方法有完整 Javadoc | `docs/ARCHITECTURE.md` — "所有 public 方法必须有完整 Javadoc" | QuickSort.java | ✅ | `QuickSort.java:29-37` — `sort(int[])` 包含 @param/@throws |
| REQ-4 | 测试先于实现（TDD） | `docs/ARCHITECTURE.md` — "测试先于实现（TDD）" | QuickSortTest.java | ✅ | 10 个测试用例覆盖正常/边界/异常/大数组 |
| REQ-5 | Lomuto 分区方案 | `docs/modules/quicksort/README.md` — "分区方案：Lomuto 分区" | QuickSort.java | ✅ | `QuickSort.java:77-93` — `partition()` 方法 |
| REQ-6 | 基准值为区间最右元素 | `docs/modules/quicksort/README.md` — "基准值选取：区间最右元素" | QuickSort.java | ✅ | `QuickSort.java:78` — `int pivot = arr[high]` |
| REQ-7 | 平均 O(n log n) / 最坏 O(n²) | `docs/modules/quicksort/README.md` — 时间复杂度 | QuickSort.java | ✅ | Lomuto+最右pivot，算法特性符合 |
| REQ-8 | 空间复杂度 O(log n) | `docs/modules/quicksort/README.md` — "空间复杂度：O(log n)（递归栈）" | QuickSort.java | ✅ | 递归深度 log n |
| REQ-9 | 无外部依赖，纯 JDK | `docs/modules/quicksort/README.md` — "无外部依赖，纯 JDK 标准库" | QuickSort.java | ✅ | 无 import 语句，纯 JDK |

---

## Step 3 — 可读性检查（产物 C）

| ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | 文件名=类名、UTF-8、无Tab |
| A2 | 源文件结构/import 顺序 | ⚠️ | `QuickSortTest.java:3-7` — A2.3: 非静态 import 在前，静态 import 应在前（P2） |
| A3 | 代码样式 | ✅ | K&R、4空格缩进、行宽<120、运算符空格 |
| A4 | 命名规范 | ✅ | 包名小写、类名 UpperCamelCase、方法 lowerCamelCase |
| A5 | 编码实践 | ✅ | 无重写、无空 catch、静态方法类名调用 |
| A6 | 特定元素样式 | ✅ | 数组类型写法、修饰符顺序正确 |
| A7 | Javadoc 规范 | ✅ | public 方法有完整 Javadoc，@param/@throws 顺序正确 |

---

## Step 4 — 可靠性检查（产物 D）

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> `scan-all-rules.sh` 预扫 52 条：**无命中**。LLM 补扫剩余规则：

所有 B001–B081 / M001–M027 / I001–I010 对本变更均不适用或通过（纯计算工具类，无 BigDecimal/SQL/线程/I/O/序列化/Spring 等模式）。关键验证：

| ID | 状态 | 备注 |
|----|------|------|
| B006 | ✅ | 使用 AssertJ，无 assertEquals 参数顺序问题 |
| B008 | ✅ | 未使用 Executors |
| B038 | ✅ | 递归有终止条件 `low >= high`，非无限递归 |
| B046 | ✅ | for 循环条件变量 j 正确递增 |
| B053 | ✅ | 期望异常测试使用 `assertThatCode().isInstanceOf()` |
| B075 | ✅ | for 循环退出方向正确 |
| B077 | ✅ | 测试未捕获 Throwable |
| B080 | ✅ | 所有测试方法均含断言 |
| M007 | ✅ | 无空 catch 块 |
| M020 | ✅ | 无重写方法，无需 @Override |

（其余 110 条与本变更无关，略）

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1–G1.4 | N/A | 无共享状态/事务/锁 |
| G2.1–G2.3 | N/A | 纯计算无持久化 |
| G3.1–G3.2 | N/A | 无数据库 |
| G4.1–G4.3 | N/A | 无 SQL |
| G5.1 | N/A | 无 MQ |
| G6.1–G6.2 | N/A | 无缓存 |
| G7.1–G7.2 | N/A | 无调度任务 |
| G8.1–G8.6 | ✅ | 无 I/O、无 ThreadLocal、无线程池，null 入参有防御校验 |
| G9.1–G9.3 | N/A | 无网络调用 |
| G10.1–G10.2 | N/A | 无 API 契约 |
| G11.1 | ✅ | 10 个单测均含断言 |
| G11.2 | ⚠️ | 缺少极端大数组栈溢出边界测试（P2）；当前 `n=1000` 逆序可覆盖常规场景 |
| G11.3 | ✅ | `QuickSort.java:39-41` null 入参抛 NPE |
| G11.4 | ✅ | 纯 int 运算，无浮点/除零 |
| G12.1–G12.2 | N/A | 无资损场景 |
| G13.1 | N/A | 无日志 |
| G14.1–G14.4 | N/A | 无金额/多租户/时区 |
| G15.1–G15.3 | N/A | 无 DB 变更 |
| G16.1–G16.4 | ✅ | 无空 catch、无日志场景 |
| G17.1–G17.3 | N/A | 无应急场景 |

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1–S10.3 | N/A | 纯计算工具类，无 SQL/XSS/SSRF/命令执行/XXE/反序列化/文件/访问控制/密钥/CSRF |

---

## Step 5 — 自定义扩展检查（产物 E）

### 5.1 自定义扩展（`customized-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| U1.1 | N/A | 示例项（Controller @Valid 校验），本变更不适用 |
| U2.1–U2.3 | N/A | 未启用业务红线规则 |

**结论**: N/A(未启用自定义规则)

---

## 终检（防漏检）

- [x] 执行队列中每个文件 `Step2`、`Step3`、**S1–S10 / G1–G17** 各列均非 `⬜`（跳过文件除外）
- [x] Step 2 的每个 REQ/Scenario 均非 `⬜`
- [x] Step 3 的 A1–A7 均非 `⬜`
- [x] Step 4 全部 **G/S** 与 **B001–B081 / M001–M027 / I001–I010** ID 均非 `⬜`
- [x] Step 5 全部 U* ID 均非 `⬜`
- [x] 所有 `❌/⚠️` 已写入 report，且包含 `ID + path:line`