from __future__ import annotations

import json
from dataclasses import dataclass, field
from pathlib import Path


@dataclass
class TodoItem:
    name: str
    description: str = ""

    def to_dict(self) -> dict:
        return {"name": self.name, "description": self.description}

    @classmethod
    def from_dict(cls, d: dict) -> TodoItem:
        return cls(name=d["name"], description=d.get("description", ""))


class TodoManager:
    def __init__(self, filepath: str) -> None:
        self.filepath = Path(filepath)

    def add(self, item: TodoItem) -> None:
        items: list[dict] = []
        if self.filepath.exists():
            with open(self.filepath, "r", encoding="utf-8") as f:
                items = json.load(f)
        items.append(item.to_dict())
        with open(self.filepath, "w", encoding="utf-8") as f:
            json.dump(items, f, ensure_ascii=False, indent=2)
