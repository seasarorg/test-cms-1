package org.seasar.cms.ymir.extension.creator.action;

import java.util.HashMap;
import java.util.Map;

public class UpdateActionSelector {

    private Map<Object, UpdateAction> strategyMap_ = new HashMap<Object, UpdateAction>();

    public UpdateActionSelector register(Object condition, UpdateAction strategy) {

        strategyMap_.put(condition, strategy);
        return this;
    }

    public UpdateAction getAction(Object condition) {

        return (UpdateAction) strategyMap_.get(condition);
    }
}
