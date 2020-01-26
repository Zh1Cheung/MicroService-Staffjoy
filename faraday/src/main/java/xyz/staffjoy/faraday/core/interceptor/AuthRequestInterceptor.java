package xyz.staffjoy.faraday.core.interceptor;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.common.auth.Sessions;
import xyz.staffjoy.common.crypto.Sign;
import xyz.staffjoy.common.env.EnvConfig;
import xyz.staffjoy.common.services.SecurityConstant;
import xyz.staffjoy.common.services.Service;
import xyz.staffjoy.common.services.ServiceDirectory;
import xyz.staffjoy.faraday.config.MappingProperties;
import xyz.staffjoy.faraday.core.http.RequestData;
import xyz.staffjoy.faraday.exceptions.FaradayException;
import xyz.staffjoy.faraday.exceptions.ForbiddenException;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class AuthRequestInterceptor implements PreForwardRequestInterceptor {
    private final static ILogger log = SLoggerFactory.getLogger(AuthRequestInterceptor.class);

    // 签名密钥
    private final String signingSecret;
    private final EnvConfig envConfig;

    // 使用map进行固定时间查找。value不重要
    // 假设这些应该是普遍唯一的，所以我们不必受env的限制
    private final Map<String, String> bannedUsers = new HashMap<String, String>() {{
        put("d7b9dbed-9719-4856-5f19-23da2d0e3dec", "hidden");
    }};

    public AuthRequestInterceptor(String signingSecret, EnvConfig envConfig) {
        this.signingSecret = signingSecret;
        this.envConfig = envConfig;
    }

    @Override
    public void intercept(RequestData data, MappingProperties mapping) {
        //清理传入请求并设置授权信息
        String authorization = this.setAuthHeader(data, mapping);

        this.validateRestrict(mapping);
        this.validateSecurity(data, mapping, authorization);

        // TODO - filter restricted headers
    }


    // 网关传递认证授权信息
    private String setAuthHeader(RequestData data, MappingProperties mapping) {
        // 如果证明不是，则默认为匿名web
        String authorization = AuthConstant.AUTHORIZATION_ANONYMOUS_WEB;
        HttpHeaders headers = data.getHeaders();
        Session session = this.getSession(data.getOriginRequest());
        if (session != null) {
            if (session.isSupport()) {
                authorization = AuthConstant.AUTHORIZATION_SUPPORT_USER;
            } else {
                authorization = AuthConstant.AUTHORIZATION_AUTHENTICATED_USER;
            }

            this.checkBannedUsers(session.getUserId());

            headers.set(AuthConstant.CURRENT_USER_HEADER, session.getUserId());
        } else {
            // 防止黑客攻击
            headers.remove(AuthConstant.CURRENT_USER_HEADER);
        }
        headers.set(AuthConstant.AUTHORIZATION_HEADER, authorization);

        return authorization;
    }

    private void checkBannedUsers(String userId) {
        if (bannedUsers.containsKey(userId)) {
            log.warn(String.format("Banned user accessing service - user %s", userId));
            throw new ForbiddenException("Banned user forbidden!");
        }
    }

    /**
     * 获取服务
     *
     * @param mapping
     * @return
     */
    private Service getService(MappingProperties mapping) {
        String host = mapping.getHost();
        String subDomain = host.replace("." + envConfig.getExternalApex(), "");
        Service service = ServiceDirectory.getMapping().get(subDomain.toLowerCase());
        if (service == null) {
            throw new FaradayException("Unsupported sub-domain " + subDomain);
        }
        return service;
    }

    /**
     * 检查Service是否受限制
     *
     * @param mapping
     */
    private void validateRestrict(MappingProperties mapping) {
        Service service = this.getService(mapping);
        if (service.isRestrictDev() && !envConfig.isDebug()) {
            throw new FaradayException("This service is restrict to dev and test environment only");
        }
    }

    /**
     * 检查安全授权是否正常
     * 使用Request Service
     *
     * @param data
     * @param mapping
     * @param authorization
     */
    private void validateSecurity(RequestData data, MappingProperties mapping, String authorization) {
        // 检查是否授权
        if (AuthConstant.AUTHORIZATION_ANONYMOUS_WEB.equals(authorization)) {
            Service service = this.getService(mapping);
            // 检查安全性
            if (SecurityConstant.SEC_PUBLIC != service.getSecurity()) {
                log.info("Anonymous user want to access secure service, redirect to login");
                // 重定向到login页面
                String scheme = "https";
                if (envConfig.isDebug()) {
                    scheme = "http";
                }

                int port = data.getOriginRequest().getServerPort();

                try {
                    URI redirectUrl = new URI(scheme,
                            null,
                            "www." + envConfig.getExternalApex(),
                            port,
                            "/login/", null, null);

                    String returnTo = data.getHost() + data.getUri();
                    String fullRedirectUrl = redirectUrl.toString() + "?return_to=" + returnTo;

                    data.setNeedRedirect(true);
                    data.setRedirectUrl(fullRedirectUrl);
                } catch (URISyntaxException e) {
                    log.error("Fail to build redirect url", e);
                }
            }
        }
    }

    /**
     * JWT校验和取出用户会话数据
     *
     * @param request
     * @return
     */
    private Session getSession(HttpServletRequest request) {
        String token = Sessions.getToken(request);
        if (token == null) return null;
        try {
            // 解码后的JWT
            DecodedJWT decodedJWT = Sign.verifySessionToken(token, signingSecret);
            String userId = decodedJWT.getClaim(Sign.CLAIM_USER_ID).asString();
            boolean support = decodedJWT.getClaim(Sign.CLAIM_SUPPORT).asBoolean();
            // 创建Session
            Session session = Session.builder().userId(userId).support(support).build();
            return session;
        } catch (Exception e) {
            log.error("fail to verify token", "token", token, e);
            return null;
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Session {
        private String userId;
        private boolean support;
    }
}
