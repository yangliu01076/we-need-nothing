package org.example.minimybatis.core;

import org.example.minimybatis.constants.StringConstants;
import org.example.minispring.annotation.Autowired;
import org.example.minispring.annotation.Component;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/**
 * @author duoyian
 * @date 2026/2/27
 */
@Component
public class SimpleExecutor {

    @Autowired
    private Configuration configuration;

    public <T> List<T> query(MappedStatement mappedStatement, Object param) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<T> results = new ArrayList<>();

        try {
            // 1. 获取连接 (这里为了简化，硬编码获取配置，实际应从 Configuration 传过来)
            // 假设我们已经在 Configuration 中存好了连接信息，这里简单模拟获取
            connection = DriverManager.getConnection(
                    configuration.getDataSource().get(StringConstants.XML_DATA_SOURCE_URL),
                    configuration.getDataSource().get(StringConstants.XML_DATA_SOURCE_USERNAME),
                    configuration.getDataSource().get(StringConstants.XML_DATA_SOURCE_PASSWORD));

            // 2. 预编译 SQL
            String sql = mappedStatement.getSql();
            preparedStatement = connection.prepareStatement(sql);

            // 3. 设置参数 (这里只处理单参数，且假设只有一个占位符 ?)
            if (param != null) {
                preparedStatement.setObject(1, param);
            }

            // 4. 执行查询
            resultSet = preparedStatement.executeQuery();

            // 5. 结果集反射映射
            String resultType = mappedStatement.getResultType();
            Class<?> clazz = Class.forName(resultType);

            while (resultSet.next()) {
                Object entity = clazz.newInstance();
                // 获取结果集元数据，以便拿到列名
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    // 列名 (如 id, name)
                    String columnName = metaData.getColumnName(i);
                    // 列值
                    Object columnValue = resultSet.getObject(i);
                    // 利用反射将值 set 进对象
                    // 注意：这里假设数据库列名和 Java 属性名一致（如 user_name -> userName 的转换这里略过）
                    Field field = clazz.getDeclaredField(columnName);
                    // 暴力反射，忽略 private 修饰符
                    field.setAccessible(true);
                    field.set(entity, columnValue);
                }
                results.add((T) entity);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("数据库查询失败: " + e.getMessage());
        } finally {
            // 【核心部分】资源关闭
            // 关闭顺序：ResultSet -> PreparedStatement -> Connection
            // 每一个关闭前都要判空，并且要单独 try-catch，防止其中一个关闭失败导致后续无法关闭

            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return results;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
