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