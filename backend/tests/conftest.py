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