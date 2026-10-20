"""Todo CLI 入口模块。"""

from __future__ import annotations

import argparse
import json
import os
import sys
from datetime import datetime, timezone
from pathlib import Path
from tempfile import NamedTemporaryFile

DEFAULT_DATA_DIR = Path.home() / ".todo"
DEFAULT_DATA_FILE = DEFAULT_DATA_DIR / "tasks.json"

# 事项名称中禁止出现的控制字符（保留普通空格 ' ' 为合法字符）
_CONTROL_CHARS = "\t\n\r\x0b\x0c"


def validate_name(name: str) -> None:
    """校验事项名称：非空且不含控制字符（空格合法）。"""
    if not name:
        raise ValueError("事项名称不能为空")
    if any(c in name for c in _CONTROL_CHARS):
        raise ValueError("事项名称不能包含控制字符")


def load_tasks(filepath: Path) -> list[dict]:
    """从 JSON 文件加载任务列表，文件不存在或损坏时返回空列表。"""
    if not filepath.exists():
        return []
    try:
        return json.loads(filepath.read_text(encoding="utf-8"))
    except json.JSONDecodeError:
        return []


def save_tasks(filepath: Path, tasks: list[dict]) -> None:
    """原子写入：先写到同目录临时文件，再 os.replace。"""
    filepath.parent.mkdir(parents=True, exist_ok=True)
    tmp = NamedTemporaryFile(
        mode="w",
        encoding="utf-8",
        dir=filepath.parent,
        delete=False,
        suffix=".tmp",
    )
    try:
        json.dump(tasks, tmp, ensure_ascii=False, indent=2)
        tmp.flush()
        os.fsync(tmp.fileno())
    finally:
        tmp.close()
    os.replace(tmp.name, filepath)


def add_task(
    name: str,
    description: str = "",
    data_file: Path | None = None,
) -> dict:
    """新增一条待办事项。加载→验证→追加→原子保存，返回新任务 dict。"""
    validate_name(name)

    if data_file is None:
        data_file = DEFAULT_DATA_FILE

    task: dict = {
        "name": name,
        "description": description,
        "created_at": datetime.now(timezone.utc).isoformat(),
    }

    tasks = load_tasks(data_file)
    tasks.append(task)
    save_tasks(data_file, tasks)
    return task


def build_parser() -> argparse.ArgumentParser:
    """构建命令行参数解析器。"""
    parser = argparse.ArgumentParser(
        prog="todo",
        description="日常待办事项管理工具",
    )
    subparsers = parser.add_subparsers(dest="command", required=True)
    add_parser = subparsers.add_parser("add", help="新增待办事项")
    add_parser.add_argument(
        "-n", "--name",
        required=True,
        help="事项名称",
    )
    add_parser.add_argument(
        "-d", "--description",
        default="",
        help="事项描述",
    )
    chat_parser = subparsers.add_parser("chat", help="对话创建待办事项")
    chat_parser.add_argument(
        "text",
        nargs="+",
        help="自然语言文本",
    )
    return parser


def main() -> None:
    """CLI 入口。"""
    parser = build_parser()
    try:
        args = parser.parse_args()
    except SystemExit as exc:
        if exc.code != 0:
            sys.exit(1)
        sys.exit(0)

    if args.command == "add":
        try:
            task = add_task(name=args.name, description=args.description)
        except ValueError as exc:
            print(f"❌ {exc}", file=sys.stderr)
            sys.exit(1)
        except OSError as exc:
            print(f"❌ 文件写入失败: {exc}", file=sys.stderr)
            sys.exit(1)
        print(f"✅ 已添加待办: {task['name']}")
        return

    if args.command == "chat":
        text = " ".join(args.text)
        from .conversation import ConversationService  # noqa: E402 惰性导入避免循环依赖
        service = ConversationService()
        try:
            task = service.create_from_text(text)
        except ValueError as exc:
            print(f"❌ {exc}", file=sys.stderr)
            sys.exit(1)
        except OSError as exc:
            print(f"❌ 文件写入失败: {exc}", file=sys.stderr)
            sys.exit(1)
        print(f"✅ 已添加待办: {task['name']}")
        return

    parser.print_help(sys.stderr)
    sys.exit(1)