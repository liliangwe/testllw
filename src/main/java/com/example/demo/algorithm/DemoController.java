package com.example.demo.algorithm;

import com.example.demo.algorithm.dto.BubbleSortRequest;
import com.example.demo.algorithm.dto.BubbleSortResult;
import com.example.demo.algorithm.dto.HashRequest;
import com.example.demo.common.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 算法接口控制器（W01/W02/W03）。
 */
@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final DemoAlgorithmService demoAlgorithmService;

    public DemoController(DemoAlgorithmService demoAlgorithmService) {
        this.demoAlgorithmService = demoAlgorithmService;
    }

    @GetMapping("/helloworld")
    public Result<Map<String, String>> helloworld(@RequestParam(required = false) String name) {
        return Result.success(demoAlgorithmService.sayHello(name));
    }

    @PostMapping("/hash")
    public Result<Map<String, Object>> hash(@Valid @RequestBody HashRequest request) {
        return Result.success(demoAlgorithmService.hash(request.input(), request.algorithm()));
    }

    @PostMapping("/bubble-sort")
    public Result<BubbleSortResult> bubbleSort(@Valid @RequestBody BubbleSortRequest request) {
        return Result.success(demoAlgorithmService.bubbleSort(request.numbers(), request.order()));
    }
}
