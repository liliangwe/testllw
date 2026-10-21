# 任务简报：创建待办事项（最小闭环）

## 需求
- 目标：帮助内部用户记录日常待办事项。
- 核心功能：新增待办事项。
- 任务信息：事项名称（title）和描述（description）。
- 最小闭环：仅创建（不做查询/更新/删除/持久化到数据库）。

## 技术栈与约定（遵循现有仓库惯例）
- 仓库为纯 Java 项目（现仅有 `HelloWorld.java`），无构建工具（无 Maven/Gradle），无第三方依赖，仅能用 JDK 标准库。
- 必须遵循既有风格：单文件 public 类同名；K&R 大括号；4 空格缩进；行宽 ≤120；UpperCamelCase 类名、lowerCamelCase 方法与变量；public 类与方法均有 Javadoc；UTF-8；无 Tab。

## 交付文件（全部基于 cwd = 仓库根目录，相对路径写入）
1. `Todo.java` — 待办事项模型。
   - 字段：`String title`（名称）、`String description`（描述），均为 `private final`。
   - 构造器 `Todo(String title, String description)`。
   - getter：`getTitle()`、`getDescription()`。
   - （可选，最小闭环不要求）`toString()`。
2. `TodoService.java` — 新增待办事项能力。
   - 内存存储：`List<Todo>`（`ArrayList`），`private final`。
   - `Todo create(String title, String description)`：构造 `Todo`，加入列表，返回创建的 `Todo`。
   - 校验：`title` 为 `null` 或去除首尾空白后为空，抛 `IllegalArgumentException`（带清晰中文提示）；`description` 允许为 `null`（内部归一为 `""`）。
3. `TodoApp.java` — 程序入口（演示「创建」闭环，供运行验证）。
   - `main`：新建 `TodoService`，`create("示例事项", "示例描述")`，向标准输出打印创建的 `Todo` 的 title 与 description，正常退出返回码 0。

## 约束（硬性）
- **Git 只读**：禁止任何 git 写操作（commit/push/merge/reset/rebase/checkout/stash/add 等一律禁止）。仅可 `git status/log/diff` 读取。
- **只允许创建上述 3 个新文件**，不得修改 `HelloWorld.java`、`README.md` 或任何既有文件。
- 环境无 JDK，`javac` 不可用：不要尝试编译或安装 JDK。改为**静态自审**：逐文件核对语法正确（括号配平、import、方法签名、类型一致），并在报告中说明「未能编译验证（环境无 JDK）」。
- 工具黑名单：禁止使用 `grep` / `glob`。

## 报告
将实施报告写入 `.agents/sdd/todo-create-report.md`，内容包含：
- 交付文件清单与关键实现说明；
- 静态自审结论（语法/类型/边界：title 为 null、空串、纯空白抛出异常；description 为 null 归一为 ""）；
- 测试验证结论（明确说明无法编译的原因）。返回信息仅需：状态（DONE / BLOCKED）、涉及提交（本次无，因 Git 只读）、一行测试摘要、关切点。