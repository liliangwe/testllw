# Code Review Report

> **Change**: 组织架构与人员管理系统编码实现 · **分支**: task/DEV-b420bb03-b4cb-11f1-a23d-6d212eeaa3dd-bb10775d-33d4-404d-9f0e-6d6341d8e805 · **日期**: 2026-10-29 · **审查者** AI
>
> **AI**：等级 **P0 / P1 / P2**；G/S 以 checklist 行内定义为准；Bug 模式以 `bug-pattern-checklist.md` 表头为准（Blocker→P0、Major→P1、Info→P2）。**须先**运行 `scan-all-rules.sh` 并将要点并入 §5，**再**写 LLM 结论。

---

## 1. 审查范围

| 项 | 值 |
|----|-----|
| `.java` 文件数 | 21 |
| XML 映射文件数 | 3 |
| 变更行数 | 新增完整工程代码 |

| 类/接口 | 路径 | 角色（可选） |
|---------|------|--------------|
| OrganizationApplication | src/main/java/com/example/organization/OrganizationApplication.java | 启动类 |
| ApiResponse | src/main/java/com/example/organization/common/ApiResponse.java | 统一响应 |
| BizException | src/main/java/com/example/organization/common/BizException.java | 业务异常 |
| ErrorCodeEnum | src/main/java/com/example/organization/common/ErrorCodeEnum.java | 错误码 |
| PageResult | src/main/java/com/example/organization/common/PageResult.java | 分页结果 |
| TenantContext | src/main/java/com/example/organization/common/TenantContext.java | 租户上下文 |
| DepartmentController | src/main/java/com/example/organization/controller/DepartmentController.java | 部门接口 |
| EmployeeController | src/main/java/com/example/organization/controller/EmployeeController.java | 员工接口 |
| DepartmentMapper | src/main/java/com/example/organization/dao/mapper/DepartmentMapper.java | 部门持久层 |
| EmployeeMapper | src/main/java/com/example/organization/dao/mapper/EmployeeMapper.java | 员工持久层 |
| TransferRecordMapper | src/main/java/com/example/organization/dao/mapper/TransferRecordMapper.java | 调动记录持久层 |
| ApprovalServiceClientImpl | src/main/java/com/example/organization/integration/impl/ApprovalServiceClientImpl.java | 审批流客户端占位 |
| AuthServiceClientImpl | src/main/java/com/example/organization/integration/impl/AuthServiceClientImpl.java | 权限客户端占位 |
| DepartmentServiceImpl | src/main/java/com/example/organization/service/impl/DepartmentServiceImpl.java | 部门业务实现 |
| EmployeeServiceImpl | src/main/java/com/example/organization/service/impl/EmployeeServiceImpl.java | 员工业务实现 |
| BaseDO / DepartmentDO / EmployeeDO / TransferRecordDO | src/main/java/com/example/organization/model/entity/*.java | 实体 |
| Request / VO / DTO | src/main/java/com/example/organization/model/*/*.java | 请求/响应对象 |
| DepartmentServiceImplTest | src/test/java/com/example/organization/service/impl/DepartmentServiceImplTest.java | 部门单元测试 |
| EmployeeServiceImplTest | src/test/java/com/example/organization/service/impl/EmployeeServiceImplTest.java | 员工单元测试 |

---

## 2. 问题计数

| P0 | P1 | P2 |
|----|----|-----|
| 2 | 4 | 2 |

---

## 3. Step 2 — 功能（REQ）

### REQ-1: 部门树加载与展示

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 按 parent_id 组装树形结构返回 | ✅ | design.md 5.1.3.1 | DepartmentServiceImpl.java:45-69 | 实现 selectAll 后内存组装；默认按 sort_order 升序 |
| 懒加载子部门 | ✅ | design.md W02 | DepartmentServiceImpl.java:71-74 | 按 parent_id 精确查询 |
| 拖拽调整部门层级 | ✅ | design.md W03 | DepartmentServiceImpl.java:77-115 | 已校验循环引用并级联更新 path |

### REQ-2: 员工新增与唯一性校验

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 实时校验工号/手机号唯一性 | ✅ | design.md W05 | EmployeeServiceImpl.java:62-71 | 仅支持 employeeNo/phone |
| 新增时再次校验工号/手机号 | ✅ | design.md W06 | EmployeeServiceImpl.java:82-89 | 校验租户内未删除员工 |
| 校验所属部门 ID 存在 | ✅ | design.md W06 | EmployeeServiceImpl.java:76-80 | 查询 departmentMapper.selectById |

