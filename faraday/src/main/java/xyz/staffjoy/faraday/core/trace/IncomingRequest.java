package xyz.staffjoy.faraday.core.trace;

import org.springframework.http.HttpMethod;

/*
 *
 * 请求（Http方法、url、host）
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:33 2019/12/29
 *
 */
public class IncomingRequest extends HttpEntity {

    protected HttpMethod method;
    protected String uri;
    protected String host;

    public HttpMethod getMethod() { return method; }

    protected void setMethod(HttpMethod method) { this.method = method;}

    public String getUri() { return uri; }

    protected void setUri(String uri) { this.uri = uri; }

    public String getHost() { return host; }

    protected  void setHost(String host) { this.host = host; }
}
