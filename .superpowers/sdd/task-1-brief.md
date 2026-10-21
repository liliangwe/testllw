# Task 1: 实现新增待办事项（最小闭环：仅创建）

## 需求
- 目标：帮助用户记录日常待办事项。
- 核心功能：新增待办事项。
- 任务信息：事项名称（name）和描述（description）。
- 目标用户：内部用户。
- 最小闭环：仅创建。

## 技术约定
- 仓库现有代码为纯 Java 单文件风格（HelloWorld.java），无构建系统（无 pom.xml / build.gradle），无任何第三方依赖。
- 环境无 JDK（javac / java 不存在），无法编译/运行，测试验证降级为精确静态代码审查。
- 沿用现有风格：public 类/方法带 Javadoc、K&R 大括号、4 空格缩进、UpperCamelCase/lowerCamelCase 命名、`String[] args` 数组风格。

## 交付物（均为仓库根目录相对路径，严禁写到 cwd 之外）
1. `TodoItem.java` — 待办事项实体（id、name、description、createdAt）；name 必填（null 或空白抛 IllegalArgumentException）；id 全局自增；description 允许为空但统一 trim。
2. `TodoService.java` — 待办事项服务：`create(name, description)` 新增并返回 TodoItem；`listAll()` 以只读视图返回当前全部待办（用于验证创建结果）。
3. `TodoApp.java` — 程序入口，演示最小闭环：新增一条待办并打印创建结果（id/name/description）。

## 验收标准
- `create` 返回新 TodoItem，且能通过 `listAll()` 取回同一条。
- name 为 null 或空白时抛 IllegalArgumentException。
- 仅实现「创建」，不实现更新/删除/持久化/Web API/命令行交互（YAGNI）。

## 硬性约束
- 禁止任何 Git 写操作（commit / push / merge / reset / rebase / checkout），仅保留工作区新文件改动。
- 禁止使用 grep / glob 工具。
- 无 JDK：不要尝试编译或运行；以静态自查代替测试，报告中注明「无 JDK，未编译运行」。