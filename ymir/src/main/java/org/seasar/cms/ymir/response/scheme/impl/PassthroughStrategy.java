package org.seasar.cms.ymir.response.scheme.impl;

import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.response.PassthroughResponse;
import org.seasar.cms.ymir.response.scheme.Strategy;

public class PassthroughStrategy implements Strategy {

    public static final String SCHEME = "passthrough";

    public String getScheme() {

        return SCHEME;
    }

    public Response constructResponse(String path, Object component) {

        return PassthroughResponse.INSTANCE;
    }
}
