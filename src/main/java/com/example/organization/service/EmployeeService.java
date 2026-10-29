package com.example.organization.service;

import com.example.organization.common.PageResult;
import com.example.organization.model.dto.EmployeeVO;
import com.example.organization.model.dto.UniqueCheckResponse;
import com.example.organization.model.request.CreateEmployeeRequest;
import com.example.organization.model.request.EmployeeQueryRequest;
import com.example.organization.model.request.ResignEmployeeRequest;
import com.example.organization.model.request.TransferEmployeeRequest;

/**
 * 员工服务。
 */
public interface EmployeeService {

    /**
     * 校验字段唯一性。
     */
    UniqueCheckResponse checkUnique(String field, String value, Long tenantId);

    /**
     * 新增员工。
     */
    Long createEmployee(CreateEmployeeRequest request, Long tenantId);

    /**
     * 分页查询员工。
     */
    PageResult<EmployeeVO> listEmployees(EmployeeQueryRequest query, Long tenantId);

    /**
     * 查看员工详情。
     */
    EmployeeVO getEmployeeById(Long id, Long tenantId);

    /**
     * 调动员工。
     */
    void transferEmployee(Long id, TransferEmployeeRequest request, Long tenantId);

    /**
     * 办理离职。
     */
    void resignEmployee(Long id, ResignEmployeeRequest request, Long tenantId);
}