### REQ-3: 人员调动（级联更新与快照）

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 更新员工 dept_id | ✅ | design.md W09 | EmployeeServiceImpl.java:143-147 | 使用 updateByVersion 乐观锁更新 |
| 级联更新审批流节点 | ✅ | design.md W09 | EmployeeServiceImpl.java:165-169 | 调用 approvalServiceClient.updateDefaultApprover，失败仅日志 |
| 记录调动历史 | ✅ | design.md W09 | EmployeeServiceImpl.java:153-163 | 写入 transfer_record |
| 操作人记录 | ❌ | design.md W09 要求 operator_id | EmployeeServiceImpl.java:160 | 见 P0-2 |

### REQ-4: 员工离职（逻辑删除与状态隔离）

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 逻辑删除，更新 status=0 | ✅ | design.md W10 | EmployeeServiceImpl.java:183-187 | updateResign 同时设置 status=0 与 is_deleted=1 |
| 释放许可、清除登录权限 | ✅ | design.md W10 | EmployeeServiceImpl.java:189-194 | 调用 authServiceClient，失败仅日志 |

### REQ-5: 异常与边界场景

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 树形结构循环引用校验 | ✅ | design.md 异常场景1 | DepartmentServiceImpl.java:98-102 | isDescendant 通过 path 前缀判断 |
| 部门下存在员工禁止删除 | ✅ | design.md 异常场景2 | DepartmentServiceImpl.java:126-134 | 查询 count 后抛 ORG_004 |
| 人员列表分页 | ✅ | design.md 异常场景3 | EmployeeMapper.xml:76-112 | count + limit 分页 |
| 并发调动冲突处理 | ✅ | design.md 异常场景4 | EmployeeServiceImpl.java:147-151 | updateByVersion 返回 0 时抛 ORG_009 |

---

## 4. Step 3 — 可读性检查

| 结果 | 说明（违规写 Ax.x 与 `path:行`） |
|------|--------------------------------|
| ⚠️ | A3.4 `DepartmentController.java:39` 行宽超过 120 字符 |
| ⚠️ | A3.4 `EmployeeServiceImplTest.java:205` 行宽超过 120 字符 |
| ✅ | 其余 A1-A7 项符合规范 |

---

## 5. Step 4 — 可靠性检查

### 5.1 Bug 模式（`scan-all-rules.sh` 预扫结果）

预扫命令：`bash /root/.agentix/skills/managed/dtazziboot-java-code-review/references/script/scan-all-rules.sh src/main src/test`

预扫命中：
- `[P0] G16.2 — CatchWithoutLogging: EmployeeServiceImpl.java:167`
- `[P0] G16.2 — CatchWithoutLogging: EmployeeServiceImpl.java:192`
- `[P0] G16.2 — CatchWithoutLogging: EmployeeServiceImpl.java:200`
- `[P1] M016 — JavaTimeDefaultTimeZone: EmployeeServiceImpl.java:161`
- `[P2] A3.4 — LineWidthExceeded: DepartmentController.java:39`
- `[P2] A3.4 — LineWidthExceeded: EmployeeServiceImplTest.java:205`

**复核说明**：
- 三个 `G16.2` 命中点为 catch 块内仅有 `LOGGER.warn(...)`，但 catch 块非空且已记录日志，符合 G16.2「捕获后须日志/告警/补救之一」的要求，本次审查将其降级为 **P2 观察项**，不视为阻塞性问题。
- `M016` 命中 `LocalDateTime.now()` 使用默认时区，按 bug-pattern-checklist.md 列为 **P1**。
- 两个 `A3.4` 行宽超限列为 **P2**。

### 5.2 可靠性军规（G）

