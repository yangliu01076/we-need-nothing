package mininetty;

import mininetty.core.EventLoopGroup;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

/**
 * @author duoyian
 * @date 2026/2/14
 */
public class MiniNettyApplication {
    public static void main(String[] args) throws IOException {
        // 1. 创建线程组
        // 1个线程用于监听连接
        EventLoopGroup bossGroup = EventLoopGroup.createBossGroup();
        // 2个线程用于处理连接
        EventLoopGroup workerGroup = new EventLoopGroup(2);

        // 2. 配置 Boss，让其知道 WorkerGroup（用于分发连接）
        bossGroup.next().setWorkerGroup(workerGroup);

        // 3. 启动 Server
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(8080));

        // 注册到 Boss
        bossGroup.register(serverChannel, null);
        System.out.println("Mini Netty Server started on 8080...");

        // 4. (可选) 设置一个全局的 Handler 或者修改 MiniChannel 的构造来注入 Handler
        // 在这个简易版中，我们在 EventLoop.handleAccept 里硬编码了 new SimpleChannelHandler()
        // 实际 Netty 是通过 Bootstrap.childHandler 传进去的。
    }
}
