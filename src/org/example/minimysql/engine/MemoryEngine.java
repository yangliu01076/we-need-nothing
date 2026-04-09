package org.example.minimysql.engine;

import org.example.minimysql.index.BPlusTree;

import java.util.*;

/**
 * @author duoyian
 * @date 2026/4/8
 */
public class MemoryEngine {
    private final Map<String, Table> tables = new HashMap<>();
    // 表名 -> B+ 树索引
    private final Map<String, BPlusTree> indexes = new HashMap<>();

    public void createTable(String name, List<String> columns) {
        tables.put(name, new Table(name, columns));
        // 默认为第一列创建索引
        indexes.put(name, new BPlusTree(4));
    }

    public void insert(String tableName, Object[] values) {
        Table t = tables.get(tableName);
        if (t == null) throw new RuntimeException("Table not found: " + tableName);

        // 1. 写入堆存储
        t.rows.add(values);
        int rowIndex = t.rows.size() - 1;

        // 2. 更新索引
        if (values.length > 0 && values[0] instanceof Long) {
            BPlusTree tree = indexes.get(tableName);
            if (tree != null) {
                tree.insert((Long) values[0], rowIndex);
            }
        }
    }

    public Iterator<Object[]> scan(String tableName) {
        Table t = tables.get(tableName);
        return t != null ? t.rows.iterator() : Collections.emptyIterator();
    }

    public Object[] lookupByIndex(String tableName, long key) {
        BPlusTree tree = indexes.get(tableName);
        if (tree == null) return null;

        Integer rowIndex = tree.search(key);
        if (rowIndex != null) {
            return tables.get(tableName).rows.get(rowIndex);
        }
        return null;
    }
}
