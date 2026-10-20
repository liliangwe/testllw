package com.example.demo.tracking;

import java.time.LocalDateTime;

/**
 * 一次埋点调用的上下文（切面组装，不含落库自增 id）。
 */
public record ApiCallContext(
        String apiName,
        String callerId,
        String personType,
        String personLevel,
        String department,
        LocalDateTime callTime
) {
}