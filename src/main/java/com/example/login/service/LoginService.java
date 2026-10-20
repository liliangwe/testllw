package com.example.login.service;

import com.example.login.model.dto.LoginRequest;
import com.example.login.model.dto.LoginResponse;

/**
 * 登录业务接口。
 *
 * @author AiWork
 * @date 2026/10/20
 */
public interface LoginService {

    /**
     * 用户登录。
     *
     * @param request 登录请求（含用户名、密码）
     * @return 登录响应（含 token）
     */
    LoginResponse login(LoginRequest request);
}