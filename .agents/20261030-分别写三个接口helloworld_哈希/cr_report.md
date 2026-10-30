# Code Review Report

> **Change** `20261030-分别写三个接口helloworld_哈希` · **分支/Commit** `task/DEV-d885ee0d-78e2-11f1-8f3f-75954cb1c56f-749cf78f-b50a-4a5e-8348-79b1e94cf367` / `base main` · **日期** `2026-10-30` · **审查者** AI
>
> **AI**：等级 **P0 / P1 / P2**；G/S 以 checklist 行内定义为准；Bug 模式以 `bug-pattern-checklist.md` 表头为准（Blocker→P0、Major→P1、Info→P2）。**已先**运行 `scan-all-rules.sh`（52/222 规则，13 项命中），要点并入 §5，**再**由 LLM 完成复核与补扫。
>
> **Spec 依据**：`.agents/20261030-分别写三个接口helloworld_哈希/design.md`（v1.0）、`impl.md`。

---

## 1. 审查范围

| 项 | 值 |
|----|-----|
| `.java` 文件数 | `29`（main 27 + test 2） |
| 变更行数 | `+2294 / -0`（35 文件全新增，含 XML/yml/SQL/前端文件） |

| 类/接口 | 路径 | 角色 |
|---------|------|--------------|
| DemoApplication | `src/main/java/com/example/demo/DemoApplication.java` | 启动类 |
| DemoController | `src/main/java/com/example/demo/algorithm/DemoController.java` | W01/W02/W03 oneapi |
| DemoAlgorithmService(Impl) | `src/main/java/com/example/demo/algorithm/DemoAlgorithmService*.java` | S01/S02/S03 算法实现 |
| HashRequest / BubbleSortRequest / BubbleSortResult | `src/main/java/com/example/demo/algorithm/dto/*.java` | 算法出入参 |
| HashAlgorithm / SortOrder | `src/main/java/com/example/demo/algorithm/enums/*.java` | 算法枚举 |
| BizException / GlobalExceptionHandler / Result | `src/main/java/com/example/demo/common/*.java` | 通用出参与异常 |
| WebConfig | `src/main/java/com/example/demo/config/WebConfig.java` | 拦截器/线程池/CORS |
| ExportController | `src/main/java/com/example/demo/exportx/ExportController.java` | W04 CSV 导出 |
| ExportService(Impl) / ExportType | `src/main/java/com/example/demo/exportx/*.java` | S04 导出实现 |
| TrackInterceptor | `src/main/java/com/example/demo/track/TrackInterceptor.java` | F06 埋点采集 |
| TrackQueryController / TrackService(Impl) | `src/main/java/com/example/demo/track/*.java` | W05/S05/S06 统计 |
| TrackStatsQuery / TrackStatsResponse / ApiInvokeTrack | `src/main/java/com/example/demo/track/dto|entity/*.java` | 埋点模型 |
| ApiCode / StatsDimension | `src/main/java/com/example/demo/track/enums/*.java` | 埋点枚举（维度白名单） |
| ApiInvokeTrackMapper(+xml) | `src/main/java/com/example/demo/track/mapper/*` + `src/main/resources/mapper/ApiInvokeTrackMapper.xml` | DAO |
| DemoAlgorithmServiceImplTest / TrackServiceImplTest | `src/test/java/com/example/demo/**/*.java` | 单测 |

跨仓（前端仓 huanmeng511331-project，不在 Java 审查范围内，仅做契约核对）：`src/api.js`、`src/pages/DemoPage.jsx`、`src/pages/ReportPanel.jsx` 调用路径与后端 W01–W05 全部一致。

---

## 2. 问题计数

| P0 | P1 | P2 |
|----|----|-----|
| 0 | 2 | 4 |

> 脚本初扫 P0=5（G16.2），经 LLM 逐条复核：3 处为误报（catch 内已有日志），2 处为「捕获后转抛业务异常」的合理写法，降级为 P2。详见 §5。

---

## 3. Step 2 — 功能（REQ）

### REQ-1: 三个算法接口（F01/F02/F03，design.md §5.1.2 W01–W03）

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| GET /api/demo/helloworld，name 缺省返回 "Hello, World!"，name≤50 否则 DEMO_001 | ✅ | design.md §5.1.2 W01、R01「name ≤ 50 字符」 | `DemoController.java:30-33`、`DemoAlgorithmServiceImpl.java:30-39` | 出参含 message+timestamp |
| POST /api/demo/hash，支持 MD5/SHA1/SHA256，默认 SHA256，input≤4096，非法算法 DEMO_002 | ✅ | W02、R02、A03 | `HashAlgorithm.java:142-152`、`HashRequest.java:10`（@NotBlank+@Size(4096)）、`DemoAlgorithmServiceImpl.java:42-56` | UTF-8 字节哈希，十六进制输出 |
| POST /api/demo/bubble-sort，数组 1–1000，ASC/DESC，返回 sorted/compareCount/swapCount/elapsedMs，含提前退出优化 | ✅ | W03、R03、R04 | `BubbleSortRequest.java:24`、`DemoAlgorithmServiceImpl.java:59-90`（`swapped` 提前退出） | 单测覆盖 |

