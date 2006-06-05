package org.seasar.cms.framework.response.constructor.impl;

import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.impl.VoidResponse;
import org.seasar.cms.framework.response.scheme.Strategy;
import org.seasar.cms.framework.response.scheme.impl.PageStrategy;

public class StringResponseConstructor extends AbstractResponseConstructor {

    public String getTargetClassName() {

        return String.class.getName();
    }

    public Response constructResponse(Object value) {

        String string = (String) value;
        if (string == null) {
            return VoidResponse.INSTANCE;
        }

        String scheme;
        String path;
        int colon = string.indexOf(':');
        if (colon < 0) {
            scheme = PageStrategy.SCHEME;
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
        return strategy.constructResponse(path);
    }
}