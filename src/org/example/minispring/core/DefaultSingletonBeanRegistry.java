package org.example.minispring.core;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author duoyian
 * @date 2026/2/14
 */
public class DefaultSingletonBeanRegistry {
    // 一级缓存：存放完整的 Bean
    protected final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    // 二级缓存：存放提前暴露的 Bean（半成品，可能是代理对象）
    protected final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(16);

    // 三级缓存：存放 Bean 的工厂
    protected final Map<String, ObjectFactory<?>> singletonFactories = new ConcurrentHashMap<>(16);

    protected final Set<String> registeredSingletons = ConcurrentHashMap.newKeySet();

    @FunctionalInterface
    public interface ObjectFactory<T> {
        T getObject();
    }

    // 添加单例
    protected void addSingleton(String beanName, Object singletonObject) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.put(beanName, singletonObject);
            this.singletonFactories.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
            this.registeredSingletons.add(beanName);
        }
    }

    // 添加三级缓存工厂
    protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
        synchronized (this.singletonObjects) {
            if (!this.singletonObjects.containsKey(beanName)) {
                this.singletonFactories.put(beanName, singletonFactory);
                this.registeredSingletons.add(beanName);
            }
        }
    }

    // 获取单例（核心逻辑：三级查找）
    protected Object getSingleton(String beanName) {
        Object singletonObject = this.singletonObjects.get(beanName);
        if (singletonObject == null) {
            synchronized (this.singletonObjects) {
                singletonObject = this.earlySingletonObjects.get(beanName);
                if (singletonObject == null) {
                    ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                    if (singletonFactory != null) {
                        // 从三级缓存获取，并升级到二级缓存
                        singletonObject = singletonFactory.getObject();
                        this.earlySingletonObjects.put(beanName, singletonObject);
                        this.singletonFactories.remove(beanName);
                    }
                }
            }
        }
        return singletonObject;
    }
}
