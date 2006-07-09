package org.seasar.cms.framework.response.scheme.impl;

import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.impl.ForwardResponse;
import org.seasar.cms.framework.response.scheme.Strategy;

public class ForwardStrategy implements Strategy {

    private static final String SCHEME = "forward";

    public String getScheme() {

        return SCHEME;
    }

    public Response constructResponse(String path, Object component) {

        return new ForwardResponse(path);
    }
}
