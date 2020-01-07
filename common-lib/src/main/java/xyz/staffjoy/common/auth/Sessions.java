package xyz.staffjoy.common.auth;

import xyz.staffjoy.common.crypto.Sign;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 21:02 2019/12/28
 *
 */

/*

JSON Web Token

客户端使用账户密码请求登录接口
登录成功后服务器使用签名密钥生成JWT ,然后返回JWT给客户端。
客户端再次向服务端请求其他接口时带上JWT
服务端接收到JWT后验证签名的有效性.对客户端做出相应的响应

---

JWT的三个部分 ： JWT头、有效载荷和签名


JWT的用法
    客户端接收服务器返回的JWT，将其存储在Cookie或localStorage中。
    此后，客户端将在与服务器交互中都会带JWT。如果将它存储在Cookie中，就可以自动发送，但是不会跨域，因此一般是将它放入HTTP请求的Header Authorization字段中。

---

session实际上是基于cookie来传输的,最重要的session信息是存储在服务器的,所以服务器每次可以通过cookie中的sessionId获取到当前会话的用户,
对于单台服务器这样做没问题,但是对于多台就涉及到共享session的问题了,而且认证用户的增多,session会占用大量的服务器内存.
jwt是存储在客户端的,服务器不需要存储jwt,jwt里面有用户id,服务器拿到jwt验证后可以获得用户信息.也就实现了session的功能,
但是相比session,jwt是无状态的,其不与任何机器绑定,只要签名秘钥足够的安全就能保证jwt的可靠性.


 */


public class Sessions {
    public static final long SHORT_SESSION = TimeUnit.HOURS.toMillis(12);
    public static final long LONG_SESSION = TimeUnit.HOURS.toMillis(30 * 24);

    /**
     * 登陆login种Cookie（Cookie里放JWT令牌）
     *
     * @param userId
     * @param support
     * @param rememberMe
     * @param signingSecret
     * @param externalApex
     * @param response
     */
    public static void loginUser(String userId,
                                 boolean support,
                                 boolean rememberMe,
                                 String signingSecret,
                                 String externalApex,
                                 HttpServletResponse response) {


        long duration;
        int maxAge;

        if (rememberMe) {
            // "Remember me"
            duration = LONG_SESSION;
        } else {
            duration = SHORT_SESSION;
        }
        maxAge = (int) (duration / 1000);

        // 生成token(JWT)
        String token = Sign.generateSessionToken(userId, signingSecret, support, duration);
        // 生成cookie（Cookie里放Token令牌）
        Cookie cookie = new Cookie(AuthConstant.COOKIE_NAME, token);
        cookie.setPath("/");
        cookie.setDomain(externalApex);
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    /**
     * Cookie中取出JWT令牌
     *
     * @param request
     * @return
     */
    public static String getToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) return null;
        Cookie tokenCookie = Arrays.stream(cookies)
                .filter(cookie -> AuthConstant.COOKIE_NAME.equals(cookie.getName()))
                .findAny().orElse(null);
        if (tokenCookie == null) return null;
        return tokenCookie.getValue();
    }


    /**
     * 登出logout解除cookie
     *
     * @param externalApex
     * @param response
     */
    public static void logout(String externalApex, HttpServletResponse response) {
        Cookie cookie = new Cookie(AuthConstant.COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setDomain(externalApex);
        response.addCookie(cookie);
    }
}
