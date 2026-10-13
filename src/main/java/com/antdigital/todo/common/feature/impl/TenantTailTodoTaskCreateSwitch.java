package com.antdigital.todo.common.feature.impl;

import com.antdigital.todo.common.feature.TodoTaskCreateSwitch;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 按租户尾号灰度的创建开关实现（系分 7.2 灰度 + 7.3 应急）。
 *
 * <p>总开关关闭时所有租户均不可创建；总开关开启且尾号列表为空时全量放通；
 * 尾号列表非空时仅尾号命中的租户可创建。</p>
 *
 * @author AiWork
 * @date 2026/10/13
 */
@Component
public class TenantTailTodoTaskCreateSwitch implements TodoTaskCreateSwitch {

    /** 尾号列表分隔符 */
    private static final String TAIL_SEPARATOR = ",";

    /** 创建功能总开关，默认关闭（系分 7.3） */
    private final boolean enabled;

    /** 灰度租户尾号集合，为空表示全量放通 */
    private final Set<Character> tenantTailSet;

    /**
     * 构造创建开关。
     *
     * @param enabled        创建功能总开关
     * @param tenantTailList 灰度租户尾号列表，逗号分隔，如 "0,1,2"
     */
    public TenantTailTodoTaskCreateSwitch(@Value("${todo.task.create.enabled:false}") boolean enabled,
            @Value("${todo.task.create.tenant-tail-list:}") String tenantTailList) {
        this.enabled = enabled;
        this.tenantTailSet = parseTenantTailList(tenantTailList);
    }

    @Override
    public boolean isEnabled(String tenantId) {
        if (!enabled || tenantId == null || tenantId.isEmpty()) {
            return false;
        }
        if (tenantTailSet.isEmpty()) {
            return true;
        }
        return tenantTailSet.contains(tenantId.charAt(tenantId.length() - 1));
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
