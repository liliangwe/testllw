package com.example.organization.service.impl;

import com.example.organization.common.BizException;
import com.example.organization.common.ErrorCodeEnum;
import com.example.organization.dao.mapper.DepartmentMapper;
import com.example.organization.dao.mapper.EmployeeMapper;
import com.example.organization.model.dto.DepartmentVO;
import com.example.organization.model.entity.DepartmentDO;
import com.example.organization.model.request.EmployeeQueryRequest;
import com.example.organization.model.request.MoveDepartmentRequest;
import com.example.organization.service.DepartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 部门服务实现。
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DepartmentServiceImpl.class);
    private static final Boolean NOT_DELETED = Boolean.FALSE;
    private static final Long ROOT_PARENT_ID = 0L;

    private final DepartmentMapper departmentMapper;
    private final EmployeeMapper employeeMapper;

    public DepartmentServiceImpl(DepartmentMapper departmentMapper, EmployeeMapper employeeMapper) {
        this.departmentMapper = departmentMapper;
        this.employeeMapper = employeeMapper;
    }

    @Override
    public List<DepartmentVO> getDepartmentTree(Long tenantId) {
        List<DepartmentDO> all = departmentMapper.selectAll(tenantId, NOT_DELETED);
        if (all.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, DepartmentVO> voMap = new HashMap<>(all.size() * 2);
        List<DepartmentVO> roots = new ArrayList<>();
        for (DepartmentDO dept : all) {
            DepartmentVO vo = convertToVo(dept);
            voMap.put(vo.getId(), vo);
        }
        for (DepartmentDO dept : all) {
            DepartmentVO vo = voMap.get(dept.getId());
            if (Objects.equals(dept.getParentId(), ROOT_PARENT_ID)) {
                roots.add(vo);
            } else {
                DepartmentVO parent = voMap.get(dept.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                }
            }
        }
        roots.sort(Comparator.comparingInt(DepartmentVO::getSortOrder).thenComparingLong(DepartmentVO::getId));
        return roots;
    }

    @Override
    public List<DepartmentVO> getChildren(Long id, Long tenantId) {
        List<DepartmentDO> children = departmentMapper.selectByParentId(id, tenantId, NOT_DELETED);
        return children.stream().map(this::convertToVo).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveDepartment(Long id, MoveDepartmentRequest request, Long tenantId) {
        Long newParentId = request.getNewParentId();
        if (Objects.equals(id, newParentId)) {
            throw new BizException(ErrorCodeEnum.DEPARTMENT_CYCLE_NOT_ALLOWED.getCode(),
                    ErrorCodeEnum.DEPARTMENT_CYCLE_NOT_ALLOWED.getMessage());
        }

        DepartmentDO department = departmentMapper.selectById(id, tenantId, NOT_DELETED);
        if (department == null) {
            throw new BizException(ErrorCodeEnum.DEPARTMENT_NOT_FOUND.getCode(),
                    ErrorCodeEnum.DEPARTMENT_NOT_FOUND.getMessage());
        }

        DepartmentDO newParent = departmentMapper.selectById(newParentId, tenantId, NOT_DELETED);
        if (newParent == null) {
            throw new BizException(ErrorCodeEnum.PARENT_DEPARTMENT_NOT_FOUND.getCode(),
                    ErrorCodeEnum.PARENT_DEPARTMENT_NOT_FOUND.getMessage());
        }

        // 校验目标父部门是否是被移动部门自身或其子孙节点
        if (isDescendant(department, newParentId, tenantId)) {
            throw new BizException(ErrorCodeEnum.DEPARTMENT_CYCLE_NOT_ALLOWED.getCode(),
                    ErrorCodeEnum.DEPARTMENT_CYCLE_NOT_ALLOWED.getMessage());
        }

        String newPath = buildPath(newParent, id);
        String oldPath = department.getPath();

        departmentMapper.updateParentAndPath(id, newParentId, newPath, tenantId);

        // 级联更新子孙节点的 path
        if (oldPath != null && !oldPath.equals(newPath)) {
            String oldPrefix = oldPath + "-";
            String newPrefix = newPath + "-";
            departmentMapper.batchUpdatePath(tenantId, oldPrefix, newPrefix, NOT_DELETED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDepartment(Long id, Long tenantId) {
        DepartmentDO department = departmentMapper.selectById(id, tenantId, NOT_DELETED);
        if (department == null) {
            throw new BizException(ErrorCodeEnum.DEPARTMENT_NOT_FOUND.getCode(),
                    ErrorCodeEnum.DEPARTMENT_NOT_FOUND.getMessage());
        }

        EmployeeQueryRequest query = new EmployeeQueryRequest();
        query.setTenantId(tenantId);
        query.setDeptId(id);
        query.setStatus(1);
        long employeeCount = employeeMapper.countByCondition(query);
        if (employeeCount > 0) {
            throw new BizException(ErrorCodeEnum.DEPARTMENT_HAS_EMPLOYEES.getCode(),
                    "该部门下存在" + employeeCount + "名员工，请先转移人员后再删除");
        }

        departmentMapper.logicDelete(id, tenantId);
    }

    private boolean isDescendant(DepartmentDO ancestor, Long targetId, Long tenantId) {
        DepartmentDO target = departmentMapper.selectById(targetId, tenantId, NOT_DELETED);
        if (target == null || target.getPath() == null || ancestor.getPath() == null) {
            return false;
        }
        return target.getPath().startsWith(ancestor.getPath() + "-") || target.getPath().equals(ancestor.getPath());
    }

    private String buildPath(DepartmentDO parent, Long id) {
        if (Objects.equals(parent.getParentId(), ROOT_PARENT_ID)) {
            return String.valueOf(id);
        }
        return parent.getPath() + "-" + id;
    }

    private DepartmentVO convertToVo(DepartmentDO dept) {
        DepartmentVO vo = new DepartmentVO();
        vo.setId(dept.getId());
        vo.setName(dept.getName());
        vo.setParentId(dept.getParentId());
        vo.setPath(dept.getPath());
        vo.setSortOrder(dept.getSortOrder());
        return vo;
    }
}
