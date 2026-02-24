package org.example.minispring.core;

import org.example.minispring.annotation.Autowired;
import org.example.minispring.annotation.PostConstruct;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * @author duoyian
 * @date 2026/2/14
 */
public class AbstractAutowireCapableBeanFactory extends DefaultSingletonBeanRegistry {

    private final Map<String, BeanDefinition> BEAN_DEFINITION_MAP = new ConcurrentHashMap<>(256);

    // 注册 BeanDefinition
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        this.BEAN_DEFINITION_MAP.put(beanName, beanDefinition);
    }

    // 获取 Bean（Template Method）
    public Object getBean(String beanName) {
        Object singleton = getSingleton(beanName);
        if (singleton != null) {
            return singleton;
        }
        return createBean(beanName, getBeanDefinition(beanName));
    }

    protected BeanDefinition getBeanDefinition(String beanName) {
        return BEAN_DEFINITION_MAP.get(beanName);
    }

    public Set<String> getBeanDefinitionNames() {
        return BEAN_DEFINITION_MAP.keySet();
    }

    // 创建 Bean 的核心流程
    protected Object createBean(String beanName, BeanDefinition bd) {
        // 1. 实例化
        Object bean = doCreateBean(bd.getBeanClass());

        // 2. 暴露早期引用（解决循环依赖）
        // 在这里简化，直接存入三级缓存
        addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, bean));

        // 3. 属性填充
        populateBean(bean);

        // 4. 初始化
        initializeBean(bean);

        // 5. 添加到一级缓存
        addSingleton(beanName, bean);

        return bean;
    }

    // 简单实例化
    protected Object doCreateBean(Class<?> beanClass) {
        try {
            return beanClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Instantiation failed", e);
        }
    }

    // AOP 前置检查：这里简化，直接返回原对象
    protected Object getEarlyBeanReference(String beanName, Object bean) {
        // 真实的 Spring 这里会调用 SmartInstantiationAwareBeanPostProcessor
        return bean;
    }

    // 属性填充
    protected void populateBean(Object bean) {
        Class<?> clazz = bean.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                try {
                    // 递归调用 getBean
                    Object value = getBean(field.getName());
                    field.set(bean, value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void initializeBean(Object bean) {
        // 1. 执行 @PostConstruct 方法
        invokePostConstruct(bean);

        // 2. 这里可以预留 InitializingBean.afterPropertiesSet() 的逻辑
        System.out.println("Bean initialized: " + bean.getClass().getSimpleName());
    }

    /**
     * 处理 @PostConstruct 注解
     */
    private void invokePostConstruct(Object bean) {
        Class<?> clazz = bean.getClass();
        // 遍历所有方法
        for (Method method : clazz.getDeclaredMethods()) {
            // 检查是否有 @PostConstruct 注解
            if (method.isAnnotationPresent(PostConstruct.class)) {
                // 检查方法签名：必须是无参，非静态
                if (method.getParameterCount() != 0 || java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                    throw new RuntimeException("@PostConstruct method must be no-arg and non-static: " + method.getName());
                }

                try {
                    method.setAccessible(true);
                    // 反射调用
                    System.out.println("Executing @PostConstruct: " + method.getName() + " in " + clazz.getSimpleName());
                    method.invoke(bean);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to invoke @PostConstruct", e);
                }
            }
        }
    }
}
