package org.example.minikafka.core;

/**
 * @author duoyian
 * @date 2026/2/25
 */
public class MiniMessage {
    private final String key;
    private final String value;
    private final long timestamp;

    public MiniMessage(String key, String value) {
        this.key = key;
        this.value = value;
        this.timestamp = System.currentTimeMillis();
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
