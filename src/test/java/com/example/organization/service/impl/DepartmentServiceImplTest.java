package com.example.organization.service.impl;

import com.example.organization.common.BizException;
import com.example.organization.dao.mapper.DepartmentMapper;
import com.example.organization.dao.mapper.EmployeeMapper;
import com.example.organization.model.dto.DepartmentVO;
import com.example.organization.model.entity.DepartmentDO;
import com.example.organization.model.request.EmployeeQueryRequest;
import com.example.organization.model.request.MoveDepartmentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DepartmentServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    private static final Long TENANT_ID = 1L;

    @Mock
    private DepartmentMapper departmentMapper;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private DepartmentDO root;
    private DepartmentDO child;
    private DepartmentDO grandChild;

    @BeforeEach
    void setUp() {
        root = buildDepartment(1L, 0L, "研发部", "1", 0L);
        child = buildDepartment(2L, 1L, "前端组", "1-2", 1L);
        grandChild = buildDepartment(3L, 2L, "前端一组", "1-2-3", 2L);
    }

    // ==================== getDepartmentTree 测试 ====================

    @Test
    @DisplayName("存在部门时，应返回正确的部门树结构")
    void should_return_tree_when_departments_exist() {
        // Arrange
        when(departmentMapper.selectAll(TENANT_ID, Boolean.FALSE))
                .thenReturn(Arrays.asList(root, child, grandChild));

        // Act
        List<DepartmentVO> tree = departmentService.getDepartmentTree(TENANT_ID);

        // Assert
        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).getId()).isEqualTo(1L);
        assertThat(tree.get(0).getChildren()).hasSize(1);
        assertThat(tree.get(0).getChildren().get(0).getId()).isEqualTo(2L);
        assertThat(tree.get(0).getChildren().get(0).getChildren()).hasSize(1);
    }

    @Test
    @DisplayName("无部门时，应返回空列表")
    void should_return_empty_tree_when_no_departments() {
        // Arrange
        when(departmentMapper.selectAll(TENANT_ID, Boolean.FALSE))
                .thenReturn(Collections.emptyList());

        // Act
        List<DepartmentVO> tree = departmentService.getDepartmentTree(TENANT_ID);

        // Assert
        assertThat(tree).isEmpty();
    }

    // ==================== moveDepartment 测试 ====================

    @Test
    @DisplayName("移动部门到合法父部门下，应更新父部门和 path")
    void should_move_department_when_target_is_valid() {
        // Arrange
        MoveDepartmentRequest request = new MoveDepartmentRequest();
        request.setNewParentId(1L);

        DepartmentDO target = buildDepartment(1L, 0L, "研发部", "1", 0L);

        when(departmentMapper.selectById(2L, TENANT_ID, Boolean.FALSE)).thenReturn(child);
        when(departmentMapper.selectById(1L, TENANT_ID, Boolean.FALSE)).thenReturn(target);

        // Act
        departmentService.moveDepartment(2L, request, TENANT_ID);

        // Assert
        verify(departmentMapper, times(1)).updateParentAndPath(eq(2L), eq(1L), any(), eq(TENANT_ID));
    }

    @Test
    @DisplayName("移动部门到自身下，应抛出循环引用异常")
    void should_throw_when_move_to_self() {
        // Arrange
        MoveDepartmentRequest request = new MoveDepartmentRequest();
        request.setNewParentId(2L);

        // Act & Assert
        assertThatThrownBy(() -> departmentService.moveDepartment(2L, request, TENANT_ID))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不能将部门移动到自身或其子孙节点下");
        verify(departmentMapper, never()).updateParentAndPath(any(), any(), any(), any());
    }

    @Test
    @DisplayName("移动部门到子孙节点下，应抛出循环引用异常")
    void should_throw_when_move_to_descendant() {
        // Arrange
        MoveDepartmentRequest request = new MoveDepartmentRequest();
        request.setNewParentId(3L);

        when(departmentMapper.selectById(2L, TENANT_ID, Boolean.FALSE)).thenReturn(child);
        when(departmentMapper.selectById(3L, TENANT_ID, Boolean.FALSE)).thenReturn(grandChild);

        // Act & Assert
        assertThatThrownBy(() -> departmentService.moveDepartment(2L, request, TENANT_ID))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不能将部门移动到自身或其子孙节点下");
        verify(departmentMapper, never()).updateParentAndPath(any(), any(), any(), any());
    }

    // ==================== deleteDepartment 测试 ====================

    @Test
    @DisplayName("部门下无员工时，应逻辑删除成功")
    void should_delete_when_no_employees() {
        // Arrange
        when(departmentMapper.selectById(1L, TENANT_ID, Boolean.FALSE)).thenReturn(root);
        when(employeeMapper.countByCondition(any(EmployeeQueryRequest.class))).thenReturn(0L);

        // Act
        departmentService.deleteDepartment(1L, TENANT_ID);

        // Assert
        verify(departmentMapper, times(1)).logicDelete(1L, TENANT_ID);
    }

    @Test
    @DisplayName("部门下存在员工时，应拒绝删除")
    void should_throw_when_delete_department_with_employees() {
        // Arrange
        when(departmentMapper.selectById(1L, TENANT_ID, Boolean.FALSE)).thenReturn(root);
        when(employeeMapper.countByCondition(any(EmployeeQueryRequest.class))).thenReturn(5L);

        // Act & Assert
        assertThatThrownBy(() -> departmentService.deleteDepartment(1L, TENANT_ID))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("部门下存在5名员工");
        verify(departmentMapper, never()).logicDelete(any(), any());
    }

    private DepartmentDO buildDepartment(Long id, Long parentId, String name, String path, Long version) {
        DepartmentDO dept = new DepartmentDO();
        dept.setId(id);
        dept.setParentId(parentId);
        dept.setName(name);
        dept.setPath(path);
        dept.setSortOrder(0);
        dept.setTenantId(TENANT_ID);
        dept.setStatus(1);
        dept.setDeleted(Boolean.FALSE);
        return dept;
    }
}
