package org.example.common.utils.statemachine.error;

/**
 * @author duoyian
 * @date 2026/3/17
 */
public class FailConditionException extends StateMachineException {
    private static final long serialVersionUID = -2747690358930961584L;

    public FailConditionException(String message) {
        super(message);
    }
}
