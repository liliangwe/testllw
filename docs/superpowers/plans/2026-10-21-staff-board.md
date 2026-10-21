# 人员看板 Staff Board 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 从零搭建「人员看板」Web 系统：员工台账增删改查、Excel 批量导入、员工+项目双维度成本预算、敏感操作授权白名单。

**Architecture:** 前后端分离。后端 FastAPI 提供 REST API 并托管前端静态资源；SQLAlchemy 2.0 ORM 映射 5 张关系表（users / employees / projects / employee_cost_budgets / whitelist）。敏感操作（员工删除、批量导入、预算修改/删除）在服务层统一做白名单校验，未命中返回 403。开发用 SQLite，通过 `DATABASE_URL` 可切 PostgreSQL。

**Tech Stack:** Python 3.11+ · FastAPI · SQLAlchemy 2.0 · Pydantic v2 · openpyxl · python-multipart · SQLite(dev)/PostgreSQL · pytest + httpx · 原生 HTML/JS 单页前端。

## Global Constraints

- 技术栈已确认：Web 前后端 + 关系型数据库（spec §5 Q1）。
- 员工工号 `employee_no` 全局唯一，重复新增/导入被拦截（spec FR1/FR3）。
- 员工删除采用**软删除**（置 `deleted_at` 时间戳），保留历史（spec FR2）。
- 成本预算为「员工 + 项目」双维度，唯一约束 `(employee_id, project_id, budget_period)`（spec FR4 / §6）。
- 白名单语义（已确认）：**敏感操作授权白名单**，仅白名单内用户可执行敏感操作「员工删除、批量导入、预算修改/删除」，否则返回 403 并提示（spec FR5）。
- 批量导入使用 Excel 模板，逐行返回成功/失败明细（spec FR3；Q4 暂定 Excel，字段规范以本计划的模板列为准）。
- 员工字段范围（Q5 草案）：工号 employee_no、姓名 name、部门 department、职位 position、邮箱 email、手机号 phone、入职日期 hire_date、状态 status。
- 敏感操作枚举常量集中在 `backend/app/services/auth.py` 定义，全项目引用同一常量，禁止写散落字符串。
- 当前会话为**只读流水线**：禁止 `git commit/push/merge` 等写操作。本计划各任务末尾的 `git commit` 步骤在托管流水线环境下**跳过**（独立手工执行时不跳过）。

---

## 文件结构总览

```
backend/
  app/
    __init__.py
    config.py          # 配置（DATABASE_URL 等）
    db.py              # engine / SessionLocal / Base / get_db
    models.py          # 5 张表 + 唯一约束 + 关系
    schemas.py         # Pydantic v2 请求/响应模型
    main.py            # FastAPI 应用、挂载静态目录、/ 路由
    routers/
      __init__.py
      employees.py     # 员工 CRUD（含软删除 + 白名单删除校验）
      imports.py       # 模板下载 + Excel 上传导入（白名单）
      projects.py      # 项目维护（预算维度用）
      budgets.py       # 成本预算 CRUD（白名单修改/删除）
      whitelist.py     # 白名单管理（仅 admin）
      users.py         # 当前用户 / 用户列表（身份与角色）
    services/
      __init__.py
      auth.py          # 角色 + 白名单授权、敏感操作常量
      importer.py      # Excel 模板生成 / 解析 / 校验
    static/
      index.html
      app.js
      style.css
  tests/
    conftest.py
    test_smoke.py
    test_employees.py
    test_whitelist.py
    test_budgets.py
    test_imports.py
  requirements.txt
  seed.py
README.md
```

末尾「自检」小节逐项核对 spec 覆盖。

---

## Task 1: 脚手架 + 数据模型 + 配置

**Files:**
- Create: `backend/requirements.txt`
- Create: `backend/app/__init__.py`
- Create: `backend/app/config.py`
- Create: `backend/app/db.py`
- Create: `backend/app/models.py`
- Create: `backend/app/schemas.py`
- Create: `backend/app/routers/__init__.py`
- Create: `backend/app/services/__init__.py`
- Create: `backend/app/main.py`
- Create: `backend/tests/conftest.py`
- Test: `backend/tests/test_smoke.py`

**Interfaces:**
- Produces: `Base`（SQLAlchemy DeclarativeBase）、`engine`、`SessionLocal`、`get_db`；5 张表模型 `User/Employee/Project/EmployeeCostBudget/Whitelist`；Pydantic 模型 `EmployeeOut/EmployeeCreate/EmployeeUpdate/BudgetOut/...`。后续任务全部依赖这些类型名。

- [ ] **Step 1: 写依赖清单**

```text
fastapi==0.115.6
uvicorn[standard]==0.34.0
sqlalchemy==2.0.36
pydantic==2.10.4
openpyxl==3.1.5
python-multipart==0.0.20
pytest==8.3.4
httpx==0.28.1
```

- [ ] **Step 2: 创建包与配置**

`backend/app/__init__.py`、`backend/app/routers/__init__.py`、`backend/app/services/__init__.py` 均为空文件。

```python
# backend/app/config.py
import os
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent            # backend/app
DB_PATH = os.getenv("DATABASE_PATH", str(BASE_DIR.parent / "staffboard.db"))
DATABASE_URL = os.getenv("DATABASE_URL", f"sqlite:///{DB_PATH}")
```

