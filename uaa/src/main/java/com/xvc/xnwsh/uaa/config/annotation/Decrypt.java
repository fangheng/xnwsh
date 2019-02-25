package com.xvc.xnwsh.uaa.config.annotation;

import java.lang.annotation.*;

/**
 * 解密注解
 *
 * 加了此注解的接口将进行数据解密操作
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Decrypt {

   // boolean inDecode() default true;
}
