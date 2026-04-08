package org.example.minies.persistence;

/**
 * @author duoyian
 * @date 2026/4/8
 */
public interface PersistOperate {

    /**
     * 持久化
     */
    void persist();

    /**
     * 初始化存储
     */
    void initStorage();

    /**
     * 加载索引
     */
    void loadIndex();
}
