from __future__ import annotations

from dataclasses import dataclass, field


@dataclass
class TodoItem:
    name: str
    description: str = ""

    def to_dict(self) -> dict:
        return {"name": self.name, "description": self.description}

    @classmethod
    def from_dict(cls, d: dict) -> TodoItem:
        return cls(name=d["name"], description=d.get("description", ""))