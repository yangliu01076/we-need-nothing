package org.example.minies.persistence;

import org.example.minies.core.MiniEsCore;

import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author duoyian
 * @date 2026/4/7
 */
public class MiniEsPersist implements PersistOperate {

    private static final String INDEX_DIR = "mini_es_index";
    private static final String DOC_STORE_FILE = "docs.dat";
    private static final String INVERTED_INDEX_FILE = "index.dat";
    private static final String DELETED_SET_FILE = "deleted.dat";

    private final MiniEsCore miniEsCore;

    public MiniEsPersist(MiniEsCore miniEsCore) {
        this.miniEsCore = miniEsCore;
    }

    @Override
    public void persist() {
        long start = System.currentTimeMillis();
        // 1. 持久化正排索引
        writeMap(new File(INDEX_DIR, DOC_STORE_FILE), miniEsCore.getDocStore());
        // 2. 持久化倒排索引
        writeIndex(new File(INDEX_DIR, INVERTED_INDEX_FILE), miniEsCore.getInvertedIndex());
        // 3. 持久化删除集合
        writeSet(new File(INDEX_DIR, DELETED_SET_FILE), miniEsCore.getDeletedDocs());
        // 4. 持久化 ID
        writeId(new File(INDEX_DIR, "meta.id"), miniEsCore.getCurrentDocId());

        System.out.println("[Info] Index persisted to disk in " + (System.currentTimeMillis() - start) + "ms");
    }

    @Override
    public void initStorage() {
        File dir = new File(INDEX_DIR);
        if (!dir.exists()) {
            boolean createSuccess = dir.mkdirs();
        }
    }

    @Override
    public void loadIndex() {
        System.out.println("[Info] Loading index from disk...");
        readMap(new File(INDEX_DIR, DOC_STORE_FILE), miniEsCore.getDocStore());
        readIndex(new File(INDEX_DIR, INVERTED_INDEX_FILE), miniEsCore.getInvertedIndex());
        readSet(new File(INDEX_DIR, DELETED_SET_FILE), miniEsCore.getDeletedDocs());
        miniEsCore.setCurrentDocId(readId(new File(INDEX_DIR, "meta.id")));
        System.out.println("[Info] Load complete. Docs: " + miniEsCore.getDocStore().size()
                + ", Terms: " + miniEsCore.getInvertedIndex().size());
    }

    // --- 简单的文件序列化工具 ---

    private void writeMap(File file, Map<Integer, String> map) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<Integer, String> entry : map.entrySet()) {
                writer.write(entry.getKey() + "\t" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMap(File file, Map<Integer, String> map) {
        if (!file.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int idx = line.indexOf('\t');
                if (idx > 0) {
                    int id = Integer.parseInt(line.substring(0, idx));
                    String content = line.substring(idx + 1);
                    map.put(id, content);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeIndex(File file, Map<String, Set<Integer>> index) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<String, Set<Integer>> entry : index.entrySet()) {
                String ids = entry.getValue().stream().map(String::valueOf).collect(Collectors.joining(","));
                writer.write(entry.getKey() + "\t" + ids);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readIndex(File file, Map<String, Set<Integer>> index) {
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int idx = line.indexOf('\t');
                if (idx > 0) {
                    String term = line.substring(0, idx);
                    String[] ids = line.substring(idx + 1).split(",");
                    Set<Integer> idSet = new HashSet<>();
                    for (String id : ids) {
                        idSet.add(Integer.parseInt(id));
                    }
                    index.put(term, idSet);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeSet(File file, Set<Integer> set) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (Integer i : set) writer.println(i);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readSet(File file, Set<Integer> set) {
        if (!file.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                set.add(Integer.parseInt(line.trim()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeId(File file, int id) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.print(id);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private int readId(File file) {
        if (!file.exists()) {
            return 0;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return Integer.parseInt(reader.readLine());
        } catch (IOException e) { return 0; }
    }

}
