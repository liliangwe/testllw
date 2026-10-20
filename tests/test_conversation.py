"""对话模块单元测试。

测试范围：
- IntentParser.is_create_intent  (S02)
- EntityExtractor.extract         (S03)
- ConversationService.create_from_text (S01)
"""

import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

from src.todo_cli.conversation import (
    ConversationService,
    EntityExtractor,
    IntentParser,
)


# ── IntentParser ──────────────────────────────────────────────

class TestIntentParser(unittest.TestCase):
    """意图识别单元测试。"""

    def setUp(self) -> None:
        self.parser = IntentParser()

    # ── 正例：检测到创建意图 ──

    def test_should_detect_添加(self) -> None:
        self.assertTrue(self.parser.is_create_intent("添加待办写周报"))

    def test_should_detect_创建(self) -> None:
        self.assertTrue(self.parser.is_create_intent("创建新任务"))

    def test_should_detect_记录(self) -> None:
        self.assertTrue(self.parser.is_create_intent("记录一下明天开会"))

    def test_should_detect_帮我记(self) -> None:
        self.assertTrue(self.parser.is_create_intent("帮我记一下写周报"))

    def test_should_detect_todo(self) -> None:
        self.assertTrue(self.parser.is_create_intent("todo buy milk"))

    def test_should_detect_add(self) -> None:
        self.assertTrue(self.parser.is_create_intent("add a task"))

    def test_should_detect_记一下(self) -> None:
        self.assertTrue(self.parser.is_create_intent("记一下：写周报"))

    # ── 反例：非创建意图 ──

    def test_should_reject_random_text(self) -> None:
        self.assertFalse(self.parser.is_create_intent("今天天气怎么样"))

    def test_should_reject_empty(self) -> None:
        self.assertFalse(self.parser.is_create_intent(""))

    def test_should_reject_whitespace_only(self) -> None:
        self.assertFalse(self.parser.is_create_intent("   "))

    # ── 排除词：明确非创建意图 ──

    def test_should_reject_查看(self) -> None:
        self.assertFalse(self.parser.is_create_intent("查看待办事项"))

    def test_should_reject_列出(self) -> None:
        self.assertFalse(self.parser.is_create_intent("列出所有任务"))

    def test_should_reject_删除(self) -> None:
        self.assertFalse(self.parser.is_create_intent("删除写周报"))

    def test_should_reject_完成(self) -> None:
        self.assertFalse(self.parser.is_create_intent("完成写周报"))

    def test_should_reject_list(self) -> None:
        self.assertFalse(self.parser.is_create_intent("list tasks"))

    def test_should_reject_done(self) -> None:
        self.assertFalse(self.parser.is_create_intent("done with report"))

    # ── 边界：创建词+排除词同时在文本 ──

    def test_should_reject_when_exclusion_present_with_intent(self) -> None:
        """若文本同时含创建词与排除词，应拒绝（排除优先）。"""
        self.assertFalse(self.parser.is_create_intent("查看创建的待办事项"))


# ── EntityExtractor ───────────────────────────────────────────

