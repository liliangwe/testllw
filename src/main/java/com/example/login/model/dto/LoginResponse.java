package com.example.login.model.dto;

/**
 * 登录响应。
 *
 * @author AiWork
 * @date 2026/10/20
 */
public class LoginResponse {

    private String token;
    private String username;

    public LoginResponse() {
    }

    public LoginResponse(String token, String username) {
        this.token = token;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "LoginResponse{username='" + username + "', token='" + token + "'}";
    }
}