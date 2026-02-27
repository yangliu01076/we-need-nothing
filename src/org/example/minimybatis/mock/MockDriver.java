package org.example.minimybatis.mock;

import java.sql.*;
import java.util.Properties;

/**
 * @author duoyian
 * @date 2026/2/27
 */
public class MockDriver implements Driver {

    static {
        // 关键步骤：静态注册，让类加载时自动注册到 DriverManager
        try {
            DriverManager.registerDriver(new MockDriver());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        // 只要 URL 包含 "jdbc:mock"，就拦截，返回我们的假连接
        if (url.startsWith("jdbc:mock")) {
            return new MockConnection();
        }
        // 不是我要处理的 URL
        return null;
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url.startsWith("jdbc:mock");
    }

    // 下面是 Driver 接口的其他方法，为了简洁省略实现（或返回默认值）
    @Override public int getMajorVersion() { return 1; }
    @Override public int getMinorVersion() { return 0; }
    @Override public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) { return new DriverPropertyInfo[0]; }
    @Override public boolean jdbcCompliant() { return true; }
    @Override public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException { return null; }
}
