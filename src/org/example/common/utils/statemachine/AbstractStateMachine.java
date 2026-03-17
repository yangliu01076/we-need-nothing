package org.example.common.utils.statemachine;

import org.example.common.utils.statemachine.error.StateMachineException;

import java.util.Map;

/**
 * @author duoyian
 * @date 2026/3/17
 */
public abstract class AbstractStateMachine <SubEvent extends Event<Data, Trace>, Data, Trace>
        implements StateMachine<SubEvent, Data, Trace> {

    protected Map<String, EventHandler<SubEvent, Data, Trace>> handlers;

    protected abstract void initEventHandlers();

    @Override
    public void transition(SubEvent event) {
        EventHandler<SubEvent, Data, Trace> handler = this.handlers.get(event.getEventType());
        if (null == handler) {
            throw new StateMachineException(String.format("未提供事件类型处理器: %s", event.getEventType()));
        } else {
            this.preHandle(event);

            try {
                if (handler.requireLock()) {
                    this.transitionWithLock(event, handler);
                } else {
                    handler.handle(event);
                }
            } finally {
                this.postHandle(event);
            }

        }
    }

    protected abstract void preHandle(SubEvent var1);

    protected abstract void postHandle(SubEvent var1);

    protected abstract void transitionWithLock(SubEvent var1, EventHandler<SubEvent, Data, Trace> var2);

}
