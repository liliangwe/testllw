class TodoItem:
    """一条待办事项记录，包含 name 和 description 两个字段。"""

    def __init__(self, name, description):
        self.name = name
        self.description = description


class TodoStore:
    """仅创建并返回待办事项，不持有集合。"""

    def create(self, name, description):
        if not isinstance(name, str) or not isinstance(description, str):
            raise ValueError("name 与 description 必须为字符串")
        if name == "":
            raise ValueError("name 不能为空")
        return TodoItem(name, description)
