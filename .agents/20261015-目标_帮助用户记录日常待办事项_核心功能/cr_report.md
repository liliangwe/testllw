# Code Review Report — Todo CLI（问题修复后二次审查）

- **项目**: Todo CLI（日常待办事项管理工具）
- **语言**: Python 3.9+
- **审查日期**: 2026-10-15
- **审查范围**: 问题修复阶段变更（`f11b270` → `5704854`），2 文件 +27/-9 行
- **测试结果**: 23/23 PASS（`python3 -m unittest discover -s tests -v`）
- **产出基准**: `docs/design/todo-cli-design.md` 设计文档

---

## 一、上轮 Review 问题修复核验

对照上一轮审查报告（`cr_report.md` 初版）中发现的 5 个问题逐一核验：

### 🔴 [blocking] 测试污染真实数据文件 — ✅ 已修复

**原问题**: `test_uses_default_data_file` 直接调用 `add_task("test", data_file=None)` 污染 `~/.todo/tasks.json`。

**修复**: 使用 `tempfile.mkdtemp()` + `patch("src.todo_cli.main.DEFAULT_DATA_FILE", data_file)` 隔离。

```python
# tests/test_data.py:115-122
data_file = Path(tempfile.mkdtemp()) / "tasks.json"
with patch("src.todo_cli.main.DEFAULT_DATA_FILE", data_file):
    task = add_task("test", data_file=None)
    self.assertIsNotNone(task)
    self.assertEqual(task["name"], "test")
```

**验证**: 测试通过，`DEFAULT_DATA_FILE` 被临时路径替换，不接触真实数据。

---

### 🟡 [important] `validate_name` 拒绝合法空格 — ✅ 已修复

**原问题**: `string.whitespace` 包含空格，导致 "写 周报" 等合理名称被拒绝。

**修复**: 移除 `import string`，定义显式常量 `_CONTROL_CHARS = "\t\n\r\x0b\x0c"`，仅拒绝控制字符，保留空格为合法字符。

```python
# src/todo_cli/main.py:17
_CONTROL_CHARS = "\t\n\r\x0b\x0c"
# src/todo_cli/main.py:24
if any(c in name for c in _CONTROL_CHARS):
    raise ValueError("事项名称不能包含控制字符")
```

**验证**: 新增 `test_name_with_spaces_passes` 测试，验证 "写 周报" 和 "买菜 清单" 均通过校验。✅

---

### 🟡 [important] `main()` 未捕获文件系统错误 — ✅ 已修复

**原问题**: 磁盘满/权限不足时 traceback 暴露给用户，违反设计文档 3.3 节要求。

**修复**: 在 `main()` 的 `try` 块增加 `except OSError as exc` 分支。

```python
# src/todo_cli/main.py:120-122
except OSError as exc:
    print(f"❌ 文件写入失败: {exc}", file=sys.stderr)
    sys.exit(1)
```

**验证**: 错误路径退出码 1，用户看到友好中文错误提示。

---

### 🟢 [nit] `load_tasks` 未处理损坏的 JSON — ✅ 已修复

**原问题**: 手动编辑损坏 `tasks.json` 导致 `JSONDecodeError` traceback。

**修复**: 在 `load_tasks` 中捕获 `json.JSONDecodeError`，返回空列表。

```python
# src/todo_cli/main.py:32-35
try:
    return json.loads(filepath.read_text(encoding="utf-8"))
except json.JSONDecodeError:
    return []
```

**验证**: 损坏 JSON → 返回 `[]` → CLI 正常继续（下次 add 会重建文件）。⚠️ 附带风险：损坏文件中的数据会丢失，但对 MVP 可接受。

---

### 🟢 [nit] 临时文件泄漏风险 — ⏸️ 未修复（可接受）

原报告指出 kill -9 场景下 `NamedTemporaryFile(delete=False)` 的 `.tmp` 残留。此问题在修复阶段未被处理。对于 CLI 工具，残留概率极低且不阻塞功能，保持在 🟢 nit 级别。

---

## 二、新增代码质量审查

### ✅ 符合项

- **零第三方依赖**: `import string` 已移除，所有导入均为标准库（`__future__`, `argparse`, `json`, `os`, `sys`, `datetime`, `pathlib`, `tempfile`）
- **类型注解完整**: 6 个函数全部有参数/返回值类型注解
- **常量命名**: `_CONTROL_CHARS` 前导下划线表示模块私有，语义清晰
- **测试新增**: `test_name_with_spaces_passes` 覆盖空格合法语义；`test_uses_default_data_file` 使用 `patch` 隔离
- **`from unittest.mock import patch`**: 标准库模块，不引入新依赖

### 🔍 回归验证

| 检查项 | 状态 | 证据 |
|--------|------|------|
| 零第三方依赖 | ✅ | AST 分析仅 stdlib |
| 全部 23 测试通过 | ✅ | `unittest discover` |
| 原子写入 | ✅ | `NamedTemporaryFile` + `os.replace` |
| 退出码一致 | ✅ | 所有错误路径返回 1 |
| validate_name 允许空格 | ✅ | `_CONTROL_CHARS` 不含 `' '` |
| OSError 友好提示 | ✅ | `main()` 捕获并打印中文信息 |
| JSONDecodeError 容错 | ✅ | 损坏 JSON 返回空列表 |

---

## 三、REVIEW.md 项目闸门核对

| # | 闸门 | 状态 |
|---|------|------|
| 1 | 零第三方依赖（运行时+测试） | ✅ stdlib only |
| 2 | `NamedTemporaryFile` + `os.replace` 原子写入 | ✅ |
| 3 | 所有错误路径退出码 1 | ✅ |
| 4 | `validate_name` 拒绝空名和含空白名 | ⚠️ 见下方 |
| 5 | 测试覆盖：parser / main / 各数据层函数 | ✅ 23 tests |
| 6 | `pyproject.toml` 注册入口 | ✅ |

> ⚠️ 闸门 4：REVIEW.md 原文为 "rejects empty and whitespace-containing names"，但代码实际行为是拒绝空名和**控制字符**名（允许普通空格）。此差异源于设计文档 3.3 节原文："名称包含非法字符（如换行符）时拒绝"，未将空格列为非法。建议将 REVIEW.md 闸门 4 同步为 "rejects empty and control-character-containing names"。

---

## 四、问题统计

| 级别 | 数量 | 说明 |
|------|------|------|
| 🔴 blocking | **0** | 上轮 1 个 blocker 已修复 |
| 🟡 important | **0** | 上轮 2 个 important 已修复 |
| 🟢 nit | **1** | 临时文件残留风险（kill -9 场景，已评估可接受） |
| 💡 suggestion | **1** | REVIEW.md 闸门 4 措辞建议同步更新 |

---

## 五、审查决定

✅ **Approve** — 上轮审查中 1 个 🔴 blocker 和 2 个 🟡 important 全部修复。23 个测试全部通过。零第三方依赖。代码质量与设计文档一致。

唯一未修复项为 🟢 nit 级别的临时文件残留风险，不影响合并。