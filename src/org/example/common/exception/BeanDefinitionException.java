package org.example.common.exception;

/**
 * @author duoyian
 * @date 2026/2/25
 */
public class BeanDefinitionException extends RuntimeException {
    private static final long serialVersionUID = -6974617493310548222L;

    public BeanDefinitionException(String message) {
        super(message);
    }

    public BeanDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanDefinitionException(Throwable cause) {
        super(cause);
    }

    public BeanDefinitionException(String beanName, String message) {
        super("bean注册失败, beanName:" + beanName + "，message:" + message);
    }

}
