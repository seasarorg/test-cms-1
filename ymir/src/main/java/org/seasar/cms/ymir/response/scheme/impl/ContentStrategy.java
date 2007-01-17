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

        String contentType = SelfContainedResponse.DEFAULT_ASCII_CONTENTTYPE;
        String contentBody = path;
        ;
        int colon = path.indexOf(':');
        if (colon >= 0) {
            String ct = path.substring(0, colon);
            if (isValidContentType(ct)) {
                contentType = ct;
                contentBody = path.substring(colon + 1);
            }
        }
        return new SelfContainedResponse(contentBody, contentType);
    }

    boolean isValidContentType(String contentType) {
        int slash = contentType.indexOf('/');
        if (slash < 0) {
            return false;
        }
        for (int i = 0; i < slash; i++) {
            if (contentType.charAt(i) == ' ') {
                return false;
            }
        }
        return true;
    }
}
