package org.example.common.utils.statemachine.error;

/**
 * @author duoyian
 * @date 2026/3/17
 */
public class SameConditionException extends StateMachineException {
    private static final long serialVersionUID = -1494640989299192057L;

    public SameConditionException(String message) {
        super(message);
    }
}
