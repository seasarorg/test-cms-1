package org.seasar.cms.framework.response.scheme.impl;

import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.impl.PassthroughResponse;
import org.seasar.cms.framework.response.scheme.Strategy;

public class PassthroughStrategy implements Strategy {

    public static final String SCHEME = "passthrough";

    public String getScheme() {

        return SCHEME;
    }

    public Response constructResponse(String path, Object component) {

        return PassthroughResponse.INSTANCE;
    }
}