```python
# backend/app/db.py
from sqlalchemy import create_engine
from sqlalchemy.orm import DeclarativeBase, sessionmaker

from .config import DATABASE_URL

connect_args = {"check_same_thread": False} if DATABASE_URL.startswith("sqlite") else {}
engine = create_engine(DATABASE_URL, connect_args=connect_args, future=True)
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False, future=True)


class Base(DeclarativeBase):
    pass


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
```

- [ ] **Step 3: 定义数据模型（5 张表）**

```python
# backend/app/models.py
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
```

- [ ] **Step 4: 定义 Pydantic 契约（schemas）**

```python
# backend/app/schemas.py
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
```

- [ ] **Step 5: 最小 FastAPI 应用**

```python
# backend/app/main.py
from contextlib import asynccontextmanager

from fastapi import FastAPI

from . import models  # noqa: F401  注册表
from .db import Base, engine


@asynccontextmanager
async def lifespan(app: FastAPI):
    Base.metadata.create_all(bind=engine)
    yield


app = FastAPI(title="人员看板 Staff Board", lifespan=lifespan)


@app.get("/")
def health():
    return {"app": "staff-board", "status": "ok"}
```

- [ ] **Step 6: 测试夹具 + 冒烟测试**

```python
# backend/tests/conftest.py
import os
import sys
from pathlib import Path

import pytest
from fastapi.testclient import TestClient

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

os.environ["DATABASE_PATH"] = str(Path(__file__).resolve().parent / "test.db")
os.environ.setdefault("DATABASE_URL", f"sqlite:///{os.environ['DATABASE_PATH']}")

from app.db import Base, engine, SessionLocal  # noqa: E402
from app.main import app  # noqa: E402
from app.models import User, Whitelist  # noqa: E402


@pytest.fixture(autouse=True)
def _fresh_db():
    Base.metadata.drop_all(bind=engine)
    Base.metadata.create_all(bind=engine)
    db = SessionLocal()
    admin = User(username="admin", role="admin")
    db.add(admin)
    db.flush()
    db.add(Whitelist(user_id=admin.id, allowed_action="*", created_by="seed"))
    db.add(User(username="viewer", role="viewer"))
    db.commit()
    yield
    db.close()


@pytest.fixture()
def client():
    return TestClient(app)
```

```python
# backend/tests/test_smoke.py
from sqlalchemy import inspect

from app.db import engine


def test_all_tables_created():
    insp = inspect(engine)
    names = set(insp.get_table_names())
    for t in ("users", "employees", "projects", "employee_cost_budgets", "whitelist"):
        assert t in names
```

- [ ] **Step 7: 安装依赖并运行，验证通过**

Run: `cd backend && python -m pip install -r requirements.txt && python -m pytest tests/test_smoke.py -q`
Expected: PASS（1 passed），5 张表均存在。

- [ ] **Step 8: 提交**（流水线环境为只读，跳过 commit；手工执行时执行）

```bash
git add backend/
git commit -m "chore: staff board 脚手架、数据模型与配置"
```

---

## Task 2: 员工 CRUD（FR1/FR2）

**Files:**
- Create: `backend/app/services/auth.py`
- Create: `backend/app/routers/employees.py`
- Modify: `backend/app/main.py` （挂载 employees 路由）
- Test: `backend/tests/test_employees.py`

**Interfaces:**
- Consumes: `get_db`、`Employee/EmployeeCreate/EmployeeUpdate/EmployeeOut/EmployeeListOut`（Task 1）。
- Produces: `get_current_user(db, x_user)`、`require_admin(user)`、`is_whitelisted(db, user, action)`；`SENSITIVE_DELETE_EMPLOYEE` 常量。Task 3/4/5 复用。

- [ ] **Step 1: 写失败测试**

```python
# backend/tests/test_employees.py
def test_create_and_get_employee(client):
    r = client.post("/api/employees", json={
        "employee_no": "E001", "name": "张三", "department": "研发", "status": "active"})
    assert r.status_code == 201
    body = r.json()
    assert body["id"] == 1 and body["employee_no"] == "E001"

    r2 = client.get("/api/employees/E001").json()
    assert r2["name"] == "张三"


def test_duplicate_employee_no_rejected(client):
    payload = {"employee_no": "E001", "name": "张三"}
    client.post("/api/employees", json=payload)
    r = client.post("/api/employees", json=payload)
    assert r.status_code == 409


def test_required_name_rejected(client):
    r = client.post("/api/employees", json={"employee_no": "E100", "name": ""})
    assert r.status_code == 422


def test_update_employee(client):
    client.post("/api/employees", json={"employee_no": "E001", "name": "张三"})
    r = client.patch("/api/employees/E001", json={"position": "工程师"})
    assert r.status_code == 200 and r.json()["position"] == "工程师"


def test_delete_requires_whitelist(client):
    client.post("/api/employees", json={"employee_no": "E001", "name": "张三"})
    # 非白名单用户
    r = client.delete("/api/employees/E001", headers={"X-User": "viewer"})
    assert r.status_code == 403
    # 白名单(admin 已授 "*")
    r2 = client.delete("/api/employees/E001", headers={"X-User": "admin"})
    assert r2.status_code == 204
    # 软删除：列表默认不含，但可按 include_deleted 查
    assert client.get("/api/employees/E001").status_code == 404
    assert client.get("/api/employees/E001?include_deleted=true").status_code == 200


def test_list_search_and_pagination(client):
    for i in range(25):
        client.post("/api/employees", json={"employee_no": f"E{i:03d}", "name": f"员工{i}", "department": "研发"})
    r = client.get("/api/employees", params={"page": 1, "page_size": 20}).json()
    assert r["total"] == 25 and len(r["items"]) == 20
    r2 = client.get("/api/employees", params={"q": "员工1", "department": "研发"}).json()
    assert r2["total"] >= 1
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd backend && python -m pytest tests/test_employees.py -q`
Expected: FAIL（`GET/POST /api/employees` 未定义，404）。

