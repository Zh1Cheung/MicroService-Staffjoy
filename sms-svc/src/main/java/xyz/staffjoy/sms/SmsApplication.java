package xyz.staffjoy.sms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/*
 *
 * 消息通知服务，支持短信通知方式，可以对接各种云服务，比如阿里云短信服务。
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:49 2019/12/30
 *
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class SmsApplication {

    public static void main(String[] args) {

        SpringApplication.run(SmsApplication.class, args);
    }
}

