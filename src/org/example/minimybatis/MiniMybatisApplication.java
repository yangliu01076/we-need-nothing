package org.example.minimybatis;

import org.example.minimybatis.core.Configuration;
import org.example.minimybatis.core.DefaultSqlSession;
import org.example.minimybatis.core.SimpleExecutor;
import org.example.minimybatis.core.XmlConfigParser;
import org.example.minimybatis.dto.User;
import org.example.minimybatis.mapper.UserMapper;
import org.example.minispring.annotation.Autowired;
import org.example.minispring.annotation.Bean;
import org.example.minispring.annotation.Component;
import org.example.utils.JsonUtil;

/**
 * @author duoyian
 * @date 2026/2/27
 */
@Component
public class MiniMybatisApplication {

    private static final String CONFIG_PATH = "/Users/duoyian/IdeaProjects/demo/src/org/example/minimybatis/xml/config/mybatis-config.xml";

    @Autowired
    private DefaultSqlSession defaultSqlSession;

    public void start() throws ClassNotFoundException {
        // 【关键】强制加载 MockDriver 类，触发其 static 代码块进行注册
        Class.forName("com.example.minimybatis.mock.MockDriver");

        // 3. 获取 Mapper 代理对象
        UserMapper userMapper = defaultSqlSession.getMapper(UserMapper.class);

        // 4. 调用方法
        User user = userMapper.selectById(1);

        System.out.println("查询结果: " + user);
    }

    @Bean
    public Configuration configuration() {
        return XmlConfigParser.parse(CONFIG_PATH);
    }

    public static void main(String[] args) throws ClassNotFoundException {
        // 【关键】强制加载 MockDriver 类，触发其 static 代码块进行注册
        Class.forName("org.example.minimybatis.mock.MockDriver");
        // 1. 解析配置文件
        Configuration configuration = XmlConfigParser.parse(CONFIG_PATH);

        // 2. 创建 SqlSession
        DefaultSqlSession sqlSession = new DefaultSqlSession();
        sqlSession.setConfiguration(configuration);
        SimpleExecutor simpleExecutor = new SimpleExecutor();
        simpleExecutor.setConfiguration(configuration);
        sqlSession.setSimpleExecutor(simpleExecutor);

        // 3. 获取 Mapper 代理对象
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

        // 4. 调用方法
        User user = userMapper.selectById(1);

        System.out.println("查询结果: " + JsonUtil.toJson(user));
    }
}
