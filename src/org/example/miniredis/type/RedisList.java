package org.example.miniredis.type;

import java.util.*;



/**
 * @author duoyian
 * @date 2026/3/4
 */
public class RedisList extends BaseRedisType<List<String>> {
    private final List<String> list;

    public RedisList() {
        super(BaseRedisType.RedisType.LIST, new ArrayList<>());
        this.list = this.getValue();
    }

    public RedisList(List<String> initial) {
        super(BaseRedisType.RedisType.LIST, new ArrayList<>(initial));
        this.list = this.getValue();
    }

    public int lpush(String... values) {
        for (int i = values.length - 1; i >= 0; i--) {
            list.add(0, values[i]);
        }
        return list.size();
    }

    public int rpush(String... values) {
        for (String value : values) {
            list.add(value);
        }
        return list.size();
    }

    public String lpop() {
        if (list.isEmpty()) return null;
        return list.remove(0);
    }

    public String rpop() {
        if (list.isEmpty()) return null;
        return list.remove(list.size() - 1);
    }

    public List<String> lrange(int start, int end) {
        int size = list.size();
        if (size == 0) return new ArrayList<>();

        // 处理负数索引
        if (start < 0) start = size + start;
        if (end < 0) end = size + end;

        if (start < 0) start = 0;
        if (end >= size) end = size - 1;
        if (start > end) return new ArrayList<>();

        return new ArrayList<>(list.subList(start, end + 1));
    }

    public String lindex(int index) {
        if (index < 0) index = list.size() + index;
        if (index < 0 || index >= list.size()) return null;
        return list.get(index);
    }

    public int llen() {
        return list.size();
    }
}
