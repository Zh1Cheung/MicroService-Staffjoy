package xyz.staffjoy.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import xyz.staffjoy.common.error.GlobalExceptionTranslator;
import xyz.staffjoy.common.aop.*;

/*
 *
 * Rest API公共配置
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 22:12 2019/12/28
 *
 */
@Configuration
@Import(value = {StaffjoyConfig.class, SentryClientAspect.class, GlobalExceptionTranslator.class})
public class StaffjoyRestConfig  {
}
