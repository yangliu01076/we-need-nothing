package minidubbo.framework.annotation;

import minispring.annotation.Component;

import java.lang.annotation.*;

/**
 * @author duoyian
 * @date 2026/2/14
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
@Component
public @interface RpcService {
}
