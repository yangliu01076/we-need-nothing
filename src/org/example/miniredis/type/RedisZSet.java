package org.example.miniredis.type;

import java.util.SortedSet;

/**
 * @author duoyian
 * @date 2026/3/4
 */
public class RedisZSet extends BaseRedisType<java.util.SortedSet<String>> {

    private final SortedSet<String> zset;

    public RedisZSet(SortedSet<String> value) {
        super(RedisType.ZSET, value);
        this.zset = this.getValue();
    }

    public RedisZSet() {
        super(RedisType.ZSET, new java.util.TreeSet<>());
        this.zset = this.getValue();
    }

    public int zadd(String member, double score) {
        return zset.add(member + "|" + score) ? 1 : 0;
    }
}
