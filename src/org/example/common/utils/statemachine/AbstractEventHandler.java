package org.example.common.utils.statemachine;

import org.example.common.collect.Lists;
import org.example.common.utils.JsonUtil;
import org.example.common.utils.statemachine.error.FailConditionException;
import org.example.common.utils.statemachine.error.SameConditionException;

import java.util.List;
import java.util.Optional;

/**
 * @author duoyian
 * @date 2026/3/17
 */
public abstract class AbstractEventHandler <SubEvent extends Event<Data, Trace>, Data, Trace>
        implements EventHandler<SubEvent, Data, Trace> {


    public void handle(SubEvent event) {
        this.prepareEvent(event);
        this.guard(event);
        this.handleInternal(event);
    }

    protected abstract SubEvent prepareEvent(SubEvent var1);

    protected List<String> acceptStatus(SubEvent event) {
        return Lists.newArrayList(new String[]{"*"});
    }

    protected abstract String eventStatus(SubEvent var1);

    protected void guard(SubEvent event) {
        String status = this.eventStatus(event);
        List<String> options = this.acceptStatus(event);
        if (options != null) {
            Optional<String> match = options.stream().filter((option) -> {
                return "*".equals(option) || option.equals(status);
            }).findFirst();
            if (!match.isPresent()) {
                if (this.isExpectedStatus(event, status)) {
                    throw new SameConditionException("[" + this.getClass().getSimpleName() + "]事件当前状态" + status + "已到达目标状态");
                }

                throw new FailConditionException("[" + this.getClass().getSimpleName() + "]事件当前状态" + status + "不符合事件前置状态" + JsonUtil.toJson(options));
            }
        }

    }

    protected boolean isExpectedStatus(SubEvent event, String status) {
        return false;
    }

    @Override
    public boolean requireLock() {
        return true;
    }

    protected abstract void handleInternal(SubEvent var1);
}
