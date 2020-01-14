package xyz.staffjoy.company;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/*
 *
 * Company API(公司服务)，支持团队(Team)，雇员(Worker)，任务(Job）和班次(Shift)等核心领域概念的管理功能。
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 12:23 2019/12/29
 *
 */
@EnableFeignClients(basePackages = {"xyz.staffjoy.account", "xyz.staffjoy.bot"})
@SpringBootApplication
public class CompanyApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompanyApplication.class, args);
    }
}

