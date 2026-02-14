package mininetty.handler;

import mininetty.core.ChannelHandler;
import mininetty.core.MiniChannel;

import java.io.IOException;

/**
 * @author duoyian
 * @date 2026/2/14
 */
public class SimpleChannelHandler implements ChannelHandler {

    private final MiniChannel channel;

    public SimpleChannelHandler(MiniChannel channel) {
        this.channel = channel;
    }

    @Override
    public void channelRead(Object msg) {
        System.out.println("Received: " + msg);
        try {
            // 加上前缀写回
            channel.write("Server Echo: " + msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelActive() {
        System.out.println("Channel Active");
    }
}
