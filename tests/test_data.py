"""数据层单元测试。"""

import json
import os
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

from src.todo_cli.main import add_task, load_tasks, save_tasks, validate_name


class TestValidateName(unittest.TestCase):
    """validate_name 测试。"""

    def test_valid_name_passes(self) -> None:
        """合法名称不抛异常。"""
        validate_name("写周报")

    def test_empty_name_raises(self) -> None:
        """空字符串抛 ValueError。"""
        with self.assertRaises(ValueError):
            validate_name("")

    def test_name_with_newline_raises(self) -> None:
        """含换行符抛 ValueError。"""
        with self.assertRaises(ValueError):
            validate_name("hello\nworld")

    def test_name_with_tab_raises(self) -> None:
        """含制表符抛 ValueError。"""
        with self.assertRaises(ValueError):
            validate_name("hello\tworld")

    def test_name_with_carriage_return_raises(self) -> None:
        """含回车符抛 ValueError。"""
        with self.assertRaises(ValueError):
            validate_name("hello\rworld")

    def test_name_with_spaces_passes(self) -> None:
        """含普通空格的名称合法。"""
        validate_name("写 周报")
        validate_name("买菜 清单")


class TestLoadTasks(unittest.TestCase):
    """load_tasks 测试。"""

    def test_returns_empty_list_for_nonexistent_file(self) -> None:
        """文件不存在时返回空列表。"""
        with tempfile.TemporaryDirectory() as tmp:
            filepath = Path(tmp) / "nonexistent.json"
            result = load_tasks(filepath)
            self.assertEqual(result, [])

    def test_returns_tasks_from_existing_file(self) -> None:
        """文件存在时返回解析后的列表。"""
        with tempfile.TemporaryDirectory() as tmp:
            filepath = Path(tmp) / "tasks.json"
            tasks = [{"name": "写周报", "description": "周五前", "created_at": "2026-10-15T12:00:00"}]
            filepath.write_text(json.dumps(tasks, ensure_ascii=False))
            result = load_tasks(filepath)
            self.assertEqual(result, tasks)


class TestSaveTasks(unittest.TestCase):
    """save_tasks 测试。"""

    def test_creates_file_and_directory(self) -> None:
        """目录和文件均不存在时自动创建并写入。"""
        with tempfile.TemporaryDirectory() as tmp:
            filepath = Path(tmp) / "subdir" / "tasks.json"
            tasks = [{"name": "买菜", "description": "", "created_at": "2026-10-15T12:00:00"}]
            save_tasks(filepath, tasks)
            self.assertTrue(filepath.exists())
            loaded = json.loads(filepath.read_text())
            self.assertEqual(loaded, tasks)

    def test_overwrites_existing_file(self) -> None:
        """已存在文件时覆盖写入。"""
        with tempfile.TemporaryDirectory() as tmp:
            filepath = Path(tmp) / "tasks.json"
            old = [{"name": "old", "description": "", "created_at": "2026-10-15T12:00:00"}]
            save_tasks(filepath, old)
            new = [{"name": "new", "description": "", "created_at": "2026-10-15T13:00:00"}]
            save_tasks(filepath, new)
            loaded = json.loads(filepath.read_text())
            self.assertEqual(loaded, new)


class TestAddTask(unittest.TestCase):
    """add_task 测试。"""

    def test_creates_task_with_timestamp(self) -> None:
        """新任务包含 name、description、created_at。"""
        with tempfile.TemporaryDirectory() as tmp:
            data_file = Path(tmp) / "tasks.json"
            task = add_task("写周报", "周五前", data_file=data_file)
            self.assertEqual(task["name"], "写周报")
            self.assertEqual(task["description"], "周五前")
            self.assertIn("created_at", task)
            self.assertTrue(task["created_at"].endswith("+00:00") or task["created_at"].endswith("Z"))

    def test_appends_to_existing_tasks(self) -> None:
        """多次 add 追加到数组末尾。"""
        with tempfile.TemporaryDirectory() as tmp:
            data_file = Path(tmp) / "tasks.json"
            add_task("第一项", data_file=data_file)
            add_task("第二项", data_file=data_file)
            tasks = load_tasks(data_file)
            self.assertEqual(len(tasks), 2)
            self.assertEqual(tasks[0]["name"], "第一项")
            self.assertEqual(tasks[1]["name"], "第二项")

    def test_uses_default_data_file(self) -> None:
        """不传 data_file 时使用 ~/.todo/tasks.json（隔离测试，不污染真实数据）。"""
        import tempfile
        data_file = Path(tempfile.mkdtemp()) / "tasks.json"
        with patch("src.todo_cli.main.DEFAULT_DATA_FILE", data_file):
            task = add_task("test", data_file=None)
            self.assertIsNotNone(task)
            self.assertEqual(task["name"], "test")

    def test_rejects_invalid_name(self) -> None:
        """非法名称（空/含控制字符）抛 ValueError。"""
        with tempfile.TemporaryDirectory() as tmp:
            data_file = Path(tmp) / "tasks.json"
            with self.assertRaises(ValueError):
                add_task("", data_file=data_file)
            with self.assertRaises(ValueError):
                add_task("bad\nname", data_file=data_file)


if __name__ == "__main__":
    unittest.main()