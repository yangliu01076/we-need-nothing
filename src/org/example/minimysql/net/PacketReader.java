package org.example.minimysql.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author duoyian
 * @date 2026/4/8
 */
public class PacketReader {
    private static final int HEADER_LENGTH = 4;
    private final SocketChannel channel;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public PacketReader(SocketChannel channel) {
        this.channel = channel;
    }

    public byte[] readPacket() throws IOException {
        ByteBuffer temp = ByteBuffer.allocate(8192);
        int readCount = channel.read(temp);
        if (readCount == -1) return null;
        if (readCount > 0) buffer.write(temp.array(), 0, readCount);

        byte[] bufData = buffer.toByteArray();
        if (bufData.length < HEADER_LENGTH) return null;

        int packetLength = (bufData[0] & 0xff) | ((bufData[1] & 0xff) << 8) | ((bufData[2] & 0xff) << 16);
        if (bufData.length < HEADER_LENGTH + packetLength) return null;

        byte[] packet = new byte[packetLength];
        System.arraycopy(bufData, HEADER_LENGTH, packet, 0, packetLength);

        byte[] remaining = new byte[bufData.length - HEADER_LENGTH - packetLength];
        System.arraycopy(bufData, HEADER_LENGTH + packetLength, remaining, 0, remaining.length);
        buffer.reset();
        buffer.write(remaining);

        return packet;
    }
}
