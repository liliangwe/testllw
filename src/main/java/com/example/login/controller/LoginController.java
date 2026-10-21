package com.example.login.controller;

import com.example.login.model.dto.ApiResponse;
import com.example.login.model.dto.LoginRequest;
import com.example.login.model.dto.LoginResponse;
import com.example.login.service.LoginService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录控制器。
 *
 * @author AiWork
 * @date 2026-10-21
 */
@RestController
@RequestMapping("/api/login")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    /**
     * 用户登录。
     *
     * @param request 登录请求（JSON body）
     * @return 登录结果
     */
    @PostMapping
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        if (logger.isInfoEnabled()) {
            logger.info("收到登录请求, username={}", request.getUsername());
        }
        LoginResponse response = loginService.login(request);
        return ApiResponse.success(response);
    }
}