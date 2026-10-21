"""Task 1: 创建待办事项（最小闭环：仅新增）。

单一职责实现：数据模型（TodoItem）+ 服务（TodoService.create + list_all）。
仅覆盖「仅创建」最小闭环，进程内存存储，Python 标准库，无第三方依赖。
不实现编辑/删除/完成/搜索/排序/持久化（YAGNI）。
"""

from dataclasses import dataclass


@dataclass
class TodoItem:
    """一条待办事项记录：唯一标识 + 事项名称 + 事项描述。"""

    id: int
    name: str
    description: str


class TodoService:
    """进程内存存储的待办事项服务，仅支持新增与读取见证。"""

    def __init__(self):
        self._items = []
        self._next_id = 1

    def create(self, name, description):
        """创建一条待办事项记录，返回带自增 id 的 TodoItem。

        名称与描述按原值保留，不做截断或转义处理。
        """
        item = TodoItem(id=self._next_id, name=name, description=description)
        self._items.append(item)
        self._next_id += 1
        return item

    def list_all(self):
        """返回所有已创建记录的列表（读取见证能力）。"""
        return list(self._items)
