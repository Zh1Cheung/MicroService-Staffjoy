package xyz.staffjoy.whoami;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import xyz.staffjoy.common.config.StaffjoyRestConfig;

/*
 *
 * WhoAmI API，支持前端应用获取当前登录用户的详情信息，包括公司和管理员身份，团队信息等，它也可以看作是一个用户会话(Session)信息服务。
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 14:13 2019/12/30
 *
 */
@Import(value = StaffjoyRestConfig.class)
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableFeignClients(basePackages = {"xyz.staffjoy.company", "xyz.staffjoy.account"})
public class WhoAmIApplication {
    public static void main(String[] args) {
        SpringApplication.run(WhoAmIApplication.class, args);
    }
}
