package xyz.staffjoy.common.services;

public class SecurityConstant {
    // Public security意味着用户可以注销或登录
    public static final int SEC_PUBLIC = 0;
    // Authenticated security意味着用户必须登录
    public static final int SEC_AUTHENTICATED = 1;
    // Admin security意味着用户必须同时登录并具有sudo标志
    public static final int SEC_ADMIN = 2;
}
