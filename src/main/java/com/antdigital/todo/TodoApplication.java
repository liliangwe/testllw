package com.antdigital.todo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 待办事项服务启动类。
 *
 * @author AiWork
 * @date 2026/10/13
 */
@SpringBootApplication
public class TodoApplication {

    /**
     * 应用入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(TodoApplication.class, args);
    }
}
