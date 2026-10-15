"""CLI 层单元测试。"""

import json
import tempfile
import unittest
from io import StringIO
from pathlib import Path
from unittest.mock import patch

from src.todo_cli.main import build_parser, main


class TestBuildParser(unittest.TestCase):
    """build_parser 测试。"""

    def test_add_requires_name(self) -> None:
        """-n 为必填参数，缺失时报错。"""
        parser = build_parser()
        with self.assertRaises(SystemExit):
            parser.parse_args(["add"])

    def test_add_accepts_name_and_description(self) -> None:
        """正确解析 name 和 description。"""
        parser = build_parser()
        args = parser.parse_args(["add", "-n", "写周报", "-d", "周五前完成"])
        self.assertEqual(args.name, "写周报")
        self.assertEqual(args.description, "周五前完成")

    def test_add_description_defaults_to_empty(self) -> None:
        """不传 -d 时 description 默认为空字符串。"""
        parser = build_parser()
        args = parser.parse_args(["add", "-n", "买菜"])
        self.assertEqual(args.name, "买菜")
        self.assertEqual(args.description, "")

    def test_invalid_command_exits(self) -> None:
        """不传子命令时报错退出。"""
        parser = build_parser()
        with self.assertRaises(SystemExit):
            parser.parse_args([])


class TestMain(unittest.TestCase):
    """main() 集成测试。"""

    def test_add_writes_task_and_prints_success(self) -> None:
        """正常 add 写入文件并输出确认信息。"""
        with tempfile.TemporaryDirectory() as tmp:
            data_file = Path(tmp) / "tasks.json"
            test_args = ["todo", "add", "-n", "写周报", "-d", "周五前"]
            with patch("sys.argv", test_args), \
                 patch("src.todo_cli.main.DEFAULT_DATA_FILE", data_file), \
                 patch("sys.stdout", new_callable=StringIO) as mock_stdout:
                main()
            output = mock_stdout.getvalue()
            self.assertIn("已添加待办", output)
            self.assertIn("写周报", output)
            tasks = json.loads(data_file.read_text(encoding="utf-8"))
            self.assertEqual(len(tasks), 1)
            self.assertEqual(tasks[0]["name"], "写周报")
            self.assertEqual(tasks[0]["description"], "周五前")

    def test_add_missing_name_exits(self) -> None:
        """缺 -n 参数时退出码为 1，打印错误。"""
        with patch("sys.argv", ["todo", "add"]), \
             patch("sys.stderr", new_callable=StringIO) as mock_stderr:
            with self.assertRaises(SystemExit) as cm:
                main()
            self.assertEqual(cm.exception.code, 1)
            self.assertIn("name", mock_stderr.getvalue().lower())

    def test_add_without_description_uses_empty(self) -> None:
        """不传 -d 时 description 为空字符串。"""
        with tempfile.TemporaryDirectory() as tmp:
            data_file = Path(tmp) / "tasks.json"
            test_args = ["todo", "add", "-n", "买菜"]
            with patch("sys.argv", test_args), \
                 patch("src.todo_cli.main.DEFAULT_DATA_FILE", data_file), \
                 patch("sys.stdout", new_callable=StringIO):
                main()
            tasks = json.loads(data_file.read_text(encoding="utf-8"))
            self.assertEqual(tasks[0]["description"], "")

    def test_add_invalid_name_exits(self) -> None:
        """名称含非法字符时退出码为 1。"""
        test_args = ["todo", "add", "-n", "bad\nname"]
        with patch("sys.argv", test_args), \
             patch("sys.stderr", new_callable=StringIO):
            with self.assertRaises(SystemExit) as cm:
                main()
            self.assertEqual(cm.exception.code, 1)

    def test_invalid_command_exits(self) -> None:
        """不传子命令时退出码为 1。"""
        with patch("sys.argv", ["todo"]), \
             patch("sys.stderr", new_callable=StringIO):
            with self.assertRaises(SystemExit) as cm:
                main()
            self.assertEqual(cm.exception.code, 1)


if __name__ == "__main__":
    unittest.main()