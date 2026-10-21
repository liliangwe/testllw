from fastapi import APIRouter, Depends, Header, HTTPException, Response, status
from sqlalchemy.orm import Session

from ..db import get_db
from ..models import Whitelist
from ..schemas import WhitelistCreate, WhitelistOut
from ..services.auth import get_current_user, require_admin

router = APIRouter(prefix="/api/whitelist", tags=["whitelist"])


@router.get("", response_model=list[WhitelistOut])
def list_whitelist(db: Session = Depends(get_db), x_user: str | None = Header(default=None)):
    require_admin(get_current_user(db, x_user))
    return db.query(Whitelist).order_by(Whitelist.id).all()


@router.post("", response_model=WhitelistOut, status_code=status.HTTP_201_CREATED)
def add_whitelist(
    payload: WhitelistCreate,
    db: Session = Depends(get_db),
    x_user: str | None = Header(default=None),
):
    user = get_current_user(db, x_user)
    require_admin(user)
    exists = (
        db.query(Whitelist)
        .filter(Whitelist.user_id == payload.user_id)
        .filter(Whitelist.allowed_action == payload.allowed_action)
        .first()
    )
    if exists:
        raise HTTPException(status_code=409, detail="该授权已存在")
    wl = Whitelist(
        user_id=payload.user_id,
        allowed_action=payload.allowed_action,
        created_by=user.username,
    )
    db.add(wl)
    db.commit()
    db.refresh(wl)
    return wl


@router.delete("/{whitelist_id}", status_code=status.HTTP_204_NO_CONTENT)
def remove_whitelist(
    whitelist_id: int,
    db: Session = Depends(get_db),
    x_user: str | None = Header(default=None),
):
    require_admin(get_current_user(db, x_user))
    wl = db.query(Whitelist).filter(Whitelist.id == whitelist_id).first()
    if wl is None:
        raise HTTPException(status_code=404, detail="授权不存在")
    db.delete(wl)
    db.commit()
    return Response(status_code=status.HTTP_204_NO_CONTENT)