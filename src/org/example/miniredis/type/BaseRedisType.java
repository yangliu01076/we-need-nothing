package org.example.miniredis.type;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author duoyian
 * @date 2026/3/4
 */
public class BaseRedisType<V> {

    public enum RedisType {
        STRING, LIST, HASH, SET, ZSET
    }

    private final RedisType type;
    private V value;
    private final AtomicInteger refCount = new AtomicInteger(1);

    public BaseRedisType(RedisType type, V value) {
        this.type = type;
        this.value = value;
    }

    public RedisType getType() {
        return type;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public void incrRefCount() {
        refCount.incrementAndGet();
    }

    public boolean decrRefCount() {
        return refCount.decrementAndGet() == 0;
    }

    @Override
    public String toString() {
        if (value == null) return null;
        return value.toString();
    }
}
