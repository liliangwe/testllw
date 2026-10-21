import datetime as dt
from decimal import Decimal

from sqlalchemy import Date, DateTime, ForeignKey, Numeric, String, UniqueConstraint, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from .db import Base


class User(Base):
    __tablename__ = "users"
    id: Mapped[int] = mapped_column(primary_key=True)
    username: Mapped[str] = mapped_column(String(64), unique=True, nullable=False)
    role: Mapped[str] = mapped_column(String(16), nullable=False, default="viewer")  # admin | viewer
    created_at: Mapped[dt.datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())


class Employee(Base):
    __tablename__ = "employees"
    id: Mapped[int] = mapped_column(primary_key=True)
    employee_no: Mapped[str] = mapped_column(String(32), unique=True, nullable=False)
    name: Mapped[str] = mapped_column(String(64), nullable=False)
    department: Mapped[str | None] = mapped_column(String(128), nullable=True)
    position: Mapped[str | None] = mapped_column(String(128), nullable=True)
    email: Mapped[str | None] = mapped_column(String(128), nullable=True)
    phone: Mapped[str | None] = mapped_column(String(32), nullable=True)
    hire_date: Mapped[dt.date | None] = mapped_column(Date, nullable=True)
    status: Mapped[str] = mapped_column(String(16), nullable=False, default="active")  # active | inactive
    created_at: Mapped[dt.datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())
    updated_at: Mapped[dt.datetime] = mapped_column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())
    deleted_at: Mapped[dt.datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)

    budgets: Mapped[list["EmployeeCostBudget"]] = relationship(back_populates="employee")


class Project(Base):
    __tablename__ = "projects"
    id: Mapped[int] = mapped_column(primary_key=True)
    code: Mapped[str] = mapped_column(String(32), unique=True, nullable=False)
    name: Mapped[str] = mapped_column(String(128), nullable=False)
    created_at: Mapped[dt.datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())


class EmployeeCostBudget(Base):
    __tablename__ = "employee_cost_budgets"
    __table_args__ = (UniqueConstraint("employee_id", "project_id", "budget_period", name="uq_emp_proj_period"),)

    id: Mapped[int] = mapped_column(primary_key=True)
    employee_id: Mapped[int] = mapped_column(ForeignKey("employees.id"), nullable=False)
    project_id: Mapped[int] = mapped_column(ForeignKey("projects.id"), nullable=False)
    cost_center: Mapped[str | None] = mapped_column(String(128), nullable=True)
    budget_amount: Mapped[Decimal] = mapped_column(Numeric(18, 2), nullable=False)
    currency: Mapped[str] = mapped_column(String(8), nullable=False, default="CNY")
    budget_period: Mapped[str] = mapped_column(String(32), nullable=False)
    start_date: Mapped[dt.date | None] = mapped_column(Date, nullable=True)
    end_date: Mapped[dt.date | None] = mapped_column(Date, nullable=True)
    remark: Mapped[str | None] = mapped_column(String(512), nullable=True)
    created_at: Mapped[dt.datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())
    updated_at: Mapped[dt.datetime] = mapped_column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())

    employee: Mapped["Employee"] = relationship(back_populates="budgets")
    project: Mapped["Project"] = relationship()


class Whitelist(Base):
    __tablename__ = "whitelist"
    __table_args__ = (UniqueConstraint("user_id", "allowed_action", name="uq_user_action"),)

    id: Mapped[int] = mapped_column(primary_key=True)
    user_id: Mapped[int] = mapped_column(ForeignKey("users.id"), nullable=False)
    allowed_action: Mapped[str] = mapped_column(String(32), nullable=False)  # "*" 或特定动作
    created_by: Mapped[str | None] = mapped_column(String(64), nullable=True)
    created_at: Mapped[dt.datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())

    user: Mapped["User"] = relationship()