from dataclasses import dataclass
from itertools import count


@dataclass(frozen=True)
class Todo:
    id: int
    name: str
    description: str = ""


class TodoService:
    def __init__(self):
        self._todos = []
        self._counter = count(1)

    def create(self, name, description=""):
        if not isinstance(name, str) or not name.strip():
            raise ValueError("name must be a non-blank string")
        todo = Todo(id=next(self._counter), name=name, description=description)
        self._todos.append(todo)
        return todo