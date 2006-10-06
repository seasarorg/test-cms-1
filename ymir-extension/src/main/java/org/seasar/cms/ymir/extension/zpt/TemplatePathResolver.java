package org.seasar.cms.ymir.extension.zpt;

import javax.servlet.http.HttpServletRequest;

public interface TemplatePathResolver {

    String resolve(String path, HttpServletRequest request);

    String getLocalPath(String path, HttpServletRequest request);
}
