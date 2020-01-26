package xyz.staffjoy.faraday;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 *
 * Faraday，是一个反向代理(功能类似 nginx)，也可以看作是一个网关(功能类似 zuul)，它是用户访问 Staffjoy 微服务应用的流量入口，
 * 它既实现对前端应用和后端 API 的路由访问，也实现登录鉴权和访问控制等安全功能。
 * Faraday 代理是 Staffjoy 微服务架构和前后分离架构的关键，并且它是唯一具有公网 IP 的服务
 *3
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:56 2019/12/29
 *
 */
@SpringBootApplication
public class FaradayApplication {
    public static void main(String[] args) {
        SpringApplication.run(FaradayApplication.class, args);
    }
}
