package org.example.minikafka.core;

/**
 * @author duoyian
 * @date 2026/2/25
 */
public class MiniProducer {
    private final MiniBroker miniBroker;

    public MiniProducer(MiniBroker miniBroker) {
        this.miniBroker = miniBroker;
    }

    public void send(String topic, String key, String value) {
        // 简单的分区策略：根据key的哈希值选择分区
        int partitionId = Math.abs(key.hashCode()) % miniBroker.getPartitionSize(topic, 0);
        miniBroker.send(topic, partitionId, key, value);
    }
}
