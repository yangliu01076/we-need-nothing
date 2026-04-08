package org.example.minihbase.regionserver;

import org.example.minihbase.model.KeyValue;
import org.example.minihbase.model.Put;

import java.io.*;
import java.util.*;

/**
 * 简单的 RegionServer
 * 管理一个表（包含多个列族 Store）
 * @author duoyian
 * @date 2026/4/8
 */
public class HRegionServer {
    private final Map<String, Store> stores = new HashMap<>();

    public HRegionServer(String tableName, String[] families) throws IOException {
        String rootDataDir = "./minihbase-data";
        String tableDir = rootDataDir + "/" + tableName;
        for (String family : families) {
            stores.put(family, new Store(family, tableDir + "/" + family));
        }
    }

    public void put(Put put) throws IOException {
        for (KeyValue kv : put.getKeyValues()) {
            Store store = stores.get(kv.getFamily());
            if (store != null) {
                store.put(kv);
            } else {
                throw new IOException("Column Family " + kv.getFamily() + " does not exist");
            }
        }
    }

    public byte[] get(String rowKey, String family, String qualifier) throws IOException {
        Store store = stores.get(family);
        if (store == null) return null;
        KeyValue kv = store.get(rowKey, qualifier);
        return kv == null ? null : kv.getValue();
    }
}
