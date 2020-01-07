package xyz.staffjoy.common.auth;

import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/*
 *
 * 认证上下文助手类
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:23 2019/12/28
 *
 */
public class AuthContext {


    private static String getRequetHeader(String headerName) {
        //获取上下文请求相关信息
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
            // 返回指定名字的请求头数据
            return request.getHeader(headerName);
        }
        return null;
    }

    public static String getUserId() {
        return getRequetHeader(AuthConstant.CURRENT_USER_HEADER);
    }

    public static String getAuthz() {
        return getRequetHeader(AuthConstant.AUTHORIZATION_HEADER);
    }

}
