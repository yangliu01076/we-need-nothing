package org.example.minikafka.core;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author duoyian
 * @date 2026/2/25
 */
public class MiniPartition {
    private final String topic;
    private final int partitionId;
    private final List<MiniMessage> miniMessages = new CopyOnWriteArrayList<>();

    public MiniPartition(String topic, int partitionId) {
        this.topic = topic;
        this.partitionId = partitionId;
    }

    public void append(MiniMessage miniMessage) {
        miniMessages.add(miniMessage);
    }

    public List<MiniMessage> getMessages(int offset, int maxNum) {
        int fromIndex = Math.min(offset, miniMessages.size());
        int toIndex = Math.min(offset + maxNum, miniMessages.size());
        return new ArrayList<>(miniMessages.subList(fromIndex, toIndex));
    }

    public int size() {
        return miniMessages.size();
    }

    public String getTopic() {
        return topic;
    }

    public int getPartitionId() {
        return partitionId;
    }

    public List<MiniMessage> getMessages() {
        return miniMessages;
    }
}
