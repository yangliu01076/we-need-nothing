package org.example.minimysql.net;

import java.nio.ByteBuffer;

/**
 * @author duoyian
 * @date 2026/4/8
 */
public class HandshakePacket {
    public static byte[] build() {
        ByteBuffer buf = ByteBuffer.allocate(256);

        // 1. Protocol Version
        buf.put((byte) 10);

        // 2. Server Version (伪装成 8.0.0，提高兼容性)
        byte[] version = "8.0.0-MiniMySQL".getBytes();
        buf.put(version);
        buf.put((byte) 0); // Null terminator

        // 3. Connection ID (4 bytes)
        buf.putInt(1);

        // 4. Auth-plugin data (Salt) - 第一部分 (8 bytes)
        buf.put(new byte[]{1,2,3,4,5,6,7,8});
        buf.put((byte) 0); // Filler

        // 5. Capabilities flags (Lower 2 bytes)
        buf.putShort((short) 0x0807);

        // 6. Character set (utf8mb4)
        buf.put((byte) 33);

        // 7. Status flags
        buf.putShort((short) 0);

        // 8. Capabilities flags (Upper 2 bytes)
        buf.putShort((short) 0x0000);

        // 9. Length of auth-plugin data (1 byte)
        buf.put((byte) 20);

        // 10. Reserved (10 bytes) - 必须全是 0
        buf.put(new byte[10]);

        // 11. Auth-plugin data (Salt) - 第二部分
        buf.put(new byte[]{9,10,11,12,13,14,15,16,17,18,19,20, 0});

        // 12. Auth-plugin name
        buf.put("mysql_native_password".getBytes());
        buf.put((byte) 0);

        // --- 组装最终数据包 ---
        byte[] payload = new byte[buf.position()];
        buf.flip();
        buf.get(payload);

        // 加上包头 (Length 3 bytes, Sequence 1 byte)
        ByteBuffer packet = ByteBuffer.allocate(4 + payload.length);
        int len = payload.length;

        // 写入长度 (Little Endian)
        packet.put((byte) (len & 0xff));
        packet.put((byte) ((len >> 8) & 0xff));
        packet.put((byte) ((len >> 16) & 0xff));

        // 写入序列号
        packet.put((byte) 0x00);

        // 写入载荷
        packet.put(payload);

        return packet.array();
    }
}
