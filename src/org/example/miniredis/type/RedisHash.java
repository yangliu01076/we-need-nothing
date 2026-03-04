package org.example.miniredis.type;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author duoyian
 * @date 2026/3/4
 */
public class RedisHash extends BaseRedisType<Map<String, String>> {
    private final Map<String, String> hash;

    public RedisHash() {
        super(BaseRedisType.RedisType.HASH, new ConcurrentHashMap<>());
        this.hash = this.getValue();
    }

    public int hset(String field, String value) {
        String old = hash.put(field, value);
        return old == null ? 1 : 0;
    }

    public String hget(String field) {
        return hash.get(field);
    }

    public boolean hdel(String field) {
        return hash.remove(field) != null;
    }

    public boolean hexists(String field) {
        return hash.containsKey(field);
    }

    public Set<String> hkeys() {
        return new HashSet<>(hash.keySet());
    }

    public List<String> hvals() {
        return new ArrayList<>(hash.values());
    }

    public Map<String, String> hgetall() {
        return new HashMap<>(hash);
    }

    public int hlen() {
        return hash.size();
    }
}
