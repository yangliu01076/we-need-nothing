package org.example.minimysql.util;

/**
 * @author duoyian
 * @date 2026/4/8
 */
public class ConnectionState {
    public enum State {
        AUTH, COMMAND
    }

    public State state = State.AUTH;
}
