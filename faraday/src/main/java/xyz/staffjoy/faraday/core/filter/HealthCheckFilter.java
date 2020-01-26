package xyz.staffjoy.faraday.core.filter;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
 *
 * 健康检查过滤器
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 16:48 2019/12/29
 *
 */
public class HealthCheckFilter extends OncePerRequestFilter {
    // HEALTH_CHECK_PATH是应用程序中的标准健康检查路径
    private static final String HEALTH_CHECK_PATH = "/health";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (HEALTH_CHECK_PATH.equals(request.getRequestURI())) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("OK");
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
