package org.example.minimysql.sql.operator;

/**
 * @author duoyian
 * @date 2026/4/8
 */
public class IndexLookupOperator implements Operator {
    private final Object[] row; // 它是数组
    private boolean called = false;

    // 修正：参数类型改为 Object[]，类型完全匹配
    public IndexLookupOperator(Object[] row) {
        this.row = row;
    }

    @Override
    public boolean hasNext() {
        return !called && row != null;
    }

    @Override
    public Object[] next() {
        called = true;
        return row;
    }
}
