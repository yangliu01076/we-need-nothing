package org.example.common.utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author duoyian
 * @date 2026/3/4
 */
public class LRUCacheUtil<K, V> {
    private final int capacity;
    private final Map<K, Node<K, V>> cache;
    private final Node<K, V> head;
    private final Node<K, V> tail;
    private final Lock lock = new ReentrantLock();

    public LRUCacheUtil(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = capacity;

        this.cache = new HashMap<>(capacity);

        // 初始化双向链表哨兵节点
        this.head = new Node<>(null, null);
        this.tail = new Node<>(null, null);
        head.next = tail;
        tail.prev = head;
    }

    public V put(K key, V value) {
        lock.lock();
        try {
            Node<K, V> node = cache.get(key);
            if (node != null) {
                // 更新已存在的节点
                node.value = value;
                moveToHead(node);
            } else {
                // 创建新节点
                node = new Node<>(key, value);

                if (cache.size() >= capacity) {
                    // 移除最久未使用的节点
                    Node<K, V> removed = removeTail();
                    if (removed != null) {
                        cache.remove(removed.key);
                    }
                }

                // 添加到缓存和链表头部
                cache.put(key, node);
                addToHead(node);
            }
        } finally {
            lock.unlock();
        }
        return value;
    }

    public V get(K key) {
        lock.lock();
        try {
            Node<K, V> node = cache.get(key);
            if (node == null) {
                return null;
            }

            // 移动到头部表示最近使用
            moveToHead(node);
            return node.value;
        } finally {
            lock.unlock();
        }
    }

    public boolean remove(K key) {
        lock.lock();
        try {
            Node<K, V> node = cache.remove(key);
            if (node != null) {
                removeNode(node);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        return cache.size();
    }

    public void clear() {
        lock.lock();
        try {
            // 移除所有节点的引用，帮助垃圾回收
            cache.forEach((k, v) -> {
                v.value = null;
                removeNode(v);
            });
            cache.clear();
        } finally {
            lock.unlock();
        }
    }

    // 私有方法
    private void addToHead(Node<K, V> node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
        node.prev = null;
        node.next = null;
    }

    private void moveToHead(Node<K, V> node) {
        removeNode(node);
        addToHead(node);
    }

    private Node<K, V> removeTail() {
        // 链表为空，返回null
        if (tail.prev == head) {
            return null;
        }
        Node<K, V> last = tail.prev;
        removeNode(last);
        return last;
    }

    public Set<K> keySet() {
        lock.lock();
        try {
            return cache.keySet();
        } finally {
            lock.unlock();
        }
    }

    // 静态内部类
    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    public boolean containsKey(K key) {
        lock.lock();
        try {
            return cache.containsKey(key);
        } finally {
            lock.unlock();
        }
    }

    public Map<K, V> snapshot() {
        lock.lock();
        try {
            Map<K, V> result = new LinkedHashMap<>();
            // 从新到旧遍历
            Node<K, V> current = head.next;
            while (current != tail) {
                result.put(current.key, current.value);
                current = current.next;
            }
            return result;
        } finally {
            lock.unlock();
        }
    }
}
