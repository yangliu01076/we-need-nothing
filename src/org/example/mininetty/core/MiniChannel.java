package org.example.mininetty.core;

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

    // 引入一个累积缓冲区，解决拆包/粘包问题
    private ByteBuffer cumulativeBuffer = ByteBuffer.allocate(1024);

    public MiniChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        this.pipeline = new ChannelPipeline();
    }

    public ChannelPipeline pipeline() {
        return pipeline;
    }

    // 读取数据
    // 注意：telnet 发送数据时，会自动在末尾加上 \n，所以这里我们只读取一行，而不是按长度读取
    public Object read() throws IOException {
        if (cumulativeBuffer.remaining() < 512) {
            expandBuffer();
        }

        int readCount = socketChannel.read(cumulativeBuffer);
        if (readCount == -1) {
            socketChannel.close();
            return null;
        }
        if (readCount == 0) {
            return null;
        }

        cumulativeBuffer.flip();

        // --- 修改开始：按行读取，而不是按长度读取 ---

        // 遍历 Buffer 找换行符
        int limit = cumulativeBuffer.limit();
        int endPosition = -1;

        for (int i = cumulativeBuffer.position(); i < limit; i++) {
            // 检查是否是换行符 (10) 或 回车 (13)
            byte b = cumulativeBuffer.get(i);
            if (b == '\n' || b == '\r') {
                endPosition = i;
                break;
            }
        }

        // 如果没找到换行符，说明还没发完（半包），compact 并等待
        if (endPosition == -1) {
            cumulativeBuffer.compact();
            return null;
        }

        // 如果找到了，计算消息长度
        int length = endPosition - cumulativeBuffer.position();

        // 读取消息体
        byte[] bodyBytes = new byte[length];
        cumulativeBuffer.get(bodyBytes);

        // 消耗掉换行符本身
        // 读取 \n 或 \r
        byte delimiter = cumulativeBuffer.get();
        if (delimiter == '\r') {
            // 如果是 \r，可能后面还有个 \n，也读掉
            if (cumulativeBuffer.hasRemaining() && cumulativeBuffer.get() != '\n') {
                // 没有继续读，指针回退
                cumulativeBuffer.position(cumulativeBuffer.position() - 1);
            }
        }

        // 打印并返回
        String msg = new String(bodyBytes, StandardCharsets.UTF_8);

        // 处理剩余数据
        if (cumulativeBuffer.hasRemaining()) {
            cumulativeBuffer.compact();
        } else {
            cumulativeBuffer.clear();
        }

        return msg;
    }

    public static void main(String[] args) {
        String s = "0020";
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        System.out.println(bytes.length);
    }

    // 写出数据
    public void write(Object msg) throws IOException {
        if (socketChannel == null || !socketChannel.isConnected()) {
            return;
        }

        // 1. 准备数据
        String string = msg.toString();
        string += "\n";
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);

        // 2. 构建 ByteBuffer：这里为了演示简单，用 Heap Buffer
        // 优化点：如果是写大文件，应该使用 ByteBuffer.allocateDirect
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        // 3. 循环写入（处理 TCP 缓冲区满的情况）
        int totalWritten = 0;
        while (buffer.hasRemaining()) {
            int written = socketChannel.write(buffer);
            if (written == 0) {
                // 如果返回 0，说明发送缓冲区满了
                // 在非阻塞模式下，应该注册 OP_WRITE 事件，等待 Selector 通知
                // 这里为了简化，我们直接让出 CPU 时间片（自旋等待）
                Thread.yield();
            }
            totalWritten += written;
        }
//        System.out.println("Wrote " + totalWritten + " bytes.");
    }

    /**
     * 动态扩容缓冲区
     */
    private void expandBuffer() {
        int newCapacity = cumulativeBuffer.capacity() * 2;
        System.out.println("Expanding buffer from " + cumulativeBuffer.capacity() + " to " + newCapacity);

        ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);
        // 将旧数据拷贝到新 Buffer
        cumulativeBuffer.flip();
        newBuffer.put(cumulativeBuffer);
        cumulativeBuffer = newBuffer;
    }
}
