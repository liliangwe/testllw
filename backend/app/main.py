from contextlib import asynccontextmanager
from pathlib import Path

from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles

from . import models  # noqa: F401  注册表
from .db import Base, engine
from .routers import budgets as budgets_router
from .routers import employees as employees_router
from .routers import imports as imports_router
from .routers import projects as projects_router
from .routers import users as users_router
from .routers import whitelist as whitelist_router


@asynccontextmanager
async def lifespan(app: FastAPI):
    Base.metadata.create_all(bind=engine)
    yield


app = FastAPI(title="人员看板 Staff Board", lifespan=lifespan)

app.include_router(employees_router.router)
app.include_router(users_router.router)
app.include_router(whitelist_router.router)
app.include_router(projects_router.router)
app.include_router(budgets_router.router)
app.include_router(imports_router.router)

app.mount("/static", StaticFiles(directory=Path(__file__).resolve().parent / "static"), name="static")


@app.get("/")
def health():
    return {"app": "staff-board", "status": "ok"}