package org.example.minispring.core;

import org.example.minispring.annotation.Component;
import org.example.minispring.annotation.ComponentScan;
import org.example.minispring.annotation.SpringBootApplication;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

/**
 * @author duoyian
 * @date 2026/2/14
 */
public class MiniApplicationContext  extends AbstractAutowireCapableBeanFactory {
    private String scanPath;

    public MiniApplicationContext(Class<?> primarySource) {
        // 1. 解析启动类上的注解
        String scanPath = resolveBasePackages(primarySource);

        System.out.println("=== Start Mini Spring ===");
        System.out.println("Scan Base Package: " + scanPath);

        // 2. 扫描包
        scanPackages(scanPath);

        // 3. 初始化 Bean
        finishBeanFactoryInitialization();
    }

    private void scanPackages(String scanPath) {
        String packagePath = scanPath.replace(".", "/");
        try {
            Enumeration<URL> urls = getClass().getClassLoader().getResources(packagePath);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                File file = new File(url.getFile());
                if (file.isDirectory()) {
                    scanDirectory(file, scanPath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scanDirectory(File directory, String packageName) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                try {
                    Class<?> clazz = Class.forName(className);
                    // 这里只会扫描到 直接标注了 @Component 注解的类
                    if (clazz.isAnnotationPresent(Component.class)) {
                        if (clazz.isAnnotation() || clazz.isInterface()) {
                            // 如果它本身是个注解（比如 @Service），跳过注册
                            continue;
                        }
                        Component component = clazz.getAnnotation(Component.class);
                        String beanName = component.value().isEmpty()
                                ? clazz.getSimpleName().substring(0, 1).toLowerCase() + clazz.getSimpleName().substring(1)
                                : component.value();

                        BeanDefinition bd = new BeanDefinition(clazz);
                        bd.setBeanName(beanName);

                        // 【关键修改】：只注册，不调用 getBean！
                        registerBeanDefinition(beanName, bd);
                        System.out.println("Registered BeanDefinition: " + beanName);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 模拟 Spring 的 finishBeanFactoryInitialization
    private void finishBeanFactoryInitialization() {
        // 遍历所有已注册的 BeanDefinition
        // 注意：这里需要访问父类的 beanDefinitionMap，建议在父类中提供一个 getter 方法
        for (String beanName : getBeanDefinitionNames()) {
            // 此时所有的 BeanDefinition 都已经注册了，依赖关系一定能找到
            getBean(beanName);
        }
    }

    // 解析扫描路径的逻辑
    private String resolveBasePackages(Class<?> primarySource) {
        // 检查是否有 @SpringBootApplication
        if (primarySource.isAnnotationPresent(SpringBootApplication.class)) {
            SpringBootApplication annotation = primarySource.getAnnotation(SpringBootApplication.class);

            // 如果用户显式指定了 scanBasePackages，就用指定的
            if (annotation.scanBasePackages().length > 0) {
                return annotation.scanBasePackages()[0];
            }

            // 如果没指定，默认扫描启动类所在的包
            return primarySource.getPackage().getName();
        }

        // 如果没有该注解（降级处理），检查是否有 @ComponentScan
        if (primarySource.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScan = primarySource.getAnnotation(ComponentScan.class);
            if (componentScan.value().length > 0) {
                return componentScan.value()[0];
            }
        }

        // 默认扫描当前包
        return primarySource.getPackage().getName();
    }
}
