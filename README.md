# testllw — 三接口全栈示例（helloworld / 哈希 / 冒泡排序 + 埋点报表）

一个最小可运行的全栈示例，打通「后端算法接口 → 前端展示 → 导出 → 调用埋点 → 报表可视化」闭环。

## 需求对照

1. 三个接口：HelloWorld、哈希算法（SHA-256）、冒泡排序（升序 + 交换次数）。
2. 前端新增页面，三个 Tab 分别展示三个接口执行结果。
3. 导出按钮 + 后端导出接口，支持导出各 Tab 展示结果（及报表数据）。
4. 后端调用埋点，记录调用次数与调用人；前端同页可视化报表，按人员类型/人员层级/人员部门维度，以折线图、饼图、柱状图展示。

## 目录

- 后端（仓库根，Maven + Spring Boot 3）：`src/main/java/com/example/demo/**`
- 前端：`frontend/`（React + Ant Design + ECharts + Vite）
- MySQL 部署 DDL：`docs/mysql_ddl.sql`

## 后端

默认使用 H2 内存库（无需外部依赖），可直接运行：

```bash
mvn -q -DskipTests=false test   # 运行测试（25 个用例）
mvn spring-boot:run            # 启动，默认端口 8080
```

切换 MySQL：修改 `src/main/resources/application.yml` 的 `spring.datasource.*`，先执行 `docs/mysql_ddl.sql`（mysql-connector-j 已在依赖中）。

统一响应结构 `{ code, msg, data }`，`code="0"` 表示成功。

### 接口（前缀 /api/demo）

| 方法 | 路径 | 入参 | 出参 data |
|------|------|------|-----------|
| GET | `/helloworld` | `name`（可选，≤64，缺省 World） | `{message}` |
| POST | `/hash` | `{text}`（非空） | `{algorithm:"SHA-256", digest}` |
| POST | `/bubble-sort` | `{array: number[]}`（1~1000） | `{sorted, swaps}` |
| POST | `/export` | `{tab, payload?, dimension?, groupBy?, start?, end?}` | CSV 文件流 |
| GET | `/tracking/report` | `dimension`(person_type/person_level/department)、`groupBy`(day/hour)、`start`、`end`、`apiName` | `{dimension, groupBy, items:[{key, series:[{bucket,count}]}]}` |
| GET | `/tracking/dimensions` | 无 | `{personTypes[], personLevels[], departments[]}` |

### 埋点

算法三接口由 `@DemoApiTracking` 注解 + AOP 切面在**成功返回后**异步落库（线程池 `trackingExecutor`），写库失败仅记日志不影响主流程。调用人/维度取自请求头，缺省 anonymous / unknown：

- `X-Caller-Id` → caller_id（调用人）
- `X-Person-Type` → person_type（人员类型）
- `X-Person-Level` → person_level（人员层级）
- `X-Dept` → department（部门）

表 `demo_api_tracking` 字段见 `schema.sql` / `docs/mysql_ddl.sql`。报表为实时聚合（SQL 查原始行、JVM 分桶），H2/MySQL 通用。

错误码：`ALG_001`~`ALG_005`（算法校验）、`RPT_001/RPT_002`（报表）、`EXP_001/EXP_002`（导出）、`PARAM_ERROR`（请求体/参数）。

## 前端

```bash
cd frontend
npm install
npm run dev     # http://localhost:5173，/api 代理到 8080
```

路由 `/demo`：

- 顶部三 Tab（HelloWorld / 哈希 / 冒泡排序），每 Tab 为「入参 + 执行 + 结果」。
- 「调用人设置」：填写调用人/人员类型/层级/部门，随请求头透传埋点。
- 导出按钮：导出当前 Tab 结果或报表 CSV。
- 报表区：维度（人员类型/层级/部门）+ 时间范围 + 时间粒度，三图（折线=趋势、饼图=占比、柱状图=分组对比）。

## 说明

- 示例工程，调用人识别以请求头占位，无登录鉴权。
- 埋点异步落库，失败不影响算法接口主链路。