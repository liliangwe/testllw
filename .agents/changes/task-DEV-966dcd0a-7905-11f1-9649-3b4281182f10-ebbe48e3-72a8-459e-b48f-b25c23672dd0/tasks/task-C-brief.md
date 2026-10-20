# Task C：前端页面（对应 plan Task 5）

在 `frontend/` 下新建一个 **零构建依赖可独立预览** 的 React 页面工程（Vite + React + Ant Design + ECharts），实现单页 `/demo`：三 Tab 展示 + 导出按钮 + 调用报表（折线/饼图/柱状图）。

## 硬性前置

- 前端仅写文件，**不要运行 npm install / npm run**（沙箱无 node；本轮评审只做静态审查，构建可用性不阻塞）。
- Git 只读；产物路径基于仓库根 `frontend/`。

## 上下文

读取 `.agents/20261020-分别写三个接口helloworld_哈希/design.md` 5.5 节（前端页面模块）与 4.1 接口总览（W01~W06），以及 `plan.md` 全局约束（前缀 /api/demo、请求头、响应 {code,msg,data}）。

## 要求实现

### 工程文件（frontend/）
- `package.json`：依赖 react、react-dom、antd、echarts、echarts-for-react、vite、@vitejs/plugin-react；scripts `dev`（vite）、`build`、`preview`。
- `vite.config.js`：dev 代理 `/api` → `http://localhost:8080`。
- `index.html`、`src/main.jsx`、`src/App.jsx`、`src/api.js`、`src/DemoPage.jsx`、`src/reportCharts.jsx`。

### 功能
1. 路由：单页应用，`/demo` 路由渲染 DemoPage（用 react-router-dom 或直接默认渲染 /demo；最简可在 App 用 window.location.pathname 判断，推荐 react-router-dom）。
2. 三 Tab（AntD Tabs）：HelloWorld / 哈希 / 冒泡排序。
   - HelloWorld：输入可选 name，点执行 → GET `/api/demo/helloworld`，展示 message。
   - 哈希：输入 text → POST `/api/demo/hash`，展示 algorithm + digest。
   - 冒泡排序：输入逗号分隔数字 → POST `/api/demo/bubble-sort`，展示 sorted + swaps。
   - 所有请求带请求头 `X-Caller-Id`（默认 anonymous）、`X-Person-Type`、`X-Person-Level`、`X-Dept`（可做成页面顶部"调用人设置"填写框，默认 anonymous/unknown）。
3. 导出按钮：点击调用 POST `/api/demo/export`（body 含 `tab` 与当前 Tab 的 payload），后端返回 CSV 文件流，前端触发浏览器下载；报表导出则 tab=report + dimension。
4. 报表区（同页下方）：
   - 维度切换：人员类型 / 人员层级 / 人员部门（select），可选时间范围与 groupBy（day/hour）。
   - GET `/api/demo/tracking/report` 拉聚合数据；GET `/api/demo/tracking/dimensions` 拉维度可选值填充下拉。
   - ECharts 三图：
     - 折线图：各 key 的 series（count 随 bucket 时间趋势）。
     - 饼图：按所选维度各 key 的 count 合计占比。
     - 柱状图：bucket 内各 key 分组对比。
   - 空数据显示空态。

### 注意
- 代码要与后端接口契约一致：响应取 `response.data`（因为 {code,msg,data}）。错误码展示 msg。
- 图表封装可放 `reportCharts.jsx`；保持组件单一职责、不写死魔法串中多余逻辑。

## 交付
写报告到 `tasks/task-C-report.md`：文件清单、各功能如何实现、如何与后端契约对齐、疑虑。回传状态 DONE / DONE_WITH_CONCERNS / BLOCKED（≤15 行）。