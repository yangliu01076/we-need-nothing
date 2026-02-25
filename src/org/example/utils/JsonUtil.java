package org.example.utils;

import java.lang.reflect.*;
import java.util.*;

/**
 * 纯手工实现的 JSON 解析工具类 (基于字符串解析和反射)
 * @author duoyian
 * @date 2026/2/25
 */
public class JsonUtil {
    // JSON 字符串
    private final String json;

    // 当前解析位置
    private int index;

    private JsonUtil(String json) {
        this.json = json;
        this.index = 0;
    }

    // ================= 公开 API =================

    public static <T> T parseObject(String jsonStr, Class<T> clazz) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return null;
        }
        try {
            JsonUtil parser = new JsonUtil(jsonStr);
            Object obj = parser.parseValue();
            return convertToObject(obj, clazz);
        } catch (Exception e) {
            throw new RuntimeException("JSON 解析失败: " + e.getMessage(), e);
        }
    }

    public static <T> List<T> parseList(String jsonStr, Class<T> clazz) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return null;
        }
        try {
            JsonUtil parser = new JsonUtil(jsonStr);
            Object obj = parser.parseValue();
            if (obj instanceof List) {
                List<?> rawList = (List<?>) obj;
                List<T> result = new ArrayList<>();
                for (Object item : rawList) {
                    result.add(convertToObject(item, clazz));
                }
                return result;
            }
            throw new RuntimeException("JSON 不是数组格式");
        } catch (Exception e) {
            throw new RuntimeException("JSON 解析 List 失败: " + e.getMessage(), e);
        }
    }

    public static Map<String, Object> parseMap(String jsonStr) {
        return parseObject(jsonStr, Map.class);
    }

    /**
     * 对象转 JSON 字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) return "null";
        return serialize(obj);
    }

    // ================= 核心解析逻辑 (递归下降) =================

    private static String serialize(Object obj) {
        // 1. 处理基本类型、包装类型、String
        if (obj instanceof String || obj instanceof Character) {
            return "\"" + escape(obj.toString()) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }

        // 2. 处理数组
        if (obj.getClass().isArray()) {
            StringBuilder sb = new StringBuilder("[");
            int len = Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                if (i > 0) sb.append(",");
                sb.append(serialize(Array.get(obj, i)));
            }
            sb.append("]");
            return sb.toString();
        }

        // 3. 处理集合 (List, Set等)
        if (obj instanceof Collection) {
            StringBuilder sb = new StringBuilder("[");
            Collection<?> coll = (Collection<?>) obj;
            boolean first = true;
            for (Object item : coll) {
                if (!first) sb.append(",");
                sb.append(serialize(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }

        // 4. 处理 Map
        if (obj instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            Map<?, ?> map = (Map<?, ?>) obj;
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(entry.getKey()).append("\":");
                sb.append(serialize(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }

        // 5. 处理普通 Java Bean (POJO)
        return serializeBean(obj);
    }

    /**
     * 利用反射处理普通对象
     */
    private static String serializeBean(Object obj) {
        StringBuilder sb = new StringBuilder("{");
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        boolean firstField = true;
        try {
            for (Field field : fields) {
                // 跳过静态字段
                if (Modifier.isStatic(field.getModifiers())) continue;

                // 暴力反射
                field.setAccessible(true);
                Object value = field.get(obj);

                if (!firstField) {
                    sb.append(",");
                }

                sb.append("\"").append(field.getName()).append("\":");
                // 递归处理字段值
                sb.append(serialize(value));

                firstField = false;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * 简单的特殊字符转义
     */
    private static String escape(String str) {
        if (str == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    // 简单处理控制字符
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    /**
     * 解析任意 JSON 值
     */
    private Object parseValue() {
        skipWhitespace();
        char c = getCurrentChar();

        if (c == '{') {
            return parseObject();
        } else if (c == '[') {
            return parseArray();
        } else if (c == '"') {
            return parseString();
        } else if (c == 't' || c == 'f') {
            return parseBoolean();
        } else if (c == 'n') {
            return parseNull();
        } else if (Character.isDigit(c) || c == '-') {
            return parseNumber();
        } else {
            throw new RuntimeException("非法字符: " + c);
        }
    }

    /**
     * 解析对象 { ... }
     */
    private Map<String, Object> parseObject() {
        // 保持顺序
        Map<String, Object> map = new LinkedHashMap<>();
        nextChar(); // 跳过 '{'
        skipWhitespace();

        while (getCurrentChar() != '}') {
            skipWhitespace();

            // 1. 解析 Key
            String key = parseString();

            skipWhitespace();
            // 2. 跳过 ':'
            if (getCurrentChar() != ':') throw new RuntimeException("缺少冒号");
            nextChar();

            // 3. 解析 Value
            Object value = parseValue();
            map.put(key, value);

            skipWhitespace();
            // 4. 检查逗号或结束
            char c = getCurrentChar();
            if (c == ',') {
                nextChar();
            } else if (c == '}') {
                break;
            } else {
                throw new RuntimeException("对象格式错误，期望 ',' 或 '}'");
            }
        }
        nextChar(); // 跳过 '}'
        return map;
    }

    /**
     * 解析数组 [ ... ]
     */
    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();
        nextChar(); // 跳过 '['
        skipWhitespace();

        while (getCurrentChar() != ']') {
            Object value = parseValue();
            list.add(value);
            skipWhitespace();

            char c = getCurrentChar();
            if (c == ',') {
                nextChar();
            } else if (c == ']') {
                break;
            } else {
                throw new RuntimeException("数组格式错误");
            }
        }
        nextChar(); // 跳过 ']'
        return list;
    }

    /**
     * 解析字符串 "..."
     */
    private String parseString() {
        nextChar(); // 跳过 '"'
        StringBuilder sb = new StringBuilder();
        while (getCurrentChar() != '"') {
            char c = getCurrentChar();
            // 处理转义字符
            if (c == '\\') {
                nextChar();
                char escape = getCurrentChar();
                switch (escape) {
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    default: sb.append(escape);
                }
            } else {
                sb.append(c);
            }
            nextChar();
        }
        nextChar(); // 跳过结束的 '"'
        return sb.toString();
    }

    /**
     * 解析布尔值
     */
    private Boolean parseBoolean() {
        if (json.startsWith("true", index)) {
            index += 4;
            return true;
        } else if (json.startsWith("false", index)) {
            index += 5;
            return false;
        }
        throw new RuntimeException("布尔值解析错误");
    }

    /**
     * 解析 Null
     */
    private Object parseNull() {
        if (json.startsWith("null", index)) {
            index += 4;
            return null;
        }
        throw new RuntimeException("Null 解析错误");
    }

    /**
     * 解析数字 (简化版，仅支持整数和小数)
     */
    private Number parseNumber() {
        StringBuilder sb = new StringBuilder();
        char c = getCurrentChar();
        if (c == '-') {
            sb.append(c);
            nextChar();
        }
        while (Character.isDigit(getCurrentChar()) || getCurrentChar() == '.' || getCurrentChar() == 'e' || getCurrentChar() == 'E' || getCurrentChar() == '+' || getCurrentChar() == '-') {
            sb.append(getCurrentChar());
            nextChar();
        }
        String numStr = sb.toString();
        if (numStr.contains(".")) {
            return Double.parseDouble(numStr);
        } else {
            long longVal = Long.parseLong(numStr);
            if (longVal <= Integer.MAX_VALUE && longVal >= Integer.MIN_VALUE) {
                return (int) longVal;
            }
            return longVal;
        }
    }

    // ================= 辅助方法 =================

    private void skipWhitespace() {
        while (index < json.length() && Character.isWhitespace(json.charAt(index))) {
            index++;
        }
    }

    private char getCurrentChar() {
        if (index >= json.length()) {
            throw new RuntimeException("JSON 字符串意外结束");
        }
        return json.charAt(index);
    }

    private void nextChar() {
        index++;
    }

    // ================= 反射转换逻辑 =================

    /**
     * 将解析出的 Map/List/基本类型 转换为目标类型的对象
     */
    @SuppressWarnings("unchecked")
    private static <T> T convertToObject(Object value, Class<T> clazz) {
        if (value == null) return null;

        // 1. 如果已经是目标类型 (或者 Map 转 Map, List 转 List)
        if (clazz.isInstance(value)) {
            return (T) value;
        }

        // 2. 基本类型转换
        if (clazz == int.class || clazz == Integer.class) {
            return (T) Integer.valueOf(value.toString());
        } else if (clazz == long.class || clazz == Long.class) {
            return (T) Long.valueOf(value.toString());
        } else if (clazz == double.class || clazz == Double.class) {
            return (T) Double.valueOf(value.toString());
        } else if (clazz == boolean.class || clazz == Boolean.class) {
            return (T) Boolean.valueOf(value.toString());
        } else if (clazz == String.class) {
            return (T) value.toString();
        }

        // 3. Map 转 Bean (核心反射逻辑)
        if (value instanceof Map && !clazz.equals(Map.class)) {
            Map<String, Object> map = (Map<String, Object>) value;
            try {
                T instance = clazz.getDeclaredConstructor().newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    Object mapValue = map.get(field.getName());
                    if (mapValue != null) {
                        // 递归转换字段类型（处理嵌套对象）
                        Object fieldValue = convertToObject(mapValue, field.getType());
                        field.set(instance, fieldValue);
                    }
                }
                return instance;
            } catch (Exception e) {
                throw new RuntimeException("实例化对象失败: " + clazz.getName(), e);
            }
        }

        throw new RuntimeException("无法将类型 " + value.getClass() + " 转换为 " + clazz);
    }
}
