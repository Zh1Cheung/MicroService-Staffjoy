package xyz.staffjoy.faraday.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TracingProperties {
    /**
     * 用于启用和禁用跟踪HTTP请求代理进程的标志
     */
    private boolean enabled;
}
