package com.mall_tiny.security.annotation;

import java.lang.annotation.*;

/**
 * 自定义注解，有该注解的缓存方法会抛出异常
 * 用于标记那些在发生缓存操作失败时需要抛出异常的方法。
 * 当这些方法执行过程中出现错误时，它们不会静默地记录错误，而是会直接将异常传播出去
 */

@Documented  // 表明该注解应该被 javadoc 工具记录
@Target(ElementType.METHOD) // 指定了此注解可以应用于方法级别
@Retention(RetentionPolicy.RUNTIME) // 表示这个注解将在运行时可用，这意味着它可以通过反射机制在程序运行期间被读取
public @interface CacheException {
}
