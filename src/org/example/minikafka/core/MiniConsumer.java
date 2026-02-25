package org.example.minikafka.core;

import java.util.List;

/**
 * @author duoyian
 * @date 2026/2/25
 */
public class MiniConsumer {
    private final MiniBroker miniBroker;
    private final String topic;
    private final int partitionId;
    private int currentOffset = 0;

    public MiniConsumer(MiniBroker miniBroker, String topic, int partitionId) {
        this.miniBroker = miniBroker;
        this.topic = topic;
        this.partitionId = partitionId;
    }

    public List<MiniMessage> poll(int maxNum) {
        List<MiniMessage> miniMessages = miniBroker.consume(topic, partitionId, currentOffset, maxNum);
        currentOffset += miniMessages.size();
        return miniMessages;
    }
}
