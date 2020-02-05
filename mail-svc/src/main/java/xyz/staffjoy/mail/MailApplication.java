package xyz.staffjoy.mail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/*
 *
 * 消息通知服务，支持邮件通知方式，可以对接各种云服务，比如阿里云邮件服务。
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:48 2019/12/30
 *
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MailApplication {

    public static void main(String[] args) {

        SpringApplication.run(MailApplication.class, args);
    }

}

