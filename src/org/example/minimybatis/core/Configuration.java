package org.example.minimybatis.core;

import org.example.minispring.annotation.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局配置对象，存解析后的所有信息
 * @author duoyian
 * @date 2026/2/27
 */
public class Configuration {
    // 数据库连接配置
    private Map<String, String> dataSource = new HashMap<>();

    // 存放所有的 MappedStatement，key = namespace.id (例如 org.example.minimybatis.xml.mapper.UserMapper.selectById)
    private Map<String, MappedStatement> mappedStatementMap = new HashMap<>();

    public Map<String, String> getDataSource() {
        return dataSource;
    }

    public void setDataSource(Map<String, String> dataSource) {
        this.dataSource = dataSource;
    }

    public Map<String, MappedStatement> getMappedStatementMap() {
        return mappedStatementMap;
    }

    public void setMappedStatementMap(Map<String, MappedStatement> mappedStatementMap) {
        this.mappedStatementMap = mappedStatementMap;
    }
}
