package xyz.staffjoy.faraday.core.http;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import xyz.staffjoy.faraday.config.MappingProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.apache.http.impl.client.HttpClientBuilder.create;

/*
 *
 * httpClient映射表
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 17:04 2019/12/29
 *
 */
public class HttpClientProvider {
    // httpClient映射表  <mappingname,RestTemplate>
    // RestTemplate是Spring提供的用于访问Rest服务的客户端，RestTemplate提供了多种便捷访问远程Http服务的方法,能够大大提高客户端的编写效率。
    protected Map<String, RestTemplate> httpClients = new HashMap<>();

    public void updateHttpClients(List<MappingProperties> mappings) {
        httpClients = mappings.stream().collect(toMap(MappingProperties::getName, this::createRestTemplate));
    }

    /**
     * 获取该映射名字对应的Rest客户端
     * @param mappingName
     * @return
     */
    public RestTemplate getHttpClient(String mappingName) {
        return httpClients.get(mappingName);
    }

    protected RestTemplate createRestTemplate(MappingProperties mapping) {
        CloseableHttpClient client = createHttpClient(mapping).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(client);
        requestFactory.setConnectTimeout(mapping.getTimeout().getConnect());
        requestFactory.setReadTimeout(mapping.getTimeout().getRead());
        return new RestTemplate(requestFactory);
    }

    protected HttpClientBuilder createHttpClient(MappingProperties mapping) {
        return create().useSystemProperties().disableRedirectHandling().disableCookieManagement();
    }
}
