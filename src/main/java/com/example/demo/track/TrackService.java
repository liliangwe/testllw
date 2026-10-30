package com.example.demo.track;

import com.example.demo.track.dto.TrackStatsQuery;
import com.example.demo.track.dto.TrackStatsResponse;
import com.example.demo.track.entity.ApiInvokeTrack;

/**
 * 埋点服务（S05/S06）。
 */
public interface TrackService {

    /**
     * 异步记录埋点，失败仅记日志不影响主流程。
     */
    void record(ApiInvokeTrack track);

    /**
     * 多维聚合统计。
     */
    TrackStatsResponse stats(TrackStatsQuery query);
}
