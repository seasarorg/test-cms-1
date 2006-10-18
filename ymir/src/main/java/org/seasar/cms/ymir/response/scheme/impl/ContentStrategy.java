package org.seasar.cms.ymir.response.scheme.impl;

import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.response.SelfContainedResponse;
import org.seasar.cms.ymir.response.scheme.Strategy;

public class ContentStrategy implements Strategy {

    public static final String SCHEME = "content";

    public String getScheme() {

        return SCHEME;
    }

    public Response constructResponse(String path, Object component) {

        String contentType;
        String contentBody;
        int colon = path.indexOf(':');
        if (colon >= 0) {
            contentType = path.substring(0, colon);
            contentBody = path.substring(colon + 1);
        } else {
            contentType = SelfContainedResponse.DEFAULT_ASCII_CONTENTTYPE;
            contentBody = path;
        }
        return new SelfContainedResponse(contentBody, contentType);
    }
}
