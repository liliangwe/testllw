# 编码实现计划（subagent-driven-development）

> 关联任务：DEV-966dcd0a-7905-11f1-9649-3b4281182f10-ebbe48e3-72a8-459e-b48f-b25c23672dd0
> 输入基线：`.agents/20261020-分别写三个接口helloworld_哈希/spec.md` + `design.md`

## 全局约束（来自 spec/design，需逐字遵守）

- 后端：Java 17 + Spring Boot 3.x + Maven，统一响应 `{ code, msg, data }`，code "0" 表示成功。
- 统一前缀 `/api/demo`，6 个接口：W01 GET /helloworld、W02 POST /hash、W03 POST /bubble-sort、W04 POST /export、W05 GET /tracking/report、W06 GET /tracking/dimensions。
- 哈希算法：SHA-256，返回 `{algorithm:"SHA-256", digest:小写hex}`，标准向量 abc → ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad。
- 冒泡排序：升序 + 交换次数 swaps；array 长度 1~1000；[5,3,8,1] → sorted [1,3,5,8], swaps 4。
- 埋点：算法接口成功返回后异步落库，失败仅记错误日志不回抛；调用人/维度来自请求头 X-Caller-Id / X-Person-Type / X-Person-Level / X-Dept，缺省 anonymous / unknown。
- 埋点表 demo_api_tracking：id/api_name/caller_id/person_type/person_level/department/call_time/gmt_create/gmt_modified。
- 报表维度：person_type / person_level / department；时间粒度 day（默认）/hour；错误码 RPT_001(维度非法)、RPT_002(start>end)。
- 导出：CSV（text/csv, attachment）；tab ∈ helloworld/hash/bubbleSort/report；错误码 EXP_001(tab非法)、EXP_002(report缺 dimension)。
- 前端：React + Ant Design + ECharts 单页路由 /demo，三 Tab + 导出按钮 + 折线图/饼图/柱状图报表区。
- Git 只读：不 commit/push，代码产物直接写入工作区。

## 任务拆分

- Task 1｜工程骨架：pom.xml、DemoApplication、统一响应/异常、H2 schema、CORS、异步线程池。
- Task 2｜算法模块：AlgorithmService + W01/W02/W03 + DTO + 单元测试。
- Task 3｜埋点模块：@DemoApiTracking + TrackingAspect + TrackingService + TrackingMapper（表 DDL）。
- Task 4｜报表与导出：ReportService + ExportService + W04/W05/W06。
- Task 5｜前端页面：React + AntD + ECharts，三 Tab / 导出 / 报表三图。
- Task 6｜集成验证：`mvn -q -DskipTests=false test`，README 与 MySQL DDL 说明。

## 目录约定

- 后端位于仓库根（pom.xml 在根，便于 test_command 直接运行）：`src/main/java/com/example/demo/**`。
- 前端位于 `frontend/`。
- MySQL 部署 DDL：`docs/mysql_ddl.sql`。
