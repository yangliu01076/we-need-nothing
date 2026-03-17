package org.example.common.utils.statemachine;

/**
 * @author duoyian
 * @date 2026/3/17
 */
public interface Event<Data, Trace> {
    Data getData();

    Trace getTrace();

    String getEventType();

    String getEventReferenceId();

    String getLock();
}
