package org.example.minimysql.sql;

import java.util.List;

/**
 * @author duoyian
 * @date 2026/4/8
 */
public class CreateTableStatement implements AST {
    public String tableName;
    public List<String> columns;

    public CreateTableStatement(String tableName, List<String> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }
}
