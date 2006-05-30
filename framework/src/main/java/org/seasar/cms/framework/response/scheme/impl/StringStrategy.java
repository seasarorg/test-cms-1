package org.seasar.cms.framework.response.scheme.impl;

import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.impl.SelfContainedResponse;
import org.seasar.cms.framework.response.scheme.Strategy;

public class StringStrategy implements Strategy {

    public static final String SCHEME = "string";

    public String getScheme() {

        return SCHEME;
    }

    public Response constructResponse(String path) {

        return new SelfContainedResponse(path);
    }
}
