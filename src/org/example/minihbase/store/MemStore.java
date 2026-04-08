package org.example.minihbase.store;

import org.example.minihbase.model.KeyValue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * 内存存储，基于 SkipList 实现
 * 线程安全
 * @author duoyian
 * @date 2026/4/8
 */
public class MemStore {
    private final ConcurrentSkipListMap<KeyValue, KeyValue> map = new ConcurrentSkipListMap<>();
    private long sizeInBytes = 0;
    private static final long FLUSH_THRESHOLD = 1024 * 1024 * 16; // 16MB 触发刷盘
//    private static final long FLUSH_THRESHOLD = 0;  // 测试 触发刷盘
    public void put(KeyValue kv) {
        map.put(kv, kv);
        sizeInBytes += kv.getRowKey().length() + kv.getValue().length; // 粗略估算
    }

    public KeyValue get(String rowKey, String family, String qualifier) {
        // 这是一个简化查找，实际 HBase 会构造一个特定的 KeyValue 对来 ceilingKey
        for (KeyValue kv : map.values()) {
            if (kv.getRowKey().equals(rowKey)
                    && kv.getFamily().equals(family)
                    && kv.getQualifier().equals(qualifier)) {
                return kv;
            }
        }
        return null;
    }

    public List<KeyValue> getAll() {
        return new ArrayList<>(map.values());
    }

    public boolean shouldFlush() {
        return sizeInBytes > FLUSH_THRESHOLD;
    }

    public void clear() {
        map.clear();
        sizeInBytes = 0;
    }
}
