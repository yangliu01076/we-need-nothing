package org.example.minihbase;

import org.example.minihbase.model.Put;
import org.example.minihbase.regionserver.HRegionServer;

import java.io.*;

/**
 * @author duoyian
 * @date 2026/4/8
 */
public class MiniHBaseDemo {
    public static void main(String[] args) throws IOException, InterruptedException {
        // 1. 启动 RegionServer，创建表 'user'，包含两个列族 'info', 'data'
        System.out.println("Starting MiniHBase...");
        HRegionServer rs = new HRegionServer("user", new String[]{"info", "data"});

        // 2. 写入数据 (模拟大量写入以触发 Flush)
        System.out.println("Start writing data...");
        for (int i = 0; i < 20000; i++) {
            Put put = new Put("row_" + i);
            put.add("info", "name", ("Alice_" + i).getBytes());
            put.add("info", "age", String.valueOf(20 + i % 10).getBytes());
            put.add("data", "json", ("{\"id\":" + i + "}").getBytes());
            rs.put(put);

            if (i % 5000 == 0) {
                System.out.println("Written " + i + " rows...");
            }
        }

        // 3. 读取数据
        System.out.println("Reading data...");
        // 读取内存中的数据（最后写入的）
        byte[] val1 = rs.get("row_19999", "info", "name");
        System.out.println("Row_19999, info:name -> " + new String(val1));

        // 读取可能已经刷到磁盘的数据
        byte[] val2 = rs.get("row_100", "data", "json");
        System.out.println("Row_100, data:json -> " + new String(val2));

        System.out.println("MiniHBase stopped.");
    }
}
