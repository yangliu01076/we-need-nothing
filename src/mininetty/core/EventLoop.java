package mininetty.core;

import mininetty.handler.SimpleChannelHandler;
import minispring.core.MiniApplicationContext;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author duoyian
 * @date 2026/2/14
 */
public class EventLoop implements Runnable {
    private Selector selector;
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

    private Consumer<MiniChannel> channelInitializer;

    // 阈值：Netty 默认是 512
    private static final int EPOLL_BUSY_WAIT_THRESHOLD = 512;

    // 记录上次处理 IO 的时间
    private long ioTime = System.nanoTime();
    private int ioRatio = 100; // IO 时间比例

    // 用于检测 Bug 的计数器
    private long selectCnt = 0;

    public void setChannelInitializer(Consumer<MiniChannel> initializer) {
        this.channelInitializer = initializer;
    }

    public EventLoop(boolean isBoss) throws IOException {
        this.isBoss = isBoss;
        this.selector = Selector.open();
    }

    // 注册 ServerSocketChannel 到 Boss
    public void register(ServerSocketChannel serverChannel, MiniApplicationContext context) throws IOException {
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        this.context = context;
        startThread(); // 启动死循环
    }

    // 注册 SocketChannel 到 Worker（由 Boss 调用）
    public void register(SocketChannel socketChannel, MiniApplicationContext context) throws IOException {
        // 这里简单处理：如果当前线程不是 EventLoop 线程，应该提交任务队列
        // 为了代码极简，我们直接注册
        socketChannel.register(selector, SelectionKey.OP_READ);
        this.context = context;
        startThread();
    }

    private void startThread() {
        if (thread == null) {
            thread = new Thread(this);
            thread.setName(isBoss ? "Boss-EventLoop:" : "Worker-EventLoop:" + thread.getId());
            thread.start();
        }
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                // 1. 计算 select 阻塞的超时时间
                // Netty 会根据任务队列情况动态调整，这里简化为 1秒
                long timeout = 1000;

                // 2. 阻塞等待事件
                int selectedKeysCount = selector.select(timeout);

                // 3. 记录时间，判断是否空转
                long time = System.nanoTime();

                // 【核心】Bug 检测逻辑
                if (selectedKeysCount == 0) {
                    // 上次处理完的时间
                    long oldTime = ioTime;

                    // 如果 select() 立即返回（耗时极短），说明可能触发了空轮询
                    // 这里的 500000L 是经验值，表示小于 0.5ms 视为空转
                    if (time - oldTime < 500000L) {
                        // 增加空转计数
                        selectCnt++;
                    } else {
                        // 正常阻塞，重置计数
                        selectCnt = 0;
                    }
                    ioTime = time;
                } else {
                    // 有事件，重置计数
                    selectCnt = 0;
                }

                // 4. 【关键修复】如果空转次数超过阈值，重建 Selector
                if (selectCnt > EPOLL_BUSY_WAIT_THRESHOLD) {
                    System.err.println("Epoll BUG detected! Rebuilding selector...");
                    rebuildSelector();
                    // 重置计数
                    selectCnt = 0;
                    // 重建后，跳过本次循环，继续下一轮
                    continue;
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isAcceptable()) {
                        System.out.println("Acceptable event received:" + thread.getName());
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        System.out.println("Readable event received:" + thread.getName());
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
        if (this.channelInitializer != null) {
            System.out.println("Initializing channel with initializer");
            this.channelInitializer.accept(miniChannel);
        } else {
            // 添加一个默认处理器或用户定义的
            miniChannel.pipeline().addLast(new SimpleChannelHandler(miniChannel));
        }

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

    /**
     * 重建 Selector 的核心逻辑
     */
    private void rebuildSelector() {
        Selector newSelector;
        try {
            // 1. 打开新的 Selector
            newSelector = Selector.open();
        } catch (Exception e) {
            e.printStackTrace();
            return; // 重建失败，无法恢复
        }

        // 2. 遍历旧 Selector 上所有的 Key
        // 注意：需要同步，防止并发修改
        try {
            for (SelectionKey key : selector.keys()) {
                Object att = key.attachment();

                try {
                    // 3. 取消旧注册
                    if (!key.isValid() || key.channel().keyFor(newSelector) != null) {
                        continue;
                    }

                    int interestOps = key.interestOps();

                    // 4. 重新注册到新 Selector
                    // 注意：这里需要根据 channel 类型注册不同的 Ops
                    key.channel().register(newSelector, interestOps, att);

                } catch (Exception e) {
                    System.err.println("Failed to re-register channel: " + e.getMessage());
                    // 关掉这个坏掉的连接
                    try { key.channel().close(); } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 5. 替换引用
        Selector oldSelector = this.selector;
        this.selector = newSelector;

        // 6. 关闭旧 Selector
        try {
            oldSelector.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Selector rebuilt successfully.");
    }
}
