package xyz.staffjoy.common.services;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/*
 *
 * ServiceDirectory允许使用其子域访问后端服务
 *
 * StaffjoyServices是一个子域映射->规范
 * 子域是<string>+Env[“rootDomain”]
 * 例如prod上的“login”服务是“login”+“staffjoy.xyz”
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 11:07 2019/12/29
 *
 */
public class ServiceDirectory {

    // <服务名,该服务的详细内容>
    private static Map<String, Service> serviceMap;

    static {

        Map<String, Service> map = new TreeMap<>();

        Service service = Service.builder()
                .security(SecurityConstant.SEC_AUTHENTICATED)
                .restrictDev(false)
                .backendDomain("account-service")
                .build();
        map.put("account", service);

        service = Service.builder()
                .security(SecurityConstant.SEC_AUTHENTICATED)
                .restrictDev(false)
                .backendDomain("app-service")
                .noCacheHtml(true)
                .build();
        map.put("app", service);

        service = Service.builder()
                .security(SecurityConstant.SEC_AUTHENTICATED)
                .restrictDev(false)
                .backendDomain("company-service")
                .build();
        map.put("company", service);

        service = Service.builder()
                // Debug site for faraday proxy
                .security(SecurityConstant.SEC_PUBLIC)
                .restrictDev(true)
                .backendDomain("httpbin.org")
                .build();
        map.put("faraday", service);

        service = Service.builder()
                .security(SecurityConstant.SEC_PUBLIC)
                .restrictDev(false)
                .backendDomain("ical-service")
                .build();
        map.put("ical", service);

        service = Service.builder()
                .security(SecurityConstant.SEC_AUTHENTICATED)
                .restrictDev(false)
                .backendDomain("myaccount-service")
                .noCacheHtml(true)
                .build();
        map.put("myaccount", service);

        service = Service.builder()
                .security(SecurityConstant.SEC_AUTHENTICATED)
                .restrictDev(true)
                .backendDomain("superpowers-service")
                .build();
        map.put("superpowers", service);

        service = Service.builder()
                .security(SecurityConstant.SEC_AUTHENTICATED)
                .restrictDev(false)
                .backendDomain("whoami-service")
                .build();
        map.put("whoami", service);

        service = Service.builder()
                .security(SecurityConstant.SEC_PUBLIC)
                .restrictDev(false)
                .backendDomain("www-service")
                .build();
        map.put("www", service);

        serviceMap = Collections.unmodifiableMap(map);
    }

    public static Map<String, Service> getMapping() {
        return serviceMap;
    }
}
