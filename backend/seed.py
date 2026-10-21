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