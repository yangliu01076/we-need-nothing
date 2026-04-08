package org.example.minies.core;

import org.example.minies.persistence.MiniEsPersist;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author duoyian
 * @date 2026/4/7
 */
public class MiniEsCore {

    // 核心数据结构
    // 正排索引: docId -> Document Content
    private final Map<Integer, String> docStore;
    // 倒排索引: term -> [docId, ...]
    private final Map<String, Set<Integer>> invertedIndex;
    // 删除标记: docId -> boolean (类似 ES 的 .del 文件)
    private final Set<Integer> deletedDocs;
    // ID 生成器
    private int currentDocId;

    private MiniEsPersist miniEsPersist;

    public void start() {
        this.miniEsPersist = new MiniEsPersist(this);
        miniEsPersist.initStorage();
        miniEsPersist.loadIndex();
    }

    /**
     * 添加或更新文档
     * 如果 docId 已存在，逻辑上标记旧文档删除，添加新文档（类 LSM 思想）
     */
    public synchronized int index(String content) {
        return index(currentDocId, content);
    }

    /**
     * 删除文档
     */
    public synchronized void deleteDocument(int docId) {
        if (docStore.containsKey(docId) && !deletedDocs.contains(docId)) {
            deletedDocs.add(docId);
            miniEsPersist.persist(); // 持久化删除标记
        }
    }

    /**
     * 搜索文档
     * 返回匹配的内容列表
     */
    public List<String> search(String query) {
        Set<String> queryTerms = analyze(query);
        if (queryTerms.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 获取所有 Term 对应的 DocID 集合
        List<Set<Integer>> docIdSets = new ArrayList<>();
        for (String term : queryTerms) {
            Set<Integer> ids = invertedIndex.get(term);
            if (ids == null || ids.isEmpty()) {
                return Collections.emptyList(); // 只要有一个词无结果，直接返回空
            }
            docIdSets.add(ids);
        }

        // 2. 求交集 (AND 查询)
        Set<Integer> resultIds = intersect(docIdSets);

        // 3. 过滤已删除的文档
        resultIds.removeAll(deletedDocs);

        // 4. 根据 ID 获取内容 (正排索引)
        return resultIds.stream()
                .sorted() // 按 ID 排序
                .map(docStore::get)
                .collect(Collectors.toList());
    }


    /**
     * 简单的中文分词器
     * 1. 去除标点
     * 2. 二元切分 - 简单但有效的中文分词策略
     */
    private Set<String> analyze(String text) {
        Set<String> terms = new HashSet<>();
        // 简单清洗：转小写，去除非中文/英文/数字
        text = text.toLowerCase().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", " ");

        if (text.trim().isEmpty()) {
            return terms;
        }

        // 英文按空格分
        String[] enWords = text.split("\\s+");
        for (String word : enWords) {
            if (!word.isEmpty()) {
                terms.add(word);
            }
        }

        // 中文按二元语法切分
        for (int i = 0; i < text.length() - 1; i++) {
            char c1 = text.charAt(i);
            char c2 = text.charAt(i + 1);
            // 简单的判断：只要包含中文就切分
            if (isChinese(c1) || isChinese(c2)) {
                terms.add(String.valueOf(c1) + c2);
            }
        }
        return terms;
    }

    public synchronized int index(int docId, String content) {
        // 如果指定 ID 已存在，先逻辑删除旧的
        if (docStore.containsKey(docId)) {
            deleteDocument(docId);
        } else {
            // 只有新 ID 才更新 generator
            if (docId >= currentDocId) {
                currentDocId = docId + 1;
            }
        }

        // 1. 存储原始文档
        docStore.put(docId, content);

        // 2. 分词并构建倒排索引
        Set<String> terms = analyze(content);
        for (String term : terms) {
            invertedIndex.computeIfAbsent(term, k -> new HashSet<>()).add(docId);
        }

        // 3. 异步/同步持久化 (这里为了简单做成同步，保证强一致性)
        miniEsPersist.persist();
        return docId;
    }

    private boolean isChinese(char c) {
        return c >= '\u4e00' && c <= '\u9fa5';
    }

    // 多个集合求交集
    private Set<Integer> intersect(List<Set<Integer>> sets) {
        if (sets.isEmpty()) {
            return new HashSet<>();
        }
        Set<Integer> result = new HashSet<>(sets.get(0));
        for (int i = 1; i < sets.size(); i++) {
            result.retainAll(sets.get(i));
        }
        return result;
    }



    public MiniEsCore() {
        this.docStore = new ConcurrentHashMap<>();
        this.invertedIndex = new ConcurrentHashMap<>();
        this.deletedDocs = new HashSet<>();
    }

    public Map<Integer, String> getDocStore() {
        return docStore;
    }

    public Map<String, Set<Integer>> getInvertedIndex() {
        return invertedIndex;
    }

    public Set<Integer> getDeletedDocs() {
        return deletedDocs;
    }

    public int getCurrentDocId() {
        return currentDocId;
    }

    public void setCurrentDocId(int currentDocId) {
        this.currentDocId = currentDocId;
    }
}
