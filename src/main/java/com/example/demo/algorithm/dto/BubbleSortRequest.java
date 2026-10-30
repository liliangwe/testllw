package com.example.demo.algorithm.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 冒泡排序入参。
 */
public record BubbleSortRequest(
        @NotEmpty(message = "numbers 不能为空") @Size(max = 1000, message = "数组长度不能超过 1000") List<Integer> numbers,
        String order) {
}
