"""待办事项最小闭环：仅支持创建待办事项。"""

from dataclasses import dataclass
import uuid


@dataclass
class Todo:
    id: str
    title: str
    description: str


class TodoService:
    def __init__(self) -> None:
        self._items: list[Todo] = []

    def create(self, title: str, description: str = "") -> Todo:
        title = title.strip()
        if not title:
            raise ValueError("标题不能为空")
        todo = Todo(id=str(uuid.uuid4()), title=title, description=description)
        self._items.append(todo)
        return todo

    @property
    def items(self) -> list[Todo]:
        return list(self._items)