- [ ] **Step 3: 实现鉴权服务**

```python
# backend/app/services/auth.py
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
```

- [ ] **Step 4: 实现员工路由**

```python
# backend/app/routers/employees.py
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
```

- [ ] **Step 5: 挂载路由**

在 `backend/app/main.py` 中 `app = FastAPI(...)` 之后加入：

```python
from .routers import employees as employees_router

app.include_router(employees_router.router)
```

- [ ] **Step 6: 运行测试确认通过**

Run: `cd backend && python -m pytest tests/test_employees.py -q`
Expected: PASS（6 passed）。

- [ ] **Step 7: 提交**（流水线只读，跳过；手工执行时）

```bash
git add backend/
git commit -m "feat: 员工 CRUD（软删除 + 工号唯一 + 列表检索分页）"
```

---

## Task 3: 白名单管理与授权（FR5）

**Files:**
- Create: `backend/app/routers/users.py`
- Create: `backend/app/routers/whitelist.py`
- Modify: `backend/app/main.py` （挂载 users / whitelist 路由）
- Test: `backend/tests/test_whitelist.py`

**Interfaces:**
- Consumes: `get_current_user`、`require_admin`（Task 2）、`User/Whitelist`（Task 1）。
- Produces: 无新增公开接口（复用 Task 2 的 `enforce_sensitive`，已在删除上验证）。

- [ ] **Step 1: 写失败测试**

```python
# backend/tests/test_whitelist.py
def test_current_user(client):
    r = client.get("/api/users/me", headers={"X-User": "admin"})
    assert r.status_code == 200
    assert r.json()["role"] == "admin"


def test_list_users_requires_admin(client):
    r = client.get("/api/users", headers={"X-User": "viewer"})
    assert r.status_code == 403
    r2 = client.get("/api/users", headers={"X-User": "admin"})
    assert r2.status_code == 200 and len(r2.json()) == 2


def test_add_whitelist_and_grant(client):
    # viewer 默认无授权 → 删除被拒
    client.post("/api/employees", json={"employee_no": "E001", "name": "张三"})
    assert client.delete("/api/employees/E001", headers={"X-User": "viewer"}).status_code == 403
    # admin 给 viewer 授权删除
    viewer_id = next(u["id"] for u in client.get("/api/users", headers={"X-User": "admin"}).json() if u["username"] == "viewer")
    r = client.post("/api/whitelist", headers={"X-User": "admin"},
                    json={"user_id": viewer_id, "allowed_action": "employee.delete"})
    assert r.status_code == 201
    assert client.delete("/api/employees/E001", headers={"X-User": "viewer"}).status_code == 204


def test_whitelist_admin_only(client):
    r = client.post("/api/whitelist", headers={"X-User": "viewer"},
                    json={"user_id": 1, "allowed_action": "employee.delete"})
    assert r.status_code == 403


def test_remove_whitelist_revokes(client):
    viewer_id = next(u["id"] for u in client.get("/api/users", headers={"X-User": "admin"}).json() if u["username"] == "viewer")
    wid = client.post("/api/whitelist", headers={"X-User": "admin"},
                      json={"user_id": viewer_id, "allowed_action": "employee.delete"}).json()["id"]
    assert client.delete(f"/api/whitelist/{wid}", headers={"X-User": "admin"}).status_code == 204
    client.post("/api/employees", json={"employee_no": "E001", "name": "张三"})
    assert client.delete("/api/employees/E001", headers={"X-User": "viewer"}).status_code == 403
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd backend && python -m pytest tests/test_whitelist.py -q`
Expected: FAIL（`/api/users/me`、`/api/whitelist` 均 404）。

- [ ] **Step 3: 实现用户路由**

```python
# backend/app/routers/users.py
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
```

- [ ] **Step 4: 实现白名单路由**

```python
# backend/app/routers/whitelist.py
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
```

- [ ] **Step 5: 挂载路由**

在 `backend/app/main.py` 中追加：

```python
from .routers import users as users_router
from .routers import whitelist as whitelist_router

app.include_router(users_router.router)
app.include_router(whitelist_router.router)
```

- [ ] **Step 6: 运行测试确认通过**

Run: `cd backend && python -m pytest tests/test_whitelist.py -q`
Expected: PASS（5 passed）。

- [ ] **Step 7: 提交**（流水线只读，跳过；手工执行时）

```bash
git add backend/
git commit -m "feat: 敏感操作授权白名单管理与校验（删除/导入/预算）"
```

---

## Task 4: 项目维护 + 成本预算（FR4）

**Files:**
- Create: `backend/app/routers/projects.py`
- Create: `backend/app/routers/budgets.py`
- Modify: `backend/app/main.py` （挂载 projects / budgets 路由）
- Test: `backend/tests/test_budgets.py`

**Interfaces:**
- Consumes: `Project/Budget*`（Task 1 schema）、`get_current_user`、`enforce_sensitive`、`SENSITIVE_BUDGET_WRITE`（Task 2）。
- Produces: 项目 CRUD（供预算关联与后续扩展）。

