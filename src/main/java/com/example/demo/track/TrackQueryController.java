package com.example.demo.track;

import com.example.demo.common.Result;
import com.example.demo.track.dto.TrackStatsQuery;
import com.example.demo.track.dto.TrackStatsResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 埋点统计查询接口（W05）。
 */
@RestController
@RequestMapping("/api/track")
public class TrackQueryController {

    private static final int DEFAULT_STATS_DAYS = 7;

    private final TrackService trackService;

    public TrackQueryController(TrackService trackService) {
        this.trackService = trackService;
    }

    @GetMapping("/stats")
    public Result<TrackStatsResponse> stats(
            @RequestParam String dimension,
            @RequestParam(required = false) String apiCode,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        LocalDateTime effectiveEnd = endTime == null ? LocalDateTime.now() : endTime;
        LocalDateTime effectiveStart = startTime == null
                ? effectiveEnd.minusDays(DEFAULT_STATS_DAYS) : startTime;
        TrackStatsQuery query = new TrackStatsQuery(dimension, apiCode, effectiveStart, effectiveEnd);
        return Result.success(trackService.stats(query));
    }
}
