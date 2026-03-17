package org.example.common.utils.statemachine.error;

/**
 * @author duoyian
 * @date 2026/3/17
 */
public class StateMachineException extends RuntimeException {
    private static final long serialVersionUID = 2135372534373355893L;

    public StateMachineException(String message) {
        super(message);
    }
}
