package org.example.minispring.annotation;

import java.lang.annotation.*;

/**
 * @author duoyian
 * @date 2026/2/14
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ComponentScan {

    // 指定扫描的包路径
    String[] value() default {};

    // 指定扫描的类
    Class<?>[] basePackageClasses() default {};
}