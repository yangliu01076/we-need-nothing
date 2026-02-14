/**
 * @author duoyian
 * @date 2026/2/12
 */
import java.util.*;

public class StateTransitionChecker {

    // 定义状态枚举
    public enum State { CREATED, PAID, SHIPPED, COMPLETED, CANCELLED }
    // 定义事件枚举
    public enum Event { PAY, SHIP,RECEIVE, CANCEL }

    // 核心数据结构：Map<源状态, Map<事件, 目标状态>>
    private static final Map<State, Map<Event, State>> TRANSITION_MAP = new HashMap<>();

    static {
        // 初始化流转规则
        // CREATED --PAY--> PAID
        addRule(State.CREATED, Event.PAY, State.PAID);
        // CREATED --CANCEL--> CANCELLED
        addRule(State.CREATED, Event.CANCEL, State.CANCELLED);
        // PAID --SHIP--> SHIPPED
        addRule(State.PAID, Event.SHIP, State.SHIPPED);
        // SHIPPED --RECEIVE--> COMPLETED
        addRule(State.SHIPPED, Event.RECEIVE, State.COMPLETED);
    }

    private static void addRule(State source, Event event, State target) {
        TRANSITION_MAP.computeIfAbsent(source, k -> new HashMap<>()).put(event, target);
    }

    /**
     * 校验并获取目标状态
     * @return 如果允许流转，返回目标状态；如果不允许，返回 null (或抛出异常)
     */
    public static State canTransition(State current, Event event) {
        if (!TRANSITION_MAP.containsKey(current)) {
            // 当前状态未定义任何出口
            throw new IllegalStateException("Invalid state: " + current);
        }
        State state = TRANSITION_MAP.get(current).get(event);
        if (state == null) {
            // 当前状态不允许该事件
            throw new IllegalStateException("Invalid event: " + event + " for state: " + current);
        }
        return state;
    }

    // 测试
    public static void main(String[] args) {
        System.out.println(canTransition(State.CREATED, Event.PAY));
        System.out.println(canTransition(State.PAID, Event.CANCEL));
        System.out.println(canTransition(State.SHIPPED, Event.PAY));
    }
}
