package com.example.demo.algorithm.enums;

import com.example.demo.common.BizException;

/**
 * 哈希算法类型。
 */
public enum HashAlgorithm {

    MD5("MD5"),
    SHA1("SHA-1"),
    SHA256("SHA-256");

    private final String jcaName;

    HashAlgorithm(String jcaName) {
        this.jcaName = jcaName;
    }

    public String getJcaName() {
        return jcaName;
    }

    public static HashAlgorithm of(String value) {
        if (value == null || value.isBlank()) {
            return SHA256;
        }
        for (HashAlgorithm algorithm : values()) {
            if (algorithm.name().equalsIgnoreCase(value.trim())) {
                return algorithm;
            }
        }
        throw new BizException("DEMO_002", "不支持的哈希算法: " + value);
    }
}
