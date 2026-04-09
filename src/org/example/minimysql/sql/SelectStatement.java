package org.example.minimysql.sql;

/**
 * @author duoyian
 * @date 2026/4/8
 */
public class SelectStatement implements AST {
    public String tableName;
    public Long whereId;

    public SelectStatement(String tableName, Long whereId) {
        this.tableName = tableName;
        this.whereId = whereId;
    }

    public boolean hasCondition() {
        return whereId != null;
    }
}
