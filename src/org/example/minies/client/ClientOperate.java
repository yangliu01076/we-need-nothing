package org.example.minies.client;

import java.util.List;

/**
 * @author duoyian
 * @date 2026/4/8
 */
public interface ClientOperate {
    /**
     * 添加或更新文档
     * 如果 docId 已存在，逻辑上标记旧文档删除，添加新文档（类 LSM 思想）
     * @param content 文档内容
     * @return docId
     */
    int index(String content);

    /**
     * 添加或更新文档
     * 如果 docId 已存在，逻辑上标记旧文档删除，添加新文档（类 LSM 思想）
     * @param docId 文档 id
     * @param content 文档内容
     * @return docId
     */
    int index(int docId, String content);

    /**
     * 删除文档
     * @param docId 文档 id
     */
    void deleteDocument(int docId);

    /**
     * 搜索文档
     * @param query 查询内容
     * @return 文档内容
     */
    List<String> search(String query);
}