- [ ] **Step 1: 写失败测试**

```python
# backend/tests/test_budgets.py
def test_create_project_and_budget(client):
    p = client.post("/api/projects", json={"code": "PRJ-1", "name": "项目一"}, headers={"X-User": "admin"})
    assert p.status_code == 201 and p.json()["id"] == 1
    e = client.post("/api/employees", json={"employee_no": "E001", "name": "张三"}, headers={"X-User": "admin"})
    eid = e.json()["id"] if e.status_code == 201 else 1
    r = client.post("/api/budgets", headers={"X-User": "admin"}, json={
        "employee_id": eid, "project_id": 1, "budget_amount": "10000.00",
        "budget_period": "2026Q1", "currency": "CNY"})
    assert r.status_code == 201
    assert str(r.json()["budget_amount"]) == "10000.00"


def test_budget_duplicate_unique_rejected(client):
    client.post("/api/projects", json={"code": "PRJ-1", "name": "项目一"}, headers={"X-User": "admin"})
    client.post("/api/employees", json={"employee_no": "E001", "name": "张三"}, headers={"X-User": "admin"})
    payload = {"employee_id": 1, "project_id": 1, "budget_amount": "100.00", "budget_period": "2026Q1"}
    assert client.post("/api/budgets", headers={"X-User": "admin"}, json=payload).status_code == 201
    assert client.post("/api/budgets", headers={"X-User": "admin"}, json=payload).status_code == 409


def test_budget_write_requires_whitelist(client):
    client.post("/api/projects", json={"code": "PRJ-1", "name": "项目一"}, headers={"X-User": "admin"})
    client.post("/api/employees", json={"employee_no": "E001", "name": "张三"}, headers={"X-User": "admin"})
    payload = {"employee_id": 1, "project_id": 1, "budget_amount": "100.00", "budget_period": "2026Q1"}
    # admin 授 "*"，可写
    assert client.post("/api/budgets", headers={"X-User": "admin"}, json=payload).status_code == 201
    # viewer 未授权 → 403
    r = client.post("/api/budgets", headers={"X-User": "viewer"}, json=payload)
    assert r.status_code == 403


def test_budget_list_filter_by_employee(client):
    client.post("/api/projects", json={"code": "PRJ-1", "name": "项目一"}, headers={"X-User": "admin"})
    client.post("/api/employees", json={"employee_no": "E001", "name": "张三"}, headers={"X-User": "admin"})
    client.post("/api/budgets", headers={"X-User": "admin"}, json={
        "employee_id": 1, "project_id": 1, "budget_amount": "100.00", "budget_period": "2026Q1"})
    r = client.get("/api/budgets", params={"employee_id": 1}).json()
    assert r["total"] == 1 and r["items"][0]["employee"]["employee_no"] == "E001"
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd backend && python -m pytest tests/test_budgets.py -q`
Expected: FAIL（`/api/projects`、`/api/budgets` 均 404）。

- [ ] **Step 3: 实现项目路由**

```python
# backend/app/routers/projects.py
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
```

- [ ] **Step 4: 实现预算路由**

```python
# backend/app/routers/budgets.py
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
```

- [ ] **Step 5: 补充 schemas 的 `BudgetListOut`**

在 `backend/app/schemas.py` 末尾追加：

```python
class BudgetListOut(BaseModel):
    total: int
    items: list[BudgetOut]
```

（Step 4 的 `budgets.py` 已直接 `from ..schemas import BudgetListOut`，实施时保证该导入存在即可。）

- [ ] **Step 6: 挂载路由**

在 `backend/app/main.py` 中追加：

```python
from .routers import budgets as budgets_router
from .routers import projects as projects_router

app.include_router(projects_router.router)
app.include_router(budgets_router.router)
```

- [ ] **Step 7: 运行测试确认通过**

Run: `cd backend && python -m pytest tests/test_budgets.py -q`
Expected: PASS（4 passed）。

- [ ] **Step 8: 提交**（流水线只读，跳过；手工执行时）

```bash
git add backend/
git commit -m "feat: 项目维护 + 员工/项目双维度成本预算（白名单保护写操作）"
```

---

## Task 5: 批量导入（模板下载 + Excel 上传）（FR3）

**Files:**
- Create: `backend/app/services/importer.py`
- Create: `backend/app/routers/imports.py`
- Modify: `backend/app/main.py` （挂载 imports 路由）
- Test: `backend/tests/test_imports.py`

**Interfaces:**
- Consumes: `Employee` 模型、`SENSITIVE_IMPORT_EMPLOYEE`、`enforce_sensitive`（Task 2）、`Import*` schema（Task 1）。
- Produces: `build_template()`、`parse_workbook(data: bytes) -> list[dict]`。

**导入字段（与模板列对齐，规范即 Q4 默认）：** `employee_no`(必填)、`name`(必填)、`department`、`position`、`email`、`phone`、`hire_date`、`status`。首行表头必须完全匹配这 8 列。

- [ ] **Step 1: 写失败测试**

