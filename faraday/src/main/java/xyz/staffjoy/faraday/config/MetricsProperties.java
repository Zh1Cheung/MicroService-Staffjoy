package xyz.staffjoy.faraday.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricsProperties {
    /**
     * 全局度量名称前缀
     */
    private String namesPrefix = "faraday";
}
