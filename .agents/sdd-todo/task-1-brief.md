# Task 1：实现「创建待办事项」最小闭环

## 需求原文（验收标准）
- 目标：帮助用户记录日常待办事项，目标用户为内部用户。
- 核心功能：新增待办事项。
- 事项信息：名称（name）和描述（description）。
- 最小闭环：仅创建。

## Global Constraints（精确值，逐字遵守）
- 事项字段：`name`（名称）、`description`（描述）两字段，均为字符串。
- 能力范围：仅「新增/创建」；不实现修改、删除、完成状态、持久化、数据库、Web、GUI、并发。
- 语言：Java；默认包（无 package 声明，与 HelloWorld.java 一致）；不引入任何第三方依赖与构建工具。
- 禁止 Git 写操作（`commit`/`push`/`checkout` 等一律禁止），只允许写源码与文档文件。
- 环境无 JDK（`javac`/`java` 不可用），无法编译运行：实现后以静态审查为主要验证手段，并在报告里给出「在有 JDK 环境下的预期运行结果」。

## 交付物
1. `TodoItem.java` — 待办事项模型：字段 `name`、`description`，构造器 + 只读 getter（不引入 id、状态等越想功能）。
2. `TodoList.java` — 待办列表：持有 `List<TodoItem>`；提供 `add(TodoItem)`（以及便捷 `add(String name, String description)`）新增方法；提供只读的查询/个数/list 方法以支撑验证（行为最小，不做越界功能）。
3. `TodoApp.java` — `main` 入口演示：创建列表、新增两条事项、打印列表，作为最小可运行 smoke 演示。

## 测试与验证
- 无 JUnit/构建工具：测试以 `TodoApp.main` 的演示 + 静态审查为主；可在 main 中做简单断言（如新增后 size 符合预期、打印内容含所加事项），并在有 JDK 时可直接 `javac *.java && java TodoApp` 验证。
- 报告须包含：文件清单、预期输出、静态审查结论（命名/边界/类型一致性）。

## 报告文件
写入 `.agents/sdd-todo/task-1-report.md`。