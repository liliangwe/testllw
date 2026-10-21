# 问题修复报告

> **Change** 生产一个helloword图片 · **分支** AI/task-DEV-c63955ca-a802-11f1-a0cc-b5858e839527-fd491201-9df9-4269-8cb4-a9dd0a7abb0e · **日期** 2026-10-21 · **阶段** 问题修复

## 一、修复来源

依据 `cr_report.md` §8 修复任务列表，修复 `ImageGenerator.java` 中的 1 个 P1 与 4 个 P2 问题。

## 二、修复清单

| 级别 | 位置 | 问题 | 修复方案 | 状态 |
|------|------|------|----------|:----:|
| P1 | parseColor / write | 异常路径仅 rethrow，无日志埋点，可观测性不足 | 3 处异常抛出路径增加 `java.util.logging` SEVERE 级日志，携带错误码 + 颜色值/输出路径 + 异常堆栈 | ✅ |
| P2 | validate | 宽高无上界校验，极大值→OOM/IllegalArgumentException | 增加 `MAX_DIMENSION=8192` 上界校验，超界抛 IMG_003 | ✅ |
| P2 | drawCenteredText | 文字宽于画布时 x 坐标为负，文字越界 | 新增 `fitFont()` 逐级缩字号（下限 8）+ `Math.max(0, x)` clamp | ✅ |
| P2 | validate | 行宽 127 字符（>120） | 断行，全文件已核验 ≤120 字符 | ✅ |
| P2 | write | `!written` 分支未清理 `.tmp` 临时文件 | throw 前调用 `deleteQuietly(tmpFile)` | ✅ |

## 三、约束说明

- **日志框架选型**：design.md 明确「纯 JDK 零第三方依赖」，故日志采用 JDK 内置 `java.util.logging`（Logger + Level + MessageFormat 占位符），不引入 SLF4J，避免违反零依赖约束。
- **修改范围**：仅修改 `src/main/java/com/example/ImageGenerator.java`，未改动测试类、错误码枚举、入参载体、异常类及 `HelloWorld.java`。

## 四、版本对比

```
 src/main/java/com/example/ImageGenerator.java | 47 +++++++++++++++++++++++++----
 1 file changed, 43 insertions(+), 4 deletions(-)
```

## 五、验证结果（L2 降级说明）

[降级说明] 环境无 JDK（`java`/`javac`/`mvn` 均不存在，`command -v java` 返回 command not found），无法执行编译与单测，切换为「静态代码审查」：

| 审查项 | 结果 |
|--------|:----:|
| 行宽 ≤120 字符 | ✅（awk 扫描全文件无命中） |
| import 均被使用且无通配 import | ✅（MessageFormat/Level/Logger 各有引用） |
| 类型一致性（Integer 与 int 上界比较、null 短路保护） | ✅ |
| `MessageFormat.format` 格式串无单引号转义陷阱 | ✅ |
| fitFont 循环终止性（步长 2，下限 8，不会无限循环/降破下限） | ✅ |
| 异常重新抛出不吞异常、cause 保留 | ✅ |
| 临时文件清理覆盖 `!written` 与 IOException 两条路径 | ✅ |

**待人工验证命令**（本地具备 JDK 17+ 时执行）：

```bash
mvn compile -DskipTests && mvn test -Dtest=ImageGeneratorTest
```

## 六、遗留风险

- 因环境无 JDK，修复后未做真实编译/单测，逻辑正确性以静态审查为准。
- `fitFont` 缩小到下限 8 后若文字仍超宽，将按最小字号 + x=0 左对齐截断式渲染（超宽部分越出右边界）；因需求未要求多行换行/文本截断策略，按最小改动原则仅在单一文字行内做字号自适应，此为已接受的边界行为。