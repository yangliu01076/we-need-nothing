package org.example.minimybatis.core;

/**
 * 对应 XML 中一个 <select> 或 <insert> 节点
 * @author duoyian
 * @date 2026/2/27
 */
public class MappedStatement {
    private String id;
    private String resultType;
    private String sql;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
