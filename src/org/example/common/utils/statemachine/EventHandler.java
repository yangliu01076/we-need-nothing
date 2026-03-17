package org.example.common.utils.statemachine;

/**
 * @author duoyian
 * @date 2026/3/17
 */
public interface EventHandler <SubEvent extends Event<Data, Trace>, Data, Trace> {
    void handle(SubEvent var1);

    boolean requireLock();

    String getType();
}
