package com.example.todo.common.constant;

/**
 * 待办事项模块错误码与提示信息常量
 *
 * <p>错误码格式为 {MODULE}_{SEQ}，模块前缀为 TODO，详见系分方案 5. 功能模块设计。</p>
 *
 * @author AiWork
 * @date 2026/10/14
 */
public final class TodoErrorCode {

    /** 操作成功 */
    public static final String SUCCESS = "SUCCESS";

    /** 参数校验失败：事项名称为空或超长 */
    public static final String PARAM_NAME_INVALID = "TODO_001";

    /** 参数校验失败：描述内容超长 */
    public static final String PARAM_DESCRIPTION_INVALID = "TODO_002";

    /** 获取租户信息失败 */
    public static final String TENANT_INFO_MISSING = "TODO_003";

    /** 系统内部错误 */
    public static final String SYSTEM_ERROR = "TODO_500";

    /** 提示信息：请求参数校验失败 */
    public static final String MSG_PARAM_INVALID = "请求参数校验失败";

    /** 提示信息：获取租户信息失败 */
    public static final String MSG_TENANT_INFO_MISSING = "获取租户信息失败";

    /** 提示信息：系统异常 */
    public static final String MSG_SYSTEM_ERROR = "系统异常，请稍后重试";

    private TodoErrorCode() {
    }
}
