package org.example.miniredis.server;

import javafx.util.Pair;
import org.example.mininetty.core.ChannelHandler;
import org.example.mininetty.core.MiniChannel;
import org.example.miniredis.protocol.RedisProtocol;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author duoyian
 * @date 2026/3/4
 */
public class MiniRedisChannelHandler implements ChannelHandler {

    private final ClientHandler<String> clientHandler;

    private final MiniChannel channel;

    public MiniRedisChannelHandler(MiniChannel channel, ClientHandler<String> clientHandler) {
        this.clientHandler = clientHandler;
        this.channel = channel;
    }

    @Override
    public void channelRead(Object msg) {
        String request = (String) msg;
        System.out.println("Server received request: " + request);
        Pair<String, String[]> stringPair = RedisProtocol.decodeNetty(request);
        String command = stringPair.getKey();
        String[] args = stringPair.getValue();
        System.out.println("Received command: " + command);
        System.out.println("Received arguments: " + Arrays.toString(args));
        String s = clientHandler.simpleExecuteCommand(command, args);
        try {
            channel.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void channelActive() {

    }
}
