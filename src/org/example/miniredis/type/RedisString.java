package org.example.miniredis.type;

/**
 * @author duoyian
 * @date 2026/3/4
 */
public class RedisString extends BaseRedisType<String> {

    // 简单写，直接使用String作为值类型
    private final String string;

    public RedisString(String value) {
        super(BaseRedisType.RedisType.STRING, value);
        this.string = this.getValue();
    }

    // 获取包装的RedisObject
    public BaseRedisType<String> getRedisObject() {
        return this;
    }

    public String get() {
        return string;
    }

    public int strlen() {
        return string.length();
    }
}
