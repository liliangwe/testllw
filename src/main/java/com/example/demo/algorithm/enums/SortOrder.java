package com.example.demo.algorithm.enums;

import com.example.demo.common.BizException;

/**
 * 排序方向，默认升序。
 */
public enum SortOrder {

    ASC,
    DESC;

    public static SortOrder of(String value) {
        if (value == null || value.isBlank()) {
            return ASC;
        }
        for (SortOrder order : values()) {
            if (order.name().equalsIgnoreCase(value.trim())) {
                return order;
            }
        }
        throw new BizException("DEMO_001", "非法排序方向: " + value);
    }
}
