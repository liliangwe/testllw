package com.example.demo.track.enums;

import com.example.demo.common.BizException;

/**
 * 统计维度，column 为分组字段名（白名单，防注入）。
 */
public enum StatsDimension {

    PERSON_TYPE("person_type"),
    PERSON_LEVEL("person_level"),
    PERSON_DEPT("person_dept"),
    CALLER("caller_name");

    private final String column;

    StatsDimension(String column) {
        this.column = column;
    }

    public String getColumn() {
        return column;
    }

    public static StatsDimension of(String value) {
        if (value == null || value.isBlank()) {
            throw new BizException("TRACK_001", "统计维度不能为空");
        }
        for (StatsDimension dimension : values()) {
            if (dimension.name().equalsIgnoreCase(value.trim())) {
                return dimension;
            }
        }
        throw new BizException("TRACK_001", "统计维度非法: " + value);
    }
}
