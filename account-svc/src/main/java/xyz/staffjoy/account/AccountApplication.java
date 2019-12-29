package xyz.staffjoy.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/*
 *
 * Account API(账户服务)，提供账户注册、登录认证和账户信息管理等基本功能。
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 11:49 2019/12/28
 *
 */
@SpringBootApplication
@EnableFeignClients(basePackages = {"xyz.staffjoy.mail", "xyz.staffjoy.bot", "xyz.staffjoy.company"})
public class AccountApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountApplication.class, args);
    }
}

