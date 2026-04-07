package org.example.minies;

import org.example.minies.client.MiniEsClient;
import org.example.minies.core.MiniEsCore;

import java.util.List;

/**
 * @author duoyian
 * @date 2026/4/7
 */
public class MiniEsBootstrap {
    public static void main(String[] args) {
        start();
    }

    public static void start() {
        MiniEsCore miniEsCore = new MiniEsCore();
        miniEsCore.start();

        MiniEsClient miniEsClient = new MiniEsClient(miniEsCore);

        System.out.println("------ 1. 索引数据 ------");
        miniEsClient.index( "Java是一门面向对象编程语言");
        miniEsClient.index("Elasticsearch是基于Lucene的搜索引擎");
        miniEsClient.index("Lucene使用了倒排索引技术");
        miniEsClient.index("MiniES是一个简单的Java搜索引擎");
        miniEsClient.index("搜索引擎的核心是倒排索引");

        System.out.println("\n------ 2. 测试搜索 ------");

        printResult(miniEsClient, "Java");
        printResult(miniEsClient, "搜索"); // 中文二元分词：搜索 -> "搜索", "索" + "引擎"(不匹配), "引擎"
        printResult(miniEsClient, "引擎"); // 二元分词：引擎 -> "引擎"
        printResult(miniEsClient, "倒排"); // 二元分词：倒排 -> "倒排"
        printResult(miniEsClient, "Lucene");

        System.out.println("\n------ 3. 测试更新与删除 ------");
        System.out.println("更新文档 1...");
        miniEsClient.index(1, "Java语言非常流行"); // 更新 ID 1

        printResult(miniEsClient, "Java"); // 应该只返回新的内容
        printResult(miniEsClient, "语言");

        System.out.println("删除文档 2...");
        miniEsClient.deleteDocument(2);

        printResult(miniEsClient, "Lucene"); // 文档2包含Lucene，现在应该搜不到了（除非文档3也有）
    }

    private static void printResult(MiniEsClient es, String query) {
        System.out.print("Search '" + query + "': ");
        List<String> results = es.search(query);
        if (results.isEmpty()) {
            System.out.println("No results.");
        } else {
            System.out.println("Found " + results.size() + " docs.");
            for (String res : results) {
                System.out.println("  - " + res);
            }
        }
        System.out.println();
    }
}
