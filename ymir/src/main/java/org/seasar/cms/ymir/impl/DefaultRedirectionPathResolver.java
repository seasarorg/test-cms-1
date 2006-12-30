package org.seasar.cms.ymir.impl;

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.RedirectionPathResolver;
import org.seasar.cms.ymir.util.ServletUtils;

public class DefaultRedirectionPathResolver implements RedirectionPathResolver {

    public String resolve(String path, Request request) {
        if (path == null) {
            return null;
        }

        String stripped = ServletUtils.stripParametersAndQueries(path);
        if (stripped.startsWith("/") || stripped.length() == 0) {
            path = request.getContextPath() + path;
            if ("".equals(ServletUtils.stripParametersAndQueries(path))) {
                path = "/" + path;
            }
        }

        return path;
    }
}
