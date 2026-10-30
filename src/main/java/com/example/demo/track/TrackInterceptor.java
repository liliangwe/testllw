package com.example.demo.track;

import com.example.demo.track.entity.ApiInvokeTrack;
import com.example.demo.track.enums.ApiCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

/**
 * 埋点拦截器：仅拦截 /api/demo/**，解析人员请求头，异步落库。
 */
@Component
public class TrackInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackInterceptor.class);
    private static final String UNKNOWN = "UNKNOWN";
    private static final String ATTR_START_NANOS = "TRACK_START_NANOS";
    private static final String ATTR_TRACK = "TRACK_ENTITY";

    private final TrackService trackService;
    private final boolean trackEnabled;

    public TrackInterceptor(TrackService trackService,
                            @Value("${track.enabled:true}") boolean trackEnabled) {
        this.trackService = trackService;
        this.trackEnabled = trackEnabled;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!trackEnabled) {
            return true;
        }
        ApiInvokeTrack track = new ApiInvokeTrack();
        track.setApiPath(truncate(request.getRequestURI(), 128));
        ApiCode apiCode = ApiCode.fromPath(request.getRequestURI());
        track.setApiCode(apiCode == null ? UNKNOWN : apiCode.getCode());
        track.setCallerId(truncate(headerOrDefault(request, "X-User-Id"), 64));
        track.setCallerName(truncate(headerOrDefault(request, "X-User-Name"), 64));
        track.setPersonType(truncate(headerOrDefault(request, "X-User-Type"), 16));
        track.setPersonLevel(truncate(headerOrDefault(request, "X-User-Level"), 8));
        track.setPersonDept(truncate(headerOrDefault(request, "X-User-Dept"), 64));
        track.setInvokeTime(LocalDateTime.now());
        request.setAttribute(ATTR_START_NANOS, System.nanoTime());
        request.setAttribute(ATTR_TRACK, track);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        if (!trackEnabled) {
            return;
        }
        try {
            Object trackAttr = request.getAttribute(ATTR_TRACK);
            Object startAttr = request.getAttribute(ATTR_START_NANOS);
            if (!(trackAttr instanceof ApiInvokeTrack track) || !(startAttr instanceof Long startNanos)) {
                return;
            }
            track.setCostMs((System.nanoTime() - startNanos) / 1_000_000);
            boolean success = ex == null && response.getStatus() < 500;
            track.setInvokeResult(success ? "SUCCESS" : "FAIL");
            trackService.record(track);
        } catch (Exception e) {
            LOGGER.error("track record failed, path={}", request.getRequestURI(), e);
        }
    }

    private String headerOrDefault(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        return (value == null || value.isBlank()) ? UNKNOWN : value.trim();
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return UNKNOWN;
        }
        if (value.length() > maxLength) {
            LOGGER.warn("header value truncated, length={}", value.length());
            return value.substring(0, maxLength);
        }
        return value;
    }
}
