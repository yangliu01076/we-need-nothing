package org.example.minimybatis.core;

import org.example.minispring.annotation.Autowired;
import org.example.minispring.annotation.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * @author duoyian
 * @date 2026/2/27
 */
@Component
public class DefaultSqlSession {

    @Autowired
    private Configuration configuration;

    @Autowired
    private SimpleExecutor simpleExecutor;

    // getMapper 方法
    @SuppressWarnings("unchecked")
    public <T> T getMapper(Class<T> mapperClass) {
        // 使用 JDK 动态代理生成 Mapper 接口的实现类
        return (T) Proxy.newProxyInstance(
                mapperClass.getClassLoader(),
                new Class[]{mapperClass},
                new MapperProxy()
        );
    }

    // 代理处理器
    private class MapperProxy implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 1. 获取接口全名
            String className = method.getDeclaringClass().getName();
            // 2. 获取方法名
            String methodName = method.getName();
            // 3. 组合成 StatementId
            String statementId = className + "." + methodName;

            // 4. 从 Configuration 中找到对应的 MappedStatement
            MappedStatement mappedStatement = configuration.getMappedStatementMap().get(statementId);
            if (mappedStatement == null) {
                throw new RuntimeException("找不到 SQL 配置: " + statementId);
            }

            // 5. 调用 Executor 执行查询
            // 这里假设返回的是 List，如果是 selectOne 可以在 MapperProxy 里做判断
            List<Object> list = simpleExecutor.query(mappedStatement, args[0]);

            // 6. 根据返回值类型决定返回 List 还是单个对象
            // 简单判断：如果是 List 类型直接返回，否则返回第一个
            if (method.getReturnType().equals(List.class)) {
                return list;
            } else {
                return list.get(0);
            }
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public SimpleExecutor getSimpleExecutor() {
        return simpleExecutor;
    }

    public void setSimpleExecutor(SimpleExecutor simpleExecutor) {
        this.simpleExecutor = simpleExecutor;
    }
}
