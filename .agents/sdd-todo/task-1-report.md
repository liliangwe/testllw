# Task 1 报告：实现「创建待办事项」最小闭环

## 状态
DONE

## 实现了什么
按 `task-1-brief.md` 的验收标准，为 HelloWorld.java 所在的默认包仓库新增「创建待办事项」最小闭环，共 3 个源码文件。仅实现 `name`、`description` 两字段与「新增/创建」能力，未引入 id、状态、修改、删除、完成、持久化、数据库、Web、GUI、并发等越界功能。

## 文件清单
- `TodoItem.java` — 待办事项模型：final 字段 `name`、`description`，构造器 + 只读 getter。
- `TodoList.java` — 待办列表：内部 `List<TodoItem>`，提供 `add(TodoItem)`、便捷 `add(String name, String description)`、只读 `size()` / `getItems()`（返回不可变视图）。
- `TodoApp.java` — main 入口演示：创建列表、新增两条事项、打印列表，并在 main 内做简单断言。

未改动既有文件 `HelloWorld.java`、`README.md`。

## 测试与验证方式（环境无 JDK）
环境已确认无 `javac`/`java`（`command -v javac java` 均无输出，调用返回 127 command_not_found）。因此本任务无法编译运行，验证手段为静态审查 + 自动化静态检查脚本。已运行的检查及结果：

1. 无 `package` 声明检查：`grep -n '^package '` → 无输出，符合「默认包」约束（与 HelloWorld.java 一致）。
2. 越界能力检查：`grep -niE '\bid\b|status|remove|delete|update|set[A-Z]|sql|jdbc|file|database'` → 无输出，未引入越界功能。
3. 新增能力检查：`TodoList.java` 存在 `add(TodoItem)` 与 `add(String, String)`，`TodoApp.java` 中调用两次 `add(...)`，符合验收。
4. 行宽检查：按源码字符计宽度 ≤120（最大 76 字符；含中文按 2 列显示宽也仅 84）。注：GNU `awk length()` 对 UTF-8 按字节计数会误报中文字符，故最终用 Python 按 Unicode 码点复核，结论为全部合规。
5. K&R 大括号自检：无私自换行/`else` 等违规迹象。

## TDD 证据
不适用。仓库无 JUnit 与构建工具（本任务简报明确「无 JUnit/构建工具，测试以 TodoApp.main 演示 + 静态审查为主」）。

## 静态审查结论
- 命名：文件名均等于唯一 public 类名；类名 UpperCamelCase（TodoItem/TodoList/TodoApp）；方法与字段 lowerCamelCase（add/size/getItems/getName/getDescription）。public 类与 public 方法均配 Javadoc（`@author`/`@date` 沿用 HelloWorld 风格）。
- 边界：`getItems()` 返回 `Collections.unmodifiableList`，只读视图不外泄可变内部集合；内部集合为 final 初始化，无空指针边界分支需求（新增任意 TodoItem 均安全）。
- 类型一致性：字段、构造参数、getter 返回、`List<TodoItem>` 泛型、`add` 重载参数全程统一，无类型不一致。
- YAGNI：未实现除「新增」以外的任何能力。

## 预期运行结果（有 JDK 环境下）
编译并运行：

```
javac *.java
java TodoApp
```

预期输出：

```
待办事项列表（共 2 条）：
购买牛奶:下班后去超市购买一盒牛奶
阅读文档:阅读待办事项任务简报，理解验收标准
```

说明：main 内的断言使用 Java `assert` 语句，默认 `java TodoApp` 不启动断言（`-ea` 未开启时断言被跳过，仅打印列表）。若要启用断言做验证，使用 `java -ea TodoApp`：两条断言分别为「新增后 size == 2」与「打印内容包含所加事项」，均成立、若失败则抛 `AssertionError`（非零退出）。

## 遗留问题或担忧
- 环境无 JDK，输出为静态推导，未做真实编译运行；若引入 JDK 后建议按上述命令复核一次。
- main 中的断言依赖 `-ea` 开关，默认 `java` 不会触发；已在报告中说明，供验证时参考。