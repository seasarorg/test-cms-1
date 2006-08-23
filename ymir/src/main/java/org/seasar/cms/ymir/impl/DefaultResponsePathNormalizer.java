package org.seasar.cms.ymir.impl;

import javax.servlet.http.HttpServletRequest;

import org.seasar.cms.ymir.ResponsePathNormalizer;
import org.seasar.cms.ymir.util.ServletUtils;

public class DefaultResponsePathNormalizer implements ResponsePathNormalizer {

    public String normalize(String path, boolean redirect,
            HttpServletRequest request) {
        if (path == null) {
            return null;
        }

        if (redirect) {
            if (path.startsWith("/") || path.length() == 0) {
                path = ServletUtils.getContextPath(request) + path;
                if ("".equals(path)) {
                    path = "/";
                }
            }
        }

        return path;
    }
}
