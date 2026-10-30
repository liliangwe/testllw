package com.example.demo.track.mapper;

import com.example.demo.track.entity.ApiInvokeTrack;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 埋点数据访问。
 */
@Mapper
public interface ApiInvokeTrackMapper {

    int insert(ApiInvokeTrack track);

    Long countByCondition(@Param("apiCode") String apiCode,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);

    /**
     * 按天聚合趋势，返回 [{date, count}]。
     */
    List<Map<String, Object>> countTrendByDay(@Param("apiCode") String apiCode,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 按维度分组计数，dimensionColumn 来自枚举白名单。
     */
    List<Map<String, Object>> countByDimension(@Param("dimensionColumn") String dimensionColumn,
                                               @Param("apiCode") String apiCode,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);
}
