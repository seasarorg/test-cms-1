package org.seasar.cms.ymir.extension.creator.action;

import java.util.HashMap;
import java.util.Map;

public class ActionSelector<T> {

    private Map<Object, T> strategyMap_ = new HashMap<Object, T>();

    public ActionSelector<T> register(Condition condition, T action) {

        State classBound = condition.getClassBound();
        State classExists = condition.getClassExists();
        State templateExists = condition.getTemplateExists();
        String method = condition.getMethod();
        if (classBound == State.ANY) {
            register(new Condition(State.FALSE, classExists, templateExists,
                    method), action);
            register(new Condition(State.TRUE, classExists, templateExists,
                    method), action);
        } else if (classExists == State.ANY) {
            register(new Condition(classBound, State.FALSE, templateExists,
                    method), action);
            register(new Condition(classBound, State.TRUE, templateExists,
                    method), action);
        } else if (templateExists == State.ANY) {
            register(
                    new Condition(classBound, classExists, State.FALSE, method),
                    action);
            register(
                    new Condition(classBound, classExists, State.TRUE, method),
                    action);
        } else {
            strategyMap_.put(condition, action);
        }
        return this;
    }

    public ActionSelector<T> register(Object condition, T action) {

        strategyMap_.put(condition, action);
        return this;
    }

    public T getAction(Object condition) {

        return (T) strategyMap_.get(condition);
    }
}
