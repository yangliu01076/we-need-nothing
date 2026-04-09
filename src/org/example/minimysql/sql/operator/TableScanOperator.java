package org.example.minimysql.sql.operator;

import org.example.minimysql.engine.MemoryEngine;

import java.util.Iterator;

/**
 * @author duoyian
 * @date 2026/4/8
 */
public class TableScanOperator implements Operator {
    private final Iterator<Object[]> iterator;

    public TableScanOperator(String tableName, MemoryEngine engine) {
        this.iterator = engine.scan(tableName);
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Object[] next() {
        return iterator.next();
    }
}
