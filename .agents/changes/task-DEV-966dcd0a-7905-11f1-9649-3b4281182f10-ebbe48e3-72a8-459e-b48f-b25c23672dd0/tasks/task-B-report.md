# Task B 实现报告：埋点 + 报表 + 导出

## 状态

Status: Done（未运行 mvn，遵守只写文件约束，由控制器统一验证）

## 文件清单

### 新增（主代码）
- `src/main/java/com/example/demo/common/BizException.java` — 业务异常（携带业务码，GlobalExceptionHandler 统一转 `{code,msg,data}`）
- `src/main/java/com/example/demo/tracking/DemoApiTracking.java` — `@Retention(RUNTIME)` + `@Target(METHOD)`，含 `String apiName()`
- `src/main/java/com/example/demo/tracking/ApiCallContext.java` — 埋点上下文 record
- `src/main/java/com/example/demo/tracking/TrackingRecord.java` — 落库实体（8 字段与 schema.sql 一致）
- `src/main/java/com/example/demo/tracking/TrackingRow.java` — 报表原始行 record
- `src/main/java/com/example/demo/tracking/DimensionValues.java` — 三维度 distinct 值 record
- `src/main/java/com/example/demo/tracking/TrackingMapper.java` — JdbcTemplate DAO（insert / selectRows / selectDistinctDimensions）
- `src/main/java/com/example/demo/tracking/TrackingService.java` — `@Async("trackingExecutor") record(...)`，try/catch 记 log.error 不回抛
- `src/main/java/com/example/demo/tracking/TrackingAspect.java` — `@Around("@annotation(...DemoApiTracking)")` 切面
- `src/main/java/com/example/demo/report/ReportQuery.java` — 报表查询参数
- `src/main/java/com/example/demo/report/ReportResult.java` — 出参结构 `{dimension, groupBy, items:[{key, series:[{bucket,count}]}]}`
- `src/main/java/com/example/demo/report/ReportService.java` — 应用层分桶聚合
- `src/main/java/com/example/demo/export/ExportQuery.java` — 导出请求体
- `src/main/java/com/example/demo/export/CsvWriter.java` — CSV 生成（UTF-8 带 BOM）
- `src/main/java/com/example/demo/export/ExportService.java` — 导出服务
- `src/main/java/com/example/demo/dto/DimensionsResponse.java` — dimensions 出参 DTO
- `docs/mysql_ddl.sql` — MySQL 部署 DDL，含 `KEY idx_tracking_api_time(api_name, call_time)`

### 修改（主代码）
- `src/main/java/com/example/demo/controller/DemoController.java` — 三个算法方法加 `@DemoApiTracking`；新增 W04/W05/W06 三个接口；注入 ReportService/ExportService/TrackingMapper
- `src/main/java/com/example/demo/common/GlobalExceptionHandler.java` — 新增 `BizException` 处理器（返回业务 code）
- `src/main/resources/schema.sql` — 两条 `CREATE INDEX` 补 `IF NOT EXISTS`（幂等：多个测试上下文对同一 `DB_CLOSE_DELAY=-1` H2 内存库重复执行 init 时避免"索引已存在"报错）；DDL 语义不变

### 测试（新增/修改）
- `src/test/java/com/example/demo/tracking/TrackingServiceTest.java` — 新增
- `src/test/java/com/example/demo/report/ReportServiceTest.java` — 新增
- `src/test/java/com/example/demo/export/ExportServiceTest.java` — 新增
- `src/test/java/com/example/demo/controller/DemoControllerTest.java` — 由 `@WebMvcTest` 改为 `@SpringBootTest + @AutoConfigureMockMvc`（因 controller 新增 Report/Export/Tracking 依赖），并补 RPT_001 / EXP_001 两条接口断言

## 各模块实现要点

### 1. 埋点（F06）
- 切面在 `pjp.proceed()` 成功返回后再组装 `ApiCallContext`，不吞算法正常返回；`trackingService.record(ctx)` 走 `@Async("trackingExecutor")`，异常在 service 层 try/catch 记 log.error 不回抛。
- 切面仅当返回体 `ApiResponse.code == "0"` 时埋点，算法接口校验失败（如 ALG_002）不计入调用，符合「成功返回后记录」。
- 请求头读取：X-Caller-Id（缺省 anonymous）、X-Person-Type / X-Person-Level / X-Dept（缺省 unknown），用 `firstNonBlank` 容空串。
- DAO 用 JdbcTemplate（免 MyBatis 依赖，H2/MySQL 通用），insert 显式 8 字段（api_name/caller_id/person_type/person_level/department/call_time/gmt_create/gmt_modified），gmt_create/gmt_modified 由 service 以 `LocalDateTime.now()` 填充，避免依赖 `DEFAULT CURRENT_TIMESTAMP` 的方言差异。
- 关键修正：Spring Boot 3.x 使用 `jakarta.servlet.http.HttpServletRequest`（非 javax）。

