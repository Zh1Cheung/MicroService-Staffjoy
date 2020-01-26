package xyz.staffjoy.faraday.core.trace;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import xyz.staffjoy.faraday.config.FaradayProperties;

import static java.util.UUID.randomUUID;

/*
 *
 * 代理追踪拦截器
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:39 2019/12/29
 *
 */
public class ProxyingTraceInterceptor {

    protected final FaradayProperties faradayProperties;
    protected final TraceInterceptor traceInterceptor;

    public ProxyingTraceInterceptor(FaradayProperties faradayProperties, TraceInterceptor traceInterceptor) {
        this.faradayProperties = faradayProperties;
        this.traceInterceptor = traceInterceptor;
    }

    public String generateTraceId() {
        return faradayProperties.getTracing().isEnabled() ? randomUUID().toString() : null;
    }

    /**
     * 收到请求
     *
     * @param traceId
     * @param method
     * @param host
     * @param uri
     * @param headers
     */
    public void onRequestReceived(String traceId, HttpMethod method, String host, String uri, HttpHeaders headers) {
        runIfTracingIsEnabled(() -> {
            IncomingRequest request = getIncomingRequest(method, host, uri, headers);
            // log.info
            traceInterceptor.onRequestReceived(traceId, request);
        });
    }

    /**
     * 获取请求
     *
     * @param method
     * @param host
     * @param uri
     * @param headers
     * @return
     */
    private IncomingRequest getIncomingRequest(HttpMethod method, String host, String uri, HttpHeaders headers) {
        IncomingRequest request = new IncomingRequest();
        request.setMethod(method);
        request.setHost(host);
        request.setUri(uri);
        request.setHeaders(headers);
        return request;
    }

    /**
     * 没有找到映射信息
     *
     * @param traceId
     * @param method
     * @param host
     * @param uri
     * @param headers
     */
    public void onNoMappingFound(String traceId, HttpMethod method, String host, String uri, HttpHeaders headers) {
        runIfTracingIsEnabled(() -> {
            IncomingRequest request = getIncomingRequest(method, host, uri, headers);
            // log.info
            traceInterceptor.onNoMappingFound(traceId, request);
        });
    }

    /**
     * 转发请求开始
     *
     * @param traceId
     * @param mappingName
     * @param method
     * @param host
     * @param uri
     * @param body
     * @param headers
     */
    public void onForwardStart(String traceId, String mappingName, HttpMethod method, String host, String uri, byte[] body, HttpHeaders headers) {
        runIfTracingIsEnabled(() -> {
            ForwardRequest request = new ForwardRequest();
            request.setMappingName(mappingName);
            request.setMethod(method);
            request.setHost(host);
            request.setUri(uri);
            request.setBody(body);
            request.setHeaders(headers);
            // log.info
            traceInterceptor.onForwardStart(traceId, request);
        });
    }

    /**
     * 转发请求失败
     *
     * @param traceId
     * @param error
     */
    public void onForwardFailed(String traceId, Throwable error) {
        // log.info
        runIfTracingIsEnabled(() -> traceInterceptor.onForwardError(traceId, error));
    }

    /**
     * 转发请求完成
     *
     * @param traceId
     * @param status
     * @param body
     * @param headers
     */
    public void onForwardComplete(String traceId, HttpStatus status, byte[] body, HttpHeaders headers) {
        runIfTracingIsEnabled(() -> {
            ReceivedResponse response = new ReceivedResponse();
            response.setStatus(status);
            response.setBody(body);
            response.setHeaders(headers);
            // log.info
            traceInterceptor.onForwardComplete(traceId, response);
        });
    }

    /**
     * 检查是否开启追踪
     *
     * @param operation
     */
    protected void runIfTracingIsEnabled(Runnable operation) {
        if (faradayProperties.getTracing().isEnabled()) {
            operation.run();
        }
    }
}
