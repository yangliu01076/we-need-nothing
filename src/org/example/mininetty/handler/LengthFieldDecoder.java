package org.example.mininetty.handler;

import org.example.mininetty.core.ChannelHandler;

import java.nio.ByteBuffer;

/**
 * @author duoyian
 * @date 2026/2/24
 */
public class LengthFieldDecoder implements ChannelHandler {

    // 累积缓冲区（处理拆包的关键：存“半截”数据）
    private final ByteBuffer cacheBuffer = ByteBuffer.allocate(1024);

    @Override
    public void channelRead(Object msg) {

    }

    @Override
    public void channelActive() {
        System.out.println("Channel Active");
    }
}
