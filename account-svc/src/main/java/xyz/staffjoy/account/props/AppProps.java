package xyz.staffjoy.account.props;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 21:48 2019/12/24
 *
 */

/**
 *
 * 大量的参数配置在 application.properties 或 application.yml 文件中
 * 通过 @ConfigurationProperties 注解，我们可以方便的获取这些参数值
 * 当我们为属性配置错误的值时，而又不希望 Spring Boot 应用启动失败，我们可以设置 ignoreInvalidFields 属性为 true (默认为 false)
 * 如果我们希望配置参数在传入到应用中时有效的，我们可以通过在字段上添加 bean validation 注解，同时在类上添加 @Validated 注解
 *
 */


@Component
@ConfigurationProperties(prefix = "staffjoy")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppProps {
    @NotNull
    private String intercomAccessToken;
    @NotNull
    private String signingSecret;
}
