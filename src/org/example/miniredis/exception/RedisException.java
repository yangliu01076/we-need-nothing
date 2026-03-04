package org.example.miniredis.exception;

/**
 * @author duoyian
 * @date 2026/3/4
 */
public class RedisException extends RuntimeException {
    private static final long serialVersionUID = -4298905343873867154L;

    public RedisException(String message) {
        super(message);
    }
}
