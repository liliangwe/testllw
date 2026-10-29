# 部门模块

## 模块职责

负责部门树形结构的加载、懒加载、拖拽移动、删除校验。

## 关键类

| 类 | 说明 |
|----|------|
| `DepartmentDO` | 部门实体 |
| `DepartmentMapper` | 部门数据访问 |
| `DepartmentService` / `DepartmentServiceImpl` | 部门业务服务 |
| `DepartmentController` | 部门 REST 接口 |
| `MoveDepartmentRequest` | 部门移动请求 |

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/departments/tree | 获取部门树 |
| GET | /api/departments/{id}/children | 懒加载子部门 |
| PUT | /api/departments/{id}/move | 拖拽移动部门 |
| DELETE | /api/departments/{id} | 删除部门 |

## 依赖

- `EmployeeMapper`：删除时校验部门下是否存在员工
