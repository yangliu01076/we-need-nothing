/**
 * @author duoyian
 * @date 2025/9/14
 */
package org.example;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolTest {

    public static void main(String[] args) throws InterruptedException {

        // 64个核心线程，64最大线程，队列3
        ThreadPoolExecutor t2tDetermineExecutor = new ThreadPoolExecutor(
                64, 64, 5, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1),  // 问题点
                new ThreadFactoryImpl("test"),
                new DiscardWithLogPolicy());

        // 第一次提交64个任务
        System.out.println("第一次提交任务：");
        for (int i = 0; i < 64; i++) {

            System.out.println("等待后线程池状态: " +
                    "PoolSize=" + t2tDetermineExecutor.getPoolSize() +
                    ", Active=" + t2tDetermineExecutor.getActiveCount());
            t2tDetermineExecutor.submit(() -> {
//                System.out.println("任务执行线程ID: " + Thread.currentThread().getId());
                try {
                    Thread.sleep(100); // 模拟任务执行
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        // 64个核心线程被创建完成

        Thread.sleep(3000); // 等待任务完成，确保线程空闲


        // 重复10轮提交任务
        for(int j=0;j<10; j++){
            Thread.sleep(3000); // 等待任务完成，确保线程空闲
            System.out.println("第"+j+"批任务开始前: " +
                    "PoolSize=" + t2tDetermineExecutor.getPoolSize() +
                    ", Active=" + t2tDetermineExecutor.getActiveCount());
            // 每轮提交12个任务
            System.out.println("第"+ j +"次提交任务：");
            for (int i = 0; i < 12; i++) {
                System.out.println("等待后线程池状态: " +
                        "PoolSize=" + t2tDetermineExecutor.getPoolSize() +
                        ", Active=" + t2tDetermineExecutor.getActiveCount());
                t2tDetermineExecutor.submit(() -> {
//                System.out.println("任务执行线程ID: " + Thread.currentThread().getId());
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        t2tDetermineExecutor.shutdown(); // 关闭线程池
    }

    static class ThreadFactoryImpl implements ThreadFactory {
        private final AtomicInteger threadCount = new AtomicInteger(1);
        private final String namePrefix;

        public ThreadFactoryImpl(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, namePrefix + threadCount.getAndIncrement());
        }
    }

    static class DiscardWithLogPolicy implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            System.out.println("任务被拒绝: " + r.toString());
        }
    }
}
