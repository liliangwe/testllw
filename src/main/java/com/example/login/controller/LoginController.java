package com.example.login.controller;

import com.example.common.model.ApiResponse;
import com.example.login.model.dto.LoginRequest;
import com.example.login.model.dto.LoginResponse;
import com.example.login.service.LoginService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录控制器。
 *
 * @author AiWork
 * @date 2026/10/20
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    /**
     * 用户登录接口。
     *
     * @param request 登录请求
     * @return 登录响应
     */
    @PostMapping
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = loginService.login(request);
        return ApiResponse.success(response);
    }
}