package com.example.login.service.impl;

import com.example.login.dao.mapper.UserMapper;
import com.example.login.model.dto.LoginRequest;
import com.example.login.model.dto.LoginResponse;
import com.example.login.model.entity.UserDO;
import com.example.login.service.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 登录业务服务实现。
 *
 * @author AiWork
 * @date 2026-10-21
 */
@Service
public class LoginServiceImpl implements LoginService {

    private static final Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public LoginServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // 参数校验
        String username = request.getUsername();
        String password = request.getPassword();

        if (username == null || username.isBlank()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (password == null || password.isBlank()) {
            throw new RuntimeException("密码不能为空");
        }

        // 查询用户
        UserDO user = userMapper.selectByUsername(username);
        if (user == null) {
            if (logger.isInfoEnabled()) {
                logger.info("登录失败：用户不存在, username={}", username);
            }
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            if (logger.isWarnEnabled()) {
                logger.warn("登录失败：用户已禁用, username={}, status={}", username, user.getStatus());
            }
            throw new RuntimeException("用户已被禁用");
        }

        // 校验密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            if (logger.isInfoEnabled()) {
                logger.info("登录失败：密码错误, username={}", username);
            }
            throw new RuntimeException("用户名或密码错误");
        }

        // 生成令牌
        String token = UUID.randomUUID().toString().replace("-", "");

        if (logger.isInfoEnabled()) {
            logger.info("登录成功, userId={}, username={}", user.getId(), username);
        }

        // 组装响应
        LoginResponse response = new LoginResponse();
        response.setUserId(String.valueOf(user.getId()));
        response.setUsername(user.getUsername());
        response.setToken(token);
        return response;
    }
}