# Task B：后端埋点 + 报表 + 导出（对应 plan Task 3-4）

在 Task A（算法三接口 + 骨架，已完成/进行中）基础上，实现调用埋点、报表聚合与 CSV 导出。**依赖 Task A 的包结构与 schema.sql**，若某些文件尚未生成，请按 A 的约定补齐最小骨架。

## 硬性前置

- 只写文件，**不要运行 mvn 构建**（由控制器统一验证）。
- Git 严格只读；禁止 grep/glob（用 rg/find/read/ls）。
- 包名 `com.example.demo`；统一响应 `{code,msg,data}`，code 字符串 "0" 成功。
- 埋点**异步**落库（线程池 bean 名 `trackingExecutor`，Task A 已定义 AsyncConfig），失败仅记错误日志不回抛。

## 上下文

读取 `.agents/20261020-分别写三个接口helloworld_哈希/design.md` 的 5.2（埋点）、5.3（报表）、5.4（导出）三节与 4.1 接口总览；`spec.md` 第 3 节接口设计；`plan.md` 全局约束。

## 要求实现

### 1. 埋点模块（F06）
- 注解 `tracking/DemoApiTracking`：`@Retention(RUNTIME)`，`@Target(METHOD)`，含 `String apiName()`。
- 切面 `tracking/TrackingAspect`：`@Around("@annotation(com.example.demo.tracking.DemoApiTracking)")`；算法方法成功返回后，从当前请求读取 Header `X-Caller-Id`（默认 anonymous）、`X-Person-Type`/`X-Person-Level`/`X-Dept`（默认 unknown），构造记录，`@Async("trackingExecutor")` 提交给 `TrackingService.record(...)`；写库异常被 service 捕获记 error 日志，不回抛；切面不应吞算法方法的正常返回。
- `tracking/TrackingService`：`@Async("trackingExecutor") void record(ApiCallContext ctx)`，内部 try/catch 调 mapper insert，异常 log.error。
- `tracking/TrackingMapper`（`@Mapper`，或纯 JdbcTemplate 方式——建议用 JdbcTemplate 免 MyBatis 依赖，接口/实现任选，体例简单一致即可）：`insert(TrackingRecord r)`；`selectGroupByDimension(query)`；`selectDistinctDimensions()`。
- `tracking/ApiCallContext` / `TrackingRecord`：字段 api_name、caller_id、person_type、person_level、department、call_time(now)、gmt_create/gmt_modified。
- 在 `DemoController` 三个算法方法上加 `@DemoApiTracking(apiName="helloworld"|"hash"|"bubbleSort")`（若 Task A 尚未加）。

### 2. 报表模块（F07）
- `report/ReportService` + `report/ReportQuery` + DTO：
  - `GET /api/demo/tracking/report?dimension=&groupBy=&start=&end=&apiName=`（controller 加方法）。
  - dimension ∈ person_type|person_level|department，否则业务码 RPT_001；start>end 校验 RPT_002；groupBy day(默认)/hour，非法回 RPT_003（或归默认 day）。
  - groupBy day：SQL 用 `DATE_FORMAT(call_time, '%Y-%m-%d')`；hour：`DATE_FORMAT(call_time, '%Y-%m-%d %H:00:00')`。
  - 出参 data：`{dimension, groupBy, items:[{key, series:[{bucket, count}]}]}`；无数据返回空 items。
  - **跨库兼容**：`DATE_FORMAT` 在 H2 的 MODE=MySQL 下受支持即可；若 H2 不支持，改用应用层分组（查询行后在 JVM 按 call_time 格式化成 bucket 再聚合），确保 H2/MySQL 都能跑通。**优先应用层分桶聚合，规避方言差异**（SQL 只做 `WHERE api_name=? AND call_time BETWEEN ? AND ?` 与维度列选择，分桶在 Java 完成）。
- `GET /api/demo/tracking/dimensions`：返回 data `{personTypes[], personLevels[], departments[]}`（distinct 查询）。

### 3. 导出模块（F05）
- `export/ExportService` + `export/ExportQuery` + `export/CsvWriter`：
  - `POST /api/demo/export` body `{tab, payload?, dimension?, groupBy?, start?, end?}`。
  - tab ∈ helloworld|hash|bubbleSort|report，否则 EXP_001。
  - tab=report 时 dimension 必填，否则 EXP_002；报告聚合导出为 CSV（表头 + 行）。
  - tab=算法时：用 payload 重算（hash 用 text；bubbleSort 用 array；helloworld 用 name）生成 CSV 行。
  - 返回 `ResponseEntity<byte[]>`，Content-Type `text/csv`，`Content-Disposition: attachment; filename="..."`，空数据带表头。
  - 重算复用 `AlgorithmService`（Task A）。
  - 报表导出复用 `ReportService.aggregate(...)`。

### 4. MySQL 部署 DDL
- `docs/mysql_ddl.sql`：`CREATE TABLE demo_api_tracking (id BIGINT AUTO_INCREMENT PRIMARY KEY, api_name VARCHAR(32) NOT NULL, caller_id VARCHAR(64) NOT NULL DEFAULT 'anonymous', person_type VARCHAR(32) NOT NULL DEFAULT 'unknown', person_level VARCHAR(32) NOT NULL DEFAULT 'unknown', department VARCHAR(64) NOT NULL DEFAULT 'unknown', call_time DATETIME NOT NULL, gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, KEY idx_tracking_api_time(api_name, call_time));`（索引命名与 design 一致）。

### 5. 单元测试（真实行为，非空断言）
- `service/ReportServiceTest`：向 H2 插入几条 tracking 记录后，断言按维度聚合 count 正确、时间桶格式正确。
- `service/ExportServiceTest`：断言 hash/report 导出 CSV 字符串含预期行/表头。
- `tracking/TrackingServiceTest`：证实用 JdbcTemplate 插入后行数 +1（同步调用测核心落库逻辑，异步注解不阻塞主路径即可）。
测试不能 mock 成空断言；用 H2 真实执行 SQL。

## 交付
写报告到 `tasks/task-B-report.md`：文件清单、每模块实现要点、跨库兼容选择（应用层分桶）、测试意图与断言、疑虑。回传 ≤15 行：Status / 变更文件 / 测试说明 / 疑虑 / report 路径。