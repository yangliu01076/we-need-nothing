package mininetty.core;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author duoyian
 * @date 2026/2/14
 */
public class EventLoopGroup {
    private final List<EventLoop> eventLoops = new ArrayList<>();
    private int nextIndex = 0;

    public EventLoopGroup(int nThreads) throws IOException {
        for (int i = 0; i < nThreads; i++) {
            // 默认创建 Worker
            eventLoops.add(new EventLoop(false));
        }
    }

    // 专门创建 Boss Group
    public static EventLoopGroup createBossGroup() throws IOException {
        // Boss 通常一个线程
        EventLoopGroup group = new EventLoopGroup(1);
        group.eventLoops.get(0).setBoss(true);
        return group;
    }

    // 选择一个 EventLoop (简单的轮询负载均衡)
    public EventLoop next() {
        return eventLoops.get(nextIndex++ % eventLoops.size());
    }

    // 注册 ServerSocket (仅 Boss 调用)
    public void register(ServerSocketChannel serverChannel) throws IOException {
        next().register(serverChannel);
    }
}
