package mininetty.core;

import minidubbo.framework.RpcHandler;
import mininetty.handler.SimpleChannelHandler;
import minispring.core.MiniApplicationContext;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author duoyian
 * @date 2026/2/14
 */
public class EventLoop implements Runnable {
    private final Selector selector;
    private final ExecutorService executor;
    private Thread thread;

    private MiniApplicationContext context;

    public boolean isBoss() {
        return isBoss;
    }

    public void setBoss(boolean boss) {
        isBoss = boss;
    }

    // 标记是否为 Boss 模式（简化版：传入 threadCount 为 1 则是 Boss，否则是 Worker）
    // 实际 Netty 区分更明显，这里为了简化代码，我们用逻辑区分
    private boolean isBoss;

    public EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    public void setWorkerGroup(EventLoopGroup workerGroup) {
        this.workerGroup = workerGroup;
    }

    // 持有 WorkerGroup 的引用
    private EventLoopGroup workerGroup;

    public EventLoop(boolean isBoss) throws IOException {
        this.isBoss = isBoss;
        this.selector = Selector.open();
        // 一个 EventLoop 对应一个线程
        this.executor = Executors.newSingleThreadExecutor();
    }

    // 注册 ServerSocketChannel 到 Boss
    public void register(ServerSocketChannel serverChannel, MiniApplicationContext context) throws IOException {
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        this.context = context;
        startThread(); // 启动死循环
    }

    // 注册 SocketChannel 到 Worker（由 Boss 调用）
    public void register(SocketChannel socketChannel) throws IOException {
        // 这里简单处理：如果当前线程不是 EventLoop 线程，应该提交任务队列
        // 为了代码极简，我们直接注册
        socketChannel.register(selector, SelectionKey.OP_READ);
        startThread();
    }

    private void startThread() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                // 阻塞等待事件
                selector.select();

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel client = server.accept();
        client.configureBlocking(false);

        System.out.println("Accepted connection from: " + client.getRemoteAddress());

        // 【关键点】：将新连接注册到 Worker Group 中的一个 EventLoop
        EventLoop worker = workerGroup.next();

        // 创建 MiniChannel 并绑定 Handler
        MiniChannel miniChannel = new MiniChannel(client);
        // 添加一个默认处理器或用户定义的
        miniChannel.pipeline().addLast(new SimpleChannelHandler(miniChannel))
                .addLast(new RpcHandler(miniChannel,context));

        // 注册读事件，并将 miniChannel 作为 attachment 附加
        worker.register(client, miniChannel);
        // 注意：register 方法里调用了 socketChannel.register(selector, OP_READ)
        // 我们还需要把 miniChannel 附着到 SelectionKey 上，以便 handleRead 使用
        // 在简易版中，我们直接修改 worker.register 方法有点麻烦，
        // 最简单的方法是：在 worker.register 之后，获取 key 并 attach。

        // 实际上，为了简化，我们在 EventLoop 类里加个 register 方法重载：
        // public void register(SocketChannel sc, MiniChannel mc) { ... sc.register(selector, OP_READ, mc); ... }
    }

    private void handleRead(SelectionKey key) throws IOException {
        MiniChannel miniChannel = (MiniChannel) key.attachment();
        if (miniChannel != null) {
            Object msg = miniChannel.read();
            if (msg != null) {
                // 触发 Pipeline
                miniChannel.pipeline().fireChannelRead(msg);
            }
        }
    }

    public void register(SocketChannel sc, MiniChannel mc) throws IOException {
        sc.register(selector, SelectionKey.OP_READ, mc);
        startThread();
    }
}
