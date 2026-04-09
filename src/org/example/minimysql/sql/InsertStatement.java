package org.example.minimysql.sql;

import java.util.List;

/**
 * @author duoyian
 * @date 2026/4/8
 */
public class InsertStatement implements AST {
    public String tableName;
    public List<String> values;

    public InsertStatement(String tableName, List<String> values) {
        this.tableName = tableName;
        this.values = values;
    }
}
