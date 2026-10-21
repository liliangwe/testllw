# Task 1 实施报告：待办事项创建（仅创建）

## 实现内容

在仓库根目录创建了两个文件（均为 Python 3 标准库）：

### `todo.py`
- `TodoItem`：`dataclass`，字段 `id: int`、`name: str`、`description: str`（`description` 默认 `""`）。
- `TodoStore`：
  - `create(name: str, description: str = "") -> TodoItem`
  - name 非字符串或空白（`name.strip()` 为空）时抛 `ValueError`
  - 自增 id 从 1 开始（内部 `_next_id`）
  - 创建项追加到内部列表，`items` 属性可读取

### `test_todo.py`
- 使用标准库 `unittest`，5 个测试方法覆盖验收标准：
  1. create 返回字段正确的 `TodoItem`
  2. 默认 description 为空串
  3. id 自增且互不相同（1, 2, 3）
  4. 空白 name（`""`、`"   "`、`"\t"`、`"\n"`）抛 `ValueError`
  5. 创建后可从 `store.items` 取回该事项

未实现更新/删除/查询/持久化/CLI/Web/多线程（YAGNI）。无第三方依赖。

## 测试结果（验收门）

```
$ python3 -m unittest test_todo -v
test_blank_name_raises_value_error (test_todo.TodoStoreCreateTest.test_blank_name_raises_value_error) ... ok
test_create_default_description_empty (test_todo.TodoStoreCreateTest.test_create_default_description_empty) ... ok
test_create_returns_item_with_correct_fields (test_todo.TodoStoreCreateTest.test_create_returns_item_with_correct_fields) ... ok
test_created_item_retrievable_from_items (test_todo.TodoStoreCreateTest.test_created_item_retrievable_from_items) ... ok
test_ids_auto_increment_and_are_distinct (test_todo.TodoStoreCreateTest.test_ids_auto_increment_and_are_distinct) ... ok

----------------------------------------------------------------------
Ran 5 tests in 0.000s

OK
```

## TDD 证据

- **RED**：先创建 `test_todo.py`，在尚未实现 `todo.py` 时运行 `python3 -m unittest test_todo -v`，得到 `ModuleNotFoundError: No module named 'todo'`，`FAILED (errors=1)`。
- **GREEN**：补齐 `todo.py` 后再次运行，5 个测试全部通过，退出码 0。

（说明：RED 为导入失败而非断言失败，因为模型文件尚不存在；第一个真实断言的失败未单独跑，后续实现一次全绿。）

## 变更文件

- `todo.py`（新建）
- `test_todo.py`（新建）

仅这两个文件，未改动或新增其他文件（未创建 README）。

## 自审发现

- 接口契约逐字符合：字段名/类型、`create` 签名、`ValueError`、自增 id 从 1、`items` 属性读取均一致。
- 测试覆盖了 brief 列出的 4 条验收标准，且 `store.items` 返回实际追加的同一对象（`assertIn(item, store.items)`），验证真实行为而非仅验证返回值。
- `items` 以 property 暴露内部列表：与"通过 items 属性可读取"要求一致；未做防御性拷贝，因 YAGNI 且 requirement 未要求不可变。

## 问题与关注点

- 无阻塞。`items` 返回内部列表引用，调用方可越权修改存储内容；若后续任务要求封装性，可改为只读视图，但本次仅创建范围不做（避免超范围）。
- 未执行任何 git 写命令，也未尝试 commit（按约束处理）。