package com.example.demo.algorithm;

import com.example.demo.algorithm.dto.BubbleSortResult;

import java.util.List;
import java.util.Map;

/**
 * 算法服务（S01/S02/S03），纯内存无状态计算。
 */
public interface DemoAlgorithmService {

    /**
     * 返回问候语。
     *
     * @param name 被问候人姓名，默认 World
     * @return message/timestamp
     */
    Map<String, String> sayHello(String name);

    /**
     * 计算哈希值。
     *
     * @param input     原文
     * @param algorithm MD5/SHA1/SHA256，默认 SHA256
     * @return digest/algorithm/inputLength
     */
    Map<String, Object> hash(String input, String algorithm);

    /**
     * 冒泡排序（提前退出优化）。
     *
     * @param numbers 待排序数组
     * @param order   ASC/DESC
     * @return 排序结果与过程指标
     */
    BubbleSortResult bubbleSort(List<Integer> numbers, String order);
}
