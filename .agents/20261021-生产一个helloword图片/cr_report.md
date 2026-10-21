# Code Review Report

> **Change** 生产一个helloword图片 · **分支/Commit** AI/task-DEV-c63955ca-a802-11f1-a0cc-b5858e839527-fd491201-9df9-4269-8cb4-a9dd0a7abb0e / 645f8b0 · **日期** 2026-10-21 · **审查者** AI

---

## 1. 审查范围

| 项 | 值 |
|----|-----|
| `.java` 文件数 | 5 |
| 变更行数 | +490 / -0（新增 5 文件，见编码报告 §变更清单） |

| 类/接口 | 路径 | 角色 |
|---------|------|------|
| ImageGenRequest | src/main/java/com/example/ImageGenRequest.java | 入参载体（F04） |
| ImageGenErrorCodeEnum | src/main/java/com/example/ImageGenErrorCodeEnum.java | 错误码枚举 |
| ImageGenException | src/main/java/com/example/ImageGenException.java | 业务异常 |
| ImageGenerator | src/main/java/com/example/ImageGenerator.java | 生成器（F01-F03） |
| ImageGeneratorTest | src/test/java/com/example/ImageGeneratorTest.java | 单元测试 |

> 说明：变更文件已提交入库（commit 645f8b0），`git diff --stat` 为空，审查范围以任输入清单 + 编码报告变更清单为准。

---

## 2. 问题计数

| P0 | P1 | P2 |
|----|----|-----|
| 0 | 1 | 3 |

---

## 3. Step 2 — 功能（REQ）

| REQ | Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|-----|----------|------|----------|----------|------|
| F01 生成 Hello World PNG | 调用 generate 产出 PNG | ✅ | design F01「生成 "Hello World" 文本图片，输出 PNG」 | ImageGenerator.java:31/49/160 | 默认文字 Hello World，`ImageIO.write(...,"png",...)` |
| F02 白底黑字居中渲染 | 背景与文字渲染 | ✅ | design F02「白底黑字居中渲染」 | ImageGenerator.java:118-135 | fillRect + setColor + 居中坐标计算 |
| F03 图片输出到指定路径 | 落盘为文件 | ✅ | design F03「图片输出到指定路径」 | ImageGenerator.java:146-171 | write() 落盘 |
| F04 参数可配置 | 文字/尺寸/颜色/路径 | ✅ | design F04「提供入参，默认可省略」 | ImageGenRequest.java:14-30 | 6 字段均可空，normalize 兜底 |
| 错误码 IMG_001/002/003 | 对应异常抛出 | ✅ | design 5.1.2 错误码表 | ImageGenErrorCodeEnum.java:14-21 | 三枚举与设计一致 |
| S01 generate(request) | 带参生成 | ✅ | design 5.1.2 S01 | ImageGenerator.java:70 | normalize→validate→render→write |
| S02 generateDefault() | 无参默认 | ✅ | design 5.1.2 S02 | ImageGenerator.java:61-63 | 等价 generate(null) |
| R01 宽高为正 | 宽高≤0 抛 IMG_003 | ✅ | design R01 | ImageGenerator.java:91-98 | validate 校验 |
| R02 目录不存在自动创建 | 创建失败抛 IMG_001 | ✅ | design R02 | ImageGenerator.java:149-156 | Files.createDirectories |
| R03 编码成功/不产半成品 | 临时文件+原子移动 | ⚠️ | design R03「ImageIO 编码/IO 失败…清理临时文件」 | ImageGenerator.java:158-170 | IO 异常路径清理 tmp；`!written` 分支未清理（见 §7.1 P2） |

**Step 2 小结**：F01-F04、S01/S02、IMG_001/002/003、R01/R02 全部满足设计；R03 存在一处 `!written` 分支遗留半成品的轻微缺陷（非功能阻断）。

---

## 4. Step 3 — 可读性检查

| ID | 结果 | 说明 |
|----|------|------|
| A1 源文件格式 | ✅ | 文件名=顶层类名、UTF-8、无 Tab（脚本 A1.3 + `grep` Tab 双验证无命中） |
| A2 文件结构/import | ✅ | 无 `import *`；静态 import 分组正确 |
| A3 代码样式 | ⚠️ | **A3.4** 行宽超 120：ImageGenerator.java:96（127 字符） |
| A4 命名规范 | ✅ | 类/方法/常量/测试类命名合规 |
| A5 编码实践 | ✅ | @Override 齐全；无空 catch；无 finalize 重写 |
| A6 特定元素样式 | ✅ | `1L` 大写；修饰符顺序正确；无 switch |
| A7 Javadoc | ✅ | 公有类/方法有 Javadoc，简单 getter 豁免 |

---

## 5. Step 4 — 可靠性检查

> 预扫：已运行 `scan-all-rules.sh`，原始输出 3 条 `[P0] G16.2 CatchWithoutLogging`（ImageGenerator.java:140/152/166）。经人工复核**三处均为误报**：三个 catch 均「捕获后 `throw new ImageGenException(..., cause)`」重新抛出，未吞异常；且 `reliability-checklist.md` 中 G16.2 行内等级为 **P1**（脚本硬编码为 P0 与清单定义不符），故不计入 P0。

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|------|
| 可靠性 | reliability-checklist.md G1-G17 | ⚠️ | P1/P2 | G8.3 ✅(Graphics2D finally dispose)；G11.1 ✅；G11.2 ⚠️P2(边界)；G16.2 ⚠️P1(无日志埋点)；其余 N/A(本地无状态工具，无并发/事务/MQ/缓存/调度/网络/资金) |
| 安全 | security-checklist.md S1-S10 | ✅/N/A | — | S4.2 ✅(文件操作用 Java API，不调用外部命令)；其余 N/A(无 Web/SQL/反序列化/密钥) |
| Bug 模式 | bug-pattern-checklist.md B/M/I | ✅/N/A | — | 脚本 52 规则 + LLM 复核 120 规则，无真实命中（无 SimpleDateFormat/ThreadLocal/空 catch/printStackTrace/Executors 等） |

