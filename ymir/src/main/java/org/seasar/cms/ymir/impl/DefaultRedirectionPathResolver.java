package org.seasar.cms.ymir.impl;

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.RedirectionPathResolver;

public class DefaultRedirectionPathResolver implements RedirectionPathResolver {

    public String resolve(String path, Request request) {
        if (path == null) {
            return null;
        }

        if (path.startsWith("/") || path.length() == 0) {
            path = request.getContextPath() + path;
            if ("".equals(path)) {
                path = "/";
            }
        }

        return path;
    }
}
