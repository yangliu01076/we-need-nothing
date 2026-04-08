package org.example.minihbase.store;

import org.example.minihbase.model.KeyValue;

import java.io.*;
import java.util.List;

/**
 * 模拟 HFile：磁盘上的有序文件
 * 格式：RowKey|Family|Qualifier|Timestamp|Value
 * @author duoyian
 * @date 2026/4/8
 */
public class HFile implements Closeable {
    private final String filePath;
    private final long fileId;

    // 移除全局的 reader，改为在 get 方法中局部创建和关闭
    // private BufferedReader reader; // <--- 删除这行

    public HFile(String dataDir, long fileId) throws IOException {
        this.fileId = fileId;
        this.filePath = dataDir + File.separator + "data_" + fileId + ".hfile";
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        // 构造函数不再打开 reader，避免持有句柄
    }

    /**
     * 写入数据：创建流 -> 写入 -> 立即关闭
     */
    public void write(List<KeyValue> kvs) throws IOException {
        // 使用 try-with-resources 语句，确保流一定会关闭
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            for (KeyValue kv : kvs) {
                String line = String.format("%s|%s|%s|%d|%s",
                        kv.getRowKey(),
                        kv.getFamily(),
                        kv.getQualifier(),
                        kv.getTimestamp(),
                        new String(kv.getValue()));
                writer.write(line);
                writer.newLine();
            }
        }
        // 离开 try 块，writer 自动调用 close()，释放文件句柄
    }

    /**
     * 读取数据：创建流 -> 读取 -> 立即关闭
     */
    public KeyValue get(String rowKey, String family, String qualifier) throws IOException {
        // 每次读取时重新打开文件，读完马上关
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length < 5) continue;

                if (rowKey.equals(parts[0]) && family.equals(parts[1]) && qualifier.equals(parts[2])) {
                    return new KeyValue(parts[0], parts[1], parts[2], Long.parseLong(parts[3]), parts[4].getBytes());
                }
            }
        }
        // 离开 try 块，reader 自动调用 close()，释放文件句柄
        return null;
    }

    // resetReader 方法不再需要，因为我们每次 get 都会从头创建 reader
    // public void resetReader() throws IOException { ... } // <--- 删除这行

    @Override
    public void close() throws IOException {
        // HFile 本身不再持有需要关闭的流，这里可以为空
    }

    public long getFileId() { return fileId; }
}
