package minispring.annotation;

import java.lang.annotation.*;

/**
 * @author duoyian
 * @date 2026/2/14
 */
@Target(ElementType.TYPE)           // 作用在类上
@Retention(RetentionPolicy.RUNTIME) // 运行时生效
@Documented
@Inherited                          // 可继承
// 【核心】组合三个注解
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan
public @interface SpringBootApplication {

    // 允许用户指定扫描包（透传给 ComponentScan）
    String[] scanBasePackages() default {};

    // 允许用户排除自动配置类（透传给 EnableAutoConfiguration）
    Class<?>[] exclude() default {};
}
