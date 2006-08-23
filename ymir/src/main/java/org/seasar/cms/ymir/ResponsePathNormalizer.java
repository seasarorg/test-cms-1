package org.seasar.cms.ymir;

import javax.servlet.http.HttpServletRequest;

public interface ResponsePathNormalizer {

    String normalize(String path, boolean redirect, HttpServletRequest request);
}
