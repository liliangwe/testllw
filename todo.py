import argparse
import json
import os
from dataclasses import dataclass
from pathlib import Path


@dataclass
class TodoItem:
    name: str
    description: str = ""

    def to_dict(self) -> dict:
        return {"name": self.name, "description": self.description}

    @classmethod
    def from_dict(cls, d: dict) -> "TodoItem":
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


def main() -> None:
    parser = argparse.ArgumentParser(description="待办事项记录工具")
    subparsers = parser.add_subparsers(dest="command", required=True)

    add_parser = subparsers.add_parser("add", help="新增待办事项")
    add_parser.add_argument("--name", required=True, help="事项名称")
    add_parser.add_argument("--description", default="", help="事项描述")

    args = parser.parse_args()

    data_file = os.environ.get("TODO_DATA_FILE", str(Path.home() / ".todos.json"))
    mgr = TodoManager(data_file)

    if args.command == "add":
        item = TodoItem(name=args.name, description=args.description)
        mgr.add(item)
        print(f"已添加待办事项: {item.name}")
        if item.description:
            print(f"  描述: {item.description}")


if __name__ == "__main__":
    main()
