package xyz.staffjoy.common.auth;

import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/*
 *
 * 服务间调用授权截获器
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:28 2019/12/28
 *
 */

/*

在SpringBoot中我们可以使用HandlerInterceptorAdapter这个适配器来实现自己的拦截器。这样就可以拦截所有的请求并做相应的处理。
在HandlerInterceptorAdapter中主要提供了以下的方法：
    preHandle：在方法被调用前执行。在该方法中可以做类似校验的功能。如果返回true，则继续调用下一个拦截器。如果返回false，则中断执行，也就是说我们想调用的方法不会被执行，但是你可以修改response为你想要的响应。
    postHandle：在方法执行后调用。
    afterCompletion：在整个请求处理完毕后进行回调，也就是说视图渲染完毕或者调用方已经拿到响应。


 */
public class AuthorizeInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Authorize authorize = handlerMethod.getMethod().getAnnotation(Authorize.class);
        if (authorize == null) {
            return true; // no need to authorize
        }

        String[] allowedHeaders = authorize.value();
        String authzHeader = request.getHeader(AuthConstant.AUTHORIZATION_HEADER);

        if (StringUtils.isEmpty(authzHeader)) {
            throw new PermissionDeniedException(AuthConstant.ERROR_MSG_MISSING_AUTH_HEADER);
        }

        if (!Arrays.asList(allowedHeaders).contains(authzHeader)) {
            throw new PermissionDeniedException(AuthConstant.ERROR_MSG_DO_NOT_HAVE_ACCESS);
        }

        return true;
    }
}
