package org.seasar.cms.ymir.response.constructor.impl;

import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.response.VoidResponse;
import org.seasar.cms.ymir.response.constructor.ResponseConstructor;
import org.seasar.cms.ymir.response.scheme.Strategy;
import org.seasar.cms.ymir.response.scheme.StrategySelector;
import org.seasar.cms.ymir.response.scheme.impl.ForwardStrategy;

public class StringResponseConstructor implements ResponseConstructor {

    private StrategySelector strategySelector_;

    public Class getTargetClass() {

        return String.class;
    }

    public Response constructResponse(Object component, Object returnValue) {

        String string = (String) returnValue;
        if (string == null) {
            return VoidResponse.INSTANCE;
        }

        String scheme;
        String path;
        int colon = string.indexOf(':');
        if (colon < 0) {
            scheme = ForwardStrategy.SCHEME;
            path = string;
        } else {
            scheme = string.substring(0, colon);
            path = string.substring(colon + 1);
        }
        Strategy strategy = strategySelector_.getStrategy(scheme);
        if (strategy == null) {
            throw new RuntimeException("Unknown scheme '" + scheme
                    + "' is specified: " + string);
        }
        return strategy.constructResponse(path, component);
    }

    public void setStrategySelector(StrategySelector strategySelector) {

        strategySelector_ = strategySelector;
    }
}
