package com.example.demo.algorithm.dto;

import java.util.List;

/**
 * 冒泡排序结果。
 *
 * @param sorted       排序结果
 * @param compareCount 比较次数
 * @param swapCount    交换次数
 * @param elapsedMs    计算耗时（毫秒）
 */
public record BubbleSortResult(List<Integer> sorted, int compareCount, int swapCount, long elapsedMs) {
}
