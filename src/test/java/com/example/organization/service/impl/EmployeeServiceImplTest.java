package com.example.organization.service.impl;

import com.example.organization.common.BizException;
import com.example.organization.common.TenantContext;
import com.example.organization.dao.mapper.DepartmentMapper;
import com.example.organization.dao.mapper.EmployeeMapper;
import com.example.organization.dao.mapper.TransferRecordMapper;
import com.example.organization.integration.ApprovalServiceClient;
import com.example.organization.integration.AuthServiceClient;
import com.example.organization.model.dto.UniqueCheckResponse;
import com.example.organization.model.entity.DepartmentDO;
import com.example.organization.model.entity.EmployeeDO;
import com.example.organization.model.request.CreateEmployeeRequest;
import com.example.organization.model.request.ResignEmployeeRequest;
import com.example.organization.model.request.TransferEmployeeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * EmployeeServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    private static final Long TENANT_ID = 1L;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private DepartmentMapper departmentMapper;

    @Mock
    private TransferRecordMapper transferRecordMapper;

    @Mock
    private ApprovalServiceClient approvalServiceClient;

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeDO activeEmployee;

    @BeforeEach
    void setUp() {
        activeEmployee = buildEmployee(100L, "10086", "13800138000", 2L, "Java开发", 1L);
    }

    // ==================== checkUnique 测试 ====================

    @Test
    @DisplayName("工号已存在时，应返回存在")
    void should_return_exist_when_employee_no_duplicate() {
        // Arrange
        when(employeeMapper.selectByEmployeeNo("10086", TENANT_ID, Boolean.FALSE)).thenReturn(activeEmployee);

        // Act
        UniqueCheckResponse response = employeeService.checkUnique("employeeNo", "10086", TENANT_ID);

        // Assert
        assertThat(response.isExist()).isTrue();
    }

    @Test
    @DisplayName("手机号不存在时，应返回不存在")
    void should_return_not_exist_when_phone_not_duplicate() {
        // Arrange
        when(employeeMapper.selectByPhone("13900139000", TENANT_ID, Boolean.FALSE)).thenReturn(null);

        // Act
        UniqueCheckResponse response = employeeService.checkUnique("phone", "13900139000", TENANT_ID);

        // Assert
        assertThat(response.isExist()).isFalse();
    }

    // ==================== createEmployee 测试 ====================

    @Test
    @DisplayName("输入合法时，应创建员工成功")
    void should_create_employee_when_input_valid() {
        // Arrange
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setName("张三");
        request.setEmployeeNo("10086");
        request.setPhone("13800138000");
        request.setDeptId(2L);
        request.setPosition("Java开发");

        when(departmentMapper.selectById(2L, TENANT_ID, Boolean.FALSE)).thenReturn(new DepartmentDO());
        when(employeeMapper.selectByEmployeeNo("10086", TENANT_ID, Boolean.FALSE)).thenReturn(null);
        when(employeeMapper.selectByPhone("13800138000", TENANT_ID, Boolean.FALSE)).thenReturn(null);
        when(employeeMapper.insert(any(EmployeeDO.class))).thenAnswer(invocation -> {
            EmployeeDO e = invocation.getArgument(0);
            e.setId(1L);
            return 1;
        });

        // Act
        Long id = employeeService.createEmployee(request, TENANT_ID);

        // Assert
        assertThat(id).isEqualTo(1L);
        verify(employeeMapper, times(1)).insert(any(EmployeeDO.class));
    }

    @Test
    @DisplayName("工号已存在时，应抛出异常")
    void should_throw_when_employee_no_exists() {
        // Arrange
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setEmployeeNo("10086");

        when(departmentMapper.selectById(anyLong(), anyLong(), any(Boolean.class))).thenReturn(new DepartmentDO());
        when(employeeMapper.selectByEmployeeNo("10086", TENANT_ID, Boolean.FALSE)).thenReturn(activeEmployee);

        // Act & Assert
        assertThatThrownBy(() -> employeeService.createEmployee(request, TENANT_ID))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("工号已存在");
        verify(employeeMapper, never()).insert(any());
    }

    // ==================== transferEmployee 测试 ====================

    @Test
    @DisplayName("调动成功时，应更新员工并记录调动历史")
    void should_transfer_employee_successfully() {
        TenantContext.setUserId(999L);
        try {
            // Arrange
            TransferEmployeeRequest request = new TransferEmployeeRequest();
            request.setNewDeptId(3L);
            request.setNewPosition("架构师");
            request.setReason("业务调整");

            when(employeeMapper.selectById(100L, TENANT_ID, Boolean.FALSE)).thenReturn(activeEmployee);
            when(departmentMapper.selectById(3L, TENANT_ID, Boolean.FALSE)).thenReturn(new DepartmentDO());
            when(employeeMapper.updateByVersion(any(EmployeeDO.class))).thenReturn(1);
            when(transferRecordMapper.insert(any())).thenReturn(1);

            // Act
            employeeService.transferEmployee(100L, request, TENANT_ID);

            // Assert
            verify(employeeMapper, times(1)).updateByVersion(any(EmployeeDO.class));
            verify(transferRecordMapper, times(1)).insert(any());
            verify(approvalServiceClient, times(1)).updateDefaultApprover(100L, 3L);
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    @DisplayName("乐观锁冲突时，应抛出并发异常")
    void should_throw_conflict_when_transfer_concurrent() {
        // Arrange
        TransferEmployeeRequest request = new TransferEmployeeRequest();
        request.setNewDeptId(3L);
        request.setNewPosition("架构师");

        when(employeeMapper.selectById(100L, TENANT_ID, Boolean.FALSE)).thenReturn(activeEmployee);
        when(departmentMapper.selectById(3L, TENANT_ID, Boolean.FALSE)).thenReturn(new DepartmentDO());
        when(employeeMapper.updateByVersion(any(EmployeeDO.class))).thenReturn(0);

        // Act & Assert
        assertThatThrownBy(() -> employeeService.transferEmployee(100L, request, TENANT_ID))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("员工信息已被他人修改");
        verify(transferRecordMapper, never()).insert(any());
        verify(approvalServiceClient, never()).updateDefaultApprover(anyLong(), anyLong());
    }

    // ==================== resignEmployee 测试 ====================

    @Test
    @DisplayName("在职员工办理离职，应成功并调用权限服务")
    void should_resign_employee_successfully() {
        // Arrange
        ResignEmployeeRequest request = new ResignEmployeeRequest();
        request.setResignDate("2023-11-01");

        when(employeeMapper.selectById(100L, TENANT_ID, Boolean.FALSE)).thenReturn(activeEmployee);
        when(employeeMapper.updateResign(100L, TENANT_ID)).thenReturn(1);

        // Act
        employeeService.resignEmployee(100L, request, TENANT_ID);

        // Assert
        verify(authServiceClient, times(1)).revokeLoginPermission(100L);
        verify(authServiceClient, times(1)).releaseLicense(100L);
    }

    private EmployeeDO buildEmployee(Long id, String employeeNo, String phone,
                                     Long deptId, String position, Long version) {
        EmployeeDO employee = new EmployeeDO();
        employee.setId(id);
        employee.setEmployeeNo(employeeNo);
        employee.setPhone(phone);
        employee.setDeptId(deptId);
        employee.setPosition(position);
        employee.setStatus(1);
        employee.setVersion(version);
        employee.setTenantId(TENANT_ID);
        employee.setDeleted(Boolean.FALSE);
        return employee;
    }
}
