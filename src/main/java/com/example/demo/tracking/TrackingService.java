package com.example.demo.tracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 埋点落库服务：异步执行，失败仅记错误日志，不回抛、不影响主流程。
 */
@Service
public class TrackingService {

    private static final Logger log = LoggerFactory.getLogger(TrackingService.class);

    private final TrackingMapper trackingMapper;

    public TrackingService(TrackingMapper trackingMapper) {
        this.trackingMapper = trackingMapper;
    }

    @Async("trackingExecutor")
    public void record(ApiCallContext ctx) {
        try {
            LocalDateTime now = LocalDateTime.now();
            TrackingRecord record = new TrackingRecord(
                    ctx.apiName(),
                    ctx.callerId(),
                    ctx.personType(),
                    ctx.personLevel(),
                    ctx.department(),
                    ctx.callTime(),
                    now,
                    now);
            trackingMapper.insert(record);
        } catch (Exception e) {
            log.error("tracking record failed for api={} caller={}", ctx.apiName(), ctx.callerId(), e);
        }
    }
}