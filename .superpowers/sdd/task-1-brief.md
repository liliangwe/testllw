# Task 1 Brief: 实现创建待办事项功能

## 需求（逐字执行）
创建一个「待办事项」最小闭环，帮助内部用户记录日常待办事项。
- 核心功能：**新增待办事项**（仅创建）。
- 任务信息字段：事项名称（title）、事项描述（description）。

## 接口与决策（已由负责人确定，不可更改）
- 技术栈：Python 3 标准库，无第三方依赖、无网络、无数据库、无文件 IO。
- 新建 `todo.py`：
  - `Todo` 数据类（dataclass），字段 `id: str`、`title: str`、`description: str`。
  - `TodoService` 类：构造时内部持有内存列表 `self._items`；
    - `create(self, title: str, description: str = "") -> Todo`：
      1. `title` 去除首尾空白后若为空 → `raise ValueError("标题不能为空")`。
      2. `description` 保留原值（不去空白，可空）。
      3. 生成唯一 id：`str(uuid.uuid4())`。
      4. 构造 `Todo` 追加到 `self._items`，返回该 `Todo`。
    - 提供只读属性 `items`（返回 `list(self._items)` 的副本），仅用于测试验证已持久化；**不要**实现 update/delete/query 等额外 API。
- 新建 `test_todo.py`，标准库 `unittest`，用例（虚构标题，确切断言）：
  1. `test_create_returns_todo_with_fields`：`create("买牛奶", "下班顺路")` 返回的 Todo 的 `title == "买牛奶"`、`description == "下班顺路"`、`id` 为非空字符串。
  2. `test_create_title_stripped`：`create("  写周报  ")` 返回 title == `"写周报"`。
  3. `test_create_empty_title_raises`：`create("   ")` 抛出 `ValueError`。
  4. `test_create_default_description_empty`：`create("开会")` 返回 description == `""`。
  5. `test_create_persists`：同一 `TodoService` 实例 create 两次后 `len(service.items) == 2`。
  6. `test_create_generates_unique_ids`：create 两次返回的 id 不相等。

## 禁止事项
- 严禁任何 Git 写操作（不 `git commit` / `add` / `checkout` 等）。只允许 `git status/diff` 读取。
- 不修改 README.md、HelloWorld.java，不新增其他文件。
- 不安装依赖、不联网。

## 验收
- `python3 -m unittest test_todo -v` 全部 6 个用例通过，无警告噪音。

## 汇报
- 状态（DONE / DONE_WITH_CONCERNS / BLOCKED / NEEDS_CONTEXT）
- 测试结果一行摘要
- 新文件清单
- 疑虑（如有）