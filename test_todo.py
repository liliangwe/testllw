import unittest

from todo import TodoItem, TodoStore


class TodoStoreCreateTest(unittest.TestCase):
    def test_create_returns_item_with_correct_fields(self):
        store = TodoStore()
        item = store.create("buy milk", "2 cartons")
        self.assertIsInstance(item, TodoItem)
        self.assertEqual(item.name, "buy milk")
        self.assertEqual(item.description, "2 cartons")

    def test_create_default_description_empty(self):
        store = TodoStore()
        item = store.create("task only")
        self.assertEqual(item.description, "")

    def test_ids_auto_increment_and_are_distinct(self):
        store = TodoStore()
        first = store.create("a")
        second = store.create("b")
        third = store.create("c")
        self.assertEqual(first.id, 1)
        self.assertEqual(second.id, 2)
        self.assertEqual(third.id, 3)

    def test_blank_name_raises_value_error(self):
        store = TodoStore()
        for blank in ("", "   ", "\t", "\n"):
            with self.subTest(blank=repr(blank)):
                with self.assertRaises(ValueError):
                    store.create(blank)

    def test_created_item_retrievable_from_items(self):
        store = TodoStore()
        item = store.create("walk dog", "morning")
        self.assertIn(item, store.items)
        self.assertEqual(len(store.items), 1)


if __name__ == "__main__":
    unittest.main()