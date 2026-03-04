package org.example.miniredis.server;

import org.example.miniredis.db.RedisDB;
import org.example.miniredis.protocol.RedisProtocol;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;

/**
 * @author duoyian
 * @date 2026/3/4
 */
@SuppressWarnings("unchecked")
public class ClientHandler<V> {
    private SocketChannel channel;
    private final RedisDB<V> db;

    // 添加Client对象
    private Client client;
    private ByteBuffer buffer;
    private static final int BUFFER_SIZE = 1024;

    public ClientHandler(SocketChannel channel, RedisDB<V> db) {
        this.channel = channel;
        this.db = db;
        this.client = new Client(channel, db);
        this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
    }

    public ClientHandler(RedisDB<V> db) {
        this.db = db;
    }

    public void handleRequest() throws Exception {
        System.out.println("Handling request from client: " + client.getId());
        buffer.clear();
        int bytesRead = channel.read(buffer);

        if (bytesRead == -1) {
            channel.close();
            return;
        }

        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        System.out.println("Received data: ");
        System.out.println("Received data: " + new String(data));

        // 解析RESP协议
        Object request = RedisProtocol.decode(new String(data));

        if (request instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> commandParts = (List<String>) request;
            System.out.println("Received command: " + Arrays.toString(commandParts.toArray()));
            if (commandParts.isEmpty()) {
                return;
            }

            String command = commandParts.get(0).toUpperCase();
            String[] args = commandParts.subList(1, commandParts.size())
                    .toArray(new String[0]);

            // 执行命令
            String response = executeCommand(command, args);

            // 发送响应
            sendResponse(response);
        }
    }

    public String simpleExecuteCommand(String command, String[] args) {
        switch (command) {
            case "PING":
                return "+PONG\r\n";

            case "SET":
                if (args.length < 2) return "-ERR wrong number of arguments\r\n";
                Long expireMs = null;
                if (args.length > 2) {
                    for (int i = 2; i < args.length; i++) {
                        if ("PX".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                            expireMs = Long.parseLong(args[++i]);
                        }
                    }
                }
                db.set(args[0], (V) args[1], expireMs);
                return "+OK\r\n";

            case "GET":
                if (args.length != 1) return "-ERR wrong number of arguments\r\n";
                String value = db.get(args[0]);
                return value == null ? "$-1\r\n" :
                        "$" + value.length() + "\r\n" + value + "\r\n";

            case "DEL":
                if (args.length < 1) return "-ERR wrong number of arguments\r\n";
                int deleted = 0;
                for (String key : args) {
                    if (db.delete(key)) deleted++;
                }
                return ":" + deleted + "\r\n";

            case "EXISTS":
                if (args.length != 1) return "-ERR wrong number of arguments\r\n";
                boolean exists = db.get(args[0]) != null;
                return ":" + (exists ? 1 : 0) + "\r\n";

            case "KEYS":
                if (args.length != 1) return "-ERR wrong number of arguments\r\n";
                Set<String> keys = db.keys(args[0]);
                return RedisProtocol.encodeArray(keys.toArray(new String[0]));

            case "EXPIRE":
                if (args.length != 2) return "-ERR wrong number of arguments\r\n";
                long seconds = Long.parseLong(args[1]);
                boolean success = db.expire(args[0], seconds * 1000);
                return ":" + (success ? 1 : 0) + "\r\n";

            case "CLIENT":
                return handleClientCommand(args);

            case "MULTI":
                client.multi();
                return "+OK\r\n";

            case "EXEC":
                if (!client.isInTransaction()) {
                    return "-ERR EXEC without MULTI\r\n";
                }
                List<String> responses = client.exec();
                return formatTransactionResponses(responses);

            case "DISCARD":
                if (!client.isInTransaction()) {
                    return "-ERR DISCARD without MULTI\r\n";
                }
                client.discard();
                return "+OK\r\n";

            case "WATCH":
                client.watch(args);
                return "+OK\r\n";

            case "UNWATCH":
                client.unwatch();
                return "+OK\r\n";

            case "SELECT":
                if (args.length != 1) return "-ERR wrong number of arguments\r\n";
                int dbIndex = Integer.parseInt(args[0]);
                client.select(dbIndex);
                return "+OK\r\n";

            default:
                return "-ERR unknown command '" + command + "'\r\n";
        }
    }


