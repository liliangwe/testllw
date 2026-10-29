# 员工模块

## 模块职责

负责员工的新增、唯一性校验、分页查询、调动、离职等全生命周期管理。

## 关键类

| 类 | 说明 |
|----|------|
| `EmployeeDO` | 员工实体 |
| `EmployeeMapper` | 员工数据访问 |
| `EmployeeService` / `EmployeeServiceImpl` | 员工业务服务 |
| `EmployeeController` | 员工 REST 接口 |
| `CreateEmployeeRequest` | 新增员工请求 |
| `TransferEmployeeRequest` | 调动请求 |
| `ResignEmployeeRequest` | 离职请求 |

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/employees/check | 唯一性校验 |
| POST | /api/employees | 新增员工 |
| GET | /api/employees | 分页查询 |
| GET | /api/employees/{id} | 查看详情 |
| POST | /api/employees/{id}/transfer | 员工调动 |
| PUT | /api/employees/{id}/resign | 办理离职 |

## 依赖

- `DepartmentMapper`：校验部门存在性
- `TransferRecordMapper`：记录调动历史
- `ApprovalServiceClient`：级联更新审批节点
- `AuthServiceClient`：释放许可、撤销登录权限
