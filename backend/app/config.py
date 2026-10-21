import os
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent            # backend/app
DB_PATH = os.getenv("DATABASE_PATH", str(BASE_DIR.parent / "staffboard.db"))
DATABASE_URL = os.getenv("DATABASE_URL", f"sqlite:///{DB_PATH}")