| ID | 结果 | 等级 | 说明 |
|----|------|------|------|
| G1.1 | ✅ | — | 员工调动使用乐观锁，删除部门事务内查询计数 |
| G3.2 | ️ | P1 | EmployeeServiceImpl.java:165-194 在 `@Transactional` 内调用外部服务（approval/auth client），仅 try-catch 不阻断事务，但外部调用仍在事务内，存在长事务隐患 |
| G4.3 | ⚠️ | P1 | EmployeeMapper.xml:111 分页查询未限制最大 pageSize，深分页/大页码存在性能风险 |
| G8.1 | ✅ | — | catch 后均有日志记录 |
| G9.1 / G9.2 | ⚠️ | P1 | ApprovalServiceClientImpl / AuthServiceClientImpl 为占位实现，未配置超时、重试、降级策略；真实调用后需补全 |
| G11.1 | ✅ | — | 核心 Service 已有 Mockito 单测覆盖 |
| G11.3 | ⚠️ | P1 | EmployeeController.check 等入口未对 `field` 做白名单外非法值统一拦截；虽内部抛 BizException，但建议 Controller 层增加 `@Valid` 或校验注解 |
| G14.2 | ❌ | P0 | `tenantId` 通过 `@RequestParam(defaultValue = "1")` 从请求参数获取，可由用户任意指定，存在租户越权风险；应使用 TenantContext 注入 |
| G16.1 | ⚠️ | P1 | 核心链路（调动、离职、外部调用）缺少 Metrics 埋点 |
| G16.2 | ⚠️ | P2 | 外部服务失败仅 warn 日志，未引入补偿队列/告警 |

### 5.3 安全（S）

| ID | 结果 | 等级 | 说明 |
|----|------|------|------|
| S1.1 | ✅ | — | Mapper XML 全部使用 `#{}` 预编译 |
| S8.1 | ❌ | P0 | 所有 Controller 方法未接入鉴权，`tenantId` 由用户可控参数传入，未校验当前登录租户；应通过 TenantContext 注入 |
| S8.2 | ✅ | — | GET 仅用于查询，增删改使用 POST/PUT/DELETE |
| S9.2 | ✅ | — | 日志中未打印敏感字段完整内容 |

---

## 6. Step 5 — 自定义扩展检查

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|------|
| 自定义扩展 | `customized-checklist.md` U* | N/A | — | 未启用自定义规则 |

---

## 7. 结论

- **合并建议**：修复后合并
- **P0**：
  1. `tenantId` 来源不可控，存在租户越权风险（S8.1/G14.2）。
  2. 调动记录 `operatorId` 硬编码为 `0L`，未使用当前操作人，违反设计约束。
- **P1/P2**：
  1. 外部服务调用在 `@Transactional` 内执行，建议移至事务外或异步化（G3.2）。
  2. 分页未限制最大 pageSize（G4.3）。
  3. 外部客户端为占位实现，缺少超时/重试/降级（G9.1/G9.2）。
  4. `LocalDateTime.now()` 使用默认时区（M016）。
  5. 行宽超限（A3.4）共 2 处。
- **一句话**：功能实现与系分设计基本一致，但租户隔离和操作人字段存在阻塞性缺陷，外部调用的事务边界和分页性能也需优化。

---

## 7.1 问题片段（必填）

### P0-1 租户 ID 来自用户可控参数，存在越权风险

- **P0** `S8.1` `G14.2` `src/main/java/com/example/organization/controller/EmployeeController.java:38` — `tenantId` 由请求参数直接传入，未从登录上下文注入。

片段范围：`src/main/java/com/example/organization/controller/EmployeeController.java:35-40`

```java
L35|@GetMapping("/check")
L36|public ApiResponse<UniqueCheckResponse> check(@RequestParam String field,
L37|                                              @RequestParam String value,
L38|                                              @RequestParam(required = false, defaultValue = "1") Long tenantId) {
L39|    return ApiResponse.ok(employeeService.checkUnique(field, value, tenantId));
L40|}
```

### P0-2 调动记录操作人硬编码

- **P0** `REQ-3` `src/main/java/com/example/organization/service/impl/EmployeeServiceImpl.java:160` — `operatorId` 硬编码为 `0L`，未使用当前操作人 ID。

片段范围：`src/main/java/com/example/organization/service/impl/EmployeeServiceImpl.java:153-163`

```java
L153|TransferRecordDO record = new TransferRecordDO();
L154|record.setTenantId(tenantId);
L155|record.setEmployeeId(id);
L156|record.setOldDeptId(oldDeptId);
L157|record.setNewDeptId(request.getNewDeptId());
L158|record.setNewPosition(request.getNewPosition());
L159|record.setReason(request.getReason());
L160|record.setOperatorId(0L);
L161|record.setTransferTime(LocalDateTime.now());
L162|record.setDeleted(Boolean.FALSE);
L163|transferRecordMapper.insert(record);
```

