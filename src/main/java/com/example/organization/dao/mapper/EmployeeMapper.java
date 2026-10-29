package com.example.organization.dao.mapper;

import com.example.organization.model.entity.EmployeeDO;
import com.example.organization.model.request.EmployeeQueryRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 员工数据访问层。
 */
public interface EmployeeMapper {

    EmployeeDO selectById(@Param("id") Long id,
                        @Param("tenantId") Long tenantId,
                        @Param("deleted") Boolean deleted);

    EmployeeDO selectByEmployeeNo(@Param("employeeNo") String employeeNo,
                                  @Param("tenantId") Long tenantId,
                                  @Param("deleted") Boolean deleted);

    EmployeeDO selectByPhone(@Param("phone") String phone,
                             @Param("tenantId") Long tenantId,
                             @Param("deleted") Boolean deleted);

    int insert(EmployeeDO record);

    int updateByVersion(EmployeeDO record);

    int updateResign(@Param("id") Long id, @Param("tenantId") Long tenantId);

    long countByCondition(EmployeeQueryRequest query);

    List<EmployeeDO> listByCondition(EmployeeQueryRequest query);
}
