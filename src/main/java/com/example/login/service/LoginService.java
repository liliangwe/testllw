package com.example.login.service;

import com.example.login.model.dto.LoginRequest;
import com.example.login.model.dto.LoginResponse;

/**
 * 登录业务服务接口。
 *
 * @author AiWork
 * @date 2026-10-21
 */
public interface LoginService {

    /**
     * 用户登录认证。
     *
     * @param request 登录请求（含用户名和密码）
     * @return 登录响应（含用户信息和令牌）
     * @throws RuntimeException 用户名或密码错误时抛出
     */
    LoginResponse login(LoginRequest request);
}