# Task 1: 待办事项创建（仅创建）

## 需求（原文转述）
创建待办事项：帮助用户记录日常待办事项。
核心功能：新增待办事项。
任务信息：事项名称（name）和描述（description）。
目标用户：内部用户。
最小闭环：仅创建。

## 技术决策（控制器已确定，必须遵守）
- 语言：Python 3（仅标准库）。原因：当前环境无 Java/JDK、Node、Maven/Gradle，仅 python3 与 gcc 可运行；python3 是本任务唯一可运行、可测试的语言。
- 无第三方依赖；测试使用标准库 unittest。
- 产物文件（均位于仓库根目录 cwd 下，禁止写到仓库外）：
  - `todo.py` — 待办事项模型与存储
  - `test_todo.py` — 单元测试

## 接口契约
在 `todo.py` 中：
- `TodoItem`：数据类，字段 `id: int`、`name: str`、`description: str`
- `TodoStore`：方法 `create(name: str, description: str = "") -> TodoItem`
  - 校验 name 非空白字符串，空白时抛出 `ValueError`
  - 分配自增 id，从 1 开始
  - 将创建的事项追加到内部列表，通过 `items` 属性可读取

## 验收标准
- `python3 -m unittest test_todo -v` 全部通过
- 测试覆盖：
  1. create 返回含正确 name / description 的 TodoItem
  2. id 自增，多次创建的 id 互不相同
  3. 空白 name 抛 ValueError
  4. 创建后可从 `store.items` 取回该事项

## 硬性约束
- 严禁执行任何 git 写操作（commit/add/push/merge/reset/rebase/checkout 等）；只读 git 命令才允许。
- 禁止使用 grep / glob 命令。
- 不新增 README 或无关文件；不引入第三方依赖。
- YAGNI：只实现“创建”。不实现更新/删除/查询/持久化/CLI/Web/多线程。