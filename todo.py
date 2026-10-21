from dataclasses import dataclass


@dataclass
class TodoItem:
    id: int
    name: str
    description: str = ""


class TodoStore:
    def __init__(self) -> None:
        self._items: list[TodoItem] = []
        self._next_id: int = 1

    @property
    def items(self) -> list[TodoItem]:
        return self._items

    def create(self, name: str, description: str = "") -> TodoItem:
        if not isinstance(name, str) or not name.strip():
            raise ValueError("name must be a non-blank string")
        item = TodoItem(id=self._next_id, name=name, description=description)
        self._next_id += 1
        self._items.append(item)
        return item