package org.seasar.cms.ymir.response.constructor.impl;

import org.seasar.cms.ymir.response.constructor.ResponseConstructor;
import org.seasar.cms.ymir.response.scheme.StrategySelector;

abstract public class AbstractResponseConstructor implements
    ResponseConstructor {

    protected StrategySelector strategySelector_;

    public void setStrategySelector(StrategySelector strategySelector) {

        strategySelector_ = strategySelector;
    }
}
