package com.example.organization.dao.mapper;

import com.example.organization.model.entity.DepartmentDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门数据访问层。
 */
public interface DepartmentMapper {

    List<DepartmentDO> selectAll(@Param("tenantId") Long tenantId, @Param("deleted") Boolean deleted);

    DepartmentDO selectById(@Param("id") Long id,
                            @Param("tenantId") Long tenantId,
                            @Param("deleted") Boolean deleted);

    List<DepartmentDO> selectByParentId(@Param("parentId") Long parentId,
                                        @Param("tenantId") Long tenantId,
                                        @Param("deleted") Boolean deleted);

    List<DepartmentDO> selectByPathPrefix(@Param("tenantId") Long tenantId,
                                          @Param("pathPrefix") String pathPrefix,
                                          @Param("deleted") Boolean deleted);

    int updateParentAndPath(@Param("id") Long id,
                            @Param("parentId") Long parentId,
                            @Param("path") String path,
                            @Param("tenantId") Long tenantId);

    int batchUpdatePath(@Param("tenantId") Long tenantId,
                          @Param("oldPrefix") String oldPrefix,
                          @Param("newPrefix") String newPrefix,
                          @Param("deleted") Boolean deleted);

    int logicDelete(@Param("id") Long id, @Param("tenantId") Long tenantId);

    long countByParentId(@Param("parentId") Long parentId,
                         @Param("tenantId") Long tenantId,
                         @Param("deleted") Boolean deleted);
}
