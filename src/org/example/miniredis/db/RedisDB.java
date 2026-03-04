package org.example.miniredis.db;

import org.example.common.utils.LRUCacheUtil;
import org.example.miniredis.server.Client;
import org.example.miniredis.type.BaseRedisType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author duoyian
 * @date 2026/3/4
 */
public class RedisDB<V> {
    private final int id;
    private final LRUCacheUtil<String, BaseRedisType<V>> dict;
    private final Map<String, Long> expires;

    /**
     * 阻塞操作管理
     */
    private final Map<String, List<Client>> blockingKeys;

    /**
     * 事务乐观锁
     */
    private final Map<String, List<Client>> watchedKeys;

    private final BaseRedisType.RedisType type;

    // 用于乐观锁的版本号
    private final Map<String, Integer> keyVersions;

    public RedisDB(int id, int capacity, BaseRedisType.RedisType type) {
        this.id = id;
        this.dict = new LRUCacheUtil<>(capacity);
        this.expires = new ConcurrentHashMap<>();
        this.blockingKeys = new ConcurrentHashMap<>();
        this.watchedKeys = new ConcurrentHashMap<>();
        this.keyVersions = new ConcurrentHashMap<>();
        this.type = type;
    }

    public BaseRedisType<V> set(String key, V value, Long expireMs) {
        BaseRedisType<V> obj = new BaseRedisType<>(type, value);
        BaseRedisType<V> old = dict.put(key, obj);

        // 更新版本号（用于乐观锁）
        int version = keyVersions.getOrDefault(key, 0) + 1;
        keyVersions.put(key, version);

        if (expireMs != null) {
            expires.put(key, System.currentTimeMillis() + expireMs);
        } else {
            expires.remove(key);
        }

        // 触发阻塞操作检查
        if (blockingKeys.containsKey(key)) {
            List<Client> clients = blockingKeys.remove(key);
            for (Client client : clients) {
                client.wakeUp("list data ready");
            }
        }

        // 触发WATCH通知
        if (watchedKeys.containsKey(key)) {
            List<Client> watchers = watchedKeys.get(key);
            for (Client client : watchers) {
                // 标记客户端的数据脏了
                // 实际Redis中会有更复杂的处理
            }
        }

        return old;
    }

    public int getId() {
        return id;
    }
    public String get(String key) {
        // 检查过期
        Long expireTime = expires.get(key);
        if (expireTime != null && System.currentTimeMillis() > expireTime) {
            dict.remove(key);
            expires.remove(key);
            keyVersions.remove(key);
            return null;
        }

        BaseRedisType<V> obj = dict.get(key);
        return obj != null ? obj.toString() : null;
    }

    // 获取 RedisObject（内部使用）
    public BaseRedisType<V> getObject(String key) {
        Long expireTime = expires.get(key);
        if (expireTime != null && System.currentTimeMillis() > expireTime) {
            dict.remove(key);
            expires.remove(key);
            keyVersions.remove(key);
            return null;
        }
        return dict.get(key);
    }

    public boolean delete(String key) {
        boolean removed = dict.remove(key);
        expires.remove(key);
        keyVersions.remove(key);
        return removed;
    }

    public boolean expire(String key, long milliseconds) {
        if (dict.containsKey(key)) {
            expires.put(key, System.currentTimeMillis() + milliseconds);
            return true;
        }
        return false;
    }

    public Set<String> keys(String pattern) {
        Set<String> result = new HashSet<>();
        for (String key : dict.keySet()) {
            if (matchPattern(key, pattern)) {
                result.add(key);
            }
        }
        return result;
    }

    private boolean matchPattern(String key, String pattern) {
        if ("*".equals(pattern)) {
            return true;
        }
        return key.equals(pattern);
    }

    // 添加WATCH支持
    public void addWatcher(String key, Client client) {
        watchedKeys.computeIfAbsent(key, k -> new ArrayList<>()).add(client);
    }

    public void removeWatcher(String key, Client client) {
        List<Client> watchers = watchedKeys.get(key);
        if (watchers != null) {
            watchers.remove(client);
        }
    }

    public boolean isKeysModified(List<String> keys) {
        // 简化版本：总是返回false
        // 实际Redis中会检查版本号
        return false;
    }

    // 添加阻塞客户端
    public void addBlockingClient(String key, Client client) {
        blockingKeys.computeIfAbsent(key, k -> new ArrayList<>()).add(client);
    }

    // 执行命令的方法
    public String executeCommand(String command, String[] args) {
        // 这里应该调用对应的命令处理器
        // 简化实现，直接返回错误
        return "-ERR Command not implemented\r\n";
    }
}
