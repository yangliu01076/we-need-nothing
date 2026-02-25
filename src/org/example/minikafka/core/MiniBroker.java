package org.example.minikafka.core;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author duoyian
 * @date 2026/2/25
 */
public class MiniBroker {
    private final Map<String, MiniTopic> topics = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public void createTopic(String name, int numPartitions) {
        topics.putIfAbsent(name, new MiniTopic(name, numPartitions));
    }

    public void produce(String topic, int partitionId, MiniMessage miniMessage) {
        executor.submit(() -> {
            MiniTopic miniTopicObj = topics.get(topic);
            if (miniTopicObj == null) {
                throw new IllegalArgumentException("Topic not found");
            }
            MiniPartition miniPartition = miniTopicObj.getPartition(partitionId);
            miniPartition.append(miniMessage);
        });
    }

    public List<MiniMessage> consume(String topic, int partitionId, int offset, int maxNum) {
        MiniTopic miniTopicObj = topics.get(topic);
        if (miniTopicObj == null) {
            throw new IllegalArgumentException("Topic not found");
        }
        MiniPartition miniPartition = miniTopicObj.getPartition(partitionId);
        return miniPartition.getMessages(offset, maxNum);
    }

    public int getPartitionSize(String topic, int partitionId) {
        MiniTopic miniTopicObj = topics.get(topic);
        if (miniTopicObj == null) {
            throw new IllegalArgumentException("Topic not found");
        }
        return miniTopicObj.getPartitions().size();
    }

    public void send(String topic, int partitionId, String key, String value) {
        MiniMessage miniMessage = new MiniMessage(key, value);
        produce(topic, partitionId, miniMessage);
    }
}
