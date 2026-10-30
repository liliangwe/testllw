package com.example.demo.exportx.enums;

import com.example.demo.common.BizException;

/**
 * 导出内容类型。
 */
public enum ExportType {

    HELLO,
    HASH,
    BUBBLE_SORT,
    TRACK_STATS;

    public static ExportType of(String value) {
        if (value == null || value.isBlank()) {
            throw new BizException("EXPORT_001", "导出类型不能为空");
        }
        for (ExportType type : values()) {
            if (type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        throw new BizException("EXPORT_001", "不支持的导出类型: " + value);
    }
}
