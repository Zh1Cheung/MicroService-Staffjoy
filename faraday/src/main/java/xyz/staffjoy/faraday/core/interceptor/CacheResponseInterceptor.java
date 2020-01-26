package xyz.staffjoy.faraday.core.interceptor;

import org.springframework.http.HttpHeaders;
import xyz.staffjoy.faraday.config.MappingProperties;
import xyz.staffjoy.faraday.core.http.ResponseData;

import java.util.List;

/*
 *
 * 缓存拦截器
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:38 2019/12/29
 *
 */
public class CacheResponseInterceptor implements PostForwardResponseInterceptor {
    @Override
    public void intercept(ResponseData data, MappingProperties mapping) {
        HttpHeaders respHeaders = data.getHeaders();
        if (respHeaders.containsKey(HttpHeaders.CONTENT_TYPE)) {
            List<String> values = respHeaders.get(HttpHeaders.CONTENT_TYPE);
            if (values.contains("text/html")) {
                //插入头以防止缓存
                respHeaders.set(HttpHeaders.CACHE_CONTROL, "no-cache");
            }
        }
    }
}
