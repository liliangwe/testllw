package com.example.demo.algorithm;

import com.example.demo.algorithm.dto.BubbleSortResult;
import com.example.demo.algorithm.enums.HashAlgorithm;
import com.example.demo.algorithm.enums.SortOrder;
import com.example.demo.common.BizException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 算法服务实现。
 */
@Service
public class DemoAlgorithmServiceImpl implements DemoAlgorithmService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DEFAULT_NAME = "World";
    private static final int MAX_NAME_LENGTH = 50;

    @Override
    public Map<String, String> sayHello(String name) {
        String effectiveName = (name == null || name.isBlank()) ? DEFAULT_NAME : name.trim();
        if (effectiveName.length() > MAX_NAME_LENGTH) {
            throw new BizException("DEMO_001", "name 长度超限");
        }
        Map<String, String> data = new HashMap<>(2);
        data.put("message", "Hello, " + effectiveName + "!");
        data.put("timestamp", LocalDateTime.now().format(TIME_FORMATTER));
        return data;
    }

    @Override
    public Map<String, Object> hash(String input, String algorithm) {
        if (input == null || input.isEmpty()) {
            throw new BizException("DEMO_001", "input 不能为空");
        }
        if (input.length() > 4096) {
            throw new BizException("DEMO_001", "input 长度超限");
        }
        HashAlgorithm hashAlgorithm = HashAlgorithm.of(algorithm);
        String digest = digest(hashAlgorithm, input);
        Map<String, Object> data = new HashMap<>(3);
        data.put("digest", digest);
        data.put("algorithm", hashAlgorithm.name());
        data.put("inputLength", input.length());
        return data;
    }

    @Override
    public BubbleSortResult bubbleSort(List<Integer> numbers, String order) {
        if (numbers == null || numbers.isEmpty() || numbers.size() > 1000) {
            throw new BizException("DEMO_001", "数组为空或长度超 1000");
        }
        SortOrder sortOrder = SortOrder.of(order);
        long startNanos = System.nanoTime();
        List<Integer> sorted = new ArrayList<>(numbers);
        int compareCount = 0;
        int swapCount = 0;
        int n = sorted.size();
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                compareCount++;
                boolean needSwap = sortOrder == SortOrder.ASC
                        ? sorted.get(j) > sorted.get(j + 1)
                        : sorted.get(j) < sorted.get(j + 1);
                if (needSwap) {
                    int tmp = sorted.get(j);
                    sorted.set(j, sorted.get(j + 1));
                    sorted.set(j + 1, tmp);
                    swapCount++;
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
        }
        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
        return new BubbleSortResult(sorted, compareCount, swapCount, elapsedMs);
    }

    private String digest(HashAlgorithm algorithm, String input) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm.getJcaName());
            byte[] bytes = messageDigest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new BizException("DEMO_002", "不支持的哈希算法: " + algorithm);
        }
    }
}
