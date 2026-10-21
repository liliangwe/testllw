# Task 1 报告：新增待办事项（最小闭环：仅创建）

## 实现内容

- `todo.py`：
  - `TodoItem` 类，字段 `name`、`description`。
  - `TodoStore` 类，`create(name, description)` 方法：
    - 校验 `name` 与 `description` 均为字符串，否则抛 `ValueError`；
    - 校验 `name` 非空，空字符串抛 `ValueError`；
    - 返回 `TodoItem`。
    - 不做去重（重复同名允许创建）。
    - 不实现删除、编辑、完成标记、持久化（符合 YAGNI 与验收标准 3）。
- `test_todo.py`：标准库 `unittest`，覆盖 5 条验收行为。

## TDD 证据

RED（实现前，仅存在测试、无 `todo` 模块）：

```
$ python3 -m unittest test_todo -v
test_todo (unittest.loader._FailedTest.test_todo) ... ERROR
...
ImportError: Failed to import test module: test_todo
ModuleNotFoundError: No module named 'todo'
----------------------------------------------------------------------
Ran 1 test in 0.000s
FAILED (errors=1)
```

GREEN（实现 `todo.py` 后）：

```
$ python3 -m unittest test_todo -v
test_create_returns_item_with_given_name_and_description ... ok
test_description_may_be_empty ... ok
test_duplicate_names_still_create ... ok
test_name_non_empty_enforced ... ok
test_name_non_string_enforced ... ok

----------------------------------------------------------------------
Ran 5 tests in 0.000s

OK
```

补充验证 `python3 -m unittest discover -v`：同样 `Ran 5 tests ... OK`。

冒烟脚本（真实执行路径，二次构造输入）：

```
$ python3 - <<'PY'
from todo import TodoItem, TodoStore
s = TodoStore()
assert s.create("a","b").name == "a"
for name in ("", 5, None):
    try:
        s.create(name, "x")
    except ValueError:
        pass
    else:
        raise SystemExit("expected ValueError for %r" % name)
print("smoke OK")
PY
smoke OK
```

## 文件清单

- 新增 `todo.py`
- 新增 `test_todo.py`
- 新增 `.superpowers/sdd/task-1-report.md`（本报告）

未改动 HelloWorld.java、README.md、.agents/ 下任何文件。

## 自审发现

- 验收标准 2 要求 `name` 与 `description` 二者均为字符串，因此 `create` 同时对 `description` 做了非字符串校验（测试仅显式覆盖 name，属于实现细节，与标准一致，未过度构建）。
- 使用 `str` 的 `isinstance` 判断非字符串，覆盖 `int`/`None` 等常见非字符串输入。
- 未提交 git、未安装任何依赖，仅使用 Python 3.12 stdlib（`unittest`）。

## 疑虑

- 无阻塞。一个轻微设计点：`TodoStore` 目前无内部存储集合（仅创建并返回），符合"仅创建"最小闭环与防过度构建要求；若后续任务需要"查询列表"，需再补充内部存储，但当前明确不实现。