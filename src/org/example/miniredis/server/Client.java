package org.example.miniredis.server;

import org.example.miniredis.db.RedisDB;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;

/**
 * @author duoyian
 * @date 2026/3/4
 */
public class Client {
    private String id;
    private final SocketChannel channel;
    private RedisDB db;
    private String name;
    private long lastInteractionTime;
    private boolean blocked;
    private BlockingInfo blockingInfo;
    private boolean watching;
    private final List<String> watchedKeys;
    private final List<String[]> multiCommands;
    private int dbIndex;

    static class BlockingInfo {
        String[] keys;
        long timeout;
        String target;
        long startTime;
    }

    public Client(SocketChannel channel, RedisDB db) {
        this.id = UUID.randomUUID().toString();
        this.channel = channel;
        this.db = db;
        this.lastInteractionTime = System.currentTimeMillis();
        this.blocked = false;
        this.watching = false;
        this.watchedKeys = new ArrayList<>();
        this.multiCommands = new ArrayList<>();
        this.dbIndex = 0;
    }

    public void updateInteractionTime() {
        this.lastInteractionTime = System.currentTimeMillis();
    }

    // 修正：添加response参数的重载方法
    public void block(String[] keys, long timeout, String target) {
        this.blocked = true;
        this.blockingInfo = new BlockingInfo();
        this.blockingInfo.keys = keys;
        this.blockingInfo.timeout = timeout;
        this.blockingInfo.target = target;
        this.blockingInfo.startTime = System.currentTimeMillis();
    }

    // 添加不带参数的wakeUp方法（兼容原代码）
    public void wakeUp() {
        this.wakeUp("+OK\r\n");
    }

    public void wakeUp(String response) {
        this.blocked = false;
        this.blockingInfo = null;

        try {
            ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());
            channel.write(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isBlockTimeout() {
        if (blockingInfo == null) return false;
        return System.currentTimeMillis() - blockingInfo.startTime > blockingInfo.timeout;
    }

    public void watch(String[] keys) {
        this.watching = true;
        Collections.addAll(watchedKeys, keys);

        for (String key : keys) {
            db.addWatcher(key, this);
        }
    }

    public void unwatch() {
        for (String key : watchedKeys) {
            db.removeWatcher(key, this);
        }
        this.watching = false;
        this.watchedKeys.clear();
    }

    public boolean isDirty() {
        return watching && db.isKeysModified(watchedKeys);
    }

    public void multi() {
        this.multiCommands.clear();
    }

    public void queueCommand(String[] commandParts) {
        this.multiCommands.add(commandParts);
    }

    // 修正：exec方法需要处理客户端状态
    public List<String> exec() {
        List<String> responses = new ArrayList<>();

        // 检查乐观锁
        if (isDirty()) {
            discard();
            // 返回空列表表示事务失败
            return responses;
        }

        for (String[] command : multiCommands) {
            // 在实际实现中，这里应该执行命令
            // responses.add(db.executeCommand(command[0], Arrays.copyOfRange(command, 1, command.length)));
        }

        this.multiCommands.clear();
        unwatch(); // 执行后清除WATCH
        return responses;
    }

    public void discard() {
        this.multiCommands.clear();
        unwatch(); // 取消时也清除WATCH
    }

    public void select(int index) {
        this.dbIndex = index;
        // 实际实现中需要获取对应的数据库实例
        // this.db = server.getDB(index);
    }

    // Getters and Setters
    public String getId() { return id; }
    public SocketChannel getChannel() { return channel; }
    public boolean isBlocked() { return blocked; }
    public BlockingInfo getBlockingInfo() { return blockingInfo; }
    public boolean isWatching() { return watching; }
    public List<String> getWatchedKeys() { return watchedKeys; }
    public boolean isInTransaction() { return !multiCommands.isEmpty(); }
    public int getDbIndex() { return dbIndex; }
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }
    public long getLastInteractionTime() { return lastInteractionTime; }

    @Override
    public String toString() {
        try {
            return String.format("Client{id=%s, addr=%s, db=%d}",
                    id,
                    channel.socket().getRemoteSocketAddress(),
                    dbIndex);
        } catch (Exception e) {
            return String.format("Client{id=%s, db=%d}", id, dbIndex);
        }
    }
}
