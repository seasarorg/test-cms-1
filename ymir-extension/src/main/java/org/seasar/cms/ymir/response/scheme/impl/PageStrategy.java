package org.seasar.cms.ymir.response.scheme.impl;

import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.impl.ForwardResponse;
import org.seasar.cms.ymir.response.scheme.Strategy;

public class PageStrategy implements Strategy {

    public static final String SCHEME = "page";

    public String getScheme() {

        return SCHEME;
    }

    public Response constructResponse(String path, Object component) {

        return new ForwardResponse(path);
    }
}
