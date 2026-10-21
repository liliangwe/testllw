package com.example.login.model.dto;

/**
 * 登录响应 VO。
 *
 * @author AiWork
 * @date 2026-10-21
 */
public class LoginResponse {

    /** 用户ID（String避免JS精度丢失） */
    private String userId;

    /** 用户名 */
    private String username;

    /** 登录令牌 */
    private String token;

    // ==================== getters & setters ====================

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "LoginResponse{userId='" + userId + "', username='" + username + "'}";
    }
}