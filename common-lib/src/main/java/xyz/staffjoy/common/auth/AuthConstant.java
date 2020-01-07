package xyz.staffjoy.common.auth;


/*
 *
 * 授权Header定义（跟安全相关）
 *
 * 所有用户操作必须通过网关转发 鉴权是强制性的
 * 服务间调用鉴权是基于内部约定机制 不是强制性的
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:07 2019/12/28
 *
 */


public class AuthConstant {

    public static final String COOKIE_NAME = "staffjoy-faraday";
    // 内部userid的集合
    public static final String CURRENT_USER_HEADER = "faraday-current-user-id";
    // AUTHORIZATION_HEADER是http请求头，用于访问内部授权的密钥
    public static final String AUTHORIZATION_HEADER = "Authorization";
    // AUTHORIZATION_ANONYMOUS_WEB设置为授权头，表示未经验证的web用户正在请求
    public static final String AUTHORIZATION_ANONYMOUS_WEB = "faraday-anonymous";
    // AUTHORIZATION_COMPANY_SERVICE被设置为授权头以表示公司服务部门正在提出请求
    public static final String AUTHORIZATION_COMPANY_SERVICE = "company-service";
    // AUTHORIZATION_BOT_SERVICE设置为授权头，表示消息微服务正在发出请求
    public static final String AUTHORIZATION_BOT_SERVICE = "bot-service";
    // AUTHORIZATION_ACCOUNT_SERVICE被设置为授权头，以表示帐户服务正在发出请求
    public static final String AUTHORIZATION_ACCOUNT_SERVICE = "account-service";
    // AUTHORIZATION_SUPPORT_USER设置为授权头，表示Staffjoy团队成员正在提出请求
    public static final String AUTHORIZATION_SUPPORT_USER = "faraday-support";
    // AUTHORIZATION_SUPERPOWERS_SERVICE设置为表示请求是由开发超级管理员服务发出的
    public static final String AUTHORIZATION_SUPERPOWERS_SERVICE = "superpowers-service";
    // AUTHORIZATION_WWW_SERVICE设置为授权头，表示www登录/注册系统正在发出请求
    public static final String AUTHORIZATION_WWW_SERVICE = "www-service";
    // AUTH_WHOAMI_SERVICE设置为授权头，表示whoami microservice正在请求
    public static final String AUTHORIZATION_WHOAMI_SERVICE = "whoami-service";
    // AUTHORIZATION_AUTHENTICATED_USER设置为授权头，表示已通过身份验证的web用户正在发出请求
    public static final String AUTHORIZATION_AUTHENTICATED_USER = "faraday-authenticated";
    // AUTHORIZATION_ICAL_SERVICE 设置为授权头，表示ical服务部门正在提出请求
    public static final String AUTHORIZATION_ICAL_SERVICE = "ical-service";
    // 验证错误消息
    public static final String ERROR_MSG_DO_NOT_HAVE_ACCESS = "You do not have access to this service";
    public static final String ERROR_MSG_MISSING_AUTH_HEADER = "Missing Authorization http header";
}
