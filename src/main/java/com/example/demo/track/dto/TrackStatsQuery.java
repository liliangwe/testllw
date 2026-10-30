package com.example.demo.track.dto;

import java.time.LocalDateTime;

/**
 * 埋点统计查询条件。
 */
public record TrackStatsQuery(String dimension, String apiCode,
                              LocalDateTime startTime, LocalDateTime endTime) {
}
