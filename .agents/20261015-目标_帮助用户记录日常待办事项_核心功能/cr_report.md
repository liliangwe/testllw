# Code Review Report — Todo CLI

- **项目**: Todo CLI（日常待办事项管理工具）
- **语言**: Python 3.9+
- **审查日期**: 2026-10-15
- **审查范围**: 全量变更（设计文档 → 实施计划 → 代码实现）
- **测试结果**: 22/22 PASS（`python3 -m unittest discover -s tests -v`）
- **产出基准**: `docs/design/todo-cli-design.md` 设计文档

---

## 审查维度与发现

### 一、功能核对（需求 vs 实现）

对照设计文档 `docs/design/todo-cli-design.md` 第 7 节验收标准：

| AC | 场景 | 实现状态 | 证据 |
|----|------|----------|------|
| AC-1 | `todo add -n "测试" -d "描述"` → 写入 JSON + ✅ 确认 | ✅ 通过 | `test_add_writes_task_and_prints_success` |
| AC-2 | `todo add -n "无描述"` → description="" | ✅ 通过 | `test_add_without_description_uses_empty` |
| AC-3 | `todo add`（缺 -n）→ 退出码 1 + 错误提示 | ✅ 通过 | `test_add_missing_name_exits` |
| AC-4 | 首次运行（无 ~/.todo）→ 自动创建 | ✅ 通过 | `test_creates_file_and_directory` + `save_tasks` 的 `mkdir(parents=True)` |
| AC-5 | 连续多次 add → 追加不覆盖 | ✅ 通过 | `test_appends_to_existing_tasks` |

**核对结论**: 核心功能全部满足设计规格。

---

### 二、可读性审查

#### ✅ 符合项
- 类型注解完整：所有公开函数均有参数/返回值注解
- 文档字符串：每个函数/类均有中文 docstring
- 命名规范：snake_case 函数名、UPPER_SNAKE_CASE 常量、PascalCase 测试类
- `from __future__ import annotations` + `Path | None` 语法，符合 Python 3.9+ 最佳实践
- 导入顺序正确：标准库在前，无第三方导入
- 原子写入实现清晰：`NamedTemporaryFile` → `json.dump` → `flush` → `fsync` → `os.replace`

#### 🟢 [nit] 建议
- `main.py:22` `string.whitespace` 的字符遍历可用 `any(c in string.whitespace for c in name)` 替代，语义相同但更简洁

---

### 三、可靠性审查

#### 🔴 [blocking] 测试污染真实数据文件

**文件**: `tests/test_data.py:109-112`
```python
def test_uses_default_data_file(self) -> None:
    """不传 data_file 时使用 ~/.todo/tasks.json。"""
    task = add_task("test", data_file=None)
    self.assertIsNotNone(task)
```

- **问题**: 该测试调用 `add_task("test", data_file=None)`，触发默认路径 `~/.todo/tasks.json`，直接写入用户真实数据文件。
- **风险**: 测试运行会污染用户真实待办数据，且 test 数据不可追溯。
- **修复建议**: 使用 `patch("src.todo_cli.main.DEFAULT_DATA_FILE", temp_file)` 隔离，或显式传入临时 `data_file`。

#### 🟡 [important] `validate_name` 拒绝合法空格字符

**文件**: `src/todo_cli/main.py:22-23`
```python
if any(c in name for c in string.whitespace):
    raise ValueError("事项名称不能包含空白字符")
```

- `string.whitespace` = `' \t\n\r\x0b\x0c'` —— 包含普通空格。
- 设计文档 3.3 节仅提及"名称包含非法字符（如换行符）时拒绝"，未将空格列为非法字符。
- "写 周报"、"买菜 清单" 等含空格的任务名会被拒绝，用户预期不符。
- **修复建议**: 将检查改为仅检查控制字符（`\t\n\r\x0b\x0c`），保留 `' '` 为合法字符：
  ```python
  CONTROL_CHARS = "\t\n\r\x0b\x0c"
  if any(c in name for c in CONTROL_CHARS):
      raise ValueError("事项名称不能包含控制字符")
  ```

