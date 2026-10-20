import json
import os
import subprocess
import tempfile
import unittest
from pathlib import Path
from todo import TodoItem, TodoManager


class TestTodoItem(unittest.TestCase):
    def test_create_item_with_name_and_description(self):
        item = TodoItem(name="买菜", description="去超市买青菜和鸡蛋")
        self.assertEqual(item.name, "买菜")
        self.assertEqual(item.description, "去超市买青菜和鸡蛋")

    def test_to_dict_roundtrip(self):
        item = TodoItem(name="写周报", description="完成本周工作总结")
        d = item.to_dict()
        self.assertEqual(d, {"name": "写周报", "description": "完成本周工作总结"})
        restored = TodoItem.from_dict(d)
        self.assertEqual(restored.name, "写周报")
        self.assertEqual(restored.description, "完成本周工作总结")

    def test_default_description_empty_string(self):
        item = TodoItem(name="开会")
        self.assertEqual(item.name, "开会")
        self.assertEqual(item.description, "")


class TestTodoManager(unittest.TestCase):
    def setUp(self):
        self.tmpdir = tempfile.mkdtemp()
        self.filepath = Path(self.tmpdir) / "todos.json"

    def tearDown(self):
        if self.filepath.exists():
            self.filepath.unlink()
        os.rmdir(self.tmpdir)

    def test_add_to_empty_file_creates_list(self):
        mgr = TodoManager(str(self.filepath))
        item = TodoItem(name="买菜", description="去超市")
        mgr.add(item)

        self.assertTrue(self.filepath.exists())
        with open(self.filepath, "r", encoding="utf-8") as f:
            data = json.load(f)
        self.assertEqual(len(data), 1)
        self.assertEqual(data[0]["name"], "买菜")
        self.assertEqual(data[0]["description"], "去超市")

    def test_add_appends_to_existing_file(self):
        existing = [
            {"name": "写周报", "description": "完成本周总结"},
        ]
        with open(self.filepath, "w", encoding="utf-8") as f:
            json.dump(existing, f, ensure_ascii=False)

        mgr = TodoManager(str(self.filepath))
        mgr.add(TodoItem(name="开会", description="下午3点站会"))

        with open(self.filepath, "r", encoding="utf-8") as f:
            data = json.load(f)
        self.assertEqual(len(data), 2)
        self.assertEqual(data[1]["name"], "开会")

    def test_file_not_exist_creates_new(self):
        mgr = TodoManager(str(self.filepath))
        self.assertFalse(self.filepath.exists())
        mgr.add(TodoItem(name="test"))
        self.assertTrue(self.filepath.exists())


class TestTodoCLI(unittest.TestCase):
    def setUp(self):
        self.tmpdir = tempfile.mkdtemp()
        self.datafile = Path(self.tmpdir) / "todos.json"
        self.env = {**os.environ, "TODO_DATA_FILE": str(self.datafile)}

    def tearDown(self):
        if self.datafile.exists():
            self.datafile.unlink()
        os.rmdir(self.tmpdir)

    def test_add_subcommand_prints_success(self):
        result = subprocess.run(
            ["python3", "todo.py", "add", "--name", "买菜", "--description", "去超市"],
            capture_output=True,
            text=True,
            env=self.env,
        )
        self.assertEqual(result.returncode, 0)
        self.assertIn("已添加待办事项", result.stdout)
        self.assertIn("买菜", result.stdout)

    def test_add_without_description_defaults_empty(self):
        result = subprocess.run(
            ["python3", "todo.py", "add", "--name", "开会"],
            capture_output=True,
            text=True,
            env=self.env,
        )
        self.assertEqual(result.returncode, 0)
        self.assertTrue(self.datafile.exists())
        with open(self.datafile, "r", encoding="utf-8") as f:
            data = json.load(f)
        self.assertEqual(data[0]["description"], "")

    def test_add_missing_name_shows_error(self):
        result = subprocess.run(
            ["python3", "todo.py", "add", "--description", "desc only"],
            capture_output=True,
            text=True,
            env=self.env,
        )
        self.assertNotEqual(result.returncode, 0)


if __name__ == "__main__":
    unittest.main()
