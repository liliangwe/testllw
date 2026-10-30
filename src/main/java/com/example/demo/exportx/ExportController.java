package com.example.demo.exportx;

import com.example.demo.exportx.enums.ExportType;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 结果导出接口（W04），返回 CSV 文件流（UTF-8 BOM）。
 */
@RestController
@RequestMapping("/api/demo")
public class ExportController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportController.class);
    private static final String UTF8_BOM = "﻿";

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/export")
    public void export(@RequestParam String type,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) String input,
                       @RequestParam(required = false) String algorithm,
                       @RequestParam(required = false) String numbers,
                       @RequestParam(required = false) String dimension,
                       @RequestParam(required = false) String startTime,
                       @RequestParam(required = false) String endTime,
                       HttpServletResponse response) throws IOException {
        ExportType exportType = ExportType.of(type);
        List<String[]> rows = exportService.export(exportType, name, input, algorithm,
                numbers, dimension, startTime, endTime);
        String filename = "demo_" + exportType.name() + "_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        try (PrintWriter writer = response.getWriter()) {
            writer.write(UTF8_BOM);
            for (String[] row : rows) {
                writer.println(toCsvLine(row));
            }
        } catch (IOException e) {
            LOGGER.warn("export stream interrupted, type={}", exportType, e);
        }
    }

    private String toCsvLine(String[] row) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escape(row[i]));
        }
        return sb.toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }
}
