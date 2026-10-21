# 人员看板 Staff Board

员工台账 + 成本预算的 Web 看板。功能：员工 CRUD（软删除、工号唯一、检索分页）、
Excel/CSV 模板批量导入（逐行结果明细）、员工+项目双维度成本预算、敏感操作授权白名单。

## 快速开始

cd backend
python -m pip install -r requirements.txt
python seed.py                 # 初始化 admin/viewer 用户与默认白名单
uvicorn app.main:app --reload  # http://127.0.0.1:8000/

## 测试

cd backend && python -m pytest -q

## 目录

- backend/app/models.py      数据模型（5 表）
- backend/app/routers/       REST 路由
- backend/app/services/      白名单授权 + Excel/CSV 导入
- backend/app/static/        前端看板（静态托管）