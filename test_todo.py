import unittest

from todo import TodoItem, TodoStore


class TodoStoreCreateTest(unittest.TestCase):
    def test_create_returns_item_with_given_name_and_description(self):
        store = TodoStore()
        item = store.create("买菜", "去超市买牛奶")
        self.assertIsInstance(item, TodoItem)
        self.assertEqual(item.name, "买菜")
        self.assertEqual(item.description, "去超市买牛奶")

    def test_name_non_empty_enforced(self):
        store = TodoStore()
        with self.assertRaises(ValueError):
            store.create("", "无名称")

    def test_name_non_string_enforced(self):
        store = TodoStore()
        with self.assertRaises(ValueError):
            store.create(123, "数字名称")
        with self.assertRaises(ValueError):
            store.create(None, "空名称")

    def test_description_non_string_enforced(self):
        store = TodoStore()
        with self.assertRaises(ValueError):
            store.create("任务", 123)
        with self.assertRaises(ValueError):
            store.create("任务", None)

    def test_duplicate_names_still_create(self):
        store = TodoStore()
        first = store.create("任务", "第一次")
        second = store.create("任务", "第二次")
        self.assertIsNot(first, second)
        self.assertEqual(first.name, second.name)
        self.assertEqual(first.description, "第一次")
        self.assertEqual(second.description, "第二次")

    def test_description_may_be_empty(self):
        store = TodoStore()
        item = store.create("只有名称", "")
        self.assertEqual(item.name, "只有名称")
        self.assertEqual(item.description, "")


if __name__ == "__main__":
    unittest.main()
