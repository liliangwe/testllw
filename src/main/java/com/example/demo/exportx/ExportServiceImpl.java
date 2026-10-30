package com.example.demo.exportx;

import com.example.demo.algorithm.DemoAlgorithmService;
import com.example.demo.algorithm.dto.BubbleSortResult;
import com.example.demo.common.BizException;
import com.example.demo.exportx.enums.ExportType;
import com.example.demo.track.TrackService;
import com.example.demo.track.dto.TrackStatsQuery;
import com.example.demo.track.dto.TrackStatsResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 导出服务实现：算法类导出重新执行算法，报表类导出复用统计查询。
 */
@Service
public class ExportServiceImpl implements ExportService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int DEFAULT_STATS_DAYS = 7;
    private static final int MAX_EXPORT_ROWS = 10_000;

    private final DemoAlgorithmService demoAlgorithmService;
    private final TrackService trackService;

    public ExportServiceImpl(DemoAlgorithmService demoAlgorithmService, TrackService trackService) {
        this.demoAlgorithmService = demoAlgorithmService;
        this.trackService = trackService;
    }

    @Override
    public List<String[]> export(ExportType type, String name, String input, String algorithm,
                                 String numbers, String dimension,
                                 String startTime, String endTime) {
        List<String[]> rows = switch (type) {
            case HELLO -> exportHello(name);
            case HASH -> exportHash(input, algorithm);
            case BUBBLE_SORT -> exportBubbleSort(numbers);
            case TRACK_STATS -> exportTrackStats(dimension, startTime, endTime);
        };
        if (rows.size() > MAX_EXPORT_ROWS) {
            throw new BizException("EXPORT_002", "导出数据量超上限");
        }
        return rows;
    }

    private List<String[]> exportHello(String name) {
        Map<String, String> data = demoAlgorithmService.sayHello(name);
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"message", "timestamp"});
        rows.add(new String[]{data.get("message"), data.get("timestamp")});
        return rows;
    }

    private List<String[]> exportHash(String input, String algorithm) {
        if (input == null || input.isBlank()) {
            throw new BizException("EXPORT_003", "导出 HASH 类型时 input 必填");
        }
        Map<String, Object> data = demoAlgorithmService.hash(input, algorithm);
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"digest", "algorithm", "inputLength"});
        rows.add(new String[]{String.valueOf(data.get("digest")),
                String.valueOf(data.get("algorithm")), String.valueOf(data.get("inputLength"))});
        return rows;
    }

    private List<String[]> exportBubbleSort(String numbers) {
        if (numbers == null || numbers.isBlank()) {
            throw new BizException("EXPORT_003", "导出 BUBBLE_SORT 类型时 numbers 必填");
        }
        List<Integer> parsed = Arrays.stream(numbers.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try {
                        return Integer.valueOf(s);
                    } catch (NumberFormatException e) {
                        throw new BizException("EXPORT_003", "numbers 含非法整数: " + s);
                    }
                })
                .toList();
        BubbleSortResult result = demoAlgorithmService.bubbleSort(parsed, null);
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"index", "value"});
        for (int i = 0; i < result.sorted().size(); i++) {
            rows.add(new String[]{String.valueOf(i), String.valueOf(result.sorted().get(i))});
        }
        rows.add(new String[]{"compareCount", String.valueOf(result.compareCount())});
        rows.add(new String[]{"swapCount", String.valueOf(result.swapCount())});
        rows.add(new String[]{"elapsedMs", String.valueOf(result.elapsedMs())});
        return rows;
    }

    private List<String[]> exportTrackStats(String dimension, String startTime, String endTime) {
        LocalDateTime effectiveEnd = parseTime(endTime, LocalDateTime.now());
        LocalDateTime effectiveStart = parseTime(startTime, effectiveEnd.minusDays(DEFAULT_STATS_DAYS));
        TrackStatsResponse stats = trackService.stats(
                new TrackStatsQuery(dimension, null, effectiveStart, effectiveEnd));
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"dimensionValue", "count", "ratio"});
        for (TrackStatsResponse.DimensionStat stat : stats.dimensionStats()) {
            rows.add(new String[]{stat.dimensionValue(), String.valueOf(stat.count()),
                    stat.ratio().toPlainString()});
        }
        return rows;
    }

    private LocalDateTime parseTime(String value, LocalDateTime defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return LocalDateTime.parse(value.trim(), TIME_FORMATTER);
    }
}
