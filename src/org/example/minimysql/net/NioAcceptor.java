package org.example.minimysql.net;

import org.example.minimysql.engine.MemoryEngine;
import org.example.minimysql.sql.AST;
import org.example.minimysql.sql.Executor;
import org.example.minimysql.sql.Parser;
import org.example.minimysql.sql.operator.Operator;
import org.example.minimysql.util.ConnectionState;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author duoyian
 * @date 2026/4/8
 */
public class NioAcceptor {
    private final int port;
    private final MemoryEngine engine = new MemoryEngine();
    private final Executor executor = new Executor(engine);

    public NioAcceptor(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.configureBlocking(false);

        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server listening on " + port);

        while (true) {
            selector.select();
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                if (key.isAcceptable()) {
                    handleAccept(serverChannel, selector);
                } else if (key.isReadable()) {
                    handleRead(key);
                }
            }
        }
    }

    private void handleAccept(ServerSocketChannel server, Selector selector) throws IOException {
        SocketChannel client = server.accept();
        client.configureBlocking(false);

        // 发送握手包
        byte[] handshake = HandshakePacket.build();
        client.write(ByteBuffer.wrap(handshake));

        // 注册读事件，初始化状态
        client.register(selector, SelectionKey.OP_READ, new ConnectionState());
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ConnectionState ctx = (ConnectionState) key.attachment();

        try {
            PacketReader reader = new PacketReader(client);
            byte[] packetData = reader.readPacket();

            if (packetData == null) return;

            if (ctx.state == ConnectionState.State.AUTH) {
                client.write(ByteBuffer.wrap(new byte[]{7, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0}));
                ctx.state = ConnectionState.State.COMMAND;
            } else if (ctx.state == ConnectionState.State.COMMAND) {
                byte command = packetData[0];
                if (command == 0x03) {
                    String sql = new String(packetData, 1, packetData.length - 1);
                    System.out.println("[SQL] " + sql);

                    // --- 关键修改：捕获异常，不让程序崩 ---
                    try {
                        Parser parser = new Parser(sql);
                        AST stmt = parser.parse();
                        Object[] result = executor.execute(stmt);

                        if (result[0] instanceof String && result[0].equals("OK")) {
                            sendOk(client);
                        } else if (result[0] instanceof Operator) {
                            sendResultSet(client, (Operator) result[0]);
                        }
                    } catch (Exception e) {
                        System.err.println("执行出错，但保持连接: " + e.getMessage());
                        sendError(client, e.getMessage());
                    }
                    // --------------------------------------
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            key.cancel();
            client.close();
        }
    }

    // 记得加上这两个辅助方法
    private void sendOk(SocketChannel client) throws IOException {
        client.write(ByteBuffer.wrap(new byte[]{7, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0}));
    }

    private void sendError(SocketChannel client, String msg) throws IOException {
        byte[] msgBytes = msg.getBytes();
        byte[] packet = new byte[9 + msgBytes.length];
        packet[3] = 1; // Seq
        packet[4] = (byte) 0xff; // Error
        System.arraycopy(msgBytes, 0, packet, 9, msgBytes.length);
        // 更新包头长度
        int len = packet.length - 4;
        packet[0] = (byte) (len & 0xff);
        packet[1] = (byte) ((len >> 8) & 0xff);
        packet[2] = (byte) ((len >> 16) & 0xff);
        client.write(ByteBuffer.wrap(packet));
    }

    private void sendResultSet(SocketChannel client, Operator op) throws IOException {
        byte seq = 1;

        try {
            // 1. 发送列数 (1列)
            writeRaw(client, new byte[]{1}, seq++);

            // 2. 发送列定义 (极简版)
            // 结构: "def" [0] "schema" [0] "table" [0] "table" [0] "col" [0] "col" [0] ... 12 bytes attributes ...
            ByteArrayOutputStream cols = new ByteArrayOutputStream();
            cols.write("def".getBytes()); cols.write(0);
            cols.write("sys".getBytes()); cols.write(0); // schema
            cols.write("users".getBytes()); cols.write(0); // table
            cols.write("users".getBytes()); cols.write(0); // org table
            cols.write("result".getBytes()); cols.write(0); // col name
            cols.write("result".getBytes()); cols.write(0); // org col name

            // 12 bytes attributes
            cols.write(12); cols.write(0); // charset length
            cols.write(63); cols.write(0); // charset (utf8)
            cols.write(0); cols.write(0); cols.write(0); cols.write(0); // column length
            cols.write((byte)253); // type (VAR_STRING)
            cols.write(0); cols.write(0); // flags
            cols.write(0); // decimals

            writeRaw(client, cols.toByteArray(), seq++);

            // 3. EOF
            writeRaw(client, new byte[]{(byte)0xFE, 0, 0, 2, 0, 0, 0}, seq++);

            // 4. Rows
            while (op.hasNext()) {
                Object[] row = op.next();
                String val = Arrays.toString(row); // [1, Alice]
                byte[] data = val.getBytes();

                // Length Encoded String (len < 251)
                byte[] payload = new byte[1 + data.length];
                payload[0] = (byte) data.length;
                System.arraycopy(data, 0, payload, 1, data.length);

                writeRaw(client, payload, seq++);
            }

            // 5. EOF
            writeRaw(client, new byte[]{(byte)0xFE, 0, 0, 2, 0, 0, 0}, seq++);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeRaw(SocketChannel client, byte[] data, byte seq) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(4 + data.length);
        int len = data.length;
        buf.put((byte)(len & 0xFF));
        buf.put((byte)((len >> 8) & 0xFF));
        buf.put((byte)((len >> 16) & 0xFF));
        buf.put(seq);
        buf.put(data);
        buf.flip();
        client.write(buf);
    }
}
