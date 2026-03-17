package org.example.common.utils.statemachine.demo;

import org.example.common.utils.statemachine.Event;

/**
 * @author duoyian
 * @date 2026/3/17
 */
public class DemoEvent implements Event<DemoData, Object> {
    @Override
    public DemoData getData() {
        return null;
    }

    @Override
    public Object getTrace() {
        return null;
    }

    @Override
    public String getEventType() {
        return null;
    }

    @Override
    public String getEventReferenceId() {
        return null;
    }

    @Override
    public String getLock() {
        return null;
    }
}