#### 🟡 [important] `main()` 未捕获文件系统错误

**文件**: `src/todo_cli/main.py:96-116`

- 设计文档 3.3 节明确规定："文件系统不可写时，输出具体错误并退出"
- 当前实现：`save_tasks` 中的 `OSError`（磁盘满、权限不足等）会向上传播为未捕获异常，用户看到的是 traceback 而非友好错误提示。
- 实现计划自审清单也承认："由 Python `OSError` 自然传播，`main()` 中未显式捕获则 traceback 输出到 stderr"。
- **修复建议**: 在 `main()` 的 `try` 块中增加 `except OSError as exc` 分支：
  ```python
  except OSError as exc:
      print(f"❌ 文件写入失败: {exc}", file=sys.stderr)
      sys.exit(1)
  ```

#### 🟢 [nit] `load_tasks` 未处理损坏的 JSON

**文件**: `src/todo_cli/main.py:26-30`

- 如果 `tasks.json` 被手动编辑损坏或为空，`json.loads` 抛出 `JSONDecodeError`，导致 traceback。
- 对于内部 CLI 工具风险较低，但建议处理。

#### 🟢 [nit] 临时文件泄漏风险

**文件**: `src/todo_cli/main.py:36-49`

- `NamedTemporaryFile(delete=False)` + `os.replace` 是标准的原子写入模式。但如果进程在 `os.replace` 之前被 kill，临时文件会残留在 `~/.todo/` 目录中。
- 对 CLI 工具影响极小，可接受。

---

### 四、安全性审查

- ✅ 无外部输入执行（无 `eval`/`exec`/`subprocess`）
- ✅ 无网络请求
- ✅ 数据仅写入用户主目录 `~/.todo/`
- ✅ 原子写入防止数据损坏
- ✅ `validate_name` 拒绝控制字符，防止 JSON 注入
- ⚠️ `string.whitespace` 包含 `\x0b`（垂直制表符）和 `\x0c`（换页符），虽然不常见但已覆盖

---

### 五、测试质量

- **覆盖率**: 22 个测试覆盖了数据层（13 tests）和 CLI 层（9 tests）
- **测试隔离**: 除 🔴 问题外，其他测试使用 `tempfile.TemporaryDirectory` + `patch` 良好隔离
- **边界条件覆盖**: 空名称、控制字符、文件不存在、目录不存在、多次追加均已覆盖
- **缺失测试**: 
  - 🟡 空格字符的合法/非法语义测试
  - 🟡 文件系统错误的集成测试（OSError 场景）

---

## 审查总结

### 评分

| 维度 | 评级 | 说明 |
|------|------|------|
| 功能完整性 | ✅ 优秀 | 5 项验收标准全部通过 |
| 代码可读性 | ✅ 优秀 | 类型注解、文档字符串、命名均规范 |
| 可靠性 | 🟡 良好 | 1 个阻塞问题 + 2 个重要问题 |
| 安全性 | ✅ 优秀 | 无外部攻击面，原子写入 |
| 测试质量 | 🟡 良好 | 覆盖充分但存在测试污染 |

### 问题统计

| 级别 | 数量 | 说明 |
|------|------|------|
| 🔴 blocking | 1 | 测试污染用户真实数据文件 |
| 🟡 important | 2 | 空格验证过严 + 缺少 OSError 处理 |
| 🟢 nit | 2 | JSON 损坏处理 + 临时文件泄漏 |

### 审查决定

🔄 **Request Changes** — 存在 1 个阻塞项，必须修复后方可合并。

阻塞项修复后（将 `test_uses_default_data_file` 改为使用 `patch` 隔离），🟡 级别问题可在后续迭代中处理，不阻塞当前最小闭环交付。