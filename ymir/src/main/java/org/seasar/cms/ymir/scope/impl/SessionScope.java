package org.seasar.cms.ymir.scope.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.seasar.cms.ymir.scope.Scope;
import org.seasar.framework.container.S2Container;

public class SessionScope implements Scope {

    private S2Container container_;

    public Object getAttribute(String name) {
        HttpSession session = getSession(false);
        if (session == null) {
            return null;
        } else {
            return session.getAttribute(name);
        }
    }

    public void setAttribute(String name, Object value) {
        getSession().setAttribute(name, value);
    }

    HttpSession getSession() {
        return getSession(true);
    }

    HttpSession getSession(boolean create) {
        return ((HttpServletRequest) container_.getRoot().getExternalContext()
                .getRequest()).getSession(create);
    }

    public void setS2Container(S2Container container) {
        container_ = container;
    }
}
