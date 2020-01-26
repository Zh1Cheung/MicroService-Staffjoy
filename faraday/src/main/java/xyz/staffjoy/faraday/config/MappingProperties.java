package xyz.staffjoy.faraday.config;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.builder.ToStringStyle.NO_CLASS_NAME_STYLE;

/*
 *
 * 映射属性
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 16:40 2019/12/29
 *
 */
public class MappingProperties {
    /**
     * 该映射属性的名字
     */
    private String name;
    /**
     * 映射传入HTTP请求uri的路径。
     */
    private String host = "";
    /**
     * 将转发HTTP请求的目标主机的列表。
     */
    private List<String> destinations = new ArrayList<>();
    /**
     * 转发HTTP请求时超时的属性。
     */
    private TimeoutProperties timeout = new TimeoutProperties();
    /**
     * 自定义属性占位符。
     */
    private Map<String, Object> customConfiguration = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public List<String> getDestinations() {
        return destinations;
    }

    public void setDestinations(List<String> destinations) {
        this.destinations = destinations;
    }

    public TimeoutProperties getTimeout() {
        return timeout;
    }

    public void setTimeout(TimeoutProperties timeout) {
        this.timeout = timeout;
    }

    public Map<String, Object> getCustomConfiguration() {
        return customConfiguration;
    }

    public void setCustomConfiguration(Map<String, Object> customConfiguration) {
        this.customConfiguration = customConfiguration;
    }

    public MappingProperties copy() {
        MappingProperties clone = new MappingProperties();
        clone.setName(name);
        clone.setHost(host);
        clone.setDestinations(destinations == null ? null : new ArrayList<>(destinations));
        clone.setTimeout(timeout);
        clone.setCustomConfiguration(customConfiguration == null ? null : new HashMap<>(customConfiguration));
        return clone;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, NO_CLASS_NAME_STYLE)
                .append("name", name)
                .append("host", host)
                .append("destinations", destinations)
                .append("timeout", timeout)
                .append("customConfiguration", customConfiguration)
                .toString();
    }


    public static class TimeoutProperties {

        /**
         * Connect timeout for HTTP requests forwarding.
         */
        private int connect = 2000;
        /**
         * Read timeout for HTTP requests forwarding.
         */
        private int read = 20000;

        public int getConnect() {
            return connect;
        }

        public void setConnect(int connect) {
            this.connect = connect;
        }

        public int getRead() {
            return read;
        }

        public void setRead(int read) {
            this.read = read;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, NO_CLASS_NAME_STYLE)
                    .append("connect", connect)
                    .append("read", read)
                    .toString();
        }
    }
}
