package xyz.staffjoy.common.crypto;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.util.StringUtils;
import xyz.staffjoy.common.error.ServiceException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Sign {

    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_SUPPORT = "support";

    // <signingToken, verifier>  通过verifier返回一个Token
    private static final Map<String, JWTVerifier> verifierMap = new HashMap<>();
    private static final Map<String, Algorithm> algorithmMap = new HashMap<>();

    public static DecodedJWT verifyEmailConfirmationToken(String tokenString, String signingToken) {
        return verifyToken(tokenString, signingToken);
    }

    public static DecodedJWT verifySessionToken(String tokenString, String signingToken) {
        return verifyToken(tokenString, signingToken);
    }

    /**
     * JWT校验算法 (返回JWT)
     * 如果相同 返回tokenString本身
     *
     * @param tokenString  生成的Token（JWT）
     * @param signingToken Token签名
     * @return
     */
    private static DecodedJWT verifyToken(String tokenString, String signingToken) {
        JWTVerifier verifier = verifierMap.get(signingToken);
        if (verifier == null) {
            synchronized (verifierMap) {
                verifier = verifierMap.get(signingToken);
                if (verifier == null) {
                    Algorithm algorithm = Algorithm.HMAC512(signingToken);
                    // 选择验证算法
                    verifier = JWT.require(algorithm).build();
                    verifierMap.put(signingToken, verifier);
                }
            }
        }
        // 公钥验证 token
        DecodedJWT jwt = verifier.verify(tokenString);
        return jwt;
    }

    /**
     * 生成Token的算法
     *
     * @param signingToken
     * @return
     */
    private static Algorithm getAlgorithm(String signingToken) {
        Algorithm algorithm = algorithmMap.get(signingToken);
        if (algorithm == null) {
            synchronized (algorithmMap) {
                algorithm = algorithmMap.get(signingToken);
                if (algorithm == null) {
                    // 签名算法
                    algorithm = Algorithm.HMAC512(signingToken);
                    algorithmMap.put(signingToken, algorithm);
                }
            }
        }
        return algorithm;
    }

    /**
     * JWT生成算法 (返回Token）
     * Jwt组成 ： header，payload，s·ignature
     *
     * @param userId
     * @param signingToken
     * @param support
     * @param duration
     * @return
     */
    public static String generateSessionToken(String userId, String signingToken, boolean support, long duration) {
        if (StringUtils.isEmpty(signingToken)) {
            throw new ServiceException("No signing token present");
        }
        Algorithm algorithm = getAlgorithm(signingToken);
        return JWT.create()
                // withClaim ： payload
                // withExpiresAt : sign time
                // sign : signature
                .withClaim(CLAIM_USER_ID, userId)
                .withClaim(CLAIM_SUPPORT, support)
                .withExpiresAt(new Date(System.currentTimeMillis() + duration))
                .sign(algorithm);
    }

    /**
     * JWT生成算法 (返回Token）
     *
     * @param userId
     * @param email
     * @param signingToken
     * @return
     */
    public static String generateEmailConfirmationToken(String userId, String email, String signingToken) {
        Algorithm algorithm = getAlgorithm(signingToken);
        return JWT.create()
                .withClaim(CLAIM_EMAIL, email)
                .withClaim(CLAIM_USER_ID, userId)
                .withExpiresAt(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2)))
                .sign(algorithm);
    }

}
