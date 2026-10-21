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