```python
# backend/tests/test_imports.py
import io

from openpyxl import Workbook


def _make_xlsx(rows):
    wb = Workbook()
    ws = wb.active
    ws.append(["employee_no", "name", "department", "position", "email", "phone", "hire_date", "status"])
    for r in rows:
        ws.append(r)
    buf = io.BytesIO()
    wb.save(buf)
    return buf.getvalue()


def test_download_template(client):
    r = client.get("/api/imports/template", headers={"X-User": "admin"})
    assert r.status_code == 200
    assert r.headers["content-type"].startswith("application/vnd.openxmlformats-officedocument")


def test_import_requires_whitelist(client):
    data = _make_xlsx([["E001", "张三", "", "", "", "", "", "active"]])
    r = client.post("/api/imports", headers={"X-User": "viewer"},
                    files={"file": ("t.xlsx", data, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")})
    assert r.status_code == 403


def test_import_success_and_partial_failure(client):
    data = _make_xlsx([
        ["E001", "张三", "研发", "", "", "", "", "active"],
        ["E002", "李四", "", "", "", "", "", ""],          # 缺 status → 失败
        ["E001", "王五", "", "", "", "", "", "active"],     # 重复工号 → 失败
    ])
    r = client.post("/api/imports", headers={"X-User": "admin"},
                    files={"file": ("t.xlsx", data, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")})
    assert r.status_code == 200
    body = r.json()
    assert body["total"] == 3 and body["success"] == 1 and body["failed"] == 2
    # E001 已入库
    assert client.get("/api/employees/E001").status_code == 200
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd backend && python -m pytest tests/test_imports.py -q`
Expected: FAIL（`/api/imports/template`、`/api/imports` 均 404）。

- [ ] **Step 3: 实现导入服务**

```python
# backend/app/services/importer.py
import io
from datetime import date

from openpyxl import Workbook, load_workbook

HEADERS = ["employee_no", "name", "department", "position", "email", "phone", "hire_date", "status"]


def build_template() -> bytes:
    wb = Workbook()
    ws = wb.active
    ws.title = "员工导入"
    ws.append(HEADERS)
    ws.append(["E001", "张三", "研发部", "工程师", "zhangsan@example.com", "13800000000", "2024-01-01", "active"])
    buf = io.BytesIO()
    wb.save(buf)
    return buf.getvalue()


def parse_workbook(data: bytes) -> list[dict]:
    wb = load_workbook(io.BytesIO(data), data_only=True)
    ws = wb.active
    rows = list(ws.iter_rows(values_only=True))
    if not rows:
        return []
    header = [str(c).strip() if c is not None else "" for c in rows[0]]
    if header != HEADERS:
        raise ValueError(f"表头不匹配，应为 {HEADERS}")
    out = []
    for row in rows[1:]:
        if row is None or all(c in (None, "") for c in row):
            continue
        rec = dict(zip(HEADERS, row))
        for k in ("employee_no", "name", "department", "position", "email", "phone", "status"):
            v = rec.get(k)
            rec[k] = str(v).strip() if v is not None else ""
        hd = rec.get("hire_date")
        if isinstance(hd, date):
            rec["hire_date"] = hd
        elif isinstance(hd, str) and hd.strip():
            rec["hire_date"] = date.fromisoformat(hd.strip())
        else:
            rec["hire_date"] = None
        rec["status"] = rec.get("status") or "active"
        out.append(rec)
    return out
```

- [ ] **Step 4: 实现导入路由**

```python
# backend/app/routers/imports.py
from fastapi import APIRouter, Depends, Header, UploadFile, File
from fastapi.responses import Response
from sqlalchemy.orm import Session
from pydantic import ValidationError

from ..db import get_db
from ..models import Employee
from ..schemas import EmployeeCreate, ImportResult
from ..services.auth import SENSITIVE_IMPORT_EMPLOYEE, enforce_sensitive, get_current_user
from ..services.importer import build_template, parse_workbook

router = APIRouter(prefix="/api/imports", tags=["imports"])


@router.get("/template")
def template(x_user: str | None = Header(default=None), db: Session = Depends(get_db)):
    enforce_sensitive(db, get_current_user(db, x_user), SENSITIVE_IMPORT_EMPLOYEE)
    data = build_template()
    return Response(
        content=data,
        media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        headers={"Content-Disposition": 'attachment; filename="employee_import_template.xlsx"'},
    )


@router.post("", response_model=ImportResult)
def import_excel(
    file: UploadFile = File(...),
    x_user: str | None = Header(default=None),
    db: Session = Depends(get_db),
):
    enforce_sensitive(db, get_current_user(db, x_user), SENSITIVE_IMPORT_EMPLOYEE)
    data = file.file.read()
    try:
        rows = parse_workbook(data)
    except ValueError as exc:
        return {"total": 0, "success": 0, "failed": 0, "rows": [{"row_index": 0, "ok": False, "error": str(exc)}]}

    results = []
    success = 0
    for i, rec in enumerate(rows, start=2):  # 第1行是表头
        row_index = i
        employee_no = rec.get("employee_no")
        try:
            payload = EmployeeCreate(**rec)
        except ValidationError as exc:
            results.append({"row_index": row_index, "ok": False, "employee_no": employee_no,
                            "error": f"字段校验失败: {exc.errors()[0]['msg']}"})
            continue
        if not employee_no or not payload.name:
            results.append({"row_index": row_index, "ok": False, "employee_no": employee_no, "error": "工号/姓名必填"})
            continue
        if db.query(Employee).filter(Employee.employee_no == employee_no).first():
            results.append({"row_index": row_index, "ok": False, "employee_no": employee_no, "error": "工号已存在"})
            continue
        db.add(Employee(**payload.model_dump()))
        success += 1
        results.append({"row_index": row_index, "ok": True, "employee_no": employee_no})

    db.commit()
    return {"total": len(rows), "success": success, "failed": len(rows) - success, "rows": results}
```

