package org.example.minihbase.model;

import java.util.*;

/**
 * 写入操作封装
 * @author duoyian
 * @date 2026/4/8
 */
public class Put {
    private final String rowKey;
    private final long timestamp;
    private final List<KeyValue> kvs;

    public Put(String rowKey) {
        this.rowKey = rowKey;
        this.timestamp = System.currentTimeMillis();
        kvs = new ArrayList<>();
    }

    public Put add(String family, String qualifier, byte[] value) {
        this.kvs.add(new KeyValue(rowKey, family, qualifier, timestamp, value));
        return this;
    }

    public String getRowKey() { return rowKey; }
    public List<KeyValue> getKeyValues() { return kvs; }
}
