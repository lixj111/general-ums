package com.mall_tiny.security.aspect;

import com.mall_tiny.security.annotation.CacheException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Redis缓存切面，防止Redistribution宕机影响正常业务逻辑
 * 即在使用该注解的方法中，出现异常时可选择性抛出，或仅在日志中记录不抛出
 */
@Aspect
@Component
// @Aspect 和 @Component 标记了这是一个Spring AOP切面类，并且是一个Spring管理的组件，自动参与Spring容器的生命周期管理。
@Order(2) // 置了切面的优先级，数字越小表示优先级越高
public class RedisCacheAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheAspect.class);

    // TODO: security??? 待修改表达式中的security
    @Pointcut("execution(public * com.mall_tiny.modules.*CacheService.*(..))")
    // 定义了一个切入点表达式，它指定了哪些方法是这个切面的目标。
    // 这里的表达式匹配的是所有在 com.mall_tiny.modules 包下以 CacheService 结尾的类中的公共方法
    public void cacheAspect() {
    }

    // @Around：一个环绕通知（around advice），它会在切入点所匹配的方法执行前后运行
    @Around("cacheAspect()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        /**
         * 它接收一个 ProceedingJoinPoint 参数，代表了连接点（即目标方法调用），允许我们控制目标方法的执行。
         * 获取当前执行的方法签名并转换为 MethodSignature，进而获取到具体的方法对象。
         * 尝试执行目标方法 (joinPoint.proceed())
         * 如果执行过程中抛出了异常：
         *   如果目标方法上有 CacheException 注解，则重新抛出异常。
         *   否则，只是记录错误日志而不抛出异常，这通常意味着系统将继续运行而不会中断业务流程。
         * 最终返回目标方法的结果。
         */
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            // 有CacheException注解的方法需要抛出异常
            if (method.isAnnotationPresent(CacheException.class)) {
                throw throwable;
            } else {
                LOGGER.error(throwable.getMessage());
            }
        }
        return result;
    }
}
