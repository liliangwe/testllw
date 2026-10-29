package com.example.organization.controller;

import com.example.organization.common.ApiResponse;
import com.example.organization.model.dto.DepartmentVO;
import com.example.organization.model.request.MoveDepartmentRequest;
import com.example.organization.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 部门控制器。
 */
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping("/tree")
    public ApiResponse<List<DepartmentVO>> getTree(@RequestParam(required = false, defaultValue = "1") Long tenantId) {
        return ApiResponse.ok(departmentService.getDepartmentTree(tenantId));
    }

    @GetMapping("/{id}/children")
    public ApiResponse<List<DepartmentVO>> getChildren(@PathVariable Long id,
                                                         @RequestParam(required = false, defaultValue = "1") Long tenantId) {
        return ApiResponse.ok(departmentService.getChildren(id, tenantId));
    }

    @PutMapping("/{id}/move")
    public ApiResponse<Void> move(@PathVariable Long id,
                                    @RequestBody @Valid MoveDepartmentRequest request,
                                    @RequestParam(required = false, defaultValue = "1") Long tenantId) {
        departmentService.moveDepartment(id, request, tenantId);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id,
                                    @RequestParam(required = false, defaultValue = "1") Long tenantId) {
        departmentService.deleteDepartment(id, tenantId);
        return ApiResponse.ok();
    }
}
