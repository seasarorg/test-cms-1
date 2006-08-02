package org.seasar.cms.ymir.extension.creator.action;

import java.util.HashMap;
import java.util.Map;

public class UpdateActionSelector {

    private Map<Object, UpdateAction> strategyMap_ = new HashMap<Object, UpdateAction>();

    public UpdateActionSelector register(Condition condition,
        UpdateAction updateAction) {

        State classBound = condition.getClassBound();
        State classExists = condition.getClassExists();
        State templateExists = condition.getTemplateExists();
        String method = condition.getMethod();
        if (classBound == State.ANY) {
            register(new Condition(State.FALSE, classExists, templateExists,
                method), updateAction);
            register(new Condition(State.TRUE, classExists, templateExists,
                method), updateAction);
        } else if (classExists == State.ANY) {
            register(new Condition(classBound, State.FALSE, templateExists,
                method), updateAction);
            register(new Condition(classBound, State.TRUE, templateExists,
                method), updateAction);
        } else if (templateExists == State.ANY) {
            register(
                new Condition(classBound, classExists, State.FALSE, method),
                updateAction);
            register(
                new Condition(classBound, classExists, State.TRUE, method),
                updateAction);
        } else {
            strategyMap_.put(condition, updateAction);
        }
        return this;
    }

    public UpdateActionSelector register(Object condition, UpdateAction strategy) {

        strategyMap_.put(condition, strategy);
        return this;
    }

    public UpdateAction getAction(Object condition) {

        return (UpdateAction) strategyMap_.get(condition);
    }
}
