package com.antdigital.todo.common.feature.impl;

import com.antdigital.todo.common.feature.TodoTaskCreateSwitch;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 按租户尾号灰度的创建开关实现（系分 7.2 灰度 + 7.3 应急）。
 *
 * <p>总开关关闭时所有租户均不可创建；总开关开启且尾号列表为空时全量放通；
 * 尾号列表非空时仅尾号命中的租户可创建。</p>
 *
 * <p><b>应急能力（系分 7.3）</b>：配置值不做启动期固化，每次调用实时从 {@link Environment} 读取，
 * 因此配置中心推送/刷新后下一次请求即按新配置生效，无需重启或重新发布（配置推送 ≤ 30s）；
 * 总开关缺失或取值非布尔时按「关闭」处理（fail-closed），不会因配置异常默认放量。</p>
 *
 * @author AiWork
 * @date 2026/10/13
 */
@Component
public class TenantTailTodoTaskCreateSwitch implements TodoTaskCreateSwitch {

    /** 总开关配置键（系分 7.3） */
    public static final String ENABLED_KEY = "todo.task.create.enabled";

    /** 灰度租户尾号列表配置键（系分 7.2） */
    public static final String TENANT_TAIL_LIST_KEY = "todo.task.create.tenant-tail-list";

    /** 尾号列表分隔符 */
    private static final String TAIL_SEPARATOR = ",";

    /** 总开关缺省值：默认关闭（系分 7.3） */
    private static final boolean DEFAULT_ENABLED = false;

    private final Environment environment;

    public TenantTailTodoTaskCreateSwitch(Environment environment) {
        this.environment = environment;
    }

    @Override
    public boolean isEnabled(String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) {
            return false;
        }
        if (!isSwitchEnabled()) {
            return false;
        }
        Set<Character> tenantTailSet = currentTenantTailSet();
        if (tenantTailSet.isEmpty()) {
            return true;
        }
        return tenantTailSet.contains(tenantId.charAt(tenantId.length() - 1));
    }

    /**
     * 实时读取创建功能总开关，配置缺失或类型非法时按「关闭」处理。
     *
     * @return true 表示总开关开启
     */
    private boolean isSwitchEnabled() {
        return Boolean.TRUE.equals(environment.getProperty(ENABLED_KEY, Boolean.class, DEFAULT_ENABLED));
    }

    /**
     * 实时读取并解析灰度租户尾号列表。
     *
     * @return 尾号字符集合，为空表示灰度批次全量放通
     */
    private Set<Character> currentTenantTailSet() {
        return parseTenantTailList(environment.getProperty(TENANT_TAIL_LIST_KEY, String.class, ""));
    }

    /**
     * 解析灰度租户尾号列表，非法尾号直接忽略，避免配置错误导致开关整体失效。
     *
     * @param tenantTailList 逗号分隔的尾号列表
     * @return 尾号字符集合
     */
    private static Set<Character> parseTenantTailList(String tenantTailList) {
        Set<Character> tailSet = new HashSet<>();
        if (tenantTailList == null || tenantTailList.isEmpty()) {
            return Collections.unmodifiableSet(tailSet);
        }
        for (String item : tenantTailList.split(TAIL_SEPARATOR)) {
            String tail = item.strip();
            if (tail.length() == 1 && Character.isDigit(tail.charAt(0))) {
                tailSet.add(tail.charAt(0));
            }
        }
        return Collections.unmodifiableSet(tailSet);
    }
}
