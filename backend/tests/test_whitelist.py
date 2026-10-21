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