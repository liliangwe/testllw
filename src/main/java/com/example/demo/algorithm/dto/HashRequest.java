package com.example.demo.algorithm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 哈希计算入参。
 */
public record HashRequest(
        @NotBlank(message = "input 不能为空") @Size(max = 4096, message = "input 长度超限") String input,
        String algorithm) {
}
