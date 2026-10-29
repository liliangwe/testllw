package com.example.organization.controller;

import com.example.organization.common.ApiResponse;
import com.example.organization.common.PageResult;
import com.example.organization.model.dto.EmployeeVO;
import com.example.organization.model.dto.UniqueCheckResponse;
import com.example.organization.model.request.CreateEmployeeRequest;
import com.example.organization.model.request.EmployeeQueryRequest;
import com.example.organization.model.request.ResignEmployeeRequest;
import com.example.organization.model.request.TransferEmployeeRequest;
import com.example.organization.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 员工控制器。
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/check")
    public ApiResponse<UniqueCheckResponse> check(@RequestParam String field,
                                                  @RequestParam String value,
                                                  @RequestParam(required = false, defaultValue = "1") Long tenantId) {
        return ApiResponse.ok(employeeService.checkUnique(field, value, tenantId));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody @Valid CreateEmployeeRequest request,
                                    @RequestParam(required = false, defaultValue = "1") Long tenantId) {
        return ApiResponse.ok(employeeService.createEmployee(request, tenantId));
    }

    @GetMapping
    public ApiResponse<PageResult<EmployeeVO>> list(EmployeeQueryRequest query,
                                                  @RequestParam(required = false, defaultValue = "1") Long tenantId) {
        return ApiResponse.ok(employeeService.listEmployees(query, tenantId));
    }

    @GetMapping("/{id}")
    public ApiResponse<EmployeeVO> getById(@PathVariable Long id,
                                           @RequestParam(required = false, defaultValue = "1") Long tenantId) {
        return ApiResponse.ok(employeeService.getEmployeeById(id, tenantId));
    }

    @PostMapping("/{id}/transfer")
    public ApiResponse<Void> transfer(@PathVariable Long id,
                                      @RequestBody @Valid TransferEmployeeRequest request,
                                      @RequestParam(required = false, defaultValue = "1") Long tenantId) {
        employeeService.transferEmployee(id, request, tenantId);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/resign")
    public ApiResponse<Void> resign(@PathVariable Long id,
                                    @RequestBody @Valid ResignEmployeeRequest request,
                                    @RequestParam(required = false, defaultValue = "1") Long tenantId) {
        employeeService.resignEmployee(id, request, tenantId);
        return ApiResponse.ok();
    }
}
