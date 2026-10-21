package com.example.login.service.impl;

import com.example.login.dao.mapper.UserMapper;
import com.example.login.model.dto.LoginRequest;
import com.example.login.model.dto.LoginResponse;
import com.example.login.model.entity.UserDO;
import com.example.login.service.LoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * LoginServiceImpl 单元测试。
 *
 * @author AiWork
 * @date 2026-10-21
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginServiceImpl 单元测试")
class LoginServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private LoginService loginService;

    @BeforeEach
    void setUp() {
        loginService = new LoginServiceImpl(userMapper, passwordEncoder);
    }

    // ==================== login 测试 ====================

    @Test
    @DisplayName("应返回登录响应 当用户名密码正确")
    void should_returnLoginResponse_when_credentialsValid() {
        // Arrange
        String username = "admin";
        String rawPassword = "123456";
        String encodedPassword = "$2a$10$encodedPasswordHash";

        UserDO mockUser = buildUserDO(1L, username, encodedPassword);
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(rawPassword);

        when(userMapper.selectByUsername(username)).thenReturn(mockUser);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        // Act
        LoginResponse response = loginService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getToken()).isNotBlank();
    }

    @Test
    @DisplayName("应抛异常 当用户不存在")
    void should_throwException_when_userNotFound() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("123456");

        when(userMapper.selectByUsername("nonexistent")).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户名或密码错误");
    }

    @Test
    @DisplayName("应抛异常 当密码错误")
    void should_throwException_when_passwordIncorrect() {
        // Arrange
        String username = "admin";
        String rawPassword = "wrong_password";
        String encodedPassword = "$2a$10$encodedPasswordHash";

        UserDO mockUser = buildUserDO(1L, username, encodedPassword);
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(rawPassword);

        when(userMapper.selectByUsername(username)).thenReturn(mockUser);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户名或密码错误");
    }

    @Test
    @DisplayName("应抛异常 当用户名为null")
    void should_throwException_when_usernameIsNull() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername(null);
        request.setPassword("123456");

        // 不 mock userMapper，因为参数校验应在数据库查询之前拦截

        // Act & Assert
        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户名");
    }

    @Test
    @DisplayName("应抛异常 当密码为null")
    void should_throwException_when_passwordIsNull() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword(null);

        // Act & Assert
        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("密码");
    }

    @Test
    @DisplayName("应抛异常 当用户名为空字符串")
    void should_throwException_when_usernameIsEmpty() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword("123456");

        // Act & Assert
        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户名");
    }

    // ==================== 辅助方法 ====================

    private UserDO buildUserDO(Long id, String username, String password) {
        UserDO user = new UserDO();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setStatus(1);
        user.setGmtCreate(LocalDateTime.now());
        user.setGmtModified(LocalDateTime.now());
        return user;
    }
}