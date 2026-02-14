package minispring.annotation;

import java.lang.annotation.*;

/**
 * @author duoyian
 * @date 2026/2/14
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PostConstruct {
}
