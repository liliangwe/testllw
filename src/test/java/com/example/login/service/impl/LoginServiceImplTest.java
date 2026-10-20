package com.example.login.service.impl;

import com.example.common.exception.BusinessException;
import com.example.login.dao.mapper.UserMapper;
import com.example.login.model.dto.LoginRequest;
import com.example.login.model.dto.LoginResponse;
import com.example.login.model.entity.UserDO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * LoginServiceImpl 单元测试。
 * 遵循 AAA（Arrange-Act-Assert）模式。
 *
 * @author AiWork
 * @date 2026/10/20
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginServiceImpl 单元测试")
class LoginServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private LoginServiceImpl loginService;

    // ==================== 正常路径 ====================

    @Nested
    @DisplayName("登录成功场景")
    class SuccessScenarios {

        @Test
        @DisplayName("正确用户名和密码应返回token")
        void shouldReturnTokenWhenValidCredentials() {
            // Arrange
            UserDO mockUser = new UserDO();
            mockUser.setId(1L);
            mockUser.setUsername("admin");
            // BCrypt("123456") → $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
            mockUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi");
            mockUser.setStatus(1);

            when(userMapper.selectByUsername("admin")).thenReturn(mockUser);

            LoginRequest request = new LoginRequest("admin", "123456");

            // Act
            LoginResponse response = loginService.login(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo("admin");
            assertThat(response.getToken()).isNotNull().startsWith("token_");
        }
    }

    // ==================== 异常路径 ====================

    @Nested
    @DisplayName("登录失败场景")
    class FailureScenarios {

        @Test
        @DisplayName("用户不存在时应抛出 BusinessException")
        void shouldThrowWhenUserNotFound() {
            // Arrange
            when(userMapper.selectByUsername("nobody")).thenReturn(null);

            LoginRequest request = new LoginRequest("nobody", "123456");

            // Act & Assert
            assertThatThrownBy(() -> loginService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("用户不存在");
        }

        @Test
        @DisplayName("密码错误时应抛出 BusinessException")
        void shouldThrowWhenPasswordWrong() {
            // Arrange
            UserDO mockUser = new UserDO();
            mockUser.setId(1L);
            mockUser.setUsername("admin");
            mockUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi");
            mockUser.setStatus(1);

            when(userMapper.selectByUsername("admin")).thenReturn(mockUser);

            LoginRequest request = new LoginRequest("admin", "wrong_password");

            // Act & Assert
            assertThatThrownBy(() -> loginService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("密码错误");
        }

        @Test
        @DisplayName("账号已禁用时应抛出 BusinessException")
        void shouldThrowWhenAccountDisabled() {
            // Arrange
            UserDO mockUser = new UserDO();
            mockUser.setId(2L);
            mockUser.setUsername("disabled_user");
            mockUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi");
            mockUser.setStatus(0); // 禁用

            when(userMapper.selectByUsername("disabled_user")).thenReturn(mockUser);

            LoginRequest request = new LoginRequest("disabled_user", "123456");

            // Act & Assert
            assertThatThrownBy(() -> loginService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("账号已被禁用");
        }
    }
}