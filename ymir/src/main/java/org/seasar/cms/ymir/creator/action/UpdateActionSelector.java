package org.seasar.cms.ymir.creator.action;

import java.util.HashMap;
import java.util.Map;


public class UpdateActionSelector {

    private Map strategyMap_ = new HashMap();

    public UpdateActionSelector register(Object condition,
        UpdateAction strategy) {

        strategyMap_.put(condition, strategy);
        return this;
    }

    public UpdateAction getAction(Object condition) {

        return (UpdateAction) strategyMap_.get(condition);
    }
}
