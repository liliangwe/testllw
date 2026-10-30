package com.example.demo.track.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 埋点统计出参：折线（trend）+ 柱状/饼图（dimensionStats）。
 */
public record TrackStatsResponse(Long totalCount, List<TrendPoint> trend,
                                 List<DimensionStat> dimensionStats) {

    public record TrendPoint(String date, Long count) {
    }

    public record DimensionStat(String dimensionValue, Long count, BigDecimal ratio) {
    }
}
