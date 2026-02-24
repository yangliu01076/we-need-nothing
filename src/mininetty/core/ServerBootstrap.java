package mininetty.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

/**
 * @author duoyian
 * @date 2026/2/14
 */
public class ServerBootstrap {
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    // 处理连接进来的 Socket
    private ChannelHandler childHandler;

    public ServerBootstrap group(EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        return this;
    }

    public ServerBootstrap childHandler(ChannelHandler handler) {
        this.childHandler = handler;
        return this;
    }

    public void bind(int port) throws IOException {
        // 1. 打开 ServerSocket
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(port));

        // 2. 注册到 Boss
        bossGroup.register(serverChannel,null);

        System.out.println("Server started on port: " + port);

        // 3. 这里的逻辑需要hack一下 EventLoop 的 handleAccept
        // 在真实 Netty 中，Boss 接受连接后，会通过 chooser 选一个 Worker，然后注册。
        // 在简易版中，我们需要修改 EventLoop 的 accept 逻辑，或者在这里做一个特殊的监听。

        // 为了让简易版跑通，我们在这里手动启动一个 "Boss" 的逻辑，
        // 实际上应该把下面这段逻辑封装进 BossEventLoop 里。
        // 这里为了演示完整性，我们假设 EventLoop 里的 handleAccept 能够感知到 workerGroup。
        // *修正策略*：我们在 EventLoop 中增加对 workerGroup 的引用，或者直接在 Bootstrap 里处理分发。

        // 下面使用一种更直观的“伪异步”方式来修正 EventLoop 的逻辑，使其能分发连接：
        // (请看下文对 EventLoop.handleAccept 的补充调用)
    }
}

