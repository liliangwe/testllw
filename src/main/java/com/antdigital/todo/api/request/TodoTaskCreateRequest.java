package com.antdigital.todo.api.request;

import com.antdigital.todo.common.constant.TodoTaskConstants;
import com.antdigital.todo.common.constant.TodoTipConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 新增待办事项请求（系分 5.1.2 W01 入参）。
 *
 * <p>名称写入时统一去除首尾空白，保证声明式校验与落库使用同一份文本。</p>
 *
 * @author AiWork
 * @date 2026/10/13
 */
public class TodoTaskCreateRequest {

    /** 事项名称，必填，去首尾空白后 1~64 字符 */
    @NotBlank(message = TodoTipConstants.TASK_NAME_INVALID)
    @Size(max = TodoTaskConstants.TASK_NAME_MAX_LEN, message = TodoTipConstants.TASK_NAME_INVALID)
    private String taskName;

    /** 事项描述，选填，最大 512 字符，不传时按空串存储 */
    @Size(max = TodoTaskConstants.TASK_DESC_MAX_LEN, message = TodoTipConstants.TASK_DESC_TOO_LONG)
    private String taskDesc;

    /** 幂等键，前端防重预留字段，本期不透传落库（系分 5.1.3.1 推荐方案 ②） */
    private String requestId;

    public String getTaskName() {
        return taskName;
    }

    /**
     * 写入事项名称，去除首尾空白（含全角空格等 Unicode 空白）。
     *
     * @param taskName 事项名称
     */
    public void setTaskName(String taskName) {
        this.taskName = taskName == null ? null : taskName.strip();
    }

    public String getTaskDesc() {
        return taskDesc;
    }

    public void setTaskDesc(String taskDesc) {
        this.taskDesc = taskDesc;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        // 系分 6.4.3.2：描述为自由文本，日志只打印长度，不打印全文
        return "TodoTaskCreateRequest{taskName='" + taskName + "', taskDescLength="
                + (taskDesc == null ? 0 : taskDesc.length()) + ", requestId='" + requestId + "'}";
    }
}
