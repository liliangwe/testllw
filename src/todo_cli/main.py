"""Todo CLI 入口模块。"""

import json
import os
import string
from datetime import datetime, timezone
from pathlib import Path
from tempfile import NamedTemporaryFile

DEFAULT_DATA_DIR = Path.home() / ".todo"
DEFAULT_DATA_FILE = DEFAULT_DATA_DIR / "tasks.json"


def validate_name(name: str) -> None:
    """校验事项名称：非空且不含控制字符。"""
    if not name:
        raise ValueError("事项名称不能为空")
    if any(c in name for c in string.whitespace):
        raise ValueError("事项名称不能包含空白字符")


def load_tasks(filepath: Path) -> list[dict]:
    """从 JSON 文件加载任务列表，文件不存在时返回空列表。"""
    if not filepath.exists():
        return []
    return json.loads(filepath.read_text(encoding="utf-8"))


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


def main() -> None:
    """CLI 入口，由 pyproject.toml 的 console_scripts 调用。"""
    pass