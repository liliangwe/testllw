package com.example.demo.track.enums;

/**
 * 被调接口编码。
 */
public enum ApiCode {

    HELLO("hello"),
    HASH("hash"),
    BUBBLE_SORT("bubble_sort"),
    EXPORT("export");

    private final String code;

    ApiCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ApiCode fromPath(String path) {
        if (path == null) {
            return null;
        }
        if (path.contains("helloworld")) {
            return HELLO;
        }
        if (path.contains("hash")) {
            return HASH;
        }
        if (path.contains("bubble-sort")) {
            return BUBBLE_SORT;
        }
        if (path.contains("export")) {
            return EXPORT;
        }
        return null;
    }
}
