package org.example.minihbase.model;

import java.io.Serializable;

/**
 * HBase 中的 Key-Value 对
 * RowKey + Family + Qualifier + Timestamp -> Value
 * @author duoyian
 * @date 2026/4/8
 */
public class KeyValue implements Comparable<KeyValue>, Serializable {
    private static final long serialVersionUID = -1661355623649739830L;
    private final String rowKey;
    private final String family;
    private final String qualifier;
    private final long timestamp;
    private final byte[] value;

    public KeyValue(String rowKey, String family, String qualifier, long timestamp, byte[] value) {
        this.rowKey = rowKey;
        this.family = family;
        this.qualifier = qualifier;
        this.timestamp = timestamp;
        this.value = value;
    }

    // 核心排序逻辑：先比 RowKey，再比 Family，再比 Qualifier，最后比 Timestamp (倒序，最新的在前)
    @Override
    public int compareTo(KeyValue other) {
        int c = this.rowKey.compareTo(other.rowKey);
        if (c != 0) return c;

        c = this.family.compareTo(other.family);
        if (c != 0) return c;

        c = this.qualifier.compareTo(other.qualifier);
        if (c != 0) return c;

        // 时间戳大的排前面（最新的数据）
        return Long.compare(other.timestamp, this.timestamp);
    }

    public String getRowKey() { return rowKey; }
    public String getFamily() { return family; }
    public String getQualifier() { return qualifier; }
    public byte[] getValue() { return value; }
    public long getTimestamp() { return timestamp; }
}
