package xyz.staffjoy.faraday.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

/*
 *
 * Faraday配置属性
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 16:30 2019/12/29
 *
 */


// 将大量的属性配置在 application.properties 或 application.yml 文件中，通过 @ConfigurationProperties 注解，我们可以方便的获取这些参数值
@ConfigurationProperties("faraday")
public class FaradayProperties {
    /**
     * Faraday servlet筛选顺序.
     */
    private int filterOrder = HIGHEST_PRECEDENCE + 100;
    /**
     * 是否启用编程映射，
     * 仅在dev环境中为false，在dev中我们通过配置文件使用映射
     */
    private boolean enableProgrammaticMapping = true;
    /**
     * 负责在HTTP请求转发期间收集度量的属性。
     */
    @NestedConfigurationProperty
    private MetricsProperties metrics = new MetricsProperties();
    /**
     * Properties responsible for tracing HTTP requests proxying processes.
     */
    @NestedConfigurationProperty
    private TracingProperties tracing = new TracingProperties();
    /**
     * 代理映射列表。
     */
    @NestedConfigurationProperty
    private List<MappingProperties> mappings = new ArrayList<>();

    public int getFilterOrder() {
        return filterOrder;
    }

    public void setFilterOrder(int filterOrder) {
        this.filterOrder = filterOrder;
    }

    public MetricsProperties getMetrics() {
        return metrics;
    }

    public void setMetrics(MetricsProperties metrics) {
        this.metrics = metrics;
    }

    public TracingProperties getTracing() {
        return tracing;
    }

    public void setTracing(TracingProperties tracing) {
        this.tracing = tracing;
    }

    public List<MappingProperties> getMappings() {
        return mappings;
    }

    public void setMappings(List<MappingProperties> mappings) {
        this.mappings = mappings;
    }

    public boolean isEnableProgrammaticMapping() {
        return this.enableProgrammaticMapping;
    }

    public void setEnableProgrammaticMapping(boolean enableProgrammaticMapping) {
        this.enableProgrammaticMapping = enableProgrammaticMapping;
    }
}
