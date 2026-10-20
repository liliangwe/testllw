package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.dto.BubbleSortRequest;
import com.example.demo.dto.DimensionsResponse;
import com.example.demo.dto.HashRequest;
import com.example.demo.export.ExportQuery;
import com.example.demo.export.ExportService;
import com.example.demo.report.ReportQuery;
import com.example.demo.report.ReportResult;
import com.example.demo.report.ReportService;
import com.example.demo.service.AlgorithmService;
import com.example.demo.service.BubbleSortResult;
import com.example.demo.tracking.DemoApiTracking;
import com.example.demo.tracking.DimensionValues;
import com.example.demo.tracking.TrackingMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final AlgorithmService algorithmService;
    private final ReportService reportService;
    private final ExportService exportService;
    private final TrackingMapper trackingMapper;

    public DemoController(AlgorithmService algorithmService, ReportService reportService,
                          ExportService exportService, TrackingMapper trackingMapper) {
        this.algorithmService = algorithmService;
        this.reportService = reportService;
        this.exportService = exportService;
        this.trackingMapper = trackingMapper;
    }

    @DemoApiTracking(apiName = "helloworld")
    @GetMapping("/helloworld")
    public ApiResponse<Map<String, String>> hello(@RequestParam(value = "name", required = false) String name) {
        if (name != null && name.length() > 64) {
            return ApiResponse.failure("ALG_001", "name length exceeds 64");
        }
        String message = algorithmService.hello(name);
        Map<String, String> data = new LinkedHashMap<>();
        data.put("message", message);
        return ApiResponse.success(data);
    }

    @DemoApiTracking(apiName = "hash")
    @PostMapping("/hash")
    public ApiResponse<Map<String, String>> hash(@RequestBody(required = false) HashRequest request) {
        if (request == null || request.getText() == null || request.getText().isEmpty()) {
            return ApiResponse.failure("ALG_002", "text is empty");
        }
        String digest = algorithmService.sha256(request.getText());
        Map<String, String> data = new LinkedHashMap<>();
        data.put("algorithm", "SHA-256");
        data.put("digest", digest);
        return ApiResponse.success(data);
    }

    @DemoApiTracking(apiName = "bubbleSort")
    @PostMapping("/bubble-sort")
    public ApiResponse<Map<String, Object>> bubbleSort(@RequestBody(required = false) BubbleSortRequest request) {
        if (request == null || request.getArray() == null || request.getArray().isEmpty()) {
            return ApiResponse.failure("ALG_003", "array is empty");
        }
        List<Object> array = request.getArray();
        if (array.size() > 1000) {
            return ApiResponse.failure("ALG_005", "array length exceeds 1000");
        }
        int[] values = new int[array.size()];
        for (int i = 0; i < array.size(); i++) {
            Object element = array.get(i);
            if (!(element instanceof Number)) {
                return ApiResponse.failure("ALG_004", "array contains non-numeric element");
            }
            values[i] = ((Number) element).intValue();
        }
        BubbleSortResult result = algorithmService.bubbleSortWithSwaps(values);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sorted", result.sorted());
        data.put("swaps", result.swaps());
        return ApiResponse.success(data);
    }

    @GetMapping("/tracking/report")
    public ApiResponse<ReportResult> report(@RequestParam(value = "dimension", required = false) String dimension,
                                            @RequestParam(value = "groupBy", required = false) String groupBy,
                                            @RequestParam(value = "start", required = false) String start,
                                            @RequestParam(value = "end", required = false) String end,
                                            @RequestParam(value = "apiName", required = false) String apiName) {
        ReportQuery query = new ReportQuery(dimension, groupBy, start, end, apiName);
        ReportResult result = reportService.aggregate(query);
        return ApiResponse.success(result);
    }

    @GetMapping("/tracking/dimensions")
    public ApiResponse<DimensionsResponse> dimensions() {
        DimensionValues values = trackingMapper.selectDistinctDimensions();
        return ApiResponse.success(new DimensionsResponse(
                values.personTypes(), values.personLevels(), values.departments()));
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> export(@RequestBody(required = false) ExportQuery query) {
        if (query == null) {
            query = new ExportQuery();
        }
        byte[] csv = exportService.export(query);
        String filename = "export-" + (query.getTab() == null ? "data" : query.getTab()) + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(csv);
    }
}