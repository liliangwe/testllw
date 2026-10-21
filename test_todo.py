import unittest

from todo import Todo, TodoService


class TodoServiceTest(unittest.TestCase):
    def test_create_returns_todo_with_nonempty_id(self):
        service = TodoService()
        todo = service.create("买牛奶")
        self.assertIsInstance(todo, Todo)
        self.assertIsInstance(todo.id, int)
        self.assertNotEqual(todo.id, "")
        self.assertIsNotNone(todo.id)

    def test_ids_are_unique(self):
        service = TodoService()
        first = service.create("第一项")
        second = service.create("第二项")
        self.assertNotEqual(first.id, second.id)

    def test_blank_name_raises_value_error(self):
        service = TodoService()
        for blank in ("", "   ", "\t\n"):
            with self.assertRaises(ValueError):
                service.create(blank)

    def test_non_string_name_raises_value_error(self):
        service = TodoService()
        for not_str in (None, 123, ["x"]):
            with self.assertRaises(ValueError):
                service.create(not_str)

    def test_description_defaults_to_empty_string(self):
        service = TodoService()
        todo = service.create("买牛奶")
        self.assertEqual(todo.description, "")

    def test_created_todo_appears_in_internal_list(self):
        service = TodoService()
        todo = service.create("买牛奶")
        self.assertIn(todo, service._todos)
        self.assertEqual(len(service._todos), 1)


if __name__ == "__main__":
    unittest.main()