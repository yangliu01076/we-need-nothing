package org.example.miniredis.type;

import java.util.Set;

/**
 * @author duoyian
 * @date 2026/3/4
 */
public class RedisSet extends BaseRedisType<Set<String>> {

    private final Set<String> set;
    public RedisSet(Set<String> value) {
        super(RedisType.SET, value);
        this.set = this.getValue();
    }
    public RedisSet() {
        super(RedisType.SET, new java.util.HashSet<>());
        this.set = this.getValue();
    }

    public int sadd(String... members) {
        int added = 0;
        for (String member : members) {
            if (set.add(member)) {
                added++;
            }
        }
        return added;
    }
}
