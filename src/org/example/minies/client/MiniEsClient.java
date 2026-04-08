package org.example.minies.client;

import org.example.minies.core.MiniEsCore;

import java.util.*;

/**
 * @author duoyian
 * @date 2026/4/7
 */
public class MiniEsClient implements ClientOperate {

    private MiniEsCore miniEsCore;

    public void setMiniEsCore(MiniEsCore miniEsCore) {
        this.miniEsCore = miniEsCore;
    }

    public MiniEsClient(){}

    public MiniEsClient(MiniEsCore miniEsCore){
        this.miniEsCore = miniEsCore;
    }

    /**
     * 添加或更新文档
     * 如果 docId 已存在，逻辑上标记旧文档删除，添加新文档（类 LSM 思想）
     */
    @Override
    public int index(String content) {
        return miniEsCore.index(content);
    }

    @Override
    public int index(int docId, String content) {
        return miniEsCore.index(docId, content);
    }

    /**
     * 删除文档
     */
    @Override
    public void deleteDocument(int docId) {
        miniEsCore.deleteDocument(docId);
    }

    /**
     * 搜索文档
     * 返回匹配的内容列表
     */
    @Override
    public List<String> search(String query) {
        return miniEsCore.search(query);
    }
}