### P1-1 外部服务调用在 `@Transactional` 内

- **P1** `G3.2` `src/main/java/com/example/organization/service/impl/EmployeeServiceImpl.java:165-194` — 调动/离职时外部调用在事务内，异常仅 catch 不阻断事务，但仍会拉长事务。

片段范围：`src/main/java/com/example/organization/service/impl/EmployeeServiceImpl.java:165-194`

```java
L165|try {
L166|    approvalServiceClient.updateDefaultApprover(id, request.getNewDeptId());
L167|} catch (Exception e) {
L168|    LOGGER.warn("调用审批流服务更新默认审批人失败，employeeId={}", id, e);
L169|}
...
L189|try {
L190|    authServiceClient.revokeLoginPermission(id);
L191|    authServiceClient.releaseLicense(id);
L192|} catch (Exception e) {
L193|    LOGGER.warn("调用权限/账号服务失败，employeeId={}", id, e);
L194|}
```

### P1-2 分页未限制最大 pageSize

- **P1** `G4.3` `src/main/resources/mapper/EmployeeMapper.xml:111` — `LIMIT` 直接使用 `#{size}`，未在业务层限制最大值。

片段范围：`src/main/resources/mapper/EmployeeMapper.xml:94-112`

```xml
L110|ORDER BY gmt_create DESC
L111|LIMIT #{offset}, #{size}
```

### P1-3 使用默认时区

- **P1** `M016` `src/main/java/com/example/organization/service/impl/EmployeeServiceImpl.java:161` — `LocalDateTime.now()` 依赖系统默认时区。

片段范围：`src/main/java/com/example/organization/service/impl/EmployeeServiceImpl.java:160-162`

```java
L160|record.setOperatorId(0L);
L161|record.setTransferTime(LocalDateTime.now());
L162|record.setDeleted(Boolean.FALSE);
```

### P2-1 行宽超限

- **P2** `A3.4` `src/main/java/com/example/organization/controller/DepartmentController.java:39` — 方法参数行超过 120 字符。

```java
L39|public ApiResponse<List<DepartmentVO>> getChildren(@PathVariable Long id,
```

### P2-2 行宽超限

- **P2** `A3.4` `src/test/java/com/example/organization/service/impl/EmployeeServiceImplTest.java:205` — 测试辅助方法行超过 120 字符。

```java
L205|private EmployeeDO buildEmployee(Long id, String employeeNo, String phone, Long deptId, String position, Long version) {
```

---

## 8. 修复任务列表

### P0

- [ ] **P0** `src/main/java/com/example/organization/controller/EmployeeController.java:38` / `src/main/java/com/example/organization/controller/DepartmentController.java:39` — 将 `tenantId` 改为从 `TenantContext` 注入，禁止从请求参数直接获取。
- [ ] **P0** `src/main/java/com/example/organization/service/impl/EmployeeServiceImpl.java:160` — 将 `record.setOperatorId(0L)` 替换为当前登录用户 ID（从 `TenantContext` 或方法参数传入）。

### P1

- [ ] **P1** `src/main/java/com/example/organization/service/impl/EmployeeServiceImpl.java:165-194` — 将外部服务调用（approval/auth）移出 `@Transactional` 方法，或采用事件/异步方式，避免事务内阻塞。
- [ ] **P1** `src/main/java/com/example/organization/model/request/EmployeeQueryRequest.java:12` / `src/main/java/com/example/organization/service/impl/EmployeeServiceImpl.java:107` — 对 `size` 设置上限（如 max 200），防止深分页/大页码拖垮数据库。
- [ ] **P1** `src/main/java/com/example/organization/integration/impl/ApprovalServiceClientImpl.java` / `AuthServiceClientImpl.java` — 补充真实远程调用实现，配置超时、重试、降级策略。
- [ ] **P1** `src/main/java/com/example/organization/service/impl/EmployeeServiceImpl.java:161` — 将 `LocalDateTime.now()` 改为 `LocalDateTime.now(ZoneId.of("Asia/Shanghai"))` 等显式时区。

### P2（可选）

- [ ] **P2** `src/main/java/com/example/organization/controller/DepartmentController.java:39` — 拆分超长参数行，保持行宽 ≤ 120 字符。
- [ ] **P2** `src/test/java/com/example/organization/service/impl/EmployeeServiceImplTest.java:205` — 拆分超长方法签名行，保持行宽 ≤ 120 字符。
