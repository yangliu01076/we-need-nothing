package org.example.minihbase.regionserver;

import org.example.minihbase.model.KeyValue;
import org.example.minihbase.store.HFile;
import org.example.minihbase.store.MemStore;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * tore 对应一个列族
 * 管理 MemStore 和磁盘上的 HFiles
 * @author duoyian
 * @date 2026/4/8
 */
public class Store {
    private final String familyName;
    private final MemStore memStore;
    private final List<HFile> hFiles; // 磁盘文件列表
    private final String dataDir;
    private long fileIdCounter = 0;

    public Store(String familyName, String dataDir) {
        this.familyName = familyName;
        this.dataDir = dataDir;
        this.memStore = new MemStore();
        this.hFiles = new CopyOnWriteArrayList<>();

        // 确保目录存在
        boolean mkdirs = new File(dataDir).mkdirs();
    }

    public void put(KeyValue kv) throws IOException {
        memStore.put(kv);
        checkAndFlush();
    }

    public KeyValue get(String rowKey, String qualifier) throws IOException {
        // 1. 查 MemStore (最新数据)
        KeyValue memResult = memStore.get(rowKey, familyName, qualifier);
        if (memResult != null) return memResult;

        // 2. 查 HFiles (历史数据)
        // 倒序遍历，从最新的文件开始找
        for (int i = hFiles.size() - 1; i >= 0; i--) {
            HFile file = hFiles.get(i);
//            file.resetReader(); // 每次查找重置文件指针（效率低，仅为演示）
            // 直接调用 get，新的 HFile.get() 内部会自动处理文件打开和关闭
            KeyValue kv = file.get(rowKey, familyName, qualifier);
            if (kv != null) return kv;
        }
        return null;
    }

    private void checkAndFlush() throws IOException {
        if (memStore.shouldFlush()) {
            System.out.println("[Store-" + familyName + "] MemStore full, flushing to disk...");
            List<KeyValue> snapshot = memStore.getAll();
            memStore.clear();

            fileIdCounter++;
            HFile newFile = new HFile(dataDir, fileIdCounter);
            newFile.write(snapshot);
            hFiles.add(newFile);

            // TODO: 这里可以触发 Compaction 逻辑，将多个小文件合并
        }
    }
}
