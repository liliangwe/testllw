package com.example.demo.tracking;

import java.util.List;

/**
 * 三个维度的可选值集合（distinct），对应 /api/demo/tracking/dimensions 的 data。
 */
public record DimensionValues(
        List<String> personTypes,
        List<String> personLevels,
        List<String> departments
) {
}