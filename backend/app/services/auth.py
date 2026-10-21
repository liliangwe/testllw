from fastapi import HTTPException, status

from ..db import SessionLocal
from ..models import User, Whitelist

# 敏感操作枚举 —— 全项目统一引用
SENSITIVE_DELETE_EMPLOYEE = "employee.delete"
SENSITIVE_IMPORT_EMPLOYEE = "employee.import"
SENSITIVE_BUDGET_WRITE = "budget.write"
SENSITIVE_ALL = "*"

_SENSITIVE_ACTIONS = {
    SENSITIVE_DELETE_EMPLOYEE, SENSITIVE_IMPORT_EMPLOYEE, SENSITIVE_BUDGET_WRITE,
}


def get_current_user(db, x_user: str | None) -> User:
    """从请求头 X-User 解析当前用户；未提供时回退为 role=viewer 的匿名用户。"""
    user = None
    if x_user:
        user = db.query(User).filter(User.username == x_user).first()
    if user is None:
        user = db.query(User).filter(User.username == "viewer").first()
    if user is None:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="未知用户")
    return user


def require_admin(user: User) -> None:
    if user.role != "admin":
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="需要管理员权限")


def is_whitelisted(db, user: User, action: str) -> bool:
    hits = (
        db.query(Whitelist)
        .filter(Whitelist.user_id == user.id)
        .filter(Whitelist.allowed_action.in_([SENSITIVE_ALL, action]))
        .all()
    )
    return len(hits) > 0


def enforce_sensitive(db, user: User, action: str) -> None:
    if not is_whitelisted(db, user, action):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail=f"敏感操作需授权：{action}",
        )