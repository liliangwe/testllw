from fastapi import APIRouter, Depends, Header, HTTPException, Query, Response, status
from sqlalchemy.orm import Session, joinedload
from sqlalchemy.exc import IntegrityError

from ..db import get_db
from ..models import EmployeeCostBudget
from ..schemas import BudgetCreate, BudgetListOut, BudgetOut, BudgetUpdate
from ..services.auth import SENSITIVE_BUDGET_WRITE, enforce_sensitive, get_current_user

router = APIRouter(prefix="/api/budgets", tags=["budgets"])


@router.get("", response_model=BudgetListOut)
def list_budgets(
    employee_id: int | None = Query(default=None),
    project_id: int | None = Query(default=None),
    page: int = Query(default=1, ge=1),
    page_size: int = Query(default=20, ge=1, le=100),
    db: Session = Depends(get_db),
):
    q = db.query(EmployeeCostBudget).options(joinedload(EmployeeCostBudget.employee), joinedload(EmployeeCostBudget.project))
    if employee_id:
        q = q.filter(EmployeeCostBudget.employee_id == employee_id)
    if project_id:
        q = q.filter(EmployeeCostBudget.project_id == project_id)
    total = q.count()
    items = q.order_by(EmployeeCostBudget.id).offset((page - 1) * page_size).limit(page_size).all()
    return {"total": total, "items": items}


@router.post("", response_model=BudgetOut, status_code=status.HTTP_201_CREATED)
def create_budget(
    payload: BudgetCreate,
    db: Session = Depends(get_db),
    x_user: str | None = Header(default=None),
):
    user = get_current_user(db, x_user)
    enforce_sensitive(db, user, SENSITIVE_BUDGET_WRITE)
    budget = EmployeeCostBudget(**payload.model_dump())
    db.add(budget)
    try:
        db.commit()
    except IntegrityError:
        db.rollback()
        raise HTTPException(status_code=409, detail="同一员工+项目+期间已存在预算")
    db.refresh(budget)
    return budget


@router.patch("/{budget_id}", response_model=BudgetOut)
def update_budget(
    budget_id: int,
    payload: BudgetUpdate,
    db: Session = Depends(get_db),
    x_user: str | None = Header(default=None),
):
    user = get_current_user(db, x_user)
    enforce_sensitive(db, user, SENSITIVE_BUDGET_WRITE)
    budget = db.query(EmployeeCostBudget).filter(EmployeeCostBudget.id == budget_id).first()
    if budget is None:
        raise HTTPException(status_code=404, detail="预算不存在")
    for k, v in payload.model_dump(exclude_unset=True).items():
        setattr(budget, k, v)
    db.commit()
    db.refresh(budget)
    return budget


@router.delete("/{budget_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_budget(
    budget_id: int,
    db: Session = Depends(get_db),
    x_user: str | None = Header(default=None),
):
    user = get_current_user(db, x_user)
    enforce_sensitive(db, user, SENSITIVE_BUDGET_WRITE)
    budget = db.query(EmployeeCostBudget).filter(EmployeeCostBudget.id == budget_id).first()
    if budget is None:
        raise HTTPException(status_code=404, detail="预算不存在")
    db.delete(budget)
    db.commit()
    return Response(status_code=status.HTTP_204_NO_CONTENT)