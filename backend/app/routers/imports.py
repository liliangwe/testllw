from fastapi import APIRouter, Depends, File, Header, Query, UploadFile
from fastapi.responses import Response
from pydantic import ValidationError
from sqlalchemy.orm import Session

from ..db import get_db
from ..models import Employee
from ..schemas import EmployeeCreate, ImportResult
from ..services.auth import SENSITIVE_IMPORT_EMPLOYEE, enforce_sensitive, get_current_user
from ..services.importer import build_template, parse_file

router = APIRouter(prefix="/api/imports", tags=["imports"])


@router.get("/template")
def template(
    format: str = Query(default="xlsx"),
    x_user: str | None = Header(default=None),
    db: Session = Depends(get_db),
):
    enforce_sensitive(db, get_current_user(db, x_user), SENSITIVE_IMPORT_EMPLOYEE)
    fmt = format.lower()
    if fmt not in ("xlsx", "csv"):
        fmt = "xlsx"
    data, mime = build_template(fmt)
    return Response(
        content=data,
        media_type=mime,
        headers={"Content-Disposition": f'attachment; filename="employee_import_template.{fmt}"'},
    )


@router.post("", response_model=ImportResult)
def import_excel(
    file: UploadFile = File(...),
    x_user: str | None = Header(default=None),
    db: Session = Depends(get_db),
):
    enforce_sensitive(db, get_current_user(db, x_user), SENSITIVE_IMPORT_EMPLOYEE)
    filename = (file.filename or "").lower()
    fmt = "csv" if filename.endswith(".csv") else "xlsx"
    data = file.file.read()
    try:
        rows = parse_file(data, fmt)
    except ValueError as exc:
        return {"total": 0, "success": 0, "failed": 0, "rows": [{"row_index": 0, "ok": False, "error": str(exc)}]}

    results = []
    success = 0
    # 批量导入内逐行去重：既含库内已有工号，也含本批已入库工号（autoflush=False 下不能依赖查询回读）
    seen = {e.employee_no for e in db.query(Employee.employee_no).all()}
    for i, rec in enumerate(rows, start=2):  # 第1行是表头
        row_index = i
        employee_no = rec.get("employee_no")
        try:
            payload = EmployeeCreate(**rec)
        except ValidationError as exc:
            results.append({"row_index": row_index, "ok": False, "employee_no": employee_no,
                            "error": f"字段校验失败: {exc.errors()[0]['msg']}"})
            continue
        if not employee_no or not payload.name:
            results.append({"row_index": row_index, "ok": False, "employee_no": employee_no, "error": "工号/姓名必填"})
            continue
        if employee_no in seen:
            results.append({"row_index": row_index, "ok": False, "employee_no": employee_no, "error": "工号已存在"})
            continue
        db.add(Employee(**payload.model_dump()))
        seen.add(employee_no)
        success += 1
        results.append({"row_index": row_index, "ok": True, "employee_no": employee_no})

    db.commit()
    return {"total": len(rows), "success": success, "failed": len(rows) - success, "rows": results}