package org.example.minimybatis.core;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * @author duoyian
 * @date 2026/2/27
 */
public class XmlConfigParser {

    public static Configuration parse(String configPath) {
        Configuration configuration = new Configuration();
        InputStream inputStream = null;
        try {
            Element root = getRootElement(configPath, inputStream);

            // 2. 解析 DataSource
            NodeList properties = root.getElementsByTagName("property");
            for (int i = 0; i < properties.getLength(); i++) {
                Node node = properties.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String name = element.getAttribute("name");
                    String value = element.getAttribute("value");
                    configuration.getDataSource().put(name, value);
                }
            }

            // 3. 解析 Mappers
            NodeList mappers = root.getElementsByTagName("mapper");
            for (int i = 0; i < mappers.getLength(); i++) {
                Element mapperElement = (Element) mappers.item(i);
                String resource = mapperElement.getAttribute("resource");
                // 递归解析 Mapper XML
                parseMapperXml(configuration, resource);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 记得关闭流
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                }
            }
        }
        return configuration;
    }

    private static void parseMapperXml(Configuration configuration, String resource) {
        InputStream inputStream = null;
        try {
            Element root = getRootElement(resource, inputStream);
            String namespace = root.getAttribute("namespace");

            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String id = element.getAttribute("id");
                    String resultType = element.getAttribute("resultType");

                    // 获取 SQL 内容（去除换行和空格）
                    String sql = element.getTextContent().trim().replaceAll("\\s+", " ");

                    MappedStatement mappedStatement = new MappedStatement();
                    mappedStatement.setId(id);
                    mappedStatement.setResultType(resultType);
                    mappedStatement.setSql(sql);

                    String key = namespace + "." + id;
                    configuration.getMappedStatementMap().put(key, mappedStatement);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static Element getRootElement(String path, InputStream inputStream) throws Exception {
        File file = new File(path);
        inputStream = Files.newInputStream(file.toPath());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(inputStream);
        return document.getDocumentElement();
    }
}
