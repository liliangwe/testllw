package com.example.demo.tracking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TrackingServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TrackingService trackingService;

    @Test
    void recordInsertsOneRow() {
        long before = countRows();
        ApiCallContext ctx = new ApiCallContext(
                "helloworld", "alice", "正式", "P6", "研发部", LocalDateTime.now());
        trackingService.record(ctx);
        // @Async 走 trackingExecutor 线程池异步落库，主线程不阻塞；轮询等待真实插入完成
        long after = before;
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            after = countRows();
            if (after == before + 1) {
                break;
            }
            sleepQuietly(50);
        }
        assertEquals(before + 1, after);
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private long countRows() {
        Long n = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM demo_api_tracking", Long.class);
        return n == null ? 0 : n;
    }
}