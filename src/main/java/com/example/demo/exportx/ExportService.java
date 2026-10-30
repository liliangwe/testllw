package com.example.demo.exportx;

import com.example.demo.exportx.enums.ExportType;

import java.util.List;

/**
 * 导出服务（S04）：生成 CSV 内容。
 */
public interface ExportService {

    /**
     * 按类型生成 CSV 行（含表头）。
     */
    List<String[]> export(ExportType type, String name, String input, String algorithm,
                          String numbers, String dimension,
                          String startTime, String endTime);
}
