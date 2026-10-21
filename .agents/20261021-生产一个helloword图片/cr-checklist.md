# Code Review Checklist

> **Change** 生产一个helloword图片 · **分支/Commit** AI/task-DEV-c63955ca-... / 645f8b0 · **日期** 2026-10-21
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。

## Step 1 — 执行队列（产物 A）

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G | S | 总状态 |
|---|----------------------|----------|-------|-------|---|--|--------|
| 1 | src/main/java/com/example/ImageGenRequest.java | F04 / design 5.1.2 入参载体 | ✅ | ✅ | ✅(无并发/I/O) | N/A | ✅ 已审 |
| 2 | src/main/java/com/example/ImageGenErrorCodeEnum.java | design 5.1.2 错误码 | ✅ | ✅ | ✅(纯枚举) | N/A | ✅ 已审 |
| 3 | src/main/java/com/example/ImageGenException.java | 业务异常规范 | ✅ | ✅ | ✅(纯异常类) | N/A | ✅ 已审 |
| 4 | src/main/java/com/example/ImageGenerator.java | F01-F03 实现 | ✅ | ⚠️ | ⚠️(G16.2/G11.2) | N/A | ⚠️ 已审有问题 |
| 5 | src/test/java/com/example/ImageGeneratorTest.java | 单测 | ✅ | ✅ | ✅(G11.1 已覆盖) | N/A | ✅ 已审 |

## Step 2 — 功能（产物 B）

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据 |
|-----|----------|----------------------|----------|------|----------|
| F01 生成 Hello World PNG | 调用 generate 产出 PNG | design F01「生成 "Hello World" 文本图片，输出 PNG」 | ImageGenerator.java | ✅ | `DEFAULT_TEXT`/`OUTPUT_FORMAT`，ImageIO.write:160 |
| F02 白底黑字居中渲染 | 背景与文字渲染 | design F02「白底黑字居中渲染」 | ImageGenerator.java | ✅ | drawBackground:118 / drawCenteredText:123 |
| F03 输出到指定路径 | 落盘为文件 | design F03「图片输出到指定路径」 | ImageGenerator.java | ✅ | write():146 |
| F04 参数可配置 | 文字/尺寸/颜色/路径 | design F04「提供入参，默认可省略」 | ImageGenRequest.java | ✅ | 6 字段 + normalize:78 |
| 错误码 IMG_001/002/003 | 对应异常抛出 | design 5.1.2 错误码表 | ImageGenErrorCodeEnum.java | ✅ | enum:14-21 |
| S01 generate(request) | 带参生成 | design 5.1.2 S01 | ImageGenerator.java | ✅ | generate:70 |
| S02 generateDefault() | 无参默认 | design 5.1.2 S02 | ImageGenerator.java | ✅ | generateDefault:61 |
| R01 宽高为正 | 宽高≤0 抛 IMG_003 | design R01 | ImageGenerator.java | ✅ | validate:91-98 |
| R02 目录不存在自动创建 | 创建失败抛 IMG_001 | design R02 | ImageGenerator.java | ✅ | Files.createDirectories:151 |
| R03 编码成功/不产半成品 | 临时文件+原子移动 | design R03 | ImageGenerator.java | ⚠️ | `.tmp`+move:158-165（`!written` 分支见 P2） |

## Step 3 — 可读性检查（产物 C）

| ID | 检查项 | 状态 | 备注 |
|----|--------|------|------|
| A1 | 源文件格式 | ✅ | 文件名=类名；UTF-8；无 Tab（已验证） |
| A2 | 源文件结构/import 顺序 | ✅ | 无通配 import；静态/非静态分组 |
| A3 | 代码样式 | ⚠️ | A3.4：ImageGenerator.java:96 行宽 127 字符（>120） |
| A4 | 命名规范 | ✅ | 类/方法/常量/测试类命名合规 |
| A5 | 编码实践 | ✅ | @Override 已加；无空 catch；静态无实例调用 |
| A6 | 特定元素样式 | ✅ | 修饰符顺序、`1L`、无 switch |
| A7 | Javadoc 规范 | ✅ | 公有类/方法有 Javadoc，getter 豁免 |

## Step 4 — 可靠性检查（产物 D）

### 4.1 Bug 模式（预扫：scan-all-rules.sh + LLM 复核）

- 脚本 B/M/I 全部 **未命中**（无 SimpleDateFormat、无 BigDecimal(double)、无 ThreadLocal、无 printStackTrace、无空 catch、无 `new Date()`、无 Executors、无 Arrays.asList 装箱等）。
- LLM 复核 B001–B081/M001–M027/I001–I010：与本次变更相关者均已核销为 ✅/N/A，无真实命中。

### 4.2 可靠性（G）

| ID | 状态 | 备注 |
|----|------|------|
| G8.3 资源释放 | ✅ | Graphics2D 在 finally dispose（ImageGenerator.java:112-114） |
| G11.1 单测存在 | ✅ | ImageGeneratorTest 6 方法，含断言 |
| G11.2 边界覆盖 | ⚠️ P2 | 宽高仅校验 ≤0，无上界（极大值→OOM/IllegalArgument）；文字长于宽度时 x 为负（ImageGenerator.java:94,129） |
| G11.3 入参空值 | ✅ | request 为 null/空字段已 normalize 兜底 |
| G16.2 异常可观测 | ⚠️ P1 | 脚本报 3 处(140/152/166)均为**误报**（均 rethrow with cause），但整体无日志埋点，可观测性不足 |
| G1-G6,G7,G9,G10,G12-G15,G17 | N/A | 无并发/事务/MQ/缓存/SQL/调度/网络调用/资金/国际化/灰度（纯本地工具） |

### 4.3 安全（S）

| 域 | 状态 | 备注 |
|----|------|------|
| S4.2 文件操作用 Java API | ✅ | 使用 Files/ImageIO，不调用外部命令 |
| S1-S3,S5-S10 | N/A | 无 SQL/Web/XSS/SSRF/XXE/反序列化/上传/鉴权/密钥（本地工具） |

## Step 5 — 自定义扩展检查（产物 E）

| ID | 状态 | 备注 |
|----|------|------|
| U1.1/U2.x | N/A | `customized-checklist.md` 仅示例项，未启用自定义规则 |

## 终检（防漏检）

- [x] 执行队列 5 文件 Step2/Step3/G/S 均非 `⬜`
- [x] Step 2 全部 REQ 非 `⬜`
- [x] Step 3 A1–A7 非 `⬜`
- [x] Step 4 G/S 与 B/M/I 全部核销
- [x] Step 5 U* 标记 N/A(未启用自定义规则)
- [x] 所有 ❌/⚠️ 已写入报告（含 ID + path:line）