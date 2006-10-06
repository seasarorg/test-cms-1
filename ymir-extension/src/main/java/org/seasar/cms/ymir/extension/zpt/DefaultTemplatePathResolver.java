package org.seasar.cms.ymir.extension.zpt;

import javax.servlet.http.HttpServletRequest;

public class DefaultTemplatePathResolver implements TemplatePathResolver {

    public String getLocalPath(String path,
            HttpServletRequest httpServletRequest) {
        return path;
    }

    public String resolve(String path, HttpServletRequest request) {
        return path;
    }
}
