# Task 1: 新增待办事项（最小闭环：仅创建）

## 需求（原文）
- 目标：帮助用户记录日常待办事项。
- 核心功能：新增待办事项。
- 任务信息：事项名称（name）和描述（description）。
- 目标用户：内部用户。
- 最小闭环：仅创建（不做编辑、删除、查询列表之外的扩展）。

## 验收标准（精确、逐条可验证）
1. 提供创建待办事项的能力：给定事项名称和描述，生成一条待办事项记录。
2. 每条待办事项必须包含两个字段：`name`（事项名称）和 `description`（事项描述），二者均为字符串，且 `name` 非空。
3. 仅实现"创建"这一闭环；不得新增删除、编辑、完成标记、持久化到数据库/文件等未要求的功能（YAGNI）。
4. 可通过确定性、可自动运行的测试验证行为。

## 技术与环境约束（来自当前仓库事实）
- 运行环境：只有 Python 3.12.3 可用；无 java/javac/mvn/gradle/node/npm；已确认无 JDK、无 ~/.m2、无 JUnit。
- 仓库现有 `HelloWorld.java` 是上一轮任务的历史产物，与本任务无关，不得改动或删除。
- 禁止使用 grep/glob 工具；禁止 git 写操作（commit/push/merge 等）。**本任务禁止 git commit**——只写工作区文件并报告。

## 实现要求
- 语言：Python 3（无任何第三方依赖，仅标准库）。
- 单文件数据模型 + 测试：实现于 `todo.py`，测试于 `test_todo.py`（使用标准库 `unittest`）。
- 命名：提供一个 `TodoItem` 类（字段 `name`、`description`）与一个 `TodoStore` 类（提供 `create(name, description)` 方法，返回创建的 `TodoItem`；重复同名事项允许创建，不做去重；`name` 为空字符串或非字符串时抛出 `ValueError`）。
- 输出文件均为当前仓库根目录（cwd），禁止写入仓库外。

## 报备要求
把完整报告写入 `.superpowers/sdd/task-1-report.md`，包括：
- 实现内容、测试结果（RED/GREEN 证据：命令 + 输出）、文件清单、自审发现、疑虑。
返回消息仅含：状态（DONE / DONE_WITH_CONCERNS / BLOCKED / NEEDS_CONTEXT）、创建的文件列表（非 commit）、一行测试结果、疑虑、报告路径。