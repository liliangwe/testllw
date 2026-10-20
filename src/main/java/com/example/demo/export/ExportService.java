package com.example.demo.export;

import com.example.demo.common.BizException;
import com.example.demo.report.ReportQuery;
import com.example.demo.report.ReportResult;
import com.example.demo.report.ReportService;
import com.example.demo.service.AlgorithmService;
import com.example.demo.service.BubbleSortResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 导出服务：算法 tab 复用 AlgorithmService 重算，report 复用 ReportService 聚合。
 */
@Service
public class ExportService {

    public static final String TAB_HELLOWORLD = "helloworld";
    public static final String TAB_HASH = "hash";
    public static final String TAB_BUBBLE_SORT = "bubbleSort";
    public static final String TAB_REPORT = "report";

    private final AlgorithmService algorithmService;
    private final ReportService reportService;

    public ExportService(AlgorithmService algorithmService, ReportService reportService) {
        this.algorithmService = algorithmService;
        this.reportService = reportService;
    }

    public byte[] export(ExportQuery query) {
        String tab = normalizeTab(query.getTab());
        return switch (tab) {
            case TAB_HELLOWORLD -> exportHelloworld(query);
            case TAB_HASH -> exportHash(query);
            case TAB_BUBBLE_SORT -> exportBubbleSort(query);
            case TAB_REPORT -> exportReport(query);
            default -> exportHelloworld(query);
        };
    }

    public String normalizeTab(String tab) {
        if (TAB_HELLOWORLD.equals(tab) || TAB_HASH.equals(tab)
                || TAB_BUBBLE_SORT.equals(tab) || TAB_REPORT.equals(tab)) {
            return tab;
        }
        throw new BizException("EXP_001", "invalid tab, expected helloworld|hash|bubbleSort|report");
    }

    private byte[] exportHelloworld(ExportQuery query) {
        String name = null;
        if (query.getPayload() != null) {
            if (query.getPayload() instanceof Map<?, ?> map && map.get("name") instanceof String s) {
                name = s;
            } else if (query.getPayload() instanceof String s) {
                name = s;
            }
        }
        String message = algorithmService.hello(name);
        return new CsvWriter()
                .header("name", "message")
                .row(name == null ? "World" : name, message)
                .bytes();
    }

    private byte[] exportHash(ExportQuery query) {
        String text = extractStringPayload(query.getPayload());
        if (text == null) {
            throw new BizException("ALG_002", "text is empty");
        }
        String digest = algorithmService.sha256(text);
        return new CsvWriter()
                .header("text", "algorithm", "digest")
                .row(text, "SHA-256", digest)
                .bytes();
    }

    private byte[] exportBubbleSort(ExportQuery query) {
        int[] values = extractArrayPayload(query.getPayload());
        if (values == null || values.length == 0) {
            throw new BizException("ALG_003", "array is empty");
        }
        if (values.length > 1000) {
            throw new BizException("ALG_005", "array length exceeds 1000");
        }
        BubbleSortResult result = algorithmService.bubbleSortWithSwaps(values);
        return new CsvWriter()
                .header("sorted", "swaps")
                .row(result.sorted(), result.swaps())
                .bytes();
    }

    private byte[] exportReport(ExportQuery query) {
        if (query.getDimension() == null || query.getDimension().isBlank()) {
            throw new BizException("EXP_002", "dimension is required when tab=report");
        }
        ReportQuery reportQuery = new ReportQuery(query.getDimension(), query.getGroupBy(),
                query.getStart(), query.getEnd(), null);
        ReportResult result = reportService.aggregate(reportQuery);

        CsvWriter writer = new CsvWriter().header("dimension", "key", "bucket", "count");
        for (ReportResult.Item item : result.items()) {
            for (ReportResult.Series series : item.series()) {
                writer.row(result.dimension(), item.key(), series.bucket(), series.count());
            }
        }
        return writer.bytes();
    }

    private String extractStringPayload(Object payload) {
        if (payload == null) {
            return null;
        }
        if (payload instanceof String s) {
            return s;
        }
        if (payload instanceof Map<?, ?> map && map.get("text") instanceof String s) {
            return s;
        }
        return null;
    }

    private int[] extractArrayPayload(Object payload) {
        List<?> array;
        if (payload instanceof List<?> list) {
            array = list;
        } else if (payload instanceof Map<?, ?> map && map.get("array") instanceof List<?> list) {
            array = list;
        } else {
            return null;
        }
        int[] values = new int[array.size()];
        for (int i = 0; i < array.size(); i++) {
            Object element = array.get(i);
            if (!(element instanceof Number)) {
                throw new BizException("ALG_004", "array contains non-numeric element");
            }
            values[i] = ((Number) element).intValue();
        }
        return values;
    }
}