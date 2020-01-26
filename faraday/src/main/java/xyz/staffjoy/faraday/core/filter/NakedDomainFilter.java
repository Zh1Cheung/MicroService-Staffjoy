package xyz.staffjoy.faraday.core.filter;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import xyz.staffjoy.common.env.EnvConfig;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/*
 *
 * 空域名过滤器
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 16:50 2019/12/29
 *
 */
public class NakedDomainFilter extends OncePerRequestFilter {

    private static final ILogger log = SLoggerFactory.getLogger(NakedDomainFilter.class);

    private final EnvConfig envConfig;
    private static final String DEFAULT_SERVICE = "www";

    public NakedDomainFilter(EnvConfig envConfig) {
        this.envConfig = envConfig;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
    // 如果你点击的是空域名-请访问www
    // 例如staffjoy.xyz/foo？true=1应该重定向到www.staffjoy.xyz/foo？teue=1
        if (envConfig.getExternalApex().equals(request.getServerName())) {
            //  redirect to www
            log.info("hitting naked domain - redirect to www");
            String scheme = "http";
            if (!envConfig.isDebug()) {
                scheme = "https";
            }
            try {
                URI redirectUrl = new URI(scheme,
                        null,
                        DEFAULT_SERVICE + "." + envConfig.getExternalApex(), // www.staffjoy.xyz
                        request.getServerPort(),
                        "/login/", null, null);
                // 重定向
                response.sendRedirect(redirectUrl.toString());
            } catch (URISyntaxException e) {
                log.error("fail to build redirect url", e);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
