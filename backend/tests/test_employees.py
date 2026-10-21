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