---

## 6. Step 5 — 自定义扩展检查

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|------|
| 自定义扩展 | customized-checklist.md U* | N/A | — | N/A(未启用自定义规则)：customized-checklist.md 仅含示例项，项目未配置私有规则 |

---

## 7. 结论

- **合并建议**：通过
- **P0**：无
- **P1**：G16.2 — 异常路径仅 rethrow，无日志埋点（可观测性不足）
- **P2**：

  1. G11.2 — 宽高无上界校验（极大值→OOM/IllegalArgumentException）、文字长于宽度时居中坐标 x 为负（文字越界）
  2. A3.4 — ImageGenerator.java:96 行宽 127 字符（>120）
  3. G11.2/R03 — `ImageIO.write` 返回 false（无编码器）分支未清理 `.tmp` 临时文件

- **一句话**：单模块纯 JDK 本地工具，功能与设计对齐，代码规范、无 P0 阻塞，可在顺手修正 3 处 P2 与 1 处 P1 后合并。

---

## 7.1 问题片段（必填）

- **P1** `G16.2` `src/main/java/com/example/ImageGenerator.java:166` — 异常路径捕获后仅 rethrow，未记录日志，排障可观测性不足；同类问题在 :140、:152。  
  片段范围：`src/main/java/com/example/ImageGenerator.java:158-170`

```java
L158|        File tmpFile = new File(target + ".tmp");
L159|        try {
L160|            boolean written = ImageIO.write(image, OUTPUT_FORMAT, tmpFile);
L161|            if (!written) {
L162|                throw new ImageGenException(
L163|                    ImageGenErrorCodeEnum.ENCODE_WRITE_FAILED, "无可用编码器写入图片: " + outputPath);
L164|            }
L165|            Files.move(tmpFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
L166|        } catch (IOException e) {
L167|            deleteQuietly(tmpFile);
L168|            throw new ImageGenException(
L169|                ImageGenErrorCodeEnum.ENCODE_WRITE_FAILED, "图片编码或写盘失败: " + outputPath, e);
L170|        }
```

- **P2** `G11.2` `src/main/java/com/example/ImageGenerator.java:94` — 宽高仅校验 `<= 0`，未设上界，超大值可能触发 OOM/`IllegalArgumentException`。  
  片段范围：`src/main/java/com/example/ImageGenerator.java:91-98`

```java
L91|    private void validate(ImageGenRequest request) {
L92|        Integer width = request.getWidth();
L93|        Integer height = request.getHeight();
L94|        if (width == null || height == null || width <= 0 || height <= 0) {
L95|            throw new ImageGenException(
L96|                ImageGenErrorCodeEnum.INVALID_PARAMETER, "宽、高必须为正整数, width=" + width + ", height=" + height);
L97|        }
L98|    }
```

- **P2** `A3.4` `src/main/java/com/example/ImageGenerator.java:96` — 行宽 127 字符，超出 120 上限。

- **P2** `G11.2` `src/main/java/com/example/ImageGenerator.java:129-130` — 文字宽于图片时 `x=(width-textWidth)/2` 为负，文字越出画布（无截断/缩放策略）。  
  片段范围：`src/main/java/com/example/ImageGenerator.java:123-135`

```java
L123|    private void drawCenteredText(Graphics2D graphics, Color color, String text, int width, int height) {
L124|        Font font = new Font(FONT_FAMILY, Font.PLAIN, DEFAULT_FONT_SIZE);
L125|        FontMetrics metrics = graphics.getFontMetrics(font);
L126|        int textWidth = metrics.stringWidth(text);
L127|        int textHeight = metrics.getHeight();
L128|
L129|        int x = (width - textWidth) / 2;
L130|        int y = (height - textHeight) / 2 + metrics.getAscent();
L131|
L132|        graphics.setFont(font);
L133|        graphics.setColor(color);
L134|        graphics.drawString(text, x, y);
L135|    }
```

- **P2** `G11.2/R03` `src/main/java/com/example/ImageGenerator.java:161-163` — `!written` 分支直接 throw，未执行 `deleteQuietly(tmpFile)`，遗留半成品文件（对应 design R03 异常场景「清理已创建的临时文件」）。

---

## 8. 修复任务列表

### P0

- 无待修复项。

### P1

- [ ] **P1** `src/main/java/com/example/ImageGenerator.java:140,152,166` — 在异常抛出路径（parseColor 非法颜色、目录创建失败、编码/写盘失败）增加 error 级别日志埋点，携带 code + outputPath 等可追溯上下文。

### P2（可选）

- [ ] **P2** `src/main/java/com/example/ImageGenerator.java:94` — 为 width/height 增加上界校验（如 0 < w,h ≤ 8192），超界抛 IMG_003。
- [ ] **P2** `src/main/java/com/example/ImageGenerator.java:129` — 文字宽于画布时采用缩字号/截断策略，避免 x 坐标为负导致文字越出画布。
- [ ] **P2** `src/main/java/com/example/ImageGenerator.java:96` — 超长行按规范断行，控制单行 ≤120 字符。
- [ ] **P2** `src/main/java/com/example/ImageGenerator.java:161-163` — `!written` 分支在 throw 前调用 `deleteQuietly(tmpFile)` 清理半成品。