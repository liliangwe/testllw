import unittest
from todo import TodoItem


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


if __name__ == "__main__":
    unittest.main()
