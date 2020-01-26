package xyz.staffjoy.faraday.core.http;

import java.net.URI;

/*
 *
 * 要转发到的目的主机
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 17:05 2019/12/29
 *
 */
public class ForwardDestination {

    protected final URI uri;
    protected final String mappingName;
    protected final String mappingMetricsName;

    public ForwardDestination(URI uri, String mappingName, String mappingMetricsName) {
        this.uri = uri;
        this.mappingName = mappingName;
        this.mappingMetricsName = mappingMetricsName;
    }

    public URI getUri() { return uri; }

    public String getMappingName() { return mappingName; }

    public String getMappingMetricsName() { return mappingMetricsName; }
}
