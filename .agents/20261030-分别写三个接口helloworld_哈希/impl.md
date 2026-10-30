# 编码实现报告：算法演示与调用埋点报表

> 对应系分：`.agents/20261030-分别写三个接口helloworld_哈希/design.md`
> 日期：2026-10-30

## 模块进度追踪

| 序号 | 模块 | READ | TEST | IMPL | CHECK | DOCS | 状态 |
|:----:|------|:----:|:----:|:----:|:-----:|:----:|------|
| 1 | algorithm（算法模块） | ✅ | ✅ | ✅ | ✅ | ✅ | 已完成 |
| 2 | exportx（导出模块） | ✅ | ✅ | ✅ | ✅ | ✅ | 已完成 |
| 3 | track（埋点模块） | ✅ | ✅ | ✅ | ✅ | ✅ | 已完成 |
| 4 | 前端演示页（huanmeng511331-project） | ✅ | ➖ | ✅ | ✅ | ✅ | 已完成 |

## 已实现文件清单

### 后端 [testllw]

- `pom.xml`（Spring Boot 3.2.5 + MyBatis + MySQL + Validation）
- `src/main/java/com/example/demo/DemoApplication.java`
- `common/Result.java` / `common/BizException.java` / `common/GlobalExceptionHandler.java`
- `algorithm/DemoAlgorithmService.java` / `DemoAlgorithmServiceImpl.java` / `DemoController.java`（W01/W02/W03）
- `algorithm/enums/HashAlgorithm.java` / `SortOrder.java`
- `algorithm/dto/HashRequest.java` / `BubbleSortRequest.java` / `BubbleSortResult.java`
- `exportx/ExportController.java`（W04）/ `ExportService.java` / `ExportServiceImpl.java` / `enums/ExportType.java`
- `track/TrackInterceptor.java` / `TrackService.java` / `TrackServiceImpl.java` / `TrackQueryController.java`（W05）
- `track/entity/ApiInvokeTrack.java` / `mapper/ApiInvokeTrackMapper.java` / `resources/mapper/ApiInvokeTrackMapper.xml`
- `track/enums/ApiCode.java` / `StatsDimension.java` / `track/dto/TrackStatsQuery.java` / `TrackStatsResponse.java`
- `config/WebConfig.java`（拦截器注册 + 异步线程池 2/4/500 CallerRuns + CORS）
- `src/main/resources/application.yml` / `src/main/resources/sql/schema.sql`

### 单测

- `src/test/java/com/example/demo/algorithm/DemoAlgorithmServiceImplTest.java`（11 用例：正常/参数校验/算法不支持/边界/提前退出）
- `src/test/java/com/example/demo/track/TrackServiceImplTest.java`（5 用例：落库/聚合/非法维度/超 90 天/写库失败吞异常）

### 前端 [huanmeng511331-project]

- `package.json` / `vite.config.js`（/api 代理 8080）/ `index.html`
- `src/main.jsx` / `src/App.jsx` / `src/index.css`
- `src/api.js`（统一 {code,msg,data} 解析 + X-User-* 请求头透传 + exportCsv 下载）
- `src/pages/DemoPage.jsx`（三 Tab + 各 Tab 导出按钮）
- `src/pages/ReportPanel.jsx`（维度/时间选择 + 折线/饼图/柱状 ECharts + 报表导出）

## CHECK 结果

### L1 静态检查

| 检查项 | 符合情况 |
|--------|:--------:|
| 命名规范（类大驼峰/方法小驼峰/常量全大写） | ✅ |
| 统一出参 {code,msg,data}，错误码 {MODULE}_{SEQ} | ✅ |
| 异常日志：SLF4J 占位符、自定义 BizException、全局异常处理器 | ✅ |
| 安全：SQL 全参数化 #{}；${dimensionColumn} 仅来源于 StatsDimension 枚举白名单；@Valid 入参校验 | ✅ |
| MySQL：表名小写下划线、gmt_create/gmt_modified、resultMap | ✅ |
| 埋点异步不阻塞主流程；失败仅记日志（R03） | ✅ |
| 单元测试存在，AAA 模式 | ✅ |

### L2 动态验证

| 验证项 | 状态 | 说明 |
|--------|:----:|------|
| 编译验证 | ⚠️ 跳过 | 执行环境无 java/mvn，未编译 |
| 单测验证 | ⚠️ 跳过 | 同上 |

### 📋 待人工验证

```bash
# 后端
cd testllw-main && mvn compile -DskipTests && mvn test
mysql -uroot -p < src/main/resources/sql/schema.sql && mvn spring-boot:run
# 前端
cd huanmeng511331-project-main && npm install && npm run dev
```

## 跨仓契约对齐

- W01 `GET /api/demo/helloworld?name=` → 前端 `callHello` ✅
- W02 `POST /api/demo/hash {input, algorithm}` → `callHash` ✅
- W03 `POST /api/demo/bubble-sort {numbers, order}` → `callBubbleSort` ✅
- W04 `GET /api/demo/export?type=&...`（CSV 流 + UTF-8 BOM）→ `exportCsv` ✅
- W05 `GET /api/track/stats?dimension=&apiCode=&startTime=&endTime=` → `fetchTrackStats`，出参 totalCount/trend/dimensionStats 字段一致 ✅
- 人员头 X-User-Id/X-User-Name/X-User-Type/X-User-Level/X-User-Dept 前后端一致 ✅
