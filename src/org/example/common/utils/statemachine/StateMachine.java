package org.example.common.utils.statemachine;

/**
 * @author duoyian
 * @date 2026/3/17
 */
public interface StateMachine <SubEvent extends Event<Data, Trace>, Data, Trace> {
    void transition(SubEvent var1);
}
