package xyz.staffjoy.faraday.core.trace;

/*
 *
 * 追踪拦截器
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:33 2019/12/29
 *
 */
public interface TraceInterceptor {

    void onRequestReceived(String traceId, IncomingRequest request);

    void onNoMappingFound(String traceId, IncomingRequest request);

    void onForwardStart(String traceId, ForwardRequest request);

    void onForwardError(String traceId, Throwable error);

    void onForwardComplete(String traceId, ReceivedResponse response);
}
