"""todo.TodoService 的单元测试（unittest，虚构标题）。"""

import unittest

from todo import TodoService


class TodoServiceCreateTests(unittest.TestCase):
    def setUp(self) -> None:
        self.service = TodoService()

    def test_create_returns_todo_with_fields(self) -> None:
        todo = self.service.create("买牛奶", "下班顺路")
        self.assertEqual(todo.title, "买牛奶")
        self.assertEqual(todo.description, "下班顺路")
        self.assertIsInstance(todo.id, str)
        self.assertTrue(todo.id)

    def test_create_title_stripped(self) -> None:
        todo = self.service.create("  写周报  ")
        self.assertEqual(todo.title, "写周报")

    def test_create_empty_title_raises(self) -> None:
        with self.assertRaises(ValueError):
            self.service.create("   ")

    def test_create_default_description_empty(self) -> None:
        todo = self.service.create("开会")
        self.assertEqual(todo.description, "")

    def test_create_persists(self) -> None:
        self.service.create("买牛奶")
        self.service.create("写周报")
        self.assertEqual(len(self.service.items), 2)

    def test_create_generates_unique_ids(self) -> None:
        first = self.service.create("买牛奶")
        second = self.service.create("写周报")
        self.assertNotEqual(first.id, second.id)


if __name__ == "__main__":
    unittest.main()