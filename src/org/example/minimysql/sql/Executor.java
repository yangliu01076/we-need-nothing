package org.example.minimysql.sql;

import org.example.minimysql.engine.MemoryEngine;
import org.example.minimysql.sql.operator.IndexLookupOperator;
import org.example.minimysql.sql.operator.TableScanOperator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author duoyian
 * @date 2026/4/8
 */
public class Executor {
    private final MemoryEngine engine;

    public Executor(MemoryEngine engine) {
        this.engine = engine;
    }

    public Object[] execute(AST stmt) {
        if (stmt instanceof CreateTableStatement) {
            CreateTableStatement s = (CreateTableStatement) stmt;
            engine.createTable(s.tableName, s.columns);
            return new Object[]{"OK"};
        } else if (stmt instanceof InsertStatement) {
            InsertStatement s = (InsertStatement) stmt;

            // 1. 遍历所有值
            List<Object> realVals = new ArrayList<>();
            for (String val : s.values) {
                val = val.trim(); // 去掉首尾空格

                // 2. 简单的类型判断
                if (val.matches("-?\\d+")) {
                    // 如果是纯数字，转成 Long
                    realVals.add(Long.parseLong(val));
                } else {
                    // 否则（比如 'Alice'），去掉单引号，作为 String 保存
                    // 去掉首尾的单引号（如果有）
                    if (val.startsWith("'") && val.endsWith("'")) {
                        val = val.substring(1, val.length() - 1);
                    }
                    realVals.add(val);
                }
            }

            // 3. 插入数据
            engine.insert(s.tableName, realVals.toArray());
            return new Object[]{"OK"};
        } else if (stmt instanceof SelectStatement) {
            SelectStatement s = (SelectStatement) stmt;
            if (s.hasCondition()) {
                // 走索引
                Object[] row = engine.lookupByIndex(s.tableName, s.whereId);
                return new Object[]{new IndexLookupOperator(row)};
            } else {
                // 全表扫描
                return new Object[]{new TableScanOperator(s.tableName, engine)};
            }
        }
        throw new RuntimeException("Unknown statement type");
    }
}
