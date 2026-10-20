package com.example.demo.report;

import com.example.demo.tracking.TrackingRecord;
import com.example.demo.tracking.TrackingMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ReportServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TrackingMapper trackingMapper;

    @Autowired
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM demo_api_tracking");
        LocalDateTime base = LocalDateTime.of(2026, 10, 20, 9, 0, 0);
        // 研发部 P6 两条、P7 一条；市场部 P5 一条
        insert("helloworld", "研发部", "P6", base.withHour(9));
        insert("hash", "研发部", "P6", base.withHour(9));
        insert("bubbleSort", "研发部", "P7", base.withHour(9));
        insert("hash", "市场部", "P5", base.withHour(10));
    }

    private void insert(String apiName, String department, String personLevel, LocalDateTime callTime) {
        LocalDateTime now = LocalDateTime.now();
        trackingMapper.insert(new TrackingRecord(apiName, "alice", "正式", personLevel, department,
                callTime, now, now));
    }

    @Test
    void aggregateByDepartmentDay() {
        ReportResult result = reportService.aggregate(
                new ReportQuery("department", "day", null, null, null));
        assertEquals("department", result.dimension());
        assertEquals("day", result.groupBy());
        assertEquals(2, result.items().size());

        ReportResult.Item dev = findItem(result, "研发部");
        assertTrue(dev != null);
        assertEquals("2026-10-20", dev.series().get(0).bucket());
        assertEquals(3, dev.series().get(0).count());

        ReportResult.Item mkt = findItem(result, "市场部");
        assertTrue(mkt != null);
        assertEquals(1, mkt.series().get(0).count());
    }

    @Test
    void aggregateByPersonLevelHourBucketFormat() {
        ReportResult result = reportService.aggregate(
                new ReportQuery("person_level", "hour", null, null, null));
        assertEquals("person_level", result.dimension());
        assertEquals("hour", result.groupBy());

        ReportResult.Item p6 = findItem(result, "P6");
        assertTrue(p6 != null);
        assertEquals("2026-10-20 09:00:00", p6.series().get(0).bucket());
        assertEquals(2, p6.series().get(0).count());

        ReportResult.Item p5 = findItem(result, "P5");
        assertTrue(p5 != null);
        assertEquals("2026-10-20 10:00:00", p5.series().get(0).bucket());
        assertEquals(1, p5.series().get(0).count());
    }

    @Test
    void aggregateFiltersByApiNameAndRange() {
        ReportResult result = reportService.aggregate(
                new ReportQuery("department", "day", "2026-10-20", "2026-10-20", "hash"));
        assertEquals(2, result.items().size()); // hash 命中研发部 + 市场部各 1 条
        long total = 0;
        for (ReportResult.Item item : result.items()) {
            for (ReportResult.Series series : item.series()) {
                total += series.count();
            }
        }
        assertEquals(2, total);
    }

    private ReportResult.Item findItem(ReportResult result, String key) {
        for (ReportResult.Item item : result.items()) {
            if (item.key().equals(key)) {
                return item;
            }
        }
        return null;
    }
}