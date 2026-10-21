# HelloWord 图片生成 编码报告

> **元信息**

| 项目 | 内容 |
|------|------|
| 文档版本 | v1.0 |
| 作者 | AiWork |
| 创建日期 | 2026-10-21 |
| 需求来源 | 任务需求「生产一个 helloword 图片」 |
| 系分方案 | .agents/20261021-生产一个helloword图片/design.md |
| 采用规范 | dtazziboot-java-coding-standards |

## 模块进度追踪

| 序号 | 模块 | READ | TEST | IMPL | CHECK | DOCS | 状态 |
|:----:|------|:----:|:----:|:----:|:-----:|:----:|------|
| 1 | image-gen（图片生成） | ✅ | ✅ | ✅ | ✅ | ✅ | 已完成 |

## 一、README（读取上下文）

- 仓库现状：`testllw` 为单体 Java 项目，仅含 `src/main/java/com/example/HelloWorld.java`（控制台输出 Hello World），无 `pom.xml`/`SSOT.md`/测试基建。
- 模块职责：使用 JDK 内置 `java.awt` + `ImageIO` 将文本渲染为 PNG 图片并落盘。
- 关键类：`ImageGenRequest`（入参）、`ImageGenErrorCodeEnum`（错误码）、`ImageGenException`（业务异常）、`ImageGenerator`（生成器）。
- 约束遵循：不改动既有 `HelloWorld.java`；无第三方依赖；无数据库/网络。
- 已加载规范：naming.md / exception-logging.md / formatting.md / constants.md / comments.md / security.md / unit-testing.md / project-structure.md。

## 二、TEST（生成单测）

测试文件：`src/test/java/com/example/ImageGeneratorTest.java`

| 方法 | 测试场景 | 状态 |
|------|----------|:----:|
| should_generatePng_when_validRequest | 正常路径，生成 PNG 且尺寸正确 | ✅ |
| should_applyDefaultValues_when_fieldsAreBlank | 空字段填充默认值（800×400 白底） | ✅ |
| should_writeDefaultFile_when_generateDefault | 无参入口，默认路径落盘 | ✅ |
| should_throwInvalidParameter_when_widthIsZero | 宽度为 0，抛 IMG_003 | ✅ |
| should_throwInvalidParameter_when_heightIsNegative | 高度为负，抛 IMG_003 | ✅ |
| should_throwInvalidParameter_when_colorIsInvalid | 非法颜色串，抛 IMG_003 | ✅ |

覆盖摘要：
- 被测类：`ImageGenerator`
- 测试方法数：6
- 覆盖场景：正常路径 ✓、默认值填充 ✓、无参入口 ✓、参数校验 ✓、异常处理 ✓

## 三、IMPL（实现代码）

已实现文件：

- `src/main/java/com/example/ImageGenRequest.java` — 入参载体（text/width/height/backgroundColor/textColor/outputPath）
- `src/main/java/com/example/ImageGenErrorCodeEnum.java` — 错误码枚举（IMG_001/IMG_002/IMG_003）
- `src/main/java/com/example/ImageGenException.java` — 业务异常，携带错误码
- `src/main/java/com/example/ImageGenerator.java` — 生成器（normalize → validate → render → write）
- `src/test/java/com/example/ImageGeneratorTest.java` — 单元测试

关键实现说明：
- `generate(ImageGenRequest)`：缺省字段自动填充；宽高 ≤ 0 抛 IMG_003；`render` 使用 `Graphics2D` 白底/黑字居中渲染；`write` 先写 `.tmp` 再原子移动，失败即删临时文件，避免半成品（对应 R03 / IMG_002）。
- 输出目录不存在时自动创建，创建失败抛 IMG_001。
- 无共享可变状态、无 System.out、无魔法值（默认值均为类内常量）。

## 四、CHECK（规范检查）

### L1 静态检查

| 检查项 | 规范要求 | 符合情况 |
|--------|----------|:--------:|
| 命名规范 | 类名大驼峰、方法/变量小驼峰、常量全大写、枚举 Enum 后缀 | ✅ |
| 异常日志 | 有业务含义自定义异常 + 错误码；无 e.printStackTrace/System.out | ✅ |
| 安全规范 | 无 SQL/网络输入面；输出路径本地可控 | ✅ |
| 常量规约 | 默认值抽为类内常量，无魔法值，Long 用大写 L | ✅ |
| 格式规约 | 4 空格缩进、运算符空格、注释 `//` 后单空格、单行 ≤120 | ✅ |
| 注释规约 | 类/方法 Javadoc，含 @author/@date | ✅ |
| 单元测试 | 测试类存在、AAA 三段式、断言具体值、方法命名 should_* | ✅ |
| 工程结构 | 接口与实现分离（此处为工具类，无接口需要）、包名小写 | ✅ |

### L2 动态验证

| 验证项 | 状态 | 说明 |
|--------|:----:|------|
| 编译验证 | ⚠️ | 环境无 JDK（`java`/`javac`/`mvn` 均不存在），无法编译 |
| 单测验证 | ⚠️ | 同上，无法执行 |

#### 📋 待人工验证

以下命令请在具备 JDK 17+ 与 Maven 的本地环境执行：

```bash
mvn compile -DskipTests
mvn test -Dtest=ImageGeneratorTest
```

纯 JDK 无 Maven 时可直接验证：

```bash
javac -d out $(find src/main/java -name '*.java')
java -cp out -Djava.awt.headless=true com.example.ImageGenerator
```

**发现问题**：无。

## 五、DOCS（文档产出）

- 编码报告：已写入 `.agents/20261021-生产一个helloword图片/impl.md`（本文档）。
- 架构/模块文档：项目无 `SSOT.md`、`docs/` 约定，属极简单体工具，未另建硬编码文档目录。

## 变更清单

| 文件 | 操作 |
|------|------|
| src/main/java/com/example/ImageGenRequest.java | 新增 |
| src/main/java/com/example/ImageGenErrorCodeEnum.java | 新增 |
| src/main/java/com/example/ImageGenException.java | 新增 |
| src/main/java/com/example/ImageGenerator.java | 新增 |
| src/test/java/com/example/ImageGeneratorTest.java | 新增 |
| .agents/20261021-生产一个helloword图片/impl.md | 新增 |