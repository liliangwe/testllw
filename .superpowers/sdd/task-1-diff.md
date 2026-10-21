# Review Package: Task 1 (创建待办事项)

> 说明：本仓库受「Git 只读」约束，子代理未产生 commit，新文件以未跟踪状态存在。
> 因此本 diff 由 `git diff --no-index /dev/null <file>` 人工合成（等价于新增文件视图）。

## 变更文件清单（新增）
- todo.py
- test_todo.py

## 完整差异视图

### todo.py（新增）
```diff
--- /dev/null
+++ b/todo.py
@@ -0,0 +1,28 @@
+"""待办事项最小闭环：仅支持创建待办事项。"""
+
+from dataclasses import dataclass
+import uuid
+
+
+@dataclass
+class Todo:
+    id: str
+    title: str
+    description: str
+
+
+class TodoService:
+    def __init__(self) -> None:
+        self._items: list[Todo] = []
+
+    def create(self, title: str, description: str = "") -> Todo:
+        title = title.strip()
+        if not title:
+            raise ValueError("标题不能为空")
+        todo = Todo(id=str(uuid.uuid4()), title=title, description=description)
+        self._items.append(todo)
+        return todo
+
+    @property
+    def items(self) -> list[Todo]:
+        return list(self._items)
+```

### test_todo.py（新增）
```diff
--- /dev/null
+++ b/test_todo.py
@@ -0,0 +1,43 @@
+"""todo.TodoService 的单元测试（unittest，虚构标题）。"""
+
+import unittest
+
+from todo import TodoService
+
+
+class TodoServiceCreateTests(unittest.TestCase):
+    def setUp(self) -> None:
+        self.service = TodoService()
+
+    def test_create_returns_todo_with_fields(self) -> None:
+        todo = self.service.create("买牛奶", "下班顺路")
+        self.assertEqual(todo.title, "买牛奶")
+        self.assertEqual(todo.description, "下班顺路")
+        self.assertIsInstance(todo.id, str)
+        self.assertTrue(todo.id)
+
+    def test_create_title_stripped(self) -> None:
+        todo = self.service.create("  写周报  ")
+        self.assertEqual(todo.title, "写周报")
+
+    def test_create_empty_title_raises(self) -> None:
+        with self.assertRaises(ValueError):
+            self.service.create("   ")
+
+    def test_create_default_description_empty(self) -> None:
+        todo = self.service.create("开会")
+        self.assertEqual(todo.description, "")
+
+    def test_create_persists(self) -> None:
+        self.service.create("买牛奶")
+        self.service.create("写周报")
+        self.assertEqual(len(self.service.items), 2)
+
+    def test_create_generates_unique_ids(self) -> None:
+        first = self.service.create("买牛奶")
+        second = self.service.create("写周报")
+        self.assertNotEqual(first.id, second.id)
+
+
+if __name__ == "__main__":
+    unittest.main()
+```