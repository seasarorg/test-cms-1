package org.seasar.cms.ymir.impl;

import org.seasar.cms.ymir.RedirectionPathResolver;
import org.seasar.cms.ymir.Request;

public class DefaultRedirectionPathResolver implements RedirectionPathResolver {

    public String resolve(String path, Request request) {
        if (path == null) {
            return null;
        }

        if (path.startsWith("/")) {
            path = request.getContextPath() + path;
        } else {
            // pathが「;a=b」などの場合。
            path = request.getContextPath() + "/" + path;
        }

        return path;
    }
}
