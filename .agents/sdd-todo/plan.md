# 计划：创建待办事项（最小闭环：仅创建）

## 需求
- 目标：帮助用户记录日常待办事项
- 核心功能：新增待办事项
- 任务信息：事项名称（title）和描述（description）
- 目标用户：内部用户
- 最小闭环：仅创建

## 全局约束
1. Git 只读：禁止任何 git 写操作（commit/push/merge/reset/rebase/checkout）
2. 技术栈：Java（仓库现有唯一源码 HelloWorld.java 为 Java，沿用惯例）
3. 代码风格：默认包；Javadoc 中文注释；`@author AiWork`；`@date`
4. YAGNI：仅实现"新增待办事项"，不做查询/修改/删除/持久化/CLI/HTTP
5. 验证：环境无 javac/java 编译器，采用静态代码审查（与上轮 HelloWorld 流程一致）

## 任务列表
- Task 1: 实现待办事项创建 —— Todo 模型 + TodoService 新增能力（唯一任务，本需求即单任务）

## Task 1 规格
### Todo.java（待办事项实体）
- 字段：`String title`（事项名称）、`String description`（描述）
- 构造器：`Todo(String title, String description)`
- title 必填非空，空则抛 `IllegalArgumentException`；description 允许为空
- getter：`getTitle()`、`getDescription()`
- 重写 `toString()` 便于查看

### TodoService.java（新增待办事项）
- 内部持有 `List<Todo>`
- `Todo addTodo(String title, String description)`：校验、创建、加入列表并返回创建的 Todo
- `int size()`：返回当前待办数量（用于验证新增生效）