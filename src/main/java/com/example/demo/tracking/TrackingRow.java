package com.example.demo.tracking;

import java.time.LocalDateTime;

/**
 * 报表聚合用的原始行（只取维度列 + 调用时间，应用层再做分桶聚合）。
 */
public record TrackingRow(
        String personType,
        String personLevel,
        String department,
        LocalDateTime callTime
) {
}