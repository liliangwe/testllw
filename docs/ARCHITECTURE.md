# 架构文档

> 项目未配置 `SSOT.md`，架构信息以本文为准。文档来源：系分方案 `.agents/20261013-目标_帮助用户记录日常待办事项_核心功能/design.md`。

## 1. 分层与依赖

应用遵循 MVC 分层，调用关系单向：

```
Web 层（api） → Service 层（service） → DAO 层（dao）
                      ↓
                  公共层（common：常量 / 枚举 / 异常 / 上下文 / 配置 / 埋点）
```

- 上层可依赖下层，下层禁止反向依赖上层；禁止模块间循环依赖。
- 公共工具类、常量类、异常类统一放在 `common` 包下。
- 请求/响应对象放在 `api/request`、`api/response`，与 oneapi 契约一一对应。

## 2. 模块清单

| 模块 | 职责 | 关键包 | 依赖 |
|------|------|--------|------|
| todo（待办事项模块） | 待办事项创建受理、入参校验、创建人与租户归属、持久化、埋点留痕 | `com.antdigital.todo` | MySQL/OB（JDBC，强依赖）；平台登录态与租户上下文（进程内） |

## 3. 技术栈

| 维度 | 选型 |
|------|------|
| 语言/运行时 | Java 21 |
| 框架 | Spring Boot 3.2.x（Web / Validation / JDBC） |
| 数据存储 | MySQL（兼容 OB）单表 `todo_task`，无缓存、无消息队列 |
| 构建 | Maven（`pom.xml`） |
| 测试 | JUnit 5 + Mockito + AssertJ + Spring Test（MockMvc） |

## 4. 全局约束

- 接口响应结构统一为 oneapi `{result, msg, data}`，字段名 lowerCamelCase。
- 错误码集中定义于 `TodoErrorCodeEnum`（TODO_001 ~ TODO_005），禁止在业务代码中散落硬编码。
- 登录态与租户标识只能通过 `InvokerContext` 从平台登录态获取，禁止由前端入参传入；
  平台登录拦截器写入的请求属性由 `TodoInvokerContextInterceptor` 接线，请求结束 `afterCompletion` 清理，
  身份缺失时 fail-closed（`TODO_002`/`TODO_003`）。
- 创建入口受应急开关 `todo.task.create.enabled` 控制，支持按租户尾号灰度；
  开关取值**每次调用实时读取**（不启动期固化），配置推送后免重启/发布生效，默认关闭。
- 数据库凭证不落明文，由环境变量注入；连接与语句超时均 ≤ 2s；应用时钟时区显式指定为 `Asia/Shanghai`。
- 日志遵守：占位符输出、不打印事项描述全文、禁止 `System.out` / `printStackTrace`。
