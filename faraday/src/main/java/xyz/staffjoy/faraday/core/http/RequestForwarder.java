package xyz.staffjoy.faraday.core.http;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import xyz.staffjoy.faraday.config.FaradayProperties;
import xyz.staffjoy.faraday.config.MappingProperties;
import xyz.staffjoy.faraday.core.balancer.LoadBalancer;
import xyz.staffjoy.faraday.core.interceptor.PostForwardResponseInterceptor;
import xyz.staffjoy.faraday.core.mappings.MappingsProvider;
import xyz.staffjoy.faraday.core.trace.ProxyingTraceInterceptor;
import xyz.staffjoy.faraday.exceptions.FaradayException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static java.lang.System.nanoTime;
import static java.time.Duration.ofNanos;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.ResponseEntity.status;

/*
 *
 * 请求转发器
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 17:09 2019/12/29
 *
 */
public class RequestForwarder {

    private static final ILogger log = SLoggerFactory.getLogger(RequestForwarder.class);

    protected final ServerProperties serverProperties;
    protected final FaradayProperties faradayProperties;
    protected final HttpClientProvider httpClientProvider;
    protected final MappingsProvider mappingsProvider;
    protected final LoadBalancer loadBalancer;
    protected final Optional<MeterRegistry> meterRegistry;
    protected final ProxyingTraceInterceptor traceInterceptor;
    protected final PostForwardResponseInterceptor postForwardResponseInterceptor;

    public RequestForwarder(
            ServerProperties serverProperties,
            FaradayProperties faradayProperties,
            HttpClientProvider httpClientProvider,
            MappingsProvider mappingsProvider,
            LoadBalancer loadBalancer,
            Optional<MeterRegistry> meterRegistry,
            ProxyingTraceInterceptor traceInterceptor,
            PostForwardResponseInterceptor postForwardResponseInterceptor
    ) {
        this.serverProperties = serverProperties;
        this.faradayProperties = faradayProperties;
        this.httpClientProvider = httpClientProvider;
        this.mappingsProvider = mappingsProvider;
        this.loadBalancer = loadBalancer;
        this.meterRegistry = meterRegistry;
        this.traceInterceptor = traceInterceptor;
        this.postForwardResponseInterceptor = postForwardResponseInterceptor;
    }

    /**
     * 转发HTTP请求
     *
     * @param data
     * @param traceId
     * @param mapping
     * @return
     */
    public ResponseEntity<byte[]> forwardHttpRequest(RequestData data, String traceId, MappingProperties mapping) {
        // 目的主机
        ForwardDestination destination = resolveForwardDestination(data.getUri(), mapping);
        // 从客户端请求中删除
        prepareForwardedRequestHeaders(data, destination);
        // 截获器（追踪转发开始）
        traceInterceptor.onForwardStart(traceId, destination.getMappingName(),
                data.getMethod(), data.getHost(), destination.getUri().toString(),
                data.getBody(), data.getHeaders());
        // 请求实体
        RequestEntity<byte[]> request = new RequestEntity<>(data.getBody(), data.getHeaders(), data.getMethod(), destination.getUri());
        // 发送请求 返回响应数据
        ResponseData response = sendRequest(traceId, request, mapping, destination.getMappingMetricsName(), data);

        log.debug(String.format("Forwarded: %s %s %s -> %s %d", data.getMethod(), data.getHost(), data.getUri(), destination.getUri(), response.getStatus().value()));

        // 截获器（追踪转发完成）
        traceInterceptor.onForwardComplete(traceId, response.getStatus(), response.getBody(), response.getHeaders());
        // 响应截获器
        postForwardResponseInterceptor.intercept(response, mapping);
        // 从远程服务器的响应中删除
        prepareForwardedResponseHeaders(response);

        return status(response.getStatus())
                .headers(response.getHeaders())
                .body(response.getBody());

    }

    /**
     * 从远程服务器的响应中删除
     * 不要应用于我们发送的新响应
     *
     * @param response
     */
    protected void prepareForwardedResponseHeaders(ResponseData response) {
        HttpHeaders headers = response.getHeaders();
        headers.remove(TRANSFER_ENCODING);
        headers.remove(CONNECTION);
        headers.remove("Public-Key-Pins");
        headers.remove(SERVER);
        headers.remove("Strict-Transport-Security");
    }

    /**
     * 从客户端请求中删除
     * 不要应用于我们发送到远程服务器的新请求
     *
     * @param request
     * @param destination
     */
    protected void prepareForwardedRequestHeaders(RequestData request, ForwardDestination destination) {
        HttpHeaders headers = request.getHeaders();
        //headers.set(HOST, destination.getUri().getAuthority());
        headers.remove(TE);
    }

    /**
     * 从路由映射表中拿出要转发到的目的主机
     *
     * @param originUri
     * @param mapping
     * @return
     */
    protected ForwardDestination resolveForwardDestination(String originUri, MappingProperties mapping) {
        return new ForwardDestination(createDestinationUrl(originUri, mapping), mapping.getName(), resolveMetricsName(mapping));
    }

    /**
     * 创建目的主机URL
     *
     * @param uri
     * @param mapping
     * @return
     */
    protected URI createDestinationUrl(String uri, MappingProperties mapping) {
        String host = loadBalancer.chooseDestination(mapping.getDestinations());
        try {
            return new URI(host + uri);
        } catch (URISyntaxException e) {
            throw new FaradayException("Error creating destination URL from HTTP request URI: " + uri + " using mapping " + mapping, e);
        }
    }

    /**
     * 发送请求
     *
     * @param traceId
     * @param request
     * @param mapping
     * @param mappingMetricsName
     * @param requestData
     * @return
     */
    protected ResponseData sendRequest(String traceId, RequestEntity<byte[]> request, MappingProperties mapping, String mappingMetricsName, RequestData requestData) {
        ResponseEntity<byte[]> response;
        long startingTime = nanoTime();
        try {
            // 根据httpClient映射表作转发（获取Rest客户端并进行请求）
            // 返回的是ResponseEntity<Byte[]>类型
            response = httpClientProvider.getHttpClient(mapping.getName()).exchange(request, byte[].class);
            // 埋点监控
            recordLatency(mappingMetricsName, startingTime);
        } catch (HttpStatusCodeException e) {
            recordLatency(mappingMetricsName, startingTime);
            response = status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            recordLatency(mappingMetricsName, startingTime);
            traceInterceptor.onForwardFailed(traceId, e);
            throw e;
        }
        UnmodifiableRequestData data = new UnmodifiableRequestData(requestData);
        // 返回相应数据
        return new ResponseData(response.getStatusCode(), response.getHeaders(), response.getBody(), data);
    }

    protected void recordLatency(String metricName, long startingTime) {
        // Timer(计时器)适用于记录耗时比较短的事件的执行时间，通过时间分布展示事件的序列和发生频率
        meterRegistry.ifPresent(meterRegistry -> meterRegistry.timer(metricName).record(ofNanos(nanoTime() - startingTime)));
    }

    protected String resolveMetricsName(MappingProperties mapping) {
        return faradayProperties.getMetrics().getNamesPrefix() + "." + mapping.getName();
    }
}
