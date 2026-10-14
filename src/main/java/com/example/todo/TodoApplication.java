package com.example.todo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 待办事项管理系统启动类
 *
 * @author AiWork
 * @date 2026/10/14
 */
@SpringBootApplication
@MapperScan("com.example.todo.dao.mapper")
public class TodoApplication {

    /**
     * 应用入口方法
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(TodoApplication.class, args);
    }
}
