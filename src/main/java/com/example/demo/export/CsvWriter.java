package com.example.demo.export;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * CSV 生成工具。表头 + 行，统一以 UTF-8（带 BOM）输出，避免 Excel 中文乱码。
 */
public class CsvWriter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final StringBuilder sb = new StringBuilder("\uFEFF");

    public CsvWriter header(String... columns) {
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('"').append(columns[i].replace("\"", "\"\"")).append('"');
        }
        sb.append("\r\n");
        return this;
    }

    public CsvWriter row(Object... values) {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            String text;
            if (values[i] == null) {
                text = "";
            } else if (values[i] instanceof String s) {
                text = s;
            } else {
                text = toJsonText(values[i]);
            }
            // 所有字段加引号（内容中双引号转义），保证含逗号/中文的字段也能被正确解析
            sb.append('"').append(text.replace("\"", "\"\"")).append('"');
        }
        sb.append("\r\n");
        return this;
    }

    public byte[] bytes() {
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private String toJsonText(Object value) {
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        try {
            // 数组等复杂结构转 JSON 文本写入单元格（对 List<Integer> 直接序列化）
            String json = OBJECT_MAPPER.writeValueAsString(value);
            if (json.startsWith("\"") && json.endsWith("\"")) {
                json = json.substring(1, json.length() - 1).replace("\\\"", "\"");
            }
            return json;
        } catch (IOException e) {
            return String.valueOf(value);
        }
    }
}