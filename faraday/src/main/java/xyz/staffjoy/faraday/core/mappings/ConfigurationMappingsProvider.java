package xyz.staffjoy.faraday.core.mappings;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import xyz.staffjoy.faraday.config.FaradayProperties;
import xyz.staffjoy.faraday.config.MappingProperties;
import xyz.staffjoy.faraday.core.http.HttpClientProvider;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/*
 *
 * 路由映射表配置类
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:01 2019/12/29
 *
 */
public class ConfigurationMappingsProvider extends MappingsProvider {

    public ConfigurationMappingsProvider(
            ServerProperties serverProperties,
            FaradayProperties faradayProperties,
            MappingsValidator mappingsValidator,
            HttpClientProvider httpClientProvider
    ) {
        super(serverProperties, faradayProperties,
                mappingsValidator, httpClientProvider);
    }


    @Override
    protected boolean shouldUpdateMappings(HttpServletRequest request) {
        return false;
    }

    /**
     * 检索出基于目前faradayProperties配置的所有映射表 通过faradayProperties->MappingProperties
     *
     * @return
     */
    @Override
    protected List<MappingProperties> retrieveMappings() {
        return faradayProperties.getMappings().stream()
                .map(MappingProperties::copy)
                .collect(Collectors.toList());
    }
}
