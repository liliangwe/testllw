from fastapi import APIRouter, Depends, Header
from sqlalchemy.orm import Session

from ..db import get_db
from ..models import User
from ..schemas import UserOut
from ..services.auth import get_current_user, require_admin

router = APIRouter(prefix="/api/users", tags=["users"])


@router.get("/me", response_model=UserOut)
def me(db: Session = Depends(get_db), x_user: str | None = Header(default=None)):
    return get_current_user(db, x_user)


@router.get("", response_model=list[UserOut])
def list_users(db: Session = Depends(get_db), x_user: str | None = Header(default=None)):
    require_admin(get_current_user(db, x_user))
    return db.query(User).order_by(User.id).all()