- [ ] **Step 5: 挂载路由**

在 `backend/app/main.py` 中追加：

```python
from .routers import imports as imports_router

app.include_router(imports_router.router)
```

- [ ] **Step 6: 运行测试确认通过**

Run: `cd backend && python -m pytest tests/test_imports.py -q`
Expected: PASS（3 passed）。

- [ ] **Step 7: 提交**（流水线只读，跳过；手工执行时）

```bash
git add backend/
git commit -m "feat: Excel 模板下载与批量导入（白名单 + 逐行结果明细）"
```

---

## Task 6: 前端看板 SPA（静态托管）

**Files:**
- Create: `backend/app/static/index.html`
- Create: `backend/app/static/app.js`
- Create: `backend/app/static/style.css`
- Modify: `backend/app/main.py` （挂载静态目录）
- Test: `backend/tests/test_smoke.py` （追加前端静态资源可达断言）

**Interfaces:**
- Consumes: 已实现的 REST API（`/api/employees`、`/api/imports`、`/api/budgets`、`/api/whitelist`、`/api/projects`、`/api/users/me`）。
- Produces: 单文件 HTML 看板（列表/新增/编辑/删除/导入/预算汇总视图），通过 `X-User` 请求头传递当前用户身份。

- [ ] **Step 1: 实现 index.html**

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>人员看板</title>
  <link rel="stylesheet" href="/static/style.css" />
</head>
<body>
  <header>
    <h1>人员看板</h1>
    <div class="toolbar">
      <label>当前用户
        <select id="xUser">
          <option value="admin">admin</option>
          <option value="viewer">viewer</option>
        </select>
      </label>
      <button id="btnAdd">新增员工</button>
      <input type="file" id="fileInput" accept=".xlsx" />
      <button id="btnImport">批量导入</button>
      <a href="/api/imports/template" id="btnTemplate" class="link">下载模板</a>
      <button id="btnMe">我的身份</button>
    </div>
  </header>
  <main>
    <section id="summary">
      <input id="search" placeholder="按姓名/工号搜索" />
      <select id="deptFilter"><option value="">全部部门</option></select>
      <button id="btnSearch">检索</button>
    </section>
    <section id="result"></section>
    <section id="budgets"></section>
  </main>
  <dialog id="dlg">
    <form id="empForm">
      <label>工号 <input name="employee_no" required /></label>
      <label>姓名 <input name="name" required /></label>
      <label>部门 <input name="department" /></label>
      <label>职位 <input name="position" /></label>
      <label>邮箱 <input name="email" /></label>
      <label>手机号 <input name="phone" /></label>
      <label>入职日期 <input name="hire_date" type="date" /></label>
      <label>状态
        <select name="status">
          <option value="active">active</option>
          <option value="inactive">inactive</option>
        </select>
      </label>
      <div class="actions">
        <button type="submit">保存</button>
        <button type="button" id="btnCancel">取消</button>
      </div>
    </form>
  </dialog>
  <script src="/static/app.js"></script>
</body>
</html>
```

- [ ] **Step 2: 实现 app.js**

```javascript
// backend/app/static/app.js
const API = "";
const userId = () => document.getElementById("xUser").value;
const headers = () => ({ "X-User": userId(), "Content-Type": "application/json" });

async function j(url, opts = {}) {
  const resp = await fetch(API + url, { ...opts, headers: { ...headers(), ...(opts.headers || {}) } });
  if (resp.status === 204) return null;
  const body = await resp.json().catch(() => ({}));
  if (!resp.ok) throw new Error(body.detail || `HTTP ${resp.status}`);
  return body;
}

async function loadList() {
  const q = document.getElementById("search").value;
  const dept = document.getElementById("deptFilter").value;
  const params = new URLSearchParams({ page: "1", page_size: "50" });
  if (q) params.set("q", q);
  if (dept) params.set("department", dept);
  const data = await j("/api/employees?" + params.toString());
  const tbody = data.items.map(e => `
    <tr>
      <td>${e.employee_no}</td><td>${e.name}</td><td>${e.department || ""}</td>
      <td>${e.position || ""}</td><td>${e.status}</td>
      <td><button data-no="${e.employee_no}" class="edit">编辑</button>
          <button data-no="${e.employee_no}" class="del">删除</button></td>
    </tr>`).join("");
  document.getElementById("result").innerHTML =
    `<table><thead><tr><th>工号</th><th>姓名</th><th>部门</th><th>职位</th><th>状态</th><th>操作</th></tr></thead><tbody>${tbody}</tbody></table>`;
  document.querySelectorAll(".edit").forEach(b => b.onclick = () => openEdit(b.dataset.no));
  document.querySelectorAll(".del").forEach(b => b.onclick = () => remove(b.dataset.no));
}

async function addOrEdit(e) {
  e.preventDefault();
  const fd = new FormData(e.target);
  const payload = Object.fromEntries(fd.entries());
  if (payload.hire_date === "") delete payload.hire_date;
  try {
    if (editingNo) {
      await j(`/api/employees/${editingNo}`, { method: "PATCH", body: JSON.stringify(payload) });
    } else {
      await j("/api/employees", { method: "POST", body: JSON.stringify(payload) });
    }
    closeDlg(); await loadList();
  } catch (err) { alert(err.message); }
}

