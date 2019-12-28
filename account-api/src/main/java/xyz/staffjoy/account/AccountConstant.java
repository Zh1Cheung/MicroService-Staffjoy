package xyz.staffjoy.account;

public class AccountConstant {
    public static final String SERVICE_NAME = "account-service";

    // 通知模板
    // 重置密码
    public static final String RESET_PASSWORD_TMPL = "<div>We received a request to reset the password on your account. To do so, click the below link. If you did not request this change, no action is needed. <br/> <a href=\"%s\">%s</a></div>";
    // 激活账户
    public static final String ACTIVATE_ACCOUNT_TMPL = "<div><p>Hi %s, and welcome to Staffjoy!</p><a href=\"%s\">Please click here to finish setting up your account.</a></p></div><br/><br/><div>If you have trouble clicking on the link, please copy and paste this link into your browser: <br/><a href=\"%s\">%s</a></div>";
    // 确认邮箱
    public static final String CONFIRM_EMAIL_TMPL = "<div>Hi %s!</div>To confirm your new email address, <a href=\"%s\">please click here</a>.</div><br/><br/><div>If you have trouble clicking on the link, please copy and paste this link into your browser: <br/><a href=\"%s\">%s</a></div>";

}
