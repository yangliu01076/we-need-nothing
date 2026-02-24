package org.example;

import minidubbo.framework.RpcServer;
import minispring.annotation.SpringBootApplication;
import minispring.core.MiniApplicationContext;

import java.io.IOException;

/**
 * @author duoyian
 * @date 2026/2/24
 */
@SpringBootApplication
public class MiniBootstrap {

    public static void main(String[] args) throws IOException {
        System.out.println("=== Starting Mini Spring ===");

        // 1. 启动 Mini Spring (扫描包，初始化 Bean)
        MiniApplicationContext context = new MiniApplicationContext(MiniBootstrap.class);
        System.out.println("=== Mini Spring Started ===");

        System.out.println("=== Starting Mini Dubbo & Netty ===");

        // 2. 启动 Mini Dubbo (传入 Spring 容器，启动 Netty)
        // 这里会开启线程监听 8080 端口
        new RpcServer(context, 8080).start();

        System.out.println("=== System Ready ===");
    }
}
