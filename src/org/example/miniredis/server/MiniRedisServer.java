package org.example.miniredis.server;

import org.example.mininetty.core.EventLoopGroup;
import org.example.mininetty.handler.SimpleChannelHandler;
import org.example.miniredis.type.BaseRedisType;
import org.example.miniredis.db.RedisDB;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

/**
 * @author duoyian
 * @date 2026/3/4
 */
public class MiniRedisServer {
    private final ClientHandler<String> clientHandler;
    private final int port;

    public MiniRedisServer(int port) throws Exception {
        RedisDB<String> db = new RedisDB<>(0, 1000, BaseRedisType.RedisType.STRING);
        this.clientHandler = new ClientHandler<>(db);
        this.port = port;
    }

    public void start() throws IOException {
        // 1. 启动 Mini Netty
        EventLoopGroup bossGroup = EventLoopGroup.createBossGroup();
        // 2个 Worker 线程
        EventLoopGroup workerGroup = new EventLoopGroup(2);
        bossGroup.next().setWorkerGroup(workerGroup);
        bossGroup.setChannelInitializer(ch -> {
            System.out.println("Initializing pipeline for new connection...");
            // 添加业务 Handler
            ch.pipeline().addLast(new SimpleChannelHandler(ch))
                    .addLast(new MiniRedisChannelHandler(ch, clientHandler));
        });

        try {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(port));

            // 2. 注册 ServerSocket
            bossGroup.register(serverChannel,null);
            System.out.println("[Mini Dubbo] Server started on port: " + port);

            // 3. 这里是关键：我们需要修改 Mini Netty 的 EventLoop，让它在处理读事件时调用我们的 handler
            // 假设你的 MiniChannel 构造函数允许传入一个回调
            // 或者我们在 Spring 启动时初始化 Netty，并设置全局处理器

            // (伪代码示意：实际需要修改 EventLoop 的逻辑来支持外部 Handler 注入)
            // setupGlobalHandler(applicationContext);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 6379;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        MiniRedisServer server = new MiniRedisServer(port);
        server.start();
    }
}
