package mininetty.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * @author duoyian
 * @date 2026/2/14
 */
public class MiniChannel {
    private final SocketChannel socketChannel;
    private final ChannelPipeline pipeline;

    public MiniChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        this.pipeline = new ChannelPipeline();
    }

    public ChannelPipeline pipeline() {
        return pipeline;
    }

    // 读取数据
    public Object read() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int readCount = socketChannel.read(buffer);
        if (readCount > 0) {
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return new String(bytes);
        }
        return null;
    }

    // 写出数据
    public void write(Object msg) throws IOException {
        if (socketChannel == null) {
            return;
        }

        // 将字符串转为字节数组
        byte[] bytes = msg.toString().getBytes(StandardCharsets.UTF_8);
        // 包装成 ByteBuffer
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        // 写入通道
        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }
    }
}
