package com.example.organization.common;

/**
 * 错误码定义。
 */
public enum ErrorCodeEnum {

    /**
     * 部门不存在
     */
    DEPARTMENT_NOT_FOUND("ORG_001", "部门不存在"),

    /**
     * 不能将部门移动到自身或其子孙节点下
     */
    DEPARTMENT_CYCLE_NOT_ALLOWED("ORG_002", "不能将部门移动到自身或其子孙节点下"),

    /**
     * 目标父部门不存在
     */
    PARENT_DEPARTMENT_NOT_FOUND("ORG_003", "目标父部门不存在"),

    /**
     * 部门下存在员工，无法删除
     */
    DEPARTMENT_HAS_EMPLOYEES("ORG_004", "部门下存在员工，无法删除"),

    /**
     * 工号已存在
     */
    EMPLOYEE_NO_ALREADY_EXISTS("ORG_005", "工号已存在"),

    /**
     * 手机号已存在
     */
    PHONE_ALREADY_EXISTS("ORG_006", "手机号已存在"),

    /**
     * 所属部门不存在
     */
    EMPLOYEE_DEPARTMENT_NOT_FOUND("ORG_007", "所属部门不存在"),

    /**
     * 员工不存在或已离职
     */
    EMPLOYEE_NOT_FOUND_OR_RESIGNED("ORG_008", "员工不存在或已离职"),

    /**
     * 并发冲突，version 已变更
     */
    EMPLOYEE_CONCURRENT_UPDATE("ORG_009", "员工信息已被他人修改，请刷新重试"),

    /**
     * 参数错误
     */
    PARAMETER_ERROR("ORG_400", "参数错误");

    private final String code;
    private final String message;

    ErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
