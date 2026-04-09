package org.example.minimysql.engine;

import java.util.*;

/**
 * @author duoyian
 * @date 2026/4/8
 */
public class Table {
    public String name;
    public List<String> columns;
    public List<Object[]> rows = new ArrayList<>();

    public Table(String name, List<String> columns) {
        this.name = name;
        this.columns = columns;
    }
}
