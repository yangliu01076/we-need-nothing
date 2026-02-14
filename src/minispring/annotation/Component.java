package minispring.annotation;

import java.lang.annotation.*;

/**
 * @author duoyian
 * @date 2026/2/14
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {
    String value() default "";
}
