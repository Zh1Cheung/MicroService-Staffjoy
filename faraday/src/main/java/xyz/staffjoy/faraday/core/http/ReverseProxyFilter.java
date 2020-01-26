package xyz.staffjoy.faraday.core.http;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.filter.OncePerRequestFilter;
import xyz.staffjoy.faraday.config.FaradayProperties;
import xyz.staffjoy.faraday.config.MappingProperties;
import xyz.staffjoy.faraday.core.interceptor.PreForwardRequestInterceptor;
import xyz.staffjoy.faraday.core.mappings.MappingsProvider;
import xyz.staffjoy.faraday.core.trace.ProxyingTraceInterceptor;
import xyz.staffjoy.faraday.exceptions.FaradayException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;



/*
 *
 * 真正的转发实现
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 17:34 2019/12/29
 *
 */

public class ReverseProxyFilter extends OncePerRequestFilter {

    protected static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";
    protected static final String X_FORWARDED_PROTO_HEADER = "X-Forwarded-Proto";
    protected static final String X_FORWARDED_HOST_HEADER = "X-Forwarded-Host";
    protected static final String X_FORWARDED_PORT_HEADER = "X-Forwarded-Port";

    private static final ILogger log = SLoggerFactory.getLogger(ReverseProxyFilter.class);

    protected final FaradayProperties faradayProperties;
    protected final RequestDataExtractor extractor;
    protected final MappingsProvider mappingsProvider;
    protected final RequestForwarder requestForwarder;
    protected final ProxyingTraceInterceptor traceInterceptor;
    protected final PreForwardRequestInterceptor preForwardRequestInterceptor;

    public ReverseProxyFilter(
            FaradayProperties faradayProperties,
            RequestDataExtractor extractor,
            MappingsProvider mappingsProvider,
            RequestForwarder requestForwarder,
            ProxyingTraceInterceptor traceInterceptor,
            PreForwardRequestInterceptor requestInterceptor
    ) {
        this.faradayProperties = faradayProperties;
        this.extractor = extractor;
        this.mappingsProvider = mappingsProvider;
        this.requestForwarder = requestForwarder;
        this.traceInterceptor = traceInterceptor;
        this.preForwardRequestInterceptor = requestInterceptor;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String originUri = extractor.extractUri(request);
        String originHost = extractor.extractHost(request);

        log.debug("Incoming request", "method", request.getMethod(),
                "host", originHost,
                "uri", originUri);

        HttpHeaders headers = extractor.extractHttpHeaders(request);
        HttpMethod method = extractor.extractHttpMethod(request);

        // 跟踪ID
        String traceId = traceInterceptor.generateTraceId();
        // 截获器（收到追踪请求）
        traceInterceptor.onRequestReceived(traceId, method, originHost, originUri, headers);


        // 路由解析模块
        MappingProperties mapping = mappingsProvider.resolveMapping(originHost, request);
        // 路由映射表为空
        if (mapping == null) {
            traceInterceptor.onNoMappingFound(traceId, method, originHost, originUri, headers);

            log.debug(String.format("Forwarding: %s %s %s -> no mapping found", method, originHost, originUri));

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Unsupported domain");
            return;
        } else {
            // mapping.getDestinations（） ： 将要请求的目的主机
            log.debug(String.format("Forwarding: %s %s %s -> %s", method, originHost, originUri, mapping.getDestinations()));
        }

        byte[] body = extractor.extractBody(request);
        // 转发头
        addForwardHeaders(request, headers);

        // 请求数据
        RequestData dataToForward = new RequestData(method, originHost, originUri, headers, body, request);
        // 请求截获器
        preForwardRequestInterceptor.intercept(dataToForward, mapping);
        // 需要重定向
        if (dataToForward.isNeedRedirect() && !isBlank(dataToForward.getRedirectUrl())) {
            log.debug(String.format("Redirecting to -> %s", dataToForward.getRedirectUrl()));
            // 重定向
            response.sendRedirect(dataToForward.getRedirectUrl());
            return;
        }
        // 返回响应实体（在请求转发器中转发）
        ResponseEntity<byte[]> responseEntity =
                requestForwarder.forwardHttpRequest(dataToForward, traceId, mapping);
        this.processResponse(response, responseEntity);
    }

    protected void addForwardHeaders(HttpServletRequest request, HttpHeaders headers) {
        List<String> forwordedFor = headers.get(X_FORWARDED_FOR_HEADER);
        if (isEmpty(forwordedFor)) {
            forwordedFor = new ArrayList<>(1);
        }
        forwordedFor.add(request.getRemoteAddr());
        headers.put(X_FORWARDED_FOR_HEADER, forwordedFor);
        headers.set(X_FORWARDED_PROTO_HEADER, request.getScheme());
        headers.set(X_FORWARDED_HOST_HEADER, request.getServerName());
        headers.set(X_FORWARDED_PORT_HEADER, valueOf(request.getServerPort()));
    }


    protected void processResponse(HttpServletResponse response, ResponseEntity<byte[]> responseEntity) {
        response.setStatus(responseEntity.getStatusCode().value());
        responseEntity.getHeaders().forEach((name, values) ->
                values.forEach(value -> response.addHeader(name, value))
        );
        if (responseEntity.getBody() != null) {
            try {
                response.getOutputStream().write(responseEntity.getBody());
            } catch (IOException e) {
                throw new FaradayException("Error writing body of HTTP response", e);
            }
        }
    }
}
