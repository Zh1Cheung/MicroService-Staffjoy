package xyz.staffjoy.faraday.core.trace;

import org.springframework.http.HttpHeaders;

/*
 *
 * http实体（Header）
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:34 2019/12/29
 *
 */
public abstract class HttpEntity {

    protected HttpHeaders headers;

    public HttpHeaders getHeaders() { return headers; }

    protected void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }
}
