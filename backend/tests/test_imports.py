import csv
import io

from openpyxl import Workbook

HEADERS = ["employee_no", "name", "department", "position", "email", "phone", "hire_date", "status"]


def _make_xlsx(rows):
    wb = Workbook()
    ws = wb.active
    ws.append(HEADERS)
    for r in rows:
        ws.append(r)
    buf = io.BytesIO()
    wb.save(buf)
    return buf.getvalue()


def _make_csv(rows):
    buf = io.StringIO()
    w = csv.writer(buf)
    w.writerow(HEADERS)
    for r in rows:
        w.writerow(r)
    return buf.getvalue().encode("utf-8")


def test_download_template(client):
    r = client.get("/api/imports/template", headers={"X-User": "admin"})
    assert r.status_code == 200
    assert r.headers["content-type"].startswith("application/vnd.openxmlformats-officedocument")


def test_download_csv_template(client):
    r = client.get("/api/imports/template", params={"format": "csv"}, headers={"X-User": "admin"})
    assert r.status_code == 200
    assert r.headers["content-type"].startswith("text/csv")


def test_import_requires_whitelist(client):
    data = _make_xlsx([["E001", "张三", "", "", "", "", "", "active"]])
    r = client.post("/api/imports", headers={"X-User": "viewer"},
                    files={"file": ("t.xlsx", data, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")})
    assert r.status_code == 403


def test_import_success_and_partial_failure(client):
    data = _make_xlsx([
        ["E001", "张三", "研发", "", "", "", "", "active"],
        ["E002", "李四", "", "", "", "", "", "离职"],        # 非法状态 → 字段校验失败
        ["E001", "王五", "", "", "", "", "", "active"],     # 重复工号 → 失败
    ])
    r = client.post("/api/imports", headers={"X-User": "admin"},
                    files={"file": ("t.xlsx", data, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")})
    assert r.status_code == 200
    body = r.json()
    assert body["total"] == 3 and body["success"] == 1 and body["failed"] == 2
    # E001 已入库
    assert client.get("/api/employees/E001").status_code == 200


def test_import_csv_success(client):
    data = _make_csv([["E010", "赵六", "产品", "", "", "", "2024-03-01", "active"]])
    r = client.post("/api/imports", headers={"X-User": "admin"},
                    files={"file": ("t.csv", data, "text/csv")})
    assert r.status_code == 200
    body = r.json()
    assert body["total"] == 1 and body["success"] == 1 and body["failed"] == 0
    assert client.get("/api/employees/E010").status_code == 200