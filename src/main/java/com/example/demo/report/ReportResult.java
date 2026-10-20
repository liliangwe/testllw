package com.example.demo.report;

import java.util.List;

/**
 * 报表出参 data：{dimension, groupBy, items:[{key, series:[{bucket, count}]}]}
 */
public record ReportResult(
        String dimension,
        String groupBy,
        List<Item> items
) {

    public record Item(String key, List<Series> series) {
    }

    public record Series(String bucket, long count) {
    }
}