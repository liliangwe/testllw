package com.example.demo.export;

import com.example.demo.tracking.TrackingMapper;
import com.example.demo.tracking.TrackingRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ExportServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TrackingMapper trackingMapper;

    @Autowired
    private ExportService exportService;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM demo_api_tracking");
        LocalDateTime now = LocalDateTime.now();
        trackingMapper.insert(new TrackingRecord("hash", "alice", "正式", "P6", "研发部",
                LocalDateTime.of(2026, 10, 20, 9, 30), now, now));
        trackingMapper.insert(new TrackingRecord("hash", "bob", "外包", "P5", "市场部",
                LocalDateTime.of(2026, 10, 20, 10, 15), now, now));
    }

    @Test
    void exportHashContainsHeaderAndDigestRow() {
        ExportQuery query = new ExportQuery();
        query.setTab("hash");
        query.setPayload(java.util.Map.of("text", "abc"));

        byte[] bytes = exportService.export(query);
        String csv = new String(bytes, StandardCharsets.UTF_8);

        assertTrue(csv.contains("\"text\",\"algorithm\",\"digest\""), "missing header: " + csv);
        assertTrue(csv.contains("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"),
                "missing digest: " + csv);
    }

    @Test
    void exportReportContainsAggregatedRows() {
        ExportQuery query = new ExportQuery();
        query.setTab("report");
        query.setDimension("department");
        query.setGroupBy("day");
        query.setStart("2026-10-20");
        query.setEnd("2026-10-20");

        byte[] bytes = exportService.export(query);
        String csv = new String(bytes, StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("\uFEFF\"dimension\",\"key\",\"bucket\",\"count\""), "missing header: " + csv);
        assertTrue(csv.contains("\"department\",\"研发部\",\"2026-10-20\",\"1\""), "missing 研发部 row: " + csv);
        assertTrue(csv.contains("\"department\",\"市场部\",\"2026-10-20\",\"1\""), "missing 市场部 row: " + csv);
    }

    @Test
    void exportBubbleSortContainsSortedArray() {
        ExportQuery query = new ExportQuery();
        query.setTab("bubbleSort");
        query.setPayload(java.util.Map.of("array", List.of(5, 3, 8, 1)));

        byte[] bytes = exportService.export(query);
        String csv = new String(bytes, StandardCharsets.UTF_8);

        assertTrue(csv.contains("\"sorted\",\"swaps\""), "missing header: " + csv);
        assertTrue(csv.contains("[1,3,5,8]"), "missing sorted array: " + csv);
        assertTrue(csv.contains("\"4\"") || csv.contains(",4\r\n"), "missing swaps: " + csv);
    }
}