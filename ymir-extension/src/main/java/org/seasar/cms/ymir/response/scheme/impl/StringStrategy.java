package org.seasar.cms.ymir.response.scheme.impl;

import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.impl.SelfContainedResponse;
import org.seasar.cms.ymir.response.scheme.Strategy;

public class StringStrategy implements Strategy {

    public static final String SCHEME = "string";

    public String getScheme() {

        return SCHEME;
    }

    public Response constructResponse(String path, Object component) {

        return new SelfContainedResponse(path);
    }
}
