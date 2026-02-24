package org.example.mininetty.core;

import java.util.ArrayList;
import java.util.List;

/**
 * @author duoyian
 * @date 2026/2/14
 */
public class ChannelPipeline {
    private final List<ChannelHandler> handlers = new ArrayList<>();

    public ChannelPipeline addLast(ChannelHandler handler) {
        handlers.add(handler);
        return this;
    }

    public void fireChannelRead(Object msg) {
        for (ChannelHandler handler : handlers) {
            handler.channelRead(msg);
        }
    }

    public void fireChannelActive() {
        for (ChannelHandler handler : handlers) {
            handler.channelActive();
        }
    }
}
