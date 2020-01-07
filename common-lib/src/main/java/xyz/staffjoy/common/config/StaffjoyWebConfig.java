package xyz.staffjoy.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import xyz.staffjoy.common.aop.SentryClientAspect;

/*
 *
 * Web App公共配置
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 22:18 2019/12/28
 *
 */
@Configuration
@Import(value = {StaffjoyConfig.class, SentryClientAspect.class,})
public class StaffjoyWebConfig {
}
