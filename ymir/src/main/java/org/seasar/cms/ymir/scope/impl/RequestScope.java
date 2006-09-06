package org.seasar.cms.ymir.scope.impl;

import javax.servlet.http.HttpServletRequest;

import org.seasar.cms.ymir.scope.Scope;
import org.seasar.framework.container.S2Container;

public class RequestScope implements Scope {

    private S2Container container_;

    public Object getAttribute(String name) {
        return getRequest().getAttribute(name);
    }

    public void setAttribute(String name, Object value) {
        getRequest().setAttribute(name, value);
    }

    HttpServletRequest getRequest() {
        return (HttpServletRequest) container_.getRoot().getExternalContext()
                .getRequest();
    }

    public void setS2Container(S2Container container) {
        container_ = container;
    }
}
