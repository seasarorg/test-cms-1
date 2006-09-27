package org.seasar.cms.ymir.impl;

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.ResponsePathNormalizer;

public class DefaultResponsePathNormalizer implements ResponsePathNormalizer {

    public String normalize(String path, Request request) {
        return path;
    }

    public String normalizeForRedirection(String path, Request request) {
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
