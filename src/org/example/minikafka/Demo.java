package org.example.minikafka;

import org.example.minikafka.core.MiniBroker;
import org.example.minikafka.core.MiniConsumer;
import org.example.minikafka.core.MiniMessage;
import org.example.minikafka.core.MiniProducer;

import java.util.List;

/**
 * @author duoyian
 * @date 2026/2/25
 */
public class Demo {
    public static void main(String[] args) throws InterruptedException {
        // 创建Broker
        MiniBroker miniBroker = new MiniBroker();

        // 创建主题，3个分区
        miniBroker.createTopic("test-topic", 3);

        // 创建生产者
        MiniProducer miniProducer = new MiniProducer(miniBroker);

        // 发送消息
        for (int i = 0; i < 100; i++) {
            miniProducer.send("test-topic", "key-" + i, "message-" + i);
        }

        // 等待消息处理完成
        Thread.sleep(1000);

        // 创建消费者
        MiniConsumer miniConsumer = new MiniConsumer(miniBroker, "test-topic", 0);

        // 消费消息
        List<MiniMessage> miniMessages = miniConsumer.poll(10);
        for (MiniMessage msg : miniMessages) {
            System.out.println("Consumed: " + msg.getValue());
        }
    }
}
