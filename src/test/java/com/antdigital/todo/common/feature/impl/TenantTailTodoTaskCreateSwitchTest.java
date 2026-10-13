package com.antdigital.todo.common.feature.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

/**
 * {@link TenantTailTodoTaskCreateSwitch} 灰度与应急开关逻辑测试（系分 7.2/7.3）。
 *
 * <p>开关取值实时读取自 {@link MockEnvironment}，其中「配置变更后免重启生效」用例模拟配置推送
 * 后 Environment 取值变化，验证运行期一键关闭能力（G17.1）。</p>
 *
 * @author AiWork
 * @date 2026/10/13
 */
class TenantTailTodoTaskCreateSwitchTest {

    @Test
    void should_disableAllTenants_when_globalSwitchClosed() {
        // Arrange
        TenantTailTodoTaskCreateSwitch createSwitch = buildSwitch("false", "0,1,2");

        // Act & Assert
        assertThat(createSwitch.isEnabled("tenant000")).isFalse();
    }

    @Test
    void should_disableAllTenants_when_globalSwitchAbsent() {
        // Arrange：未配置总开关，按默认关闭处理（fail-closed，系分 7.3）
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(TenantTailTodoTaskCreateSwitch.TENANT_TAIL_LIST_KEY, "0,1,2");
        TenantTailTodoTaskCreateSwitch createSwitch = new TenantTailTodoTaskCreateSwitch(environment);

        // Act & Assert
        assertThat(createSwitch.isEnabled("tenant001")).isFalse();
    }

    @Test
    void should_enableAllTenants_when_tailListEmpty() {
        // Arrange
        TenantTailTodoTaskCreateSwitch createSwitch = buildSwitch("true", "");

        // Act & Assert
        assertThat(createSwitch.isEnabled("tenant999")).isTrue();
    }

    @Test
    void should_enableAllTenants_when_tailListAbsent() {
        // Arrange：仅配置总开关，未配置灰度列表 → 全量放通
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(TenantTailTodoTaskCreateSwitch.ENABLED_KEY, "true");
        TenantTailTodoTaskCreateSwitch createSwitch = new TenantTailTodoTaskCreateSwitch(environment);

        // Act & Assert
        assertThat(createSwitch.isEnabled("tenant999")).isTrue();
    }

    @Test
    void should_enableTenant_when_tailMatched() {
        // Arrange
        TenantTailTodoTaskCreateSwitch createSwitch = buildSwitch("true", "0,1,2");

        // Act & Assert
        assertThat(createSwitch.isEnabled("tenant001")).isTrue();
        assertThat(createSwitch.isEnabled("tenant002")).isTrue();
    }

    @Test
    void should_disableTenant_when_tailNotMatched() {
        // Arrange
        TenantTailTodoTaskCreateSwitch createSwitch = buildSwitch("true", "0,1,2");

        // Act & Assert
        assertThat(createSwitch.isEnabled("tenant009")).isFalse();
    }

    @Test
    void should_disableTenant_when_tenantIdIsBlank() {
        // Arrange
        TenantTailTodoTaskCreateSwitch createSwitch = buildSwitch("true", "");

        // Act & Assert
        assertThat(createSwitch.isEnabled(null)).isFalse();
        assertThat(createSwitch.isEnabled("")).isFalse();
    }

    @Test
    void should_ignoreIllegalTailItem_when_configIllegal() {
        // Arrange
        TenantTailTodoTaskCreateSwitch createSwitch = buildSwitch("true", "0, x,1");

        // Act & Assert
        assertThat(createSwitch.isEnabled("tenant000")).isTrue();
        assertThat(createSwitch.isEnabled("tenantx")).isFalse();
    }

    @Test
    void should_takeEffectWithoutRestart_when_switchPropertyChanged() {
        // Arrange：初始为开启（灰度批次内）
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(TenantTailTodoTaskCreateSwitch.ENABLED_KEY, "true");
        environment.setProperty(TenantTailTodoTaskCreateSwitch.TENANT_TAIL_LIST_KEY, "0,1,2");
        TenantTailTodoTaskCreateSwitch createSwitch = new TenantTailTodoTaskCreateSwitch(environment);
        assertThat(createSwitch.isEnabled("tenant001")).isTrue();

        // Act：模拟配置中心推送新值（无重启、无重新发布）
        environment.setProperty(TenantTailTodoTaskCreateSwitch.ENABLED_KEY, "false");

        // Assert：下一次调用即按新配置生效（系分 7.3 一键关闭立即止损）
        assertThat(createSwitch.isEnabled("tenant001")).isFalse();
    }

    private static TenantTailTodoTaskCreateSwitch buildSwitch(String enabled, String tenantTailList) {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(TenantTailTodoTaskCreateSwitch.ENABLED_KEY, enabled);
        environment.setProperty(TenantTailTodoTaskCreateSwitch.TENANT_TAIL_LIST_KEY, tenantTailList);
        return new TenantTailTodoTaskCreateSwitch(environment);
    }
}
