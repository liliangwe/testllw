package com.example.demo.tracking;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 算法接口埋点切面：方法成功返回后异步落库；不吞方法正常返回；失败仅记日志。
 */
@Aspect
@Component
public class TrackingAspect {

    private static final Logger log = LoggerFactory.getLogger(TrackingAspect.class);

    private final TrackingService trackingService;

    public TrackingAspect(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @Around("@annotation(com.example.demo.tracking.DemoApiTracking)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        // 仅算法接口成功（code="0"）返回后埋点；失败请求不计入调用
        if (result instanceof com.example.demo.common.ApiResponse<?> response
                && !com.example.demo.common.ApiResponse.CODE_SUCCESS.equals(response.getCode())) {
            return result;
        }
        DemoApiTracking annotation = null;
        if (pjp.getSignature() instanceof MethodSignature signature) {
            annotation = signature.getMethod().getAnnotation(DemoApiTracking.class);
        }
        if (annotation != null) {
            try {
                record(annotation.apiName());
            } catch (Exception e) {
                log.error("failed to submit tracking for api={}", annotation.apiName(), e);
            }
        }
        return result;
    }

    private void record(String apiName) {
        String callerId = "anonymous";
        String personType = "unknown";
        String personLevel = "unknown";
        String department = "unknown";
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            callerId = firstNonBlank(request.getHeader("X-Caller-Id"), callerId);
            personType = firstNonBlank(request.getHeader("X-Person-Type"), personType);
            personLevel = firstNonBlank(request.getHeader("X-Person-Level"), personLevel);
            department = firstNonBlank(request.getHeader("X-Dept"), department);
        }
        ApiCallContext ctx = new ApiCallContext(apiName, callerId, personType, personLevel, department,
                LocalDateTime.now());
        trackingService.record(ctx);
    }

    private String firstNonBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}