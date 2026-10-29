package com.example.organization.service.impl;

import com.example.organization.common.BizException;
import com.example.organization.common.ErrorCodeEnum;
import com.example.organization.common.PageResult;
import com.example.organization.dao.mapper.DepartmentMapper;
import com.example.organization.dao.mapper.EmployeeMapper;
import com.example.organization.dao.mapper.TransferRecordMapper;
import com.example.organization.integration.ApprovalServiceClient;
import com.example.organization.integration.AuthServiceClient;
import com.example.organization.model.dto.EmployeeVO;
import com.example.organization.model.dto.UniqueCheckResponse;
import com.example.organization.model.entity.DepartmentDO;
import com.example.organization.model.entity.EmployeeDO;
import com.example.organization.model.entity.TransferRecordDO;
import com.example.organization.model.request.CreateEmployeeRequest;
import com.example.organization.model.request.EmployeeQueryRequest;
import com.example.organization.model.request.ResignEmployeeRequest;
import com.example.organization.model.request.TransferEmployeeRequest;
import com.example.organization.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 员工服务实现。
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeServiceImpl.class);
    private static final Boolean NOT_DELETED = Boolean.FALSE;
    private static final Integer EMPLOYEE_STATUS_ACTIVE = 1;
    private static final Long INITIAL_VERSION = 0L;

    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;
    private final TransferRecordMapper transferRecordMapper;
    private final ApprovalServiceClient approvalServiceClient;
    private final AuthServiceClient authServiceClient;

    public EmployeeServiceImpl(EmployeeMapper employeeMapper,
                               DepartmentMapper departmentMapper,
                               TransferRecordMapper transferRecordMapper,
                               ApprovalServiceClient approvalServiceClient,
                               AuthServiceClient authServiceClient) {
        this.employeeMapper = employeeMapper;
        this.departmentMapper = departmentMapper;
        this.transferRecordMapper = transferRecordMapper;
        this.approvalServiceClient = approvalServiceClient;
        this.authServiceClient = authServiceClient;
    }

    @Override
    public UniqueCheckResponse checkUnique(String field, String value, Long tenantId) {
        if ("employeeNo".equalsIgnoreCase(field)) {
            return new UniqueCheckResponse(employeeMapper.selectByEmployeeNo(value, tenantId, NOT_DELETED) != null);
        }
        if ("phone".equalsIgnoreCase(field)) {
            return new UniqueCheckResponse(employeeMapper.selectByPhone(value, tenantId, NOT_DELETED) != null);
        }
        throw new BizException(ErrorCodeEnum.PARAMETER_ERROR.getCode(), "仅支持 employeeNo/phone 字段校验");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createEmployee(CreateEmployeeRequest request, Long tenantId) {
        DepartmentDO department = departmentMapper.selectById(request.getDeptId(), tenantId, NOT_DELETED);
        if (department == null) {
            throw new BizException(ErrorCodeEnum.EMPLOYEE_DEPARTMENT_NOT_FOUND.getCode(),
                    ErrorCodeEnum.EMPLOYEE_DEPARTMENT_NOT_FOUND.getMessage());
        }

        if (employeeMapper.selectByEmployeeNo(request.getEmployeeNo(), tenantId, NOT_DELETED) != null) {
            throw new BizException(ErrorCodeEnum.EMPLOYEE_NO_ALREADY_EXISTS.getCode(),
                    ErrorCodeEnum.EMPLOYEE_NO_ALREADY_EXISTS.getMessage());
        }
        if (employeeMapper.selectByPhone(request.getPhone(), tenantId, NOT_DELETED) != null) {
            throw new BizException(ErrorCodeEnum.PHONE_ALREADY_EXISTS.getCode(),
                    ErrorCodeEnum.PHONE_ALREADY_EXISTS.getMessage());
        }

        EmployeeDO employee = new EmployeeDO();
        employee.setTenantId(tenantId);
        employee.setName(request.getName());
        employee.setEmployeeNo(request.getEmployeeNo());
        employee.setPhone(request.getPhone());
        employee.setDeptId(request.getDeptId());
        employee.setPosition(request.getPosition());
        employee.setStatus(EMPLOYEE_STATUS_ACTIVE);
        employee.setVersion(INITIAL_VERSION);
        employee.setDeleted(NOT_DELETED);

        employeeMapper.insert(employee);
        return employee.getId();
    }

    @Override
    public PageResult<EmployeeVO> listEmployees(EmployeeQueryRequest query, Long tenantId) {
        query.setTenantId(tenantId);
        long total = employeeMapper.countByCondition(query);
        if (total == 0) {
            return PageResult.empty();
        }
        List<EmployeeDO> employees = employeeMapper.listByCondition(query);
        List<EmployeeVO> vos = employees.stream().map(this::convertToVo).collect(Collectors.toList());
        return PageResult.of(total, vos, query.getPage(), query.getSize());
    }

    @Override
    public EmployeeVO getEmployeeById(Long id, Long tenantId) {
        EmployeeDO employee = employeeMapper.selectById(id, tenantId, NOT_DELETED);
        if (employee == null) {
            throw new BizException(ErrorCodeEnum.EMPLOYEE_NOT_FOUND_OR_RESIGNED.getCode(),
                    ErrorCodeEnum.EMPLOYEE_NOT_FOUND_OR_RESIGNED.getMessage());
        }
        return convertToVo(employee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferEmployee(Long id, TransferEmployeeRequest request, Long tenantId) {
        EmployeeDO employee = employeeMapper.selectById(id, tenantId, NOT_DELETED);
        if (employee == null || !EMPLOYEE_STATUS_ACTIVE.equals(employee.getStatus())) {
            throw new BizException(ErrorCodeEnum.EMPLOYEE_NOT_FOUND_OR_RESIGNED.getCode(),
                    ErrorCodeEnum.EMPLOYEE_NOT_FOUND_OR_RESIGNED.getMessage());
        }

        DepartmentDO newDepartment = departmentMapper.selectById(request.getNewDeptId(), tenantId, NOT_DELETED);
        if (newDepartment == null) {
            throw new BizException(ErrorCodeEnum.DEPARTMENT_NOT_FOUND.getCode(),
                    ErrorCodeEnum.DEPARTMENT_NOT_FOUND.getMessage());
        }

        Long oldDeptId = employee.getDeptId();
        employee.setDeptId(request.getNewDeptId());
        employee.setPosition(request.getNewPosition());

        int affected = employeeMapper.updateByVersion(employee);
        if (affected == 0) {
            throw new BizException(ErrorCodeEnum.EMPLOYEE_CONCURRENT_UPDATE.getCode(),
                    ErrorCodeEnum.EMPLOYEE_CONCURRENT_UPDATE.getMessage());
        }

        TransferRecordDO record = new TransferRecordDO();
        record.setTenantId(tenantId);
        record.setEmployeeId(id);
        record.setOldDeptId(oldDeptId);
        record.setNewDeptId(request.getNewDeptId());
        record.setNewPosition(request.getNewPosition());
        record.setReason(request.getReason());
        record.setOperatorId(0L);
        record.setTransferTime(LocalDateTime.now());
        record.setDeleted(Boolean.FALSE);
        transferRecordMapper.insert(record);

        try {
            approvalServiceClient.updateDefaultApprover(id, request.getNewDeptId());
        } catch (Exception e) {
            LOGGER.warn("调用审批流服务更新默认审批人失败，employeeId={}", id, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resignEmployee(Long id, ResignEmployeeRequest request, Long tenantId) {
        EmployeeDO employee = employeeMapper.selectById(id, tenantId, NOT_DELETED);
        if (employee == null || !EMPLOYEE_STATUS_ACTIVE.equals(employee.getStatus())) {
            throw new BizException(ErrorCodeEnum.EMPLOYEE_NOT_FOUND_OR_RESIGNED.getCode(),
                    ErrorCodeEnum.EMPLOYEE_NOT_FOUND_OR_RESIGNED.getMessage());
        }

        validateResignDate(request.getResignDate());

        int affected = employeeMapper.updateResign(id, tenantId);
        if (affected == 0) {
            throw new BizException(ErrorCodeEnum.EMPLOYEE_NOT_FOUND_OR_RESIGNED.getCode(),
                    ErrorCodeEnum.EMPLOYEE_NOT_FOUND_OR_RESIGNED.getMessage());
        }

        try {
            authServiceClient.revokeLoginPermission(id);
            authServiceClient.releaseLicense(id);
        } catch (Exception e) {
            LOGGER.warn("调用权限/账号服务失败，employeeId={}", id, e);
        }
    }

    private void validateResignDate(String resignDate) {
        try {
            LocalDate.parse(resignDate);
        } catch (DateTimeParseException e) {
            throw new BizException(ErrorCodeEnum.PARAMETER_ERROR.getCode(), "离职日期格式不正确");
        }
    }

    private EmployeeVO convertToVo(EmployeeDO employee) {
        EmployeeVO vo = new EmployeeVO();
        vo.setId(employee.getId());
        vo.setName(employee.getName());
        vo.setEmployeeNo(employee.getEmployeeNo());
        vo.setPhone(employee.getPhone());
        vo.setDeptId(employee.getDeptId());
        vo.setPosition(employee.getPosition());
        vo.setStatus(employee.getStatus());
        vo.setVersion(employee.getVersion());
        return vo;
    }
}
