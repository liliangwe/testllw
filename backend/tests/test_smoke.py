from sqlalchemy import inspect

from app.db import engine


def test_all_tables_created():
    insp = inspect(engine)
    names = set(insp.get_table_names())
    for t in ("users", "employees", "projects", "employee_cost_budgets", "whitelist"):
        assert t in names


def test_frontend_assets_served(client):
    assert client.get("/").status_code == 200
    assert client.get("/static/index.html").status_code == 200
    assert client.get("/static/app.js").status_code == 200