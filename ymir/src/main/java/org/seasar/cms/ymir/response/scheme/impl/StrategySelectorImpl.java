package org.seasar.cms.ymir.response.scheme.impl;

import java.util.HashMap;
import java.util.Map;

import org.seasar.cms.ymir.response.scheme.Strategy;
import org.seasar.cms.ymir.response.scheme.StrategySelector;

public class StrategySelectorImpl implements StrategySelector {

    private Map strategies_ = new HashMap();

    public Strategy getStrategy(String scheme) {

        Strategy strategy = (Strategy) strategies_.get(scheme);
        if (strategy != null) {
            return strategy;
        } else {
            throw new RuntimeException("Strategy does not exist for scheme: "
                + scheme);
        }
    }

    public void add(Strategy strategy) {

        strategies_.put(strategy.getScheme(), strategy);
    }

    public void setStrategies(Strategy[] strategies) {

        for (int i = 0; i < strategies.length; i++) {
            add(strategies[i]);
        }
    }
}
