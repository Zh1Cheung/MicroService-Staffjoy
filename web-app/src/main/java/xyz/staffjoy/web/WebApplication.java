package xyz.staffjoy.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

/*
 *
 * WWW 应用， 是一个前端 MVC 应用，它主要支持产品营销、公司介绍和用户注册登录/登出，这个应用也称为营销站点(Marketing Site)或者登录页(Landing Page)应用。
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 10:07 2019/12/30
 *
 */
@EnableFeignClients(basePackages = {"xyz.staffjoy.account", "xyz.staffjoy.company", "xyz.staffjoy.mail"})
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class WebApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}
