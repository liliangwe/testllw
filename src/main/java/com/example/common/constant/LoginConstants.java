package com.example.common.constant;

/**
 * 登录相关常量。
 *
 * @author AiWork
 * @date 2026/10/20
 */
public final class LoginConstants {

    private LoginConstants() {
        throw new IllegalStateException("Constant class");
    }

    /** 登录相关错误码 */
    public static final String ERR_USER_NOT_FOUND = "A0001";
    public static final String ERR_PASSWORD_WRONG  = "A0002";
    public static final String ERR_ACCOUNT_DISABLED = "A0003";
    public static final String ERR_PARAM_INVALID    = "A0004";

    /** 默认 token 前缀 */
    public static final String TOKEN_PREFIX = "token_";
}