package com.example.demo.tracking;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 算法接口调用埋点注解。加在控制器方法上，方法成功返回后由 TrackingAspect 异步落库。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DemoApiTracking {

    /**
     * 接口标识：helloworld / hash / bubbleSort
     */
    String apiName();
}