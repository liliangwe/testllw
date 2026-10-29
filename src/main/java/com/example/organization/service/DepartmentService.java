package com.example.organization.service;

import com.example.organization.model.dto.DepartmentVO;
import com.example.organization.model.request.MoveDepartmentRequest;

import java.util.List;

/**
 * 部门服务。
 */
public interface DepartmentService {

    /**
     * 获取部门树。
     */
    List<DepartmentVO> getDepartmentTree(Long tenantId);

    /**
     * 获取指定部门的直接子部门。
     */
    List<DepartmentVO> getChildren(Long id, Long tenantId);

    /**
     * 移动部门。
     */
    void moveDepartment(Long id, MoveDepartmentRequest request, Long tenantId);

    /**
     * 删除部门。
     */
    void deleteDepartment(Long id, Long tenantId);
}