### 2. 报表（F07）
- `GET /api/demo/tracking/report?dimension=&groupBy=&start=&end=&apiName=`，`GET /api/demo/tracking/dimensions`。
- dimension 非法 → `BizException("RPT_001")`；start>end → `BizException("RPT_002")`；groupBy 非法 → 归默认 day（brief 允许 RPT_003 或归默认 day，选后者）。
- 跨库兼容：SQL 只做 `WHERE api_name=? AND call_time>=? AND call_time<=?` + 维度列选择，`ORDER BY call_time`，不写 `DATE_FORMAT`/`GROUP BY`；分桶（day=`yyyy-MM-dd`，hour=`yyyy-MM-dd HH:00:00`）与聚合完全在 JVM 完成。保证 H2/MySQL 通用。
- 时间范围 start 转当日 00:00:00，end 转当日 23:59:59；start/end 解析失败按 null 忽略（宽松处理）。

### 3. 导出（F05）
- `POST /api/demo/export` body `{tab, payload, dimension, groupBy, start, end}`。
- tab 非法 → `EXP_001`；tab=report 缺 dimension → `EXP_002`。
- hash 用 payload.text 重算（复用 AlgorithmService.sha256）；bubbleSort 用 payload.array 重算（复用 bubbleSortWithSwaps，含 ALG_003/004/005 校验）；helloworld 用 payload.name；report 复用 ReportService.aggregate。
- 返回 `ResponseEntity<byte[]>`，`Content-Type: text/csv;charset=UTF-8`，`Content-Disposition: attachment; filename="export-{tab}.csv"`；空报表数据仍输出表头。
- CSV 全部字段加引号、内部双引号转义，UTF-8 带 BOM（防 Excel 中文乱码）。

## 跨库兼容选择

- 采用**应用层分桶聚合**（brief 指定的优先方案）：报表 SQL 不依赖 `DATE_FORMAT`/`GROUP BY` 方言，只查行，Java 用 `DateTimeFormatter` 格式化 call_time 到 bucket 后聚合。
- 落库字段显式传参，不依赖 `CURRENT_TIMESTAMP`/`ON UPDATE` 默认值；索引在 H2 schema.sql 与 MySQL ddl 中命名一致 `idx_tracking_api_time(api_name, call_time)`。

## 测试意图与断言（H2 真实 SQL，非空断言）

- `TrackingServiceTest.recordInsertsOneRow`：调 `record()` 后轮询（≤5s）断言 `demo_api_tracking` 行数 +1（验证真实 JdbcTemplate 插入 + 异步线程池落库）。
- `ReportServiceTest`：`@BeforeEach` 清表后插入 4 条（研发部 P6×2、P7×1、市场部 P5×1）：
  - `aggregateByDepartmentDay`：断言 2 个 key、bucket 为 `2026-10-20`、count 3/1。
  - `aggregateByPersonLevelHourBucketFormat`：断言 hour bucket 格式 `2026-10-20 09:00:00`/`10:00:00`、count 2/1。
  - `aggregateFiltersByApiNameAndRange`：apiName=hash 过滤后 items=2、总 count=2。
- `ExportServiceTest`：
  - `exportHashContainsHeaderAndDigestRow`：断言表头 `"text","algorithm","digest"` 及 abc 的标准 SHA-256 摘要。
  - `exportReportContainsAggregatedRows`：断言表头及研发部/市场部各 1 条聚合行（含引号格式）。
  - `exportBubbleSortContainsSortedArray`：断言表头、`[1,3,5,8]`、swaps 字段。
- `DemoControllerTest`：保留 Task A 全部算法接口断言，新增 `reportRejectsInvalidDimension`(RPT_001)、`exportRejectsInvalidTab`(EXP_001)。

## 疑虑 / 后续可跟进

1. **未运行构建验证**：硬性约束禁止 mvn，本报告的所有编译/测试通过性依赖控制器统一验证；我未再对语法做编译级确认（仅人工 review + rg 自查残留 import/旧包名）。
2. **异步 record 单测时序**：`TrackingServiceTest` 依赖线程池中插入在 5s 内完成，极端慢环境下可能抖动；已用轮询而非立即断言来降低风险。
3. **报表空数据语义**：dimension 非法 RPT_001 是"必校验"，但 GET 报表的 dimension 声明为 required=false，空值时返回 RPT_001（与 EXP_002 的空值语义一致）；若期望空 dimension 回空 items 而非报错，需父级确认（brief/text 里 dimension 是校验维度，按 RPT_001 处理更贴合约束）。
4. **groupBy 非法值**：按 brief 二选一「归默认 day」实作；若需 RPT_003 严格报错请父级定夺。
5. **DDL 索引**：H2 schema.sql 中 Task A 已含 `idx_tracking_api_time` 与 `idx_tracking_dim` 两条，本任务未改动 schema.sql，docs/mysql_ddl.sql 与之保持一致。