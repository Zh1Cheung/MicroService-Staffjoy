package xyz.staffjoy.common.aop;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import xyz.staffjoy.common.env.EnvConfig;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:33 2019/12/28
 *
 */

/*

既可以在目标方法之前织入增强动作，也可以在执行目标方法之后织入增强动作；
可以决定目标方法在什么时候执行，如何执行，甚至可以完全阻止目标目标方法的执行；
可以改变执行目标方法的参数值，也可以改变执行目标方法之后的返回值； 当需要改变目标方法的返回值时，只能使用Around方法；

---

任意公共方法的执行：execution(public * *(..))
任何一个以“set”开始的方法的执行：execution(* set*(..))
AccountService 接口的任意方法的执行：execution(* com.xyz.service.AccountService.*(..))
定义在service包里的任意方法的执行： execution(* com.xyz.service.*.*(..))
定义在service包和所有子包里的任意类的任意方法的执行：execution(* com.xyz.service..*.*(..))
第一个*表示匹配任意的方法返回值， …(两个点)表示零个或多个，第一个…表示service包及其子包,第二个*表示所有类, 第三个*表示所有方法，第二个…表示方法的任意参数个数

---

切面执行顺序：
           AOP
           @Around
           @Before
           Method
           @After
           @AfterReturning

 */
@Aspect
@Slf4j
public class SentryClientAspect {

    static final ILogger logger = SLoggerFactory.getLogger(SentryClientAspect.class);

    @Autowired
    EnvConfig envConfig;

    @Around("execution(* io.sentry.SentryClient.send*(..))")
    public void around(ProceedingJoinPoint joinPoint) throws Throwable {
        // no sentry logging in debug mode
        if (envConfig.isDebug()) {
            logger.debug("no sentry logging in debug mode");
            return;
        }
        joinPoint.proceed();
    }
}
