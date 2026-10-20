package com.example.demo.dto;

import java.util.List;

/**
 * 三个维度的可选值集合，对应 /api/demo/tracking/dimensions 的 data。
 */
public record DimensionsResponse(
        List<String> personTypes,
        List<String> personLevels,
        List<String> departments
) {
}