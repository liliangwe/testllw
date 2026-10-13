package com.antdigital.todo.common.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * {@link TodoErrorCodeEnum} 错误码双向映射测试。
 *
 * @author AiWork
 * @date 2026/10/13
 */
class TodoErrorCodeEnumTest {

    @ParameterizedTest
    @EnumSource(TodoErrorCodeEnum.class)
    void should_returnSameEnum_when_getByCode(TodoErrorCodeEnum errorCode) {
        // Act & Assert
        assertThat(errorCode.getCode()).startsWith("TODO_");
        assertThat(errorCode.getMessage()).isNotBlank();
        assertThat(TodoErrorCodeEnum.getByCode(errorCode.getCode())).isEqualTo(errorCode);
    }

    @Test
    void should_keepFrozenErrorCodeSize_when_enumEvolves() {
        // 系分 5.1.2 错误码共 5 个，新增错误码时此断言失败，提醒同步更新接口文档
        assertThat(TodoErrorCodeEnum.values()).hasSize(5);
    }

    @Test
    void should_throwIllegalArgumentException_when_codeUnknown() {
        assertThatThrownBy(() -> TodoErrorCodeEnum.getByCode("TODO_999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TODO_999");
    }

    @Test
    void should_throwIllegalArgumentException_when_codeIsNull() {
        assertThatThrownBy(() -> TodoErrorCodeEnum.getByCode(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
