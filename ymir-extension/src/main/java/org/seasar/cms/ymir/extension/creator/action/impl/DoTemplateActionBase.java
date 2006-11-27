package org.seasar.cms.ymir.extension.creator.action.impl;

import javax.servlet.http.HttpServletRequest;

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.extension.creator.SourceCreator;
import org.seasar.cms.ymir.extension.creator.Template;
import org.seasar.cms.ymir.extension.creator.action.UpdateAction;

abstract public class DoTemplateActionBase implements UpdateAction {

    private static final String PARAM_PATH = "path";

    protected SourceCreator sourceCreator_;

    public DoTemplateActionBase(SourceCreator sourceCreator) {
        sourceCreator_ = sourceCreator;
    }

    protected Template getTemplate(Request request) {
        String path = getPath(request);
        if (path == null) {
            return null;
        } else {
            return sourceCreator_.getTemplate(path);
        }
    }

    String getPath(Request request) {
        String path = request.getParameter(PARAM_PATH);
        if (path == null) {
            return null;
        }

        HttpServletRequest httpRequest = sourceCreator_.getHttpServletRequest();
        String requestURL = httpRequest.getRequestURL().toString();
        String webappRootURL = requestURL.substring(0, requestURL.length()
                - httpRequest.getRequestURI().length())
                + request.getContextPath();
        if (!path.startsWith(webappRootURL)) {
            return null;
        } else {
            return path.substring(webappRootURL.length());
        }
    }
}
