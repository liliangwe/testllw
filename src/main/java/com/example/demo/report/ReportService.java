package com.example.demo.report;

import com.example.demo.common.BizException;
import com.example.demo.tracking.TrackingMapper;
import com.example.demo.tracking.TrackingRow;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表聚合服务：SQL 只按可选条件查行，分桶与聚合在应用层完成，保证 H2/MySQL 通用。
 */
@Service
public class ReportService {

    public static final String DIM_PERSON_TYPE = "person_type";
    public static final String DIM_PERSON_LEVEL = "person_level";
    public static final String DIM_DEPARTMENT = "department";

    public static final String GROUP_BY_DAY = "day";
    public static final String GROUP_BY_HOUR = "hour";

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HOUR_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00");

    private final TrackingMapper trackingMapper;

    public ReportService(TrackingMapper trackingMapper) {
        this.trackingMapper = trackingMapper;
    }

    public ReportResult aggregate(ReportQuery query) {
        String dimension = normalizeDimension(query.getDimension());
        String groupBy = normalizeGroupBy(query.getGroupBy());

        LocalDateTime start = parseDate(query.getStart(), false);
        LocalDateTime end = parseDate(query.getEnd(), true);
        if (start != null && end != null && start.isAfter(end)) {
            throw new BizException("RPT_002", "start must not be after end");
        }

        List<TrackingRow> rows = trackingMapper.selectRows(query.getApiName(), start, end);

        Map<String, Map<String, Long>> grouped = new LinkedHashMap<>();
        for (TrackingRow row : rows) {
            String key = dimensionValue(row, dimension);
            String bucket = formatBucket(row.callTime(), groupBy);
            grouped.computeIfAbsent(key, k -> new LinkedHashMap<>())
                    .merge(bucket, 1L, Long::sum);
        }

        List<ReportResult.Item> items = new ArrayList<>();
        grouped.forEach((key, seriesMap) -> {
            List<ReportResult.Series> series = new ArrayList<>();
            seriesMap.forEach((bucket, count) -> series.add(new ReportResult.Series(bucket, count)));
            items.add(new ReportResult.Item(key, series));
        });

        return new ReportResult(dimension, groupBy, items);
    }

    public String normalizeDimension(String dimension) {
        if (DIM_PERSON_TYPE.equals(dimension) || DIM_PERSON_LEVEL.equals(dimension) || DIM_DEPARTMENT.equals(dimension)) {
            return dimension;
        }
        throw new BizException("RPT_001", "invalid dimension, expected person_type|person_level|department");
    }

    public String normalizeGroupBy(String groupBy) {
        if (groupBy == null || groupBy.isBlank() || GROUP_BY_DAY.equals(groupBy)) {
            return GROUP_BY_DAY;
        }
        if (GROUP_BY_HOUR.equals(groupBy)) {
            return GROUP_BY_HOUR;
        }
        // 非法值按业务规则归默认 day（brief 允许 RPT_003 或归默认 day）
        return GROUP_BY_DAY;
    }

    private String dimensionValue(TrackingRow row, String dimension) {
        return switch (dimension) {
            case DIM_PERSON_TYPE -> row.personType();
            case DIM_PERSON_LEVEL -> row.personLevel();
            case DIM_DEPARTMENT -> row.department();
            default -> row.personType();
        };
    }

    private String formatBucket(LocalDateTime time, String groupBy) {
        return GROUP_BY_HOUR.equals(groupBy) ? time.format(HOUR_FMT) : time.format(DAY_FMT);
    }

    private LocalDateTime parseDate(String value, boolean endOfDay) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(value);
            return endOfDay ? date.atTime(23, 59, 59) : date.atStartOfDay();
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}