async function remove(no) {
  if (!confirm(`确认删除员工 ${no}？`)) return;
  try { await j(`/api/employees/${no}`, { method: "DELETE" }); await loadList(); }
  catch (err) { alert(err.message); }
}

async function importExcel() {
  const file = document.getElementById("fileInput").files[0];
  if (!file) { alert("请选择 xlsx 文件"); return; }
  const form = new FormData(); form.append("file", file);
  try {
    const resp = await fetch(API + "/api/imports", { method: "POST", headers: { "X-User": userId() }, body: form });
    const body = await resp.json();
    if (!resp.ok) { alert(body.detail || resp.status); return; }
    alert(`导入完成：成功 ${body.success}，失败 ${body.failed}\n` +
      body.rows.filter(r => !r.ok).map(r => `第${r.row_index}行(${r.employee_no}): ${r.error}`).join("\n"));
    await loadList();
  } catch (err) { alert(err.message); }
}

async function loadMyRole() { const u = await j("/api/users/me"); alert(`${u.username} 角色：${u.role}`); }

let editingNo = null;
function openAdd() { editingNo = null; document.getElementById("empForm").reset(); document.getElementById("dlg").showModal(); }
async function openEdit(no) {
  const e = await j(`/api/employees/${no}`);
  editingNo = no;
  const f = document.getElementById("empForm");
  f.employee_no.value = e.employee_no; f.name.value = e.name; f.department.value = e.department || "";
  f.position.value = e.position || ""; f.email.value = e.email || ""; f.phone.value = e.phone || "";
  f.hire_date.value = e.hire_date || ""; f.status.value = e.status;
  document.getElementById("dlg").showModal();
}
function closeDlg() { document.getElementById("dlg").close(); }

