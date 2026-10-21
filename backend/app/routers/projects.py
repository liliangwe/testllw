from fastapi import APIRouter, Depends, Header, HTTPException, status
from sqlalchemy.orm import Session

from ..db import get_db
from ..models import Project
from ..schemas import ProjectCreate, ProjectOut
from ..services.auth import get_current_user, require_admin

router = APIRouter(prefix="/api/projects", tags=["projects"])


@router.get("", response_model=list[ProjectOut])
def list_projects(db: Session = Depends(get_db)):
    return db.query(Project).order_by(Project.id).all()


@router.post("", response_model=ProjectOut, status_code=status.HTTP_201_CREATED)
def create_project(
    payload: ProjectCreate,
    db: Session = Depends(get_db),
    x_user: str | None = Header(default=None),
):
    require_admin(get_current_user(db, x_user))
    exists = db.query(Project).filter(Project.code == payload.code).first()
    if exists:
        raise HTTPException(status_code=409, detail="项目编号已存在")
    p = Project(**payload.model_dump())
    db.add(p)
    db.commit()
    db.refresh(p)
    return p