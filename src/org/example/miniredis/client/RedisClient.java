package org.example.miniredis.client;

import java.io.*;
import java.net.Socket;

/**
 * @author duoyian
 * @date 2026/3/4
 */
public class RedisClient {
    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;

    public RedisClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
    }

    public String sendCommand(String command) throws IOException {
        writer.print(command);
        writer.flush();
        return reader.readLine();
    }

    // RESP协议格式发送命令
    public String sendRESPCommand(String... args) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(args.length).append("\r\n");
        for (String arg : args) {
            sb.append("$").append(arg.length()).append("\r\n");
            sb.append(arg).append("\r\n");
        }
        writer.print(sb.toString());
        writer.flush();

        return readResponse();
    }

    private String readResponse() throws IOException {
        StringBuilder response = new StringBuilder();
        char type = (char) reader.read();
        response.append(type);

        switch (type) {
            case '+':
            case '-':
            case ':':
                response.append(reader.readLine());
                break;

            case '$':
                int length = Integer.parseInt(reader.readLine());
                response.append(length).append("\r\n");
                if (length == -1) {
                    // 读取空行
                    response.append(reader.readLine());
                } else {
                    char[] buffer = new char[length];
                    reader.read(buffer, 0, length);
                    response.append(buffer);
                    reader.readLine(); // 读取结尾的\r\n
                }
                break;
        }

        return response.toString();
    }

    public void close() throws IOException {
        socket.close();
    }

    public static void main(String[] args) throws IOException {
        RedisClient client = new RedisClient("localhost", 6379);

        System.out.println("PING: " + client.sendRESPCommand("PING"));
        System.out.println("SET foo bar: " + client.sendRESPCommand("SET", "foo", "bar"));
        System.out.println("GET foo: " + client.sendRESPCommand("GET", "foo"));
        System.out.println("EXISTS foo: " + client.sendRESPCommand("EXISTS", "foo"));

        client.close();
    }
}
