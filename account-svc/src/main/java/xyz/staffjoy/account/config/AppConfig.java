package xyz.staffjoy.account.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import xyz.staffjoy.common.async.ContextCopyingDecorator;
import xyz.staffjoy.common.config.StaffjoyRestConfig;

import java.util.concurrent.Executor;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 21:32 2019/12/24
 *
 */

/*
@Configuration ：定义配置类，被注解的类内部包含有一个或多个被@Bean注解的方法，这些方法将会被AnnotationConfigApplicationContext或AnnotationConfigWebApplicationContext类进行扫描，并用于构建bean定义，初始化Spring容器。
注意：@Configuration注解的配置类有如下要求：
@Configuration不可以是final类型；
@Configuration不可以是匿名类；
嵌套的configuration必须是静态类。

一、用@Configuration加载spring
    1.1、@Configuration配置spring并启动spring容器
    1.2、@Configuration启动容器+@Bean注册Bean
    1.3、@Configuration启动容器+@Component注册Bean
    1.4、使用 AnnotationConfigApplicationContext 注册 AppContext 类的两种方法
    1.5、配置Web应用程序(web.xml中配置AnnotationConfigApplicationContext)
二、组合多个配置类
    2.1、在@configuration中引入spring的xml配置文件
    2.2、在@configuration中引入其它注解配置
    2.3、@configuration嵌套（嵌套的Configuration必须是静态类）
三、@EnableXXX注解
四、@Profile逻辑组配置
五、使用外部变量

---
自定义线程池的配置类，并在类上添加@EnableAsync 注解，然后在需要异步的方法上使用@Async("线程池名称") 该方法就可以异步执行了。

 */

@Configuration
@EnableAsync
@Import(value = {StaffjoyRestConfig.class})
@SuppressWarnings(value = "Duplicates")
public class AppConfig {

    public static final String ASYNC_EXECUTOR_NAME = "asyncExecutor";

    /**
     * 线程池初始化
     *
     * @return
     */
    @Bean(name = ASYNC_EXECUTOR_NAME)
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // for passing in request scope context
        executor.setTaskDecorator(new ContextCopyingDecorator());
        //核心线程池数量，方法: 返回可用处理器的Java虚拟机的数量。
        executor.setCorePoolSize(3);
        //最大线程数量
        executor.setMaxPoolSize(5);
        //线程池的队列容量
        executor.setQueueCapacity(100);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        //线程名称的前缀
        executor.setThreadNamePrefix("AsyncThread-");
        // setRejectedExecutionHandler：当pool已经达到max size的时候，如何处理新任务
        // CallerRunsPolicy：不在新线程中执行任务，而是由调用者所在的线程来执行
        //executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
