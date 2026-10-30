package com.example.demo.track;

import com.example.demo.common.BizException;
import com.example.demo.track.dto.TrackStatsQuery;
import com.example.demo.track.dto.TrackStatsResponse;
import com.example.demo.track.entity.ApiInvokeTrack;
import com.example.demo.track.mapper.ApiInvokeTrackMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.SyncTaskExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TrackServiceImpl 单元测试。
 */
class TrackServiceImplTest {

    private final ApiInvokeTrackMapper mapper = mock(ApiInvokeTrackMapper.class);
    private final TrackServiceImpl service = new TrackServiceImpl(mapper, new SyncTaskExecutor());

    @Test
    void should_insertTrack_when_record() {
        ApiInvokeTrack track = new ApiInvokeTrack();
        track.setApiCode("hello");
        service.record(track);
        verify(mapper).insert(track);
    }

    @Test
    void should_returnStats_when_validQuery() {
        when(mapper.countByCondition(any(), any(), any())).thenReturn(10L);
        when(mapper.countTrendByDay(any(), any(), any()))
                .thenReturn(List.of(Map.of("date", "2026-10-30", "count", 10L)));
        when(mapper.countByDimension(eq("person_dept"), any(), any(), any()))
                .thenReturn(List.of(Map.of("dimensionValue", "研发部", "count", 10L)));

        TrackStatsQuery query = new TrackStatsQuery("PERSON_DEPT", null,
                LocalDateTime.now().minusDays(7), LocalDateTime.now());
        TrackStatsResponse response = service.stats(query);

        assertEquals(10L, response.totalCount());
        assertEquals(1, response.trend().size());
        assertEquals(1, response.dimensionStats().size());
        assertEquals(0, response.dimensionStats().get(0).ratio()
                .compareTo(new java.math.BigDecimal("1.0000")));
    }

    @Test
    void should_throwTrack001_when_dimensionInvalid() {
        TrackStatsQuery query = new TrackStatsQuery("BAD_DIM", null, null, null);
        assertThrows(BizException.class, () -> service.stats(query));
    }

    @Test
    void should_throwTrack002_when_rangeOver90Days() {
        TrackStatsQuery query = new TrackStatsQuery("PERSON_TYPE", null,
                LocalDateTime.now().minusDays(100), LocalDateTime.now());
        BizException e = assertThrows(BizException.class, () -> service.stats(query));
        assertEquals("TRACK_002", e.getErrorCode());
    }

    @Test
    void should_swallowException_when_insertFails() {
        ApiInvokeTrack track = new ApiInvokeTrack();
        org.mockito.Mockito.doThrow(new RuntimeException("db down")).when(mapper).insert(any());
        service.record(track);
        verify(mapper).insert(track);
    }
}
