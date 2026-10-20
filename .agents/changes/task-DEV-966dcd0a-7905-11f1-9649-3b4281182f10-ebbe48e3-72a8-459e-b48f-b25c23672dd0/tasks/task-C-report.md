# Task C（前端）实现报告

## 1. 概览与结论

- 状态：DONE_WITH_CONCERNS
- 范围：在仓库根 `frontend/` 下新建零构建依赖可独立预览的 React 单页工程，实现 `/demo` 三 Tab、导出、调用报表三图。
- 约束遵守：仅写文件，未运行 `npm install`/`npm run`（沙箱确认无 node）；Git 只读未做任何写操作（commit/push/checkout 均未执行）；未使用 grep/glob，检索用 find/rg 与 read。
- 关键疑虑：后端（`src/`）在当前仓库尚未实现，前端按 design/spec/plan 三份文档的契约独立编码，本轮仅做静态评审，无法运行时联调验证。

## 2. 文件清单（均位于 frontend/，基于仓库根）

| 文件 | 职责 |
|------|------|
| `frontend/package.json` | 依赖 react/antd/echarts/echarts-for-react/vite/@vitejs/plugin-react；scripts dev/build/preview |
| `frontend/vite.config.js` | Vite 配置，dev 代理 `/api` → `http://localhost:8080` |
| `frontend/index.html` | 页面入口，挂载 `#root` |
| `frontend/src/main.jsx` | React 根渲染，ConfigProvider(zhCN) + AntdApp |
| `frontend/src/App.jsx` | 单页路由：非 `/demo` 路径 `history.replaceState` 归一化到 `/demo`，默认渲染 DemoPage |
| `frontend/src/api.js` | API 客户端：统一前缀、请求头、`{code,msg,data}` 解包、CSV 导出下载 |
| `frontend/src/DemoPage.jsx` | 页面主体：调用人设置 + 三 Tab + 报表区 |
| `frontend/src/reportCharts.jsx` | ECharts 三图封装：LineChart/PieChart/BarChart + 空数据显示 Empty |

共 8 个文件，约 699 行（README 未生成，避免无谓交付）。

## 3. 功能实现方式

### 3.1 路由与页面结构（DemoPage.jsx / App.jsx）

- 采用 brief 允许的「最简路线」：`App.jsx` 用 `window.location.pathname` 判断并在 `useEffect` 中归一化到 `/demo`，默认渲染 `DemoPage`，未引入 `react-router-dom`（brief 明确两者皆可，package.json 依赖清单也未要求该包）。
- `DemoPage` 结构：顶部「调用人设置」卡片 → AntD Tabs（HelloWorld/哈希/冒泡排序）→ 底部「调用情况报表」卡片。

### 3.2 调用人设置（顶部 + CallerSettings 组件）

- 提供四个输入框：`X-Caller-Id`（默认 anonymous）、`X-Person-Type`、`X-Person-Level`、`X-Dept`（后三者默认 unknown）。
- 「应用」后写入 `caller` state，下发到三个 Tab 与报表区；空值安全回退到 anonymous/unknown。
- 页面实时显示当前生效的请求头取值，便于联调核对埋点字段。

### 3.3 三个 Tab（HelloTab / HashTab / SortTab）

- HelloWorld：输入可选 name，执行 → `GET /api/demo/helloworld?name=…`，展示 `data.message`。
- 哈希：输入 text，执行 → `POST /api/demo/hash`（body {text}），展示 `data.algorithm` + `data.digest`（digest 可复制、无换行截断）。
- 冒泡排序：输入逗号分隔数字，前端解析为 number[]（空数组 / 非数字本地友好报错），执行 → `POST /api/demo/bubble-sort`（body {array}），展示 `data.sorted` + `data.swaps`。
- 三个 Tab 均提供「执行」与「导出结果」按钮；导出按 Tab 传 `tab`（helloworld / hash / bubbleSort）与 `payload`。

### 3.4 导出（api.exportCsv）

- 调用 `POST /api/demo/export`，body 含 `tab` 与当前 Tab payload；报表导出则 `tab=report` + `dimension`（可选 `groupBy`/时间范围/`apiName`）。
- 成功（后端返回 `text/csv` 文件流）时，从 `Content-Disposition` 解析文件名（含 `filename*=` UTF-8），用 Blob + 锚点触发浏览器下载。
- 失败时后端以 `application/json` 返回统一响应体，前端解出 `msg` 抛 `ApiError`；`!res.ok` 路径会先尝试读 JSON `msg` 兜底。错误码 EXP_001/EXP_002 场景统一以 `msg` 呈现。

