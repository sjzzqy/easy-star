package org.qzzy.configurer.logger;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * @author Qzzy
 * @Generated: 2023/4/28 9:08
 */
public class WebLogAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Pointcut("execution(public * org.qzzy.*.*(..))")
    public void webLog() {
    }

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) {
        startTime.set(System.currentTimeMillis());
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (null != attributes) {
            HttpServletRequest request = attributes.getRequest();
            String url = request.getRequestURL().toString();
            StringBuilder sb = new StringBuilder();
            final Object[] args = joinPoint.getArgs();

            Enumeration<String> enu = request.getParameterNames();
            if (!enu.hasMoreElements()) {
                sb.append(JSON.toJSONString(joinPoint.getArgs()));
            } else {
                while (enu.hasMoreElements()) {
                    String paraName = enu.nextElement();
                    sb.append(paraName).append(": ").append(request.getParameter(paraName)).append(",");
                }
            }
            logger.info(String.format("request url: %s, params: %s", url, StringUtils.removeEnd(StringUtils.trim(sb.toString()), ",")));
        } else
            logger.error("Request invalid,This is a serious error");
    }

    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        return pjp.proceed();// ob 为方法的返回值
    }

    @AfterReturning(returning = "ret", pointcut = "webLog()")
    public void doAfterReturning(JoinPoint jp, Object ret) {
        // 处理完请求，返回内容
        final String s = jp.toString();
        final String c = JSON.toJSONString(ret);
        logger.info(String.format("response url: %s, millisecond: %s, 返回值 : %s", s, (System.currentTimeMillis() - startTime.get()), c));
    }

    @AfterThrowing(throwing = "e", pointcut = "webLog()")
    public void doAfterReturningThrowable(JoinPoint jp, Throwable e) {
        // 目标方法异常继续往外抛的处理
        final String s = jp.toString();
        logger.info(String.format("response Exception: %s, millisecond: %s, 返回值 : %s", s, (System.currentTimeMillis() - startTime.get()), e));
    }
}