class TestEntityExtractor(unittest.TestCase):
    """实体提取单元测试。"""

    def setUp(self) -> None:
        self.extractor = EntityExtractor()

    # ── 仅名称 ──

    def test_should_extract_name_only_添加(self) -> None:
        result = self.extractor.extract("添加写周报")
        self.assertEqual(result["name"], "写周报")
        self.assertEqual(result["description"], "")

    def test_should_extract_name_only_创建(self) -> None:
        result = self.extractor.extract("创建新功能")
        self.assertEqual(result["name"], "新功能")
        self.assertEqual(result["description"], "")

    def test_should_extract_name_only_记录(self) -> None:
        result = self.extractor.extract("记录买菜")
        self.assertEqual(result["name"], "买菜")
        self.assertEqual(result["description"], "")

    def test_should_extract_name_only_帮我记(self) -> None:
        result = self.extractor.extract("帮我记一下 写周报")
        self.assertEqual(result["name"], "写周报")
        self.assertEqual(result["description"], "")

    def test_should_extract_name_only_todo(self) -> None:
        result = self.extractor.extract("todo buy milk")
        self.assertEqual(result["name"], "buy milk")
        self.assertEqual(result["description"], "")

    # ── 名称+描述 ──

    def test_should_extract_name_and_description_中文逗号(self) -> None:
        result = self.extractor.extract("添加写周报，描述：周五前完成")
        self.assertEqual(result["name"], "写周报")
        self.assertEqual(result["description"], "周五前完成")

    def test_should_extract_name_and_description_英文逗号(self) -> None:
        result = self.extractor.extract("添加写周报, 描述: 周五前完成")
        self.assertEqual(result["name"], "写周报")
        self.assertEqual(result["description"], "周五前完成")

    def test_should_extract_with_备注_keyword(self) -> None:
        result = self.extractor.extract("记录买菜，备注：去菜市场买青菜和鸡蛋")
        self.assertEqual(result["name"], "买菜")
        self.assertEqual(result["description"], "去菜市场买青菜和鸡蛋")

    def test_should_extract_with_说明_keyword(self) -> None:
        result = self.extractor.extract("新增代码审查，说明：完成后提交PR")
        self.assertEqual(result["name"], "代码审查")
        self.assertEqual(result["description"], "完成后提交PR")

    # ── 描述为空 ──

    def test_should_return_empty_description_when_not_provided(self) -> None:
        result = self.extractor.extract("记一下写周报")
        self.assertEqual(result["name"], "写周报")
        self.assertEqual(result["description"], "")

    # ── 带量词的输入 ──

    def test_should_strip_quantifier_一个(self) -> None:
        result = self.extractor.extract("添加一个写周报")
        self.assertEqual(result["name"], "写周报")

    def test_should_strip_quantifier_一下(self) -> None:
        result = self.extractor.extract("记录一下买菜")
        self.assertEqual(result["name"], "买菜")

    # ── 无法提取名称 ──

    def test_should_raise_when_name_not_extractable(self) -> None:
        with self.assertRaises(ValueError):
            self.extractor.extract("添加")
        with self.assertRaises(ValueError):
            self.extractor.extract("新增")
        with self.assertRaises(ValueError):
            self.extractor.extract("todo")


# ── ConversationService ───────────────────────────────────────

class TestConversationService(unittest.TestCase):
    """对话服务集成测试。"""

    def setUp(self) -> None:
        self.service = ConversationService()

    def _temp_data_file(self) -> Path:
        return Path(tempfile.mkdtemp()) / "tasks.json"

    # ── 正常创建 ──

    def test_should_create_task_with_name(self) -> None:
        data_file = self._temp_data_file()
        result = self.service.create_from_text(
            "添加写周报", data_file=data_file,
        )
        self.assertEqual(result["name"], "写周报")
        self.assertEqual(result["description"], "")
        self.assertIn("created_at", result)

    def test_should_create_task_with_name_and_description(self) -> None:
        data_file = self._temp_data_file()
        result = self.service.create_from_text(
            "帮我记一下 写周报，描述：周五前完成",
            data_file=data_file,
        )
        self.assertEqual(result["name"], "写周报")
        self.assertEqual(result["description"], "周五前完成")

    def test_should_use_default_data_file(self) -> None:
        data_file = self._temp_data_file()
        with patch("src.todo_cli.main.DEFAULT_DATA_FILE", data_file):
            result = self.service.create_from_text("添加买菜")
            self.assertIsNotNone(result)
            self.assertEqual(result["name"], "买菜")

    # ── 异常：非创建意图 ──

    def test_should_raise_for_non_intent_text(self) -> None:
        data_file = self._temp_data_file()
        with self.assertRaises(ValueError) as ctx:
            self.service.create_from_text(
                "今天天气怎么样", data_file=data_file,
            )
        self.assertIn("无法识别为创建任务意图", str(ctx.exception))

    def test_should_raise_for_empty_text(self) -> None:
        data_file = self._temp_data_file()
        with self.assertRaises(ValueError) as ctx:
            self.service.create_from_text("", data_file=data_file)
        self.assertIn("输入文本不能为空", str(ctx.exception))

    def test_should_raise_for_whitespace_only(self) -> None:
        data_file = self._temp_data_file()
        with self.assertRaises(ValueError):
            self.service.create_from_text("   ", data_file=data_file)

    # ── 异常：无法提取名称 ──

    def test_should_raise_when_name_not_extractable(self) -> None:
        data_file = self._temp_data_file()
        with self.assertRaises(ValueError) as ctx:
            self.service.create_from_text(
                "添加", data_file=data_file,
            )
        self.assertIn("无法从事项描述中提取名称", str(ctx.exception))

    # ── 异常：名称校验失败 ──

    def test_should_raise_for_invalid_name(self) -> None:
        data_file = self._temp_data_file()
        with self.assertRaises(ValueError):
            self.service.create_from_text(
                "添加 bad\nname", data_file=data_file,
            )


if __name__ == "__main__":
    unittest.main()