# SDD Progress Ledger — 人员看板 Staff Board

- Task 1: complete (脚手架+数据模型+配置，commits 无 — 只读流水线跳过 commit)
- Task 2: complete (员工 CRUD)
- Task 3: complete (白名单管理与授权)
- Task 4: complete (项目维护 + 成本预算)
- Task 5: complete (批量导入 Excel/CSV)
- Task 6: complete (前端看板 SPA 静态托管)
- Task 7: complete (seed + README + 集成冒烟)

验证：`cd backend && .venv/bin/python -m pytest -q` → 23 passed；`seed.py` 幂等执行通过；应用可导入，18 条 API 路由就绪。