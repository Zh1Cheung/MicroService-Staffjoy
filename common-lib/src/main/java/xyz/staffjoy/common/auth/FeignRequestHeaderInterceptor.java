package xyz.staffjoy.common.auth;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.util.StringUtils;

/*
 * 客户端向服务端发出请求通过Feign
 * Feign客户端传递用户认证信息
 * 外部拦截器，用于将身份验证信息传递到后端
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:53 2019/12/28
 *
 */

/*

假设现在有A服务,B服务,外部使用RESTApi请求调用A服务，在请求头上有token字段，A服务使用完后，B服务也要使用，
如何才能把token也转发到B服务呢？这里可以使用Feign的RequestInterceptor，但是直接使用一般情况下HttpServletRequest上下文对象是为空的，

---

Feign 支持请求拦截器，在发送请求前，可以对发送的模板进行操作，例如设置请求头等属性
服务端可以通过HttpServletRequest获取到前面传递的参数
就实现了各个微服务之间参数的传递。　
 */

public class FeignRequestHeaderInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String userId = AuthContext.getUserId();
        if (!StringUtils.isEmpty(userId)) {
            //将header继续往后传（也就是userid）
            //在链路中始终保持着userid
            requestTemplate.header(AuthConstant.CURRENT_USER_HEADER, userId);
        }
    }
}
