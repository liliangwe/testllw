# Code Review Report

> **Change** `三接口 helloworld/哈希/冒泡排序 + 埋点/报表/导出 + 前端三 Tab` · **分支/Commit** `AI/task-DEV-966dcd0a-7905-11f1-9649-3b4281182f10-ebbe48e3-72a8-459e-b48f-b25c23672dd0` / `bf2d904` · **日期** `2026-10-20` · **审查者** AI
>
> **AI**：等级 P0/P1/P2；G/S 以 checklist 行内定义为准；Bug 模式 Blocker→P0、Major→P1、Info→P2。已先运行 `scan-all-rules.sh`（52/222 规则可程序化预扫）并将要点并入 §5，再由 LLM 逐文件核对与复核误报。

---

## 1. 审查范围

| 项 | 值 |
|----|-----|
| `.java` 文件数 | 31（主代码 26 + 测试 5） |
| 变更行数 | `+2927 / -2`（编码阶段 commit 相对系分阶段） |
| 审查方式 | 静态审查（仓库无 JDK/Maven，`java -version` 不可用，未执行 mvn 构建，见 §7） |

| 类/接口 | 路径 | 角色 |
|---------|------|------|
| DemoApplication | src/main/java/com/example/demo/DemoApplication.java | 启动类 + @EnableAsync |
| ApiResponse / BizException / GlobalExceptionHandler | src/main/java/com/example/demo/common/ | 统一响应与异常 |
| AsyncConfig / WebConfig | src/main/java/com/example/demo/config/ | 异步线程池 / CORS |
| DemoController | src/main/java/com/example/demo/controller/DemoController.java | W01~W06 六接口 |
| HashRequest / BubbleSortRequest / DimensionsResponse | src/main/java/com/example/demo/dto/ | 请求/响应 DTO |
| AlgorithmService / BubbleSortResult | src/main/java/com/example/demo/service/ | 三算法实现 |
| CsvWriter / ExportQuery / ExportService | src/main/java/com/example/demo/export/ | CSV 导出 |
| ReportQuery / ReportResult / ReportService | src/main/java/com/example/demo/report/ | 报表聚合 |
| tracking/*（8 类） | src/main/java/com/example/demo/tracking/ | 埋点注解/切面/落库 |
| 5 个测试类 | src/test/java/... | 单测与接口测 |

---

## 2. 问题计数

| P0 | P1 | P2 |
|----|----|-----|
| 0 | 5 | 3 |

> `blocker_count = 0`（无 P0 阻塞项）。脚本预扫报出的 6 条 `G16.2 CatchWithoutLogging` 经 LLM 复核：AlgorithmService:25/TrackingAspect:47/TrackingService:38/TrackingServiceTest:43 属误报（catch 内均有 throw 或 log/中断处理），仅 CsvWriter:62、ReportService:107 两条保留为 P1（按 `reliability-checklist.md` G16.2 等级应为 P1，脚本统一标 P0 系等级错标）。

---

## 3. Step 2 — 功能（REQ）

### REQ-F01 HelloWorld 接口

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| name 缺省/空返回问候语 | ✅ | spec §1.2 F01「返回问候字符串」 | `DemoController:48-58`、`AlgorithmService:13-18` | `Hello, World!`/`Hello, {name}!` |
| name 超 64 校验失败 | ✅ | spec §5「参数校验失败 code=400 级」 | `DemoController:51-53` 返回 `ALG_001` | 备测 `DemoControllerTest:41-46` |

### REQ-F02 哈希算法接口

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| SHA-256 小写 hex 与标准向量一致 | ✅ | plan 全局约束「abc → ba7816bf…」 | `AlgorithmService:20-28` | `HexFormat.of().formatHex`，`AlgorithmServiceTest:25-29` 断言向量 |

### REQ-F03 冒泡排序接口

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 升序 + swaps | ✅ | plan「[5,3,8,1]→[1,3,5,8] swaps 4」 | `AlgorithmService:44-58`、`DemoController:74-96` | `AlgorithmServiceTest:39-43` |
| 空数组/非数字/超 1000 校验 | ✅ | spec §5 | `DemoController:76-90` 返回 `ALG_003/004/005` | `DemoControllerTest:84-116` |

### REQ-F04 前端三 Tab 页面

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 三 Tab 展示三接口结果 | ✅ | spec §1.2 F04 | `frontend/src/DemoPage.jsx`（HelloWorld/哈希/冒泡排序） | 单页 `/demo`（App.jsx 归一化） |

### REQ-F05 导出功能

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 导出按钮 + 后端接口覆盖各 Tab | ✅ | spec §1.2 F05 | `DemoController:116-127`、`ExportService:33-110` | tab ∈ helloworld/hash/bubbleSort/report，CSV attachment |

### REQ-F06 调用埋点

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 记录调用次数 + 调用人，异步落库 | ✅ | spec §1.2 F06 | `TrackingAspect:32-70`、`TrackingService:20-33` | 成功返回后从 Header 取 caller，`@Async("trackingExecutor")` |

### REQ-F07 调用报表可视化

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 按人员类型/层级/部门维度 | ✅ | spec §1.2 F07 | `ReportService:40-67` | 维度白名单 person_type/person_level/department |
| 折线/饼/柱三图 | ✅ | spec §1.2 F07 | `frontend/src/reportCharts.jsx` LineChart/PieChart/BarChart | 空数据显示 Empty |

> Step 2 结论：7 项 REQ 均满足，无功能性不符（无 P0）。

---

## 4. Step 3 — 可读性检查

| 结果 | 说明（违规写 Ax.x 与 `path:行`） |
|------|--------------------------------|
| ⚠️ | `A3.4` 行宽 >120：`ReportService.java:70`（normalizeDimension 单行） |
| ✅ | 其余 A1~A7 已扫无命中（无 Tab 缩进、无 import *、无包名大写、无重写 finalize 等，脚本 A* 项覆盖复核通过） |
| ✅ | Javadoc：公共类/主要方法含注释（`CsvWriter/CsvWriter`、`TrackingRecord`、`ApiCallContext` 等） |

---

## 5. Step 4 — 可靠性检查

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|-------------------------------------|
| 可靠性 | `reliability-checklist.md` G1–G17 | ⚠️ | P1 | `G16.2` ReportService:107 捕获 `DateTimeParseException` 后 `return null` 无日志（无效日期被静默忽略）；CsvWriter:62 捕获 IOException 无日志。`M016` 默认时区 `LocalDateTime.now()`（TrackingAspect:68、TrackingService:27）。G8.4 线程池已配置 `waitForTasksToCompleteOnShutdown`，✅ |
| 安全 | `security-checklist.md` S1–S10 | ⚠️ | P1 | `S10.2` WebConfig:13 CORS `allowedOriginPatterns("*")` + `allowedHeaders("*")`；GlobalExceptionHandler:32-34 兜底异常直接返回 `ex.getMessage()`（信息泄露）。S1 SQL 注入 ✅（JdbcTemplate `?` 预编译，dynamic column 仅白名单常量） |
| Bug 模式 | `bug-pattern-checklist.md` B/M/I（120） | ✅ | P0–P2 | 预扫 `scan-all-rules.sh`：B 类 0 命中；M016（见上）保留 P1；M 其余/ I 类 LLM 逐条无命中。 |

### 自动化预扫要点（scan-all-rules.sh）

- `G16.2 CatchWithoutLogging`：6 条 → 复核后保留 2 条（CsvWriter:62、ReportService:107），4 条误报。
- `M016 JavaTimeDefaultTimeZone`：5 条（TrackingAspect:68、TrackingService:27 为主代码，其余 3 条在测试类）。
- `S10.2 CorsWildcard`：WebConfig:13。
- `A3.4 LineWidthExceeded`：ReportService:70。

---

## 6. Step 5 — 自定义扩展检查

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|------------------------------------------|
| 自定义扩展 | `customized-checklist.md` U* | N/A | — | 项目未启用自定义规则（U1.1 为示例项） |

---

## 7. 结论

- **合并建议**：修复后合并（无 P0 阻塞，存在 5 项 P1 应修复）。
- **P0**：无。
- **P1**：
  1. `S10.2` WebConfig.java:13 — CORS 通配符放开。
  2. `M016` TrackingAspect.java:68 / TrackingService.java:27 — 依赖系统默认时区。
  3. `S9.2(信息泄露)` GlobalExceptionHandler.java:32-34 — 兜底异常消息直接回传前端。
  4. `G16.2` ReportService.java:107 — 无效日期被静默忽略且无日志。
  5. `G16.2` CsvWriter.java:62 — 序列化异常被吞且无日志。
- **P1/P2**：P2 见 §8。
- **一句话**：功能闭环（三接口/埋点/报表/导出/前端三图）实现完整、职责清晰、测试覆盖较全；主要风险集中在 CORS 通配符、默认时区与若干吞异常点，为示例项目的安全/可观测性隐患。

---

## 7.1 问题片段（必填）

**P1** `S10.2` `src/main/java/com/example/demo/config/WebConfig.java:13` — CORS 源/头全通配，任意来源可跨域访问。
片段范围：`WebConfig.java:11-16`

```java
L11|    public void addCorsMappings(CorsRegistry registry) {
L12|        registry.addMapping("/api/**")
L13|                .allowedOriginPatterns("*")
L14|                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
L15|                .allowedHeaders("*");
L16|    }
```

**P1** `M016` `src/main/java/com/example/demo/tracking/TrackingAspect.java:68` — `LocalDateTime.now()` 依赖系统默认时区，跨区部署时埋点时间不一致。
片段范围：`TrackingAspect.java:67-69`

```java
L67|        ApiCallContext ctx = new ApiCallContext(apiName, callerId, personType, personLevel, department,
L68|                LocalDateTime.now());
L69|        trackingService.record(ctx);
```

**P1** `M016` `src/main/java/com/example/demo/tracking/TrackingService.java:27` — 同上，落库时间依赖系统默认时区。
片段范围：`TrackingService.java:26-28`

```java
L26|        try {
L27|            LocalDateTime now = LocalDateTime.now();
L28|            TrackingRecord record = new TrackingRecord(
```

**P1** `S9.2(信息泄露)` `src/main/java/com/example/demo/common/GlobalExceptionHandler.java:32-34` — 兜底异常把 `ex.getMessage()` 原样返回，可能泄露内部实现细节。
片段范围：`GlobalExceptionHandler.java:31-35`

```java
L31|    @ExceptionHandler(Exception.class)
L32|    public ApiResponse<Void> handleGeneric(Exception ex) {
L33|        String detail = ex.getMessage() == null ? "unexpected error" : ex.getMessage();
L34|        return ApiResponse.failure("INTERNAL_ERROR", detail);
L35|    }
```

**P1** `G16.2` `src/main/java/com/example/demo/report/ReportService.java:107` — 捕获日期解析异常后 `return null` 且无日志，非法 start/end 被静默忽略而非报错。
片段范围：`ReportService.java:104-109`

```java
L104|        try {
L105|            LocalDate date = LocalDate.parse(value);
L106|            return endOfDay ? date.atTime(23, 59, 59) : date.atStartOfDay();
L107|        } catch (DateTimeParseException e) {
L108|            return null;
L109|        }
```

**P1** `G16.2` `src/main/java/com/example/demo/export/CsvWriter.java:62` — 捕获 IOException 后返回 `String.valueOf` 吞异常且无日志。
片段范围：`CsvWriter.java:55-64`

```java
L55|        try {
L56|            // 数组等复杂结构转 JSON 文本写入单元格（对 List<Integer> 直接序列化）
L57|            String json = OBJECT_MAPPER.writeValueAsString(value);
L58|            if (json.startsWith("\"") && json.endsWith("\"")) {
L59|                json = json.substring(1, json.length() - 1).replace("\\\"", "\"");
L60|            }
L61|            return json;
L62|        } catch (IOException e) {
L63|            return String.valueOf(value);
L64|        }
```

---

## 8. 修复任务列表

### P0

- 无待修复项。

### P1

- [ ] **P1** `src/main/java/com/example/demo/config/WebConfig.java:13` — 将 CORS 源限定为前端域名白名单（开发态可限定 `http://localhost:5173`），避免 `*`。
- [ ] **P1** `src/main/java/com/example/demo/tracking/TrackingAspect.java:68` — 埋点时间改用 `LocalDateTime.now(ZoneId.systemDefault())` 或统一 UTC（`Instant`/`OffsetDateTime`）。
- [ ] **P1** `src/main/java/com/example/demo/tracking/TrackingService.java:27` — 落库时间显式指定时区。
- [ ] **P1** `src/main/java/com/example/demo/common/GlobalExceptionHandler.java:33` — 兜底异常不再回传 `ex.getMessage()`，改为固定提示并记 error 日志（含 traceId/上下文）。
- [ ] **P1** `src/main/java/com/example/demo/report/ReportService.java:107-108` — 日期解析失败记录 warn 日志，或按 RPT_002 语义显式抛业务异常，避免静默忽略。
- [ ] **P1** `src/main/java/com/example/demo/export/CsvWriter.java:62-63` — 序列化失败记录 error 日志（可加脱敏/截断），避免静默吞异常。

### P2（可选）

- [ ] **P2** `src/main/java/com/example/demo/report/ReportService.java:70` — 拆分超 120 字符的单行（normalizeDimension）。
- [ ] **P2** `src/main/java/com/example/demo/controller/DemoController.java:89` — `((Number) element).intValue()` 对超出 int 范围的大数会截断/溢出，建议校验 `int`/`long` 范围后再转换（使冒泡排序入参校验更严格）。
- [ ] **P2** `src/main/java/com/example/demo/tracking/TrackingMapper.java:63-64` — 维度枚举三列各做一次全表 DISTINCT，示例可接受，若数据量大建议合并查询或加缓存。