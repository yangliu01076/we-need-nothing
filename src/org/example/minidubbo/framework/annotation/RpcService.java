package org.example.minidubbo.framework.annotation;

import org.example.minispring.annotation.Component;

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
