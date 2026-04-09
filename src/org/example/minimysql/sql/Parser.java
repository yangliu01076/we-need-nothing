package org.example.minimysql.sql;

import java.util.*;

/**
 * @author duoyian
 * @date 2026/4/8
 */
public class Parser {
    private String sql;
    private int pos = 0;

    public Parser(String sql) {
        this.sql = sql.trim();
    }

    public AST parse() {
        // 1. 预处理：去空格，去分号
        sql = sql.trim();
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }

        if (sql.isEmpty()) {
            throw new RuntimeException("Empty SQL");
        }

        String upper = sql.toUpperCase();

        // 2. 过滤系统查询
        if (upper.startsWith("SELECT") && (sql.contains("@@") || sql.contains("LIMIT"))) {
            throw new RuntimeException("Unsupported SQL: System query");
        }

        if (upper.startsWith("CREATE")) return parseCreate();
        if (upper.startsWith("INSERT")) return parseInsert();
        if (upper.startsWith("SELECT")) return parseSelect();

        throw new RuntimeException("Unsupported SQL: " + sql);
    }

    private AST parseCreate() {
        consume("CREATE");
        consume("TABLE");
        String name = token();

        if (name.isEmpty()) {
            throw new RuntimeException("Table name missing");
        }

        consumePunc("(");

        List<String> cols = new ArrayList<>();
        while (true) {
            skipWhitespace();
            // 使用新的 peek 方法，它会正确看到逗号
            if (peek(")")) {
                break;
            }

            String col = token();
            if (col.isEmpty()) break;
            cols.add(col);

            skipWhitespace();

            // 这里 peek 会看到逗号，所以进入 consumePunc
            if (!peek(")")) {
                consumePunc(",");
            }
        }

        consumePunc(")");
        return new CreateTableStatement(name, cols);
    }


    private AST parseInsert() {
        consume("INSERT");
        consume("INTO");
        String name = token();
        consume("VALUES");
        consumePunc("("); // 改这里
        List<String> vals = new ArrayList<>();
        while (!peek(")")) {
            vals.add(token());
            if (!peek(")")) consumePunc(","); // 改这里
        }
        consumePunc(")"); // 改这里
        return new InsertStatement(name, vals);
    }

    private AST parseSelect() {
        consume("SELECT");
        consume("*");
        consume("FROM");
        String name = token();
        Long id = null;
        if (peek("WHERE")) {
            consume("WHERE");
            String col = token();
            consumePunc("="); // 改这里
            String val = token();
            if (col.equalsIgnoreCase("id")) {
                id = Long.parseLong(val);
            }
        }
        return new SelectStatement(name, id);
    }


    private void consume(String s) {
        String t = token();
        if (!t.equalsIgnoreCase(s)) {
            throw new RuntimeException("Expected '" + s + "' but found '" + t + "'");
        }
    }

    private boolean peek(String s) {
        int old = pos;
        skipWhitespace(); // 1. 先跳过空格

        if (pos >= sql.length()) {
            pos = old;
            return false;
        }

        // 2. 直接看当前字符，不要调用 token()，否则会跳过逗号
        char c = sql.charAt(pos);

        pos = old; // 3. 回溯

        // 4. 比较
        return (c == s.charAt(0));
    }

    private void skipWhitespace() {
        while (pos < sql.length() && Character.isWhitespace(sql.charAt(pos))) {
            pos++;
        }
    }

    private void consumePunc(String punc) {
        skipWhitespace(); // 先跳过空格
        if (pos >= sql.length()) {
            throw new RuntimeException("Expected '" + punc + "' but found EOF");
        }
        char c = sql.charAt(pos);
        if (c != punc.charAt(0)) {
            // 这里打印一下，方便看看到底读到了啥
            throw new RuntimeException("Expected '" + punc + "' but found '" + c + "'");
        }
        pos++;
    }

    private String token() {
        skipWhitespace(); // token 内部本身就跳过了
        if (pos >= sql.length()) return "";
        StringBuilder sb = new StringBuilder();
        while (pos < sql.length() && !Character.isWhitespace(sql.charAt(pos)) && !isPunc(sql.charAt(pos))) {
            sb.append(sql.charAt(pos++));
        }
        return sb.toString();
    }

    private boolean isPunc(char c) {
        return c == '(' || c == ')' || c == ',';
    }
}
