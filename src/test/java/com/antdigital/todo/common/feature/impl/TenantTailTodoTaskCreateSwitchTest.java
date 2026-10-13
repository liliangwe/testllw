package com.antdigital.todo.common.feature.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * {@link TenantTailTodoTaskCreateSwitch} 灰度与应急开关逻辑测试（系分 7.2/7.3）。
 *
 * @author AiWork
 * @date 2026/10/13
 */
class TenantTailTodoTaskCreateSwitchTest {

    @Test
    void should_disableAllTenants_when_globalSwitchClosed() {
        // Arrange
        TenantTailTodoTaskCreateSwitch createSwitch = new TenantTailTodoTaskCreateSwitch(false, "0,1,2");

        // Act & Assert
        assertThat(createSwitch.isEnabled("tenant000")).isFalse();
    }

    @Test
    void should_enableAllTenants_when_tailListEmpty() {
        // Arrange
        TenantTailTodoTaskCreateSwitch createSwitch = new TenantTailTodoTaskCreateSwitch(true, "");

        // Act & Assert
        assertThat(createSwitch.isEnabled("tenant999")).isTrue();
    }

    @Test
    void should_enableTenant_when_tailMatched() {
        // Arrange
        TenantTailTodoTaskCreateSwitch createSwitch = new TenantTailTodoTaskCreateSwitch(true, "0,1,2");

        // Act & Assert
        assertThat(createSwitch.isEnabled("tenant001")).isTrue();
        assertThat(createSwitch.isEnabled("tenant002")).isTrue();
    }

    @Test
    void should_disableTenant_when_tailNotMatched() {
        // Arrange
        TenantTailTodoTaskCreateSwitch createSwitch = new TenantTailTodoTaskCreateSwitch(true, "0,1,2");

        // Act & Assert
        assertThat(createSwitch.isEnabled("tenant009")).isFalse();
    }

    @Test
    void should_disableTenant_when_tenantIdIsBlank() {
        // Arrange
        TenantTailTodoTaskCreateSwitch createSwitch = new TenantTailTodoTaskCreateSwitch(true, "");

        // Act & Assert
        assertThat(createSwitch.isEnabled(null)).isFalse();
        assertThat(createSwitch.isEnabled("")).isFalse();
    }

    @Test
    void should_ignoreIllegalTailItem_when_configIllegal() {
        // Arrange
        TenantTailTodoTaskCreateSwitch createSwitch = new TenantTailTodoTaskCreateSwitch(true, "0, x,1");

        // Act & Assert
        assertThat(createSwitch.isEnabled("tenant000")).isTrue();
        assertThat(createSwitch.isEnabled("tenantx")).isFalse();
    }
}
