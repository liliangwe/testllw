"""Task 1: 创建待办事项（最小闭环：仅新增）的单元测试。

遵循 TDD：本文件先于 todo.py 编写，用于记录 RED→GREEN 证据。
只覆盖简报列出的最小闭环范围：name/description 创建 + list_all 读取见证，以及
id 自增唯一、原值保留（含空描述）。不涉及编辑/删除/完成/搜索/排序/持久化。
"""

import unittest

from todo import TodoService


class TodoCreateTest(unittest.TestCase):
    """覆盖「仅创建」最小闭环的验收标准。"""

    def setUp(self):
        # 每个用例使用独立服务实例，保证内存存储隔离。
        self.service = TodoService()

    def test_create_increments_count_and_returns_matching_fields(self):
        """创建后记录数 +1，且返回记录的名称/描述与入参一致。"""
        self.assertEqual(len(self.service.list_all()), 0)

        record = self.service.create("买菜", "去超市买青菜和鸡蛋")

        self.assertEqual(len(self.service.list_all()), 1)
        self.assertEqual(record.name, "买菜")
        self.assertEqual(record.description, "去超市买青菜和鸡蛋")

    def test_multiple_creates_have_unique_incrementing_ids(self):
        """连续创建多条，id 唯一且自增，从 1 开始。"""
        first = self.service.create("事项一", "第一条描述")
        second = self.service.create("事项二", "第二条描述")
        third = self.service.create("事项三", "第三条描述")

        self.assertEqual(first.id, 1)
        self.assertEqual(second.id, 2)
        self.assertEqual(third.id, 3)

        ids = [r.id for r in self.service.list_all()]
        self.assertEqual(ids, [1, 2, 3])
        self.assertEqual(len(ids), len(set(ids)))

    def test_name_and_description_preserved_verbatim(self):
        """名称/描述保留原值（不截断、不转义丢失）。"""
        name = "a" * 300 + "尾字符!@#$%^&*()"
        description = "含中文、Emoji 🚀 与特殊字符 \t\n\"'\\ 及结尾空格   "

        record = self.service.create(name, description)

        self.assertEqual(record.name, name)
        self.assertEqual(record.description, description)

    def test_empty_description_is_allowed(self):
        """空描述允许创建，名称原值保留。"""
        record = self.service.create("仅有名称的事项", "")

        self.assertEqual(record.name, "仅有名称的事项")
        self.assertEqual(record.description, "")
        self.assertEqual(len(self.service.list_all()), 1)

    def test_list_all_faithfully_reflects_created_records(self):
        """list_all() 忠实反映已创建记录（数量与内容一致）。"""
        expected = [
            ("第一条", "描述 A"),
            ("第二条", "描述 B"),
            ("第三条", ""),
        ]
        for name, description in expected:
            self.service.create(name, description)

        records = self.service.list_all()

        self.assertEqual(len(records), 3)
        self.assertEqual([(r.name, r.description) for r in records], expected)


if __name__ == "__main__":
    unittest.main()