    private String executeCommand(String command, String[] args) {
        try {
            // 更新客户端最后交互时间
            client.updateInteractionTime();

            switch (command) {
                case "PING":
                    return "+PONG\r\n";

                case "SET":
                    if (args.length < 2) return "-ERR wrong number of arguments\r\n";
                    Long expireMs = null;
                    if (args.length > 2) {
                        for (int i = 2; i < args.length; i++) {
                            if ("PX".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                                expireMs = Long.parseLong(args[++i]);
                            }
                        }
                    }
                    // 修正：传入String而不是RedisObject
                    db.set(args[0], (V) args[1], expireMs);
                    return "+OK\r\n";

                case "GET":
                    if (args.length != 1) return "-ERR wrong number of arguments\r\n";
                    String value = db.get(args[0]);
                    return value == null ? "$-1\r\n" :
                            "$" + value.length() + "\r\n" + value + "\r\n";

                case "DEL":
                    if (args.length < 1) return "-ERR wrong number of arguments\r\n";
                    int deleted = 0;
                    for (String key : args) {
                        if (db.delete(key)) deleted++;
                    }
                    return ":" + deleted + "\r\n";

                case "EXISTS":
                    if (args.length != 1) return "-ERR wrong number of arguments\r\n";
                    boolean exists = db.get(args[0]) != null;
                    return ":" + (exists ? 1 : 0) + "\r\n";

                case "KEYS":
                    if (args.length != 1) return "-ERR wrong number of arguments\r\n";
                    Set<String> keys = db.keys(args[0]);
                    return RedisProtocol.encodeArray(keys.toArray(new String[0]));

                case "EXPIRE":
                    if (args.length != 2) return "-ERR wrong number of arguments\r\n";
                    long seconds = Long.parseLong(args[1]);
                    boolean success = db.expire(args[0], seconds * 1000);
                    return ":" + (success ? 1 : 0) + "\r\n";

                case "CLIENT":
                    return handleClientCommand(args);

                case "MULTI":
                    client.multi();
                    return "+OK\r\n";

                case "EXEC":
                    if (!client.isInTransaction()) {
                        return "-ERR EXEC without MULTI\r\n";
                    }
                    List<String> responses = client.exec();
                    return formatTransactionResponses(responses);

                case "DISCARD":
                    if (!client.isInTransaction()) {
                        return "-ERR DISCARD without MULTI\r\n";
                    }
                    client.discard();
                    return "+OK\r\n";

                case "WATCH":
                    client.watch(args);
                    return "+OK\r\n";

                case "UNWATCH":
                    client.unwatch();
                    return "+OK\r\n";

                case "SELECT":
                    if (args.length != 1) return "-ERR wrong number of arguments\r\n";
                    int dbIndex = Integer.parseInt(args[0]);
                    client.select(dbIndex);
                    return "+OK\r\n";

                default:
                    return "-ERR unknown command '" + command + "'\r\n";
            }
        } catch (Exception e) {
            return "-ERR " + e.getMessage() + "\r\n";
        }
    }

    private String handleClientCommand(String[] args) {
        if (args.length == 0) {
            return "-ERR wrong number of arguments for 'client' command\r\n";
        }

        String subCommand = args[0].toUpperCase();
        switch (subCommand) {
            case "SETNAME":
                if (args.length != 2) {
                    return "-ERR wrong number of arguments for CLIENT SETNAME\r\n";
                }
                client.setName(args[1]);
                return "+OK\r\n";

            case "GETNAME":
                String name = client.getName();
                return name == null ? "$-1\r\n" :
                        "$" + name.length() + "\r\n" + name + "\r\n";

            case "ID":
                return ":" + client.getId() + "\r\n";

            default:
                return "-ERR Unknown subcommand or wrong number of arguments for 'client'\r\n";
        }
    }

    private String formatTransactionResponses(List<String> responses) {
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(responses.size()).append("\r\n");
        for (String response : responses) {
            // 解析响应并重新编码
            // 简化处理
            sb.append(response);
        }
        return sb.toString();
    }

    private void sendResponse(String response) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());
        channel.write(buffer);
    }
}
