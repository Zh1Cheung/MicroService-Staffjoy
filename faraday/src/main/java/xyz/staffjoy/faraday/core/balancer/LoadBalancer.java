package xyz.staffjoy.faraday.core.balancer;

import java.util.List;
/*
 *
 * 负载均衡
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 16:44 2019/12/29
 *
 */
public interface LoadBalancer {
    /**
     * 选择将转发HTTP请求的目标主机
     *
     * @param destnations
     * @return
     */
    String chooseDestination(List<String> destnations);
}