### 3.5 报表区（ReportSection + reportCharts.jsx）

- 控件：维度 Select（person_type / person_level / department，默认 person_type）、时间范围 RangePicker、groupBy Select（day/hour，默认 day）、可选 apiName 过滤、可选「维度值过滤」Select。
- 维度可选值由 `GET /api/demo/tracking/dimensions` 填充（personTypes / personLevels / departments），失败静默不阻塞主流程。
- 「查询报表」调 `GET /api/demo/tracking/report`（拼接 dimension/groupBy/start/end/apiName），成功取 `data.items`。
- 三图对应设计与 brief 要求：
  - 折线图：各 key 一条 series，X 轴为全部时间桶（有序去重），count 随 bucket 趋势。
  - 饼图：按所选维度各 key 的 series count 合计求占比。
  - 柱状图：同一时间桶内各 key 分组对比。
- `items` 为空或 undefined 时三图渲染 AntD `Empty` 空态。

## 4. 与后端契约对齐说明

| 契约项 | 前端实现 | 依据 |
|--------|----------|------|
| 统一前缀 `/api/demo` | `api.js` 中 `API_BASE = '/api/demo'`，六个接口均拼此前缀 | design 4.1 / plan.md |
| 统一响应 `{code,msg,data}` | `requestJson` 校验 `body.code === '0'` 返回 `body.data`，否则抛 `ApiError(body.msg)` | plan.md |
| 出错展示 msg | 各 Tab/报表 catch 后以 `Alert`/`message` 展示 `e.message`（即后端 msg） | brief 第 5 条约束 |
| 请求头 X-Caller-Id / X-Person-Type / X-Person-Level / X-Dept | `buildHeaders` 统一注入，顶部「调用人设置」可改，缺省 anonymous/unknown | design 5.2 / plan.md |
| W01 GET /helloworld | `api.helloworld(name)`，`name` 为空不拼参数 | design 5.1.1 |
| W02 POST /hash | `api.hash(text)` body {text} | design 5.1.1 |
| W03 POST /bubble-sort | `api.bubbleSort(array)` body {array} | design 5.1.1 |
| W04 POST /export | `api.exportCsv`，tab 枚举 helloworld/hash/bubbleSort/report | design 5.4 |
| W05 GET /tracking/report | `api.report`，参数 dimension/groupBy/start/end/apiName | design 5.3.1 |
| W06 GET /tracking/dimensions | `api.dimensions`，读 data.personTypes/personLevels/departments | design 5.3.1 |
| 报表维度取值 | 采用 `person_type / person_level / department`（与 design 5.3 及 plan.md 全局约束一致；spec 第 3 节曾写 personType|level|department，已按 plan 收敛） | plan.md 第 14 行 |
| tab=report 导出 | body 含 `tab:"report"` + `dimension`，前端强制带 dimension | design 5.4 / plan.md EXP_002 |

## 5. 静态校验结果

- `python3 -m json.tool frontend/package.json`：通过（JSON 合法）。
- 五个 .jsx/.js 源文件括号配平检查（{}、()、[]）：全部 True。
- `find` 确认 8 个交付文件均在 `frontend/` 下。
- 未运行 `npm install`/`npm run`/任何 node 命令（沙箱 `command -v node npm` 为空，符合约束）。

未做（受制于约束/沙箱）：无法做真实构建、ESLint、单元测试或浏览器渲染验证，仅人工静态评审。

## 6. 疑虑与待跟进

1. 后端当前未实现（仓库无 `src/`），前端按文档契约独立编码，运行时联调需后端 Task 完成后进行。
2. 导出接口的失败态假设：成功为 `text/csv` 文件流、失败为 `application/json` 统一响应体，`!res.ok` 时再兜底读 JSON msg；若失败以其它 Content-Type 返回纯文本，前端将退化为 HTTP status 提示，仍可感知但不显示业务 msg。
3. 时间范围参数：前端 RangePicker 默认 `start/end` 字符串 `yyyy-MM-dd`；每小时粒度下后端若需含时间，需与后端确认（设计文档已写 start/end 为 `yyyy-MM-dd`，暂按此）。
4. W05 的 `apiName` 参数源自 design 5.3.1（spec 未列，plan 未展开），前端作为可选参数附带；若后端未实现该过滤参数，一般会被忽略，无副作用，但需后端确认。
5. 冒泡排序参数校验由前端做一次（友好提示），后端仍应校验（ALG_003/ALG_004），双保险不冲突。
