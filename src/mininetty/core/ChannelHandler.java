package mininetty.core;

/**
 * @author duoyian
 * @date 2026/2/14
 */
public interface ChannelHandler {
    // 通道读就绪事件
    void channelRead(Object msg);

    // 通道建立连接事件
    void channelActive();
}
