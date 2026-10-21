import datetime as dt
from decimal import Decimal
from typing import Literal

from pydantic import BaseModel, ConfigDict, Field


class UserOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    id: int
    username: str
    role: str


class EmployeeBase(BaseModel):
    employee_no: str = Field(min_length=1, max_length=32)
    name: str = Field(min_length=1, max_length=64)
    department: str | None = None
    position: str | None = None
    email: str | None = None
    phone: str | None = None
    hire_date: dt.date | None = None
    status: Literal["active", "inactive"] = "active"


class EmployeeCreate(EmployeeBase):
    pass


class EmployeeUpdate(BaseModel):
    name: str | None = None
    department: str | None = None
    position: str | None = None
    email: str | None = None
    phone: str | None = None
    hire_date: dt.date | None = None
    status: Literal["active", "inactive"] | None = None


class EmployeeOut(EmployeeBase):
    model_config = ConfigDict(from_attributes=True)
    id: int
    created_at: dt.datetime
    updated_at: dt.datetime


class EmployeeListOut(BaseModel):
    total: int
    items: list[EmployeeOut]


class ProjectBase(BaseModel):
    code: str = Field(min_length=1, max_length=32)
    name: str = Field(min_length=1, max_length=128)


class ProjectCreate(ProjectBase):
    pass


class ProjectOut(ProjectBase):
    model_config = ConfigDict(from_attributes=True)
    id: int


class BudgetBase(BaseModel):
    employee_id: int
    project_id: int
    cost_center: str | None = None
    budget_amount: Decimal = Field(gt=0)
    currency: str = "CNY"
    budget_period: str = Field(min_length=1, max_length=32)
    start_date: dt.date | None = None
    end_date: dt.date | None = None
    remark: str | None = None


class BudgetCreate(BudgetBase):
    pass


class BudgetUpdate(BaseModel):
    cost_center: str | None = None
    budget_amount: Decimal | None = Field(default=None, gt=0)
    currency: str | None = None
    budget_period: str | None = None
    start_date: dt.date | None = None
    end_date: dt.date | None = None
    remark: str | None = None


class BudgetOut(BudgetBase):
    model_config = ConfigDict(from_attributes=True)
    id: int
    employee: EmployeeOut
    project: ProjectOut


class BudgetListOut(BaseModel):
    total: int
    items: list[BudgetOut]


class WhitelistCreate(BaseModel):
    user_id: int
    allowed_action: str = Field(min_length=1, max_length=32)


class WhitelistOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    id: int
    user_id: int
    allowed_action: str


class ImportRowResult(BaseModel):
    row_index: int
    ok: bool
    employee_no: str | None = None
    error: str | None = None


class ImportResult(BaseModel):
    total: int
    success: int
    failed: int
    rows: list[ImportRowResult]