document.getElementById("empForm").addEventListener("submit", addOrEdit);
document.getElementById("btnAdd").onclick = openAdd;
document.getElementById("btnCancel").onclick = closeDlg;
document.getElementById("btnImport").onclick = importExcel;
document.getElementById("btnMe").onclick = loadMyRole;
document.getElementById("btnSearch").onclick = loadList;
document.getElementById("btnTemplate").href = `/api/imports/template?_=${Date.now()}`;
loadList();
```

- [ ] **Step 3: 实现 style.css**

```css
/* backend/app/static/style.css */
body { font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif; margin: 24px; color: #1f2933; }
header { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; margin-bottom: 16px; }
h1 { font-size: 22px; margin: 0; }
.toolbar, #summary { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; margin-bottom: 12px; }
table { border-collapse: collapse; width: 100%; }
th, td { border: 1px solid #d9dee4; padding: 6px 10px; text-align: left; font-size: 14px; }
th { background: #f4f6f8; }
button, .link { padding: 6px 12px; border: 1px solid #b8c2cc; background: #fff; border-radius: 4px; cursor: pointer; font-size: 14px; }
button:hover, .link:hover { background: #eef2f6; }
.link { text-decoration: none; color: #1677ff; }
dialog { border: 1px solid #d9dee4; border-radius: 8px; padding: 20px; min-width: 360px; }
dialog form { display: grid; grid-template-columns: 1fr 2fr; gap: 8px; }
dialog label { display: contents; }
.actions { grid-column: 1 / -1; display: flex; gap: 8px; justify-content: flex-end; }
.toast { color: #c0392b; }
```

- [ ] **Step 4: 挂载静态目录**

在 `backend/app/main.py` 中追加：

```python
from fastapi.staticfiles import StaticFiles
from pathlib import Path

app.mount("/static", StaticFiles(directory=Path(__file__).resolve().parent / "static"), name="static")
```

- [ ] **Step 5: 追加冒烟断言并运行**

在 `backend/tests/test_smoke.py` 追加（`client` fixture 已由 `conftest.py` 提供）：

```python
def test_frontend_assets_served(client):
    assert client.get("/").status_code == 200
    assert client.get("/static/index.html").status_code == 200
    assert client.get("/static/app.js").status_code == 200
```
（`client` 由 Task 1 的 `conftest.py::client` fixture 提供，无需在本文件内重新定义。）

Run: `cd backend && python -m pytest tests/test_smoke.py -q`
Expected: PASS。

- [ ] **Step 6: 提交**（流水线只读，跳过；手工执行时）

```bash
git add backend/
git commit -m "feat: 人员看板前端单页 + 静态托管"
```

---

## Task 7: Seed 数据 + README + 集成冒烟

**Files:**
- Create: `backend/seed.py`
- Create: `README.md`（改写仓库根目录既有文件）
- Test: `backend/tests/test_integration.py`

**Interfaces:**
- Consumes: 全部已实现路由与模型。
- Produces: `seed.py`（幂等初始化 admin/viewer 用户、默认白名单 `*`、示例项目）。

- [ ] **Step 1: 写集成冒烟测试**

```python
# backend/tests/test_integration.py
def test_full_flow(client):
    # 1) 创建项目
    p = client.post("/api/projects", headers={"X-User": "admin"}, json={"code": "P1", "name": "项目一"})
    assert p.status_code == 201
    # 2) 创建员工
    e = client.post("/api/employees", headers={"X-User": "admin"},
                    json={"employee_no": "E001", "name": "张三", "department": "研发"})
    assert e.status_code == 201
    eid = e.json()["id"]
    # 3) 建立预算（admin 授 "*"）
    b = client.post("/api/budgets", headers={"X-User": "admin"},
                    json={"employee_id": eid, "project_id": 1, "budget_amount": "5000.00", "budget_period": "2026Q1"})
    assert b.status_code == 201
    # 4) viewer 无授权操作预算 → 403
    assert client.patch("/api/budgets/1", headers={"X-User": "viewer"},
                        json={"budget_amount": "1.00"}).status_code == 403
    # 5) 列表按员工聚合可见预算
    r = client.get("/api/budgets", params={"employee_id": eid}).json()
    assert r["total"] == 1 and r["items"][0]["project"]["code"] == "P1"
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd backend && python -m pytest tests/test_integration.py -q`
Expected: PASS（依赖已实现，此测试为回归守卫；若前面任务全部完成应直接通过）。

- [ ] **Step 3: 实现 seed.py（幂等，可重复执行）**

```python
# backend/seed.py
"""初始化种子数据：admin / viewer 用户、admin 默认白名单 '*'、示例项目。幂等，可重复执行。"""
from app.db import Base, SessionLocal, engine
from app.models import Project, User, Whitelist


def seed() -> None:
    Base.metadata.create_all(bind=engine)
    db = SessionLocal()
    try:
        admin = db.query(User).filter(User.username == "admin").first()
        if admin is None:
            admin = User(username="admin", role="admin")
            db.add(admin)
            db.flush()
        viewer = db.query(User).filter(User.username == "viewer").first()
        if viewer is None:
            viewer = User(username="viewer", role="viewer")
            db.add(viewer)
            db.flush()
        wl = db.query(Whitelist).filter(Whitelist.user_id == admin.id, Whitelist.allowed_action == "*").first()
        if wl is None:
            db.add(Whitelist(user_id=admin.id, allowed_action="*", created_by="seed"))
        if db.query(Project).filter(Project.code == "DEMO").first() is None:
            db.add(Project(code="DEMO", name="示例项目"))
        db.commit()
    finally:
        db.close()


if __name__ == "__main__":
    seed()
    print("seed 完成")
```

- [ ] **Step 4: 改写 README.md**

```markdown
# 人员看板 Staff Board

员工台账 + 成本预算的 Web 看板。功能：员工 CRUD（软删除、工号唯一、检索分页）、
Excel 模板批量导入（逐行结果明细）、员工+项目双维度成本预算、敏感操作授权白名单。

## 快速开始

cd backend
python -m pip install -r requirements.txt
python seed.py                 # 初始化 admin/viewer 用户与默认白名单
uvicorn app.main:app --reload  # http://127.0.0.1:8000/

## 测试

cd backend && python -m pytest -q

## 目录

- backend/app/models.py      数据模型（5 表）
- backend/app/routers/       REST 路由
- backend/app/services/      白名单授权 + Excel 导入
- backend/app/static/        前端看板（静态托管）
```

- [ ] **Step 5: 全量测试**

Run: `cd backend && python -m pytest -q`
Expected: PASS（前序各任务测试全部通过，总数 ≥ 18）。

- [ ] **Step 6: 提交**（流水线只读，跳过；手工执行时）

```bash
git add backend/ README.md
git commit -m "feat: seed 数据 + README + 集成冒烟测试"
```

---

## 自检（Self-Review）

**Spec 覆盖核对（spec §4 FR1–FR5 → 任务）：**

| 需求 | 对应任务/步骤 |
| --- | --- |
| FR1 员工录入入口 | Task 2（`POST /api/employees`）+ Task 6（前端新增表单） |
| FR2 CRUD（查/增/改/删，工号唯一、软删除、检索分页） | Task 2（employees 路由 + 测试） |
| FR3 批量导入（模板、校验、逐行明细） | Task 5（importer + imports 路由） |
| FR4 成本预算（员工+项目双维度、唯一约束、按维度聚合） | Task 4（budgets/projects + 测试） |
| FR5 敏感操作授权白名单 | Task 2（enforce_sensitive）+ Task 3（whitelist 管理） |

**占位符扫描：** 所有代码步骤均给出完整实现，无 TBD/TODO/「类似 Task N」占位。

**类型一致性：** 关键命名跨任务对齐——`SENSITIVE_DELETE_EMPLOYEE`、`SENSITIVE_IMPORT_EMPLOYEE`、`SENSITIVE_BUDGET_WRITE` 均引自 `services/auth.py`；`BudgetListOut` 定义于 Task 4 追加的 schemas 且 budgets 路由已 import；`EmployeeListOut/BudgetListOut` 的 `{total, items}` 结构与路由 return 一致；`get_current_user(db, x_user)`、`enforce_sensitive(db, user, action)` 签名在 Task 2 定义、Task 3/4/5 复用不变量。

**共识约定提醒（Global Constraints）：** 删除=软删除；工号唯一；预算唯一 `(employee_id, project_id, budget_period)`；白名单只保护「员工删除/批量导入/预算写」三类敏感操作；导入 8 列模板表头固定。

---

## 执行交接

计划已保存至 `docs/superpowers/plans/2026-10-21-staff-board.md`。两种执行方式：

1. **Subagent-Driven（推荐）** — 每个任务派发独立 subagent，任务间做两阶段评审；
2. **Inline Execution** — 本会话内用 executing-plans 批量执行，带检查点。

> 本流水线为只读「计划」节点，不进入实现环节；下游「实施」节点可任选其一继续推进。