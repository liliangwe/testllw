package com.example.demo.track;

import com.example.demo.common.BizException;
import com.example.demo.track.dto.TrackStatsQuery;
import com.example.demo.track.dto.TrackStatsResponse;
import com.example.demo.track.entity.ApiInvokeTrack;
import com.example.demo.track.enums.StatsDimension;
import com.example.demo.track.mapper.ApiInvokeTrackMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 埋点服务实现：异步落库 + 聚合统计。
 */
@Service
public class TrackServiceImpl implements TrackService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackServiceImpl.class);
    private static final long MAX_STATS_RANGE_DAYS = 90L;

    private final ApiInvokeTrackMapper trackMapper;
    private final TaskExecutor trackTaskExecutor;

    public TrackServiceImpl(ApiInvokeTrackMapper trackMapper,
                            @Qualifier("trackTaskExecutor") TaskExecutor trackTaskExecutor) {
        this.trackMapper = trackMapper;
        this.trackTaskExecutor = trackTaskExecutor;
    }

    @Override
    public void record(ApiInvokeTrack track) {
        trackTaskExecutor.execute(() -> {
            try {
                trackMapper.insert(track);
            } catch (Exception e) {
                LOGGER.error("track insert failed, apiCode={}, callerId={}",
                        track.getApiCode(), track.getCallerId(), e);
            }
        });
    }

    @Override
    public TrackStatsResponse stats(TrackStatsQuery query) {
        StatsDimension dimension = StatsDimension.of(query.dimension());
        if (query.startTime() != null && query.endTime() != null
                && Duration.between(query.startTime(), query.endTime()).toDays() > MAX_STATS_RANGE_DAYS) {
            throw new BizException("TRACK_002", "时间跨度不能超过 90 天");
        }
        Long totalCount = trackMapper.countByCondition(query.apiCode(), query.startTime(), query.endTime());
        List<TrackStatsResponse.TrendPoint> trend = trackMapper
                .countTrendByDay(query.apiCode(), query.startTime(), query.endTime())
                .stream()
                .map(row -> new TrackStatsResponse.TrendPoint(
                        String.valueOf(row.get("date")), toLong(row.get("count"))))
                .toList();
        long total = totalCount == null ? 0L : totalCount;
        List<TrackStatsResponse.DimensionStat> dimensionStats = trackMapper
                .countByDimension(dimension.getColumn(), query.apiCode(), query.startTime(), query.endTime())
                .stream()
                .map(row -> {
                    long count = toLong(row.get("count"));
                    BigDecimal ratio = total == 0 ? BigDecimal.ZERO
                            : BigDecimal.valueOf(count).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);
                    return new TrackStatsResponse.DimensionStat(
                            String.valueOf(row.get("dimensionValue")), count, ratio);
                })
                .toList();
        return new TrackStatsResponse(total, trend, dimensionStats);
    }

    private long toLong(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }
}
