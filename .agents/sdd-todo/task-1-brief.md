# Task 1 Brief：实现待办事项创建

读取本文件作为你的需求来源，精确值如下，逐字使用。

## 需求
帮助用户记录日常待办事项。仅实现核心功能"新增待办事项"，最小闭环为"仅创建"（不做查询/修改/删除/持久化/CLI/HTTP）。

## 交付文件（均在仓库根目录，默认包，无 package 声明）
### 1. Todo.java —— 待办事项实体
- 字段：`private final String title;` 与 `private final String description;`
- 构造器 `public Todo(String title, String description)`：
  - title 为空（null 或空字符串/纯空白）时抛出 `IllegalArgumentException`
  - description 允许为 null 或空
- getter：`getTitle()` 返回 title，`getDescription()` 返回 description
- 重写 `toString()`，输出形如 `Todo{title='...', description='...'}`

### 2. TodoService.java —— 新增待办事项
- 内部私有字段 `private final List<Todo> todos = new ArrayList<>();`
- `public Todo addTodo(String title, String description)`：委托 Todo 构造器校验，创建后加入 todos 列表，返回创建的 Todo
- `public int size()`：返回 todos.size()

## 代码风格（必须遵守，与仓库 HelloWorld.java 一致）
- 默认包（无 package）
- 类级及方法级 Javadoc 使用中文注释，包含类职责说明
- 使用 `@author AiWork`、`@date 2026/10/21`
- 变量命名清晰，符合 Java 惯例
- 不引入外部依赖

## 约束（必须遵守）
- Git 只读：禁止任何 git 写操作（commit/push/merge/reset/rebase/checkout）
- 不创建额外文件（不写测试文件、不写 README、不写 main/CLI）
- 环境无 javac/java 编译器，无法编译运行，采用自检式静态审查：逐条对照规格核对字段/方法签名/空值校验逻辑

## 报告要求
将完整报告写入 `.agents/sdd-todo/task-1-report.md`，内容：
- 实现了什么
- 静态自检结果（对照规格逐条）
- 文件变更清单
- 自审发现的问题（如有）
- 任何疑虑
随后仅回传（15 行内）：Status（DONE / DONE_WITH_CONCERNS / BLOCKED / NEEDS_CONTEXT）、文件清单、一行自检结论、疑虑、报告文件路径。