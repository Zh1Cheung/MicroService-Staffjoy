package xyz.staffjoy.faraday.core.mappings;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import xyz.staffjoy.common.env.EnvConfig;
import xyz.staffjoy.common.services.Service;
import xyz.staffjoy.common.services.ServiceDirectory;
import xyz.staffjoy.faraday.config.FaradayProperties;
import xyz.staffjoy.faraday.config.MappingProperties;
import xyz.staffjoy.faraday.core.http.HttpClientProvider;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
/*
 *
 * 程序化路由映射表
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:16 2019/12/29
 *
 */
public class ProgrammaticMappingsProvider extends MappingsProvider {
    protected final EnvConfig envConfig;

    public ProgrammaticMappingsProvider(
            EnvConfig envConfig,
            ServerProperties serverProperties,
            FaradayProperties faradayProperties,
            MappingsValidator mappingsValidator,
            HttpClientProvider httpClientProvider
    ) {
        super(serverProperties, faradayProperties, mappingsValidator, httpClientProvider);
        this.envConfig = envConfig;
    }

    @Override
    protected boolean shouldUpdateMappings(HttpServletRequest request) {
        return false;
    }

    /**
     * 检索出所有staffjoy服务的映射表
     *
     * @return
     */
    @Override
    protected List<MappingProperties> retrieveMappings() {
        // 新映射表
        List<MappingProperties> mappings = new ArrayList<>();
        // 所有staffjoy服务的映射表——<服务名,该服务的详细内容>
        Map<String, Service> serviceMap = ServiceDirectory.getMapping();
        for(String key : serviceMap.keySet()) {
            // 服务名
            String subDomain = key.toLowerCase();
            Service service = serviceMap.get(key);
            // 单个路由映射
            MappingProperties mapping = new MappingProperties();
            mapping.setName(subDomain + "_route");
            mapping.setHost(subDomain + "." + envConfig.getExternalApex());
            // 后端现在没有安全性
            String dest = "http://" + service.getBackendDomain();
            mapping.setDestinations(Arrays.asList(dest));
            mappings.add(mapping);
        }
        return mappings;
    }
}
