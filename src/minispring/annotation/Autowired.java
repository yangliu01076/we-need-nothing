package minispring.annotation;

import java.lang.annotation.*;

/**
 * @author duoyian
 * @date 2026/2/14
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Autowired {
}
