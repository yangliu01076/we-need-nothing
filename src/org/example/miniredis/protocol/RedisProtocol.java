package org.example.miniredis.protocol;

import javafx.util.Pair;
import org.example.miniredis.exception.RedisException;

import java.util.*;

/**
 * @author duoyian
 * @date 2026/3/4
 */
public class RedisProtocol {
    public static Object decode(String data) {
        if (data == null || data.isEmpty()) return null;

        char type = data.charAt(0);
        String content = data.substring(1);

        switch (type) {
            case '+':  // 简单字符串
                return content.trim();

            case '-':  // 错误消息
                return new RedisException(content.trim());

            case ':':  // 整数
                return Long.parseLong(content.trim());

            case '$':  // 批量字符串
                int length = Integer.parseInt(content.split("\r\n")[0]);
                if (length == -1) return null;  // null bulk string
                int start = data.indexOf("\r\n") + 2;
                return data.substring(start, start + length);

            case '*':  // 数组
                int arrayLength = Integer.parseInt(content.split("\r\n")[0]);
                List<String> array = new ArrayList<>();
                String remaining = data.substring(data.indexOf("\r\n") + 2);

                for (int i = 0; i < arrayLength; i++) {
                    // 递归解析数组中的元素
                    String elementData = remaining;
                    Object element = decode(elementData);
                    if (element instanceof String) {
                        array.add((String) element);
                    }
                    // 移动到下一个元素
                    remaining = remaining.substring(getElementLength(elementData));
                }
                return array;

            default:
                throw new IllegalArgumentException("Invalid RESP type: " + type);
        }
    }

    public static Pair<String, String[]> decodeNetty(String data) {
        String[] split = data.split(" ");
        String command = split[0];
        String[] args = Arrays.copyOfRange(split, 1, split.length);
        return new Pair<>(command, args);
    }

    private static int getElementLength(String data) {
        // 计算一个完整RESP元素的长度
        char type = data.charAt(0);
        switch (type) {
            case '+':
            case '-':
            case ':':
                return data.indexOf("\r\n") + 2;

            case '$':
                int length = Integer.parseInt(data.substring(1, data.indexOf("\r\n")));
                if (length == -1) return data.indexOf("\r\n") + 2;
                return data.indexOf("\r\n") + 2 + length + 2;

            case '*':
                int arrayLen = Integer.parseInt(data.substring(1, data.indexOf("\r\n")));
                int total = data.indexOf("\r\n") + 2;
                String remaining = data.substring(total);
                for (int i = 0; i < arrayLen; i++) {
                    int elementLen = getElementLength(remaining);
                    total += elementLen;
                    remaining = remaining.substring(elementLen);
                }
                return total;

            default:
                return data.length();
        }
    }

    public static String encodeSimpleString(String str) {
        return "+" + str + "\r\n";
    }

    public static String encodeError(String err) {
        return "-ERR " + err + "\r\n";
    }

    public static String encodeInteger(long num) {
        return ":" + num + "\r\n";
    }

    public static String encodeBulkString(String str) {
        if (str == null) return "$-1\r\n";
        return "$" + str.length() + "\r\n" + str + "\r\n";
    }

    public static String encodeArray(String[] array) {
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(array.length).append("\r\n");
        for (String str : array) {
            sb.append(encodeBulkString(str));
        }
        return sb.toString();
    }
}
