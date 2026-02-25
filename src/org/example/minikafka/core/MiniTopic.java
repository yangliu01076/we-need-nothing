package org.example.minikafka.core;

import java.util.*;

/**
 * @author duoyian
 * @date 2026/2/25
 */
public class MiniTopic {
    private final String name;
    private final List<MiniPartition> miniPartitions = new ArrayList<>();

    public MiniTopic(String name, int numPartitions) {
        this.name = name;
        for (int i = 0; i < numPartitions; i++) {
            miniPartitions.add(new MiniPartition(name, i));
        }
    }

    public MiniPartition getPartition(int partitionId) {
        return miniPartitions.get(partitionId);
    }

    public int getNumPartitions() {
        return miniPartitions.size();
    }

    public String getName() {
        return name;
    }

    public List<MiniPartition> getPartitions() {
        return miniPartitions;
    }
}