### REQ-2: 导出接口（F05，design.md §5.2.2 W04）

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| GET /api/demo/export 按 type=HELLO/HASH/BUBBLE_SORT/TRACK_STATS 输出 CSV，UTF-8 BOM，≤1 万行，非法 type EXPORT_001，算法类缺参 EXPORT_003 | ✅ | W04、R01/R02/R03、A05 | `ExportController.java:34-59`、`ExportServiceImpl.java:37-51`（MAX_EXPORT_ROWS=10000）、`ExportType.of` | BOM 写入 `ExportController.java:26,52`；CSV 转义含引号/逗号/换行处理 |

### REQ-3: 埋点采集（F06，design.md §5.3.3.1）

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 拦截 /api/demo/**，解析 X-User-* 请求头，缺失填 UNKNOWN，超长截断，异步线程池（2/4/500，CallerRuns）落库，失败不影响主流程 | ✅ | R01/R02/R03、A02、线程池参数 | `TrackInterceptor.java:36-74`、`WebConfig.java:25-27,40-50`、`TrackServiceImpl.java:40-49` | 拦截路径含 /api/demo/export，与 R01 一致 |
| 调用结果判定：异常或 HTTP≥500 记 FAIL | ⚠️ | §5.1.3.1 异常场景「全局异常处理器返回 DEMO_999…埋点记录失败结果」 | `TrackInterceptor.java:68-69` | 业务失败（BizException，HTTP 200）被记为 SUCCESS，见 §5 G6 说明，P2 |

### REQ-4: 埋点统计查询（F07，design.md §5.3.2 W05）

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| GET /api/track/stats，dimension 必填∈白名单，跨度≤90 天 TRACK_002，返回 totalCount/trend/dimensionStats(count+ratio) | ✅ | W05、R01/R02/R03 | `TrackQueryController.java:55-66`、`TrackServiceImpl.java:52-78`、`ApiInvokeTrackMapper.xml` countTrendByDay/countByDimension | 缺省近 7 天符合设计 |

### REQ-5: 跨仓契约（F04/F08 前端消费）

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 前端三 Tab + 导出按钮 + 报表（折线/饼/柱状，ECharts）调用后端 W01–W05 | ✅ | design.md §5.4 | `[huanmeng511331-project] src/api.js:25-59`、`DemoPage.jsx:83-100`（三 Tab）、`ReportPanel.jsx:2,16`（echarts） | 路径 `/api/demo/helloworld|hash|bubble-sort|export`、`/api/track/stats` 与后端完全一致 |

---

## 4. Step 3 — 可读性检查

| 结果 | 说明（违规写 Ax.x 与 `path:行`） |
|------|--------------------------------|
| ⚠️ | A3：`WebConfig.java:47` 使用全限定名 `new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy()` 而非 import，风格不一致（P2）。其余 A1–A7（格式、import 顺序、命名、Javadoc、常量抽取）整体良好：魔法数均已抽常量（MAX_NAME_LENGTH/MAX_EXPORT_ROWS/MAX_STATS_RANGE_DAYS），类与方法注释齐备，record DTO 使用恰当。 |

---

## 5. Step 4 — 可靠性检查

**自动化预扫（scan-all-rules.sh，52/222 规则）原始输出摘要：**

```
[P0] G16.2 CatchWithoutLogging: DemoAlgorithmServiceImpl.java:101 / ExportController.java:56 /
     ExportServiceImpl.java:83 / TrackInterceptor.java:71 / TrackServiceImpl.java:44
[P1] M016 JavaTimeDefaultTimeZone: DemoAlgorithmServiceImpl.java:37 / ExportController.java:48 /
     ExportServiceImpl.java:101 / TrackInterceptor.java:49 / TrackQueryController.java:35 /
     TrackServiceImplTest.java:49,68
[P1] S10.2 CorsWildcard: WebConfig.java:32
```

**LLM 复核结论：**

| 域 | 参考 | 结果 | 等级 | 说明（列命中 ID 或「已扫无命中」） |
|----|------|------|------|-------------------------------------|
| 可靠性 | `reliability-checklist.md` G1–G17 | ⚠️ | 见下 | G16.2 脚本命中 5 处复核：`ExportController.java:56`、`TrackInterceptor.java:71`、`TrackServiceImpl.java:44` 均有 LOGGER 输出，**误报**；`DemoAlgorithmServiceImpl.java:101`（NoSuchAlgorithmException 转抛 BizException，枚举白名单下不可达）、`ExportServiceImpl.java:83`（NumberFormatException 转抛 BizException，message 含非法值）为合理转抛，降级 **P2**。G1 并发：埋点异步线程池 CallerRuns 与设计一致 ✅；G9 边界：空数组/超长/空输入均校验 ✅；G14/G15 降级开关 track.enabled/export.enabled（export.enabled 配置存在但 ExportController 未消费该开关，见 P2-3）。 |
| 安全 | `security-checklist.md` S1–S10 | ❌ | P1 | **S10.2** `WebConfig.java:32` CORS `allowedOriginPatterns("*")` + `allowedHeaders("*")`，演示可接受但生产风险，P1。S1 SQL 注入：`${dimensionColumn}` 仅来自 StatsDimension 枚举白名单（`StatsDimension.java:10-13` + xml 注释），其余全部 `#{}` 预编译 ✅。S2 认证：设计明确不自建登录（A02），请求头注入属既定假设，N/A。S3 密钥：数据库口令走环境变量占位 `${MYSQL_PASSWORD:root}`，无硬编码 ✅。 |
| Bug 模式 | `bug-pattern-checklist.md` B/M/I（120） | ⚠️ | 见下 | **M016** `LocalDateTime.now()` 使用默认时区共 6 处（见脚本清单），P1，建议统一注入 ZoneId 或依赖 serverTimezone=Asia/Shanghai 显式化。脚本未覆盖 B/M/I 项由 LLM 补扫：集合初始化容量、equals/hashCode、字符串拼接（StringBuilder 已用）、资源关闭（try-with-resources 已用）、判空（record+校验注解+显式判空齐备），无其余命中。 |

---

## 6. Step 5 — 自定义扩展检查

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|------|
| 自定义扩展 | `customized-checklist.md` U* | N/A | - | N/A(未启用自定义规则)（文件为示例项） |

---

## 7. 结论

- **合并建议**：修复后合并（无 P0 阻塞；P1 建议合并前修复）
- **P0**：无。
- **P1/P2**：
  1. **P1 S10.2** `WebConfig.java:32` CORS 通配。
  2. **P1 M016** 6 处 `LocalDateTime.now()` 默认时区。
  3. **P2** `TrackInterceptor.java:68` 业务失败（BizException/HTTP 200）被记为 SUCCESS，埋点 invoke_result 失真。
  4. **P2** `application.yml` 存在 `export.enabled` 开关但 `ExportController` 未消费，与设计 §7.2 可应急不一致。
  5. **P2** `DemoAlgorithmServiceImpl.java:101`、`ExportServiceImpl.java:83` 异常转抛未留日志（G16.2 降级项）。
  6. **P2** `WebConfig.java:47` 全限定名使用，风格不一致。
- **一句话**：功能与 spec 全量对齐、SQL 注入白名单防护到位、异步埋点降级策略完整，代码质量良好；CORS 通配与时区处理是仅有的两处 P1 工程隐患。

---

## 7.1 问题片段（必填）

- **P1** `S10.2` `src/main/java/com/example/demo/config/WebConfig.java:32` — CORS 全通配，生产环境跨域风险。  
  片段范围：`WebConfig.java:29-35`

```java
L29|    @Override
L30|    public void addCorsMappings(CorsRegistry registry) {
L31|        registry.addMapping("/api/**")
L32|                .allowedOriginPatterns("*")
L33|                .allowedMethods("GET", "POST")
L34|                .allowedHeaders("*");
L35|    }
```

- **P1** `M016` `src/main/java/com/example/demo/track/TrackInterceptor.java:49` — `LocalDateTime.now()` 依赖 JVM 默认时区，容器时区漂移时埋点时间与报表聚合错位（同类问题共 6 处，见 §5 脚本清单）。  
  片段范围：`TrackInterceptor.java:44-51`

```java
L44|        track.setCallerId(truncate(headerOrDefault(request, "X-User-Id"), 64));
L45|        track.setCallerName(truncate(headerOrDefault(request, "X-User-Name"), 64));
L46|        track.setPersonType(truncate(headerOrDefault(request, "X-User-Type"), 16));
L47|        track.setPersonLevel(truncate(headerOrDefault(request, "X-User-Level"), 8));
L48|        track.setPersonDept(truncate(headerOrDefault(request, "X-User-Dept"), 64));
L49|        track.setInvokeTime(LocalDateTime.now());
L50|        request.setAttribute(ATTR_START_NANOS, System.nanoTime());
L51|        request.setAttribute(ATTR_TRACK, track);
```

- **P2** `G6.1` `src/main/java/com/example/demo/track/TrackInterceptor.java:68` — 仅以 HTTP 状态判定成败；BizException 经 GlobalExceptionHandler 返回 HTTP 200 + `code=DEMO_001`，业务失败被埋为 SUCCESS。  
  片段范围：`TrackInterceptor.java:61-73`

```java
L61|        try {
L62|            Object trackAttr = request.getAttribute(ATTR_TRACK);
L63|            Object startAttr = request.getAttribute(ATTR_START_NANOS);
L64|            if (!(trackAttr instanceof ApiInvokeTrack track) || !(startAttr instanceof Long startNanos)) {
L65|                return;
L66|            }
L67|            track.setCostMs((System.nanoTime() - startNanos) / 1_000_000);
L68|            boolean success = ex == null && response.getStatus() < 500;
L69|            track.setInvokeResult(success ? "SUCCESS" : "FAIL");
L70|            trackService.record(track);
L71|        } catch (Exception e) {
L72|            LOGGER.error("track record failed, path={}", request.getRequestURI(), e);
L73|        }
```

- **P2** `G15.1` `src/main/java/com/example/demo/exportx/ExportController.java:34-46` — 未消费 `export.enabled` 开关（design §7.2 应急方案要求可开关摘除导出）。片段：

```java
L34|    @GetMapping("/export")
L35|    public void export(@RequestParam String type,
L36|                       @RequestParam(required = false) String name,
L37|                       @RequestParam(required = false) String input,
L38|                       @RequestParam(required = false) String algorithm,
L39|                       @RequestParam(required = false) String numbers,
L40|                       @RequestParam(required = false) String dimension,
```

- **P2** `G16.2` `src/main/java/com/example/demo/algorithm/DemoAlgorithmServiceImpl.java:101` — 捕获后转抛无日志（枚举白名单下不可达，降级 P2）。  
  片段范围：`DemoAlgorithmServiceImpl.java:92-104`

```java
L92|    private String digest(HashAlgorithm algorithm, String input) {
L93|        try {
L94|            MessageDigest messageDigest = MessageDigest.getInstance(algorithm.getJcaName());
L95|            byte[] bytes = messageDigest.digest(input.getBytes(StandardCharsets.UTF_8));
L96|            StringBuilder sb = new StringBuilder(bytes.length * 2);
L97|            for (byte b : bytes) {
L98|                sb.append(String.format("%02x", b));
L99|            }
L100|            return sb.toString();
L101|        } catch (NoSuchAlgorithmException e) {
L102|            throw new BizException("DEMO_002", "不支持的哈希算法: " + algorithm);
L103|        }
L104|    }
```

- **P2** `A3` `src/main/java/com/example/demo/config/WebConfig.java:47` — 全限定名内联使用。  
  片段范围：`WebConfig.java:42-49`

```java
L42|        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
L43|        executor.setCorePoolSize(2);
L44|        executor.setMaxPoolSize(4);
L45|        executor.setQueueCapacity(500);
L46|        executor.setThreadNamePrefix("track-async-");
L47|        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
L48|        executor.initialize();
L49|        return executor;
```

---

## 8. 修复任务列表

### P0

- 无 P0 待修复项。

### P1

- [ ] **P1** `S10.2` `src/main/java/com/example/demo/config/WebConfig.java:32` — 将 CORS `allowedOriginPatterns("*")` 收敛为可配置白名单域名（生产）。
- [ ] **P1** `M016` `DemoAlgorithmServiceImpl.java:37`、`ExportController.java:48`、`ExportServiceImpl.java:101`、`TrackInterceptor.java:49`、`TrackQueryController.java:35` — 统一显式时区（如 `LocalDateTime.now(ZoneId.of("Asia/Shanghai"))` 或注入 Clock）。

### P2（可选）

- [ ] **P2** `TrackInterceptor.java:68` — 成败判定结合响应体 `Result.code`（如包装响应或按 ex + status≥400 判定），避免业务失败记为 SUCCESS。
- [ ] **P2** `ExportController.java:34` — 接入 `export.enabled` 开关，关闭时返回 EXPORT_001/404，兑现设计 §7.2 应急方案。
- [ ] **P2** `DemoAlgorithmServiceImpl.java:101`、`ExportServiceImpl.java:83` — 异常转抛时补 WARN 日志。
- [ ] **P2** `WebConfig.java:47` — `CallerRunsPolicy` 改为 import，与全文风格一致。
