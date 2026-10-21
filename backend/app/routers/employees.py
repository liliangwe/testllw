from datetime import datetime, timezone

from fastapi import APIRouter, Depends, Header, HTTPException, Query, Response, status
from sqlalchemy.orm import Session

from ..db import get_db
from ..models import Employee
from ..schemas import EmployeeCreate, EmployeeListOut, EmployeeOut, EmployeeUpdate
from ..services.auth import SENSITIVE_DELETE_EMPLOYEE, enforce_sensitive, get_current_user

router = APIRouter(prefix="/api/employees", tags=["employees"])


def _get(db: Session, employee_no: str, include_deleted: bool) -> Employee:
    q = db.query(Employee).filter(Employee.employee_no == employee_no)
    if not include_deleted:
        q = q.filter(Employee.deleted_at.is_(None))
    emp = q.first()
    if emp is None:
        raise HTTPException(status_code=404, detail="员工不存在")
    return emp


@router.get("", response_model=EmployeeListOut)
def list_employees(
    q: str | None = Query(default=None),
    department: str | None = Query(default=None),
    page: int = Query(default=1, ge=1),
    page_size: int = Query(default=20, ge=1, le=100),
    db: Session = Depends(get_db),
):
    query = db.query(Employee).filter(Employee.deleted_at.is_(None))
    if q:
        like = f"%{q}%"
        query = query.filter((Employee.name.like(like)) | (Employee.employee_no.like(like)))
    if department:
        query = query.filter(Employee.department == department)
    total = query.count()
    items = query.order_by(Employee.id).offset((page - 1) * page_size).limit(page_size).all()
    return {"total": total, "items": items}


@router.get("/{employee_no}", response_model=EmployeeOut)
def get_employee(
    employee_no: str,
    include_deleted: bool = Query(default=False),
    db: Session = Depends(get_db),
):
    return _get(db, employee_no, include_deleted)


@router.post("", response_model=EmployeeOut, status_code=status.HTTP_201_CREATED)
def create_employee(payload: EmployeeCreate, db: Session = Depends(get_db)):
    exists = db.query(Employee).filter(Employee.employee_no == payload.employee_no).first()
    if exists:
        raise HTTPException(status_code=409, detail="工号已存在")
    emp = Employee(**payload.model_dump())
    db.add(emp)
    db.commit()
    db.refresh(emp)
    return emp


@router.patch("/{employee_no}", response_model=EmployeeOut)
def update_employee(
    employee_no: str, payload: EmployeeUpdate, db: Session = Depends(get_db)
):
    emp = _get(db, employee_no, include_deleted=False)
    for k, v in payload.model_dump(exclude_unset=True).items():
        setattr(emp, k, v)
    emp.updated_at = datetime.now(timezone.utc)
    db.commit()
    db.refresh(emp)
    return emp


@router.delete("/{employee_no}", status_code=status.HTTP_204_NO_CONTENT)
def delete_employee(
    employee_no: str,
    db: Session = Depends(get_db),
    x_user: str | None = Header(default=None),
):
    user = get_current_user(db, x_user)
    enforce_sensitive(db, user, SENSITIVE_DELETE_EMPLOYEE)
    emp = _get(db, employee_no, include_deleted=False)
    emp.deleted_at = datetime.now(timezone.utc)
    db.commit()
    return Response(status_code=status.HTTP_204_NO